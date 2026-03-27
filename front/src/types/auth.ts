/**
 * 认证相关类型
 * 对应后端 auth 模块
 */

/** 登录请求 */
export interface LoginRequest {
  username: string
  password: string
}

/** 登录响应 */
export interface LoginVO {
  accessToken: string
  tokenType: string
  expiresIn: number
  mustChangePassword: number
}

/** 当前用户信息 */
export interface CurrentUserVO {
  userId: number
  username: string
  nickname: string
  orgId: number
  communityOrgName?: string | null
  roleCodes: string[]
  permissionCodes: string[]
}

/** 居民注册请求 */
export interface ResidentRegisterRequest {
  username: string
  password: string
  nickname: string
  phone?: string
}

/** 修改密码请求 */
export interface UpdatePasswordRequest {
  oldPassword: string
  newPassword: string
}
