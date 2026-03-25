<script setup lang="ts">
/**
 * AdminLayout - 管理端骨架布局
 * 菜单按权限动态过滤，支持看板入口与 403 兜底联动。
 */
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useRoute, useRouter } from 'vue-router'
import { LayoutDashboard, LogOut, ShieldCheck } from 'lucide-vue-next'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { resolveAdminMenus } from '@/utils/permission'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const { permissionCodes } = storeToRefs(userStore)
const { pagePackCode, defaultHomeRoute } = storeToRefs(appStore)

const menuGroups = computed(() => resolveAdminMenus(permissionCodes.value))
const flattenedMenus = computed(() => menuGroups.value.flatMap((group) => group.items))
const pageTitle = computed(() => (route.meta.title as string) || '管理后台')

function isActive(path: string) {
  return route.path === path || route.path.startsWith(`${path}/`)
}

function navigate(path: string) {
  if (route.path === path) return
  router.push(path)
}

async function handleLogout() {
  await userStore.doLogout()
  appStore.clearDashboardOverview()
  router.push('/login')
}
</script>

<template>
  <div class="min-h-screen bg-slate-950 text-white">
    <aside class="hidden lg:flex fixed inset-y-0 left-0 w-72 border-r border-white/8 bg-slate-950/80 backdrop-blur-xl">
      <div class="w-full p-5">
        <button
          type="button"
          class="w-full liquid-glass rounded-2xl px-4 py-3 flex items-center justify-between"
          @click="navigate(defaultHomeRoute)"
        >
          <span class="flex items-center gap-2">
            <ShieldCheck :size="18" />
            <span class="font-body text-sm font-medium">智慧社区管理</span>
          </span>
          <span class="text-xs text-white/60">{{ pagePackCode }}</span>
        </button>

        <button
          type="button"
          class="mt-3 w-full rounded-xl px-3 py-2 text-left text-sm font-body text-white/85 hover:bg-white/8 transition-all flex items-center gap-2"
          :class="{ 'bg-white/10': route.path.startsWith('/dashboard/') }"
          @click="navigate(defaultHomeRoute)"
        >
          <LayoutDashboard :size="16" />
          角色看板
        </button>

        <div class="mt-6 space-y-5">
          <section v-for="group in menuGroups" :key="group.key">
            <h3 class="text-xs uppercase tracking-wider text-white/45 font-body mb-2">{{ group.title }}</h3>
            <div class="space-y-1">
              <button
                v-for="item in group.items"
                :key="item.key"
                type="button"
                class="w-full rounded-xl px-3 py-2 text-left text-sm font-body transition-all"
                :class="isActive(item.route) ? 'bg-white/12 text-white' : 'text-white/70 hover:bg-white/8 hover:text-white'"
                @click="navigate(item.route)"
              >
                {{ item.label }}
              </button>
            </div>
          </section>
        </div>
      </div>
    </aside>

    <div class="lg:pl-72">
      <header class="sticky top-0 z-20 px-4 md:px-6 py-4 border-b border-white/8 bg-slate-950/80 backdrop-blur-xl">
        <div class="flex items-center justify-between gap-4">
          <div>
            <h1 class="text-xl md:text-2xl font-heading italic tracking-tight">{{ pageTitle }}</h1>
            <p class="text-xs md:text-sm text-white/55 font-body mt-1">权限驱动菜单与路由，按角色看板落地默认首页</p>
          </div>
          <button
            type="button"
            class="liquid-glass rounded-full px-4 py-2 text-sm font-body text-white/85 hover:text-white flex items-center gap-2"
            @click="handleLogout"
          >
            <LogOut :size="15" />
            退出
          </button>
        </div>

        <div class="lg:hidden mt-4 flex items-center gap-2 overflow-x-auto pb-1">
          <button
            v-for="item in flattenedMenus"
            :key="item.key"
            type="button"
            class="shrink-0 rounded-full px-3 py-1.5 text-xs font-body transition-all"
            :class="isActive(item.route) ? 'liquid-glass-strong text-white' : 'liquid-glass text-white/75'"
            @click="navigate(item.route)"
          >
            {{ item.label }}
          </button>
        </div>
      </header>

      <main class="p-4 md:p-6 lg:p-8">
        <router-view />
      </main>
    </div>
  </div>
</template>
