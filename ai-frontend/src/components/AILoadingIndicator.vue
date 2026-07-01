<template>
  <div class="ai-loading">
    <div class="loading-card">
      <!-- 顶部 AI 标识 -->
      <div class="loading-header">
        <div class="ai-badge">
          <div class="badge-glow"></div>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 2a4 4 0 0 1 4 4v1a3 3 0 0 1 3 3v1a2 2 0 0 1-2 2h-1l1 5H7l1-5H7a2 2 0 0 1-2-2v-1a3 3 0 0 1 3-3V6a4 4 0 0 1 4-4z"/>
            <circle cx="12" cy="14" r="1"/>
            <path d="M8 18h8"/>
            <path d="M9 21h6"/>
          </svg>
        </div>
        <span class="loading-label">{{ label }}</span>
        <span class="loading-dots">
          <span class="dot"></span>
          <span class="dot"></span>
          <span class="dot"></span>
        </span>
      </div>

      <!-- 骨架内容 -->
      <div class="skeleton-body">
        <div class="sk-line sk-title" style="width: 45%"></div>
        <div class="sk-line" style="width: 100%"></div>
        <div class="sk-line" style="width: 92%"></div>
        <div class="sk-line" style="width: 78%"></div>
        <div class="sk-spacer"></div>
        <div class="sk-line sk-title" style="width: 35%"></div>
        <div class="sk-line" style="width: 100%"></div>
        <div class="sk-line" style="width: 88%"></div>
        <div class="sk-line" style="width: 65%"></div>
        <div class="sk-spacer"></div>
        <div class="sk-line sk-title" style="width: 40%"></div>
        <div class="sk-line" style="width: 95%"></div>
        <div class="sk-line" style="width: 70%"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  label: {
    type: String,
    default: 'A.R.I.A 正在分析中'
  }
})
</script>

<style scoped>
.ai-loading {
  animation: fadeIn 0.4s ease;
  margin-bottom: 8px;
}

.loading-card {
  padding: 24px;
  background: var(--color-paper);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.loading-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 24px;
}

.ai-badge {
  position: relative;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-forest), var(--color-forest-light));
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-white);
  flex-shrink: 0;
}

.badge-glow {
  position: absolute;
  inset: -3px;
  border-radius: 50%;
  background: var(--color-forest);
  opacity: 0;
  animation: glowPulse 2.5s ease-in-out infinite;
}

.loading-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  letter-spacing: 0.02em;
}

.loading-dots {
  display: inline-flex;
  gap: 3px;
  align-items: center;
}

.dot {
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: var(--color-forest);
  opacity: 0.4;
  animation: dotFade 1.4s ease-in-out infinite;
}

.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }

.skeleton-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.sk-line {
  height: 12px;
  border-radius: 6px;
  background: linear-gradient(
    90deg,
    var(--color-cloud) 0%,
    var(--color-linen) 40%,
    var(--color-cloud) 80%
  );
  background-size: 300% 100%;
  animation: shimmer 2s ease-in-out infinite;
}

.sk-title {
  height: 14px;
  width: 40%;
  border-radius: 7px;
  opacity: 0.7;
}

.sk-spacer {
  height: 6px;
}

/* 交错动画延迟 */
.sk-line:nth-child(1) { animation-delay: 0s; }
.sk-line:nth-child(2) { animation-delay: 0.05s; }
.sk-line:nth-child(3) { animation-delay: 0.1s; }
.sk-line:nth-child(4) { animation-delay: 0.15s; }
.sk-line:nth-child(5) { animation-delay: 0.2s; }
.sk-line:nth-child(6) { animation-delay: 0.25s; }
.sk-line:nth-child(7) { animation-delay: 0.3s; }
.sk-line:nth-child(8) { animation-delay: 0.35s; }
.sk-line:nth-child(9) { animation-delay: 0.4s; }
.sk-line:nth-child(10) { animation-delay: 0.45s; }
.sk-line:nth-child(11) { animation-delay: 0.5s; }
.sk-line:nth-child(12) { animation-delay: 0.55s; }

@keyframes shimmer {
  0% { background-position: 100% 0; }
  100% { background-position: -100% 0; }
}

@keyframes glowPulse {
  0%, 100% { opacity: 0; transform: scale(1); }
  50% { opacity: 0.15; transform: scale(1.3); }
}

@keyframes dotFade {
  0%, 80%, 100% { opacity: 0.3; }
  40% { opacity: 1; }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(6px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
