/**
 * Axios 请求封装
 * - 统一 baseURL
 * - 自动注入 JWT token
 * - 统一业务码处理（401/403/1004/1005）
 */
import axios from 'axios'
import type { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/types/common'

const UNAUTHORIZED_CODES = new Set([401])
const FORBIDDEN_CODES = new Set([403, 1004])
const INVALID_FLOW_CODES = new Set([1005])

function redirectToLogin() {
  localStorage.removeItem('access_token')
  if (window.location.pathname === '/login') return
  const redirect = encodeURIComponent(`${window.location.pathname}${window.location.search}`)
  window.location.href = `/login?redirect=${redirect}`
}

function redirectToForbidden() {
  if (window.location.pathname === '/403') return
  window.location.href = '/403'
}

const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

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

service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse<unknown>>) => {
    const res = response.data
    if (res.code === 0) {
      return res as any
    }

    if (UNAUTHORIZED_CODES.has(res.code)) {
      redirectToLogin()
      return Promise.reject(new Error(res.message || '未登录或登录已失效')) as any
    }

    if (FORBIDDEN_CODES.has(res.code)) {
      ElMessage.error(res.message || '无权限访问')
      redirectToForbidden()
      return Promise.reject(new Error(res.message || '无权限访问')) as any
    }

    if (INVALID_FLOW_CODES.has(res.code)) {
      ElMessage.warning(res.message || '操作状态已变化，请刷新后重试')
      return Promise.reject(new Error(res.message || '操作状态已变化，请刷新后重试')) as any
    }

    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败')) as any
  },
  (error) => {
    if (error.response?.status === 401) {
      redirectToLogin()
    } else if (error.response?.status === 403) {
      redirectToForbidden()
    } else {
      ElMessage.error(error.message || '网络异常')
    }
    return Promise.reject(error) as any
  },
)

export default service
