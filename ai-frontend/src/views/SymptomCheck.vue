<template>
  <div class="page">
    <nav class="page-nav">
      <a href="#" class="back-link" @click.prevent="$router.back()">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
        返回
      </a>
      <h1>症状自查</h1>
      <router-link to="/history" class="history-link">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
        历史
      </router-link>
    </nav>

    <div class="page-content">
      <!-- Step 1: Category -->
      <div v-if="step === 1" class="step-card">
        <div class="step-header">
          <span class="step-num">01</span>
          <h2>选择症状分类</h2>
        </div>
        <div class="category-grid">
          <button v-for="cat in categories" :key="cat.value"
            class="category-btn" :class="{ active: form.category === cat.value }"
            @click="form.category = cat.value">
            <div class="cat-icon" v-html="cat.icon"></div>
            <span>{{ cat.label }}</span>
          </button>
        </div>
        <button class="btn-primary" :disabled="!form.category" @click="step = 2">下一步</button>
      </div>

      <!-- Step 2: Details -->
      <div v-if="step === 2" class="step-card">
        <div class="step-header">
          <span class="step-num">02</span>
          <h2>描述症状详情</h2>
        </div>
        <div class="form-group">
          <label>症状描述 *</label>
          <textarea v-model="form.description" placeholder="请详细描述您的症状..." rows="4"></textarea>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>持续时间（天）</label>
            <input v-model.number="form.duration" type="number" min="1" placeholder="天数" />
          </div>
          <div class="form-group">
            <label>痛感等级</label>
            <div class="pain-row">
              <input v-model.number="form.painLevel" type="range" min="1" max="10" class="pain-slider" />
              <span class="pain-value" :class="painClass">{{ form.painLevel || '--' }}</span>
            </div>
          </div>
        </div>
        <div class="form-group">
          <label>伴随症状</label>
          <input v-model="form.accompanying" type="text" placeholder="如：发热、头晕、恶心等" />
        </div>
        <div class="form-group">
          <label>诱发因素</label>
          <input v-model="form.triggers" type="text" placeholder="如：受凉、劳累、饮食不当等" />
        </div>
        <div class="btn-row">
          <button class="btn-ghost" @click="step = 1">上一步</button>
          <button class="btn-primary" @click="step = 3">下一步</button>
        </div>
      </div>

      <!-- Step 3: Personal -->
      <div v-if="step === 3" class="step-card">
        <div class="step-header">
          <span class="step-num">03</span>
          <h2>补充个人信息</h2>
        </div>
        <div class="form-group">
          <label>人群类型</label>
          <div class="radio-row">
            <label v-for="p in populations" :key="p.value" class="radio-label">
              <input type="radio" v-model="form.population" :value="p.value" />
              <span class="radio-box">{{ p.label }}</span>
            </label>
          </div>
        </div>
        <div class="form-group">
          <label>既往病史</label>
          <textarea v-model="form.history" placeholder="如有请填写..." rows="2"></textarea>
        </div>
        <div class="form-group">
          <label>药物过敏史</label>
          <input v-model="form.allergies" type="text" placeholder="如有请填写" />
        </div>

        <div v-if="isHighRisk" class="alert-danger">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
          <div>
            <strong>高危预警</strong>
            <p>痛感等级≥8，可能存在较高风险，建议立即前往急诊科就诊！</p>
          </div>
        </div>

        <div class="btn-row">
          <button class="btn-ghost" @click="step = 2">上一步</button>
          <button class="btn-primary" @click="submitCheck" :disabled="loading">
            {{ loading ? '分析中...' : '开始自查' }}
          </button>
        </div>
      </div>

      <!-- Step 4: Result -->
      <div v-if="step === 4" class="step-card">
        <div class="step-header">
          <span class="step-num">结果</span>
          <h2>自查分析</h2>
        </div>

        <!-- 已完成的对话记录 -->
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

        <!-- 当前正在流式输出 -->
        <div v-if="loading" class="chat-ai-msg">
          <AILoadingIndicator v-if="!displayedContent" label="A.R.I.A 正在分析症状" />
          <template v-else>
            <div class="chat-ai-label">A.R.I.A</div>
            <div class="chat-ai-text markdown-body typing-cursor" v-html="renderMarkdown(displayedContent)"></div>
          </template>
        </div>

        <!-- 追问输入框 -->
        <div v-if="!loading && messages.length" class="follow-up">
          <input v-model="followUpText" type="text" placeholder="继续追问..." @keyup.enter="sendFollowUp" />
          <button class="btn-send" @click="sendFollowUp" :disabled="!followUpText.trim()">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
          </button>
        </div>

        <div class="btn-row">
          <button class="btn-ghost" @click="resetForm">重新自查</button>
          <router-link to="/consultation" class="btn-primary">咨询医生</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useChatWebSocket } from '../utils/streaming'
import { renderMarkdown } from '../utils/markedConfig'
import { useStreamingText } from '../composables/useStreamingText'
import { toast } from '../composables/useToast'
import AILoadingIndicator from '../components/AILoadingIndicator.vue'

const step = ref(1)
const loading = ref(false)
const result = ref('')
const followUpText = ref('')
const messages = ref([])       // 多轮对话记录 [{role, content}]
let sessionId = ''             // 首次提交时生成，追问时复用
let currentReply = ''          // 当前正在流式接收的回复

const form = ref({
  category: '', description: '', duration: null, painLevel: 5,
  accompanying: '', triggers: '', history: '', allergies: '', population: '成人'
})

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
      // 将本轮对话保存到 messages
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
  toast.error('自查失败，请稍后重试')
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

const categories = [
  { value: '发热', label: '发热', icon: '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M14 14.76V3.5a2.5 2.5 0 0 0-5 0v11.26a4.5 4.5 0 1 0 5 0z"/></svg>' },
  { value: '疼痛', label: '疼痛', icon: '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/></svg>' },
  { value: '咳嗽', label: '咳嗽', icon: '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z"/><path d="M19 10v2a7 7 0 0 1-14 0v-2"/><line x1="12" y1="19" x2="12" y2="23"/><line x1="8" y1="23" x2="16" y2="23"/></svg>' },
  { value: '胃肠不适', label: '胃肠不适', icon: '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M12 2a10 10 0 0 0-10 10c0 5 4 9 10 10 6-1 10-5 10-10A10 10 0 0 0 12 2z"/><path d="M8 14s1.5 2 4 2 4-2 4-2"/></svg>' },
  { value: '皮肤症状', label: '皮肤症状', icon: '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><circle cx="12" cy="12" r="4"/><line x1="12" y1="2" x2="12" y2="6"/><line x1="12" y1="18" x2="12" y2="22"/><line x1="2" y1="12" x2="6" y2="12"/><line x1="18" y1="12" x2="22" y2="12"/></svg>' },
  { value: '妇科/男科', label: '妇科/男科', icon: '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="3"/><path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83"/></svg>' },
  { value: '儿科', label: '儿科', icon: '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>' },
  { value: '其他', label: '其他', icon: '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><path d="M14 2v6h6"/></svg>' }
]

const populations = [
  { value: '成人', label: '成人' },
  { value: '儿童', label: '儿童' },
  { value: '老人', label: '老人' },
  { value: '孕妇', label: '孕妇' }
]

const painClass = computed(() => {
  const l = form.value.painLevel
  if (l <= 3) return 'low'
  if (l <= 6) return 'medium'
  return 'high'
})

const isHighRisk = computed(() => form.value.painLevel >= 8)

function submitCheck() {
  if (!form.value.description.trim()) { toast.warning('请填写症状描述'); return }
  loading.value = true
  result.value = ''
  currentReply = ''
  messages.value = []
  sessionId = 'symptom-' + Date.now()
  resetStreaming()
  step.value = 4

  // 记录用户消息
  const userMsg = `${form.value.category}：${form.value.description}`

  chatWs.send('', {
    memoryId: sessionId,
    scenario: 'symptom',
    ...form.value
  })
}

function sendFollowUp() {
  const text = followUpText.value.trim()
  if (!text || loading.value) return

  loading.value = true
  result.value = ''
  currentReply = ''
  resetStreaming()

  // 记录用户追问
  messages.value.push({ role: 'user', content: text })
  followUpText.value = ''

  chatWs.send('', {
    memoryId: sessionId,
    scenario: 'chat',
    message: text
  })
}

function resetForm() {
  form.value = { category: '', description: '', duration: null, painLevel: 5, accompanying: '', triggers: '', history: '', allergies: '', population: '成人' }
  result.value = ''
  currentReply = ''
  messages.value = []
  sessionId = ''
  resetStreaming()
  step.value = 1
}

function copyResult() {
  navigator.clipboard.writeText(result.value)
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
  min-width: 80px;
}

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

.back-link:hover { color: var(--color-forest); }

.page-nav h1 {
  font-size: 18px;
  font-family: var(--font-body);
  font-weight: 600;
}

.page-content {
  max-width: 800px;
  margin: 32px auto;
  padding: 0 24px;
}

.step-card {
  background: var(--color-white);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 32px;
}

.step-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.step-num {
  font-family: var(--font-display);
  font-size: 14px;
  color: var(--color-terracotta);
  font-style: italic;
}

.step-header h2 {
  font-size: 1.4rem;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 24px;
}

.category-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px 12px;
  background: var(--color-paper);
  border: 1.5px solid transparent;
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
  color: var(--text-secondary);
}

.category-btn:hover {
  border-color: var(--color-forest);
  color: var(--color-forest);
}

.category-btn.active {
  border-color: var(--color-forest);
  background: rgba(45, 90, 61, 0.06);
  color: var(--color-forest);
}

.cat-icon { line-height: 0; }

.category-btn span {
  font-size: 13px;
  font-weight: 500;
}

.form-group { margin-bottom: 16px; }

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
.form-group textarea {
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
.form-group textarea:focus {
  border-color: var(--color-forest);
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.pain-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pain-slider {
  flex: 1;
  -webkit-appearance: none;
  height: 4px;
  background: var(--color-cloud);
  border-radius: 2px;
  outline: none;
}

.pain-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: var(--color-forest);
  cursor: pointer;
}

.pain-value {
  font-size: 20px;
  font-weight: 600;
  min-width: 28px;
  text-align: center;
}

.pain-value.low { color: var(--color-success); }
.pain-value.medium { color: var(--color-warning); }
.pain-value.high { color: var(--color-danger); }

.radio-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.radio-label { cursor: pointer; }
.radio-label input { display: none; }

.radio-box {
  display: inline-block;
  padding: 8px 16px;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text-secondary);
  transition: all var(--transition-fast);
}

.radio-label input:checked + .radio-box {
  border-color: var(--color-forest);
  background: rgba(45, 90, 61, 0.06);
  color: var(--color-forest);
}

.alert-danger {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: rgba(168, 80, 58, 0.06);
  border: 1px solid rgba(168, 80, 58, 0.2);
  border-radius: var(--radius-md);
  margin: 16px 0;
  color: var(--color-danger);
}

.alert-danger strong {
  display: block;
  margin-bottom: 4px;
}

.alert-danger p {
  font-size: 14px;
  line-height: 1.5;
}

.btn-row {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.btn-primary {
  flex: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 13px 24px;
  background: var(--color-forest);
  color: var(--color-white);
  font-size: 15px;
  font-weight: 500;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
  text-decoration: none;
}

.btn-primary:hover:not(:disabled) {
  background: var(--color-forest-dark);
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-ghost {
  padding: 13px 24px;
  background: transparent;
  color: var(--text-secondary);
  font-size: 15px;
  font-weight: 500;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.btn-ghost:hover {
  border-color: var(--color-forest);
  color: var(--color-forest);
}

.result-content {
  font-size: 15px;
  line-height: 1.8;
  color: var(--text-primary);
  padding: 24px;
  background: var(--color-paper);
  border-radius: var(--radius-md);
  margin-bottom: 8px;
}

/* 对话块 */
.chat-block { margin-bottom: 20px; }
.chat-user-msg { margin-bottom: 16px; }
.chat-user-label { font-size: 12px; font-weight: 600; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 6px; }
.chat-user-text { font-size: 14px; line-height: 1.6; color: var(--text-secondary); padding: 12px 16px; background: var(--color-paper); border-radius: var(--radius-md); }
.chat-ai-msg { margin-bottom: 8px; }
.chat-ai-label { font-size: 12px; font-weight: 600; color: var(--color-forest); text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 6px; }
.chat-ai-text { font-size: 15px; line-height: 1.8; color: var(--text-primary); padding: 20px; background: var(--color-paper); border-radius: var(--radius-md); }

.typing-cursor::after { content: ''; display: inline-block; width: 2px; height: 16px; background: var(--color-forest); margin-left: 2px; vertical-align: text-bottom; animation: blink 0.8s infinite; }
@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0; } }

/* 追问输入框 */
.follow-up { display: flex; gap: 8px; margin-top: 20px; margin-bottom: 8px; }
.follow-up input { flex: 1; padding: 12px 16px; border: 1px solid var(--border-light); border-radius: var(--radius-sm); font-size: 15px; color: var(--text-primary); outline: none; transition: border-color var(--transition-fast); }
.follow-up input:focus { border-color: var(--color-forest); }
.btn-send { width: 44px; height: 44px; display: flex; align-items: center; justify-content: center; background: var(--color-forest); color: var(--color-white); border: none; border-radius: var(--radius-sm); cursor: pointer; transition: background var(--transition-fast); }
.btn-send:hover:not(:disabled) { background: var(--color-forest-dark); }
.btn-send:disabled { opacity: 0.4; cursor: not-allowed; }

@media (max-width: 767px) {
  .category-grid { grid-template-columns: repeat(2, 1fr); }
  .form-row { grid-template-columns: 1fr; }
  .page-content { padding: 0 16px; }
  .step-card { padding: 24px 20px; }
}
</style>
