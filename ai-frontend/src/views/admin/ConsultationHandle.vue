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
        <router-link to="/admin/consultation" class="nav-item active">
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
        <h1>问诊处理</h1>

        <!-- 筛选 -->
        <div class="filters">
          <select v-model="filterStatus" @change="loadPosts">
            <option value="">全部状态</option>
            <option value="PENDING">待回复</option>
            <option value="REPLIED">已回复</option>
            <option value="CLOSED">已关闭</option>
          </select>
        </div>

        <!-- 帖子列表 -->
        <div class="post-list">
          <div v-for="post in posts" :key="post.id" class="post-card" @click="openPost(post)">
            <div class="post-header">
              <span class="post-status" :class="post.status.toLowerCase()">{{ statusText(post.status) }}</span>
              <span class="post-time">{{ formatTime(post.createdAt) }}</span>
            </div>
            <h3>{{ post.title }}</h3>
            <p class="post-preview">{{ post.content.substring(0, 100) }}...</p>
          </div>
          <div v-if="posts.length === 0" class="empty">暂无问诊帖子</div>
        </div>

        <!-- 帖子详情与回复弹窗 -->
        <div v-if="selectedPost" class="modal-overlay" @click.self="selectedPost = null">
          <div class="modal large">
            <div class="modal-header">
              <div class="modal-title">
                <h2>{{ selectedPost.title }}</h2>
                <button class="profile-btn" @click="showProfile = true">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>
                  查看患者档案
                </button>
              </div>
              <button class="close-btn" @click="selectedPost = null">×</button>
            </div>
            <div class="post-detail">
              <div class="post-content">{{ selectedPost.content }}</div>
              <div class="post-meta">
                <span>发布时间：{{ formatTime(selectedPost.createdAt) }}</span>
              </div>
            </div>

            <!-- 回复列表 -->
            <div class="replies-section">
              <h3>医生回复</h3>
              <div v-for="reply in replies" :key="reply.id" class="reply-item">
                <div class="reply-header">
                  <span>👨‍⚕️ 医生</span>
                  <span class="reply-time">{{ formatTime(reply.createdAt) }}</span>
                </div>
                <div class="reply-content">{{ reply.content }}</div>
              </div>
              <div v-if="replies.length === 0" class="no-reply">暂无回复</div>
            </div>

            <!-- 回复表单 -->
            <div class="reply-form">
              <textarea v-model="replyContent" placeholder="输入回复内容..." rows="4"></textarea>
              <button class="submit-btn" @click="submitReply" :disabled="!replyContent.trim()">
                提交回复
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>

    <HealthProfileModal
      :visible="showProfile"
      :userId="selectedPost?.userId"
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

const posts = ref([])
const replies = ref([])
const filterStatus = ref('')
const selectedPost = ref(null)
const replyContent = ref('')
const showProfile = ref(false)

onMounted(async () => {
  await loadPosts()
})

function handleLogout() {
  authStore.logout()
  router.push('/login')
}

async function loadPosts() {
  try {
    const params = filterStatus.value ? { status: filterStatus.value } : {}
    const { data } = await api.get('/api/admin/consultation/posts', { params })
    if (data.code === 200) {
      posts.value = data.data || []
    }
  } catch (e) {
    console.error('Failed to load posts:', e)
  }
}

async function openPost(post) {
  selectedPost.value = post
  replyContent.value = ''
  try {
    const { data } = await api.get(`/api/admin/consultation/posts/${post.id}`)
    if (data.code === 200) {
      replies.value = data.data.replies || []
    }
  } catch (e) {
    console.error('Failed to load replies:', e)
  }
}

async function submitReply() {
  if (!replyContent.value.trim()) return
  try {
    const { data } = await api.post(`/api/admin/consultation/posts/${selectedPost.value.id}/reply`, {
      content: replyContent.value
    })
    if (data.code === 200) {
      replyContent.value = ''
      await openPost(selectedPost.value)
      await loadPosts()
    }
  } catch (e) {
    alert('回复失败')
  }
}

function statusText(status) {
  const map = { PENDING: '待回复', REPLIED: '已回复', CLOSED: '已关闭' }
  return map[status] || status
}

function formatTime(timestamp) {
  if (!timestamp) return ''
  return new Date(timestamp).toLocaleString('zh-CN')
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
.filters { margin-bottom: 16px; }
.filters select { padding: 8px 16px; border: 1px solid #e0e0e0; border-radius: 8px; font-size: 14px; }
.post-list { display: flex; flex-direction: column; gap: 12px; }
.post-card { background: white; border-radius: 14px; padding: 20px; cursor: pointer; transition: all 0.2s; }
.post-card:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(0,0,0,0.06); }
.post-header { display: flex; justify-content: space-between; margin-bottom: 8px; }
.post-status { padding: 2px 10px; border-radius: 10px; font-size: 12px; font-weight: 500; }
.post-status.pending { background: #fef3c7; color: #d97706; }
.post-status.replied { background: #d1fae5; color: #059669; }
.post-status.closed { background: #f3f4f6; color: #6b7280; }
.post-time { font-size: 12px; color: #999; }
.post-card h3 { font-size: 16px; font-weight: 600; margin-bottom: 8px; }
.post-preview { font-size: 14px; color: #666; }
.empty { text-align: center; color: #999; padding: 40px; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 100; padding: 20px; }
.modal { background: white; border-radius: 20px; padding: 32px; width: 100%; max-width: 600px; max-height: 90vh; overflow-y: auto; }
.modal.large { max-width: 700px; }
.modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.modal-header h2 { font-size: 20px; font-weight: 600; }
.modal-title { display: flex; align-items: center; gap: 12px; }
.profile-btn { display: flex; align-items: center; gap: 4px; padding: 4px 10px; background: rgba(13,148,136,0.1); border: none; border-radius: 6px; font-size: 12px; color: #0d9488; cursor: pointer; transition: all 0.2s; }
.profile-btn:hover { background: rgba(13,148,136,0.2); }
.close-btn { background: none; border: none; font-size: 28px; cursor: pointer; color: #999; }
.post-detail { margin-bottom: 24px; }
.post-content { font-size: 15px; line-height: 1.7; padding: 16px; background: #f8fafb; border-radius: 12px; white-space: pre-wrap; }
.post-meta { font-size: 13px; color: #999; margin-top: 8px; }
.replies-section { margin-bottom: 24px; }
.replies-section h3 { font-size: 16px; font-weight: 600; margin-bottom: 12px; }
.reply-item { background: #e8f5f3; border-radius: 12px; padding: 16px; margin-bottom: 8px; }
.reply-header { display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 13px; }
.reply-time { color: #999; }
.reply-content { font-size: 14px; line-height: 1.6; white-space: pre-wrap; }
.no-reply { text-align: center; color: #999; padding: 16px; }
.reply-form textarea { width: 100%; padding: 12px; border: 1px solid #e0e0e0; border-radius: 12px; font-size: 14px; outline: none; resize: vertical; margin-bottom: 12px; }
.reply-form textarea:focus { border-color: #0d9488; }
.submit-btn { width: 100%; padding: 12px; background: #0d9488; color: white; border: none; border-radius: 12px; font-size: 15px; cursor: pointer; }
.submit-btn:disabled { opacity: 0.5; }
</style>
