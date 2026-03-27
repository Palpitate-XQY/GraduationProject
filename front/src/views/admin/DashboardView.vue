<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { getLatestAnalyticsReport } from '@/api/analytics'
import type { AnalyticsReportVO } from '@/types/analytics'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { resolveAdminMenus } from '@/utils/permission'

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()

const { dashboardOverview } = storeToRefs(appStore)
const { permissionCodes } = storeToRefs(userStore)

const latestAnalyticsReport = ref<AnalyticsReportVO | null>(null)
const analyticsLoading = ref(false)

const cards = computed(() => dashboardOverview.value?.cards || [])
const menuItems = computed(() => resolveAdminMenus(permissionCodes.value).flatMap((group) => group.items).slice(0, 6))
const dashboardTitle = computed(() => (route.meta.title as string) || '角色看板')
const dashboardCode = computed(() => dashboardOverview.value?.dashboardCode || 'DASH_GENERAL')
const canViewAnalytics = computed(
  () =>
    permissionCodes.value.includes('analytics:report:view') ||
    permissionCodes.value.includes('analytics:wordcloud:view'),
)

const analyticsInsights = computed(() => {
  const markdown = latestAnalyticsReport.value?.summaryMarkdown || ''
  return markdown
    .split('\n')
    .map((line) => line.trim())
    .filter((line) => line.startsWith('- '))
    .slice(0, 3)
    .map((line) => line.replace(/^- /, ''))
})

async function loadLatestAnalytics() {
  if (!canViewAnalytics.value) {
    latestAnalyticsReport.value = null
    return
  }
  analyticsLoading.value = true
  try {
    const res = await getLatestAnalyticsReport('DAILY')
    latestAnalyticsReport.value = res.data
  } catch {
    latestAnalyticsReport.value = null
  } finally {
    analyticsLoading.value = false
  }
}

onMounted(async () => {
  if (!dashboardOverview.value) {
    await appStore.fetchDashboardOverview()
  }
  await loadLatestAnalytics()
})
</script>

<template>
  <div class="space-y-5">
    <section class="liquid-glass rounded-3xl p-6 md:p-8">
      <p class="text-xs uppercase tracking-wider text-white/50 font-body">Dashboard</p>
      <h2 class="mt-2 text-3xl font-heading italic tracking-tight text-white">{{ dashboardTitle }}</h2>
      <p class="mt-2 text-sm text-white/70 font-body">
        当前看板编码：{{ dashboardCode }}，默认首页路由：{{ dashboardOverview?.defaultHomeRoute || '/dashboard/general' }}
      </p>
    </section>

    <section class="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
      <article v-for="card in cards" :key="card.code" class="liquid-glass rounded-2xl p-5">
        <p class="text-xs text-white/55 font-body">{{ card.title }}</p>
        <p class="mt-3 text-3xl font-heading italic text-white">{{ card.value }}</p>
        <p class="mt-2 text-xs text-white/55 font-body">{{ card.description }}</p>
      </article>
      <article v-if="cards.length === 0" class="liquid-glass rounded-2xl p-5 md:col-span-2 xl:col-span-4">
        <p class="text-sm text-white/65 font-body">当前角色暂无可展示卡片，后续可由后端看板策略动态下发。</p>
      </article>
    </section>

    <section v-if="canViewAnalytics" class="liquid-glass rounded-3xl p-6">
      <div class="flex items-center justify-between gap-3">
        <h3 class="text-sm text-white/80 font-body">智能日报摘要</h3>
        <el-button size="small" @click="router.push('/dashboard/analytics')">进入分析中心</el-button>
      </div>
      <div v-loading="analyticsLoading" class="mt-3">
        <p v-if="!latestAnalyticsReport" class="text-sm text-white/65 font-body">暂无分析摘要</p>
        <ul v-else class="space-y-2 text-sm text-white/85 font-body">
          <li v-for="(line, idx) in analyticsInsights" :key="idx">- {{ line }}</li>
        </ul>
      </div>
    </section>

    <section class="liquid-glass rounded-3xl p-6">
      <h3 class="mb-4 text-sm text-white/80 font-body">常用管理入口</h3>
      <div class="flex flex-wrap gap-3">
        <button
          v-for="item in menuItems"
          :key="item.key"
          type="button"
          class="liquid-glass rounded-full px-4 py-2 text-sm text-white/85 font-body hover:text-white transition-all"
          @click="router.push(item.route)"
        >
          {{ item.label }}
        </button>
      </div>
    </section>
  </div>
</template>
