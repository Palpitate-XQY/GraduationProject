/**
 * Log module types.
 */
export interface LoginLogVO {
  id: number
  userId?: number | null
  username: string
  successFlag: number
  message?: string | null
  ip?: string | null
  userAgent?: string | null
  loginTime: string
}

export interface LoginLogPageQuery {
  current?: number
  size?: number
  username?: string
  successFlag?: number
  ip?: string
  startTime?: string
  endTime?: string
}

export interface OperationLogVO {
  id: number
  userId?: number | null
  username: string
  operationModule: string
  operationType: string
  requestUri?: string | null
  requestMethod?: string | null
  successFlag: number
  errorMessage?: string | null
  traceId?: string | null
  operationTime: string
}

export interface OperationLogPageQuery {
  current?: number
  size?: number
  username?: string
  operationModule?: string
  operationType?: string
  successFlag?: number
  traceId?: string
  startTime?: string
  endTime?: string
}
