<script setup lang="ts">
/**
 * NoticeDetailView — 公告详情页
 * 渲染后端 Markdown→HTML 的 contentHtml
 */
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Paperclip, Calendar } from 'lucide-vue-next'
import { residentNoticeDetail } from '@/api/notice'
import type { NoticeVO } from '@/types/notice'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const notice = ref<NoticeVO | null>(null)

function formatDate(d: string | null) {
  if (!d) return '—'
  return new Date(d).toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit',
  })
}

onMounted(async () => {
  const id = Number(route.params.id)
  try {
    const res = await residentNoticeDetail(id)
    notice.value = res.data
  } catch {
    // handled
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="min-h-screen pt-24 pb-12 px-4 md:px-8 lg:px-16">
    <div class="max-w-3xl mx-auto">
      <!-- 返回 -->
      <button
        class="flex items-center gap-2 text-white/50 text-sm font-body mb-6
               hover:text-white transition-colors cursor-pointer"
        @click="router.push('/notices')"
      >
        <ArrowLeft :size="16" />
        返回公告列表
      </button>

      <!-- 加载 -->
      <div v-if="loading" class="flex justify-center py-20">
        <div class="w-8 h-8 border-2 border-white/20 border-t-white/70 rounded-full animate-spin" />
      </div>

      <!-- 空 -->
      <div v-else-if="!notice" class="text-center py-20">
        <p class="text-white/40 text-sm font-body">公告不存在或已被删除</p>
      </div>

      <!-- 内容 -->
      <div v-else class="liquid-glass rounded-3xl p-6 md:p-10">
        <h1 class="text-2xl md:text-3xl font-heading italic text-white tracking-tight mb-4">
          {{ notice.title }}
        </h1>

        <div class="flex items-center gap-4 text-white/40 text-xs font-body mb-8">
          <span class="flex items-center gap-1">
            <Calendar :size="14" />
            {{ formatDate(notice.publishTime || notice.createTime) }}
          </span>
        </div>

        <!-- Markdown HTML 渲染 -->
        <div
          class="prose prose-invert prose-sm max-w-none
                 [&_h1]:text-white [&_h2]:text-white [&_h3]:text-white
                 [&_p]:text-white/80 [&_a]:text-sky-400 [&_li]:text-white/70
                 [&_code]:text-emerald-400 [&_blockquote]:border-white/20
                 font-body leading-relaxed"
          v-html="notice.contentHtml || notice.content"
        />

        <!-- 附件 -->
        <div v-if="notice.attachments && notice.attachments.length" class="mt-8 pt-6 border-t border-white/10">
          <h4 class="text-white/60 text-xs font-body uppercase tracking-wider mb-3">附件</h4>
          <div class="space-y-2">
            <a
              v-for="att in notice.attachments"
              :key="att.fileId"
              :href="att.fileUrl"
              target="_blank"
              class="flex items-center gap-2 text-white/70 text-sm font-body
                     hover:text-white transition-colors"
            >
              <Paperclip :size="14" />
              {{ att.fileName }}
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
