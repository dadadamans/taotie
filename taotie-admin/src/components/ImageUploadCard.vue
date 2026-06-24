<template>
  <div class="image-upload-card">
    <div class="image-upload-card__head">
      <div>
        <p class="title">{{ props.title }}</p>
        <p v-if="props.subtitle" class="sub">{{ props.subtitle }}</p>
      </div>
      <el-tag v-if="displaySrc" type="success" size="small">已上传</el-tag>
    </div>

    <el-upload
      ref="uploadRef"
      class="upload-trigger"
      :show-file-list="false"
      :accept="props.accept"
      :before-upload="handleBeforeUpload"
      :disabled="loading"
    >
      <div v-if="!displaySrc" class="empty">
        <el-icon :size="32">
          <UploadFilled />
        </el-icon>
        <strong>{{ loading ? '上传中...' : '点击上传图片' }}</strong>
        <span>支持 JPG / JPEG / PNG，最大 {{ props.maxSizeMb }}M</span>
      </div>

      <div v-else class="preview">
        <img :src="displaySrc" class="preview-image" alt="preview" />
        <div class="preview-mask">
          <span>点击图片可更换</span>
        </div>
        <el-button
          class="preview-delete"
          circle
          size="small"
          type="danger"
          @click.stop.prevent="handleRemove"
        >
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
    </el-upload>

    <p class="hint">
      <slot>{{ props.hint }}</slot>
    </p>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadFile } from '@/api/shop'

const props = withDefaults(defineProps<{
  modelValue: string
  title?: string
  subtitle?: string
  hint?: string
  accept?: string
  maxSizeMb?: number
}>(), {
  title: '图片上传',
  subtitle: '',
  hint: '图片大小不超过2M，仅能上传 PNG / JPEG / JPG 类型图片，建议使用 300 x 300 以上清晰图片',
  accept: 'image/png,image/jpeg,image/jpg',
  maxSizeMb: 2,
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const uploadRef = ref<any>(null)
const loading = ref(false)
const localPreview = ref('')
const committedSrc = ref('')
let previewObjectUrl = ''
let preloadToken = 0

const displaySrc = computed(() => localPreview.value || committedSrc.value)

watch(
  () => props.modelValue,
  (value) => {
    if (!value) {
      committedSrc.value = ''
      clearLocalPreview()
      return
    }
    if (value === committedSrc.value) return
    preloadCommittedSrc(value)
  },
  { immediate: true },
)

function clearUploadFiles() {
  uploadRef.value?.clearFiles?.()
}

function clearLocalPreview() {
  if (previewObjectUrl) {
    URL.revokeObjectURL(previewObjectUrl)
    previewObjectUrl = ''
  }
  localPreview.value = ''
}

function preloadCommittedSrc(url: string) {
  const currentToken = ++preloadToken
  const image = new Image()
  image.onload = () => {
    if (currentToken !== preloadToken) return
    committedSrc.value = url
    if (!localPreview.value) return
    clearLocalPreview()
  }
  image.onerror = () => {
    if (currentToken !== preloadToken) return
    committedSrc.value = url
  }
  image.src = url
}

async function handleBeforeUpload(file: File) {
  const maxBytes = props.maxSizeMb * 1024 * 1024
  if (file.size > maxBytes) {
    ElMessage.error(`图片大小不能超过 ${props.maxSizeMb}M`)
    clearUploadFiles()
    return false
  }

  clearLocalPreview()
  previewObjectUrl = URL.createObjectURL(file)
  localPreview.value = previewObjectUrl
  loading.value = true
  try {
    const res = await uploadFile(file)
    if (res.code === 1) {
      emit('update:modelValue', res.data)
    } else {
      ElMessage.error(res.msg || '上传失败')
      clearLocalPreview()
    }
  } catch {
    ElMessage.error('上传失败')
    clearLocalPreview()
  } finally {
    loading.value = false
    clearUploadFiles()
  }

  return false
}

function handleRemove() {
  emit('update:modelValue', '')
  committedSrc.value = ''
  clearLocalPreview()
  clearUploadFiles()
}

onBeforeUnmount(() => {
  clearLocalPreview()
})
</script>

<style scoped lang="scss">
.image-upload-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.image-upload-card__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;

  .title {
    margin: 0;
    font-size: 15px;
    font-weight: 600;
    color: #111827;
  }

  .sub {
    margin: 4px 0 0;
    font-size: 12px;
    color: #6b7280;
    line-height: 1.5;
  }
}

.upload-trigger {
  width: 100%;
}

.empty,
.preview {
  position: relative;
  width: 100%;
  min-height: 240px;
  border: 1px dashed #d1d5db;
  border-radius: 12px;
  overflow: hidden;
  background: #f9fafb;
  transition: border-color 0.2s ease, background 0.2s ease;
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #6b7280;
  text-align: center;
  cursor: pointer;

  strong {
    font-size: 14px;
    color: #111827;
  }

  span {
    font-size: 12px;
  }

  &:hover {
    border-color: #7c3aed;
    background: #f5f3ff;
  }
}

.preview {
  cursor: pointer;
}

.preview-image {
  width: 100%;
  height: 100%;
  min-height: 240px;
  display: block;
  object-fit: cover;
}

.preview-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, rgba(17, 24, 39, 0.02), rgba(17, 24, 39, 0.12));
  color: #fff;
  font-size: 13px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.preview:hover .preview-mask {
  opacity: 1;
}

.preview-delete {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 2;
}

.hint {
  margin: 0;
  font-size: 12px;
  color: #6b7280;
  line-height: 1.6;
}
</style>
