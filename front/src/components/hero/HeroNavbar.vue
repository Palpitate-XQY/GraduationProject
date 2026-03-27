<script setup lang="ts">
/**
 * HeroNavbar - 门户悬浮导航（截图风格）
 */
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Globe, LogIn, Menu, UserRound, X } from 'lucide-vue-next'
import { useHeroContent } from '@/composables/useHeroContent'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const { navItems } = useHeroContent()

const mobileMenuOpen = ref(false)
const scrolled = ref(false)

const authText = computed(() => userStore.nickname || '个人中心')

function handleScroll() {
  scrolled.value = window.scrollY > 30
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
  <nav class="fixed left-0 right-0 top-4 z-50 px-4 md:px-8 lg:px-16">
    <div
      :class="[
        'mx-auto flex max-w-[1320px] items-center justify-between rounded-full border px-3 py-2.5 md:px-5 transition-all duration-300',
        scrolled
          ? 'border-white/30 bg-black/35 shadow-[0_16px_48px_rgba(0,0,0,0.35)] backdrop-blur-xl'
          : 'border-white/25 bg-black/25 backdrop-blur-lg',
      ]"
    >
      <button type="button" class="flex items-center gap-2 text-white" @click="navigate('/')">
        <span class="inline-flex h-6 w-6 items-center justify-center rounded-full bg-white/85" />
        <span class="font-heading text-3xl italic leading-none">SmartComm</span>
      </button>

      <div class="hidden items-center gap-1 lg:flex">
        <button
          v-for="item in navItems"
          :key="item.href"
          type="button"
          class="rounded-full px-4 py-2 text-sm font-body text-white/85 transition hover:bg-white/10 hover:text-white"
          @click="navigate(item.href)"
        >
          {{ item.label }}
        </button>
      </div>

      <div class="hidden items-center gap-2 lg:flex">
        <button
          type="button"
          class="inline-flex items-center gap-1 rounded-full px-3 py-2 text-sm font-body text-white/85 transition hover:bg-white/10 hover:text-white"
        >
          <Globe :size="14" />
          中文
        </button>

        <button
          v-if="!userStore.isLoggedIn"
          type="button"
          class="rounded-full border border-white/30 bg-white/10 px-5 py-2 text-sm font-body font-semibold text-white transition hover:bg-white/20"
          @click="navigate('/login')"
        >
          Sign In
        </button>
        <button
          v-else
          type="button"
          class="inline-flex items-center gap-2 rounded-full border border-white/30 bg-white/10 px-4 py-2 text-sm font-body text-white transition hover:bg-white/20"
          @click="navigate('/profile')"
        >
          <UserRound :size="14" />
          {{ authText }}
        </button>
      </div>

      <button
        type="button"
        class="inline-flex rounded-full p-2 text-white transition hover:bg-white/10 lg:hidden"
        @click="mobileMenuOpen = !mobileMenuOpen"
      >
        <Menu v-if="!mobileMenuOpen" :size="20" />
        <X v-else :size="20" />
      </button>
    </div>

    <Transition
      enter-active-class="transition-all duration-300 ease-out"
      enter-from-class="-translate-y-2 opacity-0"
      enter-to-class="translate-y-0 opacity-100"
      leave-active-class="transition-all duration-200 ease-in"
      leave-from-class="translate-y-0 opacity-100"
      leave-to-class="-translate-y-2 opacity-0"
    >
      <div
        v-if="mobileMenuOpen"
        class="mx-auto mt-2 max-w-[1320px] rounded-3xl border border-white/25 bg-black/40 p-4 backdrop-blur-2xl lg:hidden"
      >
        <div class="mb-3 flex items-center justify-between rounded-2xl border border-white/20 bg-white/5 px-3 py-2 text-white/85">
          <span class="inline-flex items-center gap-1 text-sm font-body">
            <Globe :size="14" />
            中文
          </span>
          <button
            type="button"
            class="inline-flex items-center gap-1 rounded-full bg-white/10 px-3 py-1.5 text-xs font-body"
            @click="navigate(userStore.isLoggedIn ? '/profile' : '/login')"
          >
            <LogIn v-if="!userStore.isLoggedIn" :size="12" />
            <UserRound v-else :size="12" />
            {{ userStore.isLoggedIn ? '个人中心' : 'Sign In' }}
          </button>
        </div>

        <button
          v-for="item in navItems"
          :key="item.href"
          type="button"
          class="block w-full rounded-2xl px-3 py-2.5 text-left text-sm font-body text-white/90 transition hover:bg-white/10"
          @click="navigate(item.href)"
        >
          {{ item.label }}
        </button>
      </div>
    </Transition>
  </nav>
</template>
