/**
 * 公告类型
 * 对应后端 notice 模块
 */
export interface NoticeVO {
  id: number
  noticeType: string
  title: string
  content: string
  contentHtml: string
  coverFileId: number | null
  attachmentJson: string | null
  attachments: FileAttachmentVO[]
  status: string
  topFlag: number
  publisherOrgId: number
  publishTime: string | null
  createTime: string
  scopeItems: NoticeScopeItem[]
}

export interface NoticeScopeItem {
  orgId: number
  orgName?: string
}

export interface FileAttachmentVO {
  fileId: number
  fileName: string
  fileUrl: string
  fileSize: number
}

export interface NoticePageQuery {
  current?: number
  size?: number
  keyword?: string
  noticeType?: string
  status?: string
}
