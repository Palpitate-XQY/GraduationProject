/**
 * Storage config types.
 */
export interface StorageConfigVO {
  storageType: string
  localBasePath?: string | null
  localAccessBaseUrl?: string | null
  qiniuAccessKey?: string | null
  qiniuSecretKeyMasked?: string | null
  qiniuBucket?: string | null
  qiniuRegion?: string | null
  qiniuDomain?: string | null
  remark?: string | null
}

export interface StorageConfigUpdateRequest {
  storageType: string
  localBasePath?: string
  localAccessBaseUrl?: string
  qiniuAccessKey?: string
  qiniuSecretKey?: string
  qiniuBucket?: string
  qiniuRegion?: string
  qiniuDomain?: string
  remark?: string
}
