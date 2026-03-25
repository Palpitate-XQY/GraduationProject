<script setup lang="ts">
/**
 * LoginView - 登录页
 */
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Eye, EyeOff, LogIn } from 'lucide-vue-next'
import { login } from '@/api/auth'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

const form = reactive({
  username: '',
  password: '',
})

const showPassword = ref(false)
const loading = ref(false)

async function handleLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    const res = await login({ username: form.username, password: form.password })
    userStore.setToken(res.data.accessToken)
    await userStore.fetchUserInfo(true)

    const redirect = (route.query.redirect as string) || ''
    if (redirect) {
      router.push(redirect)
      return
    }

    if (userStore.hasPermission('dashboard:view')) {
      await appStore.fetchDashboardOverview(true)
      router.push(appStore.defaultHomeRoute)
    } else {
      router.push('/')
    }

    ElMessage.success('登录成功')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center relative overflow-hidden">
    <video
      class="absolute inset-0 w-full h-full object-cover z-0"
      autoplay
      loop
      muted
      playsinline
      preload="auto"
      poster="/images/smart_community_bg.jpeg"
    >
      <source src="https://cdn.pixabay.com/video/2020/05/24/40090-424838562_large.mp4" type="video/mp4" />
    </video>
    <div class="absolute inset-0 bg-black/50 z-0" />

    <div class="relative z-10 w-full max-w-md mx-4">
      <div class="liquid-glass-strong rounded-3xl p-8 md:p-10">
        <div class="text-center mb-8">
          <div class="inline-flex items-center justify-center w-14 h-14 rounded-full bg-white/10 mb-4">
            <span class="text-white font-heading text-2xl italic font-semibold">S</span>
          </div>
          <h1 class="text-2xl font-heading italic text-white tracking-tight">SmartCommunity</h1>
          <p class="text-white/60 text-sm font-body mt-1">智慧社区服务平台</p>
        </div>

        <form class="space-y-5" @submit.prevent="handleLogin">
          <div>
            <label class="block text-white/70 text-xs font-body mb-2 uppercase tracking-wider">用户名</label>
            <input
              v-model="form.username"
              type="text"
              placeholder="请输入用户名"
              class="w-full px-4 py-3 rounded-xl bg-white/5 border border-white/10 text-white text-sm font-body placeholder-white/30 focus:outline-none focus:border-white/30 focus:bg-white/10 transition-all"
            />
          </div>

          <div>
            <label class="block text-white/70 text-xs font-body mb-2 uppercase tracking-wider">密码</label>
            <div class="relative">
              <input
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="请输入密码"
                class="w-full px-4 py-3 pr-11 rounded-xl bg-white/5 border border-white/10 text-white text-sm font-body placeholder-white/30 focus:outline-none focus:border-white/30 focus:bg-white/10 transition-all"
              />
              <button
                type="button"
                class="absolute right-3 top-1/2 -translate-y-1/2 text-white/45 hover:text-white/75 transition-colors"
                @click="showPassword = !showPassword"
              >
                <Eye v-if="!showPassword" :size="18" />
                <EyeOff v-else :size="18" />
              </button>
            </div>
          </div>

          <button
            type="submit"
            :disabled="loading"
            class="w-full py-3 rounded-full liquid-glass-strong text-white text-sm font-body font-medium flex items-center justify-center gap-2 hover:scale-[1.02] transition-all cursor-pointer disabled:opacity-50"
          >
            <LogIn :size="18" />
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </form>

        <div class="mt-6 text-center">
          <router-link to="/" class="text-white/50 text-xs font-body hover:text-white/80 transition-colors">
            返回门户首页
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>
