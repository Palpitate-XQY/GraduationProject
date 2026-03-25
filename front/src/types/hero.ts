/**
 * Hero 区域类型定义
 */

/** 导航项 */
export interface NavItem {
  label: string
  href: string
  icon?: string
}

/** CTA 按钮 */
export interface CtaButton {
  text: string
  href: string
  primary?: boolean
  icon?: string
}

/** 快捷入口 */
export interface QuickEntry {
  label: string
  icon: string
  href: string
  description?: string
}

/** 合作伙伴 / 能力标签 */
export interface PartnerItem {
  name: string
}

/** Hero 区域完整数据 */
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
