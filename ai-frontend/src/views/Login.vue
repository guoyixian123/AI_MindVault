<template>
  <div class="auth-page">
    <div class="auth-layout">
      <!-- Left visual -->
      <div class="auth-visual">
        <div class="visual-content">
          <router-link to="/" class="visual-logo">
            <img src="/favicon.svg" alt="logo" width="32" height="32" />
            <span>A.R.I.A</span>
          </router-link>
          <h2>专业医疗健康<br/>咨询平台</h2>
          <p>基于医学知识库，为您提供可信赖的健康咨询服务</p>
        </div>
        <div class="visual-pattern">
          <span v-for="i in 20" :key="i" class="pattern-dot"></span>
        </div>
      </div>

      <!-- Right form -->
      <div class="auth-form-wrap">
        <div class="auth-form">
          <h1>登录</h1>
          <p class="auth-subtitle">欢迎回来，请输入您的账号信息</p>

          <!-- 登录身份选择 -->
          <div class="login-tabs">
            <button
              class="tab-btn"
              :class="{ active: loginRole === 'user' }"
              @click="loginRole = 'user'"
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              用户登录
            </button>
            <button
              class="tab-btn"
              :class="{ active: loginRole === 'admin' }"
              @click="loginRole = 'admin'"
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                <path d="M9 12l2 2 4-4"/>
              </svg>
              管理端登录
            </button>
          </div>

          <form @submit.prevent="handleLogin">
            <div class="form-group">
              <label>用户名</label>
              <input v-model="form.username" type="text" :placeholder="loginRole === 'admin' ? '请输入管理员账号' : '请输入用户名'" required />
            </div>

            <div class="form-group">
              <label>密码</label>
              <input v-model="form.password" type="password" placeholder="请输入密码" required />
            </div>

            <div v-if="error" class="error-msg">{{ error }}</div>

            <button type="submit" class="submit-btn" :disabled="loading">
              {{ loading ? '登录中...' : '登录' }}
            </button>
          </form>

          <div class="auth-footer">
            <span>还没有账号？</span>
            <router-link to="/register">立即注册</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const form = ref({ username: '', password: '' })
const error = ref('')
const loading = ref(false)
const loginRole = ref('user')

// 根据 URL 参数设置登录角色
onMounted(() => {
  if (route.query.role === 'admin') {
    loginRole.value = 'admin'
  }
})

async function handleLogin() {
  error.value = ''
  loading.value = true
  try {
    const user = await authStore.login(form.value.username, form.value.password)

    // 严格验证登录身份是否匹配
    if (loginRole.value === 'admin') {
      // 管理端登录：只允许 ROOT_ADMIN 或 DOCTOR
      if (user.role === 'ROOT_ADMIN' || user.role === 'DOCTOR') {
        router.push('/admin')
      } else {
        error.value = '该账号没有管理员权限，请使用用户登录'
        authStore.logout()
      }
    } else {
      // 用户端登录：只允许普通用户
      if (user.role === 'ROOT_ADMIN' || user.role === 'DOCTOR') {
        error.value = '管理员账号请使用管理端登录'
        authStore.logout()
      } else {
        router.push('/')
      }
    }
  } catch (e) {
    error.value = e.message || '登录失败，请检查用户名和密码'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
}

.auth-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  min-height: 100vh;
}

/* — Left visual — */
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

.visual-content {
  position: relative;
  z-index: 2;
  max-width: 400px;
}

.visual-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--color-cream);
  font-family: var(--font-display);
  font-size: 22px;
  margin-bottom: 48px;
}

.visual-content h2 {
  font-size: 2.5rem;
  line-height: 1.2;
  color: var(--color-cream);
  margin-bottom: 16px;
}

.visual-content p {
  font-size: 16px;
  line-height: 1.6;
  color: rgba(250, 248, 245, 0.7);
}

.visual-pattern {
  position: absolute;
  inset: 0;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 32px;
  padding: 32px;
  opacity: 0.15;
}

.pattern-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-cream);
}

/* — Right form — */
.auth-form-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
  background: var(--color-white);
}

.auth-form {
  width: 100%;
  max-width: 380px;
}

.auth-form h1 {
  font-size: 2rem;
  margin-bottom: 8px;
}

.auth-subtitle {
  color: var(--text-muted);
  margin-bottom: 24px;
  font-size: 15px;
}

.login-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  padding: 4px;
  background: var(--color-paper);
  border-radius: var(--radius-sm);
}

.tab-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 16px;
  border: none;
  border-radius: var(--radius-xs);
  background: transparent;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-muted);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.tab-btn:hover {
  color: var(--text-primary);
}

.tab-btn.active {
  background: var(--color-white);
  color: var(--color-forest);
  box-shadow: var(--shadow-xs);
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 6px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.form-group input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-sm);
  font-size: 15px;
  color: var(--text-primary);
  background: var(--color-white);
  transition: all var(--transition-fast);
  outline: none;
}

.form-group input::placeholder {
  color: var(--color-mist);
}

.form-group input:focus {
  border-color: var(--color-forest);
  box-shadow: 0 0 0 3px rgba(45, 90, 61, 0.1);
}

.error-msg {
  color: var(--color-danger);
  font-size: 14px;
  padding: 10px 14px;
  background: rgba(168, 80, 58, 0.08);
  border-radius: var(--radius-sm);
  margin-bottom: 16px;
}

.submit-btn {
  width: 100%;
  padding: 14px;
  background: var(--color-forest);
  color: var(--color-white);
  font-size: 15px;
  font-weight: 500;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
  margin-top: 8px;
}

.submit-btn:hover:not(:disabled) {
  background: var(--color-forest-dark);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.auth-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: var(--text-muted);
}

.auth-footer a {
  color: var(--color-forest);
  font-weight: 500;
  margin-left: 4px;
}

.auth-footer a:hover {
  text-decoration: underline;
}

@media (max-width: 767px) {
  .auth-layout {
    grid-template-columns: 1fr;
  }
  .auth-visual {
    display: none;
  }
  .auth-form-wrap {
    padding: 32px 24px;
  }
}
</style>
