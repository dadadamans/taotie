<script setup lang="ts">
import { useRouter } from 'vue-router'
defineProps<{ data: any }>()
const router = useRouter()
function go(path: string) { router.push(path) }
</script>

<template>
  <div class="stat-card">
    <div class="card-header">
      <h3>订单管理</h3>
      <router-link to="/order" class="card-more">订单明细 →</router-link>
    </div>
    <div class="stat-list">
      <div class="stat-row" @click="go('/order?status=2')">
        <span class="stat-label">待接单</span>
        <span class="stat-num" style="color: #6D28D9">{{ data.waitingOrders ?? 0 }}</span>
      </div>
      <div class="stat-row" @click="go('/order?status=3')">
        <span class="stat-label">待派送</span>
        <span class="stat-num" style="color: #3B82F6">{{ data.deliveredOrders ?? 0 }}</span>
      </div>
      <div class="stat-row" @click="go('/order?status=5')">
        <span class="stat-label">已完成</span>
        <span class="stat-num" style="color: #10B981">{{ data.completedOrders ?? 0 }}</span>
      </div>
      <div class="stat-row" @click="go('/order?status=6')">
        <span class="stat-label">已取消</span>
        <span class="stat-num" style="color: #EF4444">{{ data.cancelledOrders ?? 0 }}</span>
      </div>
      <el-divider style="margin: 6px 0" />
      <div class="stat-row" @click="go('/order')">
        <span class="stat-label">全部订单</span>
        <span class="stat-num" style="font-weight: 700; color: #1A1A2E">{{ data.allOrders ?? 0 }}</span>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.stat-card {
  background: $color-card;
  border-radius: $border-radius;
  box-shadow: $box-shadow;
  padding: 20px;
}
.card-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 12px; padding-bottom: 12px; border-bottom: 1px solid #f3f4f6;
  h3 { font-size: 16px; font-weight: 600; color: $color-text-primary; }
  .card-more { font-size: 13px; color: $color-primary; }
}
.stat-list {
  .stat-row {
    display: flex; justify-content: space-between; align-items: center;
    padding: 6px 0; cursor: pointer; border-radius: 4px; transition: background 0.15s;
    &:hover { background: $color-primary-bg; padding: 6px 8px; }
    .stat-label { font-size: 14px; color: $color-text-secondary; }
    .stat-num { font-size: 18px; font-weight: 600; }
  }
}
</style>
