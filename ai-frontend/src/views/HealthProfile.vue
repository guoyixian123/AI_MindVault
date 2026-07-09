<template>
  <div class="page">
        <nav class="page-nav">
      <a href="#" class="back-link" @click.prevent="$router.back()">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
        返回
      </a>
      <h1>健康档案</h1>
      <div class="nav-spacer"></div>
    </nav>


    <div class="page-content">
      <form @submit.prevent="saveProfile" class="profile-form">
        <!-- 基础信息 -->
        <div class="section-card">
          <h2>基础信息</h2>
          <div class="form-grid">
            <div class="form-group">
              <label>身高 (cm)</label>
              <input v-model.number="form.height" type="number" step="0.1" placeholder="170" />
            </div>
            <div class="form-group">
              <label>体重 (kg)</label>
              <input v-model.number="form.weight" type="number" step="0.1" placeholder="65" />
            </div>
            <div class="form-group">
              <label>BMI</label>
              <input :value="bmi" type="text" disabled />
            </div>
          </div>
        </div>

        <!-- 基线指标 -->
        <div class="section-card">
          <h2>基线指标</h2>
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
              <label>空腹血糖 (mmol/L)</label>
              <input v-model.number="form.bloodSugar" type="number" step="0.1" placeholder="5.6" />
            </div>
            <div class="form-group">
              <label>血脂情况</label>
              <input v-model="form.bloodLipid" type="text" placeholder="如：总胆固醇偏高" />
            </div>
          </div>
        </div>

        <!-- 病史信息 -->
        <div class="section-card">
          <h2>病史信息</h2>
          <div class="form-group">
            <label>过敏史</label>
            <textarea v-model="form.allergies" placeholder="请填写药物、食物过敏史..." rows="2"></textarea>
          </div>
          <div class="form-group">
            <label>家族病史</label>
            <textarea v-model="form.familyHistory" placeholder="如：父亲高血压、母亲糖尿病..." rows="2"></textarea>
          </div>
          <div class="form-group">
            <label>手术史</label>
            <textarea v-model="form.surgeryHistory" placeholder="如有请填写..." rows="2"></textarea>
          </div>
          <div class="form-group">
            <label>重大疾病史</label>
            <textarea v-model="form.majorDiseases" placeholder="如有请填写..." rows="2"></textarea>
          </div>
        </div>

        <button type="submit" class="submit-btn" :disabled="saving">
          {{ saving ? '保存中...' : '保存档案' }}
        </button>

        <div v-if="message" :class="isError ? 'error-msg' : 'success-msg'">{{ message }}</div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useHealthStore } from '../stores/health'

const healthStore = useHealthStore()
const saving = ref(false)
const message = ref('')

const isError = ref(false)

const form = ref({
  height: null,
  weight: null,
  bloodPressureSys: null,
  bloodPressureDia: null,
  bloodSugar: null,
  bloodLipid: '',
  allergies: '',
  familyHistory: '',
  surgeryHistory: '',
  majorDiseases: ''
})

const bmi = computed(() => {
  if (form.value.height && form.value.weight) {
    const h = form.value.height / 100
    return (form.value.weight / (h * h)).toFixed(1)
  }
  return '--'
})

onMounted(async () => {
  const profile = await healthStore.fetchProfile()
  if (profile) {
    Object.assign(form.value, profile)
  }
})

async function saveProfile() {
  saving.value = true
  message.value = ''
  isError.value = false
  try {
    const data = await healthStore.saveProfile(form.value)
    if (data.code === 200) {
      message.value = '健康档案保存成功！'
      setTimeout(() => message.value = '', 3000)
    } else {
      isError.value = true
      message.value = data.message || '保存失败，请重试'
    }
  } catch (e) {
    isError.value = true
    message.value = e?.response?.data?.message || '保存失败，请重试'
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
  max-width: 700px;
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
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
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

.form-group input,
.form-group textarea {
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

.form-group input:disabled {
  background: var(--color-paper);
  color: var(--text-muted);
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

.success-msg {
  text-align: center;
  color: var(--color-success);
  font-size: 14px;
  margin-top: 16px;
  padding: 12px;
  background: rgba(58, 114, 80, 0.08);
  border-radius: var(--radius-sm);
}

.error-msg {
  text-align: center;
  color: #dc2626;
  font-size: 14px;
  margin-top: 16px;
  padding: 12px;
  background: rgba(220, 38, 38, 0.08);
  border-radius: var(--radius-sm);
}
</style>
