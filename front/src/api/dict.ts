/**
 * Dict APIs.
 */
import request from '@/utils/request'
import type { ApiResponse, PageResult } from '@/types/common'
import type {
  DictDataCreateRequest,
  DictDataPageQuery,
  DictDataUpdateRequest,
  DictDataVO,
  DictOptionVO,
  DictTypeCreateRequest,
  DictTypePageQuery,
  DictTypeUpdateRequest,
  DictTypeVO,
} from '@/types/dict'

export const dictTypePage = (params: DictTypePageQuery) =>
  request.get<any, ApiResponse<PageResult<DictTypeVO>>>('/api/system/dicts/types', { params })

export const dictTypeDetail = (id: number) =>
  request.get<any, ApiResponse<DictTypeVO>>(`/api/system/dicts/types/${id}`)

export const createDictType = (data: DictTypeCreateRequest) =>
  request.post<any, ApiResponse<void>>('/api/system/dicts/types', data)

export const updateDictType = (data: DictTypeUpdateRequest) =>
  request.put<any, ApiResponse<void>>('/api/system/dicts/types', data)

export const deleteDictType = (id: number) =>
  request.delete<any, ApiResponse<void>>(`/api/system/dicts/types/${id}`)

export const dictDataPage = (params: DictDataPageQuery) =>
  request.get<any, ApiResponse<PageResult<DictDataVO>>>('/api/system/dicts/data', { params })

export const dictDataDetail = (id: number) =>
  request.get<any, ApiResponse<DictDataVO>>(`/api/system/dicts/data/${id}`)

export const createDictData = (data: DictDataCreateRequest) =>
  request.post<any, ApiResponse<void>>('/api/system/dicts/data', data)

export const updateDictData = (data: DictDataUpdateRequest) =>
  request.put<any, ApiResponse<void>>('/api/system/dicts/data', data)

export const deleteDictData = (id: number) =>
  request.delete<any, ApiResponse<void>>(`/api/system/dicts/data/${id}`)

export const dictOptions = (dictCode: string) =>
  request.get<any, ApiResponse<DictOptionVO[]>>(`/api/system/dicts/options/${dictCode}`)
