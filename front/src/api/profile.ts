/**
 * 居民档案 API
 * 对应后端 ResidentProfileController: /api/resident/profiles
 */
import request from '@/utils/request'
import type { ApiResponse } from '@/types/common'
import type {
  ResidentProfileAdminUpdateRequest,
  ResidentProfileMyUpdateRequest,
  ResidentProfileVO,
} from '@/types/resident'

export type { ResidentProfileVO, ResidentProfileMyUpdateRequest, ResidentProfileAdminUpdateRequest }

/** 获取当前登录居民档案 */
export const getMyProfile = () =>
  request.get<any, ApiResponse<ResidentProfileVO>>('/api/resident/profiles/me')

/** 更新当前登录居民档案 */
export const updateMyProfile = (data: ResidentProfileMyUpdateRequest) =>
  request.put<any, ApiResponse<void>>('/api/resident/profiles/me', data)

/** 鑾峰彇鎸囧畾灞呮皯妗ｆ */
export const getResidentProfileByUserId = (userId: number) =>
  request.get<any, ApiResponse<ResidentProfileVO>>(`/api/resident/profiles/${userId}`)

/** 绠＄悊鍛樻柊澧炴垨鏇存柊灞呮皯妗ｆ */
export const upsertResidentProfileByAdmin = (data: ResidentProfileAdminUpdateRequest) =>
  request.put<any, ApiResponse<void>>('/api/resident/profiles', data)
