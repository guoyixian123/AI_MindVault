import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../utils/api'

export const useHealthStore = defineStore('health', () => {
  const profile = ref(null)

  // 健康档案
  async function fetchProfile() {
    const { data } = await api.get('/api/health/profile')
    if (data.code === 200) {
      profile.value = data.data
    }
    return data.data
  }

  async function saveProfile(form) {
    const { data } = await api.post('/api/health/profile', form)
    if (data.code === 200) {
      profile.value = data.data
    }
    return data
  }

  return { profile, fetchProfile, saveProfile }
})
