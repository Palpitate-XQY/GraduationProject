/**
 * File module types.
 */
export interface FileInfoVO {
  id: number
  bizType?: string | null
  bizId?: number | null
  storageType: string
  fileName: string
  originFileName: string
  fileSize: number
  contentType: string
  accessUrl: string
  uploaderId: number
  createTime: string
}
