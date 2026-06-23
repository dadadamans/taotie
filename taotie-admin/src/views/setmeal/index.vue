<template>
  <div class="page">

    <div class="header">
      <div>
        <h2>套餐管理</h2>
        <p>管理餐厅套餐信息与价格</p>
      </div>
    </div>

    <div class="toolbar">
      <el-select v-model="selectedCategory" placeholder="分类" clearable style="width: 130px">
        <el-option v-for="item in categoryOptions" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
      <el-input v-model="searchName" placeholder="套餐名称" clearable style="width: 180px" @keyup.enter="handleSearch" />
      <el-select v-model="selectedStatus" placeholder="状态" clearable style="width: 100px">
        <el-option label="起售" :value="1" />
        <el-option label="停售" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>

      <div class="right">
        <el-button :disabled="selectedIds.length === 0" @click="handleBatchDelete">
          <el-icon><Delete /></el-icon>批量删除
        </el-button>
        <el-button type="primary" @click="goAdd">
          + 新增套餐
        </el-button>
      </div>
    </div>

    <div class="table-card">
      <el-table
        :data="list"
        v-loading="loading"
        stripe
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="45" />
        <el-table-column label="图片" width="70">
          <template #default="{ row }">
            <el-image
              v-if="row.image"
              :src="row.image"
              style="width: 44px; height: 44px; border-radius: 6px"
              fit="cover"
            >
              <template #error><span class="img-placeholder">无图</span></template>
            </el-image>
            <span v-else class="img-placeholder">无图</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="套餐名称" min-width="140" />
        <el-table-column prop="categoryName" label="分类" width="110" />
        <el-table-column label="价格" width="90">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" round>
              {{ row.status === 1 ? '起售' : '停售' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="140" show-overflow-tooltip />
        <el-table-column label="更新时间" width="170">
          <template #default="{ row }">
            {{ row.updateTime ? row.updateTime.replace('T', ' ').substring(0, 19) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-space>
              <el-button link type="primary" @click="goEdit(row.id)">编辑</el-button>
              <el-button
                link
                :type="row.status === 1 ? 'warning' : 'success'"
                @click="handleStatusChange(row)"
              >
                {{ row.status === 1 ? '停售' : '起售' }}
              </el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>

      <el-empty v-if="!loading && list.length === 0" description="暂无套餐数据" />
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSetmealPage, setSetmealStatus, deleteSetmeal, getCategoryList } from '@/api/shop'

const router = useRouter()

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const selectedIds = ref<number[]>([])

const searchName = ref('')
const selectedCategory = ref<number | undefined>(undefined)
const selectedStatus = ref<number | undefined>(undefined)
const categoryOptions = ref<{ id: number; name: string }[]>([])

const queryParams = reactive({
  page: 1,
  pageSize: 10,
  name: '',
  categoryId: undefined as number | undefined,
  status: undefined as number | undefined,
})

async function fetchData() {
  loading.value = true
  try {
    const params: Record<string, any> = { ...queryParams }
    if (params.categoryId === undefined || params.categoryId === null) delete params.categoryId
    if (params.status === undefined || params.status === null) delete params.status
    const res = await getSetmealPage(params)
    if (res.code === 1) {
      list.value = res.data.records
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  try {
    const res = await getCategoryList(2)
    if (res.code === 1) categoryOptions.value = res.data
  } catch {}
}

function handleSearch() {
  queryParams.name = searchName.value
  queryParams.categoryId = selectedCategory.value
  queryParams.status = selectedStatus.value
  queryParams.page = 1
  fetchData()
}

function handleReset() {
  searchName.value = ''
  selectedCategory.value = undefined
  selectedStatus.value = undefined
  queryParams.name = ''
  queryParams.categoryId = undefined
  queryParams.status = undefined
  queryParams.page = 1
  fetchData()
}

function handleSelectionChange(selection: any[]) {
  selectedIds.value = selection.map((item: any) => item.id)
}

async function handleBatchDelete() {
  if (selectedIds.value.length === 0) return
  try {
    await ElMessageBox.confirm(
      `确认删除选中的 ${selectedIds.value.length} 个套餐？`,
      '批量删除',
      { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' },
    )
    const res = await deleteSetmeal(selectedIds.value)
    if (res.code === 1) {
      ElMessage.success(`成功删除 ${selectedIds.value.length} 个套餐`)
      selectedIds.value = []
      if (list.value.length === 0 && queryParams.page > 1) queryParams.page--
      fetchData()
    }
  } catch {}
}

async function handleStatusChange(row: any) {
  try {
    await ElMessageBox.confirm(
      `确认${row.status === 1 ? '停售' : '起售'}该套餐？`,
      '提示',
      { type: 'warning' },
    )
    const newStatus = row.status === 1 ? 0 : 1
    const res = await setSetmealStatus(newStatus, row.id)
    if (res.code === 1) {
      row.status = newStatus
      ElMessage.success(newStatus === 1 ? '已起售' : '已停售')
    }
  } catch {}
}

function goEdit(id: number) { router.push(`/setmeal/form?id=${id}`) }
function goAdd() { router.push('/setmeal/form') }

function handleSizeChange(val: number) {
  queryParams.pageSize = val
  queryParams.page = 1
  fetchData()
}

function handleCurrentChange(val: number) {
  queryParams.page = val
  fetchData()
}

onMounted(() => {
  loadCategories()
  fetchData()
})
</script>

<style scoped lang="scss">
.page {
  padding: 20px;
  background: #f5f7fb;
  min-height: 100vh;
}

.header h2 {
  margin: 0;
}

.header p {
  margin: 4px 0 16px;
  color: #888;
}

.toolbar {
  display: flex;
  gap: 10px;
  align-items: center;
  background: #fff;
  padding: 12px;
  border-radius: 10px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.right {
  margin-left: auto;
  display: flex;
  gap: 8px;
}

.table-card {
  background: #fff;
  padding: 10px;
  border-radius: 10px;
}

.pagination {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.img-placeholder {
  display: inline-block;
  width: 44px;
  height: 44px;
  line-height: 44px;
  text-align: center;
  background: #f3f4f6;
  border-radius: 6px;
  font-size: 12px;
  color: #9ca3af;
}
</style>
