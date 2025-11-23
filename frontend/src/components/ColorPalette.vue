<template>
  <div v-if="isOpen" class="color-palette-overlay" @click.self="close">
    <div class="color-palette-panel" @click.stop>
      <div class="palette-header">
        <h3>主题设置</h3>
        <button class="palette-close" @click="close">×</button>
      </div>
      
      <div class="palette-content">
        <!-- 背景颜色 -->
        <div class="color-section">
          <label class="color-label">背景颜色</label>
          
          <!-- 纯色预设 -->
          <div class="preset-group">
            <div class="preset-group-label">纯色</div>
            <div class="color-presets">
              <button
                v-for="preset in solidPresets"
                :key="preset.name"
                class="color-preset"
                :class="{ active: currentBackground === preset.value && !isGradient }"
                :style="{ backgroundColor: preset.value }"
                @click="setBackground(preset.value)"
                :title="preset.name"
              >
                <svg v-if="currentBackground === preset.value && !isGradient" width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <path d="M13 4L6 11L3 8" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </button>
            </div>
          </div>
          
          <!-- 渐变预设 -->
          <div class="preset-group">
            <div class="preset-group-label">渐变</div>
            <div class="color-presets gradient-presets">
              <button
                v-for="preset in gradientPresets"
                :key="preset.name"
                class="color-preset gradient"
                :class="{ active: currentBackground === preset.value && isGradient }"
                :style="{ background: preset.value }"
                @click="setBackground(preset.value, true)"
                :title="preset.name"
              >
                <svg v-if="currentBackground === preset.value && isGradient" width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <path d="M13 4L6 11L3 8" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </button>
            </div>
          </div>
          
          <div class="color-custom">
            <input
              type="color"
              v-model="customBackground"
              @change="setBackground(customBackground, false)"
              class="color-input"
            />
            <span class="color-input-label">自定义纯色</span>
          </div>
        </div>

        <!-- 强调色 -->
        <div class="color-section">
          <label class="color-label">强调色</label>
          <div class="color-presets">
            <button
              v-for="preset in accentPresets"
              :key="preset.name"
              class="color-preset accent"
              :class="{ active: currentAccent === preset.value }"
              :style="{ backgroundColor: preset.value }"
              @click="setAccent(preset.value)"
              :title="preset.name"
            >
              <svg v-if="currentAccent === preset.value" width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M13 4L6 11L3 8" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
          </div>
          <div class="color-custom">
            <input
              type="color"
              v-model="customAccent"
              @change="setAccent(customAccent)"
              class="color-input"
            />
            <span class="color-input-label">自定义</span>
          </div>
        </div>

        <!-- 重置按钮 -->
        <div class="palette-actions">
          <button class="reset-btn" @click="resetColors">重置为默认</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'

const props = defineProps({
  isOpen: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close'])

const STORAGE_KEY_BG = 'agent_theme_background'
const STORAGE_KEY_ACCENT = 'agent_theme_accent'
const STORAGE_KEY_BG_TYPE = 'agent_theme_background_type'

const defaultBackground = '#ffffff'
const defaultAccent = '#165dff'

const currentBackground = ref(defaultBackground)
const currentAccent = ref(defaultAccent)
const customBackground = ref(defaultBackground)
const customAccent = ref(defaultAccent)
const isGradient = ref(false)

// 纯色预设
const solidPresets = [
  { name: '纯白', value: '#ffffff' },
  { name: '浅灰', value: '#f7f8fa' },
  { name: '米白', value: '#fafafa' },
  { name: '浅蓝', value: '#f0f4ff' },
  { name: '浅绿', value: '#f0fdf4' },
  { name: '浅粉', value: '#fef2f2' }
]

// 渐变预设
const gradientPresets = [
  { 
    name: '蓝紫渐变', 
    value: 'linear-gradient(135deg, rgba(235, 242, 255, 0.95) 0%, rgba(250, 240, 255, 0.95) 100%)' 
  },
  { 
    name: '蓝白渐变', 
    value: 'linear-gradient(135deg, rgba(235, 242, 255, 0.9) 0%, rgba(255, 255, 255, 0.95) 100%)' 
  },
  { 
    name: '粉橙渐变', 
    value: 'linear-gradient(135deg, rgba(255, 248, 240, 0.95) 0%, rgba(255, 242, 242, 0.95) 100%)' 
  },
  { 
    name: '绿蓝渐变', 
    value: 'linear-gradient(135deg, rgba(240, 253, 244, 0.95) 0%, rgba(235, 242, 255, 0.95) 100%)' 
  },
  { 
    name: '紫粉渐变', 
    value: 'linear-gradient(135deg, rgba(250, 240, 255, 0.95) 0%, rgba(255, 242, 242, 0.95) 100%)' 
  },
  { 
    name: '多彩渐变', 
    value: 'linear-gradient(135deg, rgba(235, 242, 255, 0.9) 0%, rgba(255, 255, 255, 0.88) 20%, rgba(255, 248, 240, 0.9) 40%, rgba(255, 255, 255, 0.88) 60%, rgba(250, 240, 255, 0.9) 80%, rgba(255, 255, 255, 0.88) 100%)' 
  }
]

const accentPresets = [
  { name: '蓝色', value: '#165dff' },
  { name: '青色', value: '#00d4ff' },
  { name: '绿色', value: '#10a37f' },
  { name: '紫色', value: '#8b5cf6' },
  { name: '橙色', value: '#f59e0b' },
  { name: '红色', value: '#ef4444' }
]

const loadColors = () => {
  const savedBg = localStorage.getItem(STORAGE_KEY_BG)
  const savedAccent = localStorage.getItem(STORAGE_KEY_ACCENT)
  const savedBgType = localStorage.getItem(STORAGE_KEY_BG_TYPE)
  
  if (savedBg) {
    currentBackground.value = savedBg
    if (savedBgType === 'gradient') {
      isGradient.value = true
    } else {
      isGradient.value = false
      customBackground.value = savedBg
    }
  }
  
  if (savedAccent) {
    currentAccent.value = savedAccent
    customAccent.value = savedAccent
  }
  
  applyColors()
}

const applyColors = () => {
  // 设置CSS变量（对于纯色）
  if (isGradient.value) {
    // 渐变背景：提取主要颜色作为fallback
    const mainColor = extractMainColorFromGradient(currentBackground.value)
    document.documentElement.style.setProperty('--theme-background', mainColor)
    // 渐变背景需要直接应用到元素
    document.documentElement.style.setProperty('--theme-background-gradient', currentBackground.value)
  } else {
    document.documentElement.style.setProperty('--theme-background', currentBackground.value)
    document.documentElement.style.setProperty('--theme-background-gradient', 'none')
  }
  
  document.documentElement.style.setProperty('--theme-accent', currentAccent.value)
  
  // 更新body背景
  document.body.style.background = currentBackground.value
}

// 从渐变中提取主要颜色（用于fallback）
const extractMainColorFromGradient = (gradient) => {
  // 尝试从渐变中提取第一个颜色
  const match = gradient.match(/rgba?\([^)]+\)/)
  if (match) {
    return match[0]
  }
  return '#ffffff'
}

const setBackground = (value, gradient = false) => {
  currentBackground.value = value
  isGradient.value = gradient
  
  if (!gradient) {
    customBackground.value = value
  }
  
  localStorage.setItem(STORAGE_KEY_BG, value)
  localStorage.setItem(STORAGE_KEY_BG_TYPE, gradient ? 'gradient' : 'solid')
  applyColors()
}

const setAccent = (color) => {
  currentAccent.value = color
  customAccent.value = color
  localStorage.setItem(STORAGE_KEY_ACCENT, color)
  applyColors()
}

const resetColors = () => {
  currentBackground.value = defaultBackground
  currentAccent.value = defaultAccent
  customBackground.value = defaultBackground
  customAccent.value = defaultAccent
  isGradient.value = false
  localStorage.removeItem(STORAGE_KEY_BG)
  localStorage.removeItem(STORAGE_KEY_ACCENT)
  localStorage.removeItem(STORAGE_KEY_BG_TYPE)
  applyColors()
}

const close = () => {
  emit('close')
}

onMounted(() => {
  loadColors()
})
</script>

<style scoped>
.color-palette-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  animation: fadeIn 0.2s ease;
}

.color-palette-panel {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  width: 90%;
  max-width: 480px;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  animation: slideUp 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.palette-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
}

.palette-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1d2129;
}

.palette-close {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  border-radius: 8px;
  color: #86909c;
  cursor: pointer;
  font-size: 24px;
  line-height: 1;
  transition: all 0.2s ease;
}

.palette-close:hover {
  background: rgba(0, 0, 0, 0.06);
  color: #1d2129;
}

.palette-content {
  padding: 24px;
  overflow-y: auto;
}

.color-section {
  margin-bottom: 32px;
}

.color-section:last-of-type {
  margin-bottom: 0;
}

.color-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #1d2129;
  margin-bottom: 12px;
}

.preset-group {
  margin-bottom: 20px;
}

.preset-group:last-of-type {
  margin-bottom: 16px;
}

.preset-group-label {
  font-size: 12px;
  font-weight: 500;
  color: #86909c;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.color-presets {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 8px;
  margin-bottom: 0;
}

.gradient-presets {
  grid-template-columns: repeat(3, 1fr);
}

.color-preset {
  width: 100%;
  aspect-ratio: 1;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  position: relative;
  padding: 0;
  overflow: hidden;
}

.color-preset.gradient {
  aspect-ratio: 1.5;
}

.color-preset:hover {
  transform: scale(1.05);
  border-color: #86909c;
}

.color-preset.active {
  border-color: var(--theme-accent, #165dff);
  border-width: 3px;
  box-shadow: 0 0 0 2px rgba(22, 93, 255, 0.1);
}

.color-preset svg {
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.3));
}

.color-custom {
  display: flex;
  align-items: center;
  gap: 12px;
}

.color-input {
  width: 48px;
  height: 48px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  padding: 0;
  background: none;
  -webkit-appearance: none;
  appearance: none;
}

.color-input::-webkit-color-swatch-wrapper {
  padding: 0;
}

.color-input::-webkit-color-swatch {
  border: none;
  border-radius: 6px;
}

.color-input-label {
  font-size: 14px;
  color: #86909c;
}

.palette-actions {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #e5e7eb;
}

.reset-btn {
  width: 100%;
  padding: 12px 24px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #ffffff;
  color: #86909c;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.reset-btn:hover {
  background: #f7f8fa;
  border-color: #d1d5db;
  color: #1d2129;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}
</style>

