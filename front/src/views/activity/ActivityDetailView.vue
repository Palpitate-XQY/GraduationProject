<script setup lang="ts">
/**
 * ActivityDetailView — 活动详情页
 * 渲染内容 + 报名/取消按钮
 */
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Calendar, MapPin, Users, Clock, Paperclip } from 'lucide-vue-next'
import { residentActivityDetail, signupActivity, cancelSignup } from '@/api/activity'
import type { ActivityVO } from '@/types/activity'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const activity = ref<ActivityVO | null>(null)
const actionLoading = ref(false)

function fmt(d: string | null) {
  if (!d) return '—'
  return new Date(d).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

async function loadData() {
  const id = Number(route.params.id)
  try {
    const res = await residentActivityDetail(id)
    activity.value = res.data
  } catch { /* handled */ } finally {
    loading.value = false
  }
}

async function handleSignup() {
  if (!activity.value) return
  actionLoading.value = true
  try {
    await signupActivity(activity.value.id)
    ElMessage.success('报名成功')
    await loadData()
  } catch { /* handled */ } finally {
    actionLoading.value = false
  }
}

async function handleCancelSignup() {
  if (!activity.value) return
  actionLoading.value = true
  try {
    await cancelSignup(activity.value.id)
    ElMessage.success('已取消报名')
    await loadData()
  } catch { /* handled */ } finally {
    actionLoading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="min-h-screen pt-24 pb-12 px-4 md:px-8 lg:px-16">
    <div class="max-w-3xl mx-auto">
      <button
        class="flex items-center gap-2 text-white/50 text-sm font-body mb-6
               hover:text-white transition-colors cursor-pointer"
        @click="router.push('/activities')"
      >
        <ArrowLeft :size="16" /> 返回活动列表
      </button>

      <div v-if="loading" class="flex justify-center py-20">
        <div class="w-8 h-8 border-2 border-white/20 border-t-white/70 rounded-full animate-spin" />
      </div>

      <div v-else-if="!activity" class="text-center py-20">
        <p class="text-white/40 text-sm font-body">活动不存在</p>
      </div>

      <div v-else class="liquid-glass rounded-3xl p-6 md:p-10">
        <h1 class="text-2xl md:text-3xl font-heading italic text-white tracking-tight mb-4">
          {{ activity.title }}
        </h1>

        <!-- 信息区 -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-3 mb-8">
          <div class="flex items-center gap-2 text-white/50 text-sm font-body">
            <Calendar :size="16" class="text-white/40" />
            <span>活动时间：{{ fmt(activity.activityStartTime) }} — {{ fmt(activity.activityEndTime) }}</span>
          </div>
          <div v-if="activity.location" class="flex items-center gap-2 text-white/50 text-sm font-body">
            <MapPin :size="16" class="text-white/40" />
            <span>{{ activity.location }}</span>
          </div>
          <div class="flex items-center gap-2 text-white/50 text-sm font-body">
            <Clock :size="16" class="text-white/40" />
            <span>报名时间：{{ fmt(activity.signupStartTime) }} — {{ fmt(activity.signupEndTime) }}</span>
          </div>
          <div v-if="activity.maxParticipants" class="flex items-center gap-2 text-white/50 text-sm font-body">
            <Users :size="16" class="text-white/40" />
            <span>限 {{ activity.maxParticipants }} 人</span>
          </div>
        </div>

        <!-- 操作 -->
        <div class="flex items-center gap-3 mb-8">
          <button
            :disabled="actionLoading"
            class="px-6 py-2.5 rounded-full liquid-glass-strong text-white text-sm
                   font-body font-medium hover:scale-105 transition-all cursor-pointer
                   disabled:opacity-50"
            @click="handleSignup"
          >
            {{ actionLoading ? '处理中...' : '立即报名' }}
          </button>
          <button
            :disabled="actionLoading"
            class="px-6 py-2.5 rounded-full liquid-glass text-white/70 text-sm font-body
                   hover:text-white hover:bg-white/10 transition-all cursor-pointer
                   disabled:opacity-50"
            @click="handleCancelSignup"
          >
            取消报名
          </button>
        </div>

        <!-- 内容 -->
        <div
          class="prose prose-invert prose-sm max-w-none
                 [&_p]:text-white/80 [&_a]:text-sky-400 [&_li]:text-white/70
                 font-body leading-relaxed"
          v-html="activity.contentHtml || activity.content"
        />

        <!-- 附件 -->
        <div v-if="activity.attachments?.length" class="mt-8 pt-6 border-t border-white/10">
          <h4 class="text-white/60 text-xs font-body uppercase tracking-wider mb-3">附件</h4>
          <div class="space-y-2">
            <a v-for="att in activity.attachments" :key="att.fileId" :href="att.accessUrl" target="_blank"
               class="flex items-center gap-2 text-white/70 text-sm font-body hover:text-white transition-colors">
              <Paperclip :size="14" /> {{ att.fileName }}
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
