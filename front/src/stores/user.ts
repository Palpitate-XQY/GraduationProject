/**
 * 用户状态管理
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { CurrentUserVO } from '@/types/auth'
import { getMe, logout as apiLogout } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  /* ---- state ---- */
  const token = ref(localStorage.getItem('access_token') || '')
  const userInfo = ref<CurrentUserVO | null>(null)

  /* ---- getters ---- */
  const isLoggedIn = computed(() => !!token.value)
  const nickname = computed(() => userInfo.value?.nickname ?? '')
  const roleCodes = computed(() => userInfo.value?.roleCodes ?? [])
  const permissionCodes = computed(() => userInfo.value?.permissionCodes ?? [])

  /** 是否拥有某个权限 */
  const hasPermission = (code: string) => permissionCodes.value.includes(code)

  /* ---- actions ---- */

  /** 设置 token */
  function setToken(t: string) {
    token.value = t
    localStorage.setItem('access_token', t)
  }

  /** 获取当前用户信息 */
  async function fetchUserInfo() {
    try {
      const res = await getMe()
      userInfo.value = res.data
    } catch (e) {
      console.error('获取用户信息失败', e)
    }
  }

  /** 登出 */
  async function doLogout() {
    try {
      await apiLogout()
    } finally {
      token.value = ''
      userInfo.value = null
      localStorage.removeItem('access_token')
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    nickname,
    roleCodes,
    permissionCodes,
    hasPermission,
    setToken,
    fetchUserInfo,
    doLogout,
  }
})
