<template>
  <el-drawer
    v-model="visible"
    size="420px"
    direction="rtl"
  >
    <template #header>
      <div class="title">{{ title }}</div>
    </template>

    <div class="form">
      <el-form ref="formRef" :model="form" :rules="rules">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" maxlength="20" />
        </el-form-item>

        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="1" :max="999" style="width: 200px" />
        </el-form-item>
      </el-form>

      <div class="footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { addCategory, updateCategory } from '@/api/shop'

const props = defineProps({
  modelValue: Boolean,
  id: Number,
  type: { type: Number, default: 1 },
  editData: { type: Object, default: null },
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const title = computed(() => (props.id ? '编辑分类' : '新增分类'))

const formRef = ref()

const form = ref({
  name: '',
  sort: 1,
  type: 1,
})

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  sort: [{ required: true, message: '请输入排序号', trigger: 'blur' }],
}

watch(
  () => visible.value,
  (v) => {
    if (!v) return
    if (props.editData) {
      form.value = {
        name: props.editData.name || '',
        sort: props.editData.sort ?? 1,
        type: props.editData.type ?? props.type,
      }
    } else {
      form.value = { name: '', sort: 1, type: props.type }
    }
  },
)

async function submit() {
  await formRef.value.validate()

  const payload = { ...form.value }
  if (props.id || props.editData?.id) {
    payload.id = props.id || props.editData.id
    await updateCategory(payload)
    ElMessage.success('修改成功')
  } else {
    payload.type = props.type
    await addCategory(payload)
    ElMessage.success('新增成功')
  }

  visible.value = false
  emit('success')
}
</script>

<style scoped lang="scss">
.title {
  font-size: 16px;
  font-weight: bold;
}

.form {
  padding: 10px 0;
}

.footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}
</style>
