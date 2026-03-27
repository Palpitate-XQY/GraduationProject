<script setup lang="ts">
/**
 * 活动详情页。
 */
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Calendar, Clock, MapPin, Users } from 'lucide-vue-next'
import AttachmentList from '@/components/common/AttachmentList.vue'
import { cancelSignup, residentActivityDetail, signupActivity } from '@/api/activity'
import type { ActivityVO } from '@/types/activity'
import { buildFilePreviewUrl, withFileAccessToken } from '@/utils/file-url'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const activity = ref<ActivityVO | null>(null)
const actionLoading = ref(false)

const isSigned = computed(() => Boolean(activity.value?.signedByMe || activity.value?.signupStatus === 'SIGNED'))
const coverUrl = computed(() => {
  if (!activity.value) return ''
  if (activity.value.coverFileId) {
    return buildFilePreviewUrl(activity.value.coverFileId)
  }
  const coverAttachment = activity.value.attachments?.find((item) => item.contentType?.startsWith('image/'))
  return withFileAccessToken(coverAttachment?.accessUrl)
})

function formatDateTime(value: string | null) {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

async function loadData() {
  const id = Number(route.params.id)
  try {
    const res = await residentActivityDetail(id)
    activity.value = res.data
  } finally {
    loading.value = false
  }
}

async function handleSignup() {
  if (!activity.value || isSigned.value) return
  actionLoading.value = true
  try {
    await signupActivity(activity.value.id)
    ElMessage.success('报名成功')
    await loadData()
  } finally {
    actionLoading.value = false
  }
}

async function handleCancelSignup() {
  if (!activity.value || !isSigned.value) return
  actionLoading.value = true
  try {
    await cancelSignup(activity.value.id)
    ElMessage.success('已取消报名')
    await loadData()
  } finally {
    actionLoading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="min-h-screen px-4 pb-12 pt-24 md:px-8 lg:px-16">
    <div class="mx-auto max-w-3xl">
      <button
        class="mb-6 flex cursor-pointer items-center gap-2 text-sm font-body text-white/50 transition-colors hover:text-white"
        @click="router.push('/activities')"
      >
        <ArrowLeft :size="16" /> 返回活动列表
      </button>

      <div v-if="loading" class="flex justify-center py-20">
        <div class="h-8 w-8 animate-spin rounded-full border-2 border-white/20 border-t-white/70" />
      </div>

      <div v-else-if="!activity" class="py-20 text-center">
        <p class="text-sm font-body text-white/40">活动不存在</p>
      </div>

      <div v-else class="liquid-glass rounded-3xl p-6 md:p-10">
        <h1 class="mb-4 text-2xl font-heading italic tracking-tight text-white md:text-3xl">
          {{ activity.title }}
        </h1>

        <div class="mb-6">
          <span
            v-if="isSigned"
            class="inline-flex rounded-full bg-sky-500/20 px-2.5 py-0.5 text-xs font-body text-sky-200"
          >
            已报名
          </span>
        </div>

        <div v-if="coverUrl" class="mb-7 overflow-hidden rounded-2xl border border-white/15 bg-black/20">
          <img :src="coverUrl" :alt="activity.title" class="max-h-[360px] w-full object-cover" />
        </div>

        <div class="mb-8 grid grid-cols-1 gap-3 md:grid-cols-2">
          <div class="flex items-center gap-2 text-sm font-body text-white/50">
            <Calendar :size="16" class="text-white/40" />
            <span>活动时间：{{ formatDateTime(activity.activityStartTime) }} - {{ formatDateTime(activity.activityEndTime) }}</span>
          </div>
          <div v-if="activity.location" class="flex items-center gap-2 text-sm font-body text-white/50">
            <MapPin :size="16" class="text-white/40" />
            <span>{{ activity.location }}</span>
          </div>
          <div class="flex items-center gap-2 text-sm font-body text-white/50">
            <Clock :size="16" class="text-white/40" />
            <span>报名时间：{{ formatDateTime(activity.signupStartTime) }} - {{ formatDateTime(activity.signupEndTime) }}</span>
          </div>
          <div v-if="activity.maxParticipants" class="flex items-center gap-2 text-sm font-body text-white/50">
            <Users :size="16" class="text-white/40" />
            <span>限 {{ activity.maxParticipants }} 人</span>
          </div>
        </div>

        <div class="mb-8 flex items-center gap-3">
          <button
            :disabled="actionLoading || isSigned"
            class="liquid-glass-strong cursor-pointer rounded-full px-6 py-2.5 text-sm font-medium text-white transition-all hover:scale-105 disabled:opacity-50"
            @click="handleSignup"
          >
            {{ actionLoading ? '处理中...' : isSigned ? '已报名' : '立即报名' }}
          </button>
          <button
            :disabled="actionLoading || !isSigned"
            class="liquid-glass cursor-pointer rounded-full px-6 py-2.5 text-sm font-body text-white/70 transition-all hover:bg-white/10 hover:text-white disabled:opacity-50"
            @click="handleCancelSignup"
          >
            取消报名
          </button>
        </div>

        <div
          class="prose prose-invert prose-sm max-w-none font-body leading-relaxed [&_a]:text-sky-400 [&_li]:text-white/70 [&_p]:text-white/80"
          v-html="activity.contentHtml || activity.content"
        />

        <div v-if="activity.attachments?.length" class="mt-8 border-t border-white/10 pt-6">
          <h4 class="mb-3 text-xs font-body uppercase tracking-wider text-white/60">附件</h4>
          <AttachmentList :attachments="activity.attachments" />
        </div>
      </div>
    </div>
  </div>
</template>
