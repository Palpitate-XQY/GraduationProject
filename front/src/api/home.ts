/**
 * 首页看板 API
 * 对应后端 DashboardController: /api/system/dashboard
 */
import request from '@/utils/request'
import type { ApiResponse } from '@/types/common'

export interface DashboardCardVO {
  code: string
  title: string
  value: number
  description: string
}

export interface DashboardOverviewVO {
  dashboardCode: string
  pagePackCode: string
  defaultHomeRoute: string
  roleCodes: string[]
  permissionCodes: string[]
  cards: DashboardCardVO[]
}

/** 获取看板总览 */
export const getDashboardOverview = () =>
  request.get<any, ApiResponse<DashboardOverviewVO>>('/api/system/dashboard/overview')
