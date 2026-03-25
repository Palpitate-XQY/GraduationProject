/**
 * 活动类型
 * 对应后端 activity 模块
 */
export interface ActivityVO {
  id: number
  title: string
  content: string
  contentHtml: string
  coverFileId: number | null
  attachmentJson: string | null
  attachments: import('./notice').FileAttachmentVO[]
  activityStartTime: string
  activityEndTime: string
  signupStartTime: string
  signupEndTime: string
  location: string
  maxParticipants: number
  status: string
  publisherOrgId: number
  publishTime: string | null
  createTime: string
  scopeItems: ActivityScopeItem[]
}

export interface ActivityScopeItem {
  scopeType: string
  scopeRefId: number
}

export interface ActivityPageQuery {
  current?: number
  size?: number
  keyword?: string
  status?: string
}
