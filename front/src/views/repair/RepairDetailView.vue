<script setup lang="ts">
/**
 * RepairDetailView — 报修工单详情
 * 显示完整工单信息 + 状态时间线 + 操作按钮
 */
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, Clock, MapPin, Phone, AlertTriangle, Star,
  CheckCircle, RotateCcw, BellRing,
} from 'lucide-vue-next'
import { myRepairDetail, confirmRepair, urgeRepair, reopenRepair, evaluateRepair } from '@/api/repair'
import type { RepairOrderVO } from '@/types/repair'
import { REPAIR_STATUS_MAP, type RepairStatus } from '@/types/repair'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const actionLoading = ref(false)
const order = ref<RepairOrderVO | null>(null)

function fmt(d: string | null) {
  if (!d) return '—'
  return new Date(d).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
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

/** 状态时间线数据 */
const timeline = computed(() => {
  if (!order.value) return []
  const o = order.value
  const items: { label: string; time: string | null; active: boolean }[] = [
    { label: '提交报修', time: o.createTime, active: true },
    { label: '物业受理', time: o.acceptedTime, active: !!o.acceptedTime },
    { label: '分派维修', time: o.assignedTime, active: !!o.assignedTime },
    { label: '开始处理', time: o.processingTime, active: !!o.processingTime },
    { label: '维修完成', time: o.finishTime, active: !!o.finishTime },
    { label: '居民确认', time: o.confirmTime, active: !!o.confirmTime },
  ]
  return items
})

/** 是否可以执行居民操作 */
const canConfirm = computed(() => order.value?.status === 'WAIT_CONFIRM')
const canUrge = computed(() =>
  order.value && ['PENDING_ACCEPT', 'ACCEPTED', 'ASSIGNED', 'PROCESSING'].includes(order.value.status),
)
const canReopen = computed(() => order.value?.status === 'WAIT_CONFIRM')
const canEvaluate = computed(() => order.value?.status === 'DONE' && !order.value.evaluateScore)

async function loadData() {
  const id = Number(route.params.id)
  try {
    const res = await myRepairDetail(id)
    order.value = res.data
  } catch { /* handled */ } finally {
    loading.value = false
  }
}

async function handleConfirm() {
  if (!order.value) return
  actionLoading.value = true
  try {
    await confirmRepair(order.value.id)
    ElMessage.success('已确认解决')
    await loadData()
  } catch { /* handled */ } finally { actionLoading.value = false }
}

async function handleUrge() {
  if (!order.value) return
  actionLoading.value = true
  try {
    await urgeRepair(order.value.id)
    ElMessage.success('催单成功')
    await loadData()
  } catch { /* handled */ } finally { actionLoading.value = false }
}

async function handleReopen() {
  if (!order.value) return
  try {
    const { value } = await ElMessageBox.prompt('请描述未解决的原因', '反馈未解决', {
      confirmButtonText: '提交', cancelButtonText: '取消', inputPlaceholder: '原因描述...',
    })
    if (!value?.trim()) return
    actionLoading.value = true
    await reopenRepair(order.value.id, { reason: value })
    ElMessage.success('已反馈，工单将重新处理')
    await loadData()
  } catch { /* cancelled or handled */ } finally { actionLoading.value = false }
}

async function handleEvaluate() {
  if (!order.value) return
  try {
    const { value } = await ElMessageBox.prompt('请对本次服务评价（选填）', '评价服务', {
      confirmButtonText: '提交', cancelButtonText: '取消',
    })
    actionLoading.value = true
    await evaluateRepair(order.value.id, { evaluateScore: 5, evaluateContent: value || '' })
    ElMessage.success('评价成功，感谢反馈')
    await loadData()
  } catch { /* cancelled or handled */ } finally { actionLoading.value = false }
}

onMounted(loadData)
</script>

<template>
  <div class="min-h-screen pt-24 pb-12 px-4 md:px-8 lg:px-16">
    <div class="max-w-3xl mx-auto">
      <button
        class="flex items-center gap-2 text-white/50 text-sm font-body mb-6
               hover:text-white transition-colors cursor-pointer"
        @click="router.push('/repair')"
      >
        <ArrowLeft :size="16" /> 返回我的报修
      </button>

      <div v-if="loading" class="flex justify-center py-20">
        <div class="w-8 h-8 border-2 border-white/20 border-t-white/70 rounded-full animate-spin" />
      </div>

      <div v-else-if="!order" class="text-center py-20">
        <p class="text-white/40 text-sm font-body">工单不存在</p>
      </div>

      <div v-else class="space-y-4">
        <!-- 基本信息 -->
        <div class="liquid-glass rounded-3xl p-6 md:p-8">
          <div class="flex items-start justify-between gap-4 mb-4">
            <h1 class="text-xl md:text-2xl font-heading italic text-white tracking-tight">
              {{ order.title }}
            </h1>
            <span :class="['shrink-0 px-3 py-1 rounded-full text-xs font-body', statusColor(order.status)]">
              {{ REPAIR_STATUS_MAP[order.status] }}
            </span>
          </div>

          <p class="text-white/30 text-xs font-body mb-6">{{ order.orderNo }}</p>

          <p class="text-white/80 text-sm font-body leading-relaxed mb-6">{{ order.description }}</p>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-3 text-white/50 text-sm font-body">
            <div class="flex items-center gap-2">
              <MapPin :size="15" class="text-white/30" /> {{ order.repairAddress }}
            </div>
            <div class="flex items-center gap-2">
              <Phone :size="15" class="text-white/30" /> {{ order.contactPhone }}
            </div>
            <div class="flex items-center gap-2">
              <Clock :size="15" class="text-white/30" /> 提交于 {{ fmt(order.createTime) }}
            </div>
            <div class="flex items-center gap-2">
              <AlertTriangle :size="15" class="text-white/30" />
              紧急程度：{{ order.emergencyLevel === 'HIGH' ? '高' : order.emergencyLevel === 'MEDIUM' ? '中' : '低' }}
            </div>
          </div>

          <!-- 维修结果 -->
          <div v-if="order.finishDesc" class="mt-6 pt-4 border-t border-white/10">
            <h4 class="text-white/60 text-xs font-body uppercase tracking-wider mb-2">维修结果</h4>
            <p class="text-white/70 text-sm font-body">{{ order.finishDesc }}</p>
          </div>

          <!-- 驳回原因 -->
          <div v-if="order.rejectReason" class="mt-6 pt-4 border-t border-white/10">
            <h4 class="text-red-400/80 text-xs font-body uppercase tracking-wider mb-2">驳回原因</h4>
            <p class="text-white/70 text-sm font-body">{{ order.rejectReason }}</p>
          </div>

          <!-- 评价 -->
          <div v-if="order.evaluateScore" class="mt-6 pt-4 border-t border-white/10">
            <h4 class="text-white/60 text-xs font-body uppercase tracking-wider mb-2">我的评价</h4>
            <div class="flex items-center gap-1 mb-1">
              <Star v-for="i in order.evaluateScore" :key="i" :size="16" class="text-amber-400 fill-amber-400" />
            </div>
            <p v-if="order.evaluateContent" class="text-white/60 text-sm font-body">{{ order.evaluateContent }}</p>
          </div>
        </div>

        <!-- 时间线 -->
        <div class="liquid-glass rounded-3xl p-6 md:p-8">
          <h3 class="text-white/70 text-xs font-body uppercase tracking-wider mb-5">工单进度</h3>
          <div class="space-y-0">
            <div v-for="(step, i) in timeline" :key="i" class="flex gap-4 pb-5 last:pb-0">
              <div class="flex flex-col items-center">
                <div :class="['w-3 h-3 rounded-full shrink-0', step.active ? 'bg-emerald-400' : 'bg-white/15']" />
                <div v-if="i < timeline.length - 1" :class="['w-px flex-1 mt-1', step.active ? 'bg-emerald-400/30' : 'bg-white/10']" />
              </div>
              <div class="pb-1 -mt-0.5">
                <p :class="['text-sm font-body', step.active ? 'text-white' : 'text-white/30']">{{ step.label }}</p>
                <p class="text-white/30 text-xs font-body mt-0.5">{{ step.time ? fmt(step.time) : '—' }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div v-if="canConfirm || canUrge || canReopen || canEvaluate"
             class="liquid-glass rounded-3xl p-6 flex flex-wrap gap-3">
          <button v-if="canConfirm" :disabled="actionLoading"
                  class="flex items-center gap-2 px-5 py-2.5 rounded-full liquid-glass-strong
                         text-white text-sm font-body font-medium hover:scale-105
                         transition-all cursor-pointer disabled:opacity-50"
                  @click="handleConfirm">
            <CheckCircle :size="16" /> 确认解决
          </button>
          <button v-if="canReopen" :disabled="actionLoading"
                  class="flex items-center gap-2 px-5 py-2.5 rounded-full liquid-glass
                         text-white/70 text-sm font-body hover:text-white hover:bg-white/10
                         transition-all cursor-pointer disabled:opacity-50"
                  @click="handleReopen">
            <RotateCcw :size="16" /> 未解决
          </button>
          <button v-if="canUrge" :disabled="actionLoading"
                  class="flex items-center gap-2 px-5 py-2.5 rounded-full liquid-glass
                         text-amber-300 text-sm font-body hover:bg-white/10
                         transition-all cursor-pointer disabled:opacity-50"
                  @click="handleUrge">
            <BellRing :size="16" /> 催单 ({{ order.urgeCount }})
          </button>
          <button v-if="canEvaluate" :disabled="actionLoading"
                  class="flex items-center gap-2 px-5 py-2.5 rounded-full liquid-glass
                         text-amber-300 text-sm font-body hover:bg-white/10
                         transition-all cursor-pointer disabled:opacity-50"
                  @click="handleEvaluate">
            <Star :size="16" /> 评价服务
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
