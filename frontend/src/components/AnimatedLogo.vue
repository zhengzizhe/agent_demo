<template>
  <div class="animated-logo" :class="{ 'collapsed': collapsed, 'large': size === 'large' }">
    <div class="logo-container">
      <!-- 动画图标 -->
      <div class="logo-icon-wrapper">
        <svg class="logo-icon" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
          <!-- 外圈（旋转） -->
          <circle 
            class="logo-circle outer" 
            cx="32" 
            cy="32" 
            r="28" 
            stroke="url(#gradient1)" 
            stroke-width="2.5"
            fill="none"
            stroke-linecap="round"
          />
          
          <!-- 中圈（脉冲） -->
          <circle 
            class="logo-circle middle" 
            cx="32" 
            cy="32" 
            r="20" 
            stroke="url(#gradient2)" 
            stroke-width="2"
            fill="none"
            stroke-linecap="round"
          />
          
          <!-- 内圈（旋转反向） -->
          <circle 
            class="logo-circle inner" 
            cx="32" 
            cy="32" 
            r="12" 
            stroke="url(#gradient3)" 
            stroke-width="2"
            fill="none"
            stroke-linecap="round"
          />
          
          <!-- 中心点（脉冲） -->
          <circle 
            class="logo-dot" 
            cx="32" 
            cy="32" 
            r="4" 
            fill="url(#gradient4)"
          />
          
          <!-- 连接线（动画） -->
          <g class="logo-connections">
            <line 
              class="connection-line" 
              x1="32" 
              y1="4" 
              x2="32" 
              y2="20" 
              stroke="url(#gradient2)" 
              stroke-width="2"
              stroke-linecap="round"
            />
            <line 
              class="connection-line" 
              x1="60" 
              y1="32" 
              x2="44" 
              y2="32" 
              stroke="url(#gradient2)" 
              stroke-width="2"
              stroke-linecap="round"
            />
            <line 
              class="connection-line" 
              x1="32" 
              y1="60" 
              x2="32" 
              y2="44" 
              stroke="url(#gradient2)" 
              stroke-width="2"
              stroke-linecap="round"
            />
            <line 
              class="connection-line" 
              x1="4" 
              y1="32" 
              x2="20" 
              y2="32" 
              stroke="url(#gradient2)" 
              stroke-width="2"
              stroke-linecap="round"
            />
          </g>
          
          <!-- 粒子效果 -->
          <g class="logo-particles">
            <circle class="particle" cx="20" cy="20" r="2" fill="url(#particleGradient)" />
            <circle class="particle" cx="44" cy="20" r="2" fill="url(#particleGradient)" />
            <circle class="particle" cx="44" cy="44" r="2" fill="url(#particleGradient)" />
            <circle class="particle" cx="20" cy="44" r="2" fill="url(#particleGradient)" />
          </g>
          
          <!-- 渐变定义 -->
          <defs>
            <linearGradient id="gradient1" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" :style="{ stopColor: primaryColor, stopOpacity: 1 }" />
              <stop offset="100%" :style="{ stopColor: secondaryColor, stopOpacity: 0.6 }" />
            </linearGradient>
            <linearGradient id="gradient2" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" :style="{ stopColor: secondaryColor, stopOpacity: 1 }" />
              <stop offset="100%" :style="{ stopColor: primaryColor, stopOpacity: 0.8 }" />
            </linearGradient>
            <linearGradient id="gradient3" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" :style="{ stopColor: primaryColor, stopOpacity: 1 }" />
              <stop offset="100%" :style="{ stopColor: accentColor, stopOpacity: 0.9 }" />
            </linearGradient>
            <radialGradient id="gradient4" cx="50%" cy="50%">
              <stop offset="0%" :style="{ stopColor: accentColor, stopOpacity: 1 }" />
              <stop offset="100%" :style="{ stopColor: primaryColor, stopOpacity: 0.8 }" />
            </radialGradient>
            <radialGradient id="particleGradient" cx="50%" cy="50%">
              <stop offset="0%" :style="{ stopColor: accentColor, stopOpacity: 1 }" />
              <stop offset="100%" :style="{ stopColor: primaryColor, stopOpacity: 0 }" />
            </radialGradient>
          </defs>
        </svg>
      </div>
      
      <!-- 产品名称 -->
      <span v-if="!collapsed && showText" class="logo-text">AgentFlow</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  collapsed: {
    type: Boolean,
    default: false
  },
  size: {
    type: String,
    default: 'normal' // 'small', 'normal', 'large'
  },
  showText: {
    type: Boolean,
    default: true
  }
})

// 缓存 CSS 变量值，避免频繁读取
let cachedAccentColor = null
let cacheTimestamp = 0
const CACHE_DURATION = 1000 // 缓存1秒

const primaryColor = computed(() => {
  const now = Date.now()
  // 如果缓存未过期，使用缓存值
  if (cachedAccentColor && (now - cacheTimestamp) < CACHE_DURATION) {
    return cachedAccentColor
  }
  // 否则重新读取并更新缓存
  cachedAccentColor = getComputedStyle(document.documentElement).getPropertyValue('--theme-accent').trim() || '#165dff'
  cacheTimestamp = now
  return cachedAccentColor
})
const secondaryColor = computed(() => {
  const accent = primaryColor.value
  // 根据强调色生成次要色（稍微浅一点）
  if (accent === '#165dff') return '#4080ff'
  if (accent === '#00d4ff') return '#40d4ff'
  if (accent === '#10a37f') return '#19c37d'
  if (accent === '#8b5cf6') return '#a78bfa'
  if (accent === '#f59e0b') return '#fbbf24'
  if (accent === '#ef4444') return '#f87171'
  return accent
})
const accentColor = computed(() => {
  const accent = primaryColor.value
  // 根据强调色生成强调色（更亮）
  if (accent === '#165dff') return '#00d4ff'
  if (accent === '#00d4ff') return '#00f0ff'
  if (accent === '#10a37f') return '#00ff88'
  if (accent === '#8b5cf6') return '#c084fc'
  if (accent === '#f59e0b') return '#fcd34d'
  if (accent === '#ef4444') return '#fca5a5'
  return accent
})
</script>

<style scoped>
.animated-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon-wrapper {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  flex-shrink: 0;
}

.animated-logo.large .logo-icon-wrapper {
  width: 120px;
  height: 120px;
}

.logo-icon {
  width: 100%;
  height: 100%;
  filter: drop-shadow(0 4px 16px rgba(22, 93, 255, 0.3));
}

/* 外圈旋转动画 */
.logo-circle.outer {
  animation: rotate 8s linear infinite;
  transform-origin: 32px 32px;
}

/* 中圈脉冲动画 */
.logo-circle.middle {
  animation: pulse 2s ease-in-out infinite;
  transform-origin: 32px 32px;
}

/* 内圈反向旋转 */
.logo-circle.inner {
  animation: rotateReverse 6s linear infinite;
  transform-origin: 32px 32px;
}

/* 中心点脉冲 */
.logo-dot {
  animation: dotPulse 1.5s ease-in-out infinite;
  transform-origin: 32px 32px;
}

/* 连接线动画 */
.logo-connections .connection-line {
  animation: linePulse 2s ease-in-out infinite;
  opacity: 0.8;
}

.logo-connections .connection-line:nth-child(1) {
  animation-delay: 0s;
}

.logo-connections .connection-line:nth-child(2) {
  animation-delay: 0.5s;
}

.logo-connections .connection-line:nth-child(3) {
  animation-delay: 1s;
}

.logo-connections .connection-line:nth-child(4) {
  animation-delay: 1.5s;
}

/* 粒子动画 */
.logo-particles .particle {
  animation: particleFloat 4s ease-in-out infinite;
  opacity: 0.6;
}

.logo-particles .particle:nth-child(1) {
  animation-delay: 0s;
}

.logo-particles .particle:nth-child(2) {
  animation-delay: 1s;
}

.logo-particles .particle:nth-child(3) {
  animation-delay: 2s;
}

.logo-particles .particle:nth-child(4) {
  animation-delay: 3s;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  background: linear-gradient(135deg, #165dff 0%, #4080ff 50%, #00d4ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.5px;
  animation: textShimmer 3s ease-in-out infinite;
  white-space: nowrap;
}

/* 动画定义 */
@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes rotateReverse {
  from {
    transform: rotate(360deg);
  }
  to {
    transform: rotate(0deg);
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 0.8;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.05);
  }
}

@keyframes dotPulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(1.2);
  }
}

@keyframes linePulse {
  0%, 100% {
    opacity: 0.6;
    stroke-width: 2;
  }
  50% {
    opacity: 1;
    stroke-width: 2.5;
  }
}

@keyframes particleFloat {
  0%, 100% {
    transform: translate(0, 0) scale(1);
    opacity: 0.4;
  }
  25% {
    transform: translate(5px, -5px) scale(1.2);
    opacity: 0.8;
  }
  50% {
    transform: translate(-3px, -8px) scale(1.1);
    opacity: 0.6;
  }
  75% {
    transform: translate(-5px, -3px) scale(1.15);
    opacity: 0.7;
  }
}

@keyframes textShimmer {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

/* 悬停效果 */
.animated-logo:hover .logo-icon {
  filter: drop-shadow(0 6px 20px rgba(22, 93, 255, 0.5));
  transform: scale(1.05);
  transition: all 0.3s ease;
}

.animated-logo:hover .logo-circle.outer {
  animation-duration: 4s;
}

.animated-logo:hover .logo-circle.inner {
  animation-duration: 3s;
}

/* 折叠状态 */
.animated-logo.collapsed {
  justify-content: center;
  padding: 8px;
}

.animated-logo.collapsed .logo-icon-wrapper {
  width: 32px;
  height: 32px;
}
</style>

