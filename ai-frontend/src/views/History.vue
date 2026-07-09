<template>
  <div class="page">
    <nav class="page-nav">
      <a href="#" class="back-link" @click.prevent="$router.back()">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
        返回
      </a>
      <h1>查询记录</h1>
      <div class="nav-spacer"></div>
    </nav>

    <div class="page-content">
      <!-- 筛选标签 -->
      <div class="filter-tabs">
        <button class="tab-btn" :class="{ active: filter === 'all' }" @click="filter = 'all'">全部</button>
        <button class="tab-btn" :class="{ active: filter === 'medicine' }" @click="filter = 'medicine'">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="6" y="3" width="12" height="18" rx="3"/><path d="M9 9h6M12 7.5v3"/></svg>
          用药咨询
        </button>
        <button class="tab-btn" :class="{ active: filter === 'disease' }" @click="filter = 'disease'">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="4" y="4" width="16" height="16" rx="2"/><path d="M4 8h16M8 4v16"/></svg>
          疾病科普
        </button>
        <button class="tab-btn" :class="{ active: filter === 'symptom' }" @click="filter = 'symptom'">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>
          症状自查
        </button>
        <button class="tab-btn" :class="{ active: filter === 'report' }" @click="filter = 'report'">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><path d="M14 2v6h6"/></svg>
          体检报告
        </button>
      </div>

      <!-- 记录列表 -->
      <div v-if="filteredRecords.length > 0" class="record-list">
        <div v-for="record in filteredRecords" :key="record.sessionId" class="record-card" @click="openRecord(record)">
          <div class="record-header">
            <span class="record-type" :class="record.type">{{ typeText(record.type) }}</span>
            <span class="record-time">{{ formatTime(record.updatedAt) }}</span>
          </div>
          <div class="record-title">{{ record.title }}</div>
          <div class="record-preview">{{ record.preview }}</div>
        </div>
      </div>

      <div v-else class="empty-state">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" opacity="0.3">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><path d="M14 2v6h6"/>
        </svg>
        <p>暂无查询记录</p>
      </div>

      <!-- 详情弹窗 -->
      <div v-if="selectedRecord" class="modal-overlay" @click.self="selectedRecord = null">
        <div class="modal">
          <div class="modal-header">
            <div>
              <span class="record-type" :class="selectedRecord.type">{{ typeText(selectedRecord.type) }}</span>
              <h2>{{ selectedRecord.title }}</h2>
            </div>
            <button class="close-btn" @click="selectedRecord = null">×</button>
          </div>
          <div class="modal-content">
            <div v-for="(msg, i) in selectedRecord.messages" :key="i" class="msg-item" :class="msg.role">
              <div class="msg-role">{{ msg.role === 'user' ? '我' : 'A.I.A' }}</div>
              <div class="msg-text markdown-body" v-html="renderMarkdown(msg.content)"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { renderMarkdown } from '../utils/markedConfig'
import { useRoute } from 'vue-router'
import api from '../utils/api'

const route = useRoute()
const filter = ref('all')
const records = ref([])
const selectedRecord = ref(null)

const filteredRecords = computed(() => {
  if (filter.value === 'all') return records.value
  return records.value.filter(r => r.type === filter.value)
})

onMounted(async () => {
  // 从 URL 参数获取筛选条件
  if (route.query.filter) {
    filter.value = route.query.filter
  }
  await loadHistory()
})

async function loadHistory() {
  try {
    // 加载会话记录
    const { data } = await api.get('/api/sessions')
    if (data.code === 200) {
      const sessions = data.data || []
      const historySessions = sessions.filter(s =>
        s.id.startsWith('medicine-') ||
        s.id.startsWith('disease-') ||
        s.id.startsWith('symptom-')
      )

      const result = []
      for (const session of historySessions) {
        try {
          const msgRes = await api.get(`/api/sessions/${session.id}/messages`)
          if (msgRes.data.code === 200 && msgRes.data.data?.length > 0) {
            const messages = msgRes.data.data
            const type = session.id.startsWith('medicine-') ? 'medicine' :
                        session.id.startsWith('disease-') ? 'disease' : 'symptom'
            const aiMsg = messages.find(m => m.role === 'assistant')

            result.push({
              sessionId: session.id,
              type,
              title: session.title,
              preview: aiMsg?.content ? aiMsg.content.substring(0, 80) + '...' : '',
              updatedAt: session.updatedAt,
              messages
            })
          }
        } catch (e) {
          console.error('Failed to load messages:', e)
        }
      }

      // 加载体检报告记录
      try {
        const reportRes = await api.get('/api/health/reports')
        if (reportRes.data.code === 200) {
          const reports = (reportRes.data.data || []).filter(r => r.aiAnalysis)
          for (const report of reports) {
            result.push({
              sessionId: 'report-' + report.id,
              type: 'report',
              title: report.reportType,
              preview: report.aiAnalysis ? report.aiAnalysis.substring(0, 80) + '...' : '',
              updatedAt: report.createdAt,
              messages: [
                { role: 'user', content: report.reportContent },
                { role: 'assistant', content: report.aiAnalysis }
              ]
            })
          }
        }
      } catch (e) {
        console.error('Failed to load reports:', e)
      }

      // 按时间倒序
      result.sort((a, b) => b.updatedAt - a.updatedAt)
      records.value = result
    }
  } catch (e) {
    console.error('Failed to load history:', e)
  }
}

function openRecord(record) {
  selectedRecord.value = record
}

function typeText(type) {
  return { medicine: '用药咨询', disease: '疾病科普', symptom: '症状自查', report: '体检报告' }[type] || type
}

function formatTime(timestamp) {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return date.toLocaleDateString('zh-CN')
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

.back-link {
  display: flex; align-items: center; gap: 4px;
  font-size: 14px; color: var(--text-muted); transition: color var(--transition-fast);
  min-width: 80px;
}
.back-link:hover { color: var(--color-forest); }

.page-nav h1 { flex: 1; text-align: center; font-size: 18px; font-family: var(--font-body); font-weight: 600; }
.nav-spacer { min-width: 80px; }

.page-content {
  max-width: 800px;
  margin: 24px auto;
  padding: 0 24px;
}

.filter-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.tab-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: 1px solid var(--border-light);
  border-radius: 20px;
  background: var(--color-white);
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.tab-btn:hover { border-color: var(--color-forest); color: var(--color-forest); }
.tab-btn.active { background: var(--color-forest); color: var(--color-white); border-color: var(--color-forest); }

.record-list { display: flex; flex-direction: column; gap: 12px; }

.record-card {
  background: var(--color-white);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: 16px 20px;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.record-card:hover { border-color: var(--color-forest); box-shadow: var(--shadow-sm); }

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.record-type {
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
}

.record-type.medicine { background: #e8f5f3; color: var(--color-forest); }
.record-type.disease { background: #fef3c7; color: #92400e; }
.record-type.symptom { background: #fee2e2; color: #991b1b; }
.record-type.report { background: #dbeafe; color: #1e40af; }

.record-time { font-size: 12px; color: var(--text-muted); }

.record-title {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.record-preview {
  font-size: 13px;
  color: var(--text-muted);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-muted);
}

.empty-state p { margin-top: 12px; font-size: 15px; }

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  padding: 20px;
}

.modal {
  background: var(--color-white);
  border-radius: var(--radius-lg);
  width: 100%;
  max-width: 700px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 24px 24px 16px;
  border-bottom: 1px solid var(--border-light);
}

.modal-header h2 {
  font-size: 18px;
  font-weight: 600;
  margin-top: 8px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 28px;
  cursor: pointer;
  color: var(--text-muted);
  line-height: 1;
}

.modal-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.msg-item {
  margin-bottom: 16px;
}

.msg-role {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-muted);
  margin-bottom: 4px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.msg-item.user .msg-role { color: var(--color-forest); }
.msg-item.assistant .msg-role { color: var(--color-terracotta); }

.msg-text {
  font-size: 14px;
  line-height: 1.7;
  color: var(--text-primary);
  padding: 12px 16px;
  border-radius: var(--radius-md);
  background: var(--color-paper);
}

.msg-item.user .msg-text {
  background: rgba(45, 90, 61, 0.06);
}

@media (max-width: 767px) {
  .page-content { padding: 0 16px; }
  .filter-tabs { gap: 6px; }
  .tab-btn { padding: 6px 12px; font-size: 12px; }
}
</style>
