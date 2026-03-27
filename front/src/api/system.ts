/**
 * System admin APIs: user/role/permission.
 */
import request from '@/utils/request'
import type { ApiResponse, PageResult } from '@/types/common'
import type {
  PermissionCreateRequest,
  PermissionPageQuery,
  PermissionUpdateRequest,
  PermissionVO,
  RoleCreateRequest,
  RolePageQuery,
  RoleUpdateRequest,
  RoleVO,
  UserCreateRequest,
  UserPageQuery,
  UserRoleAssignRequest,
  UserUpdateRequest,
  UserVO,
} from '@/types/system'

export const userPage = (params: UserPageQuery) =>
  request.get<any, ApiResponse<PageResult<UserVO>>>('/api/system/users', { params })

export const userDetail = (id: number) =>
  request.get<any, ApiResponse<UserVO>>(`/api/system/users/${id}`)

export const createUser = (data: UserCreateRequest) =>
  request.post<any, ApiResponse<void>>('/api/system/users', data)

export const updateUser = (data: UserUpdateRequest) =>
  request.put<any, ApiResponse<void>>('/api/system/users', data)

export const deleteUser = (id: number) =>
  request.delete<any, ApiResponse<void>>(`/api/system/users/${id}`)

export const updateUserStatus = (id: number, status: number) =>
  request.put<any, ApiResponse<void>>(`/api/system/users/${id}/status`, null, { params: { status } })

export const assignUserRoles = (data: UserRoleAssignRequest) =>
  request.put<any, ApiResponse<void>>('/api/system/users/roles', data)

export const rolePage = (params: RolePageQuery) =>
  request.get<any, ApiResponse<PageResult<RoleVO>>>('/api/system/roles', { params })

export const roleDetail = (id: number) =>
  request.get<any, ApiResponse<RoleVO>>(`/api/system/roles/${id}`)

export const createRole = (data: RoleCreateRequest) =>
  request.post<any, ApiResponse<void>>('/api/system/roles', data)

export const updateRole = (data: RoleUpdateRequest) =>
  request.put<any, ApiResponse<void>>('/api/system/roles', data)

export const deleteRole = (id: number) =>
  request.delete<any, ApiResponse<void>>(`/api/system/roles/${id}`)

export const permissionPage = (params: PermissionPageQuery) =>
  request.get<any, ApiResponse<PageResult<PermissionVO>>>('/api/system/permissions', { params })

export const permissionDetail = (id: number) =>
  request.get<any, ApiResponse<PermissionVO>>(`/api/system/permissions/${id}`)

export const createPermission = (data: PermissionCreateRequest) =>
  request.post<any, ApiResponse<void>>('/api/system/permissions', data)

export const updatePermission = (data: PermissionUpdateRequest) =>
  request.put<any, ApiResponse<void>>('/api/system/permissions', data)

export const deletePermission = (id: number) =>
  request.delete<any, ApiResponse<void>>(`/api/system/permissions/${id}`)
