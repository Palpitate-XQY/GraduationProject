/**
 * 应用级状态管理
 */
import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getDashboardOverview, type DashboardOverviewVO } from '@/api/home'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const globalLoading = ref(false)
  const dashboardOverview = ref<DashboardOverviewVO | null>(null)

  const pagePackCode = computed(() => dashboardOverview.value?.pagePackCode || 'PACK_FALLBACK')
  const dashboardCode = computed(() => dashboardOverview.value?.dashboardCode || 'DASH_GENERAL')
  const defaultHomeRoute = computed(() => dashboardOverview.value?.defaultHomeRoute || '/dashboard/general')

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function clearDashboardOverview() {
    dashboardOverview.value = null
  }

  async function fetchDashboardOverview(force = false) {
    if (dashboardOverview.value && !force) {
      return dashboardOverview.value
    }
    const res = await getDashboardOverview()
    dashboardOverview.value = res.data
    return dashboardOverview.value
  }

  return {
    sidebarCollapsed,
    globalLoading,
    dashboardOverview,
    pagePackCode,
    dashboardCode,
    defaultHomeRoute,
    toggleSidebar,
    clearDashboardOverview,
    fetchDashboardOverview,
  }
})
