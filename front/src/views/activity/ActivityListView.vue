<script setup lang="ts">
/**
 * ActivityListView — 社区活动列表页
 * 居民端活动分页，卡片网格布局
 */
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Search, MapPin, Clock, Users, ChevronLeft, ChevronRight } from 'lucide-vue-next'
import { residentActivityPage } from '@/api/activity'
import type { ActivityVO } from '@/types/activity'

const router = useRouter()
const loading = ref(false)
const list = ref<ActivityVO[]>([])
const total = ref(0)
const query = reactive({ current: 1, size: 8, keyword: '' })

async function fetchData() {
  loading.value = true
  try {
    const res = await residentActivityPage(query)
    list.value = res.data.records
    total.value = res.data.total
  } catch { /* handled */ } finally {
    loading.value = false
  }
}

function handleSearch() { query.current = 1; fetchData() }
function goPage(p: number) { query.current = p; fetchData() }
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / query.size)))

function formatDate(d: string | null) {
  if (!d) return '—'
  return new Date(d).toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

function statusLabel(s: string) {
  const m: Record<string, string> = { DRAFT: '草稿', PUBLISHED: '已发布', RECALLED: '已撤回', FINISHED: '已结束' }
  return m[s] || s
}

function statusColor(s: string) {
  const m: Record<string, string> = { DRAFT: 'bg-gray-500/20 text-gray-300', PUBLISHED: 'bg-emerald-500/20 text-emerald-300', RECALLED: 'bg-amber-500/20 text-amber-300', FINISHED: 'bg-white/10 text-white/50' }
  return m[s] || 'bg-white/10 text-white/60'
}

onMounted(fetchData)
</script>

<template>
  <div class="min-h-screen pt-24 pb-12 px-4 md:px-8 lg:px-16">
    <div class="max-w-6xl mx-auto">
      <!-- 页头 -->
      <div class="mb-8">
        <h1 class="text-3xl md:text-4xl font-heading italic text-white tracking-tight">社区活动</h1>
        <p class="text-white/50 text-sm font-body mt-2">发现并参与精彩的社区活动</p>
      </div>

      <!-- 搜索 -->
      <div class="flex items-center gap-3 mb-6">
        <div class="flex-1 relative">
          <Search class="absolute left-4 top-1/2 -translate-y-1/2 text-white/30" :size="18" />
          <input
            v-model="query.keyword" placeholder="搜索活动..."
            class="w-full pl-11 pr-4 py-3 rounded-full bg-white/5 border border-white/10
                   text-white text-sm font-body placeholder-white/30
                   focus:outline-none focus:border-white/25 transition-all"
            @keyup.enter="handleSearch"
          />
        </div>
        <button class="px-5 py-3 rounded-full liquid-glass text-white text-sm font-body
                       hover:bg-white/10 transition-all cursor-pointer" @click="handleSearch">搜索</button>
      </div>

      <!-- 加载 -->
      <div v-if="loading" class="flex justify-center py-20">
        <div class="w-8 h-8 border-2 border-white/20 border-t-white/70 rounded-full animate-spin" />
      </div>

      <!-- 空 -->
      <div v-else-if="list.length === 0" class="text-center py-20">
        <p class="text-white/40 text-sm font-body">暂无活动</p>
      </div>

      <!-- 卡片网格 -->
      <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div
          v-for="item in list" :key="item.id"
          class="liquid-glass rounded-2xl p-5 md:p-6 cursor-pointer
                 hover:bg-white/5 transition-all duration-300 group"
          @click="router.push(`/activities/${item.id}`)"
        >
          <!-- 状态 -->
          <div class="flex items-center justify-between mb-3">
            <span :class="['inline-block px-2.5 py-0.5 rounded-full text-xs font-body', statusColor(item.status)]">
              {{ statusLabel(item.status) }}
            </span>
            <span class="text-white/30 text-xs font-body">{{ formatDate(item.createTime) }}</span>
          </div>

          <h3 class="text-white text-base font-body font-medium mb-3 truncate
                     group-hover:text-white/90 transition-colors">{{ item.title }}</h3>

          <div class="space-y-1.5 text-white/50 text-xs font-body">
            <div v-if="item.location" class="flex items-center gap-1.5">
              <MapPin :size="13" class="shrink-0" /> <span class="truncate">{{ item.location }}</span>
            </div>
            <div class="flex items-center gap-1.5">
              <Clock :size="13" class="shrink-0" />
              {{ formatDate(item.activityStartTime) }} — {{ formatDate(item.activityEndTime) }}
            </div>
            <div v-if="item.maxParticipants" class="flex items-center gap-1.5">
              <Users :size="13" class="shrink-0" /> 限 {{ item.maxParticipants }} 人
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="totalPages > 1" class="flex items-center justify-center gap-3 mt-8">
        <button :disabled="query.current<=1" class="p-2 rounded-full text-white/50 hover:text-white hover:bg-white/10 transition-all cursor-pointer disabled:opacity-30" @click="goPage(query.current-1)"><ChevronLeft :size="18" /></button>
        <span class="text-white/60 text-sm font-body">{{ query.current }} / {{ totalPages }}</span>
        <button :disabled="query.current>=totalPages" class="p-2 rounded-full text-white/50 hover:text-white hover:bg-white/10 transition-all cursor-pointer disabled:opacity-30" @click="goPage(query.current+1)"><ChevronRight :size="18" /></button>
      </div>
    </div>
  </div>
</template>
