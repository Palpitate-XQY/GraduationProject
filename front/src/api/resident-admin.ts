/**
 * Resident profile admin APIs.
 */
import request from '@/utils/request'
import type { ApiResponse } from '@/types/common'
import type { ResidentProfileAdminUpdateRequest, ResidentProfileVO } from '@/types/resident'

export const getResidentProfileDetail = (userId: number) =>
  request.get<any, ApiResponse<ResidentProfileVO>>(`/api/resident/profiles/${userId}`)

export const saveResidentProfileByAdmin = (data: ResidentProfileAdminUpdateRequest) =>
  request.put<any, ApiResponse<void>>('/api/resident/profiles', data)
