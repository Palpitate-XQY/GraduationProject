/**
 * System module types for admin pages.
 */
export interface UserVO {
  id: number
  username: string
  nickname: string
  phone?: string | null
  email?: string | null
  status: number
  orgId: number
  orgName?: string | null
  roleIds: number[]
  roleCodes: string[]
  createTime: string
}

export interface UserPageQuery {
  current?: number
  size?: number
  keyword?: string
  status?: number
  orgId?: number
}

export interface UserCreateRequest {
  username: string
  password: string
  nickname: string
  phone?: string
  email?: string
  orgId: number
  status: number
  roleIds: number[]
}

export interface UserUpdateRequest {
  id: number
  nickname: string
  phone?: string
  email?: string
  orgId: number
  status: number
  roleIds: number[]
}

export interface UserRoleAssignRequest {
  userId: number
  roleIds: number[]
}

export interface RoleScopeConfigItem {
  scopeType: string
  scopeRefId?: number | null
}

export interface RoleVO {
  id: number
  roleCode: string
  roleName: string
  status: number
  parentRoleId?: number | null
  allowCreateChild: number
  sort: number
  remark?: string | null
  permissionIds: number[]
  scopeConfigs: RoleScopeConfigItem[]
  createTime: string
}

export interface RolePageQuery {
  current?: number
  size?: number
  keyword?: string
}

export interface RoleCreateRequest {
  roleCode: string
  roleName: string
  parentRoleId?: number | null
  allowCreateChild: number
  status: number
  sort: number
  remark?: string
  permissionIds: number[]
  scopeConfigs: RoleScopeConfigItem[]
}

export interface RoleUpdateRequest {
  id: number
  roleName: string
  allowCreateChild: number
  status: number
  sort: number
  remark?: string
  permissionIds: number[]
  scopeConfigs: RoleScopeConfigItem[]
}

export interface PermissionVO {
  id: number
  permissionCode: string
  permissionName: string
  permissionType: string
  parentId?: number | null
  path?: string | null
  method?: string | null
  sort: number
  createTime: string
}

export interface PermissionPageQuery {
  current?: number
  size?: number
  keyword?: string
}

export interface PermissionCreateRequest {
  permissionCode: string
  permissionName: string
  permissionType: string
  parentId?: number | null
  path?: string
  method?: string
  sort: number
}

export interface PermissionUpdateRequest {
  id: number
  permissionName: string
  permissionType: string
  parentId?: number | null
  path?: string
  method?: string
  sort: number
}
