import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../utils/api'

export const useHealthStore = defineStore('health', () => {
  const profile = ref(null)
  const records = ref([])
  const reports = ref([])
  const chronicDiseases = ref([])

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

  // 日常健康数据
  async function fetchRecords(days = 7) {
    const { data } = await api.get('/api/health/records', { params: { days } })
    if (data.code === 200) {
      records.value = data.data
    }
    return data.data
  }

  async function saveRecord(record) {
    const { data } = await api.post('/api/health/records', record)
    return data
  }

  async function fetchTrend(indicator, days = 30) {
    const { data } = await api.get('/api/health/records/trend', {
      params: { indicator, days }
    })
    return data.data
  }

  // 体检报告
  async function fetchReports() {
    const { data } = await api.get('/api/health/reports')
    if (data.code === 200) {
      reports.value = data.data
    }
    return data.data
  }

  async function uploadReport(formData) {
    const { data } = await api.post('/api/health/reports/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    return data
  }

  // 慢病管理
  async function fetchChronicDiseases() {
    const { data } = await api.get('/api/health/chronic')
    if (data.code === 200) {
      chronicDiseases.value = data.data
    }
    return data.data
  }

  async function createChronicDisease(form) {
    const { data } = await api.post('/api/health/chronic', form)
    return data
  }

  async function addChronicRecord(diseaseId, record) {
    const { data } = await api.post(`/api/health/chronic/${diseaseId}/records`, record)
    return data
  }

  return {
    profile, records, reports, chronicDiseases,
    fetchProfile, saveProfile,
    fetchRecords, saveRecord, fetchTrend,
    fetchReports, uploadReport,
    fetchChronicDiseases, createChronicDisease, addChronicRecord
  }
})
