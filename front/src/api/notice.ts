/**
 * Notice APIs.
 */
import request from '@/utils/request'
import type { ApiResponse, PageResult } from '@/types/common'
import type { NoticeCreateRequest, NoticePageQuery, NoticeUpdateRequest, NoticeVO } from '@/types/notice'

export const residentNoticePage = (params: NoticePageQuery) =>
  request.get<any, ApiResponse<PageResult<NoticeVO>>>('/api/notices/resident/page', { params })

export const residentNoticeDetail = (id: number) =>
  request.get<any, ApiResponse<NoticeVO>>(`/api/notices/resident/${id}`)

export const manageNoticePage = (params: NoticePageQuery) =>
  request.get<any, ApiResponse<PageResult<NoticeVO>>>('/api/notices', { params })

export const manageNoticeDetail = (id: number) =>
  request.get<any, ApiResponse<NoticeVO>>(`/api/notices/${id}`)

export const createNotice = (data: NoticeCreateRequest) =>
  request.post<any, ApiResponse<void>>('/api/notices', data)

export const updateNotice = (data: NoticeUpdateRequest) =>
  request.put<any, ApiResponse<void>>('/api/notices', data)

export const deleteNotice = (id: number) =>
  request.delete<any, ApiResponse<void>>(`/api/notices/${id}`)

export const publishNotice = (id: number) =>
  request.post<any, ApiResponse<void>>(`/api/notices/${id}/publish`)

export const recallNotice = (id: number) =>
  request.post<any, ApiResponse<void>>(`/api/notices/${id}/recall`)
