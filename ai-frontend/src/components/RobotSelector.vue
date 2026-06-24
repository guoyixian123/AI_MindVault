<template>
  <div class="robot-selector">
    <div class="selector-header">
      <h2>🤖 选择机器人样式</h2>
      <p>点击选择你喜欢的机器人</p>
    </div>

    <div class="robot-grid">
      <div
        v-for="(robot, index) in robots"
        :key="index"
        class="robot-card"
        :class="{ active: selectedIndex === index }"
        @click="selectRobot(index)"
      >
        <div class="robot-preview">
          <RobotSVG :styleIndex="index" />
        </div>
        <div class="robot-info">
          <span class="robot-name">{{ robot.name }}</span>
          <span class="robot-desc">{{ robot.desc }}</span>
        </div>
        <div v-if="selectedIndex === index" class="check-mark">✓</div>
      </div>
    </div>

    <div class="preview-area">
      <div class="preview-label">当前预览</div>
      <div class="preview-robot">
        <RobotSVG :styleIndex="selectedIndex" />
      </div>
      <div class="preview-name">{{ robots[selectedIndex].name }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import RobotSVG from './RobotSVG.vue'

const emit = defineEmits(['select'])

const robots = [
  { name: '经典医疗', desc: '护目镜风格，科技感强' },
  { name: '圆润伙伴', desc: '蛋形可爱，亲和力高' },
  { name: '简约科技', desc: '极简设计，现代感强' },
]

const selectedIndex = ref(0)

function selectRobot(index) {
  selectedIndex.value = index
  emit('select', index)
}
</script>

<style scoped>
.robot-selector {
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
}

.selector-header {
  text-align: center;
  margin-bottom: 32px;
}

.selector-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 8px;
}

.selector-header p {
  font-size: 14px;
  color: #64748b;
}

.robot-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 40px;
}

.robot-card {
  position: relative;
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 16px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.robot-card:hover {
  border-color: #14b8a6;
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(13, 148, 136, 0.15);
}

.robot-card.active {
  border-color: #14b8a6;
  background: #f0fdfa;
  box-shadow: 0 0 0 3px rgba(13, 148, 136, 0.2);
}

.robot-preview {
  width: 120px;
  height: 150px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
}

.robot-preview :deep(svg) {
  width: 100%;
  height: 100%;
}

.robot-info {
  text-align: center;
}

.robot-name {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
}

.robot-desc {
  display: block;
  font-size: 12px;
  color: #94a3b8;
}

.check-mark {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 24px;
  height: 24px;
  background: #14b8a6;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
}

.preview-area {
  background: linear-gradient(145deg, #f0fdfa, #e0f7fa, #f0f9ff);
  border-radius: 20px;
  padding: 32px;
  text-align: center;
}

.preview-label {
  font-size: 12px;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  margin-bottom: 20px;
}

.preview-robot {
  width: 200px;
  height: 250px;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-robot :deep(svg) {
  width: 100%;
  height: 100%;
}

.preview-name {
  font-size: 20px;
  font-weight: 700;
  color: #0d9488;
}

@media (max-width: 768px) {
  .robot-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .robot-card {
    flex-direction: row;
    padding: 16px;
  }

  .robot-preview {
    width: 80px;
    height: 100px;
    margin-bottom: 0;
    margin-right: 16px;
  }

  .robot-info {
    text-align: left;
  }
}
</style>
