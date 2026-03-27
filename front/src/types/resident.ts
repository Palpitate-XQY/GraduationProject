/**
 * Resident profile types for admin/resident views.
 */
export interface ResidentProfileVO {
  id: number
  userId: number
  communityOrgId: number
  communityOrgName?: string | null
  complexOrgId: number
  complexOrgName?: string | null
  roomNo?: string | null
  emergencyContact?: string | null
  emergencyPhone?: string | null
  updateTime: string
}

export interface ResidentProfileMyUpdateRequest {
  roomNo?: string
  emergencyContact?: string
  emergencyPhone?: string
}

export interface ResidentProfileAdminUpdateRequest {
  userId: number
  complexOrgId: number
  roomNo?: string
  emergencyContact?: string
  emergencyPhone?: string
}
