/**
 * Notice types.
 */
export type NoticeType = 'STREET_COMMUNITY' | 'PROPERTY'
export type NoticeStatus = 'DRAFT' | 'PUBLISHED' | 'RECALLED' | string

export interface NoticeScopeItem {
  scopeType: string
  scopeRefId: number
}

export interface FileAttachmentVO {
  fileId: number
  originFileName: string
  fileName: string
  fileSize: number
  contentType: string
  storageType: string
  accessUrl: string
}

export interface NoticeVO {
  id: number
  noticeType: NoticeType | string
  title: string
  content: string
  contentHtml: string
  coverFileId: number | null
  attachmentJson: string | null
  attachments: FileAttachmentVO[]
  status: NoticeStatus
  topFlag: number
  publisherOrgId: number
  publishTime: string | null
  createTime: string
  scopeItems: NoticeScopeItem[]
}

export interface NoticePageQuery {
  current?: number
  size?: number
  keyword?: string
  noticeType?: string
  status?: string
}

export interface NoticeCreateRequest {
  noticeType: NoticeType | string
  title: string
  content: string
  coverFileId?: number | null
  attachmentJson?: string | null
  topFlag: number
  scopeItems: NoticeScopeItem[]
}

export interface NoticeUpdateRequest extends NoticeCreateRequest {
  id: number
}
