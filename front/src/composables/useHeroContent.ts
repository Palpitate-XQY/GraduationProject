/**
 * Hero 内容组合式函数
 * 文案与入口基于后端模块语义：notice / activity / repair / resident / dashboard
 */
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useUserStore } from '@/stores/user'
import type { CtaButton, HeroContent, NavItem, QuickEntry } from '@/types/hero'

const BASE_NAV_ITEMS: NavItem[] = [
  { label: '首页', href: '/' },
  { label: '社区公告', href: '/notices', permissionCode: 'notice:resident:list', publicFallback: true },
  { label: '社区活动', href: '/activities', permissionCode: 'activity:resident:list', publicFallback: true },
  { label: '在线报修', href: '/repair', permissionCode: 'repair:my:list', requiresAuth: true },
  { label: '居民服务', href: '/profile', permissionCode: 'resident:profile:my:view', requiresAuth: true },
  { label: '数据看板', href: '/dashboard', permissionCode: 'dashboard:view', requiresAuth: true },
]

const BASE_QUICK_ENTRIES: Array<QuickEntry & Pick<NavItem, 'permissionCode' | 'requiresAuth' | 'publicFallback'>> = [
  {
    label: '社区公告',
    icon: 'Megaphone',
    href: '/notices',
    description: '查看社区通知与公开信息',
    permissionCode: 'notice:resident:list',
    publicFallback: true,
  },
  {
    label: '社区活动',
    icon: 'CalendarHeart',
    href: '/activities',
    description: '报名参与社区活动',
    permissionCode: 'activity:resident:list',
    publicFallback: true,
  },
  {
    label: '在线报修',
    icon: 'Wrench',
    href: '/repair',
    description: '发起工单并跟踪处理进度',
    permissionCode: 'repair:my:list',
    requiresAuth: true,
  },
  {
    label: '居民服务',
    icon: 'UserCircle',
    href: '/profile',
    description: '查看并维护个人档案',
    permissionCode: 'resident:profile:my:view',
    requiresAuth: true,
  },
]

function checkVisibility(item: Pick<NavItem, 'permissionCode' | 'requiresAuth' | 'publicFallback'>, isLoggedIn: boolean, permissionCodes: string[]) {
  if (item.requiresAuth && !isLoggedIn) {
    return false
  }
  if (!item.permissionCode) {
    return true
  }
  if (!isLoggedIn) {
    return Boolean(item.publicFallback)
  }
  return permissionCodes.includes(item.permissionCode)
}

export function useHeroContent() {
  const userStore = useUserStore()
  const { isLoggedIn, permissionCodes } = storeToRefs(userStore)

  const navItems = computed(() =>
    BASE_NAV_ITEMS.filter((item) => checkVisibility(item, isLoggedIn.value, permissionCodes.value)),
  )

  const ctas = computed<CtaButton[]>(() => {
    const hasRepairCreate = permissionCodes.value.includes('repair:create')
    const hasProfile = permissionCodes.value.includes('resident:profile:my:view')
    const hasActivity = permissionCodes.value.includes('activity:resident:list')

    const primary: CtaButton = hasRepairCreate
      ? { text: '发起在线报修', href: '/repair/create', primary: true, icon: 'ArrowUpRight' }
      : hasProfile
        ? { text: '进入居民服务', href: '/profile', primary: true, icon: 'ArrowUpRight' }
        : { text: '查看社区公告', href: '/notices', primary: true, icon: 'ArrowUpRight' }

    const secondary: CtaButton = hasActivity
      ? { text: '浏览社区活动', href: '/activities', icon: 'Play' }
      : { text: '了解平台能力', href: '#partners', icon: 'Play' }

    return [primary, secondary]
  })

  const quickEntries = computed<QuickEntry[]>(() =>
    BASE_QUICK_ENTRIES
      .filter((item) => checkVisibility(item, isLoggedIn.value, permissionCodes.value))
      .map(({ label, icon, href, description }) => ({ label, icon, href, description })),
  )

  const heroContent = computed<HeroContent>(() => ({
    title: 'Experience Intelligent Living Every Day',
    subtitle:
      '聚合社区公告、活动发布、在线报修与居民服务，构建街道、社区、物业与居民协同运转的数字化治理平台。',
    ctas: ctas.value,
    quickEntries: quickEntries.value,
    partners: [
      { name: '公告发布协同' },
      { name: '活动运营联动' },
      { name: '报修闭环流转' },
      { name: '组织权限治理' },
      { name: '数据看板洞察' },
    ],
    partnerSlogan: '连接社区治理、物业服务与居民生活的数字化能力底座',
    videoUrl: 'https://cdn.pixabay.com/video/2020/05/24/40090-424838562_large.mp4',
    posterUrl: '/images/smart_community_bg.jpeg',
  }))

  return { heroContent, navItems }
}
