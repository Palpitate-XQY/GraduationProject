<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import AdminPageShell from '@/components/admin/AdminPageShell.vue'
import AdminTablePager from '@/components/admin/AdminTablePager.vue'
import { useAdminTable } from '@/composables/useAdminTable'
import { useActionPermission } from '@/composables/useActionPermission'
import {
  analyticsReportPage,
  generateAnalyticsReport,
  getAnalyticsWordCloud,
  getLatestAnalyticsReport,
} from '@/api/analytics'
import type { AnalyticsPeriodType, AnalyticsReportVO, AnalyticsWordCloudVO } from '@/types/analytics'

const { can } = useActionPermission()

const periodType = ref<AnalyticsPeriodType>('DAILY')
const anchorDate = ref(formatDate(new Date()))
const activeTab = ref<'wordcloud' | 'reports'>(can('analytics:wordcloud:view') ? 'wordcloud' : 'reports')

const wordCloudLoading = ref(false)
const latestLoading = ref(false)
const generating = ref(false)
const wordCloudError = ref('')
const latestError = ref('')
const wordCloudData = ref<AnalyticsWordCloudVO | null>(null)
const latestReport = ref<AnalyticsReportVO | null>(null)

const wordCloudRef = ref<HTMLDivElement | null>(null)
let echartsModule: typeof import('echarts') | null = null
let echartsLoader: Promise<void> | null = null
let wordCloudChart: import('echarts').ECharts | null = null

const table = useAdminTable<AnalyticsReportVO, { current: number; size: number; periodType: AnalyticsPeriodType }>(
  {
    current: 1,
    size: 10,
    periodType: 'DAILY',
  },
  (query) => analyticsReportPage(query),
)

const detailVisible = ref(false)
const currentReport = ref<AnalyticsReportVO | null>(null)

const canGenerate = computed(() => can('analytics:report:generate'))
const canViewWordCloud = computed(() => can('analytics:wordcloud:view'))
const canViewReports = computed(() => can('analytics:report:view'))

watch(periodType, async (value) => {
  table.query.periodType = value
  await refreshAll()
})

watch(activeTab, async () => {
  await nextTick()
  await renderWordCloud()
})

watch(wordCloudData, async () => {
  await renderWordCloud()
})

function formatDate(date: Date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

async function ensureEchartsLoaded() {
  if (echartsModule) return
  if (!echartsLoader) {
    echartsLoader = Promise.all([import('echarts'), import('echarts-wordcloud')])
      .then(([module]) => {
        echartsModule = module
      })
      .finally(() => {
        echartsLoader = null
      })
  }
  await echartsLoader
}

async function ensureWordCloudChart() {
  if (!wordCloudRef.value) return null
  await ensureEchartsLoaded()
  if (!echartsModule) return null
  if (!wordCloudChart) {
    wordCloudChart = echartsModule.init(wordCloudRef.value)
  }
  return wordCloudChart
}

async function renderWordCloud() {
  if (!canViewWordCloud.value) return
  const chart = await ensureWordCloudChart()
  if (!chart) return
  const items = wordCloudData.value?.items || []
  chart.setOption(
    {
      backgroundColor: 'transparent',
      tooltip: { show: true },
      series: [
        {
          type: 'wordCloud',
          shape: 'circle',
          gridSize: 10,
          sizeRange: [14, 52],
          rotationRange: [-45, 90],
          drawOutOfBound: false,
          textStyle: {
            color: () => {
              const palette = ['#c7f2ff', '#9ad9ff', '#7dd3fc', '#a7f3d0', '#dbeafe', '#f0f9ff']
              return palette[Math.floor(Math.random() * palette.length)]
            },
            fontFamily: 'Barlow, sans-serif',
            fontWeight: 600,
          },
          emphasis: {
            textStyle: {
              shadowBlur: 8,
              shadowColor: 'rgba(255,255,255,0.4)',
            },
          },
          data: items.map((item) => ({
            name: item.word,
            value: item.weight,
          })),
        },
      ],
    } as any,
    true,
  )
  chart.resize()
}

async function loadWordCloud() {
  if (!canViewWordCloud.value) {
    wordCloudData.value = null
    return
  }
  wordCloudLoading.value = true
  wordCloudError.value = ''
  try {
    const res = await getAnalyticsWordCloud({
      periodType: periodType.value,
      anchorDate: anchorDate.value || undefined,
    })
    wordCloudData.value = res.data
  } catch (error) {
    wordCloudError.value = error instanceof Error ? error.message : '词云加载失败'
  } finally {
    wordCloudLoading.value = false
  }
}

async function loadLatestReport() {
  if (!canViewReports.value) {
    latestReport.value = null
    return
  }
  latestLoading.value = true
  latestError.value = ''
  try {
    const res = await getLatestAnalyticsReport(periodType.value)
    latestReport.value = res.data
  } catch (error) {
    latestError.value = error instanceof Error ? error.message : '最新报告加载失败'
  } finally {
    latestLoading.value = false
  }
}

async function loadReportPage() {
  if (!canViewReports.value) {
    table.records.value = []
    table.total.value = 0
    return
  }
  await table.load()
}

async function refreshAll() {
  await Promise.all([loadWordCloud(), loadLatestReport(), loadReportPage()])
}

async function handleGenerate(force = false) {
  if (!canGenerate.value) return
  generating.value = true
  try {
    await generateAnalyticsReport({
      periodType: periodType.value,
      anchorDate: anchorDate.value || undefined,
      force,
    })
    ElMessage.success('分析报告已生成')
    await refreshAll()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '生成分析报告失败')
  } finally {
    generating.value = false
  }
}

function openReportDetail(report: AnalyticsReportVO) {
  currentReport.value = report
  detailVisible.value = true
}

function resizeChart() {
  wordCloudChart?.resize()
}

onMounted(async () => {
  window.addEventListener('resize', resizeChart)
  await refreshAll()
  await nextTick()
  await renderWordCloud()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeChart)
  if (wordCloudChart) {
    wordCloudChart.dispose()
    wordCloudChart = null
  }
})
</script>

<template>
  <AdminPageShell title="智能分析中心" description="基于公告、活动、报修语料生成词云和日报/周报，可按数据范围查看。">
    <template #header-extra>
      <div class="flex flex-wrap items-center gap-2">
        <el-select v-model="periodType" style="width: 130px">
          <el-option label="日报" value="DAILY" />
          <el-option label="周报" value="WEEKLY" />
        </el-select>
        <el-date-picker
          v-model="anchorDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="锚点日期"
          style="width: 160px"
        />
        <el-button v-if="canGenerate" type="primary" :loading="generating" @click="handleGenerate(false)">手动生成</el-button>
        <el-button v-if="canGenerate" :loading="generating" @click="handleGenerate(true)">强制重算</el-button>
      </div>
    </template>

    <div class="liquid-glass rounded-3xl p-4 md:p-5 space-y-4">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="词云分析" name="wordcloud">
          <div v-if="!canViewWordCloud" class="rounded-2xl border border-white/20 bg-black/20 p-4 text-sm text-white/75">
            当前账号无词云查看权限。
          </div>
          <template v-else>
            <el-alert v-if="wordCloudError" type="error" :closable="false" :title="wordCloudError" />
            <div class="grid grid-cols-1 gap-4 lg:grid-cols-3">
              <div class="liquid-glass rounded-2xl p-4 lg:col-span-2">
                <div
                  ref="wordCloudRef"
                  v-loading="wordCloudLoading"
                  class="h-[380px] w-full rounded-xl border border-white/10 bg-black/15"
                />
              </div>
              <div class="liquid-glass rounded-2xl p-4">
                <h3 class="text-sm font-body text-white/80">Top 热词</h3>
                <ul class="mt-3 space-y-2 text-sm text-white/80">
                  <li v-for="(item, idx) in wordCloudData?.items.slice(0, 12) || []" :key="item.word" class="flex items-center justify-between">
                    <span>{{ idx + 1 }}. {{ item.word }}</span>
                    <span class="text-white/60">{{ item.weight }}</span>
                  </li>
                </ul>
              </div>
            </div>
          </template>
        </el-tab-pane>

        <el-tab-pane label="报告中心" name="reports">
          <div v-if="!canViewReports" class="rounded-2xl border border-white/20 bg-black/20 p-4 text-sm text-white/75">
            当前账号无报告查看权限。
          </div>
          <template v-else>
            <div class="grid grid-cols-1 gap-4 xl:grid-cols-3">
              <div class="liquid-glass rounded-2xl p-4 xl:col-span-1">
                <h3 class="text-sm font-body text-white/80">最新{{ periodType === 'DAILY' ? '日报' : '周报' }}摘要</h3>
                <div v-loading="latestLoading" class="mt-3">
                  <el-alert v-if="latestError" type="error" :closable="false" :title="latestError" />
                  <div v-else-if="latestReport" class="space-y-2">
                    <p class="text-xs text-white/60">生成时间：{{ latestReport.generatedAt }}</p>
                    <div class="report-markdown rounded-xl border border-white/10 bg-black/20 p-3 text-sm" v-html="latestReport.summaryHtml" />
                  </div>
                  <p v-else class="text-sm text-white/65">暂无可用摘要</p>
                </div>
              </div>

              <div class="liquid-glass rounded-2xl p-4 xl:col-span-2 space-y-3">
                <el-alert v-if="table.error.value" type="error" :closable="false" :title="table.error.value" />
                <el-table :data="table.records.value" v-loading="table.loading.value" class="admin-table" row-key="id" border>
                  <el-table-column prop="reportDate" label="报告日期" width="130" />
                  <el-table-column prop="scopeKind" label="范围类型" width="140" />
                  <el-table-column prop="aiMode" label="生成模式" width="120" />
                  <el-table-column prop="generatedAt" label="生成时间" min-width="170" />
                  <el-table-column label="操作" width="120" fixed="right">
                    <template #default="scope">
                      <el-button size="small" @click="openReportDetail(scope.row)">查看</el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <AdminTablePager
                  :current="table.query.current"
                  :size="table.query.size"
                  :total="table.total.value"
                  @update:current="table.changePage"
                  @update:size="(size) => { table.query.size = size; table.search() }"
                />
              </div>
            </div>
          </template>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-drawer v-model="detailVisible" title="报告详情" size="60%">
      <div v-if="currentReport" class="space-y-4">
        <div class="grid grid-cols-2 gap-3 text-sm">
          <p><strong>类型：</strong>{{ currentReport.reportType }}</p>
          <p><strong>日期：</strong>{{ currentReport.reportDate }}</p>
          <p><strong>范围：</strong>{{ currentReport.scopeKind }}</p>
          <p><strong>模式：</strong>{{ currentReport.aiMode }}</p>
        </div>
        <div class="report-markdown rounded-xl border border-white/10 bg-black/20 p-4 text-sm" v-html="currentReport.summaryHtml" />
      </div>
    </el-drawer>
  </AdminPageShell>
</template>

<style scoped>
.report-markdown :deep(h1),
.report-markdown :deep(h2),
.report-markdown :deep(h3) {
  margin: 0.6rem 0 0.4rem;
  font-family: var(--font-heading);
  font-style: italic;
  color: rgba(255, 255, 255, 0.94);
}

.report-markdown :deep(p),
.report-markdown :deep(li),
.report-markdown :deep(strong) {
  color: rgba(255, 255, 255, 0.86);
  font-family: var(--font-body);
}

.report-markdown :deep(ul) {
  margin: 0.4rem 0;
  padding-left: 1rem;
}
</style>
