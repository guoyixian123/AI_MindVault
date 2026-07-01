<template>
  <div class="ai-loading" :class="{ 'compact': compact }">
    <div class="ai-loading-inner">
      <div class="pulse-ring"></div>
      <div class="ai-icon">
        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M12 2a4 4 0 0 1 4 4v1a3 3 0 0 1 3 3v1a2 2 0 0 1-2 2h-1l1 5H7l1-5H7a2 2 0 0 1-2-2v-1a3 3 0 0 1 3-3V6a4 4 0 0 1 4-4z"/>
          <circle cx="12" cy="14" r="1"/>
          <path d="M8 18h8"/>
          <path d="M9 21h6"/>
        </svg>
      </div>
      <div class="loading-text">
        <span class="loading-label">{{ label }}</span>
        <span class="loading-dots">
          <span class="dot"></span>
          <span class="dot"></span>
          <span class="dot"></span>
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  label: {
    type: String,
    default: 'A.R.I.A 正在分析中'
  },
  compact: {
    type: Boolean,
    default: false
  }
})
</script>

<style scoped>
.ai-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  animation: fadeIn 0.3s ease;
}

.ai-loading.compact {
  padding: 32px 24px;
}

.ai-loading-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  position: relative;
}

.pulse-ring {
  position: absolute;
  top: -8px;
  width: 52px;
  height: 52px;
  border-radius: 50%;
  border: 2px solid var(--color-forest);
  opacity: 0;
  animation: pulseRing 2s ease-out infinite;
}

.ai-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-forest), var(--color-forest-light));
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-white);
  animation: floatIcon 3s ease-in-out infinite;
  box-shadow: 0 4px 16px rgba(45, 90, 61, 0.2);
}

.loading-text {
  display: flex;
  align-items: center;
  gap: 4px;
}

.loading-label {
  font-size: 14px;
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
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: var(--color-forest);
  animation: dotBounce 1.4s ease-in-out infinite;
}

.dot:nth-child(2) {
  animation-delay: 0.2s;
}

.dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes pulseRing {
  0% {
    transform: scale(0.8);
    opacity: 0.6;
  }
  100% {
    transform: scale(1.6);
    opacity: 0;
  }
}

@keyframes floatIcon {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-6px);
  }
}

@keyframes dotBounce {
  0%, 80%, 100% {
    transform: scale(0.6);
    opacity: 0.4;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>