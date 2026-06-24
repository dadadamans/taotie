<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { getShopStatus, setShopStatus } from '@/api/shop'
import { editPassword } from '@/api/employee'
import ConfirmDialog from '@/components/ConfirmDialog.vue'

const appStore = useAppStore()
const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const sidebarWidth = computed(() =>
  appStore.sidebarCollapsed ? 'var(--sidebar-collapsed-width)' : 'var(--sidebar-width)',
)

// ---- 营业状态 ----
const shopStatus = ref(1)
const statusDialogVisible = ref(false)
const statusLoading = ref(false)
async function loadShopStatus() {
  try {
    const res = await getShopStatus()
    if (res.code === 1) shopStatus.value = res.data
  } catch {}
}
async function handleStatusSave() {
  statusLoading.value = true
  try {
    const res = await setShopStatus(shopStatus.value)
    if (res.code === 1) {
      ElMessage.success(shopStatus.value === 1 ? '已切换为营业中' : '已切换为打烊中')
      statusDialogVisible.value = false
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败')
  } finally {
    statusLoading.value = false
  }
}

// ---- 智能客服 ----
const chatVisible = ref(false)

// ---- 修改密码 ----
const pwdDialogVisible = ref(false)
const logoutDialogVisible = ref(false)
const pwdForm = ref({ empId: 0, oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdLoading = ref(false)
async function handlePwdSave() {
  if (!pwdForm.value.oldPassword || !pwdForm.value.newPassword) {
    ElMessage.warning('请填写完整')
    return
  }
  if (pwdForm.value.newPassword !== pwdForm.value.confirmPassword) {
    ElMessage.warning('两次密码不一致')
    return
  }
  pwdLoading.value = true
  try {
    const res = await editPassword({
      empId: userStore.userInfo?.id || 0,
      oldPassword: pwdForm.value.oldPassword,
      newPassword: pwdForm.value.newPassword,
    })
    if (res.code === 1) {
      ElMessage.success('密码修改成功')
      pwdDialogVisible.value = false
      pwdForm.value = { empId: 0, oldPassword: '', newPassword: '', confirmPassword: '' }
    } else {
      ElMessage.error(res.msg || '修改失败')
    }
  } catch {
    ElMessage.error('修改失败')
  } finally {
    pwdLoading.value = false
  }
}

function handleLogout() {
  logoutDialogVisible.value = true
}

function confirmLogout() {
  userStore.logout()
  router.push('/login')
}

onMounted(() => {
  loadShopStatus()
})
</script>

<template>
  <div class="layout">
    <aside class="sidebar" :style="{ width: sidebarWidth }">
      <div class="sidebar-logo">
        <img src="/favicon.svg" alt="饕餮外卖" class="logo-icon" />
        <span v-show="!appStore.sidebarCollapsed" class="logo-text">饕餮外卖</span>
      </div>
      <el-menu
        :default-active="route.path"
        :collapse="appStore.sidebarCollapsed"
        background-color="#0C0A1D"
        text-color="#9CA3AF"
        active-text-color="#6D28D9"
      >
        <el-menu-item index="/dashboard" @click="router.push('/dashboard')">
          <el-icon><Odometer /></el-icon>
          <span>工作台</span>
        </el-menu-item>
        <el-menu-item index="/order" @click="router.push('/order')">
          <el-icon><List /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/dish" @click="router.push('/dish')">
          <el-icon><ColdDrink /></el-icon>
          <span>菜品管理</span>
        </el-menu-item>
        <el-menu-item index="/setmeal" @click="router.push('/setmeal')">
          <el-icon><Present /></el-icon>
          <span>套餐管理</span>
        </el-menu-item>
        <el-menu-item index="/category" @click="router.push('/category')">
          <el-icon><CollectionTag /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/employee" @click="router.push('/employee')">
          <el-icon><User /></el-icon>
          <span>员工管理</span>
        </el-menu-item>
        <el-menu-item index="/statistics" @click="router.push('/statistics')">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据统计</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <div class="main-area" :style="{ marginLeft: sidebarWidth }">
      <header class="topbar">
        <div class="topbar-left">
          <el-button text @click="appStore.toggleSidebar">
            <el-icon :size="20">
              <Fold v-if="!appStore.sidebarCollapsed" />
              <Expand v-else />
            </el-icon>
          </el-button>
          <el-breadcrumb>
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ route.meta?.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="topbar-right">
          <span
            class="status-badge"
            :class="shopStatus === 1 ? 'is-open' : 'is-closed'"
            @click="statusDialogVisible = true"
          >
            <span class="status-dot" />
            {{ shopStatus === 1 ? '营业中' : '打烊中' }}
          </span>

          <el-tooltip content="智能客服" placement="bottom">
            <el-button text @click="chatVisible = true">
              <el-icon :size="20"><ChatDotSquare /></el-icon>
            </el-button>
          </el-tooltip>

          <el-dropdown trigger="click">
            <span class="user-info">
              <el-avatar :size="32" style="background: #6D28D9">
                {{ userStore.userInfo?.name?.charAt(0) || '管' }}
              </el-avatar>
              <span class="user-name">{{ userStore.userInfo?.name || '管理员' }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-item @click="pwdDialogVisible = true">
                <el-icon><Lock /></el-icon>修改密码
              </el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">
                <el-icon><SwitchButton /></el-icon>退出登录
              </el-dropdown-item>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="content">
        <router-view />
      </main>
    </div>
  </div>

  <!-- 营业状态 Dialog -->
  <el-dialog v-model="statusDialogVisible" title="营业状态设置" width="420px" :close-on-click-modal="false">
    <el-radio-group v-model="shopStatus" class="status-radio-group">
      <el-radio :value="1" class="status-radio">
        <div class="status-radio-content">
          <span class="status-radio-title">营业中</span>
          <span class="status-radio-desc">当前餐厅处于营业状态，自动接收任何订单</span>
        </div>
      </el-radio>
      <el-radio :value="0" class="status-radio">
        <div class="status-radio-content">
          <span class="status-radio-title">打烊中</span>
          <span class="status-radio-desc">当前餐厅处于打烊状态，仅接受营业时间内的预定订单</span>
        </div>
      </el-radio>
    </el-radio-group>
    <template #footer>
      <el-button @click="statusDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="statusLoading" @click="handleStatusSave">确定</el-button>
    </template>
  </el-dialog>

  <!-- 智能客服 Dialog -->
  <el-dialog v-model="chatVisible" title="智能客服" width="400px" :close-on-click-modal="false">
    <div class="chat-placeholder">
      <el-icon :size="48" color="#9CA3AF"><ChatLineSquare /></el-icon>
      <p>智能客服功能开发中，敬请期待</p>
      <p style="font-size: 12px; color: #9CA3AF; margin-top: 4px">您可以在后面集成 AI 对话接口</p>
    </div>
    <template #footer>
      <el-button @click="chatVisible = false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 修改密码 Dialog -->
  <el-dialog v-model="pwdDialogVisible" title="修改密码" width="420px" :close-on-click-modal="false">
    <el-form :model="pwdForm" label-width="100px">
      <el-form-item label="原密码">
        <el-input v-model="pwdForm.oldPassword" type="password" placeholder="请输入原密码" />
      </el-form-item>
      <el-form-item label="新密码">
        <el-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码" />
      </el-form-item>
      <el-form-item label="确认密码">
        <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="pwdDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="pwdLoading" @click="handlePwdSave">确定</el-button>
    </template>
  </el-dialog>

  <ConfirmDialog
    v-model="logoutDialogVisible"
    type="info"
    title="确认退出登录？"
    description="退出后需要重新登录"
    confirm-text="退出"
    @confirm="confirmLogout"
  />
</template>

<style lang="scss" scoped>
.layout {
  height: 100vh;
  display: flex;
}

.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  background: $color-sidebar-bg;
  z-index: 100;
  transition: width 0.28s;
  overflow: hidden;
  display: flex;
  flex-direction: column;

  .sidebar-logo {
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    padding: 0 16px;
    flex-shrink: 0;
    border-bottom: 1px solid rgba(255, 255, 255, 0.06);

    .logo-icon {
      width: 36px;
      height: 36px;
      flex-shrink: 0;
    }
    .logo-text {
      font-size: 20px;
      font-weight: 700;
      color: $color-accent;
      white-space: nowrap;
      letter-spacing: 2px;
    }
  }

  .el-menu {
    border-right: none;
    flex: 1;
    overflow-y: auto;

    .el-menu-item {
      margin: 2px 8px;
      border-radius: 6px;
      &.is-active {
        background: $color-sidebar-active !important;
        border-left: 3px solid $color-primary;
      }
      &:hover {
        background: $color-sidebar-hover !important;
      }
    }
  }
}

.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  transition: margin-left 0.28s;
}

.topbar {
  height: $topbar-height;
  background: $color-card;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: $box-shadow;
  position: sticky;
  top: 0;
  z-index: 50;
  border-bottom: 2px solid $color-primary-light;

  .topbar-left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .topbar-right {
    display: flex;
    align-items: center;
    gap: 12px;

    .status-badge {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 4px 12px;
      border-radius: 20px;
      font-size: 13px;
      cursor: pointer;
      transition: all 0.2s;

      .status-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
      }

      &.is-open {
        background: #D1FAE5;
        color: #059669;
        .status-dot { background: #10B981; }
      }
      &.is-closed {
        background: #FEE2E2;
        color: #DC2626;
        .status-dot { background: #EF4444; }
      }
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;

      .user-name {
        font-size: 14px;
        color: $color-text-primary;
      }
    }
  }
}

.content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f0f2f5;
}

/* status dialog */
:deep(.status-radio-group) {
  width: 100%;
  .status-radio {
    width: 100%;
    margin-bottom: 12px;
    .el-radio__label { width: 100%; }
  }
  .status-radio-content {
    display: flex;
    flex-direction: column;
    .status-radio-title { font-weight: 600; font-size: 15px; }
    .status-radio-desc { font-size: 12px; color: #6b7280; margin-top: 4px; }
  }
}

.chat-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32px;
  gap: 12px;
  color: #6b7280;
}
</style>
