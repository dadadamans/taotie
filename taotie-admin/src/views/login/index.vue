<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const form = ref({ username: 'admin', password: '123456' })
const loading = ref(false)

async function handleLogin() {
  loading.value = true
  try {
    const res = await userStore.login(form.value)
    if (res.code === 1) {
      router.push('/')
    } else {
      ElMessage.error(res.msg || '登录失败')
    }
  } catch {
    ElMessage.error('登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <img src="/favicon.svg" alt="饕餮外卖" class="login-logo" />
        <h1 class="login-title">饕餮外卖</h1>
        <p class="login-subtitle">管理后台</p>
      </div>
      <el-form @submit.prevent="handleLogin">
        <el-form-item>
          <el-input v-model="form.username" placeholder="账号" :prefix-icon="'User'" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            :prefix-icon="'Lock'"
            size="large"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            style="width: 100%"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0C0A1D 0%, #1A0A2E 50%, #2D1B69 100%);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle at 30% 50%, rgba(245, 158, 11, 0.08) 0%, transparent 50%),
                radial-gradient(circle at 70% 50%, rgba(109, 40, 217, 0.1) 0%, transparent 50%);
    animation: drift 20s ease-in-out infinite;
  }
}

@keyframes drift {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(-5%, 5%); }
}

.login-card {
  position: relative;
  width: 420px;
  padding: 48px 40px;
  background: rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;

  .login-logo {
    width: 56px;
    height: 56px;
    margin-bottom: 16px;
  }

  .login-title {
    font-size: 28px;
    font-weight: 700;
    color: $color-accent;
    letter-spacing: 4px;
    margin-bottom: 4px;
  }

  .login-subtitle {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.5);
    letter-spacing: 2px;
  }
}

:deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: none;
  border-radius: 8px;

  .el-input__inner {
    color: #fff;

    &::placeholder {
      color: rgba(255, 255, 255, 0.35);
    }
  }

  .el-input__prefix-inner {
    color: rgba(255, 255, 255, 0.4);
  }
}

:deep(.el-button--primary) {
  height: 48px;
  font-size: 16px;
  letter-spacing: 2px;
  border-radius: 8px;
  background: linear-gradient(135deg, $color-primary, $color-primary-light);
  border: none;

  &:hover {
    background: linear-gradient(135deg, $color-primary-hover, $color-primary);
  }
}
</style>
