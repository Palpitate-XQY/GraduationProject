<script setup lang="ts">
/**
 * ProfileView — 居民个人中心
 * 支持查看与修改本人居民档案（房号、紧急联系人、紧急电话）
 */
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { User, Shield, LogOut, Key, Building2, Home as HomeIcon, Phone } from 'lucide-vue-next'
import { useUserStore } from '@/stores/user'
import { updatePassword } from '@/api/auth'
import { getMyProfile, updateMyProfile } from '@/api/profile'
import type { ResidentProfileMyUpdateRequest, ResidentProfileVO } from '@/types/resident'

const router = useRouter()
const userStore = useUserStore()

const profile = ref<ResidentProfileVO | null>(null)
const profileLoading = ref(true)
const profileMissing = ref(false)
const profileError = ref('')

const canEditProfile = computed(() => userStore.hasPermission('resident:profile:my:update'))

const editVisible = ref(false)
const saving = ref(false)
const editFormRef = ref<FormInstance>()
const editForm = reactive<ResidentProfileMyUpdateRequest>({
  roomNo: '',
  emergencyContact: '',
  emergencyPhone: '',
})

const editRules: FormRules<ResidentProfileMyUpdateRequest> = {
  roomNo: [{ max: 64, message: '房号长度不能超过 64 个字符', trigger: 'blur' }],
  emergencyContact: [{ max: 64, message: '紧急联系人长度不能超过 64 个字符', trigger: 'blur' }],
  emergencyPhone: [
    {
      pattern: /^$|^[0-9+\-]{6,20}$/,
      message: '紧急联系电话格式不正确',
      trigger: 'blur',
    },
  ],
}

function normalizeOptionalText(value?: string) {
  const text = (value || '').trim()
  return text ? text : undefined
}

async function loadMyProfile() {
  profileLoading.value = true
  profileMissing.value = false
  profileError.value = ''
  try {
    const res = await getMyProfile()
    profile.value = res.data
  } catch (error) {
    profile.value = null
    const message = error instanceof Error ? error.message : ''
    if (message.includes('居民档案不存在')) {
      profileMissing.value = true
    } else {
      profileError.value = message || '居民档案加载失败'
    }
  } finally {
    profileLoading.value = false
  }
}

function openEditProfile() {
  editForm.roomNo = profile.value?.roomNo || ''
  editForm.emergencyContact = profile.value?.emergencyContact || ''
  editForm.emergencyPhone = profile.value?.emergencyPhone || ''
  editVisible.value = true
  nextTick(() => editFormRef.value?.clearValidate())
}

async function submitProfile() {
  if (!canEditProfile.value) return
  const valid = await editFormRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    await updateMyProfile({
      roomNo: normalizeOptionalText(editForm.roomNo),
      emergencyContact: normalizeOptionalText(editForm.emergencyContact),
      emergencyPhone: normalizeOptionalText(editForm.emergencyPhone),
    })
    ElMessage.success(profile.value ? '居民档案已更新' : '居民档案已完善')
    editVisible.value = false
    await loadMyProfile()
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  if (!userStore.userInfo) {
    await userStore.fetchUserInfo()
  }
  await loadMyProfile()
})

async function handleChangePassword() {
  try {
    const { value: oldPwd } = await ElMessageBox.prompt('请输入当前密码', '修改密码 - 第 1 步', {
      confirmButtonText: '下一步',
      cancelButtonText: '取消',
      inputType: 'password',
      inputPlaceholder: '当前密码',
    })
    if (!oldPwd) return

    const { value: newPwd } = await ElMessageBox.prompt('请输入新密码（至少 6 位）', '修改密码 - 第 2 步', {
      confirmButtonText: '确认修改',
      cancelButtonText: '取消',
      inputType: 'password',
      inputPlaceholder: '新密码',
      inputValidator: (v: string) => (v && v.length >= 6 ? true : '密码至少 6 位'),
    })
    if (!newPwd) return

    await updatePassword({ oldPassword: oldPwd, newPassword: newPwd })
    ElMessage.success('密码修改成功，请重新登录')
    await userStore.doLogout()
    router.push('/login')
  } catch {
    // 用户取消或接口层已处理
  }
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '退出登录', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await userStore.doLogout()
    router.push('/')
    ElMessage.success('已退出登录')
  } catch {
    // 用户取消
  }
}
</script>

<template>
  <div class="min-h-screen pb-12 pt-24 px-4 md:px-8 lg:px-16">
    <div class="mx-auto max-w-2xl space-y-4">
      <div class="liquid-glass rounded-3xl p-6 md:p-10">
        <div class="mb-8 flex items-center gap-5">
          <div class="h-16 w-16 shrink-0 rounded-full bg-white/10 flex items-center justify-center">
            <User :size="28" class="text-white/60" />
          </div>
          <div>
            <h1 class="text-2xl font-heading italic tracking-tight text-white">
              {{ userStore.nickname || userStore.userInfo?.username || '-' }}
            </h1>
            <p class="mt-1 text-sm font-body text-white/40">
              @{{ userStore.userInfo?.username || '-' }}
            </p>
          </div>
        </div>

        <div class="space-y-4">
          <div class="flex items-center gap-3 text-sm font-body text-white/60">
            <Shield :size="16" class="shrink-0 text-white/30" />
            <span>角色：{{ userStore.roleCodes.join(', ') || '-' }}</span>
          </div>
        </div>
      </div>

      <div class="liquid-glass rounded-3xl p-6 md:p-8">
        <div class="mb-5 flex items-center justify-between gap-3">
          <h3 class="text-xs font-body uppercase tracking-wider text-white/70">居民档案</h3>
          <el-button
            v-if="canEditProfile && !profileLoading"
            size="small"
            type="primary"
            plain
            @click="openEditProfile"
          >
            {{ profile ? '编辑档案' : '完善档案' }}
          </el-button>
        </div>

        <div v-if="profileLoading" class="space-y-2">
          <div class="h-9 rounded-xl bg-white/10 animate-pulse" />
          <div class="h-9 rounded-xl bg-white/10 animate-pulse" />
          <div class="h-9 rounded-xl bg-white/10 animate-pulse" />
        </div>

        <div
          v-else-if="profileError"
          class="rounded-2xl border border-rose-300/30 bg-rose-500/10 p-4 text-sm font-body text-white"
        >
          {{ profileError }}
          <el-button class="ml-2" size="small" text @click="loadMyProfile">重试</el-button>
        </div>

        <div v-else-if="profile" class="space-y-3 text-sm font-body text-white/60">
          <div class="flex items-center gap-3">
            <Building2 :size="16" class="shrink-0 text-white/30" />
            <span>社区：{{ profile.communityOrgName || '-' }}</span>
          </div>
          <div class="flex items-center gap-3">
            <HomeIcon :size="16" class="shrink-0 text-white/30" />
            <span>小区：{{ profile.complexOrgName || '-' }}</span>
          </div>
          <div class="flex items-center gap-3">
            <HomeIcon :size="16" class="shrink-0 text-white/30" />
            <span>房号：{{ profile.roomNo || '-' }}</span>
          </div>
          <div class="flex items-center gap-3">
            <User :size="16" class="shrink-0 text-white/30" />
            <span>紧急联系人：{{ profile.emergencyContact || '-' }}</span>
          </div>
          <div class="flex items-center gap-3">
            <Phone :size="16" class="shrink-0 text-white/30" />
            <span>紧急电话：{{ profile.emergencyPhone || '-' }}</span>
          </div>
          <p class="pt-2 text-xs text-white/45">最近更新时间：{{ profile.updateTime }}</p>
        </div>

        <div v-else-if="profileMissing" class="rounded-2xl border border-white/20 bg-black/10 p-4 text-sm font-body text-white/70">
          暂无居民档案信息，请先完善档案。
        </div>
      </div>

      <div class="liquid-glass rounded-3xl p-6 space-y-3">
        <button
          class="w-full flex items-center gap-3 rounded-xl px-4 py-3 text-left text-sm font-body text-white/70 hover:bg-white/5 transition-all"
          @click="handleChangePassword"
        >
          <Key :size="18" class="text-white/40" />
          修改密码
        </button>
        <button
          class="w-full flex items-center gap-3 rounded-xl px-4 py-3 text-left text-sm font-body text-red-400/80 hover:bg-red-500/10 transition-all"
          @click="handleLogout"
        >
          <LogOut :size="18" />
          退出登录
        </button>
      </div>
    </div>

    <el-dialog v-model="editVisible" title="编辑居民档案" width="560px" destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="96px">
        <el-form-item label="房号" prop="roomNo">
          <el-input v-model="editForm.roomNo" maxlength="64" show-word-limit placeholder="例如 2-1203" />
        </el-form-item>
        <el-form-item label="紧急联系人" prop="emergencyContact">
          <el-input v-model="editForm.emergencyContact" maxlength="64" show-word-limit placeholder="请输入联系人姓名" />
        </el-form-item>
        <el-form-item label="紧急电话" prop="emergencyPhone">
          <el-input v-model="editForm.emergencyPhone" maxlength="20" placeholder="支持数字、+、-" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitProfile">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
