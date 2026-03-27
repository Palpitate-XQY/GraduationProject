/**
 * Org module types.
 */
export interface OrgTreeVO {
  id: number
  parentId?: number | null
  orgCode: string
  orgName: string
  orgType: string
  status: number
  sort: number
  children: OrgTreeVO[]
}

export interface OrgQuery {
  keyword?: string
  orgType?: string
  status?: number
}

export interface OrgCreateRequest {
  parentId?: number | null
  orgCode: string
  orgName: string
  orgType: string
  status: number
  sort: number
}

export interface OrgUpdateRequest {
  id: number
  parentId?: number | null
  orgName: string
  orgType: string
  status: number
  sort: number
}

export interface ComplexPropertyRelVO {
  id: number
  complexOrgId: number
  propertyCompanyOrgId: number
  status: number
}

export interface ComplexPropertyRelCreateRequest {
  complexOrgId: number
  propertyCompanyOrgId: number
}
