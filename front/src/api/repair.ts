/**
 * 报修 API
 * 对应后端 RepairOrderController: /api/repairs
 */
import request from '@/utils/request'
import type { ApiResponse, PageResult } from '@/types/common'
import type { RepairOrderVO, RepairPageQuery, RepairCreateRequest } from '@/types/repair'

/** 创建报修 */
export const createRepair = (data: RepairCreateRequest) =>
  request.post<any, ApiResponse<void>>('/api/repairs', data)

/** 我的报修列表 */
export const myRepairPage = (params: RepairPageQuery) =>
  request.get<any, ApiResponse<PageResult<RepairOrderVO>>>('/api/repairs/my', { params })

/** 我的报修详情 */
export const myRepairDetail = (id: number) =>
  request.get<any, ApiResponse<RepairOrderVO>>(`/api/repairs/my/${id}`)

/** 物业工单列表 */
export const manageRepairPage = (params: RepairPageQuery) =>
  request.get<any, ApiResponse<PageResult<RepairOrderVO>>>('/api/repairs/manage', { params })

/** 工单流转日志 */
export const repairLogs = (id: number) =>
  request.get<any, ApiResponse<unknown>>(`/api/repairs/${id}/logs`)
