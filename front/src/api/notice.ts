/**
 * 公告 API
 * 对应后端 NoticeController: /api/notices
 */
import request from '@/utils/request'
import type { ApiResponse, PageResult } from '@/types/common'
import type { NoticeVO, NoticePageQuery } from '@/types/notice'

/** 居民端公告分页 */
export const residentNoticePage = (params: NoticePageQuery) =>
  request.get<any, ApiResponse<PageResult<NoticeVO>>>('/api/notices/resident/page', { params })

/** 居民端公告详情 */
export const residentNoticeDetail = (id: number) =>
  request.get<any, ApiResponse<NoticeVO>>(`/api/notices/resident/${id}`)

/** 管理端公告分页 */
export const manageNoticePage = (params: NoticePageQuery) =>
  request.get<any, ApiResponse<PageResult<NoticeVO>>>('/api/notices', { params })

/** 管理端公告详情 */
export const manageNoticeDetail = (id: number) =>
  request.get<any, ApiResponse<NoticeVO>>(`/api/notices/${id}`)
