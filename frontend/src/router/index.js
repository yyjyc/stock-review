import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    redirect: '/review-guide'
  },
  {
    path: '/review-guide',
    name: 'ReviewGuide',
    component: () => import('@/views/review-guide/index.vue')
  },
  {
    path: '/active-market',
    name: 'ActiveMarket',
    component: () => import('@/views/active-market/index.vue')
  },
  {
    path: '/operation',
    name: 'Operation',
    component: () => import('@/views/operation/index.vue')
  },
  {
    path: '/position',
    name: 'Position',
    component: () => import('@/views/position/index.vue')
  },
  {
    path: '/selection',
    name: 'Selection',
    component: () => import('@/views/selection/index.vue')
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('@/views/settings/index.vue'),
    meta: { requireAdmin: true }
  },
  {
    path: '/admin/users',
    name: 'UserManagement',
    component: () => import('@/views/admin/UserManagement.vue'),
    meta: { requireAdmin: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const publicPaths = ['/login']

router.beforeEach(async (to, from, next) => {
  const token = getToken()

  if (publicPaths.includes(to.path)) {
    if (token) {
      next('/')
      return
    }
    next()
    return
  }

  if (!token) {
    next('/login')
    return
  }

  next()
})

export default router
