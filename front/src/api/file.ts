/**
 * File APIs.
 */
import request from '@/utils/request'
import type { ApiResponse } from '@/types/common'
import type { FileInfoVO } from '@/types/file'

export interface UploadFileParams {
  file: File
  bizType?: string
  bizId?: number
}

export const uploadFile = async ({ file, bizType, bizId }: UploadFileParams) => {
  const form = new FormData()
  form.append('file', file)
  if (bizType) form.append('bizType', bizType)
  if (bizId !== undefined) form.append('bizId', String(bizId))
  return request.post<any, ApiResponse<FileInfoVO>>('/api/files/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export const getFileDetail = (id: number) =>
  request.get<any, ApiResponse<FileInfoVO>>(`/api/files/${id}`)
