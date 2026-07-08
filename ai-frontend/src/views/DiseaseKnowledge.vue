<template>
  <div class="page">
        <nav class="page-nav">
      <a href="#" class="back-link" @click.prevent="$router.back()">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
        返回
      </a>
      <h1>疾病科普</h1>
      <router-link to="/history" class="history-link">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
        历史
      </router-link>
    </nav>


    <div class="page-content">
      <!-- 功能选择 -->
      <div class="mode-tabs">
        <button v-for="tab in tabs" :key="tab.value"
          class="tab-btn" :class="{ active: mode === tab.value }"
          @click="mode = tab.value">
          {{ tab.label }}
        </button>
      </div>

      <!-- 疾病知识科普 -->
      <div v-if="mode === 'knowledge'" class="consult-card">
        <h2>疾病知识科普</h2>
        <p class="desc">输入疾病名称，获取通俗易懂的科普解读</p>
        <div class="form-group">
          <input v-model="diseaseName" type="text" placeholder="请输入疾病名称，如：高血压、糖尿病" />
        </div>
        <div class="hot-tags">
          <span class="tag-label">热门：</span>
          <button v-for="tag in hotDiseases" :key="tag" class="hot-tag" @click="diseaseName = tag">{{ tag }}</button>
        </div>
        <button class="submit-btn" @click="queryDisease" :disabled="!diseaseName.trim() || loading">
          {{ loading ? '查询中...' : '开始科普' }}
        </button>
      </div>

      <!-- 相似病症鉴别 -->
      <div v-if="mode === 'compare'" class="consult-card">
        <h2>相似病症鉴别</h2>
        <p class="desc">对比两种易混淆的病症，明确核心差异</p>
        <div class="form-group">
          <input v-model="diseaseA" type="text" placeholder="病症A，如：普通感冒" />
        </div>
        <div class="form-group">
          <input v-model="diseaseB" type="text" placeholder="病症B，如：流感" />
        </div>
        <div class="hot-tags">
          <span class="tag-label">常见对比：</span>
          <button v-for="pair in compareExamples" :key="pair" class="hot-tag" @click="setCompare(pair)">{{ pair }}</button>
        </div>
        <button class="submit-btn" @click="compareDiseases" :disabled="!diseaseA.trim() || !diseaseB.trim() || loading">
          {{ loading ? '鉴别中...' : '开始鉴别' }}
        </button>
      </div>

      <!-- 健康谣言辟谣 -->
      <div v-if="mode === 'debunk'" class="consult-card">
        <h2>健康谣言辟谣</h2>
        <p class="desc">输入您看到的健康说法，AI帮您科学验证</p>
        <div class="form-group">
          <textarea v-model="claim" placeholder="请输入您想验证的健康说法，如：喝醋能软化血管" rows="3"></textarea>
        </div>
        <div class="hot-tags">
          <span class="tag-label">常见谣言：</span>
          <button v-for="tag in hotClaims" :key="tag" class="hot-tag" @click="claim = tag">{{ tag }}</button>
        </div>
        <button class="submit-btn" @click="debunkClaim" :disabled="!claim.trim() || loading">
          {{ loading ? '验证中...' : '科学辟谣' }}
        </button>
      </div>

      <!-- 对话记录 -->
      <div v-if="messages.length" class="result-card">
        <div class="result-header">
          <h3>{{ resultTitle }}</h3>
          <button class="copy-btn" @click="copyResult" title="复制">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>
          </button>
        </div>

        <div v-for="(msg, i) in messages" :key="i" class="chat-block">
          <div v-if="msg.role === 'user'" class="chat-user-msg">
            <div class="chat-user-label">你的提问</div>
            <div class="chat-user-text">{{ msg.content }}</div>
          </div>
          <div v-else class="chat-ai-msg">
            <div class="chat-ai-label">A.R.I.A</div>
            <div class="chat-ai-text markdown-body" v-html="renderMarkdown(msg.content)"></div>
          </div>
        </div>

        <div v-if="loading" class="chat-ai-msg">
          <AILoadingIndicator v-if="!displayedContent" label="A.R.I.A 正在查询中" />
          <template v-else>
            <div class="chat-ai-label">A.R.I.A</div>
            <div class="chat-ai-text markdown-body typing-cursor" v-html="renderMarkdown(displayedContent)"></div>
          </template>
        </div>

        <div v-if="!loading && messages.length" class="follow-up">
          <input v-model="followUpText" type="text" placeholder="继续追问..." @keyup.enter="sendFollowUp" />
          <button class="btn-send" @click="sendFollowUp" :disabled="!followUpText.trim()">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useChatWebSocket } from '../utils/streaming'
import { renderMarkdown } from '../utils/markedConfig'
import { useStreamingText } from '../composables/useStreamingText'
import AILoadingIndicator from '../components/AILoadingIndicator.vue'

const mode = ref('knowledge')
const loading = ref(false)
const diseaseName = ref('')
const diseaseA = ref('')
const diseaseB = ref('')
const claim = ref('')
const result = ref('')
const resultTitle = ref('查询结果')
const followUpText = ref('')
const messages = ref([])
let sessionId = ''
let currentReply = ''

const chatWs = useChatWebSocket()
const { displayedContent, isStreaming, appendText, flush, resetStreaming } = useStreamingText()

const onMessage = (data) => {
  currentReply += data
  result.value += data
  appendText(data)
}

const onDone = () => {
  const wait = setInterval(() => {
    if (!isStreaming.value) {
      clearInterval(wait)
      flush(currentReply)
      if (currentReply) {
        messages.value.push({ role: 'assistant', content: currentReply })
      }
      loading.value = false
      currentReply = ''
    }
  }, 50)
}

const onError = (error) => {
  console.error('请求错误:', error)
  result.value = '查询失败，请确保后端服务正在运行。'
  loading.value = false
}

onMounted(() => {
  chatWs.removeCallback('onMessage')
  chatWs.removeCallback('onDone')
  chatWs.removeCallback('onError')

  chatWs.on('message', onMessage)
  chatWs.on('done', onDone)
  chatWs.on('error', onError)

  chatWs.connect()
})

onUnmounted(() => {
  chatWs.removeCallback('onMessage')
  chatWs.removeCallback('onDone')
  chatWs.removeCallback('onError')
})

const tabs = [
  { value: 'knowledge', label: '疾病科普' },
  { value: 'compare', label: '病症鉴别' },
  { value: 'debunk', label: '谣言辟谣' }
]

const hotDiseases = ['高血压', '糖尿病', '冠心病', '胃炎', '颈椎病', '痛风']
const compareExamples = ['普通感冒 vs 流感', '胃炎 vs 胃溃疡', '心梗 vs 心绞痛']
const hotClaims = ['喝醋能软化血管', '饭后百步走活到九十九', '发烧要捂汗']

function setCompare(pair) {
  const [a, b] = pair.split(' vs ')
  diseaseA.value = a
  diseaseB.value = b
}

function queryDisease() {
  resultTitle.value = '疾病科普：' + diseaseName.value
  callApi({
    subScenario: 'knowledge',
    diseaseName: diseaseName.value
  })
}

function compareDiseases() {
  resultTitle.value = '病症鉴别'
  callApi({
    subScenario: 'compare',
    diseaseName: diseaseA.value,
    compareWith: diseaseB.value
  })
}

function debunkClaim() {
  resultTitle.value = '谣言辟谣'
  callApi({
    subScenario: 'debunk',
    claim: claim.value
  })
}

function callApi(body) {
  loading.value = true
  result.value = ''
  currentReply = ''
  messages.value = []
  sessionId = 'disease-' + Date.now()
  resetStreaming()

  const label = body.subScenario === 'knowledge' ? body.diseaseName
    : body.subScenario === 'compare' ? `${body.diseaseName} vs ${body.compareWith}`
    : body.claim
  messages.value.push({ role: 'user', content: label })

  chatWs.send('', {
    memoryId: sessionId,
    scenario: 'disease',
    ...body
  })
}

function sendFollowUp() {
  const text = followUpText.value.trim()
  if (!text || loading.value) return

  loading.value = true
  result.value = ''
  currentReply = ''
  resetStreaming()

  messages.value.push({ role: 'user', content: text })
  followUpText.value = ''

  chatWs.send('', {
    memoryId: sessionId,
    scenario: 'chat',
    message: text
  })
}

function copyResult() {
  const last = messages.value.filter(m => m.role === 'assistant')
  navigator.clipboard.writeText(last.length ? last[last.length - 1].content : result.value)
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
  width: 80px;
  font-size: 14px;
  color: var(--text-muted);
  transition: color var(--transition-fast);
}

.back-link:hover { color: var(--color-forest); }

.nav-spacer {
  min-width: 80px;
}

.history-link {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-muted);
  transition: color var(--transition-fast);
}
.history-link:hover {
  color: var(--color-forest);
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

.mode-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.tab-btn {
  padding: 10px 20px;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-sm);
  background: var(--color-white);
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.tab-btn:hover {
  border-color: var(--color-forest);
  color: var(--color-forest);
}

.tab-btn.active {
  background: var(--color-forest);
  color: var(--color-white);
  border-color: var(--color-forest);
}

.consult-card {
  background: var(--color-white);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 32px;
  margin-bottom: 24px;
}

.consult-card h2 {
  font-size: 1.4rem;
  margin-bottom: 8px;
}

.desc {
  color: var(--text-muted);
  font-size: 14px;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-sm);
  font-size: 15px;
  color: var(--text-primary);
  outline: none;
  resize: vertical;
  transition: border-color var(--transition-fast);
  background: var(--color-white);
}

.form-group input:focus,
.form-group textarea:focus {
  border-color: var(--color-forest);
}

.hot-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 16px;
  align-items: center;
}

.tag-label {
  font-size: 13px;
  color: var(--text-muted);
}

.hot-tag {
  padding: 6px 14px;
  background: var(--color-paper);
  border: none;
  border-radius: var(--radius-sm);
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.hot-tag:hover {
  background: rgba(45, 90, 61, 0.1);
  color: var(--color-forest);
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

.result-card {
  background: var(--color-white);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 32px;
  margin-top: 24px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.result-header h3 {
  font-size: 1.1rem;
}

.result-content {
  font-size: 15px;
  line-height: 1.8;
  color: var(--text-primary);
  padding: 24px;
  background: var(--color-paper);
  border-radius: var(--radius-md);
  position: relative;
}

/* 对话块 */
.chat-block { margin-bottom: 20px; }
.chat-user-msg { margin-bottom: 16px; }
.chat-user-label { font-size: 12px; font-weight: 600; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 6px; }
.chat-user-text { font-size: 14px; line-height: 1.6; color: var(--text-secondary); padding: 12px 16px; background: var(--color-paper); border-radius: var(--radius-md); }
.chat-ai-msg { margin-bottom: 8px; }
.chat-ai-label { font-size: 12px; font-weight: 600; color: var(--color-forest); text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 6px; }
.chat-ai-text { font-size: 15px; line-height: 1.8; color: var(--text-primary); padding: 20px; background: var(--color-paper); border-radius: var(--radius-md); }
.follow-up { display: flex; gap: 8px; margin-top: 20px; }
.follow-up input { flex: 1; padding: 12px 16px; border: 1px solid var(--border-light); border-radius: var(--radius-sm); font-size: 15px; color: var(--text-primary); outline: none; transition: border-color var(--transition-fast); }
.follow-up input:focus { border-color: var(--color-forest); }
.btn-send { width: 44px; height: 44px; display: flex; align-items: center; justify-content: center; background: var(--color-forest); color: var(--color-white); border: none; border-radius: var(--radius-sm); cursor: pointer; transition: background var(--transition-fast); }
.btn-send:hover:not(:disabled) { background: var(--color-forest-dark); }
.btn-send:disabled { opacity: 0.4; cursor: not-allowed; }

.typing-cursor::after {
  content: '';
  display: inline-block;
  width: 2px;
  height: 18px;
  background: var(--color-forest);
  margin-left: 2px;
  vertical-align: text-bottom;
  animation: blink 0.8s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}
</style>
