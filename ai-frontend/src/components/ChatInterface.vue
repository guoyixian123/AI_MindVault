<template>
  <div class="chat-panel" :class="{ open: isOpen }">
    <!-- 顶部栏 -->
    <div class="chat-header">
      <div class="chat-header-info">
        <div class="status-dot"></div>
        <span>AI 医疗顾问在线</span>
      </div>
      <button class="chat-close" @click="$emit('close')">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M18 6L6 18M6 6l12 12"/>
        </svg>
      </button>
    </div>

    <!-- 消息列表 -->
    <div ref="messagesContainer" class="chat-messages">
      <div
        v-for="(msg, index) in messages"
        :key="index"
        class="message-row"
        :class="msg.role === 'user' ? 'user' : 'assistant'"
      >
        <div v-if="msg.role === 'assistant'" class="avatar">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M12 2a4 4 0 0 1 4 4v2a4 4 0 0 1-8 0V6a4 4 0 0 1 4-4z"/>
            <path d="M6 12v2a6 6 0 0 0 12 0v-2"/>
            <circle cx="9" cy="10" r="1" fill="currentColor"/>
            <circle cx="15" cy="10" r="1" fill="currentColor"/>
          </svg>
        </div>
        <div class="message-bubble" :class="msg.role">
          <div v-if="msg.role === 'assistant'" class="msg-label">A.R.I.A</div>
          <div class="msg-content markdown-body" :class="{ 'typing-cursor': isStreaming && index === currentAssistantIndex }" v-html="formatMessage(msg, index)"></div>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="isLoading" class="message-row assistant">
        <div class="avatar">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M12 2a4 4 0 0 1 4 4v2a4 4 0 0 1-8 0V6a4 4 0 0 1 4-4z"/>
            <path d="M6 12v2a6 6 0 0 0 12 0v-2"/>
          </svg>
        </div>
        <div class="message-bubble assistant loading-bubble">
          <div class="typing-indicator">
            <span></span><span></span><span></span>
          </div>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="chat-input-area">
      <div class="input-wrapper">
        <textarea
          ref="textareaRef"
          v-model="userInput"
          @keydown.enter.exact.prevent="sendMessage"
          placeholder="描述您的症状或健康问题..."
          rows="1"
          :disabled="isLoading"
        ></textarea>
        <button
          class="send-btn"
          @click="sendMessage"
          :disabled="!userInput.trim() || isLoading"
        >
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z"/>
          </svg>
        </button>
      </div>
      <div class="input-hint">按 Enter 发送 · Shift + Enter 换行</div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch, onMounted, onUnmounted } from 'vue'
import { useChatWebSocket } from '../utils/websocket'
import { renderMarkdown } from '../utils/markedConfig'

const props = defineProps({
  isOpen: Boolean,
  initialMessage: { type: String, default: '' }
})

const emit = defineEmits(['close'])

const userInput = ref('')
const messages = ref([
  {
    role: 'assistant',
    raw: '您好！我是 A.R.I.A，您的医疗健康智能咨询助手。我可以帮您进行症状分析、用药指导、健康评估等。请问有什么可以帮您的？',
    displayed: '您好！我是 A.R.I.A，您的医疗健康智能咨询助手。我可以帮您进行症状分析、用药指导、健康评估等。请问有什么可以帮您的？'
  }
])
const isLoading = ref(false)
const isStreaming = ref(false)
const messagesContainer = ref(null)
const textareaRef = ref(null)

// WebSocket
const chatWs = useChatWebSocket()
let currentAssistantIndex = -1

// 逐词流式显示（requestAnimationFrame + 时间控制，丝滑流畅）
let wordQueue = []
let displayText = ''
let lastWordTime = 0
const WORD_DELAY = 55 // 每词间隔 ms，越大越慢

function processQueue(timestamp) {
  if (wordQueue.length === 0) {
    isStreaming.value = false
    return
  }
  isStreaming.value = true
  if (!lastWordTime) lastWordTime = timestamp
  if (timestamp - lastWordTime >= WORD_DELAY) {
    displayText += wordQueue.shift()
    if (currentAssistantIndex >= 0) {
      messages.value[currentAssistantIndex].displayed = displayText
    }
    scrollToBottom()
    lastWordTime = timestamp
  }
  requestAnimationFrame(processQueue)
}

function appendText(text) {
  if (currentAssistantIndex < 0) return
  // 累积完整原始文本（用于最终渲染）
  messages.value[currentAssistantIndex].raw += text
  // 按词拆分加入队列（保留空格和换行作为分隔）
  const words = text.split(/(?<=\s)/)
  wordQueue.push(...words)
  if (!isStreaming.value) {
    lastWordTime = 0
    requestAnimationFrame(processQueue)
  }
}

// WebSocket 回调函数
const onOpen = () => {
  console.log('WebSocket 已连接')
}

const onMessage = (data) => {
  appendText(data)
}

const onDone = () => {
  // 等逐词队列播完再隐藏加载状态
  const waitDone = setInterval(() => {
    if (!isStreaming.value) {
      clearInterval(waitDone)
      // 用完整 raw 覆盖 displayed，确保最终渲染完整
      if (currentAssistantIndex >= 0) {
        messages.value[currentAssistantIndex].displayed = messages.value[currentAssistantIndex].raw
      }
      isLoading.value = false
      currentAssistantIndex = -1
    }
  }, 50)
}

const onError = (error) => {
  console.error('WebSocket 错误:', error)
  if (currentAssistantIndex >= 0) {
    messages.value[currentAssistantIndex].raw = '抱歉，连接出现问题。请确保后端服务正在运行，然后重试。'
    messages.value[currentAssistantIndex].displayed = messages.value[currentAssistantIndex].raw
  }
  isLoading.value = false
}

const onClose = () => {
  console.log('WebSocket 已断开')
}

// 初始化 WebSocket
function initWebSocket() {
  // 先移除旧回调
  chatWs.removeCallback('onOpen')
  chatWs.removeCallback('onMessage')
  chatWs.removeCallback('onDone')
  chatWs.removeCallback('onError')
  chatWs.removeCallback('onClose')

  // 添加新回调
  chatWs.on('open', onOpen)
  chatWs.on('message', onMessage)
  chatWs.on('done', onDone)
  chatWs.on('error', onError)
  chatWs.on('close', onClose)
}

// 组件挂载时初始化 WebSocket
onMounted(() => {
  initWebSocket()
  chatWs.connect()
})

// 如果有初始消息，自动发送
watch(() => props.isOpen, (val) => {
  if (val && props.initialMessage) {
    userInput.value = props.initialMessage
    nextTick(() => sendMessage())
  }
  if (val) {
    nextTick(() => textareaRef.value?.focus())
  }
})

onUnmounted(() => {
  chatWs.close()
})

function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

function formatMessage(msg, index) {
  const text = (index === currentAssistantIndex && msg.displayed !== undefined)
    ? msg.displayed
    : msg.raw
  return renderMarkdown(text)
}

function sendMessage() {
  const msg = userInput.value.trim()
  if (!msg || isLoading.value) return

  messages.value.push({ role: 'user', raw: msg })
  userInput.value = ''
  isLoading.value = true
  scrollToBottom()

  currentAssistantIndex = messages.value.length
  messages.value.push({ role: 'assistant', raw: '', displayed: '' })
  wordQueue = []
  displayText = ''
  lastWordTime = 0

  // 通过 WebSocket 发送
  chatWs.send(msg, { memoryId: 'frontend-session' })
}
</script>

<style scoped>
.chat-panel {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 520px;
  background: var(--bg-glass-strong);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-top: 1px solid rgba(13, 148, 136, 0.15);
  box-shadow: 0 -8px 40px rgba(0, 0, 0, 0.08);
  z-index: 50;
  display: flex;
  flex-direction: column;
  transform: translateY(100%);
  transition: transform 0.45s cubic-bezier(0.16, 1, 0.3, 1);
  border-radius: var(--radius-lg) var(--radius-lg) 0 0;
}

.chat-panel.open {
  transform: translateY(0);
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  flex-shrink: 0;
}

.chat-header-info {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.status-dot {
  width: 8px;
  height: 8px;
  background: #10b981;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

.chat-close {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  transition: all var(--transition-fast);
}

.chat-close:hover {
  background: rgba(0, 0, 0, 0.06);
  color: var(--text-primary);
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-row {
  display: flex;
  gap: 10px;
  animation: fadeInUp 0.3s ease;
}

.message-row.user {
  justify-content: flex-end;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-accent));
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.message-bubble {
  max-width: 75%;
  padding: 12px 16px;
  border-radius: var(--radius-md);
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}

.message-bubble.user {
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  color: white;
  border-bottom-right-radius: 4px;
}

.message-bubble.assistant {
  background: var(--color-white);
  color: var(--text-primary);
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-bottom-left-radius: 4px;
  box-shadow: var(--shadow-sm);
}

.msg-label {
  font-size: 11px;
  font-weight: 600;
  color: var(--color-primary);
  margin-bottom: 4px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.loading-bubble {
  padding: 14px 20px;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  align-items: center;
}

.typing-indicator span {
  width: 6px;
  height: 6px;
  background: var(--color-gray);
  border-radius: 50%;
  animation: bounce 1.2s infinite;
}

.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

.chat-input-area {
  padding: 16px 24px 20px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.5);
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  background: var(--color-white);
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: var(--radius-lg);
  padding: 8px 8px 8px 16px;
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
}

.input-wrapper:focus-within {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(13, 148, 136, 0.1);
}

.input-wrapper textarea {
  flex: 1;
  border: none;
  outline: none;
  resize: none;
  font-family: var(--font-body);
  font-size: 14px;
  line-height: 1.5;
  color: var(--text-primary);
  background: transparent;
  max-height: 100px;
  padding: 6px 0;
}

.input-wrapper textarea::placeholder {
  color: var(--text-muted);
}

.send-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all var(--transition-fast);
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(13, 148, 136, 0.3);
}

.send-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.input-hint {
  font-size: 11px;
  color: var(--text-muted);
  text-align: center;
  margin-top: 8px;
}

@media (min-width: 768px) {
  .chat-panel {
    left: auto;
    right: 24px;
    bottom: 24px;
    width: 420px;
    height: 560px;
    border-radius: var(--radius-lg);
    border: 1px solid rgba(0, 0, 0, 0.08);
  }
}
</style>
