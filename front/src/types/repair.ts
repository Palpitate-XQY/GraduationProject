/**
 * 报修类型
 * 对应后端 repair 模块
 */

/** 报修工单状态枚举 */
export type RepairStatus =
  | 'PENDING_ACCEPT'
  | 'ACCEPTED'
  | 'ASSIGNED'
  | 'PROCESSING'
  | 'WAIT_CONFIRM'
  | 'DONE'
  | 'CLOSED'
  | 'REJECTED'
  | 'REOPENED'

/** 报修状态中文映射 */
export const REPAIR_STATUS_MAP: Record<RepairStatus, string> = {
  PENDING_ACCEPT: '待受理',
  ACCEPTED: '已受理',
  ASSIGNED: '已分派',
  PROCESSING: '处理中',
  WAIT_CONFIRM: '待确认',
  DONE: '已完成',
  CLOSED: '已关闭',
  REJECTED: '已驳回',
  REOPENED: '重新处理',
}

/** 报修工单 VO */
export interface RepairOrderVO {
  id: number
  orderNo: string
  title: string
  description: string
  contactPhone: string
  repairAddress: string
  emergencyLevel: string
  expectHandleTime: string | null
  status: RepairStatus
  communityOrgId: number
  complexOrgId: number
  propertyCompanyOrgId: number | null
  residentUserId: number
  acceptUserId: number | null
  assignUserId: number | null
  maintainerUserId: number | null
  processDesc: string | null
  finishDesc: string | null
  rejectReason: string | null
  residentFeedback: string | null
  evaluateScore: number | null
  evaluateContent: string | null
  urgeCount: number
  acceptedTime: string | null
  assignedTime: string | null
  processingTime: string | null
  finishTime: string | null
  confirmTime: string | null
  closedTime: string | null
  lastUrgeTime: string | null
  createTime: string
  attachments: RepairAttachmentVO[]
}

export interface RepairAttachmentVO {
  id: number
  fileId: number
  fileName: string
  fileUrl: string
}

/** 报修查询 */
export interface RepairPageQuery {
  current?: number
  size?: number
  keyword?: string
  status?: string
}

/** 创建报修请求 */
export interface RepairCreateRequest {
  title: string
  description: string
  contactPhone: string
  repairAddress: string
  emergencyLevel: string
  expectHandleTime?: string
  attachmentFileIds?: number[]
}
