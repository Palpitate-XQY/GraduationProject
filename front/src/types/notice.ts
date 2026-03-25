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

export interface NoticePageQuery {
  current?: number
  size?: number
  keyword?: string
  noticeType?: string
  status?: string
}
