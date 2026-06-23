<template>
  <div class="page">

    <!-- 顶部标题 -->
    <div class="header">
      <div>
        <h2>员工管理</h2>
        <p>管理系统员工账号、状态与权限</p>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats">
      <div class="stat-card">
        <div class="label">员工总数</div>
        <div class="value">{{ total }}</div>
      </div>
      <div class="stat-card">
        <div class="label">启用</div>
        <div class="value success">{{ enabledCount }}</div>
      </div>
      <div class="stat-card">
        <div class="label">禁用</div>
        <div class="value danger">{{ disabledCount }}</div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="toolbar">
      <el-input
        v-model="query.name"
        placeholder="搜索员工姓名"
        clearable
        style="width: 240px"
      />

      <el-button type="primary" @click="fetchList">
        查询
      </el-button>

      <el-button @click="reset">
        重置
      </el-button>

      <div class="right">
        <el-button type="primary" @click="openAdd">
          + 新增员工
        </el-button>
      </div>
    </div>

    <!-- 表格 -->
    <div class="table-card">
      <el-table
        :data="list"
        v-loading="loading"
        stripe
        border
      >

        <el-table-column prop="username" label="账号" />
        <el-table-column prop="name" label="姓名" />

        <!-- 性别（你要求保留） -->
        <el-table-column label="性别" width="80">
          <template #default="{ row }">
            {{ row.sex === '1' ? '男' : row.sex === '0' ? '女' : row.sex }}
          </template>
        </el-table-column>

        <el-table-column prop="phone" label="手机号" />

        <!-- 状态 Tag -->
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 1 ? 'success' : 'danger'"
              round
            >
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 操作 -->
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-space>
              <el-button
                link
                type="primary"
                @click="openEdit(row.id)"
              >
                编辑
              </el-button>

              <el-button
                link
                :type="row.status === 1 ? 'danger' : 'success'"
                @click="toggleStatus(row)"
              >
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
            </el-space>
          </template>
        </el-table-column>

      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @change="fetchList"
        />
      </div>
    </div>

    <!-- 抽屉表单 -->
    <EmployeeDrawer
      v-model="drawerVisible"
      :id="currentId"
      @success="fetchList"
    />

  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getEmployeePage, setEmployeeStatus } from '@/api/employee'
import EmployeeDrawer from './form.vue'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)

const drawerVisible = ref(false)
const currentId = ref<number | null>(null)

const query = reactive({
  page: 1,
  pageSize: 10,
  name: ''
})

const enabledCount = computed(() =>
  list.value.filter(i => i.status === 1).length
)

const disabledCount = computed(() =>
  list.value.filter(i => i.status === 0).length
)

async function fetchList() {
  loading.value = true
  try {
    const res = await getEmployeePage(query)
    if (res.code === 1) {
      list.value = res.data.records
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

function openAdd() {
  currentId.value = null
  drawerVisible.value = true
}

function openEdit(id: number) {
  currentId.value = id
  drawerVisible.value = true
}

function reset() {
  query.name = ''
  query.page = 1
  fetchList()
}

async function toggleStatus(row: any) {
  try {
    await ElMessageBox.confirm('确认修改状态？', '提示', {
      type: 'warning'
    })

    const newStatus = row.status === 1 ? 0 : 1
    await setEmployeeStatus(row.id, newStatus)

    row.status = newStatus
    ElMessage.success('更新成功')
  } catch {}
}

onMounted(fetchList)
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

.stats {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.stat-card {
  flex: 1;
  background: #fff;
  padding: 14px;
  border-radius: 10px;
}

.label {
  font-size: 12px;
  color: #888;
}

.value {
  font-size: 22px;
  font-weight: bold;
}

.success {
  color: #52c41a;
}

.danger {
  color: #ff4d4f;
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