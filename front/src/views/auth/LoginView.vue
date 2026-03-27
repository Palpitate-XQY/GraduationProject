<script setup lang="ts">
/**
 * Login/Register view.
 */
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Eye, EyeOff, LogIn, UserPlus } from 'lucide-vue-next'
import { login, registerResident, residentRegisterOptions } from '@/api/auth'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import type { ResidentRegisterComplexOptionVO } from '@/types/auth'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

type AuthMode = 'login' | 'register'

const mode = ref<AuthMode>(route.query.mode === 'register' ? 'register' : 'login')

const loginForm = reactive({
  username: '',
  password: '',
})

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  phone: '',
  email: '',
  complexOrgId: undefined as number | undefined,
  roomNo: '',
  emergencyContact: '',
  emergencyPhone: '',
})

const showLoginPassword = ref(false)
const showRegisterPassword = ref(false)
const loginLoading = ref(false)
const registerLoading = ref(false)

const complexOptions = ref<ResidentRegisterComplexOptionVO[]>([])
const optionsLoading = ref(false)
const optionsLoaded = ref(false)

const phoneRegex = /^1\d{10}$/
const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

const shouldManualComplexInput = computed(() => complexOptions.value.length === 0)

function switchMode(next: AuthMode) {
  mode.value = next
  const nextQuery = { ...route.query }
  if (next === 'register') {
    nextQuery.mode = 'register'
  } else {
    delete nextQuery.mode
  }
  router.replace({ path: '/login', query: nextQuery })
}

function buildComplexLabel(item: ResidentRegisterComplexOptionVO) {
  if (item.communityOrgName) {
    return `${item.communityOrgName} / ${item.complexOrgName}`
  }
  return item.complexOrgName
}

async function loadRegisterOptions(force = false) {
  if (optionsLoading.value) return
  if (optionsLoaded.value && !force) return
  optionsLoading.value = true
  try {
    const res = await residentRegisterOptions()
    complexOptions.value = res.data || []
    optionsLoaded.value = true
  } catch {
    complexOptions.value = []
    optionsLoaded.value = true
    ElMessage.warning('小区选项加载失败，可手动输入小区ID完成注册')
  } finally {
    optionsLoading.value = false
  }
}

function validateRegisterForm(): string | null {
  const username = registerForm.username.trim()
  const password = registerForm.password
  const nickname = registerForm.nickname.trim()
  const phone = registerForm.phone.trim()
  const email = registerForm.email.trim()
  const roomNo = registerForm.roomNo.trim()
  const emergencyPhone = registerForm.emergencyPhone.trim()

  if (username.length < 4 || username.length > 32) {
    return '用户名长度需在 4-32 位'
  }
  if (password.length < 8 || password.length > 64) {
    return '密码长度需在 8-64 位'
  }
  if (registerForm.confirmPassword !== password) {
    return '两次输入的密码不一致'
  }
  if (!nickname) {
    return '请输入昵称'
  }
  if (!phoneRegex.test(phone)) {
    return '请输入正确的手机号'
  }
  if (email && !emailRegex.test(email)) {
    return '请输入正确的邮箱格式'
  }
  if (!registerForm.complexOrgId || registerForm.complexOrgId <= 0) {
    return '请选择或输入正确的小区ID'
  }
  if (!roomNo) {
    return '请输入房号'
  }
  if (emergencyPhone && !phoneRegex.test(emergencyPhone)) {
    return '紧急联系电话格式不正确'
  }
  return null
}

async function handleLogin() {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  loginLoading.value = true
  try {
    const res = await login({ username: loginForm.username, password: loginForm.password })
    userStore.setToken(res.data.accessToken)
    await userStore.fetchUserInfo(true)

    const redirect = (route.query.redirect as string) || ''
    if (redirect) {
      let redirectPath = redirect
      try {
        redirectPath = decodeURIComponent(redirect)
      } catch {
        redirectPath = redirect
      }
      if (redirectPath.startsWith('/')) {
        await router.push(redirectPath)
      } else {
        await router.push('/')
      }
      ElMessage.success('登录成功')
      return
    }

    if (userStore.canAccessDashboard) {
      await appStore.fetchDashboardOverview(true)
      await router.push(appStore.defaultHomeRoute)
    } else {
      await router.push('/')
    }

    ElMessage.success('登录成功')
  } finally {
    loginLoading.value = false
  }
}

async function handleRegister() {
  const validationMessage = validateRegisterForm()
  if (validationMessage) {
    ElMessage.warning(validationMessage)
    return
  }

  registerLoading.value = true
  try {
    await registerResident({
      username: registerForm.username.trim(),
      password: registerForm.password,
      nickname: registerForm.nickname.trim(),
      phone: registerForm.phone.trim(),
      email: registerForm.email.trim() || undefined,
      complexOrgId: registerForm.complexOrgId as number,
      roomNo: registerForm.roomNo.trim(),
      emergencyContact: registerForm.emergencyContact.trim() || undefined,
      emergencyPhone: registerForm.emergencyPhone.trim() || undefined,
    })

    ElMessage.success('注册成功，请使用新账号登录')

    loginForm.username = registerForm.username.trim()
    loginForm.password = ''

    registerForm.password = ''
    registerForm.confirmPassword = ''

    switchMode('login')
  } finally {
    registerLoading.value = false
  }
}

watch(
  () => route.query.mode,
  (value) => {
    mode.value = value === 'register' ? 'register' : 'login'
  },
)

watch(mode, (value) => {
  if (value === 'register') {
    loadRegisterOptions()
  }
})

onMounted(() => {
  if (mode.value === 'register') {
    loadRegisterOptions()
  }
})
</script>

<template>
  <div class="min-h-screen flex items-center justify-center relative overflow-hidden px-4 py-10">
    <div class="absolute inset-0 bg-black/55 z-0" />

    <div class="relative z-10 w-full max-w-xl">
      <div class="liquid-glass-strong rounded-3xl p-7 md:p-9">
        <div class="text-center mb-6">
          <div class="inline-flex items-center justify-center w-14 h-14 rounded-full bg-white/10 mb-4">
            <span class="text-white font-heading text-2xl italic font-semibold">S</span>
          </div>
          <h1 class="text-2xl font-heading italic text-white tracking-tight">SmartCommunity</h1>
          <p class="text-white/60 text-sm font-body mt-1">智慧社区服务平台</p>
        </div>

        <div class="mb-6 p-1 rounded-full bg-white/5 border border-white/10 grid grid-cols-2 gap-1">
          <button
            type="button"
            class="rounded-full py-2 text-sm font-body transition-all"
            :class="mode === 'login' ? 'liquid-glass text-white' : 'text-white/70 hover:text-white'"
            @click="switchMode('login')"
          >
            登录
          </button>
          <button
            type="button"
            class="rounded-full py-2 text-sm font-body transition-all"
            :class="mode === 'register' ? 'liquid-glass text-white' : 'text-white/70 hover:text-white'"
            @click="switchMode('register')"
          >
            注册
          </button>
        </div>

        <form v-if="mode === 'login'" class="space-y-5" @submit.prevent="handleLogin">
          <div>
            <label class="block text-white/70 text-xs font-body mb-2 uppercase tracking-wider">用户名</label>
            <input
              v-model="loginForm.username"
              type="text"
              placeholder="请输入用户名"
              class="w-full px-4 py-3 rounded-xl bg-white/5 border border-white/10 text-white text-sm font-body placeholder-white/30 focus:outline-none focus:border-white/30 focus:bg-white/10 transition-all"
            />
          </div>

          <div>
            <label class="block text-white/70 text-xs font-body mb-2 uppercase tracking-wider">密码</label>
            <div class="relative">
              <input
                v-model="loginForm.password"
                :type="showLoginPassword ? 'text' : 'password'"
                placeholder="请输入密码"
                class="w-full px-4 py-3 pr-11 rounded-xl bg-white/5 border border-white/10 text-white text-sm font-body placeholder-white/30 focus:outline-none focus:border-white/30 focus:bg-white/10 transition-all"
              />
              <button
                type="button"
                class="absolute right-3 top-1/2 -translate-y-1/2 text-white/45 hover:text-white/75 transition-colors"
                @click="showLoginPassword = !showLoginPassword"
              >
                <Eye v-if="!showLoginPassword" :size="18" />
                <EyeOff v-else :size="18" />
              </button>
            </div>
          </div>

          <button
            type="submit"
            :disabled="loginLoading"
            class="w-full py-3 rounded-full liquid-glass-strong text-white text-sm font-body font-medium flex items-center justify-center gap-2 hover:scale-[1.01] transition-all cursor-pointer disabled:opacity-50"
          >
            <LogIn :size="18" />
            {{ loginLoading ? '登录中...' : '登录' }}
          </button>
        </form>

        <form v-else class="space-y-4" @submit.prevent="handleRegister">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="block text-white/70 text-xs font-body mb-2 uppercase tracking-wider">用户名</label>
              <input
                v-model="registerForm.username"
                type="text"
                placeholder="4-32 位"
                class="w-full px-4 py-2.5 rounded-xl bg-white/5 border border-white/10 text-white text-sm font-body placeholder-white/30 focus:outline-none focus:border-white/30 focus:bg-white/10 transition-all"
              />
            </div>

            <div>
              <label class="block text-white/70 text-xs font-body mb-2 uppercase tracking-wider">昵称</label>
              <input
                v-model="registerForm.nickname"
                type="text"
                placeholder="请输入昵称"
                class="w-full px-4 py-2.5 rounded-xl bg-white/5 border border-white/10 text-white text-sm font-body placeholder-white/30 focus:outline-none focus:border-white/30 focus:bg-white/10 transition-all"
              />
            </div>

            <div>
              <label class="block text-white/70 text-xs font-body mb-2 uppercase tracking-wider">密码</label>
              <div class="relative">
                <input
                  v-model="registerForm.password"
                  :type="showRegisterPassword ? 'text' : 'password'"
                  placeholder="8-64 位"
                  class="w-full px-4 py-2.5 pr-11 rounded-xl bg-white/5 border border-white/10 text-white text-sm font-body placeholder-white/30 focus:outline-none focus:border-white/30 focus:bg-white/10 transition-all"
                />
                <button
                  type="button"
                  class="absolute right-3 top-1/2 -translate-y-1/2 text-white/45 hover:text-white/75 transition-colors"
                  @click="showRegisterPassword = !showRegisterPassword"
                >
                  <Eye v-if="!showRegisterPassword" :size="18" />
                  <EyeOff v-else :size="18" />
                </button>
              </div>
            </div>

            <div>
              <label class="block text-white/70 text-xs font-body mb-2 uppercase tracking-wider">确认密码</label>
              <input
                v-model="registerForm.confirmPassword"
                :type="showRegisterPassword ? 'text' : 'password'"
                placeholder="再次输入密码"
                class="w-full px-4 py-2.5 rounded-xl bg-white/5 border border-white/10 text-white text-sm font-body placeholder-white/30 focus:outline-none focus:border-white/30 focus:bg-white/10 transition-all"
              />
            </div>

            <div>
              <label class="block text-white/70 text-xs font-body mb-2 uppercase tracking-wider">手机号</label>
              <input
                v-model="registerForm.phone"
                type="text"
                placeholder="11 位手机号"
                class="w-full px-4 py-2.5 rounded-xl bg-white/5 border border-white/10 text-white text-sm font-body placeholder-white/30 focus:outline-none focus:border-white/30 focus:bg-white/10 transition-all"
              />
            </div>

            <div>
              <label class="block text-white/70 text-xs font-body mb-2 uppercase tracking-wider">邮箱（可选）</label>
              <input
                v-model="registerForm.email"
                type="email"
                placeholder="name@example.com"
                class="w-full px-4 py-2.5 rounded-xl bg-white/5 border border-white/10 text-white text-sm font-body placeholder-white/30 focus:outline-none focus:border-white/30 focus:bg-white/10 transition-all"
              />
            </div>

            <div>
              <label class="block text-white/70 text-xs font-body mb-2 uppercase tracking-wider">所属小区</label>
              <el-select
                v-if="!shouldManualComplexInput"
                v-model="registerForm.complexOrgId"
                :loading="optionsLoading"
                filterable
                placeholder="请选择小区"
                class="w-full"
              >
                <el-option
                  v-for="item in complexOptions"
                  :key="item.complexOrgId"
                  :label="buildComplexLabel(item)"
                  :value="item.complexOrgId"
                />
              </el-select>
              <input
                v-else
                v-model.number="registerForm.complexOrgId"
                type="number"
                placeholder="请输入小区ID"
                class="w-full px-4 py-2.5 rounded-xl bg-white/5 border border-white/10 text-white text-sm font-body placeholder-white/30 focus:outline-none focus:border-white/30 focus:bg-white/10 transition-all"
              />
            </div>

            <div>
              <label class="block text-white/70 text-xs font-body mb-2 uppercase tracking-wider">房号</label>
              <input
                v-model="registerForm.roomNo"
                type="text"
                placeholder="如 3栋-1单元-1201"
                class="w-full px-4 py-2.5 rounded-xl bg-white/5 border border-white/10 text-white text-sm font-body placeholder-white/30 focus:outline-none focus:border-white/30 focus:bg-white/10 transition-all"
              />
            </div>

            <div>
              <label class="block text-white/70 text-xs font-body mb-2 uppercase tracking-wider">紧急联系人（可选）</label>
              <input
                v-model="registerForm.emergencyContact"
                type="text"
                placeholder="请输入联系人"
                class="w-full px-4 py-2.5 rounded-xl bg-white/5 border border-white/10 text-white text-sm font-body placeholder-white/30 focus:outline-none focus:border-white/30 focus:bg-white/10 transition-all"
              />
            </div>

            <div>
              <label class="block text-white/70 text-xs font-body mb-2 uppercase tracking-wider">紧急联系电话（可选）</label>
              <input
                v-model="registerForm.emergencyPhone"
                type="text"
                placeholder="11 位手机号"
                class="w-full px-4 py-2.5 rounded-xl bg-white/5 border border-white/10 text-white text-sm font-body placeholder-white/30 focus:outline-none focus:border-white/30 focus:bg-white/10 transition-all"
              />
            </div>
          </div>

          <button
            type="submit"
            :disabled="registerLoading"
            class="w-full py-3 rounded-full liquid-glass-strong text-white text-sm font-body font-medium flex items-center justify-center gap-2 hover:scale-[1.01] transition-all cursor-pointer disabled:opacity-50"
          >
            <UserPlus :size="18" />
            {{ registerLoading ? '注册中...' : '立即注册' }}
          </button>
        </form>

        <div class="mt-6 text-center flex items-center justify-center gap-4 text-xs font-body">
          <router-link to="/" class="text-white/55 hover:text-white/80 transition-colors">返回门户首页</router-link>
          <button
            type="button"
            class="text-white/55 hover:text-white/80 transition-colors"
            @click="switchMode(mode === 'login' ? 'register' : 'login')"
          >
            {{ mode === 'login' ? '没有账号？去注册' : '已有账号？去登录' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
