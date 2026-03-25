/**
 * Vue Router 配置
 * 路由结构依据后端模块设计
 */
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  /* ===== 门户路由（含 Navbar） ===== */
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
      /* ===== 公告 ===== */
      {
        path: 'notices',
        name: 'Notices',
        component: () => import('@/views/notice/NoticeListView.vue'),
        meta: { title: '社区公告 · 智慧社区' },
      },
      {
        path: 'notices/:id',
        name: 'NoticeDetail',
        component: () => import('@/views/notice/NoticeDetailView.vue'),
        meta: { title: '公告详情 · 智慧社区' },
      },
      /* ===== 活动 ===== */
      {
        path: 'activities',
        name: 'Activities',
        component: () => import('@/views/activity/ActivityListView.vue'),
        meta: { title: '社区活动 · 智慧社区' },
      },
      {
        path: 'activities/:id',
        name: 'ActivityDetail',
        component: () => import('@/views/activity/ActivityDetailView.vue'),
        meta: { title: '活动详情 · 智慧社区' },
      },
      /* ===== 报修 ===== */
      {
        path: 'repair',
        name: 'Repair',
        component: () => import('@/views/repair/RepairListView.vue'),
        meta: { title: '我的报修 · 智慧社区', requiresAuth: true },
      },
      {
        path: 'repair/create',
        name: 'RepairCreate',
        component: () => import('@/views/repair/RepairCreateView.vue'),
        meta: { title: '发起报修 · 智慧社区', requiresAuth: true },
      },
      {
        path: 'repair/:id',
        name: 'RepairDetail',
        component: () => import('@/views/repair/RepairDetailView.vue'),
        meta: { title: '报修详情 · 智慧社区', requiresAuth: true },
      },
      /* ===== 个人中心 ===== */
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/ProfileView.vue'),
        meta: { title: '个人中心 · 智慧社区', requiresAuth: true },
      },
    ],
  },
  /* ===== 独立页面（无 Navbar） ===== */
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { title: '登录 · 智慧社区' },
  },
  /* ===== 404 ===== */
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

/* 路由守卫 — 标题设置 + 登录检查 */
router.beforeEach((to) => {
  document.title = (to.meta.title as string) || '智慧社区'

  // 需要认证的路由，检查 token
  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('access_token')
    if (!token) {
      return { path: '/login', query: { redirect: to.fullPath } }
    }
  }
})

export default router
