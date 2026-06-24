<template>
  <div class="page">
    <nav class="page-nav">
      <a href="#" class="back-link" @click.prevent="$router.back()">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
        返回
      </a>
      <h1>用药咨询</h1>
      <router-link to="/history" class="history-link">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
        历史
      </router-link>
    </nav>

    <div class="page-content">
      <!-- Tabs -->
      <div class="tabs">
        <button v-for="tab in tabs" :key="tab.value"
          class="tab-btn" :class="{ active: mode === tab.value }"
          @click="mode = tab.value; result = ''">
          <span class="tab-icon" v-html="tab.icon"></span>
          {{ tab.label }}
        </button>
      </div>

      <!-- Query -->
      <div v-if="mode === 'query'" class="card">
        <h3>药品信息查询</h3>
        <p class="card-desc">输入药品名称，获取功效、适应症、用法用量、禁忌等信息</p>
        <div class="form-group">
          <input v-model="medicineName" type="text" placeholder="请输入药品名称，如：阿莫西林" />
        </div>
        <button class="btn-primary" @click="queryMedicine" :disabled="!medicineName.trim() || loading">
          {{ loading ? '查询中...' : '查询' }}
        </button>
      </div>

      <!-- Interaction -->
      <div v-if="mode === 'interaction'" class="card">
        <h3>联用风险检测</h3>
        <p class="card-desc">输入多种药物名称，检测联用风险与相互作用</p>
        <div v-for="(med, index) in medicines" :key="index" class="med-row">
          <input v-model="medicines[index]" type="text" :placeholder="'药品' + (index + 1)" />
          <button v-if="medicines.length > 2" class="btn-remove" @click="medicines.splice(index, 1)">×</button>
        </div>
        <button class="btn-dashed" @click="medicines.push('')">+ 添加药品</button>
        <button class="btn-primary" @click="checkInteraction" :disabled="loading" style="margin-top:16px">
          {{ loading ? '检测中...' : '开始检测' }}
        </button>
      </div>

      <!-- Special -->
      <div v-if="mode === 'special'" class="card">
        <h3>特殊人群用药提示</h3>
        <p class="card-desc">选择人群类型，查询药品的禁用/慎用清单</p>
        <div class="form-group">
          <input v-model="medicineName" type="text" placeholder="请输入药品名称" />
        </div>
        <div class="form-group">
          <label>特殊人群</label>
          <div class="radio-row">
            <label v-for="p in specialPopulations" :key="p" class="radio-label">
              <input type="radio" v-model="population" :value="p" />
              <span class="radio-box">{{ p }}</span>
            </label>
          </div>
        </div>
        <button class="btn-primary" @click="checkSpecial" :disabled="!medicineName.trim() || loading">
          {{ loading ? '查询中...' : '查询' }}
        </button>
      </div>

      <!-- Emergency -->
      <div v-if="mode === 'emergency'" class="card">
        <h3>用药应急处理</h3>
        <p class="card-desc">查询药品漏服、误服后的应急处理方案</p>
        <div class="form-group">
          <input v-model="medicineName" type="text" placeholder="请输入药品名称" />
        </div>
        <button class="btn-primary" @click="queryEmergency" :disabled="!medicineName.trim() || loading">
          {{ loading ? '查询中...' : '查询应急方案' }}
        </button>
      </div>

      <!-- Result -->
      <div v-if="result" class="result-card">
        <div class="result-header">
          <h3>查询结果</h3>
          <button class="copy-btn" @click="copyResult" title="复制">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>
          </button>
        </div>
        <div class="result-body markdown-body" :class="{ 'typing-cursor': isStreaming }" v-html="renderMarkdown(displayedContent)"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useChatWebSocket } from '../utils/websocket'
import { renderMarkdown } from '../utils/markedConfig'
import { useStreamingText } from '../composables/useStreamingText'

const mode = ref('query')
const loading = ref(false)
const medicineName = ref('')
const medicines = ref(['', ''])
const population = ref('孕妇')
const result = ref('')

const chatWs = useChatWebSocket()
const { displayedContent, isStreaming, appendText, flush, resetStreaming } = useStreamingText()

const onMessage = (data) => {
  result.value += data
  appendText(data)
}

const onDone = () => {
  const wait = setInterval(() => {
    if (!isStreaming.value) {
      clearInterval(wait)
      flush(result.value)
      loading.value = false
    }
  }, 50)
}

const onError = (error) => {
  console.error('WebSocket error:', error)
  result.value = '查询失败，请确保后端服务正在运行。'
  loading.value = false
}

onMounted(() => {
  // 先移除旧回调，再添加新回调
  chatWs.removeCallback('onMessage')
  chatWs.removeCallback('onDone')
  chatWs.removeCallback('onError')

  chatWs.on('message', onMessage)
  chatWs.on('done', onDone)
  chatWs.on('error', onError)

  // 初始化连接
  chatWs.connect()
})

onUnmounted(() => {
  // 移除回调
  chatWs.removeCallback('onMessage')
  chatWs.removeCallback('onDone')
  chatWs.removeCallback('onError')
})

const tabs = [
  { value: 'query', label: '药品查询', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>' },
  { value: 'interaction', label: '联用检测', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>' },
  { value: 'special', label: '特殊人群', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>' },
  { value: 'emergency', label: '应急处理', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>' }
]

const specialPopulations = ['孕妇', '哺乳期女性', '婴幼儿', '老年人', '高血压患者', '糖尿病患者', '肝肾功能不全患者']

function queryMedicine() { callApi({ subScenario: 'query', medicineName: medicineName.value }) }
function checkInteraction() {
  const valid = medicines.value.filter(m => m.trim())
  if (valid.length < 2) { alert('请至少输入两种药品'); return }
  callApi({ subScenario: 'interaction', medicineNames: valid })
}
function checkSpecial() { callApi({ subScenario: 'special', medicineName: medicineName.value, population: population.value }) }
function queryEmergency() { callApi({ subScenario: 'emergency', medicineName: medicineName.value }) }

function callApi(body) {
  loading.value = true
  result.value = ''
  resetStreaming()

  chatWs.send('', {
    memoryId: 'medicine-' + Date.now(),
    scenario: 'medicine',
    ...body
  })
}

function copyResult() {
  navigator.clipboard.writeText(result.value)
}
</script>

<style scoped>
.page { min-height: 100vh; background: var(--bg-primary); }

.page-nav {
  background: var(--color-white);
  border-bottom: 1px solid var(--border-light);
  padding: 0 32px;
  height: 60px;
  display: flex;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 10;
}

.back-link {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: var(--text-muted);
  transition: color var(--transition-fast);
  width: 80px;
}
.back-link:hover { color: var(--color-forest); }

.page-nav h1 {
  flex: 1;
  text-align: center;
  font-size: 18px;
  font-family: var(--font-body);
  font-weight: 600;
}

.history-link {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
  font-size: 14px;
  color: var(--text-muted);
  transition: color var(--transition-fast);
  width: 80px;
}
.history-link:hover { color: var(--color-forest); }

.page-content {
  max-width: 800px;
  margin: 32px auto;
  padding: 0 24px;
}

.tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.tab-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-sm);
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
  background: var(--color-white);
  transition: all var(--transition-fast);
}

.tab-btn:hover {
  border-color: var(--color-forest);
  color: var(--color-forest);
}

.tab-btn.active {
  background: var(--color-forest);
  color: var(--color-white);
  border-color: var(--color-forest);
}

.tab-icon { line-height: 0; display: flex; }

.card {
  background: var(--color-white);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 32px;
  margin-bottom: 20px;
}

.card h3 {
  font-size: 1.3rem;
  margin-bottom: 6px;
}

.card-desc {
  font-size: 14px;
  color: var(--text-muted);
  margin-bottom: 20px;
}

.form-group { margin-bottom: 16px; }

.form-group label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.form-group input {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-sm);
  font-size: 15px;
  color: var(--text-primary);
  outline: none;
  transition: border-color var(--transition-fast);
}

.form-group input:focus { border-color: var(--color-forest); }

.med-row {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.med-row input {
  flex: 1;
  padding: 12px 14px;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-sm);
  font-size: 15px;
  outline: none;
}

.med-row input:focus { border-color: var(--color-forest); }

.btn-remove {
  width: 40px;
  background: rgba(168,80,58,0.08);
  color: var(--color-danger);
  border: none;
  border-radius: var(--radius-sm);
  font-size: 18px;
}

.btn-dashed {
  width: 100%;
  padding: 10px;
  background: none;
  border: 1px dashed var(--border-medium);
  border-radius: var(--radius-sm);
  color: var(--text-muted);
  font-size: 14px;
  transition: all var(--transition-fast);
}

.btn-dashed:hover {
  border-color: var(--color-forest);
  color: var(--color-forest);
}

.radio-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.radio-label { cursor: pointer; }
.radio-label input { display: none; }

.radio-box {
  display: inline-block;
  padding: 8px 14px;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-sm);
  font-size: 13px;
  color: var(--text-secondary);
  transition: all var(--transition-fast);
}

.radio-label input:checked + .radio-box {
  border-color: var(--color-forest);
  background: rgba(45,90,61,0.06);
  color: var(--color-forest);
}

.btn-primary {
  width: 100%;
  padding: 13px;
  background: var(--color-forest);
  color: var(--color-white);
  font-size: 15px;
  font-weight: 500;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.btn-primary:hover:not(:disabled) { background: var(--color-forest-dark); }
.btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }

.result-card {
  background: var(--color-white);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 32px;
  margin-top: 24px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.result-header h3 { font-size: 1.1rem; }

.result-body {
  font-size: 15px;
  line-height: 1.8;
  color: var(--text-primary);
  padding: 24px;
  background: var(--color-paper);
  border-radius: var(--radius-md);
  position: relative;
}

@media (max-width: 767px) {
  .page-nav { padding: 0 16px; }
  .page-content { padding: 0 16px; }
  .tabs { gap: 6px; }
  .tab-btn { padding: 8px 12px; font-size: 13px; }
  .card { padding: 24px 20px; }
}
</style>
