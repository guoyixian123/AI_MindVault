<template>
  <Teleport to="body">
    <div class="toast-container">
      <TransitionGroup name="toast">
        <div
          v-for="t in toasts"
          :key="t.id"
          :class="['toast-item', t.type]"
        >
          <span class="toast-icon">
            <svg v-if="t.type === 'success'" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M8 12l3 3 5-5"/></svg>
            <svg v-else-if="t.type === 'error'" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M15 9l-6 6M9 9l6 6"/></svg>
            <svg v-else-if="t.type === 'warning'" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2L2 22h20L12 2z"/><path d="M12 10v4M12 17v1"/></svg>
            <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 16v-5M12 8v-1"/></svg>
          </span>
          <span class="toast-text">{{ t.message }}</span>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<script setup>
import { toast } from '../composables/useToast'
const { toasts } = toast
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 24px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 9999;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  pointer-events: none;
}

.toast-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 24px;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 500;
  line-height: 1.4;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.12), 0 1px 4px rgba(0, 0, 0, 0.06);
  backdrop-filter: blur(20px);
  pointer-events: auto;
  cursor: default;
  max-width: 420px;
}

.toast-item.success {
  background: rgba(58, 114, 80, 0.95);
  color: #fff;
}

.toast-item.error {
  background: rgba(220, 38, 38, 0.95);
  color: #fff;
}

.toast-item.warning {
  background: rgba(184, 134, 11, 0.95);
  color: #fff;
}

.toast-item.info {
  background: rgba(74, 111, 165, 0.95);
  color: #fff;
}

.toast-icon {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

/* TransitionGroup 动画 */
.toast-enter-active {
  transition: all 0.4s cubic-bezier(0.22, 1, 0.36, 1);
}

.toast-leave-active {
  transition: all 0.25s ease-in;
}

.toast-enter-from {
  opacity: 0;
  transform: translateY(-20px) scale(0.95);
}

.toast-leave-to {
  opacity: 0;
  transform: translateY(-12px) scale(0.96);
}

.toast-move {
  transition: transform 0.3s ease;
}
</style>
