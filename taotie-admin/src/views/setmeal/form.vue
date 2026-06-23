<template>
  <div class="page">

    <div class="page-head">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon> 返回套餐列表
      </el-button>
      <h2 style="margin-top: 8px">{{ title }}</h2>
    </div>

    <div class="form-card">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" v-loading="pageLoading">
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="套餐名称" prop="name">
              <el-input v-model="form.name" placeholder="请填写套餐名称" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="套餐分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择套餐分类" style="width: 100%">
                <el-option v-for="item in categoryOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="套餐价格" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" style="width: 200px" />
              <span style="margin-left: 8px; color: #888">元</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="套餐菜品" required>
          <div class="dish-section">
            <el-button type="primary" @click="openDishSelector">
              + 添加菜品
            </el-button>
            <el-table v-if="setmealDishes.length > 0" :data="setmealDishes" stripe size="small" style="width: 100%; margin-top: 12px">
              <el-table-column type="index" label="#" width="50" />
              <el-table-column prop="name" label="名称" width="180" />
              <el-table-column label="原价" width="180">
                <template #default="{ row }">¥{{ row.price.toFixed(2) }}</template>
              </el-table-column>
              <el-table-column label="份数" width="180">
                <template #default="{ row }">
                  <el-input-number v-model="row.copies" :min="1" :max="99" size="small" style="width: 100px" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180">
                <template #default="{ $index }">
                  <el-button type="danger" link size="small" @click="removeDish($index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <p v-if="setmealDishes.length === 0" style="color: #9ca3af; margin-top: 8px">暂未添加菜品，请点击上方按钮选择菜品</p>
          </div>
        </el-form-item>

        <el-form-item label="套餐图片" prop="image">
          <div class="upload-wrap">
            <el-upload
              :show-file-list="false"
              :before-upload="(f: File) => { handleUpload(f); return false }"
              accept="image/png,image/jpeg,image/jpg"
            >
              <el-button type="primary" :loading="uploadLoading">
                {{ uploadLoading ? '上传中...' : '选择图片' }}
              </el-button>
            </el-upload>
            <div v-if="form.image" class="preview-box">
              <el-image :src="form.image" style="width: 100px; height: 100px; border-radius: 8px" fit="cover" />
              <el-button class="preview-del" circle size="small" type="danger" @click="form.image = ''">
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
            <div class="upload-hint">
              图片大小不超过2M<br>仅能上传 PNG JPEG JPG类型图片
            </div>
          </div>
        </el-form-item>

        <el-form-item label="套餐描述">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="200" placeholder="套餐描述" style="width: 480px" />
        </el-form-item>

        <el-form-item>
          <el-button @click="goBack">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submit(false)">保存</el-button>
          <el-button v-if="!isEdit" type="primary" :loading="submitting" @click="submit(true)">保存并继续添加</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-dialog v-model="dishDialogVisible" title="选择菜品" width="640px" :close-on-click-modal="false">
      <el-form :inline="true">
        <el-input v-model="dishSearchName" placeholder="菜品名称" clearable style="width: 200px" @keyup.enter="loadDishList" />
        <el-button type="primary" @click="loadDishList">查询</el-button>
      </el-form>
      <el-table :data="dishList" v-loading="dishLoading" stripe size="small" style="width: 100%"
        @selection-change="(s: any) => selectedDishIds = new Set(s.map((d: any) => d.id))">
        <el-table-column type="selection" width="45" />
        <el-table-column prop="name" label="菜品名称" min-width="140" />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column label="价格" width="80"><template #default="{ row }">¥{{ row.price }}</template></el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="dishDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmDishSelection">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addSetmeal, updateSetmeal, getSetmealById, getCategoryList, getDishPage, uploadFile } from '@/api/shop'
import type { FormInstance, FormRules } from 'element-plus'

interface SetmealDishItem { dishId: number; name: string; price: number; copies: number }

const route = useRoute()
const router = useRouter()

const id = computed(() => (route.query.id ? Number(route.query.id) : null))
const isEdit = computed(() => id.value !== null)
const title = computed(() => (isEdit.value ? '编辑套餐' : '新增套餐'))

const formRef = ref<FormInstance>()
const pageLoading = ref(false)
const submitting = ref(false)
const uploadLoading = ref(false)

const categoryOptions = ref<{ id: number; name: string }[]>([])
const setmealDishes = ref<SetmealDishItem[]>([])

const form = ref({ name: '', categoryId: null as number | null, price: null as number | null, image: '', description: '', status: 1 })

const rules: FormRules = {
  name: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }, { max: 20, message: '名称不超过20个字符', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择套餐分类', trigger: 'change' }],
  price: [{ required: true, message: '请设置套餐价格', trigger: 'blur' }, { type: 'number', min: 0.01, message: '价格必须大于0', trigger: 'blur' }],
  image: [{ required: true, message: '请上传套餐图片', trigger: 'change' }],
}

// dish selector
const dishDialogVisible = ref(false)
const dishList = ref<any[]>([])
const dishLoading = ref(false)
const dishSearchName = ref('')
const selectedDishIds = ref<Set<number>>(new Set())

async function loadData() {
  pageLoading.value = true
  try {
    const [catRes] = await Promise.all([getCategoryList(2)])
    if (catRes.code === 1) categoryOptions.value = catRes.data
    if (isEdit.value) {
      const r = await getSetmealById(id.value!)
      if (r.code === 1) {
        const d = r.data
        form.value = { name: d.name || '', categoryId: d.categoryId ?? null, price: d.price ?? null, image: d.image || '', description: d.description || '', status: d.status ?? 1 }
        setmealDishes.value = (d.setmealDishes || []).map((sd: any) => ({ dishId: sd.dishId, name: sd.name || '', price: sd.price ?? 0, copies: sd.copies ?? 1 }))
      }
    }
  } finally { pageLoading.value = false }
}

async function loadDishList() {
  dishLoading.value = true
  try { const r = await getDishPage({ page: 1, pageSize: 999, name: dishSearchName.value || undefined }); if (r.code === 1) dishList.value = r.data.records || [] } finally { dishLoading.value = false }
}

function openDishSelector() {
  selectedDishIds.value = new Set(setmealDishes.value.map(d => d.dishId))
  dishDialogVisible.value = true; loadDishList()
}

function confirmDishSelection() {
  const exist = new Set(setmealDishes.value.map(d => d.dishId))
  for (const dish of dishList.value) {
    if (selectedDishIds.value.has(dish.id) && !exist.has(dish.id)) {
      setmealDishes.value.push({ dishId: dish.id, name: dish.name, price: dish.price, copies: 1 })
    }
  }
  dishDialogVisible.value = false
}

function removeDish(i: number) { setmealDishes.value.splice(i, 1) }

async function handleUpload(file: File) {
  if (file.size > 2 * 1024 * 1024) { ElMessage.error('图片大小不能超过 2M'); return }
  uploadLoading.value = true
  try { const r = await uploadFile(file); if (r.code === 1) form.value.image = r.data } catch { ElMessage.error('上传失败') } finally { uploadLoading.value = false }
}

async function submit(continueAdd: boolean) {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }
  submitting.value = true
  try {
    const payload: Record<string, any> = {
      name: form.value.name, categoryId: form.value.categoryId, price: form.value.price,
      image: form.value.image, description: form.value.description, status: form.value.status,
      setmealDishes: setmealDishes.value.map(d => ({ dishId: d.dishId, name: d.name, price: d.price, copies: d.copies })),
    }
    if (isEdit.value) payload.id = id.value
    const r = isEdit.value ? await updateSetmeal(payload) : await addSetmeal(payload)
    if (r.code === 1) {
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
      if (continueAdd) {
        form.value = { name: '', categoryId: null, price: null, image: '', description: '', status: 1 }
        setmealDishes.value = []
      } else {
        router.push('/setmeal')
      }
    }
  } catch { ElMessage.error('操作失败') } finally { submitting.value = false }
}

function goBack() { router.push('/setmeal') }

onMounted(loadData)
</script>

<style scoped lang="scss">
.page { width: 100%; }
.page-head { margin-bottom: 20px; h2 { margin: 0; font-size: 20px; } }
.form-card {
  background: #fff; border-radius: 10px; padding: 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05); max-width: 960px;
}
.upload-wrap { display: flex; gap: 16px; align-items: flex-start; }
.preview-box { position: relative; }
.preview-del { position: absolute; top: -8px; right: -8px; width: 22px; height: 22px; padding: 0; }
.upload-hint { font-size: 12px; color: #9ca3af; line-height: 1.6; }
.dish-section { width: 100%; }
</style>
