import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../utils/api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ROOT_ADMIN' || user.value?.role === 'DOCTOR')
  const isRootAdmin = computed(() => user.value?.role === 'ROOT_ADMIN')
  const isDoctor = computed(() => user.value?.role === 'DOCTOR')
  const isUser = computed(() => user.value?.role === 'USER')

  async function login(username, password) {
    const { data } = await api.post('/api/auth/login', { username, password })
    if (data.code === 200) {
      token.value = data.data.token
      user.value = data.data
      localStorage.setItem('token', data.data.token)
      localStorage.setItem('user', JSON.stringify(data.data))
      return data.data
    }
    throw new Error(data.message)
  }

  async function register(form) {
    const { data } = await api.post('/api/auth/register', form)
    if (data.code === 200) {
      token.value = data.data.token
      user.value = data.data
      localStorage.setItem('token', data.data.token)
      localStorage.setItem('user', JSON.stringify(data.data))
      return data.data
    }
    throw new Error(data.message)
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    // 退出后跳转到登录页
    window.location.href = '/login'
  }

  return {
    token, user,
    isLoggedIn, isAdmin, isRootAdmin, isDoctor, isUser,
    login, register, logout
  }
})
