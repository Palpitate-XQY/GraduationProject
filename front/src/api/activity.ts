/**
 * 活动 API
 * 对应后端 ActivityController: /api/activities
 */
import request from '@/utils/request'
import type { ApiResponse, PageResult } from '@/types/common'
import type { ActivityVO, ActivityPageQuery } from '@/types/activity'

/** 居民端活动分页 */
export const residentActivityPage = (params: ActivityPageQuery) =>
  request.get<any, ApiResponse<PageResult<ActivityVO>>>('/api/activities/resident/page', { params })

/** 居民端活动详情 */
export const residentActivityDetail = (id: number) =>
  request.get<any, ApiResponse<ActivityVO>>(`/api/activities/resident/${id}`)

/** 居民报名活动 */
export const signupActivity = (id: number) =>
  request.post<any, ApiResponse<void>>(`/api/activities/${id}/signup`)

/** 取消报名 */
export const cancelSignup = (id: number) =>
  request.post<any, ApiResponse<void>>(`/api/activities/${id}/cancel-signup`)
