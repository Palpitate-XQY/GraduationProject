/**
 * Repair types.
 */
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

export const REPAIR_STATUS_MAP: Record<RepairStatus, string> = {
  PENDING_ACCEPT: '待受理',
  ACCEPTED: '已受理',
  ASSIGNED: '已转派',
  PROCESSING: '处理中',
  WAIT_CONFIRM: '待确认',
  DONE: '已完成',
  CLOSED: '已关闭',
  REJECTED: '已驳回',
  REOPENED: '重新处理',
}

export interface RepairAttachmentVO {
  id: number
  fileId: number
  attachmentType: string
  createTime: string
}

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

export interface RepairOrderLogVO {
  id: number
  repairOrderId: number
  fromStatus?: string | null
  toStatus?: string | null
  operationType: string
  operatorUserId?: number | null
  operationRemark?: string | null
  operationTime: string
}

export interface RepairPageQuery {
  current?: number
  size?: number
  keyword?: string
  status?: string
  complexOrgId?: number
  propertyCompanyOrgId?: number
  maintainerUserId?: number
  residentUserId?: number
  createStartTime?: string
  createEndTime?: string
}

export interface RepairCreateRequest {
  title: string
  description: string
  contactPhone: string
  repairAddress: string
  emergencyLevel: string
  expectHandleTime?: string
  complexOrgId: number
  propertyCompanyOrgId?: number
  attachmentFileIds?: number[]
}

export interface RepairAcceptRequest {
  remark?: string
}

export interface RepairRejectRequest {
  reason: string
}

export interface RepairAssignRequest {
  maintainerUserId: number
  remark?: string
}

export interface RepairTakeRequest {
  remark?: string
}

export interface RepairProcessRequest {
  processDesc: string
  attachmentFileIds?: number[]
}

export interface RepairSubmitRequest {
  finishDesc: string
  attachmentFileIds?: number[]
}

export interface RepairConfirmRequest {
  remark?: string
}

export interface RepairCloseRequest {
  remark?: string
}

export interface RepairReopenRequest {
  feedback: string
  attachmentFileIds?: number[]
}

export interface RepairEvaluateRequest {
  score: number
  content?: string
}

export interface RepairUrgeRequest {
  content: string
}
