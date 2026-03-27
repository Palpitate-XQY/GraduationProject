/**
 * Dict module types.
 */
export interface DictTypeVO {
  id: number
  dictCode: string
  dictName: string
  status: number
  createTime: string
}

export interface DictDataVO {
  id: number
  dictTypeCode: string
  dictLabel: string
  dictValue: string
  sort: number
  status: number
  createTime: string
}

export interface DictOptionVO {
  label: string
  value: string
  sort: number
}

export interface DictTypePageQuery {
  current?: number
  size?: number
  keyword?: string
  status?: number
}

export interface DictDataPageQuery {
  current?: number
  size?: number
  dictTypeCode?: string
  keyword?: string
  status?: number
}

export interface DictTypeCreateRequest {
  dictCode: string
  dictName: string
  status: number
}

export interface DictTypeUpdateRequest extends DictTypeCreateRequest {
  id: number
}

export interface DictDataCreateRequest {
  dictTypeCode: string
  dictLabel: string
  dictValue: string
  sort: number
  status: number
}

export interface DictDataUpdateRequest extends DictDataCreateRequest {
  id: number
}
