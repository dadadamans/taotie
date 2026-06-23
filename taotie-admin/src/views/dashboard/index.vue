<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getBusinessData, getOverviewOrders, getOverviewDishes, getOverviewSetmeals } from '@/api/order'
import Overview from './components/Overview.vue'
import OrderView from './components/OrderView.vue'
import DishView from './components/DishView.vue'
import SetmealView from './components/SetmealView.vue'

const userStore = useUserStore()
const loading = ref(true)

const businessData = ref<any>({})
const orderOverview = ref<any>({})
const dishOverview = ref<any>({})
const setmealOverview = ref<any>({})

const hour = new Date().getHours()
const greeting = hour < 6 ? '凌晨好' : hour < 12 ? '上午好' : hour < 14 ? '中午好' : hour < 18 ? '下午好' : '晚上好'
const userName = userStore.userInfo?.name || '管理员'

async function loadData() {
  loading.value = true
  try {
    const [biz, orders, dishes, setmeals] = await Promise.all([
      getBusinessData(), getOverviewOrders(), getOverviewDishes(), getOverviewSetmeals(),
    ])
    if (biz.code === 1) businessData.value = biz.data
    if (orders.code === 1) orderOverview.value = orders.data
    if (dishes.code === 1) dishOverview.value = dishes.data
    if (setmeals.code === 1) setmealOverview.value = setmeals.data
  } catch {} finally { loading.value = false }
}

onMounted(loadData)
</script>

<template>
  <div v-loading="loading" class="dashboard">
    <div class="welcome">
      <h2>工作台</h2>
      <p class="welcome-text">{{ greeting }}，{{ userName }}</p>
    </div>

    <Overview :data="businessData" />

    <div class="middle-grid">
      <OrderView :data="orderOverview" />
      <DishView :data="dishOverview" />
      <SetmealView :data="setmealOverview" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.dashboard { min-height: 400px; }
.welcome {
  margin-bottom: 20px;
  h2 { font-size: 22px; font-weight: 600; color: $color-text-primary; }
  .welcome-text { margin-top: 4px; font-size: 14px; color: $color-text-secondary; }
}
.middle-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 16px;
}
</style>
