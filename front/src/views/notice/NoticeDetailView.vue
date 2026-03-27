<script setup lang="ts">
/**
 * 公告详情页。
 */
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Calendar } from 'lucide-vue-next'
import AttachmentList from '@/components/common/AttachmentList.vue'
import { residentNoticeDetail } from '@/api/notice'
import type { NoticeVO } from '@/types/notice'
import { buildFilePreviewUrl, withFileAccessToken } from '@/utils/file-url'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const notice = ref<NoticeVO | null>(null)

const coverUrl = computed(() => {
  if (!notice.value) return ''
  if (notice.value.coverFileId) {
    return buildFilePreviewUrl(notice.value.coverFileId)
  }
  const coverAttachment = notice.value.attachments?.find((item) => item.contentType?.startsWith('image/'))
  return withFileAccessToken(coverAttachment?.accessUrl)
})

function formatDate(value: string | null) {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

onMounted(async () => {
  const id = Number(route.params.id)
  try {
    const res = await residentNoticeDetail(id)
    notice.value = res.data
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="min-h-screen px-4 pb-12 pt-24 md:px-8 lg:px-16">
    <div class="mx-auto max-w-3xl">
      <button
        class="mb-6 flex cursor-pointer items-center gap-2 text-sm font-body text-white/50 transition-colors hover:text-white"
        @click="router.push('/notices')"
      >
        <ArrowLeft :size="16" />
        返回公告列表
      </button>

      <div v-if="loading" class="flex justify-center py-20">
        <div class="h-8 w-8 animate-spin rounded-full border-2 border-white/20 border-t-white/70" />
      </div>

      <div v-else-if="!notice" class="py-20 text-center">
        <p class="text-sm font-body text-white/40">公告不存在或已被删除</p>
      </div>

      <div v-else class="liquid-glass rounded-3xl p-6 md:p-10">
        <h1 class="mb-4 text-2xl font-heading italic tracking-tight text-white md:text-3xl">
          {{ notice.title }}
        </h1>

        <div class="mb-8 flex items-center gap-4 text-xs font-body text-white/40">
          <span class="flex items-center gap-1">
            <Calendar :size="14" />
            {{ formatDate(notice.publishTime || notice.createTime) }}
          </span>
        </div>

        <div v-if="coverUrl" class="mb-7 overflow-hidden rounded-2xl border border-white/15 bg-black/20">
          <img :src="coverUrl" :alt="notice.title" class="max-h-[360px] w-full object-cover" />
        </div>

        <div
          class="prose prose-invert prose-sm max-w-none font-body leading-relaxed
                 [&_a]:text-sky-400 [&_blockquote]:border-white/20 [&_code]:text-emerald-400
                 [&_h1]:text-white [&_h2]:text-white [&_h3]:text-white
                 [&_li]:text-white/70 [&_p]:text-white/80"
          v-html="notice.contentHtml || notice.content"
        />

        <div v-if="notice.attachments && notice.attachments.length" class="mt-8 border-t border-white/10 pt-6">
          <h4 class="mb-3 text-xs font-body uppercase tracking-wider text-white/60">附件</h4>
          <AttachmentList :attachments="notice.attachments" />
        </div>
      </div>
    </div>
  </div>
</template>
