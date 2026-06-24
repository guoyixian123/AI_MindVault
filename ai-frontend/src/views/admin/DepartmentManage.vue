<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <h2>A.R.I.A</h2>
        <span class="role-badge">根管理员</span>
      </div>
      <nav class="sidebar-nav">
        <router-link to="/admin" class="nav-item">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M3 9h18M9 21V9"/></svg>
          工作台
        </router-link>
        <router-link to="/admin/users" class="nav-item">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
          账号管理
        </router-link>
        <router-link to="/admin/departments" class="nav-item active">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
          科室管理
        </router-link>
        <router-link to="/admin/consultation" class="nav-item">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
          问诊处理
        </router-link>
        <router-link to="/admin/appointments" class="nav-item">
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
        <div class="page-header">
          <h1>科室管理</h1>
          <button class="new-btn" @click="showForm = true">+ 新增科室</button>
        </div>

        <div class="data-table">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>科室名称</th>
                <th>描述</th>
                <th>排序</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="dept in departments" :key="dept.id">
                <td>{{ dept.id }}</td>
                <td>{{ dept.name }}</td>
                <td>{{ dept.description || '-' }}</td>
                <td>{{ dept.sortOrder }}</td>
                <td>
                  <span class="status-tag" :class="dept.status === 1 ? 'active' : 'inactive'">
                    {{ dept.status === 1 ? '启用' : '停用' }}
                  </span>
                </td>
                <td>
                  <button class="action-btn" @click="editDept(dept)">编辑</button>
                  <button class="action-btn" @click="toggleStatus(dept)">
                    {{ dept.status === 1 ? '停用' : '启用' }}
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 新增/编辑弹窗 -->
        <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
          <div class="modal">
            <div class="modal-header">
              <h2>{{ editingId ? '编辑科室' : '新增科室' }}</h2>
              <button class="close-btn" @click="showForm = false">×</button>
            </div>
            <form @submit.prevent="saveDept">
              <div class="form-group">
                <label>科室名称 *</label>
                <input v-model="deptForm.name" type="text" required />
              </div>
              <div class="form-group">
                <label>描述</label>
                <textarea v-model="deptForm.description" rows="2"></textarea>
              </div>
              <div class="form-group">
                <label>排序（数字越小越靠前）</label>
                <input v-model.number="deptForm.sortOrder" type="number" />
              </div>
              <button type="submit" class="submit-btn">保存</button>
            </form>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import api from '../../utils/api'

const router = useRouter()
const authStore = useAuthStore()

const departments = ref([])
const showForm = ref(false)
const editingId = ref(null)

const deptForm = ref({
  name: '',
  description: '',
  sortOrder: 0
})

onMounted(async () => {
  await loadDepartments()
})

function handleLogout() {
  authStore.logout()
  router.push('/login')
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

function editDept(dept) {
  editingId.value = dept.id
  deptForm.value = {
    name: dept.name,
    description: dept.description,
    sortOrder: dept.sortOrder
  }
  showForm.value = true
}

async function saveDept() {
  try {
    if (editingId.value) {
      await api.put(`/api/admin/departments/${editingId.value}`, deptForm.value)
    } else {
      await api.post('/api/admin/departments', deptForm.value)
    }
    showForm.value = false
    editingId.value = null
    deptForm.value = { name: '', description: '', sortOrder: 0 }
    await loadDepartments()
  } catch (e) {
    alert('保存失败')
  }
}

async function toggleStatus(dept) {
  const newStatus = dept.status === 1 ? 0 : 1
  try {
    await api.put(`/api/admin/departments/${dept.id}/status`, null, { params: { status: newStatus } })
    await loadDepartments()
  } catch (e) {
    alert('操作失败')
  }
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
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-header h1 { font-size: 24px; font-weight: 600; }
.new-btn { padding: 10px 24px; background: #0d9488; color: white; border: none; border-radius: 20px; cursor: pointer; }
.data-table { background: white; border-radius: 12px; overflow: hidden; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 12px 16px; text-align: left; border-bottom: 1px solid #f3f4f6; font-size: 14px; }
th { background: #f8fafb; font-weight: 600; color: #666; }
.status-tag { padding: 2px 10px; border-radius: 10px; font-size: 12px; }
.status-tag.active { background: #d1fae5; color: #059669; }
.status-tag.inactive { background: #fee2e2; color: #dc2626; }
.action-btn { padding: 4px 12px; background: #f3f4f6; border: none; border-radius: 6px; font-size: 13px; cursor: pointer; margin-right: 4px; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: white; border-radius: 20px; padding: 32px; width: 100%; max-width: 500px; }
.modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.modal-header h2 { font-size: 20px; font-weight: 600; }
.close-btn { background: none; border: none; font-size: 28px; cursor: pointer; color: #999; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 14px; font-weight: 500; margin-bottom: 6px; }
.form-group input, .form-group textarea { width: 100%; padding: 12px 16px; border: 1px solid #e0e0e0; border-radius: 12px; font-size: 15px; outline: none; }
.submit-btn { width: 100%; padding: 14px; background: #0d9488; color: white; border: none; border-radius: 12px; font-size: 16px; cursor: pointer; }
</style>
