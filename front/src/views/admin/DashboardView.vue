<script setup lang="ts">
/**
 * DashboardView - 角色看板骨架页
 */
import { computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { resolveAdminMenus } from '@/utils/permission'

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()

const { dashboardOverview } = storeToRefs(appStore)
const { permissionCodes } = storeToRefs(userStore)

const cards = computed(() => dashboardOverview.value?.cards || [])
const menuItems = computed(() => resolveAdminMenus(permissionCodes.value).flatMap((group) => group.items).slice(0, 6))
const dashboardTitle = computed(() => (route.meta.title as string) || '角色看板')
const dashboardCode = computed(() => dashboardOverview.value?.dashboardCode || 'DASH_GENERAL')

onMounted(async () => {
  if (!dashboardOverview.value) {
    await appStore.fetchDashboardOverview()
  }
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

    <section class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
      <article
        v-for="card in cards"
        :key="card.code"
        class="liquid-glass rounded-2xl p-5"
      >
        <p class="text-xs text-white/55 font-body">{{ card.title }}</p>
        <p class="mt-3 text-3xl font-heading italic text-white">{{ card.value }}</p>
        <p class="mt-2 text-xs text-white/55 font-body">{{ card.description }}</p>
      </article>
      <article
        v-if="cards.length === 0"
        class="liquid-glass rounded-2xl p-5 md:col-span-2 xl:col-span-4"
      >
        <p class="text-sm text-white/65 font-body">当前角色暂无可展示卡片，后续可由后端看板策略动态下发。</p>
      </article>
    </section>

    <section class="liquid-glass rounded-3xl p-6">
      <h3 class="text-sm text-white/80 font-body mb-4">常用管理入口</h3>
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
