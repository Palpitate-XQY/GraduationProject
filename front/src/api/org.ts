/**
 * Org admin APIs.
 */
import request from '@/utils/request'
import type { ApiResponse } from '@/types/common'
import type {
  ComplexPropertyRelCreateRequest,
  ComplexPropertyRelVO,
  OrgCreateRequest,
  OrgQuery,
  OrgTreeVO,
  OrgUpdateRequest,
} from '@/types/org'

export const orgTree = (params: OrgQuery) =>
  request.get<any, ApiResponse<OrgTreeVO[]>>('/api/org/tree', { params })

export const createOrg = (data: OrgCreateRequest) =>
  request.post<any, ApiResponse<void>>('/api/org', data)

export const updateOrg = (data: OrgUpdateRequest) =>
  request.put<any, ApiResponse<void>>('/api/org', data)

export const deleteOrg = (id: number) =>
  request.delete<any, ApiResponse<void>>(`/api/org/${id}`)

export const listComplexPropertyRel = (complexOrgId: number) =>
  request.get<any, ApiResponse<ComplexPropertyRelVO[]>>('/api/org/complex-property-rel', { params: { complexOrgId } })

export const createComplexPropertyRel = (data: ComplexPropertyRelCreateRequest) =>
  request.post<any, ApiResponse<void>>('/api/org/complex-property-rel', data)

export const deleteComplexPropertyRel = (id: number) =>
  request.delete<any, ApiResponse<void>>(`/api/org/complex-property-rel/${id}`)
