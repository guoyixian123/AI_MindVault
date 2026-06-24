import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import './style.css'
import './styles/markdown.css'

// 动态设置 favicon，防止浏览器缓存旧图标
function setFavicon() {
  let link = document.querySelector("link[rel~='icon']")
  if (!link) {
    link = document.createElement('link')
    link.rel = 'icon'
    document.head.appendChild(link)
  }
  link.type = 'image/svg+xml'
  link.href = '/favicon.svg?v=' + Date.now()
}
setFavicon()
router.afterEach(() => setFavicon())

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')
