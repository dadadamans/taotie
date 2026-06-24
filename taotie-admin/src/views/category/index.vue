<template>
  <div class="page">

    <!-- 顶部标题 -->
    <div class="header">
      <div>
        <h2>分类管理</h2>
        <p>管理菜品与套餐分类</p>
      </div>
    </div>

    <!-- Tab + 新增 -->
    <div class="toolbar">
      <el-button-group>
        <el-button :type="activeType === 1 ? 'primary' : 'default'" @click="switchTab(1)">
          菜品分类
        </el-button>
        <el-button :type="activeType === 2 ? 'primary' : 'default'" @click="switchTab(2)">
          套餐分类
        </el-button>
      </el-button-group>

      <div class="right">
        <el-button type="primary" @click="openAdd">
          + 新增分类
        </el-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="toolbar">
      <el-input
        v-model="searchName"
        placeholder="搜索分类名称"
        clearable
        style="width: 240px"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 表格 -->
    <div class="table-card">
      <el-table :data="list" v-loading="loading" stripe border>

        <el-table-column prop="name" label="分类名称" min-width="160" />

        <el-table-column label="类型" width="120">
          <template #default="{ row }">
            {{ row.type === 1 ? '菜品分类' : '套餐分类' }}
          </template>
        </el-table-column>

        <el-table-column prop="sort" label="排序" width="80" />

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" round>
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ row.createTime ? row.createTime.replace('T', ' ').substring(0, 19) : '-' }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-space>
              <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="handleStatusChange(row)">
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
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

      <el-empty v-if="!loading && list.length === 0" description="暂无分类数据" />
    </div>
    </div>

    <CategoryForm
    v-model="drawerVisible"
    :id="editId"
    :type="activeType"
    :edit-data="editRow"
    @success="fetchData"
  />

  <ConfirmDialog
    v-model="statusConfirmVisible"
    type="warning"
    :title="`确认${statusConfirmRow?.status === 1 ? '禁用' : '启用'}该分类？`"
    confirm-text="确认"
    @confirm="confirmStatusChange"
  />

  <ConfirmDialog
    v-model="deleteConfirmVisible"
    type="danger"
    title="确认删除该分类？"
    description="删除后不可恢复"
    confirm-text="确认删除"
    @confirm="confirmDelete"
  />
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getCategoryPage,
  deleteCategory,
  setCategoryStatus,
} from '@/api/shop'
import CategoryForm from './form.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const searchName = ref('')
const activeType = ref(1)

const queryParams = reactive({
  page: 1,
  pageSize: 10,
  type: 1,
  name: '',
})

const drawerVisible = ref(false)
const editId = ref<number | null>(null)
const editRow = ref<any>(null)
const deleteConfirmVisible = ref(false)
const deleteId = ref<number>(0)
const statusConfirmVisible = ref(false)
const statusConfirmRow = ref<any>(null)

async function fetchData() {
  loading.value = true
  try {
    queryParams.type = activeType.value
    const res = await getCategoryPage({ ...queryParams })
    if (res.code === 1) {
      list.value = res.data.records
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

function switchTab(type: number) {
  if (type === activeType.value) return
  activeType.value = type
  queryParams.page = 1
  queryParams.name = ''
  searchName.value = ''
  fetchData()
}

function handleSearch() {
  queryParams.name = searchName.value
  queryParams.page = 1
  fetchData()
}

function handleReset() {
  searchName.value = ''
  queryParams.name = ''
  queryParams.page = 1
  fetchData()
}

function handleSizeChange(val: number) {
  queryParams.pageSize = val
  queryParams.page = 1
  fetchData()
}

function handleCurrentChange(val: number) {
  queryParams.page = val
  fetchData()
}

function handleStatusChange(row: any) {
  statusConfirmRow.value = row
  statusConfirmVisible.value = true
}

async function confirmStatusChange() {
  const newStatus = statusConfirmRow.value.status === 1 ? 0 : 1
  const res = await setCategoryStatus(newStatus, statusConfirmRow.value.id)
  if (res.code === 1) {
    statusConfirmRow.value.status = newStatus
    ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
  }
}

function openAdd() {
  editId.value = null
  editRow.value = null
  drawerVisible.value = true
}

function openEdit(row: any) {
  editId.value = row.id
  editRow.value = row
  drawerVisible.value = true
}

function handleDelete(id: number) {
  deleteId.value = id
  deleteConfirmVisible.value = true
}

async function confirmDelete() {
  const res = await deleteCategory(deleteId.value)
  if (res.code === 1) {
    ElMessage.success('删除成功')
    if (list.value.length === 1 && queryParams.page > 1) queryParams.page--
    fetchData()
  }
}

onMounted(fetchData)
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
}

.right {
  margin-left: auto;
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
</style>
