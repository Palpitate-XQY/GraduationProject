/**
 * Hero 区域内容组合式函数
 * 根据后端业务分析结果，提供 Hero 区域所需的全部数据
 *
 * ▸ 导航项 → 对应后端 notice / activity / repair / dashboard 模块
 * ▸ CTA → 引导居民进入公告和报修流程
 * ▸ 快捷入口 → 映射后端 4 个核心居民端接口
 * ▸ 合作伙伴 → 映射后端平台核心能力模块
 */
import { reactive } from 'vue'
import type { HeroContent, NavItem } from '@/types/hero'

/** 导航项（基于后端业务模块） */
export const navItems: NavItem[] = [
  { label: '首页', href: '/' },
  { label: '社区公告', href: '/notices' },
  { label: '社区活动', href: '/activities' },
  { label: '在线报修', href: '/repair' },
  { label: '数据中心', href: '/dashboard' },
]

/** 首屏 Hero 完整内容 */
export function useHeroContent() {
  const heroContent = reactive<HeroContent>({
    title: 'Experience Intelligent Living Every Day',
    subtitle:
      '聚合公告、活动、报修与居民服务，打造连接街道、社区、物业与居民的智慧生活平台。以数字化协同为基础，让社区治理更高效、物业服务更及时、居民生活更便捷。',
    ctas: [
      { text: '探索社区服务', href: '/notices', primary: true, icon: 'ArrowUpRight' },
      { text: '了解更多', href: '#features', primary: false, icon: 'Play' },
    ],
    quickEntries: [
      {
        label: '社区公告',
        icon: 'Megaphone',
        href: '/notices',
        description: '查看最新社区通知与公告',
      },
      {
        label: '社区活动',
        icon: 'CalendarHeart',
        href: '/activities',
        description: '报名参与社区活动',
      },
      {
        label: '在线报修',
        icon: 'Wrench',
        href: '/repair',
        description: '一键发起物业报修',
      },
      {
        label: '居民档案',
        icon: 'UserCircle',
        href: '/profile',
        description: '管理我的居民信息',
      },
    ],
    partners: [
      { name: '智慧治理' },
      { name: '数据看板' },
      { name: '物业协同' },
      { name: '居民服务' },
      { name: '安全保障' },
    ],
    partnerSlogan: '连接社区治理、物业服务与居民生活的数字化能力底座',
    videoUrl:
      'https://videos.pexels.com/video-files/3129671/3129671-uhd_2560_1440_30fps.mp4',
    posterUrl: '/images/smart_community_bg.jpeg',
  })

  return { heroContent }
}
