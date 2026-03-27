/**
 * Audit log APIs.
 */
import request from '@/utils/request'
import type { ApiResponse, PageResult } from '@/types/common'
import type { LoginLogPageQuery, LoginLogVO, OperationLogPageQuery, OperationLogVO } from '@/types/log'

export const loginLogPage = (params: LoginLogPageQuery) =>
  request.get<any, ApiResponse<PageResult<LoginLogVO>>>('/api/logs/logins', { params })

export const operationLogPage = (params: OperationLogPageQuery) =>
  request.get<any, ApiResponse<PageResult<OperationLogVO>>>('/api/logs/operations', { params })
