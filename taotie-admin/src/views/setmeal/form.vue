<template>
  <div class="page-shell">
    <div class="page-head">
      <el-button text class="back-btn" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回套餐列表
      </el-button>
      <div>
        <h2>{{ title }}</h2>
        <p>维护套餐基础信息、套餐菜品和图片素材</p>
      </div>
    </div>

    <el-skeleton v-if="pageLoading" :rows="8" animated />

    <template v-else>
      <div class="editor-grid setmeal-grid">
        <aside class="panel side-panel">
          <ImageUploadCard v-model="form.image" title="套餐图片" hint="建议使用 300 x 300 或更高分辨率图片。上传后可直接预览并删除重传。" />

          <div class="summary-card">
            <h4>保存摘要</h4>
            <div class="summary-item"><span>套餐名称</span><strong>{{ form.name || '-' }}</strong></div>
            <div class="summary-item"><span>分类</span><strong>{{ categoryName || '-' }}</strong></div>
            <div class="summary-item"><span>价格</span><strong>￥{{ priceText }}</strong></div>
            <div class="summary-item"><span>菜品数量</span><strong>{{ setmealDishes.length }}</strong></div>
            <div class="summary-item"><span>合计金额</span><strong>￥{{ totalText }}</strong></div>
          </div>
        </aside>

        <section class="panel main-panel">
          <div class="panel-head">
            <div>
              <h3>基础信息</h3>
              <p>套餐名称、分类、价格和描述</p>
            </div>
          </div>

          <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="editor-form">
            <el-row :gutter="20">
              <el-col :xs="24" :md="12">
                <el-form-item label="套餐名称" prop="name">
                  <el-input v-model="form.name" maxlength="20" placeholder="请填写套餐名称" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :md="12">
                <el-form-item label="套餐分类" prop="categoryId">
                  <el-select v-model="form.categoryId" placeholder="请选择套餐分类" style="width: 100%">
                    <el-option v-for="item in categoryOptions" :key="item.id" :label="item.name" :value="item.id" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :xs="24" :md="12">
                <el-form-item label="套餐价格" prop="price">
                  <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :md="12">
                <el-form-item label="当前状态">
                  <el-tag :type="form.status === 1 ? 'success' : 'info'" size="large">{{ form.status === 1 ? '启售' : '停售' }}</el-tag>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="套餐描述">
              <el-input v-model="form.description" type="textarea" :rows="4" maxlength="200" placeholder="套餐描述，最长200字" />
            </el-form-item>

            <el-form-item label="套餐菜品">
              <div class="dish-card">
                <div class="dish-toolbar">
                  <div>
                    <strong>套餐组成</strong>
                    <p>添加菜品后可调整份数、删除或重新选择</p>
                  </div>
                  <el-button type="primary" plain @click="openDishDrawer">添加菜品</el-button>
                </div>

                <div v-if="setmealDishes.length === 0" class="dish-empty">
                  <p>暂未添加菜品</p>
                  <span>请先选择套餐包含的菜品</span>
                </div>

                <el-table v-else :data="setmealDishes" border stripe class="dish-table">
                  <el-table-column type="index" label="#" width="60" />
                  <el-table-column prop="name" label="菜品名称" min-width="140" />
                  <el-table-column label="原价" width="120">
                    <template #default="{ row }">￥{{ Number(row.price).toFixed(2) }}</template>
                  </el-table-column>
                  <el-table-column label="份数" width="160">
                    <template #default="{ row }">
                      <el-input-number v-model="row.copies" :min="1" :max="99" size="small" style="width: 110px" />
                    </template>
                  </el-table-column>
                  <el-table-column label="小计" width="120">
                    <template #default="{ row }">￥{{ (Number(row.price) * Number(row.copies)).toFixed(2) }}</template>
                  </el-table-column>
                  <el-table-column label="操作" width="100">
                    <template #default="{ $index }">
                      <el-button type="danger" link @click="removeDish($index)">删除</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </el-form-item>
          </el-form>
        </section>
      </div>

      <div class="action-bar">
        <el-button @click="goBack">取消</el-button>
        <div class="action-right">
          <el-button type="primary" :loading="submitting" @click="submit(false)">保存</el-button>
          <el-button v-if="!isEdit" type="primary" plain :loading="submitting" @click="submit(true)">保存并继续添加</el-button>
        </div>
      </div>
    </template>

    <el-drawer v-model="dishDrawerVisible" title="选择菜品" size="720px" :close-on-click-modal="false">
      <div class="drawer-toolbar">
        <el-select v-model="dishCategoryFilter" placeholder="菜品分类" clearable style="width: 160px" @change="loadDishList">
          <el-option v-for="item in dishCategoryOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <el-input v-model="dishSearchName" placeholder="菜品名称" clearable style="width: 220px" @keyup.enter="loadDishList" />
        <el-button type="primary" @click="loadDishList">查询</el-button>
      </div>

      <el-table
        ref="dishTableRef"
        :data="dishList"
        row-key="id"
        v-loading="dishLoading"
        border
        stripe
        class="drawer-table"
        @selection-change="handleDishSelectionChange"
      >
        <el-table-column type="selection" width="48" :reserve-selection="true" />
        <el-table-column prop="name" label="菜品名称" min-width="140" />
        <el-table-column label="价格" width="110">
          <template #default="{ row }">￥{{ Number(row.price).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '启售' : '停售' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="dishDrawerVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmDishSelection">确定</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { addSetmeal, updateSetmeal, getSetmealById, getCategoryList, getDishList, getDishPage } from '@/api/shop'
import ImageUploadCard from '@/components/ImageUploadCard.vue'

interface SetmealDishItem { dishId: number; name: string; price: number; copies: number }

const route = useRoute()
const router = useRouter()

const id = computed(() => (route.query.id ? Number(route.query.id) : null))
const isEdit = computed(() => id.value !== null)
const title = computed(() => (isEdit.value ? '编辑套餐' : '新增套餐'))

const formRef = ref<FormInstance>()
const pageLoading = ref(false)
const submitting = ref(false)
const categoryOptions = ref<{ id: number; name: string }[]>([])
const setmealDishes = ref<SetmealDishItem[]>([])
const form = ref({ name: '', categoryId: null as number | null, price: null as number | null, image: '', description: '', status: 0 })

const nameRegex = /^[A-Za-z0-9\u4e00-\u9fa5]{2,20}$/
const priceRegex = /^([1-9]\d{0,5}|0)(\.\d{1,2})?$/

const rules: FormRules = {
  name: [
    { required: true, message: '请输入套餐名称', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: Function) => {
        if (!value) return callback(new Error('请输入套餐名称'))
        if (!nameRegex.test(value)) return callback(new Error('套餐名称输入不符，请输入2-20个字符（汉字/字母/数字）'))
        callback()
      },
      trigger: 'blur',
    },
  ],
  categoryId: [{ required: true, message: '请选择套餐分类', trigger: 'change' }],
  price: [
    { required: true, message: '请设置套餐价格', trigger: 'blur' },
    {
      validator: (_rule: any, value: any, callback: Function) => {
        if (value === null || value === '') return callback(new Error('请设置套餐价格'))
        if (!priceRegex.test(String(value))) return callback(new Error('套餐价格格式有误，请输入大于零且最多保留两位小数的金额'))
        if (Number(value) <= 0) return callback(new Error('价格必须大于0'))
        callback()
      },
      trigger: 'blur',
    },
  ],
  image: [{ required: true, message: '请上传套餐图片', trigger: 'change' }],
}

const dishTableRef = ref<any>(null)
const dishDrawerVisible = ref(false)
const dishList = ref<any[]>([])
const dishLoading = ref(false)
const dishSearchName = ref('')
const dishCategoryFilter = ref<number | null>(null)
const dishCategoryOptions = ref<{ id: number; name: string }[]>([])
const selectedDishIds = ref<Set<number>>(new Set())

const categoryName = computed(() => categoryOptions.value.find(item => item.id === form.value.categoryId)?.name || '')
const priceText = computed(() => (form.value.price === null || form.value.price === undefined ? '-' : Number(form.value.price).toFixed(2)))
const totalText = computed(() => setmealDishes.value.reduce((sum, item) => sum + Number(item.price) * Number(item.copies), 0).toFixed(2))

async function loadData() {
  pageLoading.value = true
  try {
    const [catRes] = await Promise.all([getCategoryList(2)])
    if (catRes.code === 1) categoryOptions.value = catRes.data || []
    if (isEdit.value) {
      const r = await getSetmealById(id.value!)
      if (r.code === 1) {
        const d = r.data
        form.value = {
          name: d.name || '',
          categoryId: d.categoryId ?? null,
          price: d.price ?? null,
          image: d.image || '',
          description: d.description || '',
          status: d.status ?? 0,
        }
        setmealDishes.value = (d.setmealDishes || []).map((sd: any) => ({
          dishId: sd.dishId,
          name: sd.name || '',
          price: sd.price ?? 0,
          copies: sd.copies ?? 1,
        }))
      }
    }
  } finally {
    pageLoading.value = false
  }
}

async function loadDishList() {
  dishLoading.value = true
  try {
    if (dishCategoryFilter.value) {
      const r = await getDishList({ categoryId: dishCategoryFilter.value })
      if (r.code === 1) dishList.value = r.data || []
    } else {
      const r = await getDishPage({ page: 1, pageSize: 999, name: dishSearchName.value || undefined })
      if (r.code === 1) dishList.value = r.data.records || []
    }
    await nextTick()
    dishTableRef.value?.clearSelection?.()
    dishList.value.forEach((row) => {
      if (selectedDishIds.value.has(row.id)) dishTableRef.value?.toggleRowSelection?.(row, true)
    })
  } finally {
    dishLoading.value = false
  }
}

async function openDishDrawer() {
  dishSearchName.value = ''
  dishCategoryFilter.value = null
  selectedDishIds.value = new Set(setmealDishes.value.map(d => d.dishId))
  const catRes = await getCategoryList(1)
  if (catRes.code === 1) dishCategoryOptions.value = catRes.data || []
  dishDrawerVisible.value = true
  await loadDishList()
}

function handleDishSelectionChange(selection: any[]) {
  selectedDishIds.value = new Set(selection.map(item => item.id))
}


function confirmDishSelection() {
  const exist = new Set(setmealDishes.value.map(d => d.dishId))
  for (const dish of dishList.value) {
    if (selectedDishIds.value.has(dish.id) && !exist.has(dish.id)) {
      setmealDishes.value.push({ dishId: dish.id, name: dish.name, price: dish.price, copies: 1 })
    }
  }
  dishDrawerVisible.value = false
}

function removeDish(i: number) {
  setmealDishes.value.splice(i, 1)
}

async function submit(continueAdd: boolean) {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  if (setmealDishes.value.length === 0) {
    ElMessage.error('套餐下菜品不能为空')
    return
  }
  if (!form.value.image) {
    ElMessage.error('套餐图片不能为空')
    return
  }
  submitting.value = true
  try {
    const payload: Record<string, any> = {
      name: form.value.name,
      categoryId: form.value.categoryId,
      price: form.value.price,
      image: form.value.image,
      description: form.value.description,
      status: isEdit.value ? form.value.status : 0,
      setmealDishes: setmealDishes.value.map(d => ({ dishId: d.dishId, name: d.name, price: d.price, copies: d.copies })),
    }
    if (isEdit.value) payload.id = id.value
    const r = isEdit.value ? await updateSetmeal(payload) : await addSetmeal(payload)
    if (r.code === 1) {
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
      if (continueAdd) {
        form.value = { name: '', categoryId: null, price: null, image: '', description: '', status: 0 }
        setmealDishes.value = []
      } else {
        router.push('/setmeal')
      }
    }
  } catch {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

function goBack() {
  router.push('/setmeal')
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.page-shell {
  min-height: 100%;
  padding: 20px;
  background: #f5f7fb;
}

.page-head {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;

  h2 {
    margin: 0;
    font-size: 22px;
    color: #111827;
  }

  p {
    margin: 6px 0 0;
    color: #6b7280;
  }
}

.back-btn {
  margin-top: -2px;
}

.editor-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(320px, 0.8fr);
  gap: 16px;
  align-items: start;
}

.panel {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.06);
}

.main-panel {
  padding: 20px;
}

.side-panel {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: sticky;
  top: 20px;
}

.panel-head {
  margin-bottom: 8px;

  h3 {
    margin: 0;
    font-size: 16px;
    color: #111827;
  }

  p {
    margin: 6px 0 0;
    font-size: 12px;
    color: #6b7280;
  }
}

.editor-form :deep(.el-form-item__label) {
  font-weight: 600;
}

.dish-card {
  width: 100%;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
  background: #fafafa;
}

.dish-toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;

  strong {
    display: block;
    font-size: 14px;
    color: #111827;
  }

  p {
    margin: 4px 0 0;
    font-size: 12px;
    color: #6b7280;
  }
}

.dish-empty {
  padding: 24px;
  border: 1px dashed #d1d5db;
  border-radius: 10px;
  text-align: center;
  color: #6b7280;

  p {
    margin: 0;
    font-weight: 600;
    color: #374151;
  }

  span {
    display: block;
    margin-top: 6px;
    font-size: 12px;
  }
}

.dish-table {
  background: #fff;
}

.summary-card {
  padding: 16px;
  border-radius: 12px;
  background: linear-gradient(180deg, #f9fafb, #fff);
  border: 1px solid #e5e7eb;

  h4 {
    margin: 0 0 12px;
    font-size: 14px;
    color: #111827;
  }
}

.summary-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 0;
  border-top: 1px solid #f3f4f6;
  font-size: 13px;

  &:first-of-type {
    border-top: 0;
    padding-top: 0;
  }

  span {
    color: #6b7280;
  }

  strong {
    color: #111827;
    font-weight: 600;
    text-align: right;
  }
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
  padding: 14px 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.06);
}

.action-right {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.drawer-toolbar {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.drawer-table {
  width: 100%;
}

@media (max-width: 1100px) {
  .editor-grid {
    grid-template-columns: 1fr;
  }

  .side-panel {
    position: static;
  }
}
</style>
