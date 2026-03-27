export type AnalyticsPeriodType = 'DAILY' | 'WEEKLY'

export interface WordCloudItemVO {
  word: string
  weight: number
}

export interface AnalyticsWordCloudVO {
  reportType: AnalyticsPeriodType
  reportDate: string
  scopeKind: string
  generatedAt: string
  items: WordCloudItemVO[]
}

export interface AnalyticsReportVO {
  id: number
  reportType: AnalyticsPeriodType
  reportDate: string
  scopeKind: string
  scopeKey: string
  periodStart: string
  periodEnd: string
  metrics: Record<string, unknown>
  summaryMarkdown: string
  summaryHtml: string
  aiMode: string
  modelName?: string | null
  status: string
  generatedAt: string
}

export interface AnalyticsReportPageQuery {
  current?: number
  size?: number
  periodType?: AnalyticsPeriodType
}

export interface AnalyticsGenerateRequest {
  periodType: AnalyticsPeriodType
  anchorDate?: string
  force?: boolean
}
