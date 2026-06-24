<template>
  <div class="auth-page">
    <div class="auth-layout">
      <div class="auth-visual">
        <div class="visual-content">
          <router-link to="/" class="visual-logo">
            <img src="/favicon.svg" alt="logo" width="32" height="32" />
            <span>A.R.I.A</span>
          </router-link>
          <h2>开始您的<br/>健康管理之旅</h2>
          <p>建立个人健康档案，获取专业医疗咨询建议</p>
        </div>
        <div class="visual-pattern">
          <span v-for="i in 20" :key="i" class="pattern-dot"></span>
        </div>
      </div>

      <div class="auth-form-wrap">
        <div class="auth-form">
          <h1>注册</h1>
          <p class="auth-subtitle">创建您的健康账户</p>

          <form @submit.prevent="handleRegister">
            <div class="form-group">
              <label>用户名 *</label>
              <input v-model="form.username" type="text" placeholder="3-50个字符" required />
            </div>
            <div class="form-group">
              <label>密码 *</label>
              <input v-model="form.password" type="password" placeholder="至少6个字符" required />
            </div>
            <div class="form-group">
              <label>确认密码 *</label>
              <input v-model="confirmPassword" type="password" placeholder="再次输入密码" required />
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>昵称</label>
                <input v-model="form.nickname" type="text" placeholder="选填" />
              </div>
              <div class="form-group">
                <label>手机号</label>
                <input v-model="form.phone" type="tel" placeholder="选填" />
              </div>
            </div>
            <div v-if="error" class="error-msg">{{ error }}</div>
            <button type="submit" class="submit-btn" :disabled="loading">
              {{ loading ? '注册中...' : '注册' }}
            </button>
          </form>

          <div class="auth-footer">
            <span>已有账号？</span>
            <router-link to="/login">立即登录</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const form = ref({ username: '', password: '', nickname: '', phone: '', email: '' })
const confirmPassword = ref('')
const error = ref('')
const loading = ref(false)

async function handleRegister() {
  error.value = ''
  if (form.value.password !== confirmPassword.value) {
    error.value = '两次输入的密码不一致'
    return
  }
  if (form.value.password.length < 6) {
    error.value = '密码长度至少6个字符'
    return
  }
  loading.value = true
  try {
    await authStore.register(form.value)
    router.push('/dashboard')
  } catch (e) {
    error.value = e.message || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page { min-height: 100vh; }
.auth-layout { display: grid; grid-template-columns: 1fr 1fr; min-height: 100vh; }
.auth-visual {
  background: var(--color-forest);
  color: var(--color-cream);
  padding: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}
.visual-content { position: relative; z-index: 2; max-width: 400px; }
.visual-logo {
  display: flex; align-items: center; gap: 10px;
  color: var(--color-cream); font-family: var(--font-display); font-size: 22px; margin-bottom: 48px;
}
.visual-content h2 {
  font-size: 2.5rem; line-height: 1.2; color: var(--color-cream); margin-bottom: 16px;
}
.visual-content p { font-size: 16px; line-height: 1.6; color: rgba(250,248,245,0.7); }
.visual-pattern {
  position: absolute; inset: 0; display: grid;
  grid-template-columns: repeat(5, 1fr); gap: 32px; padding: 32px; opacity: 0.15;
}
.pattern-dot { width: 6px; height: 6px; border-radius: 50%; background: var(--color-cream); }

.auth-form-wrap {
  display: flex; align-items: center; justify-content: center;
  padding: 48px; background: var(--color-white);
}
.auth-form { width: 100%; max-width: 400px; }
.auth-form h1 { font-size: 2rem; margin-bottom: 8px; }
.auth-subtitle { color: var(--text-muted); margin-bottom: 32px; font-size: 15px; }
.form-group { margin-bottom: 16px; }
.form-group label {
  display: block; font-size: 13px; font-weight: 600; color: var(--text-secondary);
  margin-bottom: 6px; text-transform: uppercase; letter-spacing: 0.05em;
}
.form-group input {
  width: 100%; padding: 12px 16px; border: 1px solid var(--border-light);
  border-radius: var(--radius-sm); font-size: 15px; color: var(--text-primary);
  background: var(--color-white); transition: all var(--transition-fast); outline: none;
}
.form-group input::placeholder { color: var(--color-mist); }
.form-group input:focus { border-color: var(--color-forest); box-shadow: 0 0 0 3px rgba(45,90,61,0.1); }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.error-msg {
  color: var(--color-danger); font-size: 14px; padding: 10px 14px;
  background: rgba(168,80,58,0.08); border-radius: var(--radius-sm); margin-bottom: 16px;
}
.submit-btn {
  width: 100%; padding: 14px; background: var(--color-forest); color: var(--color-white);
  font-size: 15px; font-weight: 500; border-radius: var(--radius-sm);
  transition: all var(--transition-fast); margin-top: 8px;
}
.submit-btn:hover:not(:disabled) { background: var(--color-forest-dark); }
.submit-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.auth-footer {
  text-align: center; margin-top: 24px; font-size: 14px; color: var(--text-muted);
}
.auth-footer a { color: var(--color-forest); font-weight: 500; margin-left: 4px; }
.auth-footer a:hover { text-decoration: underline; }

@media (max-width: 767px) {
  .auth-layout { grid-template-columns: 1fr; }
  .auth-visual { display: none; }
  .auth-form-wrap { padding: 32px 24px; }
  .form-row { grid-template-columns: 1fr; }
}
</style>
