import { createRouter, createWebHistory } from 'vue-router'

const routes = [
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
    component: () => import('@/views/settings/index.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
