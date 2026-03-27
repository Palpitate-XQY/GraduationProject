/**
 * Activity types.
 */
import type { FileAttachmentVO } from './notice'

export type ActivityStatus = 'DRAFT' | 'PUBLISHED' | 'RECALLED' | 'FINISHED' | string
export type ActivitySignupStatus = 'SIGNED' | 'CANCELED' | string

export interface ActivityScopeItem {
  scopeType: string
  scopeRefId: number
}

export interface ActivityVO {
  id: number
  title: string
  content: string
  contentHtml: string
  coverFileId: number | null
  attachmentJson: string | null
  attachments: FileAttachmentVO[]
  activityStartTime: string
  activityEndTime: string
  signupStartTime: string
  signupEndTime: string
  location: string
  maxParticipants: number
  status: ActivityStatus
  publisherOrgId: number
  publishTime: string | null
  createTime: string
  signedByMe?: boolean
  signupStatus?: ActivitySignupStatus | null
  mySignupTime?: string | null
  scopeItems: ActivityScopeItem[]
}

export interface ActivityPageQuery {
  current?: number
  size?: number
  keyword?: string
  status?: string
}

export interface ActivityCreateRequest {
  title: string
  content: string
  coverFileId?: number | null
  attachmentJson?: string | null
  activityStartTime: string
  activityEndTime: string
  signupStartTime: string
  signupEndTime: string
  location: string
  maxParticipants: number
  scopeItems: ActivityScopeItem[]
}

export interface ActivityUpdateRequest extends ActivityCreateRequest {
  id: number
}

export interface ActivitySignupVO {
  id: number
  activityId: number
  userId: number
  signupStatus: ActivitySignupStatus
  signupTime?: string | null
  cancelTime?: string | null
}

export interface ActivityStatsVO {
  activityId: number
  signupCount: number
  maxParticipants: number
  remainingSlots: number
}
