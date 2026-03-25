/**
 * 应用级状态管理
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  /** 侧边栏是否折叠（管理后台用） */
  const sidebarCollapsed = ref(false)

  /** 全局 loading */
  const globalLoading = ref(false)

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  return {
    sidebarCollapsed,
    globalLoading,
    toggleSidebar,
  }
})
