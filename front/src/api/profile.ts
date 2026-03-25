/**
 * 居民档案 API
 * 对应后端 ResidentProfileController: /api/resident/profiles
 */
import request from '@/utils/request'
import type { ApiResponse } from '@/types/common'

export interface ResidentProfileVO {
  id: number
  userId: number
  communityOrgId: number
  communityOrgName: string
  complexOrgId: number
  complexOrgName: string
  roomNo: string
  emergencyContact: string
  emergencyPhone: string
  updateTime: string
}

export interface ResidentProfileMyUpdateRequest {
  roomNo?: string
  emergencyContact?: string
  emergencyPhone?: string
}

/** 获取当前登录居民档案 */
export const getMyProfile = () =>
  request.get<any, ApiResponse<ResidentProfileVO>>('/api/resident/profiles/me')

/** 更新当前登录居民档案 */
export const updateMyProfile = (data: ResidentProfileMyUpdateRequest) =>
  request.put<any, ApiResponse<void>>('/api/resident/profiles/me', data)
