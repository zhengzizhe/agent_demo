<template>
  <div class="sidebar" :class="{ collapsed: isCollapsed }">
    <!-- 顶部操作已移至文档库内 -->

    <nav class="sidebar-nav">
      <!-- 对话 -->
      <div class="nav-section">
        <button
          class="nav-item"
          :class="{ active: currentView === 'chat' }"
          @click="$emit('view-change', 'chat')"
          :title="isCollapsed ? '对话' : ''"
        >
          <svg class="nav-icon" width="14" height="14" viewBox="0 0 20 20" fill="none">
            <path d="M2 5a2 2 0 012-2h7a2 2 0 012 2v4a2 2 0 01-2 2H4a2 2 0 01-2-2V5z" stroke="currentColor" stroke-width="1.5" fill="none"/>
            <path d="M13 5a2 2 0 012-2h3a2 2 0 012 2v4a2 2 0 01-2 2h-3a2 2 0 01-2-2V5z" stroke="currentColor" stroke-width="1.5" fill="none"/>
            <path d="M2 13a2 2 0 012-2h3a2 2 0 012 2v4a2 2 0 01-2 2H4a2 2 0 01-2-2v-4z" stroke="currentColor" stroke-width="1.5" fill="none"/>
            <path d="M13 13a2 2 0 012-2h3a2 2 0 012 2v4a2 2 0 01-2 2h-3a2 2 0 01-2-2v-4z" stroke="currentColor" stroke-width="1.5" fill="none"/>
          </svg>
          <span v-if="!isCollapsed" class="nav-text">对话</span>
        </button>
      </div>

      <!-- 任务功能已移至文档库内 -->

      <!-- 文档库 -->
      <div class="nav-section">
        <button
          class="nav-item"
          :class="{ active: currentView === 'docs' }"
          @click="$emit('view-change', 'docs')"
          :title="isCollapsed ? '文档库' : ''"
        >
          <svg class="nav-icon" width="14" height="14" viewBox="0 0 20 20" fill="none">
            <path d="M4 4h12v12H4V4z" stroke="currentColor" stroke-width="1.5" fill="none"/>
            <path d="M6 8h8M6 12h6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <span v-if="!isCollapsed" class="nav-text">文档库</span>
        </button>
      </div>

      <!-- 工具 -->
      <div class="nav-section">
        <div class="nav-section-title" v-if="!isCollapsed">工具</div>
        <button
          class="nav-item"
          :class="{ active: showColorPalette }"
          @click="$emit('color-palette-toggle')"
          :title="isCollapsed ? '主题设置' : ''"
        >
          <svg class="nav-icon" width="14" height="14" viewBox="0 0 20 20" fill="none">
            <circle cx="10" cy="6" r="3" stroke="currentColor" stroke-width="1.5" fill="none"/>
            <circle cx="6" cy="14" r="3" stroke="currentColor" stroke-width="1.5" fill="none"/>
            <circle cx="14" cy="14" r="3" stroke="currentColor" stroke-width="1.5" fill="none"/>
            <path d="M10 9v5M6 11l8 0" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <span v-if="!isCollapsed" class="nav-text">主题设置</span>
        </button>
        <button
          class="nav-item"
          :class="{ active: showDebugPanel }"
          @click="$emit('debug-toggle')"
          :title="isCollapsed ? '调试' : ''"
        >
          <svg class="nav-icon" width="14" height="14" viewBox="0 0 20 20" fill="none">
            <path d="M10 2L2 7l8 5 8-5-8-5z" stroke="currentColor" stroke-width="1.5" fill="none"/>
            <path d="M2 7v6l8 5 8-5V7" stroke="currentColor" stroke-width="1.5" fill="none"/>
          </svg>
          <span v-if="!isCollapsed" class="nav-text">调试</span>
        </button>
      </div>
    </nav>

    <div class="sidebar-footer">
      <!-- 语言切换 -->
      <div class="language-switcher" v-if="!isCollapsed">
        <div class="language-switcher-container">
          <div class="language-switcher-slider" :style="langSliderStyle"></div>
          <button
            v-for="lang in languages"
            :key="lang.code"
            class="lang-btn"
            :class="{ active: currentLang === lang.code }"
            @click="switchLanguage(lang.code)"
            :title="lang.name"
            :data-lang="lang.code"
          >
            <span class="lang-flag">{{ lang.flag }}</span>
            <span class="lang-name">{{ lang.name }}</span>
          </button>
        </div>
      </div>
      <div class="language-switcher-collapsed" v-else>
        <button
          class="lang-btn-icon"
          :title="currentLanguageName"
          @click="showLangDropdown = !showLangDropdown"
        >
          <span>{{ currentLanguageFlag }}</span>
        </button>
        <div v-if="showLangDropdown" class="lang-dropdown">
          <button
            v-for="lang in languages"
            :key="lang.code"
            class="lang-dropdown-item"
            :class="{ active: currentLang === lang.code }"
            @click="switchLanguage(lang.code); showLangDropdown = false"
          >
            <span class="lang-flag">{{ lang.flag }}</span>
            <span class="lang-name">{{ lang.name }}</span>
          </button>
        </div>
      </div>
      
      <div class="user-info" v-if="!isCollapsed">
        <div class="user-avatar">U</div>
        <div class="user-details">
          <div class="user-name">用户</div>
          <div class="user-status">在线</div>
        </div>
      </div>
      <button class="collapse-btn" @click="toggleCollapse" :title="isCollapsed ? '展开' : '收起'">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <path v-if="!isCollapsed" d="M6 12L2 8l4-4M10 12l4-4-4-4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          <path v-else d="M6 2l4 4-4 4M10 2l-4 4 4 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useI18n } from '../composables/useI18n.js'

defineProps({
  currentView: {
    type: String,
    default: 'chat'
  },
  showDebugPanel: {
    type: Boolean,
    default: false
  },
  showColorPalette: {
    type: Boolean,
    default: false
  }
})

defineEmits(['view-change', 'debug-toggle', 'color-palette-toggle'])

const isCollapsed = ref(false)
const showLangDropdown = ref(false)

// 展开/收起状态管理
const expandedSections = ref({
  tasks: true,
  docs: true
})

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
  if (isCollapsed.value) {
    showLangDropdown.value = false
    // 收起时关闭所有展开的section
    Object.keys(expandedSections.value).forEach(key => {
      expandedSections.value[key] = false
    })
  }
}

const toggleSection = (section) => {
  if (isCollapsed.value) return
  expandedSections.value[section] = !expandedSections.value[section]
}

// 语言切换
const { currentLang, languages, switchLanguage } = useI18n()

const currentLanguageName = computed(() => {
  return languages.find(l => l.code === currentLang.value)?.name || '中文'
})

const currentLanguageFlag = computed(() => {
  return languages.find(l => l.code === currentLang.value)?.flag || '🇨🇳'
})

// 语言切换器滑动指示器样式
const langSliderStyle = computed(() => {
  const index = languages.findIndex(lang => lang.code === currentLang.value)
  if (index === -1) return { transform: 'translateX(0)', width: '0' }
  
  const width = 100 / languages.length
  return {
    transform: `translateX(${index * 100}%)`,
    width: `${width}%`
  }
})
</script>

<style scoped>
.sidebar {
  width: 280px;
  height: calc(100vh - 40px);
  margin: 20px;
  background: var(--glass-bg);
  display: flex;
  flex-direction: column;
  transition: all 0.5s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
  z-index: 10;
  will-change: width, transform;
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-large);
  overflow: hidden;
  isolation: isolate;
  backdrop-filter: blur(60px) saturate(200%);
  -webkit-backdrop-filter: blur(60px) saturate(200%);
  box-shadow: 
    0 0 0 1px rgba(255, 255, 255, 0.6) inset,
    var(--shadow-soft),
    0 0 60px rgba(0, 102, 255, 0.03);
}

.sidebar::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: var(--radius-large);
  background: 
    linear-gradient(135deg, 
      rgba(255, 255, 255, 0.6) 0%, 
      rgba(255, 255, 255, 0.3) 30%,
      rgba(255, 255, 255, 0.2) 50%,
      rgba(255, 255, 255, 0.3) 70%,
      rgba(255, 255, 255, 0.5) 100%);
  pointer-events: none;
  z-index: 0;
  opacity: 0.7;
}

.sidebar::after {
  content: '';
  position: absolute;
  top: -30%;
  right: -30%;
  width: 60%;
  height: 60%;
  background: radial-gradient(circle, var(--theme-accent-subtle) 0%, transparent 70%);
  pointer-events: none;
  z-index: 0;
  opacity: 0;
  transition: opacity 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

.sidebar:hover {
  transform: translateY(-3px);
  box-shadow: 
    0 0 0 1px rgba(255, 255, 255, 0.7) inset,
    var(--shadow-medium),
    0 0 80px rgba(0, 102, 255, 0.05);
}

.sidebar:hover::after {
  opacity: 0.4;
}

.sidebar.collapsed {
  width: 60px;
}

.sidebar-header {
  display: none;
}

/* 顶部操作 */
.sidebar-actions {
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border: none;
  background: rgba(0, 0, 0, 0.04);
  border-radius: 8px;
  color: rgba(0, 0, 0, 0.7);
  font-size: 13px;
  font-weight: 400;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  text-align: left;
  position: relative;
  overflow: hidden;
}

.action-btn::before {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.04);
  opacity: 0;
  transition: opacity 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}

.action-btn:hover {
  background: rgba(0, 0, 0, 0.08);
  color: rgba(0, 0, 0, 0.9);
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.action-btn:hover::before {
  opacity: 1;
}

.action-btn:active {
  transform: translateY(0);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.action-icon {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
  color: rgba(0, 0, 0, 0.5);
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
}

.action-btn:hover .action-icon {
  transform: scale(1.1);
  color: rgba(0, 0, 0, 0.7);
}

.action-text {
  flex: 1;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  width: 100%;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 8px;
  border-radius: 8px;
}

.sidebar-logo:hover {
  background: rgba(0, 0, 0, 0.04); /* 深色悬停效果 */
  transform: scale(0.98);
}

.sidebar-logo:active {
  transform: scale(0.95);
  background: rgba(0, 0, 0, 0.06);
}

.logo-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  border-radius: 8px;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #1d2129; /* 深色文字，高对比度 */
  white-space: nowrap;
  letter-spacing: -0.02em;
}

.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  padding: 20px 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  transition: padding 0.5s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
  z-index: 1;
}

.nav-section {
  margin-bottom: 16px;
}

.nav-section-title {
  font-size: 11px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.4);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 8px 12px 4px;
  margin-bottom: 2px;
  transition: opacity 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.nav-section-hint {
  font-size: 11px;
  color: rgba(0, 0, 0, 0.4);
  padding: 0 16px 8px;
  line-height: 1.4;
}

.nav-item {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 16px;
  border: none;
  background: transparent;
  border-radius: var(--radius-small);
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  color: var(--color-text-secondary);
  font-size: 14px;
  font-weight: 500;
  text-align: left;
  position: relative;
  min-height: 44px;
  overflow: hidden;
  transform-origin: left center;
}

.nav-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%) scaleY(0);
  width: 4px;
  height: 0;
  background: linear-gradient(135deg, var(--theme-accent) 0%, var(--theme-accent-light) 100%);
  border-radius: 0 4px 4px 0;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  opacity: 0;
  box-shadow: 0 0 12px var(--theme-accent-glow);
}

.nav-item::after {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.04);
  border-radius: 8px;
  opacity: 0;
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  pointer-events: none;
}

.nav-item:hover {
  background: var(--theme-accent-subtle);
  color: var(--color-text-primary);
  transform: translateX(6px) scale(1.02);
  box-shadow: var(--shadow-soft);
}

.nav-item:hover::after {
  opacity: 1;
  background: rgba(0, 102, 255, 0.08);
}

.nav-item:hover .nav-icon {
  transform: scale(1.1) translateX(2px);
  color: var(--theme-accent);
}

.nav-item.active {
  background: linear-gradient(135deg, var(--theme-accent-subtle) 0%, rgba(0, 102, 255, 0.06) 100%);
  color: var(--theme-accent);
  font-weight: 600;
  box-shadow: var(--shadow-soft), 0 0 20px var(--theme-accent-glow);
  transform: translateX(2px);
}

.nav-item.active::before {
  transform: translateY(-50%) scaleY(1);
  height: 24px;
  opacity: 1;
}

.nav-item.active::after {
  background: rgba(0, 102, 255, 0.1);
  opacity: 1;
}

.nav-item.active .nav-icon {
  transform: scale(1.1);
  color: var(--theme-accent);
  filter: drop-shadow(0 0 4px var(--theme-accent-glow));
}


.nav-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
  color: var(--color-text-tertiary);
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  transform-origin: center;
}

.nav-item.active .nav-icon {
  color: var(--theme-accent);
}

.nav-item-group {
  position: relative;
}

.nav-chevron {
  width: 12px;
  height: 12px;
  flex-shrink: 0;
  color: rgba(0, 0, 0, 0.4);
  transition: transform 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  margin-left: auto;
}

.nav-chevron-folder {
  margin-left: 0;
  margin-right: 4px;
}

.nav-item-group.expanded .nav-chevron,
.nav-item-folder.expanded .nav-chevron-folder {
  transform: rotate(90deg);
}

.nav-space-icon-wrapper {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-space-icon {
  width: 20px;
  height: 20px;
  background: #8b5cf6;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 10px;
  font-weight: 600;
  letter-spacing: -0.5px;
}

.nav-submenu {
  padding-left: 20px;
  margin-top: 2px;
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
  animation: slideDown 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  overflow: hidden;
}

@keyframes slideDown {
  from {
    opacity: 0;
    max-height: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    max-height: 500px;
    transform: translateY(0);
  }
}

.nav-subitem {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border: none;
  background: transparent;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  color: rgba(0, 0, 0, 0.7);
  font-size: 13px;
  font-weight: 400;
  text-align: left;
  min-height: 32px;
  position: relative;
}

.nav-subitem::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 2px;
  height: 0;
  background: var(--theme-accent, #165dff);
  border-radius: 0 1px 1px 0;
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  opacity: 0;
}

.nav-subitem:hover {
  background: rgba(0, 0, 0, 0.04);
  color: rgba(0, 0, 0, 0.9);
  transform: translateX(2px);
}

.nav-subitem:hover::before {
  height: 16px;
  opacity: 1;
}

.nav-subitem.active {
  background: rgba(0, 0, 0, 0.06);
  color: rgba(0, 0, 0, 0.9);
  font-weight: 500;
}

.nav-subitem.active::before {
  height: 16px;
  opacity: 1;
}

.nav-subitem-icon {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
  color: rgba(0, 0, 0, 0.5);
}

.nav-subitem.active .nav-subitem-icon {
  color: rgba(0, 0, 0, 0.7);
}

.nav-subitem-text {
  flex: 1;
}

.nav-badge {
  font-size: 11px;
  font-weight: 500;
  color: rgba(0, 0, 0, 0.5);
  background: rgba(0, 0, 0, 0.06);
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
  line-height: 1.4;
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  transform-origin: center;
}

.nav-subitem:hover .nav-badge {
  transform: scale(1.1);
}

.nav-badge-primary {
  background: var(--theme-accent, #165dff);
  color: #ffffff;
}

.nav-badge-urgent {
  background: #ef4444;
  color: #ffffff;
}

.nav-divider {
  height: 1px;
  background: rgba(0, 0, 0, 0.06);
  margin: 4px 0;
}

.nav-project-color {
  width: 12px;
  height: 12px;
  border-radius: 3px;
  flex-shrink: 0;
  margin-right: 8px;
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  box-shadow: 0 0 0 0 rgba(0, 0, 0, 0);
}

.nav-subitem:hover .nav-project-color {
  transform: scale(1.15);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.nav-subitem-project {
  display: flex;
  align-items: center;
}

.nav-subitem-add {
  color: rgba(0, 0, 0, 0.5);
  font-style: italic;
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}

.nav-subitem-add:hover {
  color: var(--theme-accent, #165dff);
  background: rgba(22, 93, 255, 0.05);
  transform: translateX(2px);
}

.nav-subitem-add svg {
  transition: transform 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}

.nav-subitem-add:hover svg {
  transform: rotate(90deg) scale(1.1);
}

.nav-subitem-add svg {
  margin-right: 8px;
}

.nav-space-icon {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
  margin-right: 8px;
  color: rgba(0, 0, 0, 0.4);
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}

.nav-subitem:hover .nav-space-icon {
  color: rgba(0, 0, 0, 0.6);
  transform: scale(1.1);
}

.nav-item-recent {
  position: relative;
}

.nav-item-close {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  display: none;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: rgba(0, 0, 0, 0.4);
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s ease;
  z-index: 1;
}

.nav-item-recent:hover .nav-item-close {
  display: flex;
}

.nav-item-close:hover {
  background: rgba(0, 0, 0, 0.06);
  color: rgba(0, 0, 0, 0.7);
}

.nav-text {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: opacity 0.3s cubic-bezier(0.16, 1, 0.3, 1), 
              width 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.sidebar.collapsed .nav-text,
.sidebar.collapsed .nav-section-title {
  opacity: 0;
  width: 0;
  overflow: hidden;
}

.sidebar.collapsed .nav-item {
  justify-content: center;
  padding: 8px;
  min-height: 32px;
}

.sidebar.collapsed .action-btn {
  justify-content: center;
  padding: 8px;
}

.sidebar.collapsed .action-text {
  display: none;
}

.sidebar-footer {
  padding: 16px 12px;
  margin-top: auto;
  display: flex;
  flex-direction: column;
  background: transparent;
  align-items: stretch;
  gap: 12px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  position: relative;
  z-index: 1;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: var(--radius-small);
  border: 1px solid rgba(0, 0, 0, 0.04);
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  cursor: pointer;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.7);
  transform: translateY(-2px);
  box-shadow: var(--shadow-soft);
  border-color: rgba(0, 102, 255, 0.1);
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-small);
  background: linear-gradient(135deg, var(--theme-accent) 0%, var(--theme-accent-light) 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
  flex-shrink: 0;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  box-shadow: 0 4px 12px var(--theme-accent-glow);
}

.user-info:hover .user-avatar {
  transform: scale(1.05) rotate(3deg);
  box-shadow: 0 6px 16px var(--theme-accent-glow);
}

.user-details {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 12px;
  font-weight: 500;
  color: rgba(0, 0, 0, 0.8);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-status {
  font-size: 10px;
  color: rgba(0, 0, 0, 0.5);
  margin-top: 2px;
}

.collapse-btn {
  width: 100%;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(0, 0, 0, 0.04);
  border-radius: var(--radius-small);
  cursor: pointer;
  color: var(--color-text-secondary);
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  flex-shrink: 0;
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.collapse-btn::before {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.04);
  opacity: 0;
  transition: opacity 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}

.collapse-btn:hover {
  background: rgba(255, 255, 255, 0.7);
  color: var(--color-text-primary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-soft);
  border-color: rgba(0, 102, 255, 0.1);
}

.collapse-btn:hover::before {
  opacity: 1;
}

.collapse-btn:active {
  transform: translateY(0);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.collapse-btn:hover {
  background: rgba(0, 0, 0, 0.04);
  color: rgba(0, 0, 0, 0.7);
}

.collapse-btn:active {
  background: rgba(0, 0, 0, 0.06);
}

.collapse-btn svg {
  transition: transform 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
  z-index: 1;
}

.collapse-btn:hover svg {
  transform: scale(1.1);
}

/* 语言切换 */
.language-switcher {
  margin-bottom: 8px;
  width: 100%;
}

.language-switcher-container {
  position: relative;
  display: flex;
  align-items: center;
  background: rgba(0, 0, 0, 0.04);
  border-radius: 10px;
  padding: 3px;
  gap: 0;
  box-shadow: 
    0 1px 3px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
}

.language-switcher-slider {
  position: absolute;
  top: 3px;
  left: 3px;
  height: calc(100% - 6px);
  background: linear-gradient(135deg, var(--theme-accent, #165dff) 0%, #4c7fff 50%, #7b9fff 100%);
  border-radius: 7px;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  box-shadow: 
    0 2px 8px rgba(22, 93, 255, 0.3),
    0 1px 3px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  z-index: 1;
}

.lang-btn {
  position: relative;
  z-index: 2;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 6px 8px;
  font-size: 11px;
  color: rgba(0, 0, 0, 0.6);
  background: transparent;
  border: none;
  border-radius: 7px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: visible;
  font-weight: 600;
}

.lang-btn:hover {
  color: rgba(0, 0, 0, 0.8);
  transform: translateY(-1px);
}

.lang-btn.active {
  color: white;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.lang-btn.active:hover {
  transform: translateY(-2px);
}

.lang-flag {
  font-size: 14px;
  line-height: 1;
}

.lang-name {
  font-size: 11px;
  font-weight: 500;
}

.language-switcher-collapsed {
  position: relative;
  margin-bottom: 12px;
  width: 100%;
}

.lang-btn-icon {
  width: 100%;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  background: rgba(0, 0, 0, 0.02);
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
  overflow: hidden;
}

.lang-btn-icon::before {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.04);
  opacity: 0;
  transition: opacity 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}

.lang-btn-icon:hover {
  background: rgba(0, 0, 0, 0.04);
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.lang-btn-icon:hover::before {
  opacity: 1;
}

.lang-dropdown {
  position: absolute;
  bottom: 100%;
  left: 0;
  right: 0;
  margin-bottom: 8px;
  background: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  z-index: 100;
  animation: slideUp 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  transform-origin: bottom center;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(8px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.lang-dropdown-item {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.8);
  background: transparent;
  border: none;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
}

.lang-dropdown-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 0;
  background: var(--theme-accent, #165dff);
  transition: width 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}

.lang-dropdown-item:last-child {
  border-bottom: none;
}

.lang-dropdown-item:hover {
  background: rgba(0, 0, 0, 0.03);
  transform: translateX(2px);
}

.lang-dropdown-item:hover::before {
  width: 3px;
}

.lang-dropdown-item.active {
  background: rgba(22, 93, 255, 0.08);
  color: var(--theme-accent, #165dff);
}

.lang-dropdown-item.active::before {
  width: 3px;
}

.collapse-btn svg {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.sidebar.collapsed .collapse-btn svg {
  transform: rotate(180deg);
}

.sidebar.collapsed .user-info {
  display: none;
}

.sidebar.collapsed .collapse-btn {
  width: 100%;
}

/* 滚动条样式 - 浅色主题 */
.sidebar-nav::-webkit-scrollbar {
  width: 6px;
}

.sidebar-nav::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar-nav::-webkit-scrollbar-thumb {
  background: #d1d5db; /* 浅色滚动条 */
  border-radius: 3px;
}

.sidebar-nav::-webkit-scrollbar-thumb:hover {
  background: #9ca3af; /* 悬停时更深 */
}
</style>

