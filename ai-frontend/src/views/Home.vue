<template>
  <div class="home">
    <!-- Navbar -->
    <nav class="navbar">
      <div class="nav-inner">
        <router-link to="/" class="nav-logo">
          <img src="/favicon.svg" alt="logo" width="28" height="28" />
          <span>A.R.I.A</span>
        </router-link>

        <div class="nav-links desktop-only">
          <router-link to="/symptom">症状自查</router-link>
          <router-link to="/medicine">用药咨询</router-link>
          <router-link to="/disease">疾病百科</router-link>
          <router-link to="/consultation">问诊社区</router-link>
        </div>

        <div class="nav-actions">
          <template v-if="!authStore.isLoggedIn">
            <router-link to="/login" class="nav-btn-text">登录</router-link>
            <router-link to="/register" class="nav-btn-filled">注册</router-link>
          </template>
          <template v-else>
            <div class="user-dropdown" ref="dropdownRef">
              <div class="user-trigger" @click="toggleDropdown">
                <div class="user-avatar">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                    <circle cx="12" cy="7" r="4"/>
                  </svg>
                </div>
                <span class="user-nickname">{{ authStore.user?.nickname || authStore.user?.username }}</span>
                <svg class="dropdown-arrow" :class="{ open: dropdownOpen }" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M6 9l6 6 6-6"/>
                </svg>
              </div>
              <Transition name="dropdown">
                <div v-if="dropdownOpen" class="dropdown-menu">
                  <router-link to="/dashboard" class="dropdown-item" @click="dropdownOpen = false">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                      <circle cx="12" cy="7" r="4"/>
                    </svg>
                    我的健康
                  </router-link>
                  <div class="dropdown-divider"></div>
                  <a href="#" class="dropdown-item logout" @click.prevent="handleLogout">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                      <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                      <polyline points="16 17 21 12 16 7"/>
                      <line x1="21" y1="12" x2="9" y2="12"/>
                    </svg>
                    退出登录
                  </a>
                </div>
              </Transition>
            </div>
          </template>
        </div>

        <button class="hamburger" :class="{ active: menuOpen }" @click="menuOpen = !menuOpen">
          <span></span><span></span><span></span>
        </button>
      </div>
    </nav>

    <!-- Mobile menu -->
    <Transition name="slide">
      <div v-if="menuOpen" class="mobile-overlay">
        <div class="mobile-nav">
          <router-link to="/symptom" @click="menuOpen = false">症状自查</router-link>
          <router-link to="/medicine" @click="menuOpen = false">用药咨询</router-link>
          <router-link to="/disease" @click="menuOpen = false">疾病百科</router-link>
          <router-link to="/consultation" @click="menuOpen = false">问诊社区</router-link>
          <div class="mobile-divider"></div>
          <template v-if="!authStore.isLoggedIn">
            <router-link to="/login" @click="menuOpen = false">登录</router-link>
            <router-link to="/register" @click="menuOpen = false">注册</router-link>
          </template>
          <template v-else>
            <router-link to="/dashboard" @click="menuOpen = false">我的健康</router-link>
          </template>
        </div>
      </div>
    </Transition>

    <!-- Hero -->
    <section class="hero">
      <div class="hero-inner">
        <div class="hero-content">
          <div class="hero-badge">专业 · 严谨 · 通俗</div>
          <h1 class="hero-title">
            您的私人<br/>
            <span class="title-accent">健康顾问</span>
          </h1>
          <p class="hero-desc">
            基于医学知识库，为您提供症状分析、用药指导、疾病科普等专业健康咨询服务。
          </p>
          <div class="hero-actions">
            <router-link to="/symptom" class="btn-primary">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>
              开始自查
            </router-link>
            <router-link to="/medicine" class="btn-secondary">
              药品查询
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
            </router-link>
          </div>
        </div>

        <div class="hero-visual">
          <RobotAnimated />
        </div>
      </div>
    </section>

    <!-- Features -->
    <section class="features">
      <div class="features-inner">
        <div class="section-header">
          <h2>核心服务</h2>
          <p>覆盖日常健康管理的主要场景</p>
        </div>
        <div class="feature-grid">
          <router-link to="/symptom" class="feature-card">
            <div class="feature-icon">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M16 4v24M8 12h16M10 8l6 8-6 8" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <h3>症状自查</h3>
            <p>多部位症状分析，分层风险判断，给出居家护理或就医建议</p>
          </router-link>
          <router-link to="/medicine" class="feature-card">
            <div class="feature-icon">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none" stroke="currentColor" stroke-width="1.5">
                <rect x="8" y="4" width="16" height="24" rx="4"/>
                <path d="M12 12h8M16 10v4"/>
              </svg>
            </div>
            <h3>用药咨询</h3>
            <p>药品信息查询、联用风险检测、特殊人群用药提示</p>
          </router-link>
          <router-link to="/disease" class="feature-card">
            <div class="feature-icon">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M6 6h20v20H6z" rx="2"/>
                <path d="M6 12h20M12 6v20"/>
              </svg>
            </div>
            <h3>疾病科普</h3>
            <p>常见病慢性病解读、相似病症鉴别、健康谣言辟谣</p>
          </router-link>
          <router-link to="/health/profile" class="feature-card">
            <div class="feature-icon">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M8 4h16v24H8z" rx="2"/>
                <path d="M12 10h8M12 14h8M12 18h5"/>
              </svg>
            </div>
            <h3>健康档案</h3>
            <p>体检报告解读、健康趋势分析、慢病长期管理</p>
          </router-link>
        </div>
      </div>
    </section>

    <!-- Footer -->
    <footer class="footer">
      <div class="footer-inner">
        <span>© 2026 A.R.I.A</span>
        <span class="footer-divider">·</span>
        <span>专业医疗健康咨询平台</span>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import RobotAnimated from '../components/RobotAnimated.vue'

const router = useRouter()
const authStore = useAuthStore()
const menuOpen = ref(false)
const dropdownOpen = ref(false)
const dropdownRef = ref(null)

function toggleDropdown() {
  dropdownOpen.value = !dropdownOpen.value
}

function handleClickOutside(event) {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target)) {
    dropdownOpen.value = false
  }
}

function handleLogout() {
  dropdownOpen.value = false
  authStore.logout()
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.home {
  min-height: 100vh;
  background: var(--bg-primary);
}

/* — Navbar — */
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  padding: 0 32px;
  height: 64px;
  background: var(--color-white);
  border-bottom: 1px solid var(--border-light);
}

.nav-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--color-forest);
  font-family: var(--font-display);
  font-size: 20px;
}

.nav-links {
  display: flex;
  gap: 32px;
}

.nav-links a {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
  transition: color var(--transition-fast);
  position: relative;
}

.nav-links a::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 0;
  height: 1.5px;
  background: var(--color-forest);
  transition: width var(--transition-base);
}

.nav-links a:hover {
  color: var(--color-forest);
}

.nav-links a:hover::after {
  width: 100%;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-dropdown {
  position: relative;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.user-trigger:hover {
  background: var(--color-paper);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--color-paper);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-forest);
}

.user-nickname {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.dropdown-arrow {
  color: var(--text-muted);
  transition: transform var(--transition-fast);
}

.dropdown-arrow.open {
  transform: rotate(180deg);
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 160px;
  background: var(--color-white);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  padding: 8px;
  z-index: 1000;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  font-size: 14px;
  color: var(--text-primary);
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
  text-decoration: none;
}

.dropdown-item:hover {
  background: var(--color-paper);
  color: var(--color-forest);
}

.dropdown-item.logout {
  color: var(--text-muted);
}

.dropdown-item.logout:hover {
  color: var(--color-danger);
  background: #fef2f2;
}

.dropdown-divider {
  height: 1px;
  background: var(--border-light);
  margin: 4px 0;
}

.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.nav-btn-text {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
  transition: color var(--transition-fast);
}

.nav-btn-text:hover {
  color: var(--color-forest);
}

.nav-btn-filled {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-white);
  background: var(--color-forest);
  padding: 8px 20px;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.nav-btn-filled:hover {
  background: var(--color-forest-dark);
}

.hamburger {
  display: none;
  flex-direction: column;
  gap: 5px;
  padding: 4px;
}

.hamburger span {
  display: block;
  width: 20px;
  height: 1.5px;
  background: var(--text-primary);
  transition: all 0.3s;
}

.hamburger.active span:nth-child(1) {
  transform: rotate(45deg) translate(4px, 4px);
}

.hamburger.active span:nth-child(2) {
  opacity: 0;
}

.hamburger.active span:nth-child(3) {
  transform: rotate(-45deg) translate(5px, -5px);
}

/* — Mobile — */
.mobile-overlay {
  position: fixed;
  inset: 0;
  top: 64px;
  background: var(--color-white);
  z-index: 99;
}

.mobile-nav {
  display: flex;
  flex-direction: column;
  padding: 24px 32px;
}

.mobile-nav a {
  font-size: 18px;
  font-weight: 500;
  color: var(--text-primary);
  padding: 16px 0;
  border-bottom: 1px solid var(--border-light);
}

.mobile-divider {
  height: 1px;
  background: var(--border-light);
  margin: 8px 0;
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s var(--ease-out);
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  transform: translateY(-16px);
}

/* — Hero — */
.hero {
  padding: 120px 32px 80px;
  min-height: 85vh;
  display: flex;
  align-items: center;
}

.hero-inner {
  max-width: 1100px;
  margin: 0 auto;
  width: 100%;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 48px;
  align-items: center;
}

.hero-badge {
  display: inline-block;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.15em;
  color: var(--color-terracotta);
  text-transform: uppercase;
  margin-bottom: 24px;
  padding: 6px 14px;
  border: 1px solid var(--color-terracotta);
  border-radius: var(--radius-xs);
}

.hero-title {
  font-size: clamp(2.5rem, 5vw, 3.8rem);
  line-height: 1.15;
  margin-bottom: 24px;
  color: var(--color-charcoal);
}

.title-accent {
  color: var(--color-forest);
  font-style: italic;
}

.hero-desc {
  font-size: 17px;
  line-height: 1.7;
  color: var(--text-secondary);
  margin-bottom: 40px;
  max-width: 480px;
}

.hero-actions {
  display: flex;
  gap: 16px;
  align-items: center;
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 28px;
  background: var(--color-forest);
  color: var(--color-white);
  font-size: 15px;
  font-weight: 500;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.btn-primary:hover {
  background: var(--color-forest-dark);
  transform: translateY(-1px);
}

.btn-secondary {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 14px 24px;
  background: transparent;
  color: var(--text-primary);
  font-size: 15px;
  font-weight: 500;
  border: 1px solid var(--border-medium);
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.btn-secondary:hover {
  border-color: var(--color-forest);
  color: var(--color-forest);
}

/* — Hero Visual — */
.hero-visual {
  display: flex;
  align-items: center;
  justify-content: center;
  padding-top: 40px;
}

/* — Features — */
.features {
  padding: 80px 32px;
  background: var(--color-white);
}

.features-inner {
  max-width: 1200px;
  margin: 0 auto;
}

.section-header {
  margin-bottom: 48px;
}

.section-header h2 {
  font-size: 2rem;
  margin-bottom: 8px;
}

.section-header p {
  font-size: 16px;
  color: var(--text-muted);
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.feature-card {
  padding: 32px 24px;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  transition: all var(--transition-base);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-card:hover {
  border-color: var(--color-forest);
  box-shadow: var(--shadow-md);
  transform: translateY(-4px);
}

.feature-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-paper);
  border-radius: var(--radius-sm);
  color: var(--color-forest);
}

.feature-card h3 {
  font-size: 1.2rem;
  color: var(--text-primary);
}

.feature-card p {
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-muted);
}

/* — Footer — */
.footer {
  padding: 24px 32px;
  border-top: 1px solid var(--border-light);
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-muted);
}

.footer-divider {
  color: var(--color-cloud);
}

/* — Responsive — */
@media (max-width: 767px) {
  .desktop-only { display: none !important; }
  .hamburger { display: flex; }
  .nav-actions { display: none; }

  .hero { padding: 100px 20px 60px; min-height: auto; }
  .hero-inner {
    grid-template-columns: 1fr;
    gap: 40px;
  }
  .hero-visual { order: -1; }
  .hero-title { font-size: 2.2rem; }
  .hero-actions { flex-direction: column; align-items: flex-start; }

  .feature-grid { grid-template-columns: 1fr 1fr; gap: 16px; }
  .features { padding: 48px 20px; }
}

@media (min-width: 768px) and (max-width: 1023px) {
  .feature-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
