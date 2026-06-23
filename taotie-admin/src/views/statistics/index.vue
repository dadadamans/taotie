<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import {
  getTurnoverStatistics,
  getUserStatistics,
  getOrderStatisticsReport,
  getTop10,
  exportReport,
} from '@/api/order'

const loading = ref(false)
const now = new Date()
const sevenDaysAgo = new Date(now.getTime() - 6 * 24 * 60 * 60 * 1000)

function fmtDate(d: Date) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

const dateRange = ref<[string, string]>([fmtDate(sevenDaysAgo), fmtDate(now)])

// summary stats
const turnoverStats = ref({ total: 0, avg: 0, max: 0 })
const userStats = ref({ total: 0, newUsers: 0 })

// echarts instances
let chartTurnover: echarts.ECharts | null = null
let chartUser: echarts.ECharts | null = null
let chartTop10: echarts.ECharts | null = null

const turnoverRef = ref<HTMLElement | null>(null)
const userRef = ref<HTMLElement | null>(null)
const top10Ref = ref<HTMLElement | null>(null)

// summary
const totalOrders = ref(0)
const validOrders = ref(0)
const completionRate = ref(0)

function initCharts() {
  if (turnoverRef.value) chartTurnover = echarts.init(turnoverRef.value)
  if (userRef.value) chartUser = echarts.init(userRef.value)
  if (top10Ref.value) chartTop10 = echarts.init(top10Ref.value)
}

function disposeCharts() {
  chartTurnover?.dispose()
  chartUser?.dispose()
  chartTop10?.dispose()
  chartTurnover = null
  chartUser = null
  chartTop10 = null
}

function handleResize() {
  chartTurnover?.resize()
  chartUser?.resize()
  chartTop10?.resize()
}

async function fetchData() {
  if (!dateRange.value[0] || !dateRange.value[1]) {
    ElMessage.warning('请选择日期范围')
    return
  }
  loading.value = true
  const [begin, end] = dateRange.value
  try {
    const [tr, ur, or, top] = await Promise.all([
      getTurnoverStatistics({ begin, end }),
      getUserStatistics({ begin, end }),
      getOrderStatisticsReport({ begin, end }),
      getTop10({ begin, end }),
    ])
    if (tr.code === 1) renderTurnover(tr.data)
    if (ur.code === 1) renderUser(ur.data)
    if (or.code === 1) renderOrderSummary(or.data)
    if (top.code === 1) renderTop10(top.data)
  } catch {
    ElMessage.error('获取统计数据失败')
  } finally {
    loading.value = false
  }
}

function renderTurnover(data: any) {
  if (!chartTurnover) return
  const dates = (data.dateList || '').split(',').filter(Boolean)
  const values = (data.turnoverList || '').split(',').filter(Boolean).map(Number)
  const total = values.reduce((s: number, v: number) => s + v, 0)
  const avg = values.length ? total / values.length : 0
  const max = values.length ? Math.max(...values) : 0
  turnoverStats.value = { total, avg: Math.round(avg), max: Math.round(max) }

  chartTurnover.setOption({
    tooltip: {
      trigger: 'axis',
      valueFormatter: (v: any) => `¥${Number(v).toFixed(2)}`,
    },
    grid: { left: 60, right: 30, bottom: 30, top: 45 },
    xAxis: { type: 'category', data: dates, axisLabel: { fontSize: 11 } },
    yAxis: { type: 'value', name: '营业额(元)' },
    series: [{
      type: 'bar',
      data: values.map((v: number) => ({
        value: v,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#6D28D9' },
            { offset: 1, color: '#C4B5FD' },
          ]),
          borderRadius: [4, 4, 0, 0],
        },
      })),
      barWidth: '50%',
      label: {
        show: true,
        position: 'top',
        fontSize: 11,
        color: '#6D28D9',
        formatter: (p: any) => p.value > 0 ? `¥${p.value}` : '',
      },
      markLine: {
        silent: true,
        data: [{ type: 'average', name: '日均' }],
        lineStyle: { color: '#EF4444', type: 'dashed', width: 1.5 },
        label: { formatter: '日均: ¥{c}', fontSize: 11, color: '#EF4444' },
      },
    }],
  })
}

function renderUser(data: any) {
  if (!chartUser) return
  const dates = (data.dateList || '').split(',').filter(Boolean)
  const total = (data.totalUserList || '').split(',').filter(Boolean).map(Number)
  const newUsers = (data.newUserList || '').split(',').filter(Boolean).map(Number)
  userStats.value = {
    total: total.length ? total[total.length - 1] : 0,
    newUsers: newUsers.reduce((s: number, v: number) => s + v, 0),
  }

  chartUser.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['总用户', '新增用户'], bottom: 0 },
    grid: { left: 60, right: 60, bottom: 50, top: 45 },
    xAxis: { type: 'category', data: dates, axisLabel: { fontSize: 11 } },
    yAxis: [
      { type: 'value', name: '总用户数' },
      { type: 'value', name: '新增数' },
    ],
    series: [
      {
        name: '总用户',
        type: 'line',
        data: total,
        smooth: true,
        showSymbol: true,
        symbolSize: 6,
        lineStyle: { color: '#6D28D9', width: 2.5 },
        itemStyle: { color: '#6D28D9' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(109, 40, 217, 0.15)' },
            { offset: 1, color: 'rgba(109, 40, 217, 0.02)' },
          ]),
        },
      },
      {
        name: '新增用户',
        type: 'bar',
        yAxisIndex: 1,
        data: newUsers.map((v: number) => ({
          value: v,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#F59E0B' },
              { offset: 1, color: '#FDE68A' },
            ]),
            borderRadius: [4, 4, 0, 0],
          },
        })),
        barWidth: '40%',
        label: {
          show: true,
          position: 'top',
          fontSize: 11,
          color: '#D97706',
          formatter: (p: any) => p.value > 0 ? `${p.value}` : '',
        },
      },
    ],
  })
}

function renderOrderSummary(data: any) {
  totalOrders.value = data.totalOrderCount ?? 0
  validOrders.value = data.validOrderCount ?? 0
  completionRate.value = data.orderCompletionRate ?? 0
}

function renderTop10(data: any) {
  if (!chartTop10) return
  const names = (data.nameList || '').split(',').filter(Boolean).reverse()
  const nums = (data.numberList || '').split(',').filter(Boolean).map(Number).reverse()
  chartTop10.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 100, right: 50, top: 10, bottom: 20 },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: names, axisLabel: { fontSize: 12 } },
    series: [{
      type: 'bar',
      data: nums.map((v: number) => ({
        value: v,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#F59E0B' },
            { offset: 1, color: '#FBBF24' },
          ]),
          borderRadius: [0, 4, 4, 0],
        },
      })),
      barWidth: '60%',
      label: { show: true, position: 'right', fontSize: 11 },
    }],
  })
}

async function handleExport() {
  if (!dateRange.value[0] || !dateRange.value[1]) {
    ElMessage.warning('请选择日期范围')
    return
  }
  try {
    const blob = await exportReport()
    const url = URL.createObjectURL(blob as Blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `运营数据报表_${dateRange.value[0]}_${dateRange.value[1]}.xls`
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  }
}

onMounted(async () => {
  await nextTick()
  initCharts()
  fetchData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  disposeCharts()
})
</script>

<template>
  <div class="page" v-loading="loading">

    <div class="page-head">
      <h2>数据统计</h2>
      <p class="page-desc">查看餐厅运营数据与分析</p>
    </div>

    <div class="toolbar">
      <el-date-picker
        v-model="dateRange" type="daterange" range-separator="至"
        start-placeholder="开始日期" end-placeholder="结束日期"
        value-format="YYYY-MM-DD" style="width: 280px"
      />
      <el-button type="primary" @click="fetchData">查询</el-button>
      <el-button @click="handleExport">
        <el-icon><Download /></el-icon>导出报表
      </el-button>
    </div>

    <div class="data-card">
      <div class="card-head">
        <h3>营业额趋势</h3>
      </div>
      <div class="stat-row">
        <div class="stat-item">
          <span class="stat-val">¥{{ turnoverStats.total.toFixed(2) }}</span>
          <span class="stat-lbl">总营业额</span>
        </div>
        <div class="stat-item">
          <span class="stat-val">¥{{ turnoverStats.avg }}</span>
          <span class="stat-lbl">日均营业额</span>
        </div>
        <div class="stat-item">
          <span class="stat-val">¥{{ turnoverStats.max }}</span>
          <span class="stat-lbl">最高单日</span>
        </div>
      </div>
      <div ref="turnoverRef" class="chart-box" />
    </div>

    <div class="data-card">
      <div class="card-head">
        <h3>用户统计</h3>
      </div>
      <div class="stat-row">
        <div class="stat-item">
          <span class="stat-val">{{ userStats.total }}</span>
          <span class="stat-lbl">期末总用户</span>
        </div>
        <div class="stat-item">
          <span class="stat-val">{{ userStats.newUsers }}</span>
          <span class="stat-lbl">期间新增</span>
        </div>
      </div>
      <div ref="userRef" class="chart-box" />
    </div>

    <div class="chart-grid">
      <div class="data-card">
        <div class="card-head">
          <h3>订单统计</h3>
        </div>
        <div class="order-stats">
          <div class="order-item">
            <span class="order-val" style="color: #6D28D9">{{ totalOrders }}</span>
            <span class="order-lbl">订单总数</span>
          </div>
          <div class="order-item">
            <span class="order-val" style="color: #10B981">{{ validOrders }}</span>
            <span class="order-lbl">有效订单</span>
          </div>
          <div class="order-item">
            <span class="order-val" style="color: #F59E0B">{{ completionRate }}%</span>
            <span class="order-lbl">完成率</span>
          </div>
        </div>
      </div>

      <div class="data-card">
        <div class="card-head">
          <h3>销量排名 TOP10</h3>
        </div>
        <div ref="top10Ref" class="chart-box" />
      </div>
    </div>

  </div>
</template>

<style scoped lang="scss">
.page { width: 100%; }

.page-head {
  margin-bottom: 20px;
  h2 { margin: 0; font-size: 20px; }
  .page-desc { margin: 4px 0 0; color: #888; font-size: 14px; }
}

.toolbar {
  display: flex; gap: 10px; align-items: center; background: #fff;
  padding: 12px; border-radius: 10px; margin-bottom: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.data-card {
  background: #fff; border-radius: 10px; padding: 20px;
  margin-bottom: 16px; box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.card-head {
  margin-bottom: 12px;
  h3 { margin: 0; font-size: 16px; font-weight: 600; color: #1A1A2E; }
}

.stat-row {
  display: flex; gap: 20px; margin-bottom: 16px;
}

.stat-item {
  background: #f5f3ff; padding: 12px 20px; border-radius: 8px; min-width: 140px;
}

.stat-val {
  display: block; font-size: 22px; font-weight: 700; color: #6D28D9;
}

.stat-lbl {
  display: block; font-size: 12px; color: #888; margin-top: 2px;
}

.chart-box {
  width: 100%; height: 320px;
}

.chart-grid {
  display: grid; grid-template-columns: 1fr 1fr; gap: 16px;
}

.order-stats {
  display: flex; flex-direction: column; gap: 16px; padding: 12px 0;
}

.order-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 0 8px;
}

.order-val {
  font-size: 24px; font-weight: 700;
}

.order-lbl {
  font-size: 14px; color: #888;
}

@media (max-width: 900px) {
  .chart-grid { grid-template-columns: 1fr; }
}
</style>
