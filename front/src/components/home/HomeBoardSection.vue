<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { AlertCircle, CalendarDays, ChevronRight, RefreshCw, Wrench } from 'lucide-vue-next'
import { myActivityPage, residentActivityPage } from '@/api/activity'
import { residentNoticePage } from '@/api/notice'
import { myRepairPage } from '@/api/repair'
import { useUserStore } from '@/stores/user'
import type { ActivityVO } from '@/types/activity'
import type { NoticeVO } from '@/types/notice'
import type { RepairOrderVO } from '@/types/repair'
import { REPAIR_STATUS_MAP } from '@/types/repair'
import { buildFilePreviewUrl, withFileAccessToken } from '@/utils/file-url'

type SectionKey = 'activities' | 'myActivities' | 'notices' | 'repairs'

const router = useRouter()
const userStore = useUserStore()
const { userInfo } = storeToRefs(userStore)

const activities = ref<ActivityVO[]>([])
const myActivities = ref<ActivityVO[]>([])
const notices = ref<NoticeVO[]>([])
const repairs = ref<RepairOrderVO[]>([])

const sectionLoading = reactive<Record<SectionKey, boolean>>({
  activities: false,
  myActivities: false,
  notices: false,
  repairs: false,
})

const sectionError = reactive<Record<SectionKey, string>>({
  activities: '',
  myActivities: '',
  notices: '',
  repairs: '',
})

const canViewActivityList = computed(() => userStore.hasPermission('activity:resident:list'))
const canViewActivityDetail = computed(() => userStore.hasPermission('activity:resident:view'))
const canViewMyActivity = computed(
  () => userStore.hasPermission('activity:signup') && canViewActivityList.value,
)
const canViewNoticeList = computed(() => userStore.hasPermission('notice:resident:list'))
const canViewNoticeDetail = computed(() => userStore.hasPermission('notice:resident:view'))
const canViewRepairList = computed(() => userStore.hasPermission('repair:my:list'))
const canViewRepairDetail = computed(() => userStore.hasPermission('repair:my:view'))

const displayName = computed(() => userInfo.value?.nickname || userInfo.value?.username || '')
const communityName = computed(() => {
  const name = userInfo.value?.communityOrgName?.trim()
  return name || '社区'
})
const welcomeTitle = computed(() => {
  if (!displayName.value) {
    return `欢迎回到${communityName.value}`
  }
  return `欢迎回到${communityName.value}，${displayName.value}`
})

const currentDate = computed(() => {
  const formatter = new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  })
  return formatter.format(new Date()).replace(/\//g, '-')
})

const featuredActivity = computed(() => activities.value[0] || null)
const activityQueue = computed(() => activities.value.slice(1, 4))
const progressActivities = computed(() => myActivities.value.slice(0, 3))

function navigate(path: string) {
  router.push(path)
}

function openActivity(id: number) {
  if (canViewActivityDetail.value) {
    navigate(`/activities/${id}`)
    return
  }
  navigate('/activities')
}

function openNotice(id: number) {
  if (canViewNoticeDetail.value) {
    navigate(`/notices/${id}`)
    return
  }
  navigate('/notices')
}

function openRepair(id: number) {
  if (canViewRepairDetail.value) {
    navigate(`/repair/${id}`)
    return
  }
  navigate('/repair')
}

function setSectionLoading(section: SectionKey, loading: boolean) {
  sectionLoading[section] = loading
}

function setSectionError(section: SectionKey, message = '') {
  sectionError[section] = message
}

function formatDateTime(value?: string | null) {
  if (!value) return '待定'
  return new Date(value).toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function formatNoticeTime(value?: string | null) {
  if (!value) return '刚刚'
  const diff = Date.now() - new Date(value).getTime()
  const hours = Math.floor(diff / (1000 * 60 * 60))
  if (hours < 1) return '刚刚'
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 7) return `${days}天前`
  return formatDateTime(value)
}

function resolveActivityCover(activity: ActivityVO | null) {
  if (!activity) return '/images/smart_community_bg.jpeg'
  if (activity.coverFileId) {
    return buildFilePreviewUrl(activity.coverFileId)
  }
  const attachment = activity.attachments?.find((item) => item.contentType?.startsWith('image/'))
  return withFileAccessToken(attachment?.accessUrl) || '/images/smart_community_bg.jpeg'
}

function repairStatusClass(status: RepairOrderVO['status']) {
  switch (status) {
    case 'DONE':
    case 'CLOSED':
      return 'bg-emerald-500/30 text-emerald-100'
    case 'PROCESSING':
    case 'ASSIGNED':
    case 'ACCEPTED':
      return 'bg-sky-500/30 text-sky-100'
    case 'REJECTED':
      return 'bg-rose-500/30 text-rose-100'
    default:
      return 'bg-amber-500/30 text-amber-100'
  }
}

function getActivityProgressLabel(activity: ActivityVO) {
  if (activity.status === 'FINISHED') return '已结束'
  if (activity.status === 'RECALLED') return '已撤回'
  if (activity.status === 'DRAFT') return '筹备中'

  const start = activity.activityStartTime ? new Date(activity.activityStartTime).getTime() : 0
  const end = activity.activityEndTime ? new Date(activity.activityEndTime).getTime() : 0
  const now = Date.now()

  if (start && now < start) return '待开始'
  if (end && now > end) return '已结束'
  return '进行中'
}

function activityProgressClass(activity: ActivityVO) {
  const label = getActivityProgressLabel(activity)
  if (label === '进行中') return 'bg-sky-500/30 text-sky-100'
  if (label === '待开始' || label === '筹备中') return 'bg-amber-500/30 text-amber-100'
  return 'bg-white/20 text-white/85'
}

async function fetchActivities() {
  activities.value = []
  if (!canViewActivityList.value) {
    return
  }

  setSectionError('activities')
  setSectionLoading('activities', true)

  try {
    const res = await residentActivityPage({ current: 1, size: 4 })
    activities.value = res.data.records || []
  } catch {
    setSectionError('activities', '活动数据加载失败，请稍后重试。')
  } finally {
    setSectionLoading('activities', false)
  }
}

async function fetchMyActivities() {
  myActivities.value = []
  if (!canViewMyActivity.value) {
    return
  }

  setSectionError('myActivities')
  setSectionLoading('myActivities', true)

  try {
    const res = await myActivityPage({ current: 1, size: 3 })
    myActivities.value = res.data.records || []
  } catch {
    setSectionError('myActivities', '我的活动进度加载失败，请稍后重试。')
  } finally {
    setSectionLoading('myActivities', false)
  }
}

async function fetchNotices() {
  notices.value = []
  if (!canViewNoticeList.value) {
    return
  }

  setSectionError('notices')
  setSectionLoading('notices', true)

  try {
    const res = await residentNoticePage({ current: 1, size: 3 })
    notices.value = res.data.records || []
  } catch {
    setSectionError('notices', '公告数据加载失败，请稍后重试。')
  } finally {
    setSectionLoading('notices', false)
  }
}

async function fetchRepairs() {
  repairs.value = []
  if (!canViewRepairList.value) {
    return
  }

  setSectionError('repairs')
  setSectionLoading('repairs', true)

  try {
    const res = await myRepairPage({ current: 1, size: 3 })
    repairs.value = res.data.records || []
  } catch {
    setSectionError('repairs', '报修数据加载失败，请稍后重试。')
  } finally {
    setSectionLoading('repairs', false)
  }
}

async function fetchBoardData() {
  try {
    await userStore.ensureUserInfo()
  } catch {
    await router.replace({ path: '/login', query: { redirect: '/' } })
    return
  }

  await Promise.all([fetchActivities(), fetchMyActivities(), fetchNotices(), fetchRepairs()])
}

onMounted(fetchBoardData)
</script>

<template>
  <section class="relative min-h-screen px-4 pb-10 pt-24 md:px-8 lg:px-12 lg:pt-28">
    <div class="mx-auto max-w-[1320px] space-y-5">
      <article class="liquid-panel rounded-[28px] px-6 py-6 md:px-8 md:py-8">
        <div class="flex flex-col gap-6 md:flex-row md:items-center md:justify-between">
          <div>
            <h1 class="text-4xl font-heading italic leading-[1.05] text-white md:text-6xl">
              {{ welcomeTitle }}
            </h1>
            <p class="mt-2 text-base font-body text-white/75 md:text-lg">
              今天也是美好的一天，看看社区里有什么新鲜事。
            </p>
          </div>
          <div class="text-left md:text-right">
            <p class="text-sm font-body text-white/60">当前时间</p>
            <p class="mt-1 text-3xl font-heading italic text-white md:text-4xl">
              {{ currentDate }}
            </p>
          </div>
        </div>
      </article>

      <div class="grid items-start gap-5 lg:grid-cols-12">
        <section class="space-y-5 lg:col-span-8">
          <div class="flex items-center justify-between">
            <h2 class="text-4xl font-heading italic text-white">近期活动概览</h2>
            <button
              v-if="canViewActivityList"
              type="button"
              class="inline-flex items-center gap-1 text-sm font-body text-white/70 transition hover:text-white"
              @click="navigate('/activities')"
            >
              查看全部
              <ChevronRight :size="15" />
            </button>
          </div>

          <article class="liquid-panel rounded-[24px] p-4 md:p-5">
            <div
              v-if="!canViewActivityList"
              class="rounded-2xl border border-white/20 bg-black/15 p-5 text-sm font-body text-white/70"
            >
              当前账号暂无“社区活动”访问权限。
            </div>

            <div v-else-if="sectionLoading.activities" class="grid gap-4 md:grid-cols-2">
              <div class="skeleton-card min-h-[250px] rounded-2xl" />
              <div class="space-y-3">
                <div class="skeleton-card h-[84px] rounded-2xl" />
                <div class="skeleton-card h-[84px] rounded-2xl" />
                <div class="skeleton-card h-[84px] rounded-2xl" />
              </div>
            </div>

            <div
              v-else-if="sectionError.activities"
              class="rounded-2xl border border-rose-300/30 bg-rose-500/10 p-5 text-white"
            >
              <p class="flex items-center gap-2 text-sm font-body">
                <AlertCircle :size="15" />
                {{ sectionError.activities }}
              </p>
              <button
                type="button"
                class="mt-3 inline-flex items-center gap-2 rounded-full border border-white/25 px-3 py-1.5 text-xs font-body text-white/85"
                @click="fetchActivities"
              >
                <RefreshCw :size="12" />
                重试
              </button>
            </div>

            <div v-else class="grid gap-4 md:grid-cols-2">
              <button
                v-if="featuredActivity"
                type="button"
                class="group relative min-h-[250px] overflow-hidden rounded-2xl text-left"
                @click="openActivity(featuredActivity.id)"
              >
                <img
                  :src="resolveActivityCover(featuredActivity)"
                  alt="activity-cover"
                  class="absolute inset-0 h-full w-full object-cover transition duration-500 group-hover:scale-105"
                />
                <div class="absolute inset-0 bg-black/35" />
                <div class="absolute bottom-4 left-4 right-4">
                  <span class="inline-flex rounded-full bg-black/45 px-2 py-0.5 text-xs text-white/90">进行中</span>
                  <p class="mt-2 line-clamp-2 text-3xl font-heading italic leading-tight text-white">
                    {{ featuredActivity.title }}
                  </p>
                </div>
              </button>
              <div
                v-else
                class="flex min-h-[250px] items-center justify-center rounded-2xl border border-white/20 bg-black/15 text-sm font-body text-white/65"
              >
                暂无近期活动
              </div>

              <div class="space-y-3">
                <button
                  v-for="item in activityQueue"
                  :key="item.id"
                  type="button"
                  class="liquid-sub-card group flex w-full items-center gap-3 rounded-2xl p-3 text-left"
                  @click="openActivity(item.id)"
                >
                  <img
                    :src="resolveActivityCover(item)"
                    alt="activity-thumb"
                    class="h-16 w-16 rounded-xl object-cover"
                  />
                  <div class="min-w-0">
                    <p class="truncate text-2xl font-heading italic text-white">{{ item.title }}</p>
                    <p class="mt-1 flex items-center gap-1 text-sm font-body text-white/65">
                      <CalendarDays :size="14" />
                      {{ formatDateTime(item.activityStartTime) }}
                    </p>
                  </div>
                </button>

                <div
                  v-if="activityQueue.length === 0"
                  class="liquid-sub-card rounded-2xl p-4 text-sm font-body text-white/65"
                >
                  更多活动正在筹备中，稍后回来看看。
                </div>
              </div>
            </div>
          </article>
        </section>

        <aside class="liquid-panel space-y-6 rounded-[24px] p-5 md:p-6 lg:col-span-4">
          <h2 class="text-4xl font-heading italic text-white">我的报修活动进度</h2>

          <section class="space-y-3">
            <div class="flex items-center justify-between">
              <h3 class="flex items-center gap-2 text-lg font-body font-semibold text-white/90">
                <Wrench :size="16" />
                我的报修
              </h3>
              <button
                v-if="canViewRepairList"
                type="button"
                class="inline-flex items-center gap-1 text-sm font-body text-white/70 transition hover:text-white"
                @click="navigate('/repair')"
              >
                查看全部
                <ChevronRight :size="15" />
              </button>
            </div>

            <div
              v-if="!canViewRepairList"
              class="rounded-2xl border border-white/20 bg-black/15 p-4 text-sm font-body text-white/70"
            >
              当前账号暂无“报修服务”访问权限。
            </div>

            <div v-else-if="sectionLoading.repairs" class="space-y-3">
              <div class="skeleton-card h-[88px] rounded-2xl" />
              <div class="skeleton-card h-[88px] rounded-2xl" />
              <div class="skeleton-card h-[88px] rounded-2xl" />
            </div>

            <div
              v-else-if="sectionError.repairs"
              class="rounded-2xl border border-rose-300/30 bg-rose-500/10 p-4 text-white"
            >
              <p class="flex items-center gap-2 text-sm font-body">
                <AlertCircle :size="15" />
                {{ sectionError.repairs }}
              </p>
              <button
                type="button"
                class="mt-3 inline-flex items-center gap-2 rounded-full border border-white/25 px-3 py-1.5 text-xs font-body text-white/85"
                @click="fetchRepairs"
              >
                <RefreshCw :size="12" />
                重试
              </button>
            </div>

            <template v-else>
              <button
                v-for="item in repairs"
                :key="item.id"
                type="button"
                class="liquid-sub-card w-full rounded-2xl p-4 text-left"
                @click="openRepair(item.id)"
              >
                <div class="flex items-center justify-between gap-2">
                  <p class="truncate text-xl font-heading italic text-white">{{ item.title }}</p>
                  <span class="shrink-0 rounded-full px-2 py-0.5 text-xs" :class="repairStatusClass(item.status)">
                    {{ REPAIR_STATUS_MAP[item.status] || item.status }}
                  </span>
                </div>
                <p class="mt-2 truncate text-sm font-body text-white/60">
                  维修工单号 {{ item.orderNo }}
                </p>
              </button>

              <div
                v-if="repairs.length === 0"
                class="liquid-sub-card rounded-2xl p-4 text-sm font-body text-white/65"
              >
                暂无报修记录
              </div>
            </template>
          </section>

          <div class="h-px bg-white/15" />

          <section class="space-y-3">
            <div class="flex items-center justify-between">
              <h3 class="flex items-center gap-2 text-lg font-body font-semibold text-white/90">
                <CalendarDays :size="16" />
                我的活动进度
              </h3>
              <button
                v-if="canViewMyActivity"
                type="button"
                class="inline-flex items-center gap-1 text-sm font-body text-white/70 transition hover:text-white"
                @click="navigate('/activities')"
              >
                查看全部
                <ChevronRight :size="15" />
              </button>
            </div>

            <div
              v-if="!canViewMyActivity"
              class="rounded-2xl border border-white/20 bg-black/15 p-4 text-sm font-body text-white/70"
            >
              当前账号暂无“我的活动”访问权限。
            </div>

            <div v-else-if="sectionLoading.myActivities" class="space-y-3">
              <div class="skeleton-card h-[88px] rounded-2xl" />
              <div class="skeleton-card h-[88px] rounded-2xl" />
              <div class="skeleton-card h-[88px] rounded-2xl" />
            </div>

            <div
              v-else-if="sectionError.myActivities"
              class="rounded-2xl border border-rose-300/30 bg-rose-500/10 p-4 text-white"
            >
              <p class="flex items-center gap-2 text-sm font-body">
                <AlertCircle :size="15" />
                {{ sectionError.myActivities }}
              </p>
              <button
                type="button"
                class="mt-3 inline-flex items-center gap-2 rounded-full border border-white/25 px-3 py-1.5 text-xs font-body text-white/85"
                @click="fetchMyActivities"
              >
                <RefreshCw :size="12" />
                重试
              </button>
            </div>

            <template v-else>
              <button
                v-for="item in progressActivities"
                :key="`progress-${item.id}`"
                type="button"
                class="liquid-sub-card w-full rounded-2xl p-4 text-left"
                @click="openActivity(item.id)"
              >
                <div class="flex items-center justify-between gap-2">
                  <p class="truncate text-xl font-heading italic text-white">{{ item.title }}</p>
                  <span class="shrink-0 rounded-full px-2 py-0.5 text-xs" :class="activityProgressClass(item)">
                    {{ getActivityProgressLabel(item) }}
                  </span>
                </div>
                <p class="mt-2 text-sm font-body text-white/60">
                  {{ formatDateTime(item.activityStartTime) }}
                </p>
              </button>

              <div
                v-if="progressActivities.length === 0"
                class="liquid-sub-card rounded-2xl p-4 text-sm font-body text-white/65"
              >
                暂无活动记录
              </div>
            </template>
          </section>
        </aside>
      </div>

      <section class="space-y-4">
        <div class="flex items-center justify-between">
          <h2 class="text-4xl font-heading italic text-white">最新紧急公告</h2>
          <button
            v-if="canViewNoticeList"
            type="button"
            class="inline-flex items-center gap-1 text-sm font-body text-white/70 transition hover:text-white"
            @click="navigate('/notices')"
          >
            查看全部
            <ChevronRight :size="15" />
          </button>
        </div>

        <div
          v-if="!canViewNoticeList"
          class="liquid-panel rounded-2xl p-5 text-sm font-body text-white/70"
        >
          当前账号暂无“社区公告”访问权限。
        </div>

        <div v-else-if="sectionLoading.notices" class="space-y-4">
          <div class="skeleton-card h-[112px] rounded-2xl" />
          <div class="skeleton-card h-[112px] rounded-2xl" />
          <div class="skeleton-card h-[112px] rounded-2xl" />
        </div>

        <div
          v-else-if="sectionError.notices"
          class="liquid-panel rounded-2xl border border-rose-300/30 bg-rose-500/10 p-5 text-white"
        >
          <p class="flex items-center gap-2 text-sm font-body">
            <AlertCircle :size="15" />
            {{ sectionError.notices }}
          </p>
          <button
            type="button"
            class="mt-3 inline-flex items-center gap-2 rounded-full border border-white/25 px-3 py-1.5 text-xs font-body text-white/85"
            @click="fetchNotices"
          >
            <RefreshCw :size="12" />
            重试
          </button>
        </div>

        <template v-else>
          <button
            v-for="item in notices"
            :key="item.id"
            type="button"
            class="liquid-panel w-full rounded-2xl p-5 text-left transition hover:bg-white/[0.06]"
            @click="openNotice(item.id)"
          >
            <div class="flex items-start justify-between gap-4">
              <div class="min-w-0">
                <div class="mb-2 inline-flex items-center gap-2">
                  <span class="rounded-full bg-rose-500/30 px-2 py-0.5 text-xs font-body text-rose-100">紧急</span>
                </div>
                <p class="truncate text-2xl font-heading italic text-white">{{ item.title }}</p>
                <p class="mt-2 line-clamp-1 text-sm font-body text-white/65">
                  {{ item.content || '点击查看公告详情。' }}
                </p>
              </div>
              <p class="shrink-0 text-sm font-body text-white/55">
                {{ formatNoticeTime(item.publishTime || item.createTime) }}
              </p>
            </div>
          </button>

          <div
            v-if="notices.length === 0"
            class="liquid-panel rounded-2xl p-5 text-sm font-body text-white/65"
          >
            暂无公告
          </div>
        </template>
      </section>
    </div>
  </section>
</template>

<style scoped>
.liquid-panel {
  border: 1px solid rgba(255, 255, 255, 0.24);
  background: rgba(10, 18, 30, 0.28);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.18), 0 20px 50px rgba(0, 0, 0, 0.26);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
}

.liquid-sub-card {
  border: 1px solid rgba(255, 255, 255, 0.16);
  background: rgba(14, 25, 38, 0.34);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  transition: transform 200ms ease, background-color 200ms ease, border-color 200ms ease;
}

.liquid-sub-card:hover {
  background: rgba(20, 35, 52, 0.4);
  border-color: rgba(255, 255, 255, 0.24);
  transform: translateY(-1px);
}

.skeleton-card {
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.08) 25%, rgba(255, 255, 255, 0.16) 37%, rgba(255, 255, 255, 0.08) 63%);
  background-size: 400% 100%;
  animation: shimmer 1.5s ease infinite;
}

@keyframes shimmer {
  0% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0 50%;
  }
}
</style>

