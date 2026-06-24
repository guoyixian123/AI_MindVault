<template>
  <div class="page">
    <nav class="page-nav">
      <div class="nav-inner">
        <a href="#" @click.prevent="goBack" class="back-link">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
          返回列表
        </a>
        <h1>帖子详情</h1>
        <div class="nav-spacer"></div>
      </div>
    </nav>

    <div class="page-content">
      <div v-if="post" class="detail-card">
        <div class="post-header">
          <span class="post-status" :class="post.status.toLowerCase()">{{ statusText(post.status) }}</span>
          <span class="post-time">{{ formatTime(post.createdAt) }}</span>
        </div>
        <h2 class="post-title">{{ post.title }}</h2>
        <div class="post-content">{{ post.content }}</div>

        <!-- 医生回复 -->
        <div class="replies-section">
          <h3>医生回复 ({{ replies.length }})</h3>
          <div v-for="reply in replies" :key="reply.id" class="reply-card">
            <div class="reply-header">
              <span class="doctor-name">{{ reply.doctorName || '医生' }} 回复</span>
              <span class="reply-time">{{ formatTime(reply.createdAt) }}</span>
            </div>
            <div class="reply-content">{{ reply.content }}</div>
          </div>
          <div v-if="replies.length === 0" class="no-reply">暂无医生回复，请耐心等待...</div>
        </div>
      </div>
      <div v-else class="loading">加载中...</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../utils/api'

const route = useRoute()
const router = useRouter()
const post = ref(null)
const replies = ref([])

onMounted(async () => {
  const id = route.params.id
  try {
    const { data } = await api.get(`/api/consultation/posts/${id}`)
    if (data.code === 200) {
      post.value = data.data.post
      replies.value = data.data.replies || []
    }
  } catch (e) {
    console.error('Failed to load post:', e)
  }
})

function goBack() {
  // 如果有历史记录则返回，否则跳转到列表页
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/consultation')
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
.page { min-height: 100vh; background: var(--bg-primary); }

.page-nav {
  background: var(--color-white);
  border-bottom: 1px solid var(--border-light);
  padding: 0 32px;
  height: 60px;
  position: sticky;
  top: 0;
  z-index: 10;
}

.nav-inner {
  max-width: 700px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-link {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--text-muted);
  transition: color var(--transition-fast);
  white-space: nowrap;
}

.back-link:hover { color: var(--color-forest); }

.page-nav h1 {
  font-size: 18px;
  font-family: var(--font-body);
  font-weight: 600;
  flex: 1;
  text-align: center;
}

.nav-spacer {
  width: 80px; /* 与返回按钮宽度相近，保持平衡 */
}

.page-content {
  max-width: 700px;
  margin: 32px auto;
  padding: 0 24px;
}

.detail-card {
  background: var(--color-white);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 32px;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.post-status {
  padding: 3px 10px;
  border-radius: var(--radius-xs);
  font-size: 12px;
  font-weight: 500;
}

.post-status.pending {
  background: #fef3c7;
  color: #92400e;
}

.post-status.replied {
  background: #d1fae5;
  color: #065f46;
}

.post-status.closed {
  background: var(--color-paper);
  color: var(--text-muted);
}

.post-time {
  font-size: 13px;
  color: var(--text-muted);
}

.post-title {
  font-size: 1.4rem;
  margin-bottom: 16px;
}

.post-content {
  font-size: 15px;
  line-height: 1.8;
  color: var(--text-primary);
  padding: 20px;
  background: var(--color-paper);
  border-radius: var(--radius-md);
  margin-bottom: 32px;
  white-space: pre-wrap;
}

.replies-section h3 {
  font-size: 1.1rem;
  margin-bottom: 16px;
}

.reply-card {
  background: rgba(45, 90, 61, 0.06);
  border-radius: var(--radius-md);
  padding: 20px;
  margin-bottom: 12px;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.doctor-name {
  font-weight: 600;
  color: var(--color-forest);
}

.reply-time {
  font-size: 12px;
  color: var(--text-muted);
}

.reply-content {
  font-size: 15px;
  line-height: 1.7;
  color: var(--text-primary);
  white-space: pre-wrap;
}

.no-reply {
  text-align: center;
  color: var(--text-muted);
  padding: 32px;
}

.loading {
  text-align: center;
  padding: 48px;
  color: var(--text-muted);
}
</style>
