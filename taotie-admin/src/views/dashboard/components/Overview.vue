<script setup lang="ts">
defineProps<{ data: any }>()
</script>

<template>
  <div class="overview-card">
    <div class="card-header">
      <h3>今日数据</h3>
      <router-link to="/statistics" class="card-more">详细数据 →</router-link>
    </div>
    <div class="overview-grid">
      <div class="overview-item">
        <span class="label">营业额</span>
        <span class="value">¥{{ data.turnover ?? '-' }}</span>
      </div>
      <div class="overview-item">
        <span class="label">有效订单</span>
        <span class="value">{{ data.validOrderCount ?? '-' }}</span>
      </div>
      <div class="overview-item">
        <span class="label">订单完成率</span>
        <span class="value">{{ data.orderCompletionRate ?? 0 }}%</span>
      </div>
      <div class="overview-item">
        <span class="label">平均客单价</span>
        <span class="value">¥{{ data.unitPrice ?? '-' }}</span>
      </div>
      <div class="overview-item">
        <span class="label">新增用户</span>
        <span class="value">{{ data.newUsers ?? '-' }}</span>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.overview-card {
  background: $color-card;
  border-radius: $border-radius;
  box-shadow: $box-shadow;
  padding: 20px;
  margin-bottom: 16px;
}
.card-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 16px;
  h3 { font-size: 16px; font-weight: 600; color: $color-text-primary; }
  .card-more { font-size: 13px; color: $color-primary; }
}
.overview-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}
.overview-item {
  text-align: center;
  padding: 12px 8px;
  background: $color-primary-bg;
  border-radius: $border-radius-sm;
  .label { display: block; font-size: 13px; color: $color-text-secondary; margin-bottom: 8px; }
  .value { display: block; font-size: 22px; font-weight: 700; color: $color-text-primary; }
}
</style>
