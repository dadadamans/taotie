<template>
  <div class="page">

    <div class="page-head">
      <h2>订单管理</h2>
      <p class="page-desc">查看和处理餐厅订单</p>
    </div>

    <div class="stats-row">
      <div class="stat-item">
        <span class="stat-num" style="color: #6D28D9">{{ statistics.toBeConfirmed }}</span>
        <span class="stat-lbl">待接单</span>
      </div>
      <div class="stat-item">
        <span class="stat-num" style="color: #3B82F6">{{ statistics.confirmed }}</span>
        <span class="stat-lbl">待派送</span>
      </div>
      <div class="stat-item">
        <span class="stat-num" style="color: #F59E0B">{{ statistics.deliveryInProgress }}</span>
        <span class="stat-lbl">派送中</span>
      </div>
      <div class="stat-item">
        <span class="stat-num" style="color: #10B981">{{ completedCount }}</span>
        <span class="stat-lbl">已完成</span>
      </div>
    </div>

    <div class="filter-card">
      <div class="tabs">
        <el-radio-group v-model="activeStatus" @change="switchTab">
          <el-radio-button :value="undefined">全部</el-radio-button>
          <el-radio-button :value="1">待付款</el-radio-button>
          <el-radio-button :value="2">
            待接单
            <el-badge :value="statistics.toBeConfirmed" :hidden="!statistics.toBeConfirmed" />
          </el-radio-button>
          <el-radio-button :value="3">
            待派送
            <el-badge :value="statistics.confirmed" :hidden="!statistics.confirmed" />
          </el-radio-button>
          <el-radio-button :value="4">
            派送中
            <el-badge :value="statistics.deliveryInProgress" :hidden="!statistics.deliveryInProgress" />
          </el-radio-button>
          <el-radio-button :value="5">已完成</el-radio-button>
          <el-radio-button :value="6">已取消</el-radio-button>
        </el-radio-group>
      </div>

      <div class="search-row">
        <el-input v-model="searchNumber" placeholder="订单号" clearable style="width: 180px" @keyup.enter="handleSearch" />
        <el-input v-model="searchPhone" placeholder="手机号" clearable style="width: 150px" @keyup.enter="handleSearch" />
        <el-date-picker
          v-model="searchDateRange" type="daterange" range-separator="至"
          start-placeholder="开始日期" end-placeholder="结束日期"
          value-format="YYYY-MM-DD HH:mm:ss"
          :default-time="[new Date(2000, 0, 1, 0, 0, 0), new Date(2000, 0, 1, 23, 59, 59)]"
          style="width: 270px"
        />
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </div>

    <div class="data-card">
      <el-table :data="list" v-loading="loading" stripe @row-click="goDetail" style="cursor: pointer">
        <el-table-column label="订单号" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="order-number">{{ row.number }}</span>
          </template>
        </el-table-column>

        <el-table-column label="订单菜品" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <el-popover placement="top-start" :width="300" trigger="hover" :content="row.orderDishes">
              <template #reference>
                <span class="order-dishes">{{ row.orderDishes }}</span>
              </template>
            </el-popover>
          </template>
        </el-table-column>

        <el-table-column label="金额" width="90" align="center">
          <template #default="{ row }">¥{{ Number(row.amount).toFixed(2) }}</template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type" effect="dark" size="small" round>
              {{ statusMap[row.status]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="收货人" width="90">
          <template #default="{ row }">{{ row.consignee || '-' }}</template>
        </el-table-column>

        <el-table-column prop="phone" label="手机号" width="130" />

        <el-table-column label="地址" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.address || '-' }}</template>
        </el-table-column>

        <el-table-column label="下单时间" width="170">
          <template #default="{ row }">
            {{ row.orderTime ? row.orderTime.replace('T', ' ').substring(0, 19) : '-' }}
          </template>
        </el-table-column>

        <el-table-column v-if="activeStatus === 6" label="取消原因" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.cancelReason || '-' }}</template>
        </el-table-column>

        <el-table-column label="操作" width="80" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click.stop="goDetail(row.id)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="queryParams.page" v-model:page-size="queryParams.pageSize"
          :total="total" :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange" @current-change="handleCurrentChange"
        />
      </div>

      <el-empty v-if="!loading && list.length === 0" description="暂无订单数据" />
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getOrderPage, getOrderStatistics } from '@/api/order'

const router = useRouter()

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)

const activeStatus = ref<number | undefined>(undefined)
const statistics = ref({ toBeConfirmed: 0, confirmed: 0, deliveryInProgress: 0 })

const completedCount = computed(() => list.value.filter(r => r.status === 5).length)

const searchNumber = ref('')
const searchPhone = ref('')
const searchDateRange = ref<[string, string] | null>(null)

const queryParams = reactive({
  page: 1,
  pageSize: 10,
  number: '',
  phone: '',
  status: undefined as number | undefined,
  beginTime: undefined as string | undefined,
  endTime: undefined as string | undefined,
})

const statusMap: Record<number, { label: string; type: 'warning' | 'primary' | 'info' | 'success' | 'danger' }> = {
  1: { label: '待付款', type: 'warning' },
  2: { label: '待接单', type: 'primary' },
  3: { label: '待派送', type: 'primary' },
  4: { label: '派送中', type: 'info' },
  5: { label: '已完成', type: 'success' },
  6: { label: '已取消', type: 'danger' },
}

async function fetchData() {
  loading.value = true
  try {
    const params: Record<string, any> = { ...queryParams }
    if (!params.number) delete params.number
    if (!params.phone) delete params.phone
    if (params.status === undefined || params.status === null) delete params.status
    if (!params.beginTime) delete params.beginTime
    if (!params.endTime) delete params.endTime
    const res = await getOrderPage(params)
    if (res.code === 1) {
      list.value = res.data.records
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

async function loadStatistics() {
  try {
    const res = await getOrderStatistics()
    if (res.code === 1) statistics.value = res.data
  } catch {}
}

function switchTab(val: number | undefined) {
  queryParams.status = val
  queryParams.page = 1
  fetchData()
}

function handleSearch() {
  queryParams.number = searchNumber.value
  queryParams.phone = searchPhone.value
  if (searchDateRange.value) {
    queryParams.beginTime = searchDateRange.value[0]
    queryParams.endTime = searchDateRange.value[1]
  } else {
    queryParams.beginTime = undefined
    queryParams.endTime = undefined
  }
  queryParams.page = 1
  fetchData()
}

function handleReset() {
  searchNumber.value = ''
  searchPhone.value = ''
  searchDateRange.value = null
  queryParams.number = ''
  queryParams.phone = ''
  queryParams.beginTime = undefined
  queryParams.endTime = undefined
  queryParams.page = 1
  fetchData()
}

function goDetail(id: number) { router.push(`/order/detail/${id}`) }

function handleSizeChange(val: number) {
  queryParams.pageSize = val
  queryParams.page = 1
  fetchData()
}

function handleCurrentChange(val: number) {
  queryParams.page = val
  fetchData()
}

onMounted(() => { loadStatistics(); fetchData() })
</script>

<style scoped lang="scss">
.page { width: 100%; }

.page-head {
  margin-bottom: 20px;
  h2 { margin: 0; font-size: 20px; }
  .page-desc { margin: 4px 0 0; color: #888; font-size: 14px; }
}

.stats-row {
  display: flex; gap: 14px; margin-bottom: 18px;
}
.stat-item {
  flex: 1; background: #fff; border-radius: 10px; padding: 16px 20px;
  display: flex; flex-direction: column; gap: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}
.stat-num { font-size: 26px; font-weight: 700; line-height: 1.2; }
.stat-lbl { font-size: 13px; color: #888; }

.filter-card {
  background: #fff; border-radius: 10px; padding: 16px 20px;
  margin-bottom: 16px; box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}
.tabs { margin-bottom: 14px; }
.search-row {
  display: flex; gap: 10px; align-items: center; flex-wrap: wrap;
  padding-top: 14px; border-top: 1px solid #f0f0f0;
}

.data-card {
  background: #fff; border-radius: 10px; padding: 10px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}
.pagination { margin-top: 12px; display: flex; justify-content: flex-end; }

.order-number { font-family: monospace; font-size: 13px; }
.order-dishes { color: #555; }
</style>
