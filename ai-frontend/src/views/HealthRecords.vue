<template>
  <div class="page">
        <nav class="page-nav">
      <a href="#" class="back-link" @click.prevent="$router.back()">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
        返回
      </a>
      <h1>健康数据记录</h1>
      <div class="nav-spacer"></div>
    </nav>


    <div class="page-content">
      <!-- 数据录入 -->
      <div class="section-card">
        <h2>今日数据录入</h2>
        <form @submit.prevent="saveRecord" class="record-form">
          <div class="form-grid">
            <div class="form-group">
              <label>收缩压 (mmHg)</label>
              <input v-model.number="form.bloodPressureSys" type="number" placeholder="120" />
            </div>
            <div class="form-group">
              <label>舒张压 (mmHg)</label>
              <input v-model.number="form.bloodPressureDia" type="number" placeholder="80" />
            </div>
            <div class="form-group">
              <label>血糖 (mmol/L)</label>
              <input v-model.number="form.bloodSugar" type="number" step="0.1" placeholder="5.6" />
            </div>
            <div class="form-group">
              <label>步数</label>
              <input v-model.number="form.steps" type="number" placeholder="8000" />
            </div>
            <div class="form-group">
              <label>睡眠时长 (小时)</label>
              <input v-model.number="form.sleepHours" type="number" step="0.5" placeholder="7.5" />
            </div>
            <div class="form-group">
              <label>体温 (°C)</label>
              <input v-model.number="form.bodyTemperature" type="number" step="0.1" placeholder="36.5" />
            </div>
          </div>
          <div class="form-group">
            <label>生理期记录（女性选填）</label>
            <input v-model="form.menstrualNote" type="text" placeholder="如：第3天" />
          </div>
          <button type="submit" class="submit-btn" :disabled="saving">
            {{ saving ? '保存中...' : '保存今日数据' }}
          </button>
        </form>
              </div>

      <!-- 趋势图 -->
      <div class="section-card">
        <h2>数据趋势</h2>
        <div class="trend-tabs">
          <button v-for="ind in indicators" :key="ind.value"
            class="trend-tab" :class="{ active: selectedIndicator === ind.value }"
            @click="selectIndicator(ind.value)">
            {{ ind.label }}
          </button>
        </div>
        <div class="chart-container">
          <canvas ref="chartCanvas"></canvas>
        </div>
        <div v-if="warnings.length > 0" class="warnings">
          <div v-for="w in warnings" :key="w" class="warning-item">{{ w }}</div>
        </div>
      </div>

      <!-- 历史记录 -->
      <div class="section-card">
        <h2>历史记录</h2>
        <div class="record-list">
          <div v-for="record in records" :key="record.id" class="record-item">
            <div class="record-date">{{ record.recordDate }}</div>
            <div class="record-data">
              <span v-if="record.bloodPressureSys">血压: {{ record.bloodPressureSys }}/{{ record.bloodPressureDia }}</span>
              <span v-if="record.bloodSugar">血糖: {{ record.bloodSugar }}</span>
              <span v-if="record.steps">步数: {{ record.steps }}</span>
              <span v-if="record.sleepHours">睡眠: {{ record.sleepHours }}h</span>
            </div>
          </div>
          <div v-if="records.length === 0" class="empty">暂无记录</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useHealthStore } from '../stores/health'
import { toast } from '../composables/useToast'
import { Chart, registerables } from 'chart.js'

Chart.register(...registerables)

const healthStore = useHealthStore()
const saving = ref(false)
const records = ref([])
const warnings = ref([])
const selectedIndicator = ref('blood_pressure_sys')
const chartCanvas = ref(null)
let chartInstance = null

const form = ref({
  recordDate: new Date().toISOString().split('T')[0],
  bloodPressureSys: null,
  bloodPressureDia: null,
  bloodSugar: null,
  steps: null,
  sleepHours: null,
  bodyTemperature: null,
  menstrualNote: ''
})

const indicators = [
  { value: 'blood_pressure_sys', label: '收缩压' },
  { value: 'blood_pressure_dia', label: '舒张压' },
  { value: 'blood_sugar', label: '血糖' },
  { value: 'steps', label: '步数' },
  { value: 'sleep_hours', label: '睡眠' },
  { value: 'body_temperature', label: '体温' }
]

onMounted(async () => {
  await loadRecords()
  await loadTrend()
})

async function loadRecords() {
  records.value = await healthStore.fetchRecords(30) || []
}

async function loadTrend() {
  const data = await healthStore.fetchTrend(selectedIndicator.value, 30)
  if (data) {
    warnings.value = data.warnings || []
    await nextTick()
    renderChart(data.data || [])
  }
}

function selectIndicator(value) {
  selectedIndicator.value = value
  loadTrend()
}

function renderChart(data) {
  if (chartInstance) {
    chartInstance.destroy()
  }

  if (!chartCanvas.value) return

  chartInstance = new Chart(chartCanvas.value, {
    type: 'line',
    data: {
      labels: data.map(d => d.date),
      datasets: [{
        label: indicators.find(i => i.value === selectedIndicator.value)?.label || '',
        data: data.map(d => d.value),
        borderColor: '#0d9488',
        backgroundColor: 'rgba(13, 148, 136, 0.1)',
        fill: true,
        tension: 0.3
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false }
      },
      scales: {
        x: { display: true, grid: { display: false } },
        y: { display: true, beginAtZero: false }
      }
    }
  })
}

async function saveRecord() {
  saving.value = true
  try {
    const data = await healthStore.saveRecord(form.value)
    if (data.code === 200) {
      toast.success('数据保存成功！')
      await loadRecords()
      await loadTrend()
    } else {
      toast.error(data.message || '保存失败')
    }
  } catch (e) {
    toast.error('保存失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: var(--bg-primary); }

.page-nav {
  background: var(--color-white);
  border-bottom: 1px solid var(--border-light);
  padding: 0 32px;
  height: 60px;
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;

}

.page-nav h1 {
  flex: 1;
  text-align: center;
}



.back-link {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: var(--text-muted);
  transition: color var(--transition-fast);
}

.back-link:hover { color: var(--color-forest); }

.nav-spacer {
  min-width: 80px;
}

.page-nav h1 {
  font-size: 18px;
  font-family: var(--font-body);
  font-weight: 600;
}

.page-content {
  max-width: 800px;
  margin: 32px auto;
  padding: 0 24px;
}

.section-card {
  background: var(--color-white);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 28px;
  margin-bottom: 20px;
}

.section-card h2 {
  font-size: 1.2rem;
  margin-bottom: 20px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
}

.form-group {
  margin-bottom: 12px;
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
  padding: 12px 14px;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text-primary);
  outline: none;
  transition: border-color var(--transition-fast);
  background: var(--color-white);
}

.form-group input:focus {
  border-color: var(--color-forest);
}

.submit-btn {
  padding: 13px 32px;
  background: var(--color-forest);
  color: var(--color-white);
  border: none;
  border-radius: var(--radius-sm);
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  margin-top: 8px;
  transition: all var(--transition-fast);
}

.submit-btn:hover:not(:disabled) {
  background: var(--color-forest-dark);
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.trend-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.trend-tab {
  padding: 8px 16px;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-sm);
  background: var(--color-white);
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.trend-tab:hover {
  border-color: var(--color-forest);
  color: var(--color-forest);
}

.trend-tab.active {
  background: var(--color-forest);
  color: var(--color-white);
  border-color: var(--color-forest);
}

.chart-container {
  height: 250px;
  margin-bottom: 20px;
}

.warnings {
  padding: 16px;
  background: #fef3c7;
  border-radius: var(--radius-sm);
}

.warning-item {
  font-size: 13px;
  color: #92400e;
  margin-bottom: 4px;
}

.record-list {
  max-height: 300px;
  overflow-y: auto;
}

.record-item {
  display: flex;

  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--border-light);
}

.record-date {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.record-data {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: var(--text-secondary);
}

.empty {
  text-align: center;
  color: var(--text-muted);
  padding: 24px;
}
</style>
