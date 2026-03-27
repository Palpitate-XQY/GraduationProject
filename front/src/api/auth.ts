/**
 * Auth APIs.
 */
import request from '@/utils/request'
import type { ApiResponse } from '@/types/common'
import type {
  CurrentUserVO,
  LoginRequest,
  LoginVO,
  ResidentRegisterComplexOptionVO,
  ResidentRegisterRequest,
  UpdatePasswordRequest,
} from '@/types/auth'

export const login = (data: LoginRequest) =>
  request.post<any, ApiResponse<LoginVO>>('/api/auth/login', data)

export const registerResident = (data: ResidentRegisterRequest) =>
  request.post<any, ApiResponse<void>>('/api/auth/resident/register', data)

export const residentRegisterOptions = () =>
  request.get<any, ApiResponse<ResidentRegisterComplexOptionVO[]>>('/api/auth/resident/register/options')

export const getMe = () =>
  request.get<any, ApiResponse<CurrentUserVO>>('/api/auth/me')

export const logout = () =>
  request.post<any, ApiResponse<void>>('/api/auth/logout')

export const updatePassword = (data: UpdatePasswordRequest) =>
  request.put<any, ApiResponse<void>>('/api/auth/password', data)
