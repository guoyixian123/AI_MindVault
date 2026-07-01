<template>
  <div class="page">
    <nav class="page-nav">
      <a href="#" class="back-link" @click.prevent="$router.back()">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
        返回
      </a>
      <h1>体检报告解读</h1>
      <router-link to="/history?filter=report" class="history-link">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
        历史
      </router-link>
    </nav>

    <div class="page-content">
      <!-- 输入报告 -->
      <div class="section-card">
        <h2>粘贴体检报告内容</h2>
        <p class="desc">将您的体检报告文字内容粘贴到下方，AI 将为您解读各项指标</p>
        <form @submit.prevent="analyzeReport">
          <div class="form-group">
            <label>报告类型</label>
            <div class="type-tags">
              <button v-for="t in reportTypes" :key="t" type="button"
                class="type-tag" :class="{ active: reportType === t }"
                @click="reportType = t">
                {{ t }}
              </button>
            </div>
          </div>
          <div class="form-group">
            <label>报告内容 *</label>
            <textarea
              v-model="reportContent"
              placeholder="请粘贴体检报告的文字内容...&#10;&#10;例如：&#10;血常规：白细胞 5.2×10^9/L，红细胞 4.8×10^12/L，血红蛋白 145g/L...&#10;肝功能：ALT 25U/L，AST 28U/L...&#10;血糖：空腹血糖 5.6mmol/L..."
              rows="12"
              required
            ></textarea>
          </div>
          <button type="submit" class="submit-btn" :disabled="!reportContent.trim() || analyzing">
            {{ analyzing ? 'AI 解读中...' : '开始解读' }}
          </button>
        </form>
      </div>

      <!-- 加载动画 -->
      <AILoadingIndicator v-if="analyzing && !result" label="A.R.I.A 正在解读报告" />

      <!-- 解读结果 -->
      <div v-if="result" class="section-card result-card">
        <div class="result-header">
          <h2>AI 解读结果</h2>
          <button class="copy-btn" @click="copyResult" title="复制">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>
          </button>
        </div>
        <div class="result-content markdown-body" :class="{ 'typing-cursor': isStreaming }">
          <div v-html="renderMarkdown(displayedContent)"></div>
        </div>
        <div class="result-actions">
          <button class="action-btn" @click="reset">继续解读其他报告</button>
        </div>
      </div>

      <!-- 历史报告 -->
      <div v-if="history.length > 0 && !result" class="section-card">
        <div class="section-row">
          <h2>历史解读</h2>
        </div>
        <div class="history-list">
          <div v-for="item in history" :key="item.id" class="history-item" @click="viewHistory(item)">
            <div class="history-top">
              <span class="history-type">{{ item.reportType }}</span>
              <span class="history-time">{{ formatTime(item.createdAt) }}</span>
            </div>
            <div class="history-preview">{{ item.reportContent?.substring(0, 50) }}...</div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { renderMarkdown } from '../utils/markedConfig'
import { useChatWebSocket } from '../utils/websocket'
import { useStreamingText } from '../composables/useStreamingText'
import api from '../utils/api'
import AILoadingIndicator from '../components/AILoadingIndicator.vue'

const reportType = ref('综合体检报告')
const reportContent = ref('')
const result = ref('')
const rawResult = ref('')
const analyzing = ref(false)
const history = ref([])

const chatWs = useChatWebSocket()
const { displayedContent, isStreaming, appendText, flush, resetStreaming } = useStreamingText()

const onMessage = (data) => {
  rawResult.value += data
  result.value += data
  appendText(data)
}

const onDone = () => {
  const wait = setInterval(() => {
    if (!isStreaming.value) {
      clearInterval(wait)
      flush(rawResult.value)
      analyzing.value = false
    }
  }, 50)
}

const onError = (error) => {
  console.error('WebSocket error:', error)
  result.value = '解读失败，请确保后端服务正在运行。'
  analyzing.value = false
}

onMounted(async () => {
  chatWs.removeCallback('onMessage')
  chatWs.removeCallback('onDone')
  chatWs.removeCallback('onError')

  chatWs.on('message', onMessage)
  chatWs.on('done', onDone)
  chatWs.on('error', onError)

  chatWs.connect()
  await loadHistory()
})

onUnmounted(() => {
  chatWs.removeCallback('onMessage')
  chatWs.removeCallback('onDone')
  chatWs.removeCallback('onError')
})

async function loadHistory() {
  try {
    const { data } = await api.get('/api/health/reports')
    if (data.code === 200) {
      history.value = (data.data || []).filter(r => r.aiAnalysis)
    }
  } catch (e) {
    console.error('Failed to load history:', e)
  }
}

function viewHistory(item) {
  reportType.value = item.reportType
  reportContent.value = item.reportContent
  result.value = item.aiAnalysis
  rawResult.value = item.aiAnalysis
  flush(item.aiAnalysis)
}

const reportTypes = ['综合体检报告', '血常规', '肝功能', '肾功能', '血脂', '血糖', '彩超', '其他']

async function analyzeReport() {
  if (!reportContent.value.trim()) return

  analyzing.value = true
  result.value = ''
  rawResult.value = ''
  resetStreaming()

  try {
    // 先保存报告
    const uploadRes = await api.post('/api/health/reports/upload', {
      reportType: reportType.value,
      reportContent: reportContent.value
    })
    const reportId = uploadRes.data?.data?.id

    // 通过 WebSocket 发送获取 AI 解读
    chatWs.send('', {
      memoryId: 'report-' + Date.now(),
      scenario: 'report',
      reportId: reportId,
      reportType: reportType.value,
      reportContent: reportContent.value
    })
  } catch (e) {
    console.error('体检报告解读失败:', e)
    result.value = '解读失败，请确保后端服务正在运行。'
    analyzing.value = false
  }
}

function reset() {
  reportContent.value = ''
  result.value = ''
  rawResult.value = ''
  resetStreaming()
}

function copyResult() {
  navigator.clipboard.writeText(rawResult.value || result.value)
}

function formatTime(timestamp) {
  if (!timestamp) return ''
  return new Date(timestamp).toLocaleDateString('zh-CN')
}

</script>

<style scoped>
.page { min-height: 100vh; background: var(--bg-primary); }

.page-nav {
  background: var(--color-white);
  border-bottom: 1px solid var(--border-light);
  padding: 0 32px;
  height: 60px;
  display: flex;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 10;
}

.back-link {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: var(--text-muted);
  transition: color var(--transition-fast);
  width: 80px;
}
.back-link:hover { color: var(--color-forest); }

.page-nav h1 {
  flex: 1;
  text-align: center;
  font-size: 18px;
  font-family: var(--font-body);
  font-weight: 600;
}

.history-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: none;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text-muted);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.history-btn:hover {
  border-color: var(--color-forest);
  color: var(--color-forest);
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

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h2 {
  font-size: 1.2rem;
}

.section-card h2 {
  font-size: 1.2rem;
  margin-bottom: 8px;
}

.close-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-paper);
  border: none;
  border-radius: var(--radius-xs);
  font-size: 18px;
  color: var(--text-muted);
  cursor: pointer;
}

.desc {
  font-size: 14px;
  color: var(--text-muted);
  margin-bottom: 20px;
}

.type-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.type-tag {
  padding: 6px 14px;
  border: 1px solid var(--border-light);
  border-radius: 16px;
  background: var(--color-white);
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.type-tag:hover { border-color: var(--color-forest); color: var(--color-forest); }
.type-tag.active { background: var(--color-forest); color: white; border-color: var(--color-forest); }

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.form-group textarea {
  width: 100%;
  padding: 14px;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-sm);
  font-size: 14px;
  line-height: 1.7;
  color: var(--text-primary);
  outline: none;
  resize: vertical;
  font-family: var(--font-body);
  transition: border-color var(--transition-fast);
}

.form-group textarea:focus {
  border-color: var(--color-forest);
}

.form-group textarea::placeholder {
  color: var(--color-mist);
}

.submit-btn {
  width: 100%;
  padding: 14px;
  background: var(--color-forest);
  color: var(--color-white);
  border: none;
  border-radius: var(--radius-sm);
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.submit-btn:hover:not(:disabled) { background: var(--color-forest-dark); }
.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.result-card { animation: fadeInUp 0.3s ease; }

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.result-content {
  font-size: 14px;
  line-height: 1.8;
  color: var(--text-primary);
  padding: 20px;
  background: var(--color-paper);
  border-radius: var(--radius-md);
  position: relative;
}

.result-actions {
  margin-top: 16px;
  display: flex;
  gap: 12px;
}

.action-btn {
  flex: 1;
  padding: 12px;
  background: var(--color-paper);
  border: none;
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text-secondary);
  cursor: pointer;
}

.action-btn:hover { background: var(--color-linen); }

.section-row {
  margin-bottom: 16px;
}

.section-row h2 {
  font-size: 1.2rem;
}

.history-list { display: flex; flex-direction: column; gap: 10px; }

.history-item {
  padding: 14px 16px;
  background: var(--color-white);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.history-item:hover {
  border-color: var(--color-forest);
  box-shadow: var(--shadow-sm);
}

.history-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.history-type {
  font-weight: 500;
  font-size: 14px;
  color: var(--text-primary);
}

.history-time {
  font-size: 12px;
  color: var(--text-muted);
}

.history-preview {
  font-size: 13px;
  color: var(--text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.empty-history {
  text-align: center;
  padding: 24px;
}

.empty-history p {
  color: var(--text-muted);
  font-size: 14px;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 767px) {
  .page-content { padding: 0 16px; }
  .section-card { padding: 20px; }
}
</style>
