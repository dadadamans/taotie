<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getOrderDetail,
  confirmOrder,
  rejectOrder,
  cancelOrder,
  deliveryOrder,
  completeOrder,
} from '@/api/order'

const route = useRoute()
const router = useRouter()

const orderId = computed(() => Number(route.params.id))
const loading = ref(false)
const order = ref<any>(null)

// reason dialogs
const reasonDialogVisible = ref(false)
const reasonType = ref<'rejection' | 'cancel'>('rejection')
const reasonText = ref('')
const reasonTitle = computed(() => (reasonType.value === 'rejection' ? '拒单' : '取消订单'))
const reasonPlaceholder = computed(() =>
  reasonType.value === 'rejection' ? '请输入拒单原因' : '请输入取消原因',
)
const reasoning = ref(false)

const statusMap: Record<number, { label: string; type: string }> = {
  1: { label: '待付款', type: 'warning' },
  2: { label: '待接单', type: 'primary' },
  3: { label: '已接单', type: 'primary' },
  4: { label: '派送中', type: 'info' },
  5: { label: '已完成', type: 'success' },
  6: { label: '已取消', type: 'danger' },
}

const payMethodMap: Record<number, string> = {
  1: '微信支付',
  2: '支付宝',
}

const payStatusMap: Record<number, string> = {
  0: '未支付',
  1: '已支付',
  2: '已退款',
}

async function loadOrder() {
  loading.value = true
  try {
    const res = await getOrderDetail(orderId.value)
    if (res.code === 1) {
      order.value = res.data
    } else {
      ElMessage.error(res.msg || '获取订单详情失败')
    }
  } catch {
    ElMessage.error('获取订单详情失败')
  } finally {
    loading.value = false
  }
}

// actions
const actions = computed(() => {
  if (!order.value) return [] as { key: string; label: string; type: 'primary' | 'warning' | 'danger' }[]
  const s = order.value.status
  const list: { key: string; label: string; type: 'primary' | 'warning' | 'danger' }[] = []
  if (s === 2) {
    list.push({ key: 'confirm', label: '接单', type: 'primary' })
    list.push({ key: 'rejection', label: '拒单', type: 'danger' })
    list.push({ key: 'cancel', label: '取消', type: 'warning' })
  } else if (s === 3) {
    list.push({ key: 'delivery', label: '派送', type: 'primary' })
    list.push({ key: 'cancel', label: '取消', type: 'warning' })
  } else if (s === 4) {
    list.push({ key: 'complete', label: '完成', type: 'primary' })
  } else if (s === 1) {
    list.push({ key: 'cancel', label: '取消', type: 'warning' })
  }
  return list
})

function openReasonDialog(type: 'rejection' | 'cancel') {
  reasonType.value = type
  reasonText.value = ''
  reasonDialogVisible.value = true
}

async function confirmAction(key: string) {
  if (!order.value) return
  try {
    let res: any
    if (key === 'confirm') {
      res = await confirmOrder({ id: order.value.id })
    } else if (key === 'delivery') {
      res = await deliveryOrder(order.value.id)
    } else if (key === 'complete') {
      res = await completeOrder(order.value.id)
    }
    if (res.code === 1) {
      ElMessage.success('操作成功')
      loadOrder()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败')
  }
}

async function submitReason() {
  if (!reasonText.value.trim()) {
    ElMessage.warning('请填写原因')
    return
  }
  if (!order.value) return
  reasoning.value = true
  try {
    let res: any
    if (reasonType.value === 'rejection') {
      res = await rejectOrder({ id: order.value.id, rejectionReason: reasonText.value })
    } else {
      res = await cancelOrder({ id: order.value.id, cancelReason: reasonText.value })
    }
    if (res.code === 1) {
      ElMessage.success('操作成功')
      reasonDialogVisible.value = false
      loadOrder()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败')
  } finally {
    reasoning.value = false
  }
}

function handleAction(key: string) {
  if (key === 'rejection' || key === 'cancel') {
    openReasonDialog(key === 'rejection' ? 'rejection' : 'cancel')
  } else {
    confirmAction(key)
  }
}

function goBack() {
  router.push('/order')
}

onMounted(loadOrder)
</script>

<template>
  <div class="page-card" v-loading="loading">
    <div class="page-header">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon> 返回订单列表
      </el-button>
      <h2>订单详情</h2>
    </div>

    <template v-if="order">
      <!-- Order Info -->
      <el-card class="info-card" shadow="never">
        <template #header><span>订单信息</span></template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ order.number }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="statusMap[order.status]?.type as any" effect="dark" size="small">
              {{ statusMap[order.status]?.label }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="下单时间">
            {{ order.orderTime?.replace('T', ' ').substring(0, 19) || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="实收金额">¥{{ order.amount }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">
            {{ payMethodMap[order.payMethod] || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="支付状态">
            {{ payStatusMap[order.payStatus] || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">
            {{ order.remark || '无' }}
          </el-descriptions-item>
          <el-descriptions-item v-if="order.cancelReason" label="取消原因" :span="2">
            {{ order.cancelReason }}
          </el-descriptions-item>
          <el-descriptions-item v-if="order.rejectionReason" label="拒单原因" :span="2">
            {{ order.rejectionReason }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- Customer Info -->
      <el-card class="info-card" shadow="never">
        <template #header><span>收货信息</span></template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="收货人">{{ order.consignee }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ order.phone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ order.address }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- Order Items -->
      <el-card class="info-card" shadow="never">
        <template #header><span>商品明细</span></template>
        <el-table :data="order.orderDetailList || []" stripe border size="small" style="width: 100%">
          <el-table-column label="图片" width="70" align="center">
            <template #default="{ row }">
              <el-image
                v-if="row.image"
                :src="row.image"
                style="width: 40px; height: 40px; border-radius: 4px"
                fit="cover"
              >
                <template #error><span style="font-size: 12px; color: #9ca3af">无图</span></template>
              </el-image>
              <span v-else style="font-size: 12px; color: #9ca3af">无图</span>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="名称" min-width="140" />
          <el-table-column prop="dishFlavor" label="口味" width="120" />
          <el-table-column label="单价" width="90" align="center">
            <template #default="{ row }">¥{{ row.amount }}</template>
          </el-table-column>
          <el-table-column prop="number" label="数量" width="70" align="center" />
          <el-table-column label="小计" width="100" align="center">
            <template #default="{ row }">¥{{ (row.amount * row.number).toFixed(2) }}</template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- Actions -->
      <el-card v-if="actions.length > 0" class="info-card" shadow="never">
        <template #header><span>操作</span></template>
        <div class="action-bar">
          <el-button
            v-for="act in actions"
            :key="act.key"
            :type="act.type"
            size="large"
            @click="handleAction(act.key)"
          >
            {{ act.label }}
          </el-button>
        </div>
      </el-card>
      <el-card v-else class="info-card" shadow="never">
        <div style="color: #9ca3af; text-align: center; padding: 8px 0">
          {{ order.status === 5 ? '订单已完成' : '订单已取消' }}，无可用操作
        </div>
      </el-card>
    </template>

    <el-empty v-if="!loading && !order" description="未找到订单" />
  </div>

  <!-- Reason Dialog -->
  <el-dialog
    v-model="reasonDialogVisible"
    :title="reasonTitle"
    width="420px"
    :close-on-click-modal="false"
  >
    <el-input
      v-model="reasonText"
      type="textarea"
      :rows="4"
      :placeholder="reasonPlaceholder"
      maxlength="200"
      show-word-limit
    />
    <template #footer>
      <el-button @click="reasonDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="reasoning" @click="submitReason">
        {{ reasoning ? '提交中...' : '确定' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<style lang="scss" scoped>
.page-card {
  background: $color-card;
  border-radius: $border-radius;
  box-shadow: $box-shadow;
  padding: 24px;
  min-height: 400px;
}
.page-header {
  margin-bottom: 20px;
  h2 { font-size: 20px; font-weight: 600; color: $color-text-primary; margin-top: 8px; }
}

.info-card {
  margin-bottom: 16px;
}

.action-bar {
  display: flex;
  gap: 12px;
}
</style>
