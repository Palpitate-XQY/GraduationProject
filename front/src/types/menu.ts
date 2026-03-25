export interface AdminMenuItem {
  key: string
  label: string
  route: string
  permissionCodes: string[]
}

export interface AdminMenuGroup {
  key: string
  title: string
  items: AdminMenuItem[]
}
