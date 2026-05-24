import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/transactions',
    children: [
      {
        path: 'transactions',
        name: 'Transactions',
        component: () => import('@/views/Transactions.vue')
      },
      {
        path: 'budget',
        name: 'Budget',
        component: () => import('@/views/Budget.vue')
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/Statistics.vue')
      },
      {
        path: 'admin',
        name: 'Admin',
        component: () => import('@/views/Admin.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'ai-assistant',
        name: 'AiAssistant',
        component: () => import('@/views/AiAssistant.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const token = userStore.token

  if (to.path === '/login' || to.path === '/register') {
    if (token) {
      if (userStore.user?.role === 2) {
        next('/admin')
      } else {
        next('/')
      }
    } else {
      next()
    }
  } else {
    if (!token) {
      next('/login')
    } else {
      if (to.meta.requiresAdmin && userStore.user?.role !== 2) {
        next('/transactions')
      } else {
        if (to.path === '/' && userStore.user?.role === 2) {
          next('/admin')
        } else {
          next()
        }
      }
    }
  }
})

export default router
