/**
 * 认证 API
 * 对应后端 AuthController: /api/auth
 */
import request from '@/utils/request'
import type { ApiResponse } from '@/types/common'
import type { LoginRequest, LoginVO, CurrentUserVO, UpdatePasswordRequest } from '@/types/auth'

/** 登录 */
export const login = (data: LoginRequest) =>
  request.post<any, ApiResponse<LoginVO>>('/api/auth/login', data)

/** 获取当前用户 */
export const getMe = () =>
  request.get<any, ApiResponse<CurrentUserVO>>('/api/auth/me')

/** 退出登录 */
export const logout = () =>
  request.post<any, ApiResponse<void>>('/api/auth/logout')

/** 修改密码 */
export const updatePassword = (data: UpdatePasswordRequest) =>
  request.put<any, ApiResponse<void>>('/api/auth/password', data)
