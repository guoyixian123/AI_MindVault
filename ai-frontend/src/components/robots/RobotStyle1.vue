<template>
  <svg viewBox="0 0 280 330" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="s1-headGrad" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" stop-color="#2d5a3d" />
        <stop offset="100%" stop-color="#1e3d2a" />
      </linearGradient>
      <linearGradient id="s1-screenGrad" x1="0%" y1="0%" x2="0%" y2="100%">
        <stop offset="0%" stop-color="#0f172a" />
        <stop offset="100%" stop-color="#1e293b" />
      </linearGradient>
      <radialGradient id="s1-eyeGlow" cx="50%" cy="50%" r="50%">
        <stop offset="0%" stop-color="#8fae8b" />
        <stop offset="100%" stop-color="#3a7250" />
      </radialGradient>
      <filter id="s1-glow">
        <feGaussianBlur stdDeviation="3" result="blur"/>
        <feMerge><feMergeNode in="blur"/><feMergeNode in="SourceGraphic"/></feMerge>
      </filter>
    </defs>

    <!-- 头部 -->
    <rect x="55" y="30" width="170" height="130" rx="50" fill="url(#s1-headGrad)"/>
    <rect x="80" y="60" width="120" height="60" rx="20" fill="url(#s1-screenGrad)" stroke="#3a7250" stroke-width="1.5" stroke-opacity="0.4"/>

    <!-- 眼睛 -->
    <ellipse cx="120" cy="90" rx="14" ry="14" fill="url(#s1-eyeGlow)" filter="url(#s1-glow)"/>
    <ellipse cx="120" cy="90" rx="6" ry="6" fill="white" opacity="0.9"/>
    <circle cx="124" cy="87" r="2.5" fill="white" opacity="0.7"/>

    <ellipse cx="160" cy="90" rx="14" ry="14" fill="url(#s1-eyeGlow)" filter="url(#s1-glow)"/>
    <ellipse cx="160" cy="90" rx="6" ry="6" fill="white" opacity="0.9"/>
    <circle cx="164" cy="87" r="2.5" fill="white" opacity="0.7"/>

    <!-- 嘴巴 -->
    <path d="M125,108 Q140,118 155,108" fill="none" stroke="#8fae8b" stroke-width="2.5" stroke-linecap="round"/>

    <!-- 耳朵 -->
    <rect x="45" y="75" width="14" height="30" rx="7" fill="#3a7250"/>
    <rect x="221" y="75" width="14" height="30" rx="7" fill="#3a7250"/>

    <!-- 天线 -->
    <line x1="140" y1="30" x2="140" y2="10" stroke="#3a7250" stroke-width="2.5" stroke-linecap="round"/>
    <circle cx="140" cy="8" r="4" fill="#c4654a"/>

    <!-- 颈部 -->
    <rect x="125" y="160" width="30" height="10" rx="4" fill="#e2e8f0"/>

    <!-- 身体 -->
    <rect x="70" y="170" width="140" height="100" rx="24" fill="#f8fafc" stroke="#e2e8f0" stroke-width="1.5"/>
    <rect x="136" y="196" width="8" height="28" rx="2" fill="#14b8a6"/>
    <rect x="126" y="206" width="28" height="8" rx="2" fill="#14b8a6"/>

    <!-- 手臂 -->
    <rect x="38" y="180" width="26" height="60" rx="13" fill="#f1f5f9" stroke="#e2e8f0" stroke-width="1"/>
    <rect x="216" y="180" width="26" height="60" rx="13" fill="#f1f5f9" stroke="#e2e8f0" stroke-width="1"/>

    <!-- 腿 -->
    <rect x="100" y="270" width="26" height="35" rx="10" fill="#f1f5f9" stroke="#e2e8f0" stroke-width="1"/>
    <rect x="94" y="300" width="38" height="12" rx="6" fill="#e2e8f0"/>
    <rect x="154" y="270" width="26" height="35" rx="10" fill="#f1f5f9" stroke="#e2e8f0" stroke-width="1"/>
    <rect x="148" y="300" width="38" height="12" rx="6" fill="#e2e8f0"/>
  </svg>
</template>
