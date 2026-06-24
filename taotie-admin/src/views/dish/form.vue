<template>
  <div class="page-shell">
    <div class="page-head">
      <el-button text class="back-btn" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回菜品列表
      </el-button>
      <div>
        <h2>{{ title }}</h2>
        <p>维护菜品基础信息、口味配置和图片素材</p>
      </div>
    </div>

    <el-skeleton v-if="pageLoading" :rows="8" animated />

    <template v-else>
      <div class="editor-grid dish-grid">
        <aside class="panel side-panel">
          <ImageUploadCard v-model="form.image" title="菜品图片" hint="建议使用 300 x 300 或更高分辨率图片。上传后可直接预览并删除重传。" />

          <div class="summary-card">
            <h4>保存摘要</h4>
            <div class="summary-item"><span>菜品名称</span><strong>{{ form.name || '-' }}</strong></div>
            <div class="summary-item"><span>分类</span><strong>{{ categoryName || '-' }}</strong></div>
            <div class="summary-item"><span>价格</span><strong>￥{{ priceText }}</strong></div>
            <div class="summary-item"><span>口味数</span><strong>{{ flavors.filter(f => f.name).length }}</strong></div>
          </div>
        </aside>

        <section class="panel main-panel">
          <div class="panel-head">
            <div>
              <h3>基础信息</h3>
              <p>菜品名称、分类、价格和描述</p>
            </div>
          </div>

          <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="editor-form">
            <el-row :gutter="20">
              <el-col :xs="24" :md="12">
                <el-form-item label="菜品名称" prop="name">
                  <el-input v-model="form.name" maxlength="20" placeholder="请填写菜品名称" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :md="12">
                <el-form-item label="菜品分类" prop="categoryId">
                  <el-select v-model="form.categoryId" placeholder="请选择菜品分类" style="width: 100%">
                    <el-option v-for="item in categoryOptions" :key="item.id" :label="item.name" :value="item.id" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :xs="24" :md="12">
                <el-form-item label="菜品价格" prop="price">
                  <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :md="12">
                <el-form-item label="当前状态">
                  <el-tag :type="form.status === 1 ? 'success' : 'info'" size="large">{{ form.status === 1 ? '启售' : '停售' }}</el-tag>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="菜品描述">
              <el-input v-model="form.description" type="textarea" :rows="4" maxlength="200" placeholder="菜品描述，最长200字" />
            </el-form-item>

            <el-form-item label="口味做法配置">
              <div class="flavor-card">
                <div class="flavor-toolbar">
                  <div>
                    <strong>口味配置</strong>
                    <p>按需添加口味名，并为每项配置标签</p>
                  </div>
                  <el-button v-if="flavors.length < maxFlavors" type="primary" plain @click="addFlavor">添加口味</el-button>
                </div>

                <div v-if="flavors.length === 0" class="flavor-empty">
                  <p>当前还没有口味配置</p>
                  <span>可直接点击右上角按钮添加常见口味项</span>
                </div>

                <div v-else class="flavor-list">
                  <div v-for="(flavor, fi) in flavors" :key="fi" class="flavor-item">
                    <div class="flavor-item__head">
                      <el-select v-model="flavor.name" placeholder="口味名" style="width: 150px" @change="(v: string) => onFlavorNameChange(fi, v)">
                        <el-option v-for="opt in leftFlavorOptions" :key="opt" :label="opt" :value="opt" />
                      </el-select>
                      <el-button type="danger" link @click="removeFlavor(fi)">删除</el-button>
                    </div>
                    <div class="tag-row">
                      <el-tag v-for="(tag, ti) in flavor.value" :key="ti" closable @close="removeFlavorTag(fi, ti)">{{ tag }}</el-tag>
                      <el-input
                        :model-value="flavorInputs[fi] || ''"
                        placeholder="输入标签后回车"
                        style="width: 160px"
                        @update:model-value="(v: string) => (flavorInputs[fi] = v)"
                        @keydown="(e: KeyboardEvent) => { if (e.key === 'Enter') { e.preventDefault(); addFlavorTag(fi) } }"
                      />
                    </div>
                  </div>
                </div>
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
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { addDish, updateDish, getDishById, getCategoryList } from '@/api/shop'
import ImageUploadCard from '@/components/ImageUploadCard.vue'

interface FlavorItem { name: string; value: string[] }

const route = useRoute()
const router = useRouter()

const id = computed(() => (route.query.id ? Number(route.query.id) : null))
const isEdit = computed(() => id.value !== null)
const title = computed(() => (isEdit.value ? '编辑菜品' : '新增菜品'))

const formRef = ref<FormInstance>()
const pageLoading = ref(false)
const submitting = ref(false)
const categoryOptions = ref<{ id: number; name: string }[]>([])
const flavorInputs = ref<Record<number, string>>({})
const maxFlavors = 10

const form = ref({ name: '', categoryId: null as number | null, price: null as number | null, image: '', description: '', status: 0 })
const flavors = ref<FlavorItem[]>([])

const presetFlavors = [
  { name: '甜味', value: ['无糖', '少糖', '半糖', '多糖', '全糖'] },
  { name: '温度', value: ['热饮', '常温', '去冰', '少冰', '多冰'] },
  { name: '忌口', value: ['不要葱', '不要蒜', '不要香菜', '不要辣'] },
  { name: '辣度', value: ['不辣', '微辣', '中辣', '重辣'] },
]

const leftFlavorOptions = computed(() => {
  const selected = new Set(flavors.value.map(f => f.name))
  const fromPresets = presetFlavors.map(f => f.name).filter(n => !selected.has(n))
  return [...new Set([...fromPresets])]
})

const nameRegex = /^[A-Za-z0-9\u4e00-\u9fa5]{2,20}$/
const priceRegex = /^([1-9]\d{0,5}|0)(\.\d{1,2})?$/

const rules: FormRules = {
  name: [
    { required: true, message: '请输入菜品名称', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: Function) => {
        if (!value) return callback(new Error('请输入菜品名称'))
        if (!nameRegex.test(value)) return callback(new Error('菜品名称输入不符，请输入2-20个字符（汉字/字母/数字）'))
        callback()
      },
      trigger: 'blur',
    },
  ],
  categoryId: [{ required: true, message: '请选择菜品分类', trigger: 'change' }],
  price: [
    { required: true, message: '请设置菜品价格', trigger: 'blur' },
    {
      validator: (_rule: any, value: any, callback: Function) => {
        if (value === null || value === '') return callback(new Error('请设置菜品价格'))
        if (!priceRegex.test(String(value))) return callback(new Error('菜品价格格式有误，请输入大于零且最多保留两位小数的金额'))
        if (Number(value) <= 0) return callback(new Error('价格必须大于0'))
        callback()
      },
      trigger: 'blur',
    },
  ],
  image: [{ required: true, message: '请上传菜品图片', trigger: 'change' }],
}

const categoryName = computed(() => categoryOptions.value.find(item => item.id === form.value.categoryId)?.name || '')
const priceText = computed(() => (form.value.price === null || form.value.price === undefined || form.value.price === ('' as any) ? '-' : Number(form.value.price).toFixed(2)))

async function loadData() {
  pageLoading.value = true
  try {
    const [catRes] = await Promise.all([getCategoryList(1)])
    if (catRes.code === 1) categoryOptions.value = catRes.data || []
    if (isEdit.value) {
      const r = await getDishById(id.value!)
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
        flavors.value = (d.flavors || []).map((f: any) => ({
          name: f.name || '',
          value: (() => {
            try {
              return JSON.parse(f.value || '[]')
            } catch {
              return []
            }
          })(),
        }))
      }
    }
  } finally {
    pageLoading.value = false
  }
}

function addFlavor() {
  flavors.value.push({ name: '', value: [] })
}

function removeFlavor(i: number) {
  flavors.value.splice(i, 1)
}

function addFlavorTag(fi: number) {
  const value = flavorInputs.value[fi]?.trim()
  if (!value) return
  flavors.value[fi].value.push(value)
  flavorInputs.value[fi] = ''
}

function removeFlavorTag(fi: number, ti: number) {
  flavors.value[fi].value.splice(ti, 1)
}

function onFlavorNameChange(fi: number, name: string) {
  const preset = presetFlavors.find(f => f.name === name)
  if (preset && flavors.value[fi].value.length === 0) {
    flavors.value[fi].value = [...preset.value]
  }
}

async function submit(continueAdd: boolean) {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  if (!form.value.image) {
    ElMessage.error('菜品图片不能为空')
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
      flavors: flavors.value.filter(f => f.name).map(f => ({ name: f.name, value: JSON.stringify(f.value) })),
    }
    if (isEdit.value) payload.id = id.value
    const r = isEdit.value ? await updateDish(payload) : await addDish(payload)
    if (r.code === 1) {
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
      if (continueAdd) {
        form.value = { name: '', categoryId: null, price: null, image: '', description: '', status: 0 }
        flavors.value = []
      } else {
        router.push('/dish')
      }
    }
  } catch {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

function goBack() {
  router.push('/dish')
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
  grid-template-columns: minmax(320px, 0.8fr) minmax(0, 1.4fr);
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

.flavor-card {
  width: 100%;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
  background: #fafafa;
}

.flavor-toolbar {
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

.flavor-empty {
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

.flavor-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.flavor-item {
  padding: 12px;
  border-radius: 10px;
  background: #fff;
  border: 1px solid #e5e7eb;
}

.flavor-item__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
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

@media (max-width: 1100px) {
  .editor-grid {
    grid-template-columns: 1fr;
  }

  .side-panel {
    position: static;
  }
}
</style>
