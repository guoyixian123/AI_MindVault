<template>
  <div class="poster">
    <RobotSVG class="robot-main" />
  </div>
</template>

<script setup>
import RobotSVG from './RobotSVG.vue'
</script>

<style scoped>
.poster {
  display: flex;
  align-items: center;
  justify-content: center;
}

.robot-main {
  width: 100%;
  max-width: 320px;
  animation: float 5s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

@media (max-width: 767px) {
  .robot-main { max-width: 200px; }
}
</style>
