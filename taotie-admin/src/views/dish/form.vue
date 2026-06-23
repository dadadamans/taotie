<template>
  <div class="page">

    <div class="page-head">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon> 返回菜品列表
      </el-button>
      <h2 style="margin-top: 8px">{{ title }}</h2>
    </div>

    <div class="form-card">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" v-loading="pageLoading">
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="菜品名称" prop="name">
              <el-input v-model="form.name" placeholder="请填写菜品名称" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜品分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择菜品分类" style="width: 100%">
                <el-option v-for="item in categoryOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="菜品价格" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" style="width: 200px" />
              <span style="margin-left: 8px; color: #888">元</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="口味做法配置">
          <div class="flavor-section">
            <el-button v-if="flavors.length === 0" type="primary" link @click="addFlavor">
              + 添加口味
            </el-button>
            <template v-for="(flavor, fi) in flavors" :key="fi">
              <div class="flavor-row">
                <div class="flavor-name">
                  <el-select v-model="flavor.name" placeholder="口味名" style="width: 120px" @change="(v: string) => flavor.name = v">
                    <el-option v-for="opt in flavorOptions" :key="opt" :label="opt" :value="opt" />
                  </el-select>
                </div>
                <div class="flavor-tags">
                  <el-tag v-for="(tag, ti) in flavor.value" :key="ti" closable size="small" @close="removeFlavorTag(fi, ti)">
                    {{ tag }}
                  </el-tag>
                  <el-input
                    :model-value="flavorInputs[fi] || ''"
                    @update:model-value="(v: string) => flavorInputs[fi] = v"
                    placeholder="输入标签后回车"
                    size="small"
                    style="width: 140px"
                    @keydown="(e: KeyboardEvent) => { if (e.key === 'Enter') { e.preventDefault(); addFlavorTag(fi) } }"
                  />
                </div>
                <el-button type="danger" link size="small" @click="removeFlavor(fi)">删除</el-button>
              </div>
              <el-button v-if="fi === flavors.length - 1 && flavors.length < 10" type="primary" link @click="addFlavor" style="margin-top: 8px">
                + 添加口味
              </el-button>
            </template>
          </div>
        </el-form-item>

        <el-form-item label="菜品图片" prop="image">
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
              图片大小不超过2M<br>仅能上传 PNG JPEG JPG类型图片<br>建议上传200*200或300*300尺寸的图片
            </div>
          </div>
        </el-form-item>

        <el-form-item label="菜品描述">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="200" placeholder="菜品描述，最长200字" style="width: 480px" />
        </el-form-item>

        <el-form-item>
          <el-button @click="goBack">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submit(false)">保存</el-button>
          <el-button v-if="!isEdit" type="primary" :loading="submitting" @click="submit(true)">保存并继续添加</el-button>
        </el-form-item>
      </el-form>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addDish, updateDish, getDishById, getCategoryList, uploadFile } from '@/api/shop'
import type { FormInstance, FormRules } from 'element-plus'

interface FlavorItem { name: string; value: string[] }

const route = useRoute()
const router = useRouter()

const id = computed(() => (route.query.id ? Number(route.query.id) : null))
const isEdit = computed(() => id.value !== null)
const title = computed(() => (isEdit.value ? '编辑菜品' : '新增菜品'))

const formRef = ref<FormInstance>()
const pageLoading = ref(false)
const submitting = ref(false)
const uploadLoading = ref(false)

const categoryOptions = ref<{ id: number; name: string }[]>([])
const flavorInputs = ref<Record<number, string>>({})

const form = ref({ name: '', categoryId: null as number | null, price: null as number | null, image: '', description: '', status: 1 })
const flavors = ref<FlavorItem[]>([])

const flavorOptions = ['甜度', '辣度', '温度', '做法', '规格', '口味', '冰量']

const rules: FormRules = {
  name: [{ required: true, message: '请输入菜品名称', trigger: 'blur' }, { max: 20, message: '名称不超过20个字符', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择菜品分类', trigger: 'change' }],
  price: [{ required: true, message: '请设置菜品价格', trigger: 'blur' }, { type: 'number', min: 0.01, message: '价格必须大于0', trigger: 'blur' }],
  image: [{ required: true, message: '请上传菜品图片', trigger: 'change' }],
}

async function loadData() {
  pageLoading.value = true
  try {
    const [catRes] = await Promise.all([getCategoryList(1)])
    if (catRes.code === 1) categoryOptions.value = catRes.data
    if (isEdit.value) {
      const r = await getDishById(id.value!)
      if (r.code === 1) {
        const d = r.data
        form.value = { name: d.name || '', categoryId: d.categoryId ?? null, price: d.price ?? null, image: d.image || '', description: d.description || '', status: d.status ?? 1 }
        flavors.value = (d.flavors || []).map((f: any) => ({ name: f.name || '', value: (() => { try { return JSON.parse(f.value || '[]') } catch { return [] } })() }))
      }
    }
  } finally { pageLoading.value = false }
}

async function handleUpload(file: File) {
  if (file.size > 2 * 1024 * 1024) { ElMessage.error('图片大小不能超过 2M'); return }
  uploadLoading.value = true
  try { const r = await uploadFile(file); if (r.code === 1) form.value.image = r.data } catch { ElMessage.error('上传失败') } finally { uploadLoading.value = false }
}

function addFlavor() { flavors.value.push({ name: '', value: [] }) }
function removeFlavor(i: number) { flavors.value.splice(i, 1) }
function addFlavorTag(fi: number) {
  const v = flavorInputs.value[fi]?.trim(); if (!v) return
  flavors.value[fi].value.push(v); flavorInputs.value[fi] = ''
}
function removeFlavorTag(fi: number, ti: number) { flavors.value[fi].value.splice(ti, 1) }

async function submit(continueAdd: boolean) {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }
  submitting.value = true
  try {
    const payload: Record<string, any> = {
      name: form.value.name, categoryId: form.value.categoryId, price: form.value.price,
      image: form.value.image, description: form.value.description, status: form.value.status,
      flavors: flavors.value.filter(f => f.name).map(f => ({ name: f.name, value: JSON.stringify(f.value) })),
    }
    if (isEdit.value) payload.id = id.value
    const r = isEdit.value ? await updateDish(payload) : await addDish(payload)
    if (r.code === 1) {
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
      if (continueAdd) {
        form.value = { name: '', categoryId: null, price: null, image: '', description: '', status: 1 }
        flavors.value = []
      } else {
        router.push('/dish')
      }
    }
  } catch { ElMessage.error('操作失败') } finally { submitting.value = false }
}

function goBack() { router.push('/dish') }

onMounted(loadData)
</script>

<style scoped lang="scss">
.page { width: 100%; }
.page-head { margin-bottom: 20px; h2 { margin: 0; font-size: 20px; } }
.form-card {
  background: #fff; border-radius: 10px; padding: 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05); max-width: 960px;
}
.flavor-section { width: 100%; max-width: 640px; }
.flavor-row {
  display: flex; gap: 10px; align-items: flex-start; margin-bottom: 8px;
  padding: 12px; background: #f9fafb; border-radius: 8px;
}
.flavor-tags { display: flex; flex-wrap: wrap; gap: 4px; align-items: center; flex: 1; min-width: 0; }
.upload-wrap { display: flex; gap: 16px; align-items: flex-start; }
.preview-box { position: relative; }
.preview-del { position: absolute; top: -8px; right: -8px; width: 22px; height: 22px; padding: 0; }
.upload-hint { font-size: 12px; color: #9ca3af; line-height: 1.6; }
</style>
