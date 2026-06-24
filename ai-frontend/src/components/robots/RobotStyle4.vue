<template>
  <svg viewBox="0 0 300 380" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <!-- 身体渐变 -->
      <radialGradient id="s4-bodyGrad" cx="50%" cy="40%" r="55%">
        <stop offset="0%" stop-color="#ffffff"/>
        <stop offset="70%" stop-color="#f0f0f0"/>
        <stop offset="100%" stop-color="#d4d4d4"/>
      </radialGradient>

      <!-- 头部渐变 -->
      <radialGradient id="s4-headGrad" cx="50%" cy="45%" r="55%">
        <stop offset="0%" stop-color="#ffffff"/>
        <stop offset="80%" stop-color="#f5f5f5"/>
        <stop offset="100%" stop-color="#e0e0e0"/>
      </radialGradient>

      <!-- 面罩渐变 - 紫蓝 -->
      <radialGradient id="s4-screenGrad" cx="50%" cy="45%" r="50%">
        <stop offset="0%" stop-color="#818cf8"/>
        <stop offset="35%" stop-color="#6366f1"/>
        <stop offset="70%" stop-color="#7c3aed"/>
        <stop offset="100%" stop-color="#6d28d9"/>
      </radialGradient>

      <!-- 面罩光晕 -->
      <radialGradient id="s4-screenGlow" cx="50%" cy="40%" r="50%">
        <stop offset="0%" stop-color="#a5b4fc" stop-opacity="0.8"/>
        <stop offset="60%" stop-color="#818cf8" stop-opacity="0.4"/>
        <stop offset="100%" stop-color="#6366f1" stop-opacity="0"/>
      </radialGradient>

      <!-- 心形渐变 -->
      <linearGradient id="s4-heartGrad" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" stop-color="#f87171"/>
        <stop offset="100%" stop-color="#ef4444"/>
      </linearGradient>

      <!-- 气泡渐变 -->
      <linearGradient id="s4-bubbleGrad" x1="0%" y1="0%" x2="0%" y2="100%">
        <stop offset="0%" stop-color="#ffffff"/>
        <stop offset="100%" stop-color="#f5f5f5"/>
      </linearGradient>

      <!-- 发光效果 -->
      <filter id="s4-glow">
        <feGaussianBlur stdDeviation="4" result="blur"/>
        <feMerge>
          <feMergeNode in="blur"/>
          <feMergeNode in="SourceGraphic"/>
        </feMerge>
      </filter>

      <!-- 柔和阴影 -->
      <filter id="s4-shadow">
        <feDropShadow dx="0" dy="10" stdDeviation="20" flood-color="#94a3b8" flood-opacity="0.2"/>
      </filter>

      <filter id="s4-headShadow">
        <feDropShadow dx="0" dy="6" stdDeviation="12" flood-color="#94a3b8" flood-opacity="0.15"/>
      </filter>
    </defs>

    <!-- ===== 心形气泡 ===== -->
    <g class="heart-bubble">
      <!-- 气泡背景 -->
      <rect x="120" y="18" width="60" height="52" rx="16" fill="url(#s4-bubbleGrad)" stroke="#e5e7eb" stroke-width="1.5"/>
      <!-- 气泡尾巴 -->
      <path d="M148,70 L152,80 L156,70" fill="url(#s4-bubbleGrad)" stroke="#e5e7eb" stroke-width="1.5" stroke-linejoin="round"/>
      <!-- 心形 -->
      <path d="M150,38 C150,32 142,28 138,32 C134,28 126,32 126,38 C126,48 150,56 150,56 C150,56 174,48 174,38 C174,32 166,28 162,32 C158,28 150,32 150,38Z" fill="url(#s4-heartGrad)"/>
    </g>

    <!-- ===== 头部 ===== -->
    <g class="head" filter="url(#s4-headShadow)">
      <!-- 头部球体 -->
      <circle cx="150" cy="140" r="65" fill="url(#s4-headGrad)"/>

      <!-- 面罩 -->
      <circle cx="150" cy="140" r="52" fill="url(#s4-screenGrad)"/>
      <circle cx="150" cy="140" r="52" fill="url(#s4-screenGlow)"/>

      <!-- 面罩高光 -->
      <ellipse cx="135" cy="120" rx="25" ry="15" fill="white" opacity="0.15"/>

      <!-- 眼睛 - 椭圆 -->
      <ellipse cx="132" cy="140" rx="10" ry="14" fill="none" stroke="white" stroke-width="3" opacity="0.85"/>
      <ellipse cx="168" cy="140" rx="10" ry="14" fill="none" stroke="white" stroke-width="3" opacity="0.85"/>

      <!-- 耳朵/天线底座 -->
      <g class="ear-left">
        <circle cx="82" cy="140" r="10" fill="#e5e7eb"/>
        <circle cx="82" cy="140" r="6" fill="#d1d5db"/>
        <!-- 小天线 -->
        <line x1="75" y1="132" x2="72" y2="122" stroke="#d1d5db" stroke-width="2.5" stroke-linecap="round"/>
        <circle cx="71" cy="120" r="3" fill="#d1d5db"/>
      </g>

      <g class="ear-right">
        <circle cx="218" cy="140" r="10" fill="#e5e7eb"/>
        <circle cx="218" cy="140" r="6" fill="#d1d5db"/>
        <line x1="225" y1="132" x2="228" y2="122" stroke="#d1d5db" stroke-width="2.5" stroke-linecap="round"/>
        <circle cx="229" cy="120" r="3" fill="#d1d5db"/>
      </g>
    </g>

    <!-- ===== 颈部连接 ===== -->
    <ellipse cx="150" cy="205" rx="28" ry="6" fill="#e5e7eb"/>

    <!-- ===== 身体 ===== -->
    <g class="body" filter="url(#s4-shadow)">
      <ellipse cx="150" cy="280" rx="95" ry="95" fill="url(#s4-bodyGrad)"/>

      <!-- 身体纹理/斑点 -->
      <g class="body-texture" opacity="0.08">
        <circle cx="110" cy="240" r="2" fill="#94a3b8"/>
        <circle cx="130" cy="260" r="1.5" fill="#94a3b8"/>
        <circle cx="175" cy="250" r="2.5" fill="#94a3b8"/>
        <circle cx="195" cy="275" r="1.8" fill="#94a3b8"/>
        <circle cx="120" cy="290" r="2" fill="#94a3b8"/>
        <circle cx="160" cy="310" r="1.5" fill="#94a3b8"/>
        <circle cx="185" cy="300" r="2" fill="#94a3b8"/>
        <circle cx="140" cy="320" r="1.8" fill="#94a3b8"/>
        <circle cx="115" cy="310" r="2.2" fill="#94a3b8"/>
        <circle cx="170" cy="280" r="1.5" fill="#94a3b8"/>
        <circle cx="135" cy="275" r="2" fill="#94a3b8"/>
        <circle cx="190" cy="290" r="1.8" fill="#94a3b8"/>
        <circle cx="125" cy="305" r="1.5" fill="#94a3b8"/>
        <circle cx="155" cy="295" r="2" fill="#94a3b8"/>
        <circle cx="175" cy="320" r="1.8" fill="#94a3b8"/>
        <circle cx="105" cy="275" r="2" fill="#94a3b8"/>
        <circle cx="145" cy="245" r="1.5" fill="#94a3b8"/>
        <circle cx="165" cy="265" r="2.2" fill="#94a3b8"/>
        <circle cx="180" cy="315" r="1.5" fill="#94a3b8"/>
        <circle cx="128" cy="325" r="2" fill="#94a3b8"/>
        <circle cx="152" cy="330" r="1.8" fill="#94a3b8"/>
        <circle cx="198" cy="260" r="2" fill="#94a3b8"/>
        <circle cx="108" cy="295" r="1.5" fill="#94a3b8"/>
        <circle cx="142" cy="305" r="2" fill="#94a3b8"/>
      </g>

      <!-- 身体高光 -->
      <ellipse cx="135" cy="230" rx="45" ry="35" fill="white" opacity="0.12"/>
    </g>

    <!-- ===== 地面阴影 ===== -->
    <ellipse cx="150" cy="375" rx="80" ry="6" fill="#94a3b8" opacity="0.12"/>
  </svg>
</template>

<style scoped>
svg {
  width: 100%;
  height: 100%;
}

/* 心形气泡浮动动画 */
.heart-bubble {
  animation: bubble-float 3s ease-in-out infinite;
  transform-origin: 150px 45px;
}

@keyframes bubble-float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

/* 头部微动 */
.head {
  animation: head-tilt 5s ease-in-out infinite;
  transform-origin: 150px 140px;
}

@keyframes head-tilt {
  0%, 100% { transform: rotate(0deg); }
  30% { transform: rotate(2deg); }
  70% { transform: rotate(-2deg); }
}

/* 耳朵天线闪烁 */
.ear-left, .ear-right {
  animation: ear-pulse 4s ease-in-out infinite;
}

.ear-right {
  animation-delay: 0.5s;
}

@keyframes ear-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}
</style>
