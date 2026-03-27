/**
 * Repair APIs.
 */
import request from '@/utils/request'
import type { ApiResponse, PageResult } from '@/types/common'
import type {
  RepairAcceptRequest,
  RepairAssignRequest,
  RepairCloseRequest,
  RepairConfirmRequest,
  RepairCreateRequest,
  RepairEvaluateRequest,
  RepairOrderLogVO,
  RepairOrderVO,
  RepairPageQuery,
  RepairProcessRequest,
  RepairRejectRequest,
  RepairReopenRequest,
  RepairSubmitRequest,
  RepairTakeRequest,
  RepairUrgeRequest,
} from '@/types/repair'

export const createRepair = (data: RepairCreateRequest) =>
  request.post<any, ApiResponse<void>>('/api/repairs', data)

export const myRepairPage = (params: RepairPageQuery) =>
  request.get<any, ApiResponse<PageResult<RepairOrderVO>>>('/api/repairs/my', { params })

export const myRepairDetail = (id: number) =>
  request.get<any, ApiResponse<RepairOrderVO>>(`/api/repairs/my/${id}`)

export const manageRepairPage = (params: RepairPageQuery) =>
  request.get<any, ApiResponse<PageResult<RepairOrderVO>>>('/api/repairs/manage', { params })

export const manageRepairDetail = (id: number) =>
  request.get<any, ApiResponse<RepairOrderVO>>(`/api/repairs/${id}`)

export const repairLogs = (id: number) =>
  request.get<any, ApiResponse<RepairOrderLogVO[]>>(`/api/repairs/${id}/logs`)

export const acceptRepair = (id: number, data?: RepairAcceptRequest) =>
  request.post<any, ApiResponse<void>>(`/api/repairs/${id}/accept`, data || {})

export const rejectRepair = (id: number, data: RepairRejectRequest) =>
  request.post<any, ApiResponse<void>>(`/api/repairs/${id}/reject`, data)

export const assignRepair = (id: number, data: RepairAssignRequest) =>
  request.post<any, ApiResponse<void>>(`/api/repairs/${id}/assign`, data)

export const takeRepair = (id: number, data?: RepairTakeRequest) =>
  request.post<any, ApiResponse<void>>(`/api/repairs/${id}/take`, data || {})

export const processRepair = (id: number, data: RepairProcessRequest) =>
  request.post<any, ApiResponse<void>>(`/api/repairs/${id}/process`, data)

export const submitRepair = (id: number, data: RepairSubmitRequest) =>
  request.post<any, ApiResponse<void>>(`/api/repairs/${id}/submit`, data)

export const confirmRepair = (id: number, data?: RepairConfirmRequest) =>
  request.post<any, ApiResponse<void>>(`/api/repairs/${id}/confirm`, data || {})

export const reopenRepair = (id: number, data: RepairReopenRequest) =>
  request.post<any, ApiResponse<void>>(`/api/repairs/${id}/reopen`, data)

export const evaluateRepair = (id: number, data: RepairEvaluateRequest) =>
  request.post<any, ApiResponse<void>>(`/api/repairs/${id}/evaluate`, data)

export const urgeRepair = (id: number, data: RepairUrgeRequest) =>
  request.post<any, ApiResponse<void>>(`/api/repairs/${id}/urge`, data)

export const closeRepair = (id: number, data?: RepairCloseRequest) =>
  request.post<any, ApiResponse<void>>(`/api/repairs/${id}/close`, data || {})
