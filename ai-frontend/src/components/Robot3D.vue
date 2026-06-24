<template>
  <div ref="container" class="robot-3d"
    @mousemove="onMouseMove"
    @click="onClick"
    @mouseenter="onHoverEnter"
    @mouseleave="onHoverLeave"
    @touchstart="onTouchStart"
    @touchmove="onTouchMove">
    <Transition name="bubble">
      <div v-if="showBubble" class="chat-bubble" :style="bubblePos">
        <div class="bubble-content">{{ bubbleText }}</div>
        <div class="bubble-tail"></div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as THREE from 'three'
import SplineLoader from '@splinetool/loader'

const container = ref(null)
let renderer, scene, camera, animationId
const clock = new THREE.Clock()
let robotGroup = null
let resizeObserver = null

// ── 部件引用 ──
let helmetGroup = null   // 头盔壳（旋转，颈关节感）
let faceBall    = null   // 头盔内紫色渐变球（position 偏移，视差感）
let eyeGroup    = null   // 眼睛显示屏（在 faceBall 或 helmet 内）
let leftEar     = null
let rightEar    = null

// 记录 faceBall 和 eyeGroup 的初始局部 position
let faceBallBasePos = { x: 0, y: 0, z: 0 }
let eyeBasePos      = { x: 0, y: 0, z: 0 }

// ── 基础变换 ──
let baseY = 0
let baseScale = 1

// ── 动画状态 ──
let entryProgress = 0
let clickReaction = 0
let clickIntensity = 0
let isHovered = false
let lastMouseMoveTime = Date.now()
let isIdle = false

// ── 闲置行为 ──
let idleAction  = null
let nextIdleTime = 0

// ── 鼠标 ──
const mouseX = ref(0)
const mouseY = ref(0)
let smoothMouseX = 0, smoothMouseY = 0
let prevSmoothX  = 0, prevSmoothY  = 0
let mouseVelX = 0,    mouseVelY    = 0

// ── 平滑旋转 / 位移状态 ──
let bodyRot     = { x: 0, y: 0, z: 0 }
let headRot     = { x: 0, y: 0, z: 0 }
let facePos     = { x: 0, y: 0 }        // faceBall 局部偏移（视差）
let faceRot     = { x: 0, y: 0, z: 0 } // faceBall 自转（慢速）
let eyePos      = { x: 0, y: 0 }
let leftEarRot  = { z: 0 }
let rightEarRot = { z: 0 }

// ── 气泡 ──
const showBubble = ref(false)
const bubbleText = ref('')
const bubblePos  = ref({})
let bubbleTimer  = null

const healthMessages = [
  '记得多喝水', '久坐要站起来活动', '保持良好作息很重要',
  '有什么不舒服可以问我', '定期体检是健康保障', '心情好身体才会好',
  '饮食均衡很重要', '睡眠充足精力充沛', '有健康问题随时问我',
  '点击我可以开始咨询', '深呼吸放松一下', '保持微笑心情更好',
]
const clickMessages = [
  '你好呀', '需要帮助吗', '我在这里等你',
  '点击咨询开始对话', '很高兴见到你', '有什么可以帮你的吗',
]

function showRandomBubble(msg) {
  bubbleText.value = msg || healthMessages[Math.floor(Math.random() * healthMessages.length)]
  const positions = [
    { top: '5%',    right: '8%',  left: 'auto' },
    { top: '12%',   left: '5%',   right: 'auto' },
    { bottom: '25%', right: '8%', top: 'auto', left: 'auto' },
    { bottom: '15%', left: '10%', top: 'auto', right: 'auto' },
  ]
  bubblePos.value = positions[Math.floor(Math.random() * positions.length)]
  showBubble.value = true
  setTimeout(() => { showBubble.value = false }, 4500)
}
function scheduleNextBubble() {
  bubbleTimer = setTimeout(() => { showRandomBubble(); scheduleNextBubble() }, 8000 + Math.random() * 7000)
}

/* ── 输入 ── */
function onMouseMove(e) {
  const r = container.value.getBoundingClientRect()
  mouseX.value =  ((e.clientX - r.left) / r.width)  * 2 - 1
  mouseY.value = -((e.clientY - r.top)  / r.height) * 2 + 1
  lastMouseMoveTime = Date.now(); isIdle = false
}
function onTouchMove(e) {
  if (!e.touches.length) return
  const r = container.value.getBoundingClientRect()
  mouseX.value =  ((e.touches[0].clientX - r.left) / r.width)  * 2 - 1
  mouseY.value = -((e.touches[0].clientY - r.top)  / r.height) * 2 + 1
  lastMouseMoveTime = Date.now(); isIdle = false
}
function onTouchStart() {
  clickReaction = 1.0; clickIntensity = 1.0
  showRandomBubble(clickMessages[Math.floor(Math.random() * clickMessages.length)])
}
function onHoverEnter() { isHovered = true  }
function onHoverLeave() { isHovered = false }
function onClick() {
  clickReaction = 1.0; clickIntensity = 1.0
  showRandomBubble(clickMessages[Math.floor(Math.random() * clickMessages.length)])
}

/* ── 场景 ── */
function initScene() {
  scene = new THREE.Scene()
  const w = container.value.clientWidth
  let h = container.value.clientHeight || 300
  camera = new THREE.PerspectiveCamera(35, w / h, 0.1, 100)
  camera.position.set(0, 0.3, 6)
  camera.lookAt(0, 0, 0)
  try {
    renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  } catch {
    container.value.innerHTML = '<div style="display:flex;align-items:center;justify-content:center;height:100%;color:#94a3b8;font-size:14px;">3D 渲染不可用</div>'
    return false
  }
  renderer.setSize(w, h)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.toneMapping = THREE.ACESFilmicToneMapping
  renderer.toneMappingExposure = 1.2
  renderer.outputColorSpace = THREE.SRGBColorSpace
  container.value.appendChild(renderer.domElement)
  scene.add(new THREE.AmbientLight(0xffffff, 0.6))
  const main = new THREE.DirectionalLight(0xffffff, 0.8); main.position.set(2, 5, 4); scene.add(main)
  const fill = new THREE.DirectionalLight(0xe0e8f0, 0.3); fill.position.set(-3, 3, 2); scene.add(fill)
}

/* ── 模型加载 ── */
function loadModel() {
  const loader = new SplineLoader()
  loader.load('/models/scene.splinecode', splineScene => {
    if (!container.value) return
    const model = splineScene

    // 隐藏辅助网格 & 计算包围盒
    const tightBox = new THREE.Box3()
    model.traverse(c => {
      if (!c.isMesh) return
      const nl = c.name.toLowerCase()
      if (nl.includes('floor') || nl.includes('ground') || nl.includes('plane') ||
          nl.includes('text')  || nl.includes('message') || nl.includes('shadow') ||
          nl.includes('collar')|| nl.includes('tracking') || nl.includes('label') ||
          nl.includes('cursor')) { c.visible = false; return }
      if (c.scale.x === 0 && c.scale.y === 0 && c.scale.z === 0) { c.visible = false; return }
      tightBox.union(new THREE.Box3().setFromObject(c))
    })
    let s = 1
    if (!tightBox.isEmpty()) {
      const sz = tightBox.getSize(new THREE.Vector3())
      const ct = tightBox.getCenter(new THREE.Vector3())
      s = 3.5 / sz.y
      model.scale.setScalar(s)
      model.position.set(-ct.x * s, -ct.y * s, -ct.z * s)
    } else {
      model.scale.setScalar(s)
    }
    baseScale = s
    baseY = model.position.y

    // ── 全量扫描，打印所有节点方便调试 ──
    console.group('🤖 Robot parts scan')
    model.traverse(c => {
      if (c.name) console.log(`  [${c.type}] "${c.name}"`, c.isMesh ? '(mesh)' : '')
    })
    console.groupEnd()

    // ── 收集部件 ──
    // 优先精确名称匹配，其次关键词匹配
    model.traverse(c => {
      const n  = c.name
      const nl = n.toLowerCase()

      // 头盔壳（最外层）：旋转，产生颈关节感
      if (nl === 'helmet' || (nl.includes('helmet') && !helmetGroup)) {
        helmetGroup = c
      }

      // 面部渐变球体：在头盔内部，负责视差偏移
      // Spline 模型里通常命名为 Sphere / Ball / Face / Orb / Globe
      // 用颜色/尺寸无法判断，先匹配常见名称，后面可根据 log 调整
      if (!faceBall) {
        if (n === 'Sphere' || n === 'Ball' || n === 'Face' || n === 'Orb' || n === 'Globe'
          || nl === 'face sphere' || nl === 'faceball' || nl === 'face ball'
          || (nl.includes('sphere') && !nl.includes('body') && !nl.includes('base'))
          || nl.includes('orb') || nl.includes('globe')) {
          faceBall = c
          faceBallBasePos = { x: c.position.x, y: c.position.y, z: c.position.z }
        }
      }

      // 眼睛
      if (n === 'Eyes' || nl === 'eyes') {
        eyeGroup   = c
        eyeBasePos = { x: c.position.x, y: c.position.y, z: c.position.z }
      }
      // 耳朵
      if (n === 'ear'  || nl === 'ear')  leftEar  = c
      if (n === 'ear2' || nl === 'ear2') rightEar = c
    })

    // 如果没匹配到 faceBall，用启发式方法：
    // 找头盔内最大的球形 Mesh（通常就是渐变面部球）
    if (!faceBall && helmetGroup) {
      let maxVol = 0
      helmetGroup.traverse(c => {
        if (!c.isMesh) return
        const nl = c.name.toLowerCase()
        // 排除耳朵、眼睛
        if (nl.includes('ear') || nl.includes('eye') || nl === 'eyes') return
        const box = new THREE.Box3().setFromObject(c)
        const sz  = box.getSize(new THREE.Vector3())
        const vol = sz.x * sz.y * sz.z
        if (vol > maxVol) { maxVol = vol; faceBall = c }
      })
      if (faceBall) faceBallBasePos = { x: faceBall.position.x, y: faceBall.position.y, z: faceBall.position.z }
    }

    console.log('✅ Identified:', {
      helmet:   helmetGroup?.name,
      faceBall: faceBall?.name,
      eyes:     eyeGroup?.name,
      leftEar:  leftEar?.name,
      rightEar: rightEar?.name,
    })

    robotGroup = model
    model.scale.setScalar(0)
    scene.add(model)
    setTimeout(() => showRandomBubble(), 2500)
    scheduleNextBubble()
  }, undefined, err => {
    console.error(err)
    if (container.value)
      container.value.innerHTML = '<div style="display:flex;align-items:center;justify-content:center;height:100%;color:#94a3b8;font-size:14px;">模型加载失败，请刷新重试</div>'
  })
}

/* ── 缓动 ── */
function easeOutElastic(t) {
  const c4 = (2 * Math.PI) / 3
  return t === 0 ? 0 : t === 1 ? 1 : Math.pow(2, -10 * t) * Math.sin((t * 10 - 0.75) * c4) + 1
}

/* ── 闲置行为 ── */
function triggerIdleAction(time) {
  const actions = [
    { type: 'headTurnLeft',  dur: [0.8, 1.2] },
    { type: 'headTurnRight', dur: [0.8, 1.2] },
    { type: 'headNod',       dur: [0.7, 1.0] },
    { type: 'headTiltLeft',  dur: [0.9, 1.3] },
    { type: 'headTiltRight', dur: [0.9, 1.3] },
    { type: 'headShake',     dur: [0.8, 1.1] },
    { type: 'headLookUp',    dur: [0.8, 1.2] },
    { type: 'faceDrift',     dur: [1.5, 2.5] }, // 面部球单独漂移
    { type: 'earWiggle',     dur: [0.8, 1.5] },
    { type: 'blink',         dur: [0.5, 0.8] },
    { type: 'bodySway',      dur: [1.2, 2.0] },
    { type: 'bodyBounce',    dur: [0.8, 1.2] },
  ]
  const pick = actions[Math.floor(Math.random() * actions.length)]
  const [dMin, dMax] = pick.dur
  idleAction = { ...pick, startTime: time, duration: dMin + Math.random() * (dMax - dMin) }
}

/* ── 动画主循环 ── */
function animate() {
  animationId = requestAnimationFrame(animate)
  const delta = Math.min(clock.getDelta(), 0.05)
  const time  = clock.elapsedTime

  // 鼠标平滑 + 速度
  prevSmoothX = smoothMouseX; prevSmoothY = smoothMouseY
  smoothMouseX += (mouseX.value - smoothMouseX) * 0.10
  smoothMouseY += (mouseY.value - smoothMouseY) * 0.10
  mouseVelX = smoothMouseX - prevSmoothX
  mouseVelY = smoothMouseY - prevSmoothY

  // 闲置检测
  const idleSec = (Date.now() - lastMouseMoveTime) / 1000
  isIdle = idleSec > 1.5
  if (isIdle) {
    if (!idleAction && time > nextIdleTime) {
      triggerIdleAction(time)
      nextIdleTime = time + 2.5 + Math.random() * 3
    }
  } else {
    idleAction = null
  }

  // 闲置偏移量（分层）
  let idleBodyY = 0, idleBodyX = 0, idleBodyZ = 0
  let idleHeadY = 0, idleHeadX = 0, idleHeadZ = 0
  let idleFaceX = 0, idleFaceY = 0              // 面部球额外漂移
  let earWiggle = 0, blinkScale = 1, idleBounceY = 0

  if (idleAction) {
    const p    = (time - idleAction.startTime) / idleAction.duration
    if (p >= 1) {
      idleAction = null
    } else {
      const wave = Math.sin(p * Math.PI)
      const osc2 = Math.sin(p * Math.PI * 2)
      const osc3 = Math.sin(p * Math.PI * 3)
      switch (idleAction.type) {
        case 'headTurnLeft':  idleHeadY = -wave * 0.45; break
        case 'headTurnRight': idleHeadY =  wave * 0.45; break
        case 'headNod':       idleHeadX =  osc2 * 0.22; break
        case 'headTiltLeft':  idleHeadZ =  wave * 0.25; break
        case 'headTiltRight': idleHeadZ = -wave * 0.25; break
        case 'headShake':     idleHeadY =  osc3 * 0.28; break
        case 'headLookUp':    idleHeadX = -wave * 0.28; break
        // ── 面部球单独漂移（头盔不动，球体在内部游走）──
        case 'faceDrift':
          idleFaceX = Math.sin(p * Math.PI * 1.5) * 0.12
          idleFaceY = Math.cos(p * Math.PI * 1.5) * 0.08
          break
        case 'earWiggle': earWiggle = wave * 2.2; break
        case 'blink':     blinkScale = Math.abs(Math.cos(p * Math.PI * 5)) < 0.15 ? 0.05 : 1; break
        case 'bodySway':
          idleBodyZ = osc2 * 0.10; idleBodyY = Math.sin(p * Math.PI * 2 + 0.5) * 0.12
          idleHeadZ = idleBodyZ * 0.4; idleHeadY = idleBodyY * 0.4
          break
        case 'bodyBounce':
          idleBounceY = Math.abs(Math.sin(p * Math.PI * 3)) * 0.06; break
      }
    }
  }

  // 点击衰减
  if (clickReaction  > 0) clickReaction  = Math.max(0, clickReaction  - delta * 4)
  if (clickIntensity > 0) clickIntensity = Math.max(0, clickIntensity - delta * 3)

  if (!robotGroup) { renderer.render(scene, camera); return }

  // 入场
  if (entryProgress < 1) entryProgress = Math.min(1, entryProgress + delta * 0.8)
  const entryScale = easeOutElastic(entryProgress)

  // 点击弹缩
  const clickT = 1 - clickReaction
  const clickScale = clickReaction > 0 ? 1 + (1 - clickT * clickT) * 0.07 : 1

  // 呼吸 & 浮动
  const breathe = 1 + Math.sin(time * 1.1) * 0.007
  const floatY  = Math.sin(time * 0.65) * 0.028 + idleBounceY

  robotGroup.scale.setScalar(baseScale * entryScale * breathe * clickScale)
  robotGroup.position.y = baseY + floatY

  // ══════════════════════════════════════════════════
  //  Layer 1: 整体（身体）旋转
  //  幅度克制 → 像企鹅重心摆动
  // ══════════════════════════════════════════════════
  const natBodyY = Math.sin(time * 0.35) * 0.05
  const natBodyX = Math.sin(time * 0.28 + 1.2) * 0.03
  const natBodyZ = Math.sin(time * 0.24 + 0.7) * 0.03
  const bodyVelZ = -mouseVelX * 2.0
  const clickBodyY = Math.sin(time * 12) * clickIntensity * 0.05

  bodyRot.y += (smoothMouseX * 0.18 + natBodyY + idleBodyY + clickBodyY - bodyRot.y) * 0.07
  bodyRot.x += (smoothMouseY * 0.10 + natBodyX + idleBodyX - bodyRot.x) * 0.06
  bodyRot.z += (bodyVelZ + natBodyZ + idleBodyZ - bodyRot.z) * 0.05

  robotGroup.rotation.y = bodyRot.y
  robotGroup.rotation.x = bodyRot.x
  robotGroup.rotation.z = bodyRot.z

  // ══════════════════════════════════════════════════
  //  Layer 2: 头盔旋转（叠加）
  //  幅度约为身体 2x，速度快 → 颈关节感
  //  ⚠️ 只改 rotation，不改 position
  // ══════════════════════════════════════════════════
  if (helmetGroup) {
    const natHeadY = Math.sin(time * 0.48 + 0.3) * 0.07
    const natHeadX = Math.sin(time * 0.42 + 1.5) * 0.05
    const natHeadZ = Math.sin(time * 0.38 + 0.9) * 0.04
    const headVelY = mouseVelX * 4.0
    const headVelZ = -mouseVelX * 2.5
    const clickHeadX = Math.sin(time * 10) * clickIntensity * 0.12

    headRot.y += (smoothMouseX * 0.22 + headVelY + natHeadY + idleHeadY - headRot.y) * 0.14
    headRot.x += (smoothMouseY * 0.14 + natHeadX + idleHeadX + clickHeadX - headRot.x) * 0.13
    headRot.z += (headVelZ + natHeadZ + idleHeadZ - headRot.z) * 0.11

    helmetGroup.rotation.y = headRot.y
    helmetGroup.rotation.x = headRot.x
    helmetGroup.rotation.z = headRot.z
  }

  // ══════════════════════════════════════════════════
  //  Layer 3: 面部渐变球 —— position 视差偏移
  //
  //  原理：头盔转动时，球体滞后移动
  //       → 看起来像球在头盔玻璃罩内侧"滚动"
  //       ① 鼠标方向的反向偏移（视差）
  //       ② 慢速自转（表面渐变流动感）
  //       ③ 呼吸起伏（轻微 Y 轴上下）
  //       ④ 闲置漂移动作
  // ══════════════════════════════════════════════════
  if (faceBall) {
    const sf = 1 / baseScale   // 抵消 scale 缩放，让偏移量在世界空间稳定

    // ① 视差：鼠标往右看，球向右偏（同向，营造球在罩内贴着转）
    //    幅度要小，太大会穿出头盔
    const parallaxX = smoothMouseX * 0.08 * sf
    const parallaxY = smoothMouseY * 0.06 * sf

    // ② 速度惯性：鼠标甩动时球稍微"甩"出去再弹回
    const velDriftX = mouseVelX * 1.5 * sf
    const velDriftY = mouseVelY * 1.2 * sf

    // ③ 呼吸起伏（比整体更细腻，频率不同）
    const breatheOffY = Math.sin(time * 1.4 + 0.5) * 0.02 * sf

    // ④ 慢速自转（让渐变颜色流动，频率极低）
    //    注意：faceBall 自转只在局部 Y 轴，不影响位置
    faceRot.y += (Math.sin(time * 0.12) * 0.3 - faceRot.y) * 0.02
    faceRot.x += (Math.sin(time * 0.09) * 0.2 - faceRot.x) * 0.02

    // ⑤ 闲置漂移
    facePos.x += (parallaxX + velDriftX + idleFaceX * sf - facePos.x) * 0.10
    facePos.y += (parallaxY + velDriftY + breatheOffY + idleFaceY * sf - facePos.y) * 0.10

    faceBall.position.x = faceBallBasePos.x + facePos.x
    faceBall.position.y = faceBallBasePos.y + facePos.y
    faceBall.rotation.y = faceRot.y
    faceBall.rotation.x = faceRot.x
  }

  // ══════════════════════════════════════════════════
  //  Layer 4: 眼睛（在面部球内，最灵敏）
  //  比 faceBall 还快，像眼球精确锁定目标
  // ══════════════════════════════════════════════════
  if (eyeGroup) {
    const sf = 1 / baseScale
    const eyeTgtX = (smoothMouseX * 0.55 + mouseVelX * 1.5) * sf
    const eyeTgtY = (smoothMouseY * 0.40 + mouseVelY * 1.5) * sf
    eyePos.x += (eyeTgtX - eyePos.x) * 0.16
    eyePos.y += (eyeTgtY - eyePos.y) * 0.16
    eyeGroup.position.x = eyeBasePos.x + eyePos.x * 3.2
    eyeGroup.position.y = eyeBasePos.y + eyePos.y * 2.5
    eyeGroup.children.forEach(c => { if (c.isMesh) c.scale.y = blinkScale })
  }

  // ══════════════════════════════════════════════════
  //  Layer 5: 耳朵 —— 随头盔顺风摆
  // ══════════════════════════════════════════════════
  const baseSway = Math.sin(time * 1.3) * 0.06
  const earF     = headRot.y * 0.5
  leftEarRot.z  += ( baseSway + earF + earWiggle * Math.sin(time * 15)           * 0.35 - leftEarRot.z)  * 0.13
  rightEarRot.z += (-baseSway - earF + earWiggle * Math.sin(time * 15 + Math.PI) * 0.35 - rightEarRot.z) * 0.13
  if (leftEar)  leftEar.rotation.z  = leftEarRot.z
  if (rightEar) rightEar.rotation.z = rightEarRot.z

  renderer.render(scene, camera)
}

/* ── 响应式 & 生命周期 ── */
function onResize() {
  if (!container.value || !renderer) return
  const w = container.value.clientWidth, h = container.value.clientHeight
  if (!w || !h) return
  camera.aspect = w / h; camera.updateProjectionMatrix(); renderer.setSize(w, h)
}
onMounted(() => {
  if (initScene() === false) return
  loadModel(); animate()
  window.addEventListener('resize', onResize)
  resizeObserver = new ResizeObserver(onResize)
  resizeObserver.observe(container.value)
})
onBeforeUnmount(() => {
  cancelAnimationFrame(animationId); clearTimeout(bubbleTimer)
  window.removeEventListener('resize', onResize)
  if (resizeObserver) { resizeObserver.disconnect(); resizeObserver = null }
  if (renderer) { renderer.dispose(); renderer.forceContextLoss() }
})
</script>

<style scoped>
.robot-3d {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  overflow: hidden; background: transparent; cursor: pointer;
}
.robot-3d canvas { display: block; width: 100% !important; height: 100% !important; }

.chat-bubble {
  position: absolute; z-index: 10;
  animation: float 3s ease-in-out infinite; pointer-events: none;
}
.bubble-content {
  background: rgba(255,255,255,0.92); backdrop-filter: blur(12px);
  border: 1px solid rgba(13,148,136,0.15);
  border-radius: 16px 16px 16px 4px;
  padding: 12px 18px; font-size: 14px; line-height: 1.5;
  color: #1e293b; max-width: 220px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08); white-space: nowrap;
}
.bubble-tail {
  position: absolute; bottom: -6px; left: 8px;
  width: 12px; height: 12px;
  background: rgba(255,255,255,0.92);
  border-bottom: 1px solid rgba(13,148,136,0.15);
  border-left:   1px solid rgba(13,148,136,0.15);
  transform: rotate(-45deg);
}
.bubble-enter-active { transition: all 0.5s cubic-bezier(0.16,1,0.3,1); }
.bubble-leave-active  { transition: all 0.35s ease; }
.bubble-enter-from    { opacity: 0; transform: translateY(12px) scale(0.9); }
.bubble-leave-to      { opacity: 0; transform: translateY(-8px)  scale(0.95); }

@keyframes float {
  0%,100% { transform: translateY(0);    }
  50%      { transform: translateY(-5px); }
}
</style>