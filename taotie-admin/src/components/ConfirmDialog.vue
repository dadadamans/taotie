<template>
  <el-dialog
    v-model="visible"
    :width="width"
    :close-on-click-modal="false"
    align-center
    class="confirm-dialog"
  >
    <div class="confirm-body">
      <div class="confirm-icon" :class="`is-${type}`">
        <el-icon :size="48">
          <WarningFilled v-if="type === 'warning'" />
          <DeleteFilled v-if="type === 'danger'" />
          <QuestionFilled v-if="type === 'info'" />
        </el-icon>
      </div>
      <h3 class="confirm-title">{{ title }}</h3>
      <p v-if="description" class="confirm-desc">{{ description }}</p>
    </div>
    <template #footer>
      <div class="confirm-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button
          :type="btnType"
          :loading="loading"
          :class="{ 'is-danger': type === 'danger', 'is-warning': type === 'warning' }"
          @click="handleConfirm"
        >
          {{ confirmText || '确定' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { WarningFilled, DeleteFilled, QuestionFilled } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  type: { type: String, default: 'info' as 'info' | 'warning' | 'danger' },
  title: { type: String, default: '确认操作' },
  description: { type: String, default: '' },
  confirmText: { type: String, default: '确定' },
  width: { type: String, default: '400px' },
})

const emit = defineEmits(['update:modelValue', 'confirm'])

const visible = computed({
  get: () => props.modelValue,
  set: v => emit('update:modelValue', v),
})

const loading = ref(false)

const btnType = computed(() => {
  if (props.type === 'danger') return 'danger'
  return 'primary'
})

async function handleConfirm() {
  loading.value = true
  try {
    await emit('confirm')
    visible.value = false
  } finally {
    loading.value = false
  }
}

watch(() => visible.value, (v) => { if (!v) loading.value = false })
</script>

<style scoped lang="scss">
.confirm-dialog {
  :deep(.el-dialog__body) { padding: 24px; }
  :deep(.el-dialog__header) { display: none; }
}

.confirm-body {
  text-align: center;
  padding: 8px 0 16px;
}

.confirm-icon {
  margin-bottom: 16px;
  &.is-info { color: #6D28D9; }
  &.is-warning { color: #F59E0B; }
  &.is-danger { color: #EF4444; }
}

.confirm-title {
  margin: 0 0 8px;
  font-size: 17px;
  font-weight: 600;
  color: #1A1A2E;
}

.confirm-desc {
  margin: 0;
  font-size: 14px;
  color: #6B7280;
  line-height: 1.5;
}

.confirm-footer {
  display: flex;
  justify-content: center;
  gap: 12px;

  :deep(.is-danger) {
    background: #EF4444;
    border-color: #EF4444;
    &:hover { background: #DC2626; border-color: #DC2626; }
  }
}
</style>
