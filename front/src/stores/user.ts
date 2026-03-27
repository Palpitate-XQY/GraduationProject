/**
 * 用户状态管理
 */
import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getMe, logout as apiLogout } from '@/api/auth'
import type { CurrentUserVO } from '@/types/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('access_token') || '')
  const userInfo = ref<CurrentUserVO | null>(null)
  const userLoaded = ref(false)

  const isLoggedIn = computed(() => Boolean(token.value))
  const nickname = computed(() => userInfo.value?.nickname ?? '')
  const roleCodes = computed(() => userInfo.value?.roleCodes ?? [])
  const permissionCodes = computed(() => userInfo.value?.permissionCodes ?? [])
  const isResidentOnly = computed(() => {
    const roles = roleCodes.value
    return roles.length === 1 && roles[0] === 'RESIDENT'
  })
  const canAccessDashboard = computed(
    () => permissionCodes.value.includes('dashboard:view') && !isResidentOnly.value,
  )

  function hasPermission(code: string) {
    return permissionCodes.value.includes(code)
  }

  function hasAnyPermission(codes: string[]) {
    if (!codes.length) return true
    return codes.some((code) => permissionCodes.value.includes(code))
  }

  function setToken(value: string) {
    token.value = value
    localStorage.setItem('access_token', value)
  }

  function clearAuth() {
    token.value = ''
    userInfo.value = null
    userLoaded.value = false
    localStorage.removeItem('access_token')
  }

  async function fetchUserInfo(force = false) {
    if (userLoaded.value && userInfo.value && !force) {
      return userInfo.value
    }
    const res = await getMe()
    userInfo.value = res.data
    userLoaded.value = true
    return userInfo.value
  }

  async function ensureUserInfo() {
    if (!token.value) return null
    try {
      return await fetchUserInfo()
    } catch {
      clearAuth()
      throw new Error('用户登录已失效')
    }
  }

  async function doLogout() {
    try {
      await apiLogout()
    } finally {
      clearAuth()
    }
  }

  return {
    token,
    userInfo,
    userLoaded,
    isLoggedIn,
    nickname,
    roleCodes,
    permissionCodes,
    isResidentOnly,
    canAccessDashboard,
    hasPermission,
    hasAnyPermission,
    setToken,
    clearAuth,
    fetchUserInfo,
    ensureUserInfo,
    doLogout,
  }
})
