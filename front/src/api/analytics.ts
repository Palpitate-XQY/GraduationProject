import request from '@/utils/request'
import type { ApiResponse, PageResult } from '@/types/common'
import type {
  AnalyticsGenerateRequest,
  AnalyticsPeriodType,
  AnalyticsReportPageQuery,
  AnalyticsReportVO,
  AnalyticsWordCloudVO,
} from '@/types/analytics'

export const getAnalyticsWordCloud = (params: { periodType?: AnalyticsPeriodType; anchorDate?: string }) =>
  request.get<any, ApiResponse<AnalyticsWordCloudVO>>('/api/system/analytics/wordcloud', { params })

export const getLatestAnalyticsReport = (periodType: AnalyticsPeriodType = 'DAILY') =>
  request.get<any, ApiResponse<AnalyticsReportVO>>('/api/system/analytics/reports/latest', {
    params: { periodType },
  })

export const analyticsReportPage = (params: AnalyticsReportPageQuery) =>
  request.get<any, ApiResponse<PageResult<AnalyticsReportVO>>>('/api/system/analytics/reports/page', {
    params,
  })

export const generateAnalyticsReport = (data: AnalyticsGenerateRequest) =>
  request.post<any, ApiResponse<AnalyticsReportVO>>('/api/system/analytics/reports/generate', data)
