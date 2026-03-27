/**
 * Storage config APIs.
 */
import request from '@/utils/request'
import type { ApiResponse } from '@/types/common'
import type { StorageConfigUpdateRequest, StorageConfigVO } from '@/types/storage'

export const getStorageConfig = () =>
  request.get<any, ApiResponse<StorageConfigVO>>('/api/system/storage-config')

export const updateStorageConfig = (data: StorageConfigUpdateRequest) =>
  request.put<any, ApiResponse<void>>('/api/system/storage-config', data)
