<template>
  <div class="sidebar" :class="{ collapsed: isCollapsed }">
    <!-- 顶部操作 -->
    <div v-if="!isCollapsed" class="sidebar-actions">
      <button class="action-btn" @click="$emit('view-change', 'new-doc')">
        <svg class="action-icon" width="16" height="16" viewBox="0 0 16 16" fill="none">
          <path d="M8 2v12M2 8h12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          <path d="M4 4h8v8H4V4z" stroke="currentColor" stroke-width="1.5" fill="none"/>
        </svg>
        <span class="action-text">新文件</span>
      </button>
      <button class="action-btn" @click="$emit('view-change', 'shared')">
        <svg class="action-icon" width="16" height="16" viewBox="0 0 16 16" fill="none">
          <path d="M8 2l6 6-6 6M2 8h12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <span class="action-text">与我分享</span>
      </button>
    </div>

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

      <!-- 任务 -->
      <div class="nav-section">
        <div class="nav-section-title" v-if="!isCollapsed">任务</div>
        <button
          class="nav-item nav-item-group"
          :class="{ expanded: expandedSections.tasks }"
          @click="toggleSection('tasks')"
          :title="isCollapsed ? '任务' : ''"
        >
          <svg class="nav-icon" width="14" height="14" viewBox="0 0 20 20" fill="none">
            <path d="M4 4h12v12H4V4z" stroke="currentColor" stroke-width="1.5" fill="none"/>
            <path d="M6 8h8M6 12h6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <span v-if="!isCollapsed" class="nav-text">任务</span>
          <svg v-if="!isCollapsed" class="nav-chevron" width="12" height="12" viewBox="0 0 12 12" fill="none">
            <path d="M4 2l4 4-4 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
        <div v-if="!isCollapsed && expandedSections.tasks" class="nav-submenu">
          <button class="nav-subitem" @click="$emit('view-change', 'tasks-inbox')">
            <span class="nav-subitem-text">收件箱</span>
            <span class="nav-badge">3</span>
          </button>
          <button class="nav-subitem" @click="$emit('view-change', 'tasks-today')">
            <span class="nav-subitem-text">今天</span>
            <span class="nav-badge nav-badge-urgent">5</span>
          </button>
          <button class="nav-subitem" @click="$emit('view-change', 'tasks-week')">
            <span class="nav-subitem-text">本周</span>
            <span class="nav-badge">12</span>
          </button>
          <button class="nav-subitem" @click="$emit('view-change', 'tasks-todo')">
            <span class="nav-subitem-text">待办</span>
          </button>
          <button class="nav-subitem" @click="$emit('view-change', 'tasks-progress')">
            <span class="nav-subitem-text">进行中</span>
            <span class="nav-badge">8</span>
          </button>
          <button class="nav-subitem" @click="$emit('view-change', 'tasks-done')">
            <span class="nav-subitem-text">已完成</span>
          </button>
          <div class="nav-divider"></div>
          <button class="nav-subitem nav-subitem-project" @click="$emit('view-change', 'project-1')">
            <div class="nav-project-color" style="background: #3b82f6;"></div>
            <span class="nav-subitem-text">产品设计</span>
            <span class="nav-badge">7</span>
          </button>
          <button class="nav-subitem nav-subitem-project" @click="$emit('view-change', 'project-2')">
            <div class="nav-project-color" style="background: #10b981;"></div>
            <span class="nav-subitem-text">开发任务</span>
            <span class="nav-badge">15</span>
          </button>
          <button class="nav-subitem nav-subitem-project" @click="$emit('view-change', 'project-3')">
            <div class="nav-project-color" style="background: #f59e0b;"></div>
            <span class="nav-subitem-text">市场营销</span>
            <span class="nav-badge">4</span>
          </button>
          <button class="nav-subitem nav-subitem-add" @click="$emit('view-change', 'new-project')">
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
              <path d="M7 3v8M3 7h8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <span class="nav-subitem-text">新建项目</span>
          </button>
        </div>
      </div>

      <!-- 文档库 -->
      <div class="nav-section">
        <div class="nav-section-title" v-if="!isCollapsed">文档库</div>
        <button
          class="nav-item nav-item-group"
          :class="{ expanded: expandedSections.docs }"
          @click="toggleSection('docs')"
          :title="isCollapsed ? '文档库' : ''"
        >
          <svg class="nav-icon" width="14" height="14" viewBox="0 0 20 20" fill="none">
            <path d="M4 4h12v12H4V4z" stroke="currentColor" stroke-width="1.5" fill="none"/>
            <path d="M6 8h8M6 12h6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <span v-if="!isCollapsed" class="nav-text">文档库</span>
          <svg v-if="!isCollapsed" class="nav-chevron" width="12" height="12" viewBox="0 0 12 12" fill="none">
            <path d="M4 2l4 4-4 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
        <div v-if="!isCollapsed && expandedSections.docs" class="nav-submenu">
          <button class="nav-subitem" @click="$emit('view-change', 'docs-recent')">
            <span class="nav-subitem-text">最近</span>
          </button>
          <button class="nav-subitem" @click="$emit('view-change', 'docs-favorites')">
            <span class="nav-subitem-text">收藏</span>
            <span class="nav-badge">8</span>
          </button>
          <button class="nav-subitem" @click="$emit('view-change', 'docs-my')">
            <span class="nav-subitem-text">我的文档</span>
          </button>
          <button class="nav-subitem" @click="$emit('view-change', 'docs-shared')">
            <span class="nav-subitem-text">共享给我</span>
            <span class="nav-badge">5</span>
          </button>
          <div class="nav-divider"></div>
          <button class="nav-subitem nav-subitem-space" @click="$emit('view-change', 'space-personal')">
            <svg class="nav-space-icon" width="14" height="14" viewBox="0 0 14 14" fill="none">
              <circle cx="7" cy="7" r="6" stroke="currentColor" stroke-width="1.2" fill="none"/>
            </svg>
            <span class="nav-subitem-text">个人空间</span>
          </button>
          <button class="nav-subitem nav-subitem-space" @click="$emit('view-change', 'space-work')">
            <svg class="nav-space-icon" width="14" height="14" viewBox="0 0 14 14" fill="none">
              <rect x="2" y="2" width="10" height="10" rx="1" stroke="currentColor" stroke-width="1.2" fill="none"/>
            </svg>
            <span class="nav-subitem-text">工作空间</span>
          </button>
          <div class="nav-divider"></div>
          <button class="nav-subitem" @click="$emit('view-change', 'docs-templates')">
            <span class="nav-subitem-text">模板</span>
          </button>
          <button class="nav-subitem" @click="$emit('view-change', 'docs-trash')">
            <span class="nav-subitem-text">回收站</span>
          </button>
        </div>
      </div>

      <!-- RAG知识库 -->
      <div class="nav-section">
        <button
          class="nav-item"
          :class="{ active: currentView === 'rag' }"
          @click="$emit('view-change', 'rag')"
          :title="isCollapsed ? 'RAG知识库' : ''"
        >
          <svg class="nav-icon" width="14" height="14" viewBox="0 0 20 20" fill="none">
            <path d="M4 4h12v12H4V4z" stroke="currentColor" stroke-width="1.5" fill="none"/>
            <path d="M6 8h8M6 12h8M6 16h5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <span v-if="!isCollapsed" class="nav-text">RAG知识库</span>
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
        <button
          v-for="lang in languages"
          :key="lang.code"
          class="lang-btn"
          :class="{ active: currentLang === lang.code }"
          @click="switchLanguage(lang.code)"
          :title="lang.name"
        >
          <span class="lang-flag">{{ lang.flag }}</span>
          <span class="lang-name">{{ lang.name }}</span>
        </button>
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
</script>

<style scoped>
.sidebar {
  width: 240px;
  height: 100vh;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  transition: width 0.3s cubic-bezier(0.16, 1, 0.3, 1), box-shadow 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
  z-index: 10;
  will-change: width;
  border-right: 1px solid rgba(0, 0, 0, 0.08);
  overflow: hidden;
  isolation: isolate;
  box-shadow: 0 0 0 rgba(0, 0, 0, 0);
}

.sidebar:hover {
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.04);
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
  padding: 12px 8px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  transition: padding 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.nav-section {
  margin-bottom: 8px;
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
  gap: 10px;
  padding: 8px 12px;
  border: none;
  background: transparent;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  color: rgba(0, 0, 0, 0.7);
  font-size: 13px;
  font-weight: 400;
  text-align: left;
  position: relative;
  min-height: 36px;
  overflow: hidden;
  transform-origin: left center;
}

.nav-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%) scaleY(0);
  width: 3px;
  height: 0;
  background: var(--theme-accent, #165dff);
  border-radius: 0 2px 2px 0;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  opacity: 0;
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
  background: rgba(0, 0, 0, 0.04);
  color: rgba(0, 0, 0, 0.9);
  transform: translateX(2px);
}

.nav-item:hover::after {
  opacity: 1;
}

.nav-item:hover .nav-icon {
  transform: scale(1.05);
  color: rgba(0, 0, 0, 0.7);
}

.nav-item.active {
  background: rgba(0, 0, 0, 0.06);
  color: rgba(0, 0, 0, 0.9);
  font-weight: 500;
}

.nav-item.active::before {
  transform: translateY(-50%) scaleY(1);
  height: 20px;
  opacity: 1;
}

.nav-item.active::after {
  background: rgba(0, 0, 0, 0.06);
  opacity: 1;
}

.nav-item.active .nav-icon {
  transform: scale(1);
  color: rgba(0, 0, 0, 0.8);
}


.nav-icon {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
  color: rgba(0, 0, 0, 0.5);
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  transform-origin: center;
}

.nav-item.active .nav-icon {
  color: rgba(0, 0, 0, 0.7);
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
  padding: 12px 8px;
  margin-top: auto;
  display: flex;
  flex-direction: column;
  background: transparent;
  align-items: stretch;
  gap: 8px;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 1;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
  padding: 8px 12px;
  background: rgba(0, 0, 0, 0.02);
  border-radius: 8px;
  border: none;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  cursor: pointer;
}

.user-info:hover {
  background: rgba(0, 0, 0, 0.04);
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 13px;
  flex-shrink: 0;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  box-shadow: 0 2px 4px rgba(99, 102, 241, 0.2);
}

.user-info:hover .user-avatar {
  transform: scale(1.08) rotate(2deg);
  box-shadow: 0 4px 8px rgba(99, 102, 241, 0.3);
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
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: rgba(0, 0, 0, 0.02);
  border-radius: 8px;
  cursor: pointer;
  color: rgba(0, 0, 0, 0.5);
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  flex-shrink: 0;
  position: relative;
  overflow: hidden;
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
  background: rgba(0, 0, 0, 0.04);
  color: rgba(0, 0, 0, 0.7);
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
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
  display: flex;
  gap: 4px;
  margin-bottom: 8px;
  width: 100%;
}

.lang-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 6px 8px;
  font-size: 11px;
  color: rgba(0, 0, 0, 0.6);
  background: rgba(0, 0, 0, 0.02);
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
  overflow: hidden;
}

.lang-btn::before {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.04);
  opacity: 0;
  transition: opacity 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}

.lang-btn:hover {
  background: rgba(0, 0, 0, 0.04);
  color: rgba(0, 0, 0, 0.8);
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.lang-btn:hover::before {
  opacity: 1;
}

.lang-btn.active {
  background: var(--theme-accent, #165dff);
  color: #ffffff;
  box-shadow: 0 2px 6px rgba(22, 93, 255, 0.3);
  transform: translateY(-1px);
}

.lang-btn.active:hover {
  box-shadow: 0 4px 8px rgba(22, 93, 255, 0.4);
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

