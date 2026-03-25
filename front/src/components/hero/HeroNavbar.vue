<script setup lang="ts">
/**
 * HeroNavbar — 全屏悬浮导航栏
 * 悬浮在视频之上，透明+液态玻璃质感
 * 导航项基于后端业务模块设计
 */
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Menu, X, LogIn, User } from 'lucide-vue-next'
import { navItems } from '@/composables/useHeroContent'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

/** 移动端菜单是否展开 */
const mobileMenuOpen = ref(false)

/** 是否已经滚动过（用于添加背景模糊） */
const scrolled = ref(false)

function handleScroll() {
  scrolled.value = window.scrollY > 50
}

onMounted(() => window.addEventListener('scroll', handleScroll, { passive: true }))
onUnmounted(() => window.removeEventListener('scroll', handleScroll))

function navigate(href: string) {
  mobileMenuOpen.value = false
  if (href.startsWith('#')) {
    document.querySelector(href)?.scrollIntoView({ behavior: 'smooth' })
  } else {
    router.push(href)
  }
}
</script>

<template>
  <nav
    :class="[
      'fixed top-4 left-0 right-0 z-50 px-6 lg:px-16 transition-all duration-500',
    ]"
  >
    <div
      :class="[
        'mx-auto max-w-7xl rounded-full px-6 py-3 flex items-center justify-between transition-all duration-500',
        scrolled ? 'liquid-glass-strong bg-black/20' : 'bg-transparent',
      ]"
    >
      <!-- Logo -->
      <a
        class="flex items-center gap-2 cursor-pointer select-none"
        @click.prevent="navigate('/')"
      >
        <div class="w-8 h-8 rounded-full bg-white/10 flex items-center justify-center">
          <span class="text-white font-heading text-lg italic font-bold">S</span>
        </div>
        <span class="text-white font-heading text-xl italic tracking-tight hidden sm:inline">
          SmartCommunity
        </span>
      </a>

      <!-- 桌面导航 -->
      <div class="hidden md:flex items-center gap-1">
        <a
          v-for="item in navItems"
          :key="item.href"
          class="px-4 py-2 text-sm font-body text-white/80 hover:text-white rounded-full
                 hover:bg-white/5 transition-all duration-300 cursor-pointer select-none"
          @click.prevent="navigate(item.href)"
        >
          {{ item.label }}
        </a>
      </div>

      <!-- 右侧操作区 -->
      <div class="flex items-center gap-3">
        <!-- 登录按钮 / 用户信息 -->
        <button
          v-if="!userStore.isLoggedIn"
          class="hidden md:flex items-center gap-2 px-4 py-2 text-sm font-body font-medium
                 text-white liquid-glass rounded-full cursor-pointer hover:bg-white/10 transition-all duration-300"
          @click="navigate('/login')"
        >
          <LogIn :size="16" />
          登录
        </button>
        <button
          v-else
          class="hidden md:flex items-center gap-2 px-4 py-2 text-sm font-body font-medium
                 text-white liquid-glass rounded-full cursor-pointer hover:bg-white/10 transition-all duration-300"
          @click="navigate('/profile')"
        >
          <User :size="16" />
          {{ userStore.nickname || '个人中心' }}
        </button>

        <!-- 移动端汉堡菜单 -->
        <button
          class="md:hidden text-white p-2 rounded-full hover:bg-white/10 transition-all cursor-pointer"
          @click="mobileMenuOpen = !mobileMenuOpen"
        >
          <Menu v-if="!mobileMenuOpen" :size="22" />
          <X v-else :size="22" />
        </button>
      </div>
    </div>

    <!-- 移动端下拉菜单 -->
    <Transition
      enter-active-class="transition-all duration-300 ease-out"
      enter-from-class="opacity-0 -translate-y-2"
      enter-to-class="opacity-100 translate-y-0"
      leave-active-class="transition-all duration-200 ease-in"
      leave-from-class="opacity-100 translate-y-0"
      leave-to-class="opacity-0 -translate-y-2"
    >
      <div
        v-if="mobileMenuOpen"
        class="md:hidden mt-2 mx-4 rounded-2xl liquid-glass-strong py-4 px-6"
      >
        <a
          v-for="item in navItems"
          :key="item.href"
          class="block py-3 text-sm font-body text-white/90 hover:text-white
                 border-b border-white/5 last:border-0 cursor-pointer"
          @click.prevent="navigate(item.href)"
        >
          {{ item.label }}
        </a>
        <button
          v-if="!userStore.isLoggedIn"
          class="w-full mt-3 py-2.5 text-sm font-body font-medium text-white
                 liquid-glass rounded-full cursor-pointer hover:bg-white/10 transition-all"
          @click="navigate('/login')"
        >
          登录 / 注册
        </button>
      </div>
    </Transition>
  </nav>
</template>
