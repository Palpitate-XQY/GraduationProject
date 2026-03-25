<script setup lang="ts">
/**
 * ProfileView — 居民个人中心
 * 显示用户信息、居民档案，提供修改密码和退出
 */
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Mail, Shield, LogOut, Key, Building2, Home as HomeIcon } from 'lucide-vue-next'
import { useUserStore } from '@/stores/user'
import { updatePassword } from '@/api/auth'
import { getMyProfile } from '@/api/profile'
import type { ResidentProfileVO } from '@/api/profile'

const router = useRouter()
const userStore = useUserStore()
const profile = ref<ResidentProfileVO | null>(null)
const profileLoading = ref(true)

onMounted(async () => {
  // 确保有用户信息
  if (!userStore.userInfo) {
    await userStore.fetchUserInfo()
  }
  // 尝试获取居民档案
  try {
    const res = await getMyProfile()
    profile.value = res.data
  } catch { /* 可能不是居民角色 */ } finally {
    profileLoading.value = false
  }
})

async function handleChangePassword() {
  try {
    const { value: oldPwd } = await ElMessageBox.prompt('请输入当前密码', '修改密码 — 第1步', {
      confirmButtonText: '下一步', cancelButtonText: '取消',
      inputType: 'password', inputPlaceholder: '当前密码',
    })
    if (!oldPwd) return

    const { value: newPwd } = await ElMessageBox.prompt('请输入新密码（至少 6 位）', '修改密码 — 第2步', {
      confirmButtonText: '确认修改', cancelButtonText: '取消',
      inputType: 'password', inputPlaceholder: '新密码',
      inputValidator: (v: string) => (v && v.length >= 6 ? true : '密码至少 6 位'),
    })
    if (!newPwd) return

    await updatePassword({ oldPassword: oldPwd, newPassword: newPwd })
    ElMessage.success('密码修改成功，请重新登录')
    await userStore.doLogout()
    router.push('/login')
  } catch { /* cancelled or handled */ }
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '退出登录', {
      confirmButtonText: '退出', cancelButtonText: '取消', type: 'warning',
    })
    await userStore.doLogout()
    router.push('/')
    ElMessage.success('已退出登录')
  } catch { /* cancelled */ }
}
</script>

<template>
  <div class="min-h-screen pt-24 pb-12 px-4 md:px-8 lg:px-16">
    <div class="max-w-2xl mx-auto space-y-4">
      <!-- 用户信息卡片 -->
      <div class="liquid-glass rounded-3xl p-6 md:p-10">
        <!-- 头像区 -->
        <div class="flex items-center gap-5 mb-8">
          <div class="w-16 h-16 rounded-full bg-white/10 flex items-center justify-center shrink-0">
            <User :size="28" class="text-white/60" />
          </div>
          <div>
            <h1 class="text-2xl font-heading italic text-white tracking-tight">
              {{ userStore.nickname || userStore.userInfo?.username || '—' }}
            </h1>
            <p class="text-white/40 text-sm font-body mt-1">
              @{{ userStore.userInfo?.username || '—' }}
            </p>
          </div>
        </div>

        <!-- 信息列表 -->
        <div class="space-y-4">
          <div class="flex items-center gap-3 text-white/60 text-sm font-body">
            <Shield :size="16" class="text-white/30 shrink-0" />
            <span>角色：{{ userStore.roleCodes.join(', ') || '—' }}</span>
          </div>
        </div>
      </div>

      <!-- 居民档案（如果有） -->
      <div v-if="!profileLoading && profile" class="liquid-glass rounded-3xl p-6 md:p-8">
        <h3 class="text-white/70 text-xs font-body uppercase tracking-wider mb-5">居民档案</h3>
        <div class="space-y-3 text-white/60 text-sm font-body">
          <div class="flex items-center gap-3">
            <Building2 :size="16" class="text-white/30 shrink-0" />
            <span>社区：{{ profile.communityOrgName || '—' }}</span>
          </div>
          <div class="flex items-center gap-3">
            <HomeIcon :size="16" class="text-white/30 shrink-0" />
            <span>小区：{{ profile.complexOrgName || '—' }}</span>
          </div>
          <div v-if="profile.roomNo" class="flex items-center gap-3">
            <HomeIcon :size="16" class="text-white/30 shrink-0" />
            <span>房号：{{ profile.roomNo }}</span>
          </div>
          <div v-if="profile.emergencyContact" class="flex items-center gap-3">
            <User :size="16" class="text-white/30 shrink-0" />
            <span>紧急联系人：{{ profile.emergencyContact }} {{ profile.emergencyPhone }}</span>
          </div>
        </div>
      </div>

      <!-- 操作 -->
      <div class="liquid-glass rounded-3xl p-6 space-y-3">
        <button
          class="w-full flex items-center gap-3 px-4 py-3 rounded-xl text-white/70 text-sm font-body
                 hover:bg-white/5 transition-all cursor-pointer text-left"
          @click="handleChangePassword"
        >
          <Key :size="18" class="text-white/40" /> 修改密码
        </button>
        <button
          class="w-full flex items-center gap-3 px-4 py-3 rounded-xl text-red-400/80 text-sm font-body
                 hover:bg-red-500/10 transition-all cursor-pointer text-left"
          @click="handleLogout"
        >
          <LogOut :size="18" /> 退出登录
        </button>
      </div>
    </div>
  </div>
</template>
