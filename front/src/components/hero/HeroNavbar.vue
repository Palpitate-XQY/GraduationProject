<script setup lang="ts">
/**
 * HeroNavbar - 首屏悬浮导航
 * 内容按后端权限语义过滤，样式保持 Cinematic Hero 玻璃质感。
 */
import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { LogIn, Menu, User, X } from 'lucide-vue-next'
import { useHeroContent } from '@/composables/useHeroContent'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const { navItems } = useHeroContent()

const mobileMenuOpen = ref(false)
const scrolled = ref(false)

function handleScroll() {
  scrolled.value = window.scrollY > 24
}

function navigate(href: string) {
  mobileMenuOpen.value = false
  if (href.startsWith('#')) {
    document.querySelector(href)?.scrollIntoView({ behavior: 'smooth' })
    return
  }
  router.push(href)
}

onMounted(() => {
  handleScroll()
  window.addEventListener('scroll', handleScroll, { passive: true })
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<template>
  <nav class="fixed top-4 left-0 right-0 z-50 px-8 lg:px-16">
    <div
      :class="[
        'mx-auto max-w-7xl rounded-full px-4 md:px-6 py-3 flex items-center justify-between transition-all duration-300',
        scrolled ? 'liquid-glass bg-black/25 shadow-lg shadow-black/20' : 'bg-transparent',
      ]"
    >
      <button
        class="flex items-center gap-2 cursor-pointer select-none"
        type="button"
        @click="navigate('/')"
      >
        <div class="w-8 h-8 rounded-full bg-white/10 flex items-center justify-center">
          <span class="text-white font-heading text-lg italic font-semibold">S</span>
        </div>
        <span class="hidden sm:inline text-white font-heading italic text-xl tracking-tight">SmartCommunity</span>
      </button>

      <div class="hidden md:flex items-center gap-1">
        <button
          v-for="item in navItems"
          :key="item.href"
          type="button"
          class="px-4 py-2 text-sm font-body text-white/80 hover:text-white hover:bg-white/8 rounded-full transition-all duration-300 cursor-pointer"
          @click="navigate(item.href)"
        >
          {{ item.label }}
        </button>
      </div>

      <div class="flex items-center gap-2">
        <button
          v-if="!userStore.isLoggedIn"
          type="button"
          class="hidden md:flex items-center gap-2 px-4 py-2 rounded-full liquid-glass text-white text-sm font-body font-medium hover:bg-white/10 transition-all"
          @click="navigate('/login')"
        >
          <LogIn :size="15" />
          登录
        </button>
        <button
          v-else
          type="button"
          class="hidden md:flex items-center gap-2 px-4 py-2 rounded-full liquid-glass text-white text-sm font-body font-medium hover:bg-white/10 transition-all"
          @click="navigate('/profile')"
        >
          <User :size="15" />
          {{ userStore.nickname || '居民服务' }}
        </button>

        <button
          type="button"
          class="md:hidden p-2 rounded-full text-white hover:bg-white/10 transition-all"
          @click="mobileMenuOpen = !mobileMenuOpen"
        >
          <Menu v-if="!mobileMenuOpen" :size="22" />
          <X v-else :size="22" />
        </button>
      </div>
    </div>

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
        class="md:hidden mt-2 liquid-glass-strong rounded-2xl px-5 py-4"
      >
        <button
          v-for="item in navItems"
          :key="item.href"
          type="button"
          class="w-full text-left py-2.5 text-sm font-body text-white/90 hover:text-white border-b border-white/8 last:border-0"
          @click="navigate(item.href)"
        >
          {{ item.label }}
        </button>

        <button
          v-if="!userStore.isLoggedIn"
          type="button"
          class="mt-3 w-full px-4 py-2.5 rounded-full liquid-glass text-white text-sm font-body"
          @click="navigate('/login')"
        >
          登录 / 注册
        </button>
      </div>
    </Transition>
  </nav>
</template>
