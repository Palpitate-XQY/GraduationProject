/**
 * Vue Router 配置
 * - 门户端 + 管理端骨架
 * - 路由/菜单/按钮三层权限联动
 */
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { canAccessRoute } from '@/utils/permission'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/layout/PortalLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/home/HomeView.vue'),
        meta: { title: '首页 · 智慧社区' },
      },
      {
        path: 'notices',
        name: 'Notices',
        component: () => import('@/views/notice/NoticeListView.vue'),
        meta: { title: '社区公告 · 智慧社区', requiresAuth: true, permissions: ['notice:resident:list'] },
      },
      {
        path: 'notices/:id',
        name: 'NoticeDetail',
        component: () => import('@/views/notice/NoticeDetailView.vue'),
        meta: { title: '公告详情 · 智慧社区', requiresAuth: true, permissions: ['notice:resident:view'] },
      },
      {
        path: 'activities',
        name: 'Activities',
        component: () => import('@/views/activity/ActivityListView.vue'),
        meta: { title: '社区活动 · 智慧社区', requiresAuth: true, permissions: ['activity:resident:list'] },
      },
      {
        path: 'activities/:id',
        name: 'ActivityDetail',
        component: () => import('@/views/activity/ActivityDetailView.vue'),
        meta: { title: '活动详情 · 智慧社区', requiresAuth: true, permissions: ['activity:resident:view'] },
      },
      {
        path: 'repair',
        name: 'Repair',
        component: () => import('@/views/repair/RepairListView.vue'),
        meta: { title: '在线报修 · 智慧社区', requiresAuth: true, permissions: ['repair:my:list'] },
      },
      {
        path: 'repair/create',
        name: 'RepairCreate',
        component: () => import('@/views/repair/RepairCreateView.vue'),
        meta: { title: '发起报修 · 智慧社区', requiresAuth: true, permissions: ['repair:create'] },
      },
      {
        path: 'repair/:id',
        name: 'RepairDetail',
        component: () => import('@/views/repair/RepairDetailView.vue'),
        meta: { title: '报修详情 · 智慧社区', requiresAuth: true, permissions: ['repair:my:view'] },
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/ProfileView.vue'),
        meta: { title: '居民服务 · 智慧社区', requiresAuth: true, permissions: ['resident:profile:my:view'] },
      },
    ],
  },
  {
    path: '/dashboard',
    component: () => import('@/layout/AdminLayout.vue'),
    meta: { title: '管理后台', requiresAuth: true, permissions: ['dashboard:view'] },
    children: [
      {
        path: '',
        name: 'DashboardEntry',
        component: () => import('@/views/admin/DashboardRedirectView.vue'),
        meta: { title: '数据看板', requiresAuth: true, permissions: ['dashboard:view'] },
      },
      {
        path: 'super-admin',
        name: 'DashboardSuperAdmin',
        component: () => import('@/views/admin/DashboardView.vue'),
        meta: { title: '超级管理员看板', requiresAuth: true, permissions: ['dashboard:view'] },
      },
      {
        path: 'street-admin',
        name: 'DashboardStreetAdmin',
        component: () => import('@/views/admin/DashboardView.vue'),
        meta: { title: '街道管理员看板', requiresAuth: true, permissions: ['dashboard:view'] },
      },
      {
        path: 'community-admin',
        name: 'DashboardCommunityAdmin',
        component: () => import('@/views/admin/DashboardView.vue'),
        meta: { title: '社区管理员看板', requiresAuth: true, permissions: ['dashboard:view'] },
      },
      {
        path: 'property-admin',
        name: 'DashboardPropertyAdmin',
        component: () => import('@/views/admin/DashboardView.vue'),
        meta: { title: '物业管理员看板', requiresAuth: true, permissions: ['dashboard:view'] },
      },
      {
        path: 'maintainer',
        name: 'DashboardMaintainer',
        component: () => import('@/views/admin/DashboardView.vue'),
        meta: { title: '维修员看板', requiresAuth: true, permissions: ['dashboard:view'] },
      },
      {
        path: 'general',
        name: 'DashboardGeneral',
        component: () => import('@/views/admin/DashboardView.vue'),
        meta: { title: '通用看板', requiresAuth: true, permissions: ['dashboard:view'] },
      },
      {
        path: 'notice',
        name: 'AdminNotice',
        component: () => import('@/views/admin/AdminSkeletonView.vue'),
        meta: {
          title: '公告管理',
          description: '对接 /api/notices 列表、发布、撤回等能力。',
          requiresAuth: true,
          permissions: ['notice:list'],
        },
      },
      {
        path: 'activity',
        name: 'AdminActivity',
        component: () => import('@/views/admin/AdminSkeletonView.vue'),
        meta: {
          title: '活动管理',
          description: '对接 /api/activities 管理页、报名统计与状态流转。',
          requiresAuth: true,
          permissions: ['activity:list'],
        },
      },
      {
        path: 'repair',
        name: 'AdminRepair',
        component: () => import('@/views/admin/AdminSkeletonView.vue'),
        meta: {
          title: '报修工单',
          description: '对接 /api/repairs/manage 与受理、转派、处理动作链。',
          requiresAuth: true,
          permissions: ['repair:manage:list'],
        },
      },
      {
        path: 'resident',
        name: 'AdminResident',
        component: () => import('@/views/admin/AdminSkeletonView.vue'),
        meta: {
          title: '居民档案',
          description: '对接 /api/resident/profiles 管理能力。',
          requiresAuth: true,
          permissions: ['resident:profile:view'],
        },
      },
      {
        path: 'system/users',
        name: 'AdminSystemUsers',
        component: () => import('@/views/admin/AdminSkeletonView.vue'),
        meta: {
          title: '用户管理',
          description: '对接 /api/system/users 与角色分配。',
          requiresAuth: true,
          permissions: ['sys:user:list'],
        },
      },
      {
        path: 'system/roles',
        name: 'AdminSystemRoles',
        component: () => import('@/views/admin/AdminSkeletonView.vue'),
        meta: {
          title: '角色管理',
          description: '对接 /api/system/roles 与 role_scope 策略。',
          requiresAuth: true,
          permissions: ['sys:role:list'],
        },
      },
      {
        path: 'system/permissions',
        name: 'AdminSystemPermissions',
        component: () => import('@/views/admin/AdminSkeletonView.vue'),
        meta: {
          title: '权限管理',
          description: '对接 /api/system/permissions 权限点维护。',
          requiresAuth: true,
          permissions: ['sys:permission:list'],
        },
      },
      {
        path: 'org/tree',
        name: 'AdminOrgTree',
        component: () => import('@/views/admin/AdminSkeletonView.vue'),
        meta: {
          title: '组织架构',
          description: '对接 /api/org/tree 与组织节点增删改。',
          requiresAuth: true,
          permissions: ['org:tree:view'],
        },
      },
      {
        path: 'org/complex-property',
        name: 'AdminOrgComplexProperty',
        component: () => import('@/views/admin/AdminSkeletonView.vue'),
        meta: {
          title: '小区物业关联',
          description: '对接 /api/org/complex-property-rel 维护小区与物业关系。',
          requiresAuth: true,
          permissions: ['org:complex-property:list'],
        },
      },
      {
        path: 'log/login',
        name: 'AdminLogLogin',
        component: () => import('@/views/admin/AdminSkeletonView.vue'),
        meta: {
          title: '登录日志',
          description: '对接 /api/logs/logins 审计登录行为。',
          requiresAuth: true,
          permissions: ['log:login:list'],
        },
      },
      {
        path: 'log/operation',
        name: 'AdminLogOperation',
        component: () => import('@/views/admin/AdminSkeletonView.vue'),
        meta: {
          title: '操作日志',
          description: '对接 /api/logs/operations 审计系统操作。',
          requiresAuth: true,
          permissions: ['log:operation:list'],
        },
      },
      {
        path: 'system/storage',
        name: 'AdminSystemStorage',
        component: () => import('@/views/admin/AdminSkeletonView.vue'),
        meta: {
          title: '存储配置',
          description: '对接 /api/system/storage-config，维护 LOCAL/QINIU 参数。',
          requiresAuth: true,
          permissions: ['sys:storage:view'],
        },
      },
    ],
  },
  {
    path: '/admin',
    redirect: '/dashboard',
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { title: '登录 · 智慧社区' },
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/ForbiddenView.vue'),
    meta: { title: '无权限访问 · 智慧社区' },
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach(async (to) => {
  document.title = (to.meta.title as string) || '智慧社区'

  const userStore = useUserStore()
  const appStore = useAppStore()

  const localToken = localStorage.getItem('access_token') || ''
  if (!userStore.token && localToken) {
    userStore.setToken(localToken)
  }

  const requiresAuth = Boolean(to.meta.requiresAuth)
  if (requiresAuth && !userStore.token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  if (userStore.token && !userStore.userLoaded) {
    try {
      await userStore.ensureUserInfo()
    } catch {
      appStore.clearDashboardOverview()
      return { path: '/login', query: { redirect: to.fullPath } }
    }
  }

  if (to.path === '/login' && userStore.isLoggedIn) {
    if (userStore.hasPermission('dashboard:view')) {
      try {
        await appStore.fetchDashboardOverview()
        return appStore.defaultHomeRoute
      } catch {
        return '/'
      }
    }
    return '/'
  }

  const requiredPermissions = (to.meta.permissions as string[] | undefined) || []
  if (!canAccessRoute(userStore.permissionCodes, requiredPermissions)) {
    return { path: '/403', query: { from: to.fullPath } }
  }

  if (to.path.startsWith('/dashboard') && userStore.hasPermission('dashboard:view')) {
    try {
      await appStore.fetchDashboardOverview()
    } catch {
      if (to.path !== '/403') {
        return { path: '/403', query: { from: to.fullPath } }
      }
    }

    if (to.name === 'DashboardEntry') {
      return appStore.defaultHomeRoute
    }
  }
})

export default router
