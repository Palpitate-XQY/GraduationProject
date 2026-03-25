/**
 * 权限与菜单过滤工具
 */
import type { AdminMenuGroup } from '@/types/menu'

export function hasPermission(permissionCodes: string[], code: string) {
  return permissionCodes.includes(code)
}

export function hasAnyPermission(permissionCodes: string[], codes: string[]) {
  if (!codes.length) return true
  return codes.some((code) => permissionCodes.includes(code))
}

export function canAccessRoute(permissionCodes: string[], requiredPermissions?: string[]) {
  if (!requiredPermissions?.length) return true
  return hasAnyPermission(permissionCodes, requiredPermissions)
}

export const ADMIN_MENU_GROUPS: AdminMenuGroup[] = [
  {
    key: 'biz',
    title: '业务管理',
    items: [
      { key: 'notice', label: '公告管理', route: '/dashboard/notice', permissionCodes: ['notice:list'] },
      { key: 'activity', label: '活动管理', route: '/dashboard/activity', permissionCodes: ['activity:list'] },
      { key: 'repair', label: '报修工单', route: '/dashboard/repair', permissionCodes: ['repair:manage:list'] },
      { key: 'resident', label: '居民档案', route: '/dashboard/resident', permissionCodes: ['resident:profile:view'] },
    ],
  },
  {
    key: 'sys',
    title: '系统管理',
    items: [
      { key: 'sys-users', label: '用户管理', route: '/dashboard/system/users', permissionCodes: ['sys:user:list'] },
      { key: 'sys-roles', label: '角色管理', route: '/dashboard/system/roles', permissionCodes: ['sys:role:list'] },
      { key: 'sys-permissions', label: '权限管理', route: '/dashboard/system/permissions', permissionCodes: ['sys:permission:list'] },
    ],
  },
  {
    key: 'org',
    title: '组织管理',
    items: [
      { key: 'org-tree', label: '组织架构', route: '/dashboard/org/tree', permissionCodes: ['org:tree:view'] },
      {
        key: 'org-complex-property',
        label: '小区物业关联',
        route: '/dashboard/org/complex-property',
        permissionCodes: ['org:complex-property:list'],
      },
    ],
  },
  {
    key: 'log',
    title: '日志审计',
    items: [
      { key: 'log-login', label: '登录日志', route: '/dashboard/log/login', permissionCodes: ['log:login:list'] },
      { key: 'log-operation', label: '操作日志', route: '/dashboard/log/operation', permissionCodes: ['log:operation:list'] },
    ],
  },
  {
    key: 'storage',
    title: '存储配置',
    items: [
      { key: 'storage-config', label: '存储参数', route: '/dashboard/system/storage', permissionCodes: ['sys:storage:view'] },
    ],
  },
]

export function resolveAdminMenus(permissionCodes: string[]) {
  return ADMIN_MENU_GROUPS
    .map((group) => ({
      ...group,
      items: group.items.filter((item) => hasAnyPermission(permissionCodes, item.permissionCodes)),
    }))
    .filter((group) => group.items.length > 0)
}
