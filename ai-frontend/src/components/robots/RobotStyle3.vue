<template>
  <svg viewBox="0 0 280 320" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="s3-bodyGrad" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" stop-color="#14b8a6" />
        <stop offset="100%" stop-color="#0d9488" />
      </linearGradient>
      <filter id="s3-glow">
        <feGaussianBlur stdDeviation="4" result="blur"/>
        <feMerge><feMergeNode in="blur"/><feMergeNode in="SourceGraphic"/></feMerge>
      </filter>
    </defs>

    <!-- 头部 - 圆角方形 -->
    <rect x="85" y="40" width="110" height="95" rx="24" fill="url(#s3-bodyGrad)"/>
    <!-- 面罩 -->
    <rect x="98" y="55" width="84" height="55" rx="14" fill="#0f172a"/>
    <!-- 面罩高光 -->
    <rect x="103" y="59" width="74" height="12" rx="6" fill="white" opacity="0.05"/>

    <!-- 眼睛 - 简约线条 -->
    <line x1="112" y1="82" x2="132" y2="82" stroke="#22d3ee" stroke-width="4" stroke-linecap="round" filter="url(#s3-glow)"/>
    <line x1="148" y1="82" x2="168" y2="82" stroke="#22d3ee" stroke-width="4" stroke-linecap="round" filter="url(#s3-glow)"/>

    <!-- 嘴巴 - 简约弧线 -->
    <path d="M125,100 Q140,110 155,100" fill="none" stroke="#22d3ee" stroke-width="2" stroke-linecap="round" opacity="0.6"/>

    <!-- 天线 -->
    <line x1="140" y1="40" x2="140" y2="22" stroke="#14b8a6" stroke-width="2" stroke-linecap="round"/>
    <circle cx="140" cy="20" r="3" fill="#06b6d4"/>

    <!-- 耳朵指示灯 -->
    <circle cx="82" cy="85" r="5" fill="#0f172a"/>
    <circle cx="82" cy="85" r="3" fill="#14b8a6" opacity="0.8"/>
    <circle cx="198" cy="85" r="5" fill="#0f172a"/>
    <circle cx="198" cy="85" r="3" fill="#14b8a6" opacity="0.8"/>

    <!-- 颈部 -->
    <rect x="130" y="135" width="20" height="8" rx="4" fill="#e2e8f0"/>

    <!-- 身体 - 梯形 -->
    <path d="M90,143 L190,143 L185,250 L95,250 Z" fill="#f8fafc" stroke="#e2e8f0" stroke-width="1.5" stroke-linejoin="round"/>

    <!-- 胸口装饰 -->
    <circle cx="140" cy="185" r="18" fill="none" stroke="#14b8a6" stroke-width="1.5" opacity="0.3"/>
    <circle cx="140" cy="185" r="12" fill="none" stroke="#14b8a6" stroke-width="1" opacity="0.5"/>
    <circle cx="140" cy="185" r="4" fill="#14b8a6" opacity="0.6"/>

    <!-- 心跳线 -->
    <polyline points="95,220 115,220 122,210 130,230 138,215 146,225 154,220 185,220" fill="none" stroke="#06b6d4" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" opacity="0.5"/>

    <!-- 手臂 - 简约 -->
    <rect x="60" y="150" width="22" height="55" rx="11" fill="#f8fafc" stroke="#e2e8f0" stroke-width="1"/>
    <circle cx="71" cy="208" r="7" fill="#f8fafc" stroke="#e2e8f0" stroke-width="1"/>
    <rect x="198" y="150" width="22" height="55" rx="11" fill="#f8fafc" stroke="#e2e8f0" stroke-width="1"/>
    <circle cx="209" cy="208" r="7" fill="#f8fafc" stroke="#e2e8f0" stroke-width="1"/>

    <!-- 腿 -->
    <rect x="108" y="250" width="22" height="35" rx="8" fill="#f8fafc" stroke="#e2e8f0" stroke-width="1"/>
    <rect x="103" y="280" width="32" height="10" rx="5" fill="#e2e8f0"/>
    <rect x="150" y="250" width="22" height="35" rx="8" fill="#f8fafc" stroke="#e2e8f0" stroke-width="1"/>
    <rect x="145" y="280" width="32" height="10" rx="5" fill="#e2e8f0"/>
  </svg>
</template>
