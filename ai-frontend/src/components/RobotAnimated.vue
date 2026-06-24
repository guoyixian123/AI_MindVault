<template>
  <div class="robot-container" :data-smile="smileLevel" @click="onClick" @mouseenter="onHover" @mouseleave="onLeave">
    <!-- 聊天气泡 -->
    <Transition name="bubble">
      <div v-if="showBubble" class="chat-bubble">
        <div class="bubble-content">
          <p>{{ bubbleText }}</p>
        </div>
        <div class="bubble-tail"></div>
      </div>
    </Transition>

    <!-- 机器人 SVG -->
    <svg class="robot-svg" :class="{ waving: isWaving, bouncing: isBouncing }" viewBox="0 0 280 330" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <linearGradient id="s1-headGrad" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#2d5a3d" />
          <stop offset="100%" stop-color="#1e3d2a" />
        </linearGradient>
        <linearGradient id="s1-screenGrad" x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" stop-color="#0f172a" />
          <stop offset="100%" stop-color="#1e293b" />
        </linearGradient>
        <radialGradient id="s1-eyeGlow" cx="50%" cy="50%" r="50%">
          <stop offset="0%" stop-color="#8fae8b" />
          <stop offset="100%" stop-color="#3a7250" />
        </radialGradient>
        <filter id="s1-glow">
          <feGaussianBlur stdDeviation="3" result="blur"/>
          <feMerge><feMergeNode in="blur"/><feMergeNode in="SourceGraphic"/></feMerge>
        </filter>
      </defs>

      <!-- 天线 -->
      <g class="antenna-group">
        <line x1="140" y1="30" x2="140" y2="10" stroke="#3a7250" stroke-width="2.5" stroke-linecap="round"/>
        <circle cx="140" cy="8" r="4" fill="#c4654a" class="antenna-tip"/>
      </g>

      <!-- 头部 -->
      <g class="head-group">
        <rect x="55" y="30" width="170" height="130" rx="50" fill="url(#s1-headGrad)"/>
        <rect x="80" y="60" width="120" height="60" rx="20" fill="url(#s1-screenGrad)" stroke="#3a7250" stroke-width="1.5" stroke-opacity="0.4"/>

        <!-- 正常眼睛 -->
        <g class="eyes-normal" :style="{ transform: `translate(${eyeX}px, ${eyeY}px)` }">
          <ellipse cx="120" cy="90" rx="14" ry="14" fill="url(#s1-eyeGlow)" filter="url(#s1-glow)"/>
          <ellipse cx="120" cy="90" rx="6" ry="6" fill="white" opacity="0.9"/>
          <circle cx="124" cy="87" r="2.5" fill="white" opacity="0.7"/>
          <ellipse cx="160" cy="90" rx="14" ry="14" fill="url(#s1-eyeGlow)" filter="url(#s1-glow)"/>
          <ellipse cx="160" cy="90" rx="6" ry="6" fill="white" opacity="0.9"/>
          <circle cx="164" cy="87" r="2.5" fill="white" opacity="0.7"/>
        </g>

        <!-- 灭灯眼睛（眨眼时显示） -->
        <g class="eyes-dim">
          <ellipse cx="120" cy="90" rx="14" ry="14" fill="#1e3d2a"/>
          <ellipse cx="160" cy="90" rx="14" ry="14" fill="#1e3d2a"/>
        </g>

        <!-- 眯眯眼（大笑时显示） -->
        <g class="eyes-happy">
          <path d="M108,90 Q120,84 132,90" fill="none" stroke="url(#s1-eyeGlow)" stroke-width="3" stroke-linecap="round"/>
          <path d="M148,90 Q160,84 172,90" fill="none" stroke="url(#s1-eyeGlow)" stroke-width="3" stroke-linecap="round"/>
        </g>

        <!-- 嘴巴 -->
        <g class="mouth-group">
          <path d="M125,108 Q140,116 155,108" fill="none" stroke="#8fae8b" stroke-width="2.5" stroke-linecap="round" class="mouth-normal"/>
          <path d="M120,106 Q140,122 160,106" fill="none" stroke="#8fae8b" stroke-width="2.5" stroke-linecap="round" class="mouth-smile"/>
          <path d="M122,106 Q140,124 158,106" fill="none" stroke="#8fae8b" stroke-width="2.5" stroke-linecap="round" class="mouth-laugh"/>
        </g>
      </g>

      <!-- 耳朵 -->
      <rect x="45" y="75" width="14" height="30" rx="7" fill="#3a7250"/>
      <rect x="221" y="75" width="14" height="30" rx="7" fill="#3a7250"/>

      <!-- 颈部 -->
      <rect x="125" y="160" width="30" height="10" rx="4" fill="#e2e8f0"/>

      <!-- 身体 -->
      <rect x="70" y="170" width="140" height="100" rx="24" fill="#f8fafc" stroke="#e2e8f0" stroke-width="1.5"/>
      <rect x="136" y="196" width="8" height="28" rx="2" fill="#14b8a6"/>
      <rect x="126" y="206" width="28" height="8" rx="2" fill="#14b8a6"/>

      <!-- 左手臂 -->
      <g class="arm-left">
        <rect x="38" y="180" width="26" height="60" rx="13" fill="#f1f5f9" stroke="#e2e8f0" stroke-width="1"/>
      </g>

      <!-- 右手臂 -->
      <g class="arm-right">
        <rect x="216" y="180" width="26" height="60" rx="13" fill="#f1f5f9" stroke="#e2e8f0" stroke-width="1"/>
      </g>

      <!-- 腿 -->
      <g class="legs">
        <rect x="100" y="270" width="26" height="35" rx="10" fill="#f1f5f9" stroke="#e2e8f0" stroke-width="1"/>
        <rect x="94" y="300" width="38" height="12" rx="6" fill="#e2e8f0"/>
        <rect x="154" y="270" width="26" height="35" rx="10" fill="#f1f5f9" stroke="#e2e8f0" stroke-width="1"/>
        <rect x="148" y="300" width="38" height="12" rx="6" fill="#e2e8f0"/>
      </g>
    </svg>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const showBubble = ref(false)
const isWaving = ref(false)
const isBouncing = ref(false)
const smileLevel = ref(0) // 0=普通, 1=微笑, 2=大笑
const bubbleText = ref('')
const eyeX = ref(0)
const eyeY = ref(0)

let blinkTimer = null
let autoMessageTimer = null
let bubbleHideTimer = null
let scrollTimer = null
let pageStartTime = Date.now()

// ===== 消息库 =====
const messages = {
  // 刚进入页面
  enter: [
    '你好呀！欢迎来到健康站～ (◕‿◕)',
    '嗨！我是你的健康小助手，有什么可以帮你的？',
    '欢迎！我可以帮你查药品、看疾病、做症状自查哦～',
    '你好！今天想了解什么健康知识？',
  ],

  // 常规问候
  greet: [
    '你好呀！今天过得怎么样？',
    '嗨，又见面啦～',
    '你好！看到你真开心 (＾▽＾)',
    '哈喽～有什么可以帮你的吗？',
    '你好呀！我一直在呢～',
  ],

  // 关心
  care: [
    '今天有好好吃饭吗？',
    '记得多喝水哦 (◠‿◠)',
    '久坐记得站起来活动一下',
    '今天有没有笑一笑？',
    '天气变化大，注意添衣保暖',
    '记得保护眼睛，少看手机哦',
    '吃饭七分饱，身体更健康',
  ],

  // 健康贴士
  tip: [
    '小贴士：饭后百步走，活到九十九',
    '小贴士：多吃蔬果，补充维生素',
    '小贴士：早睡早起，精神百倍',
    '小贴士：每天八杯水，健康常相伴',
    '小贴士：保持好心情是最好的养生',
    '小贴士：适当运动，增强免疫力',
    '小贴士：定期体检，早发现早预防',
    '小贴士：细嚼慢咽，有助消化',
    '小贴士：睡前泡脚，睡眠更好',
  ],

  // 祝福
  bless: [
    '祝你今天一切顺利！',
    '愿你每天都元气满满 (•̀ᴗ•́)و',
    '祝你心情愉快，万事如意～',
    '愿你和家人都平安健康',
    '祝你工作顺利，记得劳逸结合哦',
    '今天也要加油呀！',
    '愿你被世界温柔以待～',
  ],

  // 滚动时
  scroll: [
    '看得认真哦～',
    '慢慢看，不着急～',
    '有什么想了解的可以问我哦',
    '浏览中有什么问题吗？',
  ],

  // 页面停留一段时间后
  stay: [
    '还在看呀？有什么可以帮你的吗？',
    '需要我帮你查什么吗？',
    '有什么问题随时问我哦～',
    '我在这里，随时为你服务～',
  ],

  // 用户回来时
  back: [
    '欢迎回来！我好想你呀～',
    '你终于回来了！',
    '回来啦！有什么需要帮忙的吗？',
    '欢迎回来！我一直在这里等你～',
  ],

  // 点击时
  click: [
    '哎呀，被你发现了～ (◕‿◕)',
    '别戳我呀，会痒的～',
    '你好呀！有什么需要帮忙的吗？',
    '我在呢！随时为你服务～',
    '点我干嘛？想聊天吗？ (＾▽＾)',
    '嘿嘿，你好调皮～',
    '想和我玩呀？',
  ],

  // 悬停时
  hover: [
    '想和我聊天吗？',
    '你好呀～',
    '有什么可以帮你的？',
    '我在呢！',
    '需要帮忙吗？',
  ],

  // 时间问候
  morning: ['早上好！新的一天开始了～', '早安！今天也要元气满满哦', '早上好！记得吃早餐哦'],
  afternoon: ['下午好！工作累了吗？', '下午好！记得休息一下眼睛', '下午茶时间到了～'],
  evening: ['晚上好！今天辛苦了', '晚上好！记得放松一下', '晚上好！早点休息哦'],
  night: ['这么晚还没睡吗？注意休息哦', '夜深了，早点休息吧', '熬夜对身体不好哦～'],
}

// ===== 工具函数 =====
function pick(arr) {
  return arr[Math.floor(Math.random() * arr.length)]
}

function getTimeMessages() {
  const hour = new Date().getHours()
  if (hour < 12) return messages.morning
  if (hour < 18) return messages.afternoon
  if (hour < 22) return messages.evening
  return messages.night
}

function speak(text, level = 1) {
  bubbleText.value = text
  showBubble.value = true
  isWaving.value = true
  smileLevel.value = level

  setTimeout(() => { isWaving.value = false }, 1200)
  setTimeout(() => { smileLevel.value = 0 }, 3000)

  clearTimeout(bubbleHideTimer)
  bubbleHideTimer = setTimeout(() => { showBubble.value = false }, 4000)
}

function speakRandom(category) {
  const pool = messages[category] || messages.greet
  speak(pick(pool))
}

// ===== 主动交互逻辑 =====
function startAutoMessages() {
  // 进入页面 1 秒后打招呼
  setTimeout(() => speakRandom('enter'), 1000)

  // 3 秒后说时间问候
  setTimeout(() => speak(pick(getTimeMessages())), 4000)

  // 每 6-10 秒主动说话
  autoMessageTimer = setInterval(() => {
    const elapsed = Date.now() - pageStartTime
    const categories = ['greet', 'care', 'tip', 'bless']
    const category = categories[Math.floor(Math.random() * categories.length)]

    // 停留超过 30 秒，说 stay 类消息
    if (elapsed > 30000 && Math.random() < 0.3) {
      speakRandom('stay')
    } else {
      speakRandom(category)
    }
  }, 6000 + Math.random() * 4000)
}

// ===== 生命周期 =====
onMounted(() => {
  // 眨眼 - 使用 CSS 动画类
  const doBlink = () => {
    const container = document.querySelector('.robot-container')
    if (container) {
      container.classList.add('blinking')
      setTimeout(() => {
        container.classList.remove('blinking')
      }, 200)
    }
    blinkTimer = setTimeout(doBlink, 2500 + Math.random() * 3000)
  }
  doBlink()

  // 启动主动消息
  startAutoMessages()

  // 鼠标跟踪
  document.addEventListener('mousemove', onMouseMove)

  // 滚动监听
  window.addEventListener('scroll', onScroll)

  // 页面可见性变化（用户切回来）
  document.addEventListener('visibilitychange', onVisibilityChange)
})

onUnmounted(() => {
  clearTimeout(blinkTimer)
  clearInterval(autoMessageTimer)
  clearTimeout(bubbleHideTimer)
  clearTimeout(scrollTimer)
  document.removeEventListener('mousemove', onMouseMove)
  window.removeEventListener('scroll', onScroll)
  document.removeEventListener('visibilitychange', onVisibilityChange)
})

// ===== 事件处理 =====
function onMouseMove(e) {
  const container = document.querySelector('.robot-container')
  if (!container) return

  const rect = container.getBoundingClientRect()
  const centerX = rect.left + rect.width / 2
  const centerY = rect.top + rect.height / 3

  const dx = e.clientX - centerX
  const dy = e.clientY - centerY
  const distance = Math.sqrt(dx * dx + dy * dy)

  const maxMove = 4
  const factor = distance > 100 ? 1 : distance / 100
  eyeX.value = Math.max(-maxMove, Math.min(maxMove, (dx / (distance || 1)) * maxMove * factor))
  eyeY.value = Math.max(-maxMove, Math.min(maxMove, (dy / (distance || 1)) * maxMove * factor))
}

function onScroll() {
  clearTimeout(scrollTimer)
  scrollTimer = setTimeout(() => {
    if (Math.random() < 0.4) {
      speakRandom('scroll')
    }
  }, 1500)
}

function onVisibilityChange() {
  if (!document.hidden) {
    // 用户回来了
    speakRandom('back')
    pageStartTime = Date.now()
  }
}

function onClick() {
  isBouncing.value = true
  setTimeout(() => { isBouncing.value = false }, 500)
  speak(pick(messages.click), 2) // 大笑
}

function onHover() {
  if (!showBubble.value) {
    speakRandom('hover')
  }
}

function onLeave() {
  // nothing
}
</script>

<style scoped>
.robot-container {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  animation: float 5s ease-in-out infinite;
}

.robot-svg {
  width: 100%;
  max-width: 300px;
  transition: transform 0.3s ease;
}

.robot-container:hover .robot-svg {
  transform: scale(1.02);
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

/* ===== 眼睛状态 ===== */

/* 默认状态：正常眼睛可见，其他隐藏 */
.eyes-normal { display: block; }
.eyes-dim { display: none; }
.eyes-happy { display: none; }

/* 眨眼状态：灭灯 */
.blinking .eyes-normal { display: none; }
.blinking .eyes-dim { display: block; }

/* 大笑状态：眯眯眼 */
.robot-container[data-smile="2"] .eyes-normal { display: none; }
.robot-container[data-smile="2"] .eyes-happy { display: block; }

.eyes {
  transition: transform 0.1s ease-out;
}

.arm-left { transform-origin: 51px 210px; }
.arm-right { transform-origin: 229px 210px; }

.waving .arm-left {
  animation: wave 0.5s ease-in-out 3;
}

.waving .arm-right {
  animation: waveRight 0.5s ease-in-out 3;
}

@keyframes wave {
  0%, 100% { transform: rotate(0deg); }
  20% { transform: rotate(-35deg); }
  40% { transform: rotate(-25deg); }
  60% { transform: rotate(-35deg); }
  80% { transform: rotate(-25deg); }
}

@keyframes waveRight {
  0%, 100% { transform: rotate(0deg); }
  20% { transform: rotate(35deg); }
  40% { transform: rotate(25deg); }
  60% { transform: rotate(35deg); }
  80% { transform: rotate(25deg); }
}

.bouncing {
  animation: bounce 0.5s ease;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  30% { transform: translateY(-15px); }
  50% { transform: translateY(-5px); }
  70% { transform: translateY(-10px); }
}

.antenna-tip {
  animation: antennaBob 2s ease-in-out infinite;
}

@keyframes antennaBob {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-3px); }
}

/* ===== 嘴巴状态 ===== */

/* 默认状态 */
.mouth-normal { display: block; }
.mouth-smile { display: none; }
.mouth-laugh { display: none; }

/* 微笑状态 */
.robot-container[data-smile="1"] .mouth-normal { display: none; }
.robot-container[data-smile="1"] .mouth-smile { display: block; }

/* 大笑状态 */
.robot-container[data-smile="2"] .mouth-normal { display: none; }
.robot-container[data-smile="2"] .mouth-laugh { display: block; }

.chat-bubble {
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%);
  margin-bottom: 16px;
  z-index: 10;
}

.bubble-content {
  background: white;
  border: 1px solid var(--border-light, #e8e4dd);
  border-radius: 16px;
  padding: 14px 20px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
  min-width: 180px;
  max-width: 240px;
}

.bubble-content p {
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-primary, #2c2c2c);
  margin: 0;
  text-align: center;
}

.bubble-tail {
  width: 12px;
  height: 12px;
  background: white;
  border-right: 1px solid var(--border-light, #e8e4dd);
  border-bottom: 1px solid var(--border-light, #e8e4dd);
  transform: rotate(45deg);
  position: absolute;
  bottom: -6px;
  left: 50%;
  margin-left: -6px;
}

.bubble-enter-active {
  transition: all 0.4s cubic-bezier(0.22, 1, 0.36, 1);
}

.bubble-leave-active {
  transition: all 0.3s ease;
}

.bubble-enter-from {
  opacity: 0;
  transform: translateX(-50%) translateY(10px) scale(0.9);
}

.bubble-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(6px) scale(0.95);
}

@media (max-width: 767px) {
  .robot-svg { max-width: 200px; }
  .bubble-content { min-width: 150px; max-width: 180px; padding: 10px 14px; }
  .bubble-content p { font-size: 13px; }
}
</style>
