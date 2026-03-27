/**
 * Authentication related types.
 */

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginVO {
  accessToken: string
  tokenType: string
  expiresIn: number
  mustChangePassword: number
}

export interface CurrentUserVO {
  userId: number
  username: string
  nickname: string
  orgId: number
  communityOrgName?: string | null
  roleCodes: string[]
  permissionCodes: string[]
}

export interface ResidentRegisterRequest {
  username: string
  password: string
  nickname: string
  phone: string
  email?: string
  complexOrgId: number
  roomNo: string
  emergencyContact?: string
  emergencyPhone?: string
}

export interface ResidentRegisterComplexOptionVO {
  complexOrgId: number
  complexOrgName: string
  communityOrgId?: number | null
  communityOrgName?: string | null
}

export interface UpdatePasswordRequest {
  oldPassword: string
  newPassword: string
}
