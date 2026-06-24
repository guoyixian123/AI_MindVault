<template>
  <svg viewBox="0 0 280 325" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="s2-screenGrad" x1="0%" y1="0%" x2="0%" y2="100%">
        <stop offset="0%" stop-color="#0f172a" />
        <stop offset="100%" stop-color="#1e293b" />
      </linearGradient>
      <linearGradient id="s2-accentGrad" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" stop-color="#14b8a6" />
        <stop offset="100%" stop-color="#0d9488" />
      </linearGradient>
      <filter id="s2-glow">
        <feGaussianBlur stdDeviation="3" result="blur"/>
        <feMerge><feMergeNode in="blur"/><feMergeNode in="SourceGraphic"/></feMerge>
      </filter>
    </defs>

    <!-- 身体 -->
    <ellipse cx="140" cy="200" rx="75" ry="90" fill="#f8fafc" stroke="#e2e8f0" stroke-width="1.5"/>

    <!-- 头部 -->
    <circle cx="140" cy="100" r="55" fill="url(#s2-accentGrad)"/>

    <!-- 屏幕脸 -->
    <rect x="100" y="72" width="80" height="50" rx="16" fill="url(#s2-screenGrad)"/>

    <!-- 眼睛 -->
    <rect x="115" y="88" width="10" height="10" rx="2" fill="#22d3ee"/>
    <rect x="127" y="88" width="10" height="10" rx="2" fill="#22d3ee"/>
    <rect x="151" y="88" width="10" height="10" rx="2" fill="#22d3ee"/>
    <rect x="163" y="88" width="10" height="10" rx="2" fill="#22d3ee"/>

    <!-- 嘴巴 -->
    <rect x="130" y="108" width="6" height="6" rx="1" fill="#22d3ee" opacity="0.7"/>
    <rect x="138" y="108" width="6" height="6" rx="1" fill="#22d3ee" opacity="0.7"/>
    <rect x="146" y="108" width="6" height="6" rx="1" fill="#22d3ee" opacity="0.7"/>

    <!-- 天线 -->
    <line x1="140" y1="45" x2="140" y2="25" stroke="#14b8a6" stroke-width="2.5" stroke-linecap="round"/>
    <circle cx="140" cy="22" r="4" fill="#06b6d4"/>

    <!-- 耳朵 -->
    <circle cx="82" cy="100" r="8" fill="#f1f5f9" stroke="#e2e8f0" stroke-width="1.5"/>
    <circle cx="82" cy="100" r="4" fill="#14b8a6" opacity="0.6"/>
    <circle cx="198" cy="100" r="8" fill="#f1f5f9" stroke="#e2e8f0" stroke-width="1.5"/>
    <circle cx="198" cy="100" r="4" fill="#14b8a6" opacity="0.6"/>

    <!-- 颈部 -->
    <rect x="128" y="150" width="24" height="10" rx="5" fill="#e2e8f0"/>

    <!-- 胸口十字 -->
    <circle cx="140" cy="195" r="15" fill="none" stroke="#14b8a6" stroke-width="1.5" opacity="0.4"/>
    <path d="M140,183 L140,207 M128,195 L152,195" stroke="#14b8a6" stroke-width="2" stroke-linecap="round"/>

    <!-- 手臂 -->
    <ellipse cx="68" cy="200" rx="12" ry="25" fill="#f8fafc" stroke="#e2e8f0" stroke-width="1"/>
    <circle cx="68" cy="228" r="8" fill="#f8fafc" stroke="#e2e8f0" stroke-width="1"/>
    <ellipse cx="212" cy="200" rx="12" ry="25" fill="#f8fafc" stroke="#e2e8f0" stroke-width="1"/>
    <circle cx="212" cy="228" r="8" fill="#f8fafc" stroke="#e2e8f0" stroke-width="1"/>

    <!-- 腿 -->
    <rect x="115" y="280" width="20" height="25" rx="8" fill="#f8fafc" stroke="#e2e8f0" stroke-width="1"/>
    <ellipse cx="125" cy="308" rx="16" ry="6" fill="#e2e8f0"/>
    <rect x="145" y="280" width="20" height="25" rx="8" fill="#f8fafc" stroke="#e2e8f0" stroke-width="1"/>
    <ellipse cx="155" cy="308" rx="16" ry="6" fill="#e2e8f0"/>
  </svg>
</template>
