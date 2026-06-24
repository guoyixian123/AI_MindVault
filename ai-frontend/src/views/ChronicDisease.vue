<template>
  <div class="page">
        <nav class="page-nav">
      <a href="#" class="back-link" @click.prevent="$router.back()">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
        返回
      </a>
      <h1>慢病管理</h1>
      <div class="nav-spacer"></div>
    </nav>


    <div class="page-content">
      <!-- 慢病列表 -->
      <div v-for="disease in diseases" :key="disease.id" class="disease-card">
        <div class="disease-header">
          <h3>{{ disease.diseaseType }}</h3>
          <span class="diagnosis-date">确诊：{{ disease.diagnosisDate || '未填写' }}</span>
        </div>
        <div v-if="disease.currentMedications" class="medications">
          <strong>当前用药：</strong>{{ disease.currentMedications }}
        </div>

        <!-- 指标记录 -->
        <div class="records-section">
          <div class="records-header">
            <h4>指标记录</h4>
            <button class="add-record-btn" @click="openRecordForm(disease.id)">+ 记录</button>
          </div>
          <div class="record-list">
            <div v-for="record in getRecords(disease.id)" :key="record.id" class="record-item">
              <span class="record-date">{{ record.recordDate }}</span>
              <span class="record-name">{{ record.indicatorName }}</span>
              <span class="record-value">{{ record.indicatorValue }}</span>
            </div>
            <div v-if="getRecords(disease.id).length === 0" class="no-records">暂无记录</div>
          </div>
        </div>
      </div>

      <div v-if="diseases.length === 0" class="empty-state">
        <p>暂无慢病档案</p>
        <button class="action-btn" @click="showForm = true">创建慢病档案</button>
      </div>

      <!-- 新增慢病弹窗 -->
      <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
        <div class="modal">
          <div class="modal-header">
            <h2>新增慢病档案</h2>
            <button class="close-btn" @click="showForm = false">×</button>
          </div>
          <form @submit.prevent="createDisease">
            <div class="form-group">
              <label>疾病类型 *</label>
              <select v-model="diseaseForm.diseaseType" required>
                <option value="">请选择</option>
                <option value="糖尿病">糖尿病</option>
                <option value="高血压">高血压</option>
                <option value="痛风">痛风</option>
                <option value="甲减">甲减</option>
                <option value="其他">其他</option>
              </select>
            </div>
            <div class="form-group">
              <label>确诊日期</label>
              <input v-model="diseaseForm.diagnosisDate" type="date" />
            </div>
            <div class="form-group">
              <label>当前用药</label>
              <textarea v-model="diseaseForm.currentMedications" placeholder="请填写当前用药方案..." rows="2"></textarea>
            </div>
            <div class="form-group">
              <label>医嘱备注</label>
              <textarea v-model="diseaseForm.doctorNotes" placeholder="医生建议..." rows="2"></textarea>
            </div>
            <button type="submit" class="submit-btn">创建</button>
          </form>
        </div>
      </div>

      <!-- 记录指标弹窗 -->
      <div v-if="showRecordForm" class="modal-overlay" @click.self="showRecordForm = false">
        <div class="modal">
          <div class="modal-header">
            <h2>记录指标</h2>
            <button class="close-btn" @click="showRecordForm = false">×</button>
          </div>
          <form @submit.prevent="addRecord">
            <div class="form-group">
              <label>指标名称 *</label>
              <input v-model="recordForm.indicatorName" type="text" placeholder="如：空腹血糖" required />
            </div>
            <div class="form-group">
              <label>指标值 *</label>
              <input v-model="recordForm.indicatorValue" type="text" placeholder="如：6.5" required />
            </div>
            <div class="form-group">
              <label>记录日期</label>
              <input v-model="recordForm.recordDate" type="date" />
            </div>
            <div class="form-group">
              <label>备注</label>
              <input v-model="recordForm.notes" type="text" placeholder="选填" />
            </div>
            <button type="submit" class="submit-btn">保存</button>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../utils/api'

const diseases = ref([])
const recordsMap = ref({})
const showForm = ref(false)
const showRecordForm = ref(false)
const currentDiseaseId = ref(null)

const diseaseForm = ref({
  diseaseType: '',
  diagnosisDate: '',
  currentMedications: '',
  doctorNotes: ''
})

const recordForm = ref({
  indicatorName: '',
  indicatorValue: '',
  recordDate: new Date().toISOString().split('T')[0],
  notes: ''
})

onMounted(async () => {
  await loadDiseases()
})

async function loadDiseases() {
  try {
    const { data } = await api.get('/api/health/chronic')
    if (data.code === 200) {
      diseases.value = data.data || []
      // 加载每个慢病的指标记录
      for (const disease of diseases.value) {
        await loadRecords(disease.id)
      }
    }
  } catch (e) {
    console.error('Failed to load diseases:', e)
  }
}

async function loadRecords(diseaseId) {
  try {
    const { data } = await api.get(`/api/health/chronic/${diseaseId}/records`)
    if (data.code === 200) {
      recordsMap.value[diseaseId] = data.data || []
    }
  } catch (e) {
    console.error('Failed to load records:', e)
  }
}

function getRecords(diseaseId) {
  return recordsMap.value[diseaseId] || []
}

async function createDisease() {
  try {
    const { data } = await api.post('/api/health/chronic', diseaseForm.value)
    if (data.code === 200) {
      showForm.value = false
      diseaseForm.value = { diseaseType: '', diagnosisDate: '', currentMedications: '', doctorNotes: '' }
      await loadDiseases()
    }
  } catch (e) {
    alert('创建失败')
  }
}

function openRecordForm(diseaseId) {
  currentDiseaseId.value = diseaseId
  recordForm.value = {
    indicatorName: '',
    indicatorValue: '',
    recordDate: new Date().toISOString().split('T')[0],
    notes: ''
  }
  showRecordForm.value = true
}

async function addRecord() {
  try {
    const { data } = await api.post(`/api/health/chronic/${currentDiseaseId.value}/records`, recordForm.value)
    if (data.code === 200) {
      showRecordForm.value = false
      await loadRecords(currentDiseaseId.value)
    }
  } catch (e) {
    alert('保存失败')
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
  flex: 1;
}

.new-btn {
  padding: 8px 20px;
  background: var(--color-forest);
  color: var(--color-white);
  border: none;
  border-radius: var(--radius-sm);
  font-size: 14px;
  font-weight: 500;
  transition: all var(--transition-fast);
}

.new-btn:hover { background: var(--color-forest-dark); }

.page-content {
  max-width: 700px;
  margin: 32px auto;
  padding: 0 24px;
}

.disease-card {
  background: var(--color-white);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 24px;
  margin-bottom: 16px;
}

.disease-header {
  display: flex;

  align-items: center;
  margin-bottom: 14px;
}

.disease-header h3 {
  font-size: 1.2rem;
}

.diagnosis-date {
  font-size: 13px;
  color: var(--text-muted);
}

.medications {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 16px;
}

.records-section {
  border-top: 1px solid var(--border-light);
  padding-top: 16px;
}

.records-header {
  display: flex;

  align-items: center;
  margin-bottom: 14px;
}

.records-header h4 {
  font-size: 15px;
  font-weight: 600;
}

.add-record-btn {
  padding: 6px 14px;
  background: rgba(45, 90, 61, 0.08);
  color: var(--color-forest);
  border: none;
  border-radius: var(--radius-sm);
  font-size: 13px;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.add-record-btn:hover {
  background: rgba(45, 90, 61, 0.15);
}

.record-list {
  max-height: 200px;
  overflow-y: auto;
}

.record-item {
  display: flex;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid var(--border-light);
  font-size: 14px;
}

.record-date {
  color: var(--text-muted);
  min-width: 100px;
}

.record-name {
  flex: 1;
  color: var(--text-primary);
}

.record-value {
  font-weight: 600;
  color: var(--color-forest);
}

.no-records {
  text-align: center;
  color: var(--text-muted);
  padding: 16px;
  font-size: 13px;
}

.empty-state {
  text-align: center;
  padding: 48px;
  background: var(--color-white);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
}

.empty-state p {
  color: var(--text-muted);
  margin-bottom: 20px;
}

.action-btn {
  padding: 10px 24px;
  background: var(--color-forest);
  color: var(--color-white);
  border: none;
  border-radius: var(--radius-sm);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.action-btn:hover { background: var(--color-forest-dark); }

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  padding: 20px;
}

.modal {
  background: var(--color-white);
  border-radius: var(--radius-lg);
  padding: 32px;
  width: 100%;
  max-width: 500px;
}

.modal-header {
  display: flex;

  align-items: center;
  margin-bottom: 24px;
}

.modal-header h2 {
  font-size: 20px;
  font-weight: 600;
}

.close-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-paper);
  border: none;
  border-radius: var(--radius-sm);
  font-size: 20px;
  color: var(--text-muted);
  cursor: pointer;
}

.form-group {
  margin-bottom: 16px;
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

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-sm);
  font-size: 15px;
  color: var(--text-primary);
  outline: none;
  transition: border-color var(--transition-fast);
  background: var(--color-white);
}

.form-group input:focus,
.form-group textarea:focus {
  border-color: var(--color-forest);
}

.submit-btn {
  width: 100%;
  padding: 13px;
  background: var(--color-forest);
  color: var(--color-white);
  border: none;
  border-radius: var(--radius-sm);
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.submit-btn:hover {
  background: var(--color-forest-dark);
}
</style>
