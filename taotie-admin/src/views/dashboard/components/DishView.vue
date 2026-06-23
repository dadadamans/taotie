<script setup lang="ts">
import { useRouter } from 'vue-router'
defineProps<{ data: any }>()
const router = useRouter()
</script>

<template>
  <div class="stat-card">
    <div class="card-header">
      <h3>菜品总览</h3>
      <router-link to="/dish" class="card-more">菜品管理 →</router-link>
    </div>
    <div class="mini-list">
      <div class="mini-row">
        <span class="label">已启售</span>
        <span class="num green">{{ data.sold ?? 0 }}</span>
      </div>
      <div class="mini-row">
        <span class="label">已停售</span>
        <span class="num red">{{ data.discontinued ?? 0 }}</span>
      </div>
      <el-button type="primary" class="add-btn" @click="router.push('/dish/form')">
        <el-icon><Plus /></el-icon>新增菜品
      </el-button>
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
.mini-list {
  .mini-row {
    display: flex; justify-content: space-between; align-items: center; padding: 6px 0;
    .label { font-size: 14px; color: $color-text-secondary; }
    .num { font-size: 18px; font-weight: 600; }
    .green { color: #10B981; }
    .red { color: #EF4444; }
  }
  .add-btn { margin-top: 12px; width: 100%; }
}
</style>
