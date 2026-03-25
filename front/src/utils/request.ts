/**
 * Axios 请求封装
 * - 统一 baseURL
 * - JWT Token 自动注入
 * - 响应拦截 & 错误处理
 * - 401 自动跳登录
 */
import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/types/common'

const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

/* ========== 请求拦截 ========== */
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('access_token')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

/* ========== 响应拦截 ========== */
service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data

    // 后端业务码 0 = 成功
    if (res.code === 0) {
      return res as any
    }

    // 401 未登录 / token 失效
    if (res.code === 401) {
      localStorage.removeItem('access_token')
      window.location.href = '/login'
      return Promise.reject(new Error(res.message || '未授权'))
    }

    // 其他业务错误
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('access_token')
      window.location.href = '/login'
    }
    ElMessage.error(error.message || '网络异常')
    return Promise.reject(error)
  },
)

export default service
