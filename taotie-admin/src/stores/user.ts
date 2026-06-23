import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<Record<string, any>>({})

  async function login(credentials: { username: string; password: string }) {
    const res = await loginApi(credentials)
    if (res.code === 1) {
      token.value = res.data.token
      userInfo.value = res.data
      localStorage.setItem('token', res.data.token)
    }
    return res
  }

  function logout() {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
  }

  return { token, userInfo, login, logout }
})
