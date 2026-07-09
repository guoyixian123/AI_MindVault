<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <h2>A.R.I.A</h2>
        <span class="role-badge">{{ isRootAdmin ? '根管理员' : '医生' }}</span>
      </div>
      <nav class="sidebar-nav">
        <router-link to="/admin" class="nav-item">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M3 9h18M9 21V9"/></svg>
          工作台
        </router-link>
        <router-link v-if="isRootAdmin" to="/admin/users" class="nav-item">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
          账号管理
        </router-link>
        <router-link v-if="isRootAdmin" to="/admin/departments" class="nav-item">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
          科室管理
        </router-link>
        <router-link to="/admin/consultation" class="nav-item">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
          问诊处理
        </router-link>
        <router-link to="/admin/appointments" class="nav-item active">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
          预约管理
        </router-link>
      </nav>
      <div class="sidebar-footer">
        <button @click="handleLogout" class="logout-btn">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
          退出登录
        </button>
      </div>
    </aside>

    <main class="admin-main">
      <div class="admin-content">
        <h1>预约管理</h1>

        <div class="data-table">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>用户ID</th>
                <th>科室</th>
                <th>预约时间</th>
                <th>诉求</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="apt in appointments" :key="apt.id">
                <td>{{ apt.id }}</td>
                <td>{{ apt.userId }}</td>
                <td>{{ getDeptName(apt.departmentId) }}</td>
                <td>{{ formatTime(apt.appointmentTime) }}</td>
                <td>{{ apt.complaint || '-' }}</td>
                <td>
                  <span class="status-tag" :class="apt.status.toLowerCase()">{{ statusText(apt.status) }}</span>
                </td>
                <td>
                  <button class="action-btn view" @click="viewProfile(apt.userId)">档案</button>
                  <button v-if="apt.status === 'PENDING'" class="action-btn confirm" @click="updateStatus(apt.id, 'CONFIRMED')">确认</button>
                  <button v-if="apt.status === 'PENDING'" class="action-btn cancel" @click="updateStatus(apt.id, 'CANCELLED')">取消</button>
                  <button v-if="apt.status === 'CONFIRMED'" class="action-btn" @click="updateStatus(apt.id, 'COMPLETED')">完成</button>
                </td>
              </tr>
            </tbody>
          </table>
          <div v-if="appointments.length === 0" class="empty">暂无预约记录</div>
        </div>
      </div>
    </main>

    <HealthProfileModal
      :visible="showProfile"
      :userId="selectedUserId"
      @close="showProfile = false"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import api from '../../utils/api'
import HealthProfileModal from '../../components/HealthProfileModal.vue'

const router = useRouter()
const authStore = useAuthStore()
const isRootAdmin = computed(() => authStore.user?.role === 'ROOT_ADMIN')

const appointments = ref([])
const departments = ref([])
const showProfile = ref(false)
const selectedUserId = ref(null)

onMounted(async () => {
  await loadDepartments()
  await loadAppointments()
})

function handleLogout() {
  authStore.logout()
}

async function loadDepartments() {
  try {
    const { data } = await api.get('/api/admin/departments')
    if (data.code === 200) {
      departments.value = data.data || []
    }
  } catch (e) {
    console.error('Failed to load departments:', e)
  }
}

async function loadAppointments() {
  try {
    const { data } = await api.get('/api/admin/appointments')
    if (data.code === 200) {
      appointments.value = data.data || []
    }
  } catch (e) {
    console.error('Failed to load appointments:', e)
  }
}

async function updateStatus(id, status) {
  try {
    await api.put(`/api/admin/appointments/${id}/status`, null, { params: { status } })
    await loadAppointments()
  } catch (e) {
    alert('操作失败')
  }
}

function viewProfile(userId) {
  selectedUserId.value = userId
  showProfile.value = true
}

function getDeptName(deptId) {
  return departments.value.find(d => d.id === deptId)?.name || '-'
}

function statusText(status) {
  const map = { PENDING: '待确认', CONFIRMED: '已确认', CANCELLED: '已取消', COMPLETED: '已完成' }
  return map[status] || status
}

function formatTime(time) {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.admin-layout { display: flex; min-height: 100vh; }
.sidebar { width: 240px; background: #1a1a2e; color: white; display: flex; flex-direction: column; position: fixed; top: 0; bottom: 0; left: 0; }
.sidebar-header { padding: 24px 20px; border-bottom: 1px solid rgba(255,255,255,0.1); }
.sidebar-header h2 { font-size: 22px; font-weight: 700; color: #0d9488; }
.role-badge { display: inline-block; padding: 2px 10px; background: rgba(13,148,136,0.2); border-radius: 10px; font-size: 12px; color: #0d9488; }
.sidebar-nav { flex: 1; padding: 16px 12px; display: flex; flex-direction: column; gap: 4px; }
.nav-item { display: flex; align-items: center; gap: 10px; padding: 12px 16px; border-radius: 10px; color: rgba(255,255,255,0.7); text-decoration: none; font-size: 14px; }
.nav-item:hover { background: rgba(255,255,255,0.1); color: white; }
.nav-item.active { background: #0d9488; color: white; }
.sidebar-footer { padding: 16px 20px; border-top: 1px solid rgba(255,255,255,0.1); }
.logout-btn { display: flex; align-items: center; justify-content: center; gap: 8px; width: 100%; padding: 10px; background: transparent; color: rgba(255,255,255,0.5); border: 1px solid rgba(255,255,255,0.1); border-radius: 8px; cursor: pointer; font-size: 14px; transition: all 0.2s; }
.logout-btn:hover { background: rgba(239,68,68,0.2); color: #ef4444; border-color: rgba(239,68,68,0.3); }
.admin-main { flex: 1; margin-left: 240px; background: #f5f7fa; }
.admin-content { padding: 32px; }
.admin-content h1 { font-size: 24px; font-weight: 600; margin-bottom: 24px; }
.data-table { background: white; border-radius: 12px; overflow: hidden; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 12px 16px; text-align: left; border-bottom: 1px solid #f3f4f6; font-size: 14px; }
th { background: #f8fafb; font-weight: 600; color: #666; }
.status-tag { padding: 2px 10px; border-radius: 10px; font-size: 12px; }
.status-tag.pending { background: #fef3c7; color: #d97706; }
.status-tag.confirmed { background: #d1fae5; color: #059669; }
.status-tag.cancelled { background: #f3f4f6; color: #6b7280; }
.status-tag.completed { background: #e0f2fe; color: #0284c7; }
.action-btn { padding: 4px 12px; background: #f3f4f6; border: none; border-radius: 6px; font-size: 13px; cursor: pointer; margin-right: 4px; }
.action-btn.confirm { background: #d1fae5; color: #059669; }
.action-btn.cancel { background: #fee2e2; color: #dc2626; }
.action-btn.view { background: #e0f2fe; color: #0284c7; }
.empty { text-align: center; color: #999; padding: 24px; }
</style>
