/**
 * Activity APIs.
 */
import request from '@/utils/request'
import type { ApiResponse, PageResult } from '@/types/common'
import type {
  ActivityCreateRequest,
  ActivityPageQuery,
  ActivitySignupVO,
  ActivityStatsVO,
  ActivityUpdateRequest,
  ActivityVO,
} from '@/types/activity'

export const residentActivityPage = (params: ActivityPageQuery) =>
  request.get<any, ApiResponse<PageResult<ActivityVO>>>('/api/activities/resident/page', { params })

export const residentActivityDetail = (id: number) =>
  request.get<any, ApiResponse<ActivityVO>>(`/api/activities/resident/${id}`)

export const myActivityPage = (params: ActivityPageQuery) =>
  request.get<any, ApiResponse<PageResult<ActivityVO>>>('/api/activities/my/page', { params })

export const signupActivity = (id: number) =>
  request.post<any, ApiResponse<void>>(`/api/activities/${id}/signup`)

export const cancelSignup = (id: number) =>
  request.post<any, ApiResponse<void>>(`/api/activities/${id}/cancel-signup`)

export const manageActivityPage = (params: ActivityPageQuery) =>
  request.get<any, ApiResponse<PageResult<ActivityVO>>>('/api/activities', { params })

export const manageActivityDetail = (id: number) =>
  request.get<any, ApiResponse<ActivityVO>>(`/api/activities/${id}`)

export const createActivity = (data: ActivityCreateRequest) =>
  request.post<any, ApiResponse<void>>('/api/activities', data)

export const updateActivity = (data: ActivityUpdateRequest) =>
  request.put<any, ApiResponse<void>>('/api/activities', data)

export const deleteActivity = (id: number) =>
  request.delete<any, ApiResponse<void>>(`/api/activities/${id}`)

export const publishActivity = (id: number) =>
  request.post<any, ApiResponse<void>>(`/api/activities/${id}/publish`)

export const recallActivity = (id: number) =>
  request.post<any, ApiResponse<void>>(`/api/activities/${id}/recall`)

export const activitySignupList = (id: number) =>
  request.get<any, ApiResponse<ActivitySignupVO[]>>(`/api/activities/${id}/signups`)

export const activityStats = (id: number) =>
  request.get<any, ApiResponse<ActivityStatsVO>>(`/api/activities/${id}/stats`)
