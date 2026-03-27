import { useUserStore } from '@/stores/user'

/**
 * Permission helpers for action-level control.
 */
export function useActionPermission() {
  const userStore = useUserStore()

  function can(permissionCode: string) {
    return userStore.hasPermission(permissionCode)
  }

  function canAny(permissionCodes: string[]) {
    return userStore.hasAnyPermission(permissionCodes)
  }

  return {
    can,
    canAny,
  }
}
