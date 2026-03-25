/**
 * 统一 API 响应结构
 * 对应后端 xxqqyyy.community.common.api.ApiResponse
 */
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
  traceId?: string
}

/**
 * 分页响应结构
 * 对应后端 xxqqyyy.community.common.api.PageResult
 */
export interface PageResult<T = unknown> {
  records: T[]
  total: number
  current: number
  size: number
}

/**
 * 通用分页查询参数
 */
export interface PageQuery {
  current?: number
  size?: number
  keyword?: string
}
