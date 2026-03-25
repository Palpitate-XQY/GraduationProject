<script setup lang="ts">
/**
 * RepairListView — 我的报修列表
 * 居民端报修工单列表，带状态筛选
 */
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Search, ChevronLeft, ChevronRight, AlertTriangle } from 'lucide-vue-next'
import { myRepairPage } from '@/api/repair'
import type { RepairOrderVO } from '@/types/repair'
import { REPAIR_STATUS_MAP, type RepairStatus } from '@/types/repair'

const router = useRouter()
const loading = ref(false)
const list = ref<RepairOrderVO[]>([])
const total = ref(0)
const query = reactive({ current: 1, size: 10, keyword: '', status: '' })

async function fetchData() {
  loading.value = true
  try {
    const params: any = { current: query.current, size: query.size }
    if (query.keyword) params.keyword = query.keyword
    if (query.status) params.status = query.status
    const res = await myRepairPage(params)
    list.value = res.data.records
    total.value = res.data.total
  } catch { /* handled */ } finally {
    loading.value = false
  }
}

function handleSearch() { query.current = 1; fetchData() }
function goPage(p: number) { query.current = p; fetchData() }
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / query.size)))

function formatDate(d: string) {
  return new Date(d).toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

function statusColor(s: RepairStatus) {
  const m: Record<string, string> = {
    PENDING_ACCEPT: 'bg-amber-500/20 text-amber-300',
    ACCEPTED: 'bg-sky-500/20 text-sky-300',
    ASSIGNED: 'bg-indigo-500/20 text-indigo-300',
    PROCESSING: 'bg-blue-500/20 text-blue-300',
    WAIT_CONFIRM: 'bg-violet-500/20 text-violet-300',
    DONE: 'bg-emerald-500/20 text-emerald-300',
    CLOSED: 'bg-white/10 text-white/50',
    REJECTED: 'bg-red-500/20 text-red-300',
    REOPENED: 'bg-orange-500/20 text-orange-300',
  }
  return m[s] || 'bg-white/10 text-white/60'
}

function emergencyColor(e: string) {
  const m: Record<string, string> = { HIGH: 'text-red-400', MEDIUM: 'text-amber-400', LOW: 'text-white/40' }
  return m[e] || 'text-white/40'
}

const statusOptions = [
  { label: '全部状态', value: '' },
  ...Object.entries(REPAIR_STATUS_MAP).map(([k, v]) => ({ label: v, value: k })),
]

onMounted(fetchData)
</script>

<template>
  <div class="min-h-screen pt-24 pb-12 px-4 md:px-8 lg:px-16">
    <div class="max-w-5xl mx-auto">
      <!-- 页头 -->
      <div class="flex items-center justify-between mb-8">
        <div>
          <h1 class="text-3xl md:text-4xl font-heading italic text-white tracking-tight">我的报修</h1>
          <p class="text-white/50 text-sm font-body mt-2">查看和管理您的报修工单</p>
        </div>
        <button
          class="flex items-center gap-2 px-5 py-2.5 rounded-full liquid-glass-strong
                 text-white text-sm font-body font-medium hover:scale-105
                 transition-all cursor-pointer"
          @click="router.push('/repair/create')"
        >
          <Plus :size="18" /> 发起报修
        </button>
      </div>

      <!-- 搜索 + 筛选 -->
      <div class="flex items-center gap-3 mb-6 flex-wrap">
        <div class="flex-1 min-w-[200px] relative">
          <Search class="absolute left-4 top-1/2 -translate-y-1/2 text-white/30" :size="18" />
          <input
            v-model="query.keyword" placeholder="搜索工单..."
            class="w-full pl-11 pr-4 py-3 rounded-full bg-white/5 border border-white/10
                   text-white text-sm font-body placeholder-white/30
                   focus:outline-none focus:border-white/25 transition-all"
            @keyup.enter="handleSearch"
          />
        </div>
        <select
          v-model="query.status"
          class="px-4 py-3 rounded-full bg-white/5 border border-white/10
                 text-white text-sm font-body appearance-none cursor-pointer
                 focus:outline-none focus:border-white/25 transition-all min-w-[130px]"
          @change="handleSearch"
        >
          <option v-for="opt in statusOptions" :key="opt.value" :value="opt.value" class="bg-gray-900">
            {{ opt.label }}
          </option>
        </select>
      </div>

      <!-- 加载 -->
      <div v-if="loading" class="flex justify-center py-20">
        <div class="w-8 h-8 border-2 border-white/20 border-t-white/70 rounded-full animate-spin" />
      </div>

      <!-- 空 -->
      <div v-else-if="list.length === 0" class="text-center py-20">
        <p class="text-white/40 text-sm font-body mb-4">暂无报修记录</p>
        <button
          class="px-5 py-2.5 rounded-full liquid-glass text-white text-sm font-body
                 hover:bg-white/10 transition-all cursor-pointer"
          @click="router.push('/repair/create')"
        >
          发起第一个报修
        </button>
      </div>

      <!-- 列表 -->
      <div v-else class="space-y-3">
        <div
          v-for="item in list" :key="item.id"
          class="liquid-glass rounded-2xl p-5 cursor-pointer
                 hover:bg-white/5 transition-all duration-300 group"
          @click="router.push(`/repair/${item.id}`)"
        >
          <div class="flex items-start justify-between gap-4">
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 mb-2 flex-wrap">
                <span :class="['inline-block px-2.5 py-0.5 rounded-full text-xs font-body', statusColor(item.status)]">
                  {{ REPAIR_STATUS_MAP[item.status] || item.status }}
                </span>
                <span class="text-white/30 text-xs font-body">{{ item.orderNo }}</span>
                <AlertTriangle v-if="item.emergencyLevel === 'HIGH'" :size="14" :class="emergencyColor(item.emergencyLevel)" />
              </div>
              <h3 class="text-white text-sm font-body font-medium truncate
                         group-hover:text-white/90 transition-colors">{{ item.title }}</h3>
              <p class="text-white/40 text-xs font-body mt-1 truncate">{{ item.repairAddress }}</p>
            </div>
            <span class="text-white/30 text-xs font-body shrink-0">{{ formatDate(item.createTime) }}</span>
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
