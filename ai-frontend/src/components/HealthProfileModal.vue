<template>
  <div v-if="visible" class="modal-overlay" @click.self="close">
    <div class="modal">
      <div class="modal-header">
        <h3>患者健康档案</h3>
        <button class="close-btn" @click="close">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18M6 6l12 12"/></svg>
        </button>
      </div>

      <div class="modal-body" v-if="loading">
        <div class="loading">加载中...</div>
      </div>

      <div class="modal-body" v-else-if="!profile">
        <div class="empty">该用户暂未填写健康档案</div>
      </div>

      <div class="modal-body" v-else>
        <!-- 基本信息 -->
        <div class="section">
          <h4>基本信息</h4>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">身高</span>
              <span class="value">{{ profile.height || '--' }} cm</span>
            </div>
            <div class="info-item">
              <span class="label">体重</span>
              <span class="value">{{ profile.weight || '--' }} kg</span>
            </div>
            <div class="info-item">
              <span class="label">BMI</span>
              <span class="value" :class="bmiClass">{{ profile.bmi || '--' }}</span>
            </div>
          </div>
        </div>

        <!-- 基线指标 -->
        <div class="section">
          <h4>基线指标</h4>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">血压</span>
              <span class="value">{{ profile.bloodPressureSys || '--' }}/{{ profile.bloodPressureDia || '--' }} mmHg</span>
            </div>
            <div class="info-item">
              <span class="label">空腹血糖</span>
              <span class="value">{{ profile.bloodSugar || '--' }} mmol/L</span>
            </div>
            <div class="info-item">
              <span class="label">血脂</span>
              <span class="value">{{ profile.bloodLipid || '--' }}</span>
            </div>
          </div>
        </div>

        <!-- 病史信息 -->
        <div class="section">
          <h4>病史信息</h4>
          <div class="history-list">
            <div class="history-item" v-if="profile.allergies">
              <span class="history-label">过敏史</span>
              <span class="history-value">{{ profile.allergies }}</span>
            </div>
            <div class="history-item" v-if="profile.familyHistory">
              <span class="history-label">家族病史</span>
              <span class="history-value">{{ profile.familyHistory }}</span>
            </div>
            <div class="history-item" v-if="profile.surgeryHistory">
              <span class="history-label">手术史</span>
              <span class="history-value">{{ profile.surgeryHistory }}</span>
            </div>
            <div class="history-item" v-if="profile.majorDiseases">
              <span class="history-label">重大疾病史</span>
              <span class="history-value">{{ profile.majorDiseases }}</span>
            </div>
          </div>
          <div v-if="!hasHistory" class="no-history">暂无病史记录</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import api from '../utils/api'

const props = defineProps({
  visible: Boolean,
  userId: Number
})

const emit = defineEmits(['close'])

const loading = ref(false)
const profile = ref(null)

const bmiClass = computed(() => {
  if (!profile.value?.bmi) return ''
  const v = parseFloat(profile.value.bmi)
  if (v < 18.5) return 'underweight'
  if (v < 24) return 'normal'
  if (v < 28) return 'overweight'
  return 'obese'
})

const hasHistory = computed(() => {
  if (!profile.value) return false
  return profile.value.allergies || profile.value.familyHistory ||
         profile.value.surgeryHistory || profile.value.majorDiseases
})

watch(() => props.visible, async (val) => {
  if (val && props.userId) {
    await loadProfile()
  } else {
    profile.value = null
  }
})

async function loadProfile() {
  loading.value = true
  try {
    const { data } = await api.get(`/api/health/profile/user/${props.userId}`)
    profile.value = data.code === 200 ? data.data : null
  } catch (e) {
    console.error('Failed to load profile:', e)
    profile.value = null
  } finally {
    loading.value = false
  }
}

function close() {
  emit('close')
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 520px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #f3f4f6;
}

.modal-header h3 {
  font-size: 18px;
  font-weight: 600;
}

.close-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  border-radius: 8px;
  color: #6b7280;
  cursor: pointer;
}

.close-btn:hover { background: #f3f4f6; }

.modal-body {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
}

.loading, .empty {
  text-align: center;
  padding: 40px 0;
  color: #9ca3af;
}

.section {
  margin-bottom: 24px;
}

.section:last-child { margin-bottom: 0; }

.section h4 {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f3f4f6;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item .label {
  font-size: 12px;
  color: #9ca3af;
}

.info-item .value {
  font-size: 15px;
  font-weight: 500;
  color: #111827;
}

.underweight { color: #3b82f6; }
.normal { color: #059669; }
.overweight { color: #f59e0b; }
.obese { color: #ef4444; }

.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-item {
  display: flex;
  gap: 12px;
}

.history-label {
  min-width: 80px;
  font-size: 13px;
  color: #6b7280;
  flex-shrink: 0;
}

.history-value {
  font-size: 14px;
  color: #111827;
}

.no-history {
  text-align: center;
  color: #9ca3af;
  font-size: 14px;
  padding: 12px 0;
}
</style>
