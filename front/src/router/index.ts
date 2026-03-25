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
      /* 后续扩展的居民端页面 */
      // {
      //   path: 'notices',
      //   name: 'Notices',
      //   component: () => import('@/views/notice/NoticeListView.vue'),
      //   meta: { title: '社区公告' },
      // },
      // {
      //   path: 'activities',
      //   name: 'Activities',
      //   component: () => import('@/views/activity/ActivityListView.vue'),
      //   meta: { title: '社区活动' },
      // },
      // {
      //   path: 'repair',
      //   name: 'Repair',
      //   component: () => import('@/views/repair/RepairView.vue'),
      //   meta: { title: '在线报修' },
      // },
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

/* 路由守卫 — 设置页面标题 */
router.beforeEach((to) => {
  document.title = (to.meta.title as string) || '智慧社区'
})

export default router
