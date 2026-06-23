<template>
  <el-drawer
    v-model="visible"
    size="420px"
    direction="rtl"
  >
    <template #header>
      <div class="title">
        {{ title }}
      </div>
    </template>

    <div class="form">

      <el-form ref="formRef" :model="form" :rules="rules">

        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>

        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>

        <!-- 性别（保留） -->
        <el-form-item label="性别">
          <el-radio-group v-model="form.sex">
            <el-radio value="1">男</el-radio>
            <el-radio value="0">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>

        <el-form-item label="身份证">
          <el-input v-model="form.idNumber" />
        </el-form-item>

      </el-form>

      <div class="footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">
          保存
        </el-button>
      </div>

    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { addEmployee, updateEmployee, getEmployeeById } from '@/api/employee'

const props = defineProps({
  modelValue: Boolean,
  id: Number
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: v => emit('update:modelValue', v)
})

const title = computed(() =>
  props.id ? '编辑员工' : '新增员工'
)

const formRef = ref()

const form = ref({
  username: '',
  name: '',
  phone: '',
  sex: '1',
  idNumber: ''
})

const rules = {
  username: [{ required: true, message: '请输入账号' }],
  name: [{ required: true, message: '请输入姓名' }],
  phone: [{ required: true, message: '请输入手机号' }]
}

watch(
  () => visible.value,
  async (v) => {
    if (!v) return

    if (props.id) {
      const res = await getEmployeeById(props.id)
      if (res.code === 1) {
        form.value = res.data
      }
    } else {
      form.value = {
        username: '',
        name: '',
        phone: '',
        sex: '男',
        idNumber: ''
      }
    }
  }
)

async function submit() {
  await formRef.value.validate()

  if (props.id) {
    await updateEmployee({ ...form.value, id: props.id })
    ElMessage.success('修改成功')
  } else {
    await addEmployee(form.value)
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
