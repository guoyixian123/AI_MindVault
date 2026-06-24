<template>
  <div class="page">
        <nav class="page-nav">
      <a href="#" class="back-link" @click.prevent="$router.back()">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
        返回
      </a>
      <h1>预约挂号</h1>
      <div class="nav-spacer"></div>
    </nav>


    <div class="page-content">
      <!-- 预约表单 -->
      <div class="section-card">
        <h2>新建预约</h2>
        <form @submit.prevent="submitAppointment">
          <div class="form-group">
            <label>选择科室 *</label>
            <select v-model="form.departmentId" required>
              <option value="">请选择科室</option>
              <option v-for="dept in departments" :key="dept.id" :value="dept.id">{{ dept.name }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>预约时间 *</label>
            <input v-model="form.appointmentTime" type="datetime-local" required />
          </div>
          <div class="form-group">
            <label>就诊诉求</label>
            <textarea v-model="form.complaint" placeholder="请简述您的就诊需求..." rows="3"></textarea>
          </div>
          <button type="submit" class="submit-btn" :disabled="submitting">
            {{ submitting ? '提交中...' : '提交预约' }}
          </button>
        </form>
        <div v-if="message" class="success-msg">{{ message }}</div>
      </div>

      <!-- 我的预约 -->
      <div class="section-card">
        <h2>我的预约</h2>
        <div class="appointment-list">
          <div v-for="apt in appointments" :key="apt.id" class="apt-card">
            <div class="apt-header">
              <span class="apt-status" :class="apt.status.toLowerCase()">{{ statusText(apt.status) }}</span>
              <span class="apt-dept">{{ getDeptName(apt.departmentId) }}</span>
            </div>
            <div class="apt-time">{{ formatTime(apt.appointmentTime) }}</div>
            <div v-if="apt.complaint" class="apt-complaint">{{ apt.complaint }}</div>
            <button v-if="apt.status === 'PENDING'" class="cancel-btn" @click="cancelAppointment(apt.id)">取消预约</button>
          </div>
          <div v-if="appointments.length === 0" class="empty">暂无预约记录</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../utils/api'

const departments = ref([])
const appointments = ref([])
const submitting = ref(false)
const message = ref('')

const form = ref({
  departmentId: '',
  appointmentTime: '',
  complaint: ''
})

onMounted(async () => {
  await loadDepartments()
  await loadAppointments()
})

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
    const { data } = await api.get('/api/appointment')
    if (data.code === 200) {
      appointments.value = data.data || []
    }
  } catch (e) {
    console.error('Failed to load appointments:', e)
  }
}

async function submitAppointment() {
  if (!form.value.departmentId || !form.value.appointmentTime) {
    alert('请填写完整信息')
    return
  }

  submitting.value = true
  message.value = ''
  try {
    const { data } = await api.post('/api/appointment', form.value)
    if (data.code === 200) {
      message.value = '预约成功！'
      form.value = { departmentId: '', appointmentTime: '', complaint: '' }
      await loadAppointments()
      setTimeout(() => message.value = '', 3000)
    }
  } catch (e) {
    alert('预约失败，请重试')
  } finally {
    submitting.value = false
  }
}

async function cancelAppointment(id) {
  if (!confirm('确定取消该预约吗？')) return
  try {
    await api.put(`/api/appointment/${id}/cancel`)
    await loadAppointments()
  } catch (e) {
    alert('取消失败')
  }
}

function getDeptName(deptId) {
  return departments.value.find(d => d.id === deptId)?.name || '未知科室'
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
.page { min-height: 100vh; background: var(--bg-primary); }

.page-nav {
  background: var(--color-white);
  border-bottom: 1px solid var(--border-light);
  padding: 0 32px;
  height: 60px;
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;

}

.page-nav h1 {
  flex: 1;
  text-align: center;
}



.back-link {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: var(--text-muted);
  transition: color var(--transition-fast);
}

.back-link:hover { color: var(--color-forest); }

.nav-spacer {
  min-width: 80px;
}

.page-nav h1 {
  font-size: 18px;
  font-family: var(--font-body);
  font-weight: 600;
}

.page-content {
  max-width: 700px;
  margin: 32px auto;
  padding: 0 24px;
}

.section-card {
  background: var(--color-white);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 28px;
  margin-bottom: 20px;
}

.section-card h2 {
  font-size: 1.2rem;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 6px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-sm);
  font-size: 15px;
  color: var(--text-primary);
  outline: none;
  transition: border-color var(--transition-fast);
  background: var(--color-white);
}

.form-group input:focus,
.form-group textarea:focus,
.form-group select:focus {
  border-color: var(--color-forest);
}

.submit-btn {
  width: 100%;
  padding: 13px;
  background: var(--color-forest);
  color: var(--color-white);
  border: none;
  border-radius: var(--radius-sm);
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.submit-btn:hover:not(:disabled) {
  background: var(--color-forest-dark);
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.success-msg {
  text-align: center;
  color: var(--color-success);
  margin-top: 16px;
  padding: 12px;
  background: rgba(58, 114, 80, 0.08);
  border-radius: var(--radius-sm);
  font-size: 14px;
}

.appointment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.apt-card {
  padding: 16px;
  background: var(--color-paper);
  border-radius: var(--radius-md);
}

.apt-header {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.apt-status {
  padding: 3px 10px;
  border-radius: var(--radius-xs);
  font-size: 12px;
  font-weight: 500;
}

.apt-status.pending {
  background: #fef3c7;
  color: #92400e;
}

.apt-status.confirmed {
  background: #d1fae5;
  color: #065f46;
}

.apt-status.cancelled {
  background: var(--color-cloud);
  color: var(--text-muted);
}

.apt-status.completed {
  background: var(--color-paper);
  color: var(--color-forest);
}

.apt-dept {
  padding: 3px 10px;
  border-radius: var(--radius-xs);
  font-size: 12px;
  background: var(--color-paper);
  color: var(--color-forest);
}

.apt-time {
  font-size: 14px;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.apt-complaint {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 10px;
}

.cancel-btn {
  padding: 6px 16px;
  background: rgba(168, 80, 58, 0.08);
  color: var(--color-danger);
  border: none;
  border-radius: var(--radius-sm);
  font-size: 13px;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.cancel-btn:hover {
  background: rgba(168, 80, 58, 0.15);
}

.empty {
  text-align: center;
  color: var(--text-muted);
  padding: 32px;
}
</style>
