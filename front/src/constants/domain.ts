/**
 * Domain constants aligned with backend enums.
 */
export const NOTICE_TYPE_OPTIONS = [
  { label: '街道/社区公告', value: 'STREET_COMMUNITY' },
  { label: '物业公告', value: 'PROPERTY' },
] as const

export const NOTICE_STATUS_OPTIONS = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '已撤回', value: 'RECALLED' },
] as const

export const ACTIVITY_STATUS_OPTIONS = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '已撤回', value: 'RECALLED' },
  { label: '已结束', value: 'FINISHED' },
] as const

export const DATA_SCOPE_TYPE_OPTIONS = [
  { label: '全部数据', value: 'ALL', needOrgRef: false },
  { label: '街道范围', value: 'STREET', needOrgRef: true },
  { label: '社区范围', value: 'COMMUNITY', needOrgRef: true },
  { label: '小区范围', value: 'COMPLEX', needOrgRef: true },
  { label: '物业公司范围', value: 'PROPERTY_COMPANY', needOrgRef: true },
  { label: '自定义范围', value: 'CUSTOM', needOrgRef: true },
  { label: '本人数据', value: 'SELF', needOrgRef: false },
] as const

export const REPAIR_STATUS_OPTIONS = [
  { label: '待受理', value: 'PENDING_ACCEPT' },
  { label: '已受理', value: 'ACCEPTED' },
  { label: '已转派', value: 'ASSIGNED' },
  { label: '处理中', value: 'PROCESSING' },
  { label: '待确认', value: 'WAIT_CONFIRM' },
  { label: '已完成', value: 'DONE' },
  { label: '已关闭', value: 'CLOSED' },
  { label: '已驳回', value: 'REJECTED' },
  { label: '重新处理', value: 'REOPENED' },
] as const

export function isScopeTypeNeedOrgRef(scopeType: string) {
  const hit = DATA_SCOPE_TYPE_OPTIONS.find((item) => item.value === scopeType)
  return hit ? hit.needOrgRef : true
}
