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
            <div class="datetime-row">
              <input v-model="form.appointmentDate" type="date" :min="minDate" :max="maxDate" required />
              <select v-model="form.appointmentTimeSlot" required>
                <option value="">请选择时段</option>
                <option v-for="t in morningSlots" :key="t" :value="t">上午 {{ t }}</option>
                <option v-for="t in afternoonSlots" :key="t" :value="t">下午 {{ t }}</option>
              </select>
            </div>
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
          <div v-for="apt in appointments" :key="apt.id" class="apt-card" :class="apt.status.toLowerCase()">
            <div class="apt-topbar">
              <span class="apt-dept">{{ getDeptName(apt.departmentId) }}</span>
              <span class="apt-status" :class="apt.status.toLowerCase()">{{ statusText(apt.status) }}</span>
            </div>
            <div class="apt-time-block">
              <span class="apt-time-big">{{ formatTimeOnly(apt.appointmentTime) }}</span>
              <span class="apt-date-sub">{{ formatDate(apt.appointmentTime) }}</span>
            </div>
            <div v-if="apt.complaint" class="apt-bottom">
              <span class="apt-complaint">{{ apt.complaint }}</span>
              <button v-if="apt.status === 'PENDING'" class="cancel-btn" @click="cancelAppointment(apt.id)">取消预约</button>
            </div>
            <div v-else class="apt-bottom">
              <span></span>
              <button v-if="apt.status === 'PENDING'" class="cancel-btn" @click="cancelAppointment(apt.id)">取消预约</button>
            </div>
          </div>
          <div v-if="appointments.length === 0" class="empty">暂无预约记录</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '../utils/api'

const departments = ref([])
const appointments = ref([])
const submitting = ref(false)
const message = ref('')

const form = ref({
  departmentId: '',
  appointmentDate: '',
  appointmentTimeSlot: '',
  complaint: ''
})

// 可选时段：上午 9:00-12:00，下午 14:00-18:00，每30分钟
const morningSlots = ['09:00', '09:30', '10:00', '10:30', '11:00', '11:30']
const afternoonSlots = ['14:00', '14:30', '15:00', '15:30', '16:00', '16:30', '17:00', '17:30']

// 日期限制（纯日期，给 type="date" 用）
const minDate = computed(() => {
  const t = new Date()
  t.setDate(t.getDate() + 1)
  return t.toISOString().slice(0, 10)
})
const maxDate = computed(() => {
  const t = new Date()
  t.setDate(t.getDate() + 31)
  return t.toISOString().slice(0, 10)
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
  if (!form.value.departmentId || !form.value.appointmentDate || !form.value.appointmentTimeSlot) {
    alert('请填写完整信息')
    return
  }

  // 拼合日期和时间
  const appointmentTime = form.value.appointmentDate + 'T' + form.value.appointmentTimeSlot + ':00'

  submitting.value = true
  message.value = ''
  try {
    const { data } = await api.post('/api/appointment', {
      departmentId: form.value.departmentId,
      appointmentTime,
      complaint: form.value.complaint
    })
    if (data.code === 200) {
      message.value = '预约成功！'
      form.value = { departmentId: '', appointmentDate: '', appointmentTimeSlot: '', complaint: '' }
      await loadAppointments()
      setTimeout(() => message.value = '', 3000)
    } else {
      alert(data.message || '预约失败，请重试')
    }
  } catch (e) {
    const msg = e?.response?.data?.message || '预约失败，请重试'
    alert(msg)
  } finally {
    submitting.value = false
  }
}

async function cancelAppointment(id) {
  if (!confirm('确定取消该预约吗？')) return
  try {
    const { data } = await api.put(`/api/appointment/${id}/cancel`)
    if (data.code === 200) {
      await loadAppointments()
    } else {
      alert(data.message || '取消失败')
    }
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

function safeParseDate(time) {
  if (!time) return null
  // 兼容 Safari：将空格分隔的日期转为 T 分隔
  const d = new Date(time.replace(' ', 'T'))
  return isNaN(d.getTime()) ? null : d
}

function formatDate(time) {
  if (!time) return ''
  const d = safeParseDate(time)
  if (!d) return time
  const week = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return `${d.getFullYear()}/${d.getMonth() + 1}/${d.getDate()} ${week[d.getDay()]}`
}

function formatTimeOnly(time) {
  if (!time) return ''
  const d = safeParseDate(time)
  if (!d) return time
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
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

/* 日期 + 时段并排 */
.datetime-row {
  display: flex;
  gap: 12px;
}

.datetime-row input,
.datetime-row select {
  flex: 1;
  padding: 12px 14px;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-sm);
  font-size: 15px;
  color: var(--text-primary);
  outline: none;
  transition: border-color var(--transition-fast);
  background: var(--color-white);
}

.datetime-row input:focus,
.datetime-row select:focus {
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

/* ---- 预约卡片 - 登机牌风格 ---- */
.apt-card {
  background: var(--color-white);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: 16px 20px;
  position: relative;
  overflow: hidden;
  transition: box-shadow 0.2s;
}

.apt-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0; bottom: 0;
  width: 3px;
}

.apt-card.pending::before   { background: #f59e0b; }
.apt-card.confirmed::before { background: #22c55e; }
.apt-card.cancelled::before { background: #cbd5e1; }
.apt-card.completed::before { background: var(--color-forest); }

.apt-card:hover {
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

/* 顶栏：科室 + 状态 */
.apt-topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.apt-dept {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.apt-status {
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
}

.apt-status.pending   { background: #fef3c7; color: #b45309; }
.apt-status.confirmed { background: #dcfce7; color: #15803d; }
.apt-status.cancelled { background: #f1f5f9; color: #64748b; }
.apt-status.completed { background: #dcfce7; color: #15803d; }

/* 时间区 */
.apt-time-block {
  display: flex;
  align-items: baseline;
  gap: 14px;
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 1px dashed var(--border-light);
}

.apt-time-big {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  font-family: 'SF Mono', 'Menlo', 'Consolas', monospace;
}

.apt-date-sub {
  font-size: 13px;
  color: var(--text-muted);
}

/* 底部：诉求 + 取消 */
.apt-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.apt-complaint {
  font-size: 13px;
  color: var(--text-secondary);
}

.cancel-btn {
  flex-shrink: 0;
  padding: 5px 14px;
  background: transparent;
  color: var(--text-muted);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-sm);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.15s;
}

.cancel-btn:hover {
  color: #ef4444;
  border-color: #fca5a5;
  background: #fef2f2;
}

.empty {
  text-align: center;
  color: var(--text-muted);
  padding: 32px;
}
</style>
