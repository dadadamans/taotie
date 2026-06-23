import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const service: AxiosInstance = axios.create({
  baseURL: '/',
  timeout: 60000,
})

service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.token = token
    }
    return config
  },
  (error) => Promise.reject(error),
)

service.interceptors.response.use(
  (response: AxiosResponse) => {
    const { data } = response
    if (data.code === 0) {
      ElMessage.error(data.msg || '请求失败')
      return Promise.reject(data)
    }
    return data
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      router.push('/login')
    }
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  },
)

export default service
