<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="logo-area">
          <img src="/favicon.svg" alt="logo" width="28" height="28" />
          <span>A.R.I.A</span>
        </div>
        <span class="admin-badge">管理后台</span>
      </div>

      <nav class="sidebar-nav">
        <router-link to="/admin" class="nav-item" exact-active-class="active">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M3 9h18M9 21V9"/></svg>
          工作台
        </router-link>
        <router-link v-if="authStore.isRootAdmin" to="/admin/users" class="nav-item">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
          账号管理
        </router-link>
        <router-link v-if="authStore.isRootAdmin" to="/admin/departments" class="nav-item">
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

    <!-- 主内容 -->
    <main class="admin-main">
      <div class="admin-content">
        <!-- 顶部栏 -->
        <div class="top-bar">
          <div>
            <h1>工作台</h1>
            <p class="welcome-text">欢迎回来，{{ authStore.user?.nickname || authStore.user?.username }}</p>
          </div>
          <div class="top-actions">
            <span class="current-time">{{ currentTime }}</span>
          </div>
        </div>

        <!-- 统计卡片 -->
        <div class="stats-grid">
          <div class="stat-card pending-posts">
            <div class="stat-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pendingPosts }}</div>
              <div class="stat-label">待回复问诊</div>
            </div>
            <div class="stat-trend up">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 15l-6-6-6 6"/></svg>
              需及时处理
            </div>
          </div>

          <div class="stat-card pending-appointments">
            <div class="stat-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pendingAppointments }}</div>
              <div class="stat-label">待确认预约</div>
            </div>
            <div class="stat-trend">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>
              等待确认
            </div>
          </div>

          <div v-if="authStore.isRootAdmin" class="stat-card total-users">
            <div class="stat-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalUsers }}</div>
              <div class="stat-label">注册用户</div>
            </div>
            <div class="stat-trend up">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 15l-6-6-6 6"/></svg>
              持续增长
            </div>
          </div>

          <div v-if="authStore.isRootAdmin" class="stat-card total-doctors">
            <div class="stat-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalDoctors }}</div>
              <div class="stat-label">医生账号</div>
            </div>
            <div class="stat-trend">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>
              在线服务
            </div>
          </div>
        </div>

        <!-- 图表区域 -->
        <div class="charts-grid">
          <!-- 问诊趋势图 -->
          <div class="chart-card">
            <div class="chart-header">
              <h3>近7日问诊趋势</h3>
            </div>
            <div class="chart-body">
              <div class="bar-chart">
                <div v-for="(item, index) in consultationTrend" :key="index" class="bar-item">
                  <div class="bar-value">{{ item.value }}</div>
                  <div class="bar-track">
                    <div class="bar-fill" :style="{ height: (item.value / maxConsultation * 100) + '%' }"></div>
                  </div>
                  <div class="bar-label">{{ item.day }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 预约状态分布 -->
          <div class="chart-card">
            <div class="chart-header">
              <h3>预约状态分布</h3>
            </div>
            <div class="chart-body">
              <div class="pie-chart-container">
                <svg class="pie-chart" viewBox="0 0 200 200">
                  <circle v-for="(item, index) in pieSegments" :key="index"
                    cx="100" cy="100" r="80"
                    fill="none"
                    :stroke="item.color"
                    stroke-width="40"
                    :stroke-dasharray="item.dashArray"
                    :stroke-dashoffset="item.dashOffset"
                    :transform="'rotate(-90 100 100)'"
                  />
                </svg>
                <div class="pie-center">
                  <div class="pie-total">{{ stats.pendingAppointments + appointmentStats.confirmed + appointmentStats.completed }}</div>
                  <div class="pie-label">总预约</div>
                </div>
              </div>
              <div class="pie-legend">
                <div v-for="item in pieLegend" :key="item.label" class="legend-item">
                  <span class="legend-dot" :style="{ background: item.color }"></span>
                  <span class="legend-label">{{ item.label }}</span>
                  <span class="legend-value">{{ item.value }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 科室问诊排行 -->
          <div class="chart-card">
            <div class="chart-header">
              <h3>科室问诊排行</h3>
            </div>
            <div class="chart-body">
              <div class="rank-list">
                <div v-for="(dept, index) in departmentRank" :key="dept.name" class="rank-item">
                  <span class="rank-num" :class="{ top: index < 3 }">{{ index + 1 }}</span>
                  <span class="rank-name">{{ dept.name }}</span>
                  <div class="rank-bar-track">
                    <div class="rank-bar-fill" :style="{ width: (dept.count / maxDeptCount * 100) + '%' }"></div>
                  </div>
                  <span class="rank-count">{{ dept.count }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 用户角色分布 -->
          <div class="chart-card">
            <div class="chart-header">
              <h3>用户角色分布</h3>
            </div>
            <div class="chart-body">
              <div class="role-cards">
                <div class="role-card">
                  <div class="role-icon users">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                  </div>
                  <div class="role-info">
                    <div class="role-value">{{ stats.totalUsers }}</div>
                    <div class="role-label">普通用户</div>
                  </div>
                </div>
                <div class="role-card">
                  <div class="role-icon doctors">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
                  </div>
                  <div class="role-info">
                    <div class="role-value">{{ stats.totalDoctors }}</div>
                    <div class="role-label">医生</div>
                  </div>
                </div>
                <div class="role-card">
                  <div class="role-icon admins">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
                  </div>
                  <div class="role-info">
                    <div class="role-value">1</div>
                    <div class="role-label">管理员</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 快捷操作 -->
        <div class="quick-links">
          <h2>快捷操作</h2>
          <div class="links-grid">
            <router-link to="/admin/consultation" class="link-card">
              <span class="link-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
              </span>
              <span>处理问诊</span>
            </router-link>
            <router-link to="/admin/appointments" class="link-card">
              <span class="link-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
              </span>
              <span>预约管理</span>
            </router-link>
            <router-link v-if="authStore.isRootAdmin" to="/admin/users" class="link-card">
              <span class="link-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
              </span>
              <span>账号管理</span>
            </router-link>
            <router-link v-if="authStore.isRootAdmin" to="/admin/departments" class="link-card">
              <span class="link-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
              </span>
              <span>科室管理</span>
            </router-link>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import api from '../../utils/api'

const router = useRouter()
const authStore = useAuthStore()

const currentTime = ref('')
const stats = ref({
  pendingPosts: 0,
  pendingAppointments: 0,
  totalUsers: 0,
  totalDoctors: 0
})

const appointmentStats = ref({
  confirmed: 0,
  completed: 0,
  cancelled: 0
})

const consultationTrend = ref([])
const departmentRank = ref([])

const maxConsultation = computed(() => {
  const max = Math.max(...consultationTrend.value.map(i => i.value))
  return max > 0 ? max : 1
})
const maxDeptCount = computed(() => {
  const max = Math.max(...departmentRank.value.map(i => i.count))
  return max > 0 ? max : 1
})

const pieSegments = computed(() => {
  const total = stats.value.pendingAppointments + appointmentStats.value.confirmed + appointmentStats.value.completed
  if (total === 0) return []

  const segments = [
    { value: stats.value.pendingAppointments, color: '#f59e0b' },
    { value: appointmentStats.value.confirmed, color: '#10b981' },
    { value: appointmentStats.value.completed, color: '#3b82f6' }
  ]

  let offset = 0
  const circumference = 2 * Math.PI * 80

  return segments.map(seg => {
    const ratio = seg.value / total
    const dashArray = `${ratio * circumference} ${circumference}`
    const dashOffset = -offset * circumference
    offset += ratio
    return { ...seg, dashArray, dashOffset }
  })
})

const pieLegend = computed(() => [
  { label: '待确认', value: stats.value.pendingAppointments, color: '#f59e0b' },
  { label: '已确认', value: appointmentStats.value.confirmed, color: '#10b981' },
  { label: '已完成', value: appointmentStats.value.completed, color: '#3b82f6' }
])

function updateTime() {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(async () => {
  updateTime()
  setInterval(updateTime, 60000)

  try {
    // 并行加载所有数据
    const [statsRes, trendRes, rankRes, distRes] = await Promise.all([
      api.get('/api/admin/dashboard/stats'),
      api.get('/api/admin/dashboard/consultation-trend'),
      api.get('/api/admin/dashboard/department-rank'),
      api.get('/api/admin/dashboard/appointment-distribution')
    ])

    // 加载概览统计
    if (statsRes.data.code === 200) {
      const data = statsRes.data.data
      stats.value = {
        pendingPosts: data.pendingPosts || 0,
        pendingAppointments: data.pendingAppointments || 0,
        totalUsers: data.totalUsers || 0,
        totalDoctors: data.totalDoctors || 0
      }
      appointmentStats.value = {
        confirmed: data.confirmedAppointments || 0,
        completed: data.completedAppointments || 0,
        cancelled: 0
      }
    }

    // 加载问诊趋势
    if (trendRes.data.code === 200) {
      consultationTrend.value = trendRes.data.data || []
    }

    // 加载科室排行
    if (rankRes.data.code === 200) {
      departmentRank.value = rankRes.data.data || []
    }

    // 加载预约分布
    if (distRes.data.code === 200) {
      const dist = distRes.data.data
      appointmentStats.value = {
        confirmed: dist.CONFIRMED || 0,
        completed: dist.COMPLETED || 0,
        cancelled: dist.CANCELLED || 0
      }
    }
  } catch (e) {
    console.error('Failed to load dashboard data:', e)
  }
})

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 240px;
  background: #1a1a2e;
  color: white;
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  bottom: 0;
  left: 0;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.logo-area span {
  font-size: 20px;
  font-weight: 600;
  color: white;
}

.admin-badge {
  display: inline-block;
  padding: 2px 10px;
  background: rgba(13,148,136,0.2);
  border-radius: 10px;
  font-size: 12px;
  color: #0d9488;
}

.sidebar-nav {
  flex: 1;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 10px;
  color: rgba(255,255,255,0.7);
  text-decoration: none;
  font-size: 14px;
  transition: all 0.2s;
}

.nav-item:hover {
  background: rgba(255,255,255,0.1);
  color: white;
}

.nav-item.active {
  background: #0d9488;
  color: white;
}

.sidebar-footer {
  padding: 16px 20px;
  border-top: 1px solid rgba(255,255,255,0.1);
}

.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 10px;
  background: transparent;
  color: rgba(255,255,255,0.5);
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.logout-btn:hover {
  background: rgba(239,68,68,0.2);
  color: #ef4444;
  border-color: rgba(239,68,68,0.3);
}

.admin-main {
  flex: 1;
  margin-left: 240px;
  background: #f5f7fa;
}

.admin-content {
  padding: 32px;
  max-width: 1400px;
}

/* 顶部栏 */
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
}

.top-bar h1 {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 4px;
}

.welcome-text {
  color: #666;
  font-size: 14px;
}

.current-time {
  color: #999;
  font-size: 14px;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
}

.stat-card.pending-posts::before { background: #f59e0b; }
.stat-card.pending-appointments::before { background: #10b981; }
.stat-card.total-users::before { background: #3b82f6; }
.stat-card.total-doctors::before { background: #8b5cf6; }

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pending-posts .stat-icon { background: #fef3c7; color: #f59e0b; }
.pending-appointments .stat-icon { background: #d1fae5; color: #10b981; }
.total-users .stat-icon { background: #dbeafe; color: #3b82f6; }
.total-doctors .stat-icon { background: #ede9fe; color: #8b5cf6; }

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 2px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #999;
}

.stat-trend.up { color: #f59e0b; }

/* 图表区域 */
.charts-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 32px;
}

.chart-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
}

.chart-header {
  margin-bottom: 20px;
}

.chart-header h3 {
  font-size: 16px;
  font-weight: 600;
}

/* 柱状图 */
.bar-chart {
  display: flex;
  align-items: flex-end;
  gap: 16px;
  height: 200px;
  padding-top: 20px;
}

.bar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
}

.bar-value {
  font-size: 12px;
  font-weight: 600;
  color: #666;
  margin-bottom: 8px;
}

.bar-track {
  flex: 1;
  width: 100%;
  background: #f5f7fa;
  border-radius: 8px;
  display: flex;
  align-items: flex-end;
  overflow: hidden;
}

.bar-fill {
  width: 100%;
  background: linear-gradient(180deg, #0d9488, #14b8a6);
  border-radius: 8px;
  transition: height 0.5s ease;
  min-height: 4px;
}

.bar-label {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

/* 饼图 */
.pie-chart-container {
  position: relative;
  width: 200px;
  height: 200px;
  margin: 0 auto;
}

.pie-chart {
  width: 100%;
  height: 100%;
}

.pie-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
}

.pie-total {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
}

.pie-label {
  font-size: 12px;
  color: #999;
}

.pie-legend {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-top: 20px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.legend-label {
  font-size: 12px;
  color: #666;
}

.legend-value {
  font-size: 12px;
  font-weight: 600;
  color: #333;
}

/* 排行榜 */
.rank-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.rank-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rank-num {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: #666;
}

.rank-num.top {
  background: #fef3c7;
  color: #f59e0b;
}

.rank-name {
  width: 80px;
  font-size: 14px;
  color: #333;
}

.rank-bar-track {
  flex: 1;
  height: 8px;
  background: #f5f7fa;
  border-radius: 4px;
  overflow: hidden;
}

.rank-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #0d9488, #14b8a6);
  border-radius: 4px;
  transition: width 0.5s ease;
}

.rank-count {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  min-width: 30px;
  text-align: right;
}

/* 角色卡片 */
.role-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.role-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f8fafb;
  border-radius: 12px;
}

.role-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.role-icon.users { background: #dbeafe; color: #3b82f6; }
.role-icon.doctors { background: #d1fae5; color: #10b981; }
.role-icon.admins { background: #fef3c7; color: #f59e0b; }

.role-value {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
}

.role-label {
  font-size: 14px;
  color: #999;
}

/* 快捷操作 */
.quick-links h2 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
}

.links-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.link-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  text-decoration: none;
  color: #333;
  font-size: 15px;
  transition: all 0.2s;
}

.link-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.06);
}

.link-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #0d9488;
}

@media (max-width: 1200px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .charts-grid { grid-template-columns: 1fr; }
  .links-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 767px) {
  .sidebar { display: none; }
  .admin-main { margin-left: 0; }
  .stats-grid { grid-template-columns: 1fr; }
  .links-grid { grid-template-columns: 1fr; }
}
</style>
