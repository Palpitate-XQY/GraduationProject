<script setup lang="ts">
/**
 * NoticeListView — 社区公告列表页
 * 调用居民端公告分页接口，暗色主题 + liquid-glass 卡片
 */
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Pin, ChevronLeft, ChevronRight } from 'lucide-vue-next'
import { residentNoticePage } from '@/api/notice'
import type { NoticeVO } from '@/types/notice'

const router = useRouter()

const loading = ref(false)
const list = ref<NoticeVO[]>([])
const total = ref(0)
const query = reactive({ current: 1, size: 10, keyword: '' })

async function fetchData() {
  loading.value = true
  try {
    const res = await residentNoticePage(query)
    list.value = res.data.records
    total.value = res.data.total
  } catch {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.current = 1
  fetchData()
}

function goPage(page: number) {
  query.current = page
  fetchData()
}

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / query.size)))

function formatDate(d: string | null) {
  if (!d) return '—'
  return new Date(d).toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

function noticeTypeLabel(type: string) {
  const map: Record<string, string> = { NOTICE: '通知', ANNOUNCEMENT: '公告', NEWS: '新闻' }
  return map[type] || type
}

onMounted(fetchData)
</script>

<template>
  <div class="min-h-screen pt-24 pb-12 px-4 md:px-8 lg:px-16">
    <div class="max-w-5xl mx-auto">
      <!-- 页头 -->
      <div class="mb-8">
        <h1 class="text-3xl md:text-4xl font-heading italic text-white tracking-tight">社区公告</h1>
        <p class="text-white/50 text-sm font-body mt-2">查看来自社区、物业的最新通知与公告</p>
      </div>

      <!-- 搜索栏 -->
      <div class="flex items-center gap-3 mb-6">
        <div class="flex-1 relative">
          <Search class="absolute left-4 top-1/2 -translate-y-1/2 text-white/30" :size="18" />
          <input
            v-model="query.keyword"
            placeholder="搜索公告标题..."
            class="w-full pl-11 pr-4 py-3 rounded-full bg-white/5 border border-white/10
                   text-white text-sm font-body placeholder-white/30
                   focus:outline-none focus:border-white/25 transition-all"
            @keyup.enter="handleSearch"
          />
        </div>
        <button
          class="px-5 py-3 rounded-full liquid-glass text-white text-sm font-body
                 hover:bg-white/10 transition-all cursor-pointer"
          @click="handleSearch"
        >
          搜索
        </button>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="flex justify-center py-20">
        <div class="w-8 h-8 border-2 border-white/20 border-t-white/70 rounded-full animate-spin" />
      </div>

      <!-- 空状态 -->
      <div v-else-if="list.length === 0" class="text-center py-20">
        <p class="text-white/40 text-sm font-body">暂无公告</p>
      </div>

      <!-- 列表 -->
      <div v-else class="space-y-4">
        <div
          v-for="item in list"
          :key="item.id"
          class="liquid-glass rounded-2xl p-5 md:p-6 cursor-pointer
                 hover:bg-white/5 transition-all duration-300 group"
          @click="router.push(`/notices/${item.id}`)"
        >
          <div class="flex items-start justify-between gap-4">
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 mb-2">
                <Pin v-if="item.topFlag === 1" :size="14" class="text-amber-400 shrink-0" />
                <span
                  class="inline-block px-2.5 py-0.5 rounded-full text-xs font-body
                         bg-white/10 text-white/70"
                >
                  {{ noticeTypeLabel(item.noticeType) }}
                </span>
              </div>
              <h3 class="text-white text-base font-body font-medium truncate
                         group-hover:text-white/90 transition-colors">
                {{ item.title }}
              </h3>
            </div>
            <span class="text-white/30 text-xs font-body shrink-0 mt-1">
              {{ formatDate(item.publishTime || item.createTime) }}
            </span>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="totalPages > 1" class="flex items-center justify-center gap-3 mt-8">
        <button
          :disabled="query.current <= 1"
          class="p-2 rounded-full text-white/50 hover:text-white hover:bg-white/10
                 transition-all cursor-pointer disabled:opacity-30 disabled:cursor-default"
          @click="goPage(query.current - 1)"
        >
          <ChevronLeft :size="18" />
        </button>
        <span class="text-white/60 text-sm font-body">
          {{ query.current }} / {{ totalPages }}
        </span>
        <button
          :disabled="query.current >= totalPages"
          class="p-2 rounded-full text-white/50 hover:text-white hover:bg-white/10
                 transition-all cursor-pointer disabled:opacity-30 disabled:cursor-default"
          @click="goPage(query.current + 1)"
        >
          <ChevronRight :size="18" />
        </button>
      </div>
    </div>
  </div>
</template>
