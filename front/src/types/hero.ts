/**
 * Hero 区域与导航类型定义
 */

export interface NavItem {
  label: string
  href: string
  icon?: string
  permissionCode?: string
  requiresAuth?: boolean
  publicFallback?: boolean
}

export interface CtaButton {
  text: string
  href: string
  primary?: boolean
  icon?: string
}

export interface QuickEntry {
  label: string
  icon: string
  href: string
  description?: string
}

export interface PartnerItem {
  name: string
}

export interface HeroContent {
  title: string
  subtitle: string
  ctas: CtaButton[]
  quickEntries: QuickEntry[]
  partners: PartnerItem[]
  partnerSlogan: string
  videoUrl: string
  posterUrl: string
}
