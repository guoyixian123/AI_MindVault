import { createRouter, createWebHistory } from 'vue-router'
import { toast } from '../composables/useToast'

const routes = [
  // 公共页面
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },

  // 主页需要登录
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: { requiresAuth: true }
  },

  // 用户功能页面（需要登录）
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/symptom',
    name: 'SymptomCheck',
    component: () => import('../views/SymptomCheck.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/medicine',
    name: 'MedicineConsult',
    component: () => import('../views/MedicineConsult.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/disease',
    name: 'DiseaseKnowledge',
    component: () => import('../views/DiseaseKnowledge.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/consultation',
    name: 'Consultation',
    component: () => import('../views/Consultation.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/consultation/:id',
    name: 'ConsultationDetail',
    component: () => import('../views/ConsultationDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/appointment',
    name: 'Appointment',
    component: () => import('../views/Appointment.vue'),
    meta: { requiresAuth: true }
  },

  {
    path: '/history',
    name: 'History',
    component: () => import('../views/History.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/health/reports',
    name: 'HealthReports',
    component: () => import('../views/HealthReports.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/health/profile',
    name: 'HealthProfile',
    component: () => import('../views/HealthProfile.vue'),
    meta: { requiresAuth: true }
  },

  // 管理端页面
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: () => import('../views/admin/AdminDashboard.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: () => import('../views/admin/UserManage.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/departments',
    name: 'AdminDepartments',
    component: () => import('../views/admin/DepartmentManage.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/consultation',
    name: 'AdminConsultation',
    component: () => import('../views/admin/ConsultationHandle.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/appointments',
    name: 'AdminAppointments',
    component: () => import('../views/admin/AppointmentManage.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('user') || 'null')

  // 未登录
  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }

  // 已登录用户
  if (token && user) {
    const isAdmin = user.role === 'ROOT_ADMIN' || user.role === 'DOCTOR'

    // 管理员访问用户页面 → 重定向到管理端
    if (isAdmin && !to.meta.requiresAdmin && to.path !== '/login') {
      next('/admin')
      return
    }

    // 普通用户访问管理页面 → 提示并重定向
    if (!isAdmin && to.meta.requiresAdmin) {
      toast.error('权限不足，您没有管理员权限')
      next('/')
      return
    }
  }

  next()
})

export default router
