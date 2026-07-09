<template>
  <div class="dashboard">
    <!-- Nav -->
    <nav class="dash-nav">
      <div class="nav-inner">
        <router-link to="/" class="nav-logo">
          <img src="/favicon.svg" alt="logo" width="24" height="24" />
          <span>A.R.I.A</span>
        </router-link>
        <div class="nav-right">
          <div class="user-dropdown" ref="dropdownRef">
            <div class="user-trigger" @click="toggleDropdown">
              <div class="user-avatar">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                  <circle cx="12" cy="7" r="4"/>
                </svg>
              </div>
              <span class="user-name">{{ authStore.user?.nickname || authStore.user?.username }}</span>
              <svg class="dropdown-arrow" :class="{ open: dropdownOpen }" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M6 9l6 6 6-6"/>
              </svg>
            </div>
            <Transition name="dropdown">
              <div v-if="dropdownOpen" class="dropdown-menu">
                <router-link to="/" class="dropdown-item" @click="dropdownOpen = false">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
                    <polyline points="9 22 9 12 15 12 15 22"/>
                  </svg>
                  返回首页
                </router-link>
                <div class="dropdown-divider"></div>
                <a href="#" class="dropdown-item logout" @click.prevent="handleLogout">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                    <polyline points="16 17 21 12 16 7"/>
                    <line x1="21" y1="12" x2="9" y2="12"/>
                  </svg>
                  退出登录
                </a>
              </div>
            </Transition>
          </div>
        </div>
      </div>
    </nav>

    <div class="dash-content">
      <!-- Quick actions -->
      <div class="section-label">常用功能</div>
      <div class="quick-grid">
        <router-link to="/symptom" class="quick-card">
          <div class="quick-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>
          </div>
          <span>症状自查</span>
        </router-link>
        <router-link to="/medicine" class="quick-card">
          <div class="quick-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="6" y="3" width="12" height="18" rx="3"/><path d="M9 9h6M12 7.5v3"/></svg>
          </div>
          <span>用药咨询</span>
        </router-link>
        <router-link to="/disease" class="quick-card">
          <div class="quick-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="4" y="4" width="16" height="16" rx="2"/><path d="M4 8h16M8 4v16"/></svg>
          </div>
          <span>疾病科普</span>
        </router-link>
        <router-link to="/consultation" class="quick-card">
          <div class="quick-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
          </div>
          <span>问诊社区</span>
        </router-link>
        <router-link to="/appointment" class="quick-card">
          <div class="quick-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4M8 2v4M3 10h18"/></svg>
          </div>
          <span>预约挂号</span>
        </router-link>
        <router-link to="/health/reports" class="quick-card">
          <div class="quick-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><path d="M14 2v6h6M16 13H8M16 17H8M10 9H8"/></svg>
          </div>
          <span>报告解读</span>
        </router-link>
        <router-link to="/health/profile" class="quick-card">
          <div class="quick-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>
          </div>
          <span>健康档案</span>
        </router-link>
      </div>

      <!-- Health profile -->
      <div class="section-row">
        <div class="section-label">健康档案</div>
        <router-link to="/health/profile" class="section-link">查看详情 →</router-link>
      </div>
      <div v-if="profile" class="stats-grid">
        <div class="stat-card">
          <div class="stat-label">身高 / 体重</div>
          <div class="stat-value">{{ profile.height }}cm / {{ profile.weight }}kg</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">BMI</div>
          <div class="stat-value" :class="bmiClass">{{ profile.bmi || '--' }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">血压</div>
          <div class="stat-value">{{ profile.bloodPressureSys || '--' }}/{{ profile.bloodPressureDia || '--' }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">血糖</div>
          <div class="stat-value">{{ profile.bloodSugar || '--' }} <span class="stat-unit">mmol/L</span></div>
        </div>
      </div>
      <div v-else class="empty-card">
        <p>尚未完善健康档案</p>
        <router-link to="/health/profile" class="empty-link">立即完善</router-link>
      </div>

      <!-- My appointments -->
      <div class="section-row">
        <div class="section-label">我的预约</div>
        <router-link to="/appointment" class="section-link">查看全部 →</router-link>
      </div>
      <div v-if="appointments.length > 0" class="appointment-list">
        <div v-for="apt in appointments.slice(0, 3)" :key="apt.id" class="appointment-item">
          <div class="apt-top">
            <span class="apt-dept">{{ getDeptName(apt.departmentId) }}</span>
            <span class="apt-status" :class="apt.status.toLowerCase()">{{ statusText(apt.status) }}</span>
          </div>
          <div class="apt-time">{{ formatTime(apt.appointmentTime) }}</div>
        </div>
      </div>
      <div v-else class="empty-card">
        <p>暂无预约记录</p>
        <router-link to="/appointment" class="empty-link">去预约挂号</router-link>
      </div>

      <!-- My posts -->
      <div class="section-row">
        <div class="section-label">我的问诊</div>
        <router-link to="/consultation" class="section-link">查看全部 →</router-link>
      </div>
      <div v-if="posts.length > 0" class="post-list">
        <div v-for="post in posts.slice(0, 3)" :key="post.id" class="post-item">
          <div class="post-top">
            <span class="post-title">{{ post.title }}</span>
            <span class="post-status" :class="post.status.toLowerCase()">{{ postStatusText(post.status) }}</span>
          </div>
          <div class="post-time">{{ formatTime(post.createdAt) }}</div>
        </div>
      </div>
      <div v-else class="empty-card">
        <p>暂无问诊记录</p>
        <router-link to="/consultation" class="empty-link">去发帖咨询</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useHealthStore } from '../stores/health'
import api from '../utils/api'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const healthStore = useHealthStore()
const profile = ref(null)
const posts = ref([])
const appointments = ref([])
const departments = ref([])
const dropdownOpen = ref(false)
const dropdownRef = ref(null)

const bmiClass = computed(() => {
  if (!profile.value?.bmi) return ''
  const bmi = parseFloat(profile.value.bmi)
  if (bmi < 18.5) return 'low'
  if (bmi < 24) return 'normal'
  if (bmi < 28) return 'high'
  return 'very-high'
})

function toggleDropdown() {
  dropdownOpen.value = !dropdownOpen.value
}

function handleClickOutside(event) {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target)) {
    dropdownOpen.value = false
  }
}

// 加载数据函数
async function loadData() {
  if (!authStore.isLoggedIn) return

  try {
    // 加载健康档案
    profile.value = await healthStore.fetchProfile()

    // 加载部门列表
    const deptRes = await api.get('/api/admin/departments')
    if (deptRes.data.code === 200) {
      departments.value = deptRes.data.data || []
    }

    // 加载我的预约
    const aptRes = await api.get('/api/appointment')
    if (aptRes.data.code === 200) {
      appointments.value = aptRes.data.data || []
    }

    // 加载我的问诊
    const postRes = await api.get('/api/consultation/posts/my')
    if (postRes.data.code === 200) {
      posts.value = postRes.data.data || []
    }
  } catch (e) {
    console.error('Failed to load data:', e)
    if (e.response?.status === 401) {
      authStore.logout()
    }
  }
}

onMounted(async () => {
  document.addEventListener('click', handleClickOutside)

  // 检查是否登录
  if (!authStore.isLoggedIn) {
    router.push('/login')
    return
  }

  await loadData()
})

// 监听路由变化，每次进入 Dashboard 都刷新数据
watch(() => route.path, (newPath) => {
  if (newPath === '/dashboard' && authStore.isLoggedIn) {
    loadData()
  }
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

function getDeptName(deptId) {
  return departments.value.find(d => d.id === deptId)?.name || '未知科室'
}

function statusText(s) {
  return { PENDING: '待确认', CONFIRMED: '已确认', CANCELLED: '已取消', COMPLETED: '已完成' }[s] || s
}

function postStatusText(s) {
  return { PENDING: '待回复', REPLIED: '已回复', CLOSED: '已关闭' }[s] || s
}

function formatTime(t) {
  return t ? new Date(t).toLocaleString('zh-CN') : ''
}

function handleLogout() {
  dropdownOpen.value = false
  authStore.logout()
}
</script>

<style scoped>
.dashboard { min-height: 100vh; background: var(--bg-primary); }

.dash-nav {
  background: var(--color-white);
  border-bottom: 1px solid var(--border-light);
  padding: 0 32px;
  height: 60px;
  position: sticky;
  top: 0;
  z-index: 10;
}

.dash-nav .nav-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-forest);
  font-family: var(--font-display);
  font-size: 18px;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-dropdown {
  position: relative;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.user-trigger:hover {
  background: var(--color-paper);
}

.user-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: var(--color-paper);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-forest);
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.dropdown-arrow {
  color: var(--text-muted);
  transition: transform var(--transition-fast);
}

.dropdown-arrow.open {
  transform: rotate(180deg);
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 160px;
  background: var(--color-white);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  padding: 8px;
  z-index: 1000;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  font-size: 14px;
  color: var(--text-primary);
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
  text-decoration: none;
}

.dropdown-item:hover {
  background: var(--color-paper);
  color: var(--color-forest);
}

.dropdown-item.logout {
  color: var(--text-muted);
}

.dropdown-item.logout:hover {
  color: var(--color-danger);
  background: #fef2f2;
}

.dropdown-divider {
  height: 1px;
  background: var(--border-light);
  margin: 4px 0;
}

.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.dash-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px;
}

.section-label {
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: var(--text-muted);
  margin-bottom: 16px;
  margin-top: 32px;
}

.section-label:first-child {
  margin-top: 0;
}

.section-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 32px;
  margin-bottom: 16px;
}

.section-row .section-label {
  margin: 0;
}

.section-link {
  font-size: 13px;
  color: var(--color-forest);
  font-weight: 500;
}

.section-link:hover {
  text-decoration: underline;
}

/* — Quick actions — */
.quick-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
}

.quick-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 24px 16px;
  background: var(--color-white);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  transition: all var(--transition-base);
}

.quick-card:hover {
  border-color: var(--color-forest);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.quick-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-paper);
  border-radius: var(--radius-sm);
  color: var(--color-forest);
}

.quick-card span {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}

/* — Stats — */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.stat-card {
  background: var(--color-white);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: 20px;
}

.stat-label {
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 6px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.stat-value {
  font-size: 22px;
  font-weight: 600;
  color: var(--text-primary);
}

.stat-value.low { color: var(--color-warning); }
.stat-value.normal { color: var(--color-success); }
.stat-value.high { color: var(--color-warning); }
.stat-value.very-high { color: var(--color-danger); }

.stat-unit {
  font-size: 13px;
  font-weight: 400;
  color: var(--text-muted);
}

/* — Appointments — */
.appointment-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.appointment-item {
  padding: 14px 16px;
  background: var(--color-white);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
}

.apt-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.apt-dept {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.apt-time {
  font-size: 12px;
  color: var(--text-muted);
}

.apt-status {
  padding: 2px 8px;
  border-radius: var(--radius-xs);
  font-size: 11px;
  font-weight: 500;
}

.apt-status.pending { background: #fef3c7; color: #92400e; }
.apt-status.confirmed { background: #d1fae5; color: #065f46; }
.apt-status.cancelled { background: var(--color-paper); color: var(--text-muted); }
.apt-status.completed { background: var(--color-paper); color: var(--color-forest); }

/* — Posts — */
.post-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.post-item {
  padding: 14px 16px;
  background: var(--color-white);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
}

.post-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.post-status {
  padding: 2px 8px;
  border-radius: var(--radius-xs);
  font-size: 11px;
  font-weight: 500;
}

.post-status.pending { background: #fef3c7; color: #92400e; }
.post-status.replied { background: #d1fae5; color: #065f46; }
.post-status.closed { background: var(--color-paper); color: var(--text-muted); }

.post-title {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}

.post-time {
  font-size: 12px;
  color: var(--text-muted);
}

/* — Empty — */
.empty-card {
  text-align: center;
  padding: 32px;
  background: var(--color-white);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
}

.empty-card p {
  color: var(--text-muted);
  margin-bottom: 12px;
  font-size: 14px;
}

.empty-link {
  font-size: 14px;
  color: var(--color-forest);
  font-weight: 500;
}

.empty-link:hover {
  text-decoration: underline;
}

@media (max-width: 767px) {
  .quick-grid { grid-template-columns: repeat(3, 1fr); }
  .dash-content { padding: 20px; }
}
</style>
