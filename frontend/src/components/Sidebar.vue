<template>
  <div class="sidebar" :class="{ collapsed: isCollapsed }">
    <div class="sidebar-header">
      <div class="sidebar-logo" @click="toggleCollapse">
        <div class="logo-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <rect x="3" y="3" width="18" height="18" rx="3" fill="#2563eb"/>
            <path d="M8 12l3 3 5-6" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <span v-if="!isCollapsed" class="logo-text">Agent</span>
      </div>
    </div>

    <nav class="sidebar-nav">
      <div class="nav-section">
        <div class="nav-section-title" v-if="!isCollapsed">主要功能</div>
        <button
          class="nav-item"
          :class="{ active: currentView === 'chat' }"
          @click="$emit('view-change', 'chat')"
          :title="isCollapsed ? '对话' : ''"
        >
          <svg class="nav-icon" width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M2 5a2 2 0 012-2h7a2 2 0 012 2v4a2 2 0 01-2 2H4a2 2 0 01-2-2V5z" stroke="currentColor" stroke-width="1.5" fill="none"/>
            <path d="M13 5a2 2 0 012-2h3a2 2 0 012 2v4a2 2 0 01-2 2h-3a2 2 0 01-2-2V5z" stroke="currentColor" stroke-width="1.5" fill="none"/>
            <path d="M2 13a2 2 0 012-2h3a2 2 0 012 2v4a2 2 0 01-2 2H4a2 2 0 01-2-2v-4z" stroke="currentColor" stroke-width="1.5" fill="none"/>
            <path d="M13 13a2 2 0 012-2h3a2 2 0 012 2v4a2 2 0 01-2 2h-3a2 2 0 01-2-2v-4z" stroke="currentColor" stroke-width="1.5" fill="none"/>
          </svg>
          <span v-if="!isCollapsed" class="nav-text">对话</span>
        </button>

        <button
          class="nav-item"
          :class="{ active: currentView === 'rag' }"
          @click="$emit('view-change', 'rag')"
          :title="isCollapsed ? 'RAG知识库' : ''"
        >
          <svg class="nav-icon" width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M4 4h12v12H4V4z" stroke="currentColor" stroke-width="1.5" fill="none"/>
            <path d="M6 8h8M6 12h8M6 16h5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <span v-if="!isCollapsed" class="nav-text">RAG知识库</span>
        </button>

        <button
          class="nav-item"
          :class="{ active: currentView === 'docs' }"
          @click="$emit('view-change', 'docs')"
          :title="isCollapsed ? '文档库' : ''"
        >
          <svg class="nav-icon" width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M4 4h12v12H4V4z" stroke="currentColor" stroke-width="1.5" fill="none"/>
            <path d="M6 8h8M6 12h6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <span v-if="!isCollapsed" class="nav-text">文档库</span>
        </button>
      </div>

      <div class="nav-section">
        <div class="nav-section-title" v-if="!isCollapsed">工具</div>
        <button
          class="nav-item"
          :class="{ active: showDebugPanel }"
          @click="$emit('debug-toggle')"
          :title="isCollapsed ? '调试' : ''"
        >
          <svg class="nav-icon" width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M10 2L2 7l8 5 8-5-8-5z" stroke="currentColor" stroke-width="1.5" fill="none"/>
            <path d="M2 7v6l8 5 8-5V7" stroke="currentColor" stroke-width="1.5" fill="none"/>
          </svg>
          <span v-if="!isCollapsed" class="nav-text">调试</span>
        </button>
      </div>
    </nav>

    <div class="sidebar-footer">
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
import { ref } from 'vue'

defineProps({
  currentView: {
    type: String,
    default: 'chat'
  },
  showDebugPanel: {
    type: Boolean,
    default: false
  }
})

defineEmits(['view-change', 'debug-toggle'])

const isCollapsed = ref(false)

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}
</script>

<style scoped>
.sidebar {
  width: 240px;
  height: 100vh;
  background: #f7f8fa;
  display: flex;
  flex-direction: column;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 100;
  will-change: width;
  border-right: 1px solid #e5e6eb;
}

.sidebar.collapsed {
  width: 64px;
}

.sidebar-header {
  padding: 16px;
  min-height: 64px;
  display: flex;
  align-items: center;
  background: transparent;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  width: 100%;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 4px;
  border-radius: 6px;
}

.sidebar-logo:hover {
  background: #e5e6eb;
  transform: scale(0.98);
}

.sidebar-logo:active {
  transform: scale(0.95);
}

.logo-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  white-space: nowrap;
}

.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nav-section {
  margin-bottom: 8px;
}

.nav-section-title {
  font-size: 11px;
  font-weight: 600;
  color: #9ca3af;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 8px 12px 4px;
  margin-bottom: 4px;
}

.nav-item {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border: none;
  background: transparent;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  color: #4e5969;
  font-size: 14px;
  font-weight: 400;
  text-align: left;
  position: relative;
  overflow: hidden;
}

.nav-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%) scaleY(0);
  width: 3px;
  height: 0;
  background: #2563eb;
  border-radius: 0 3px 3px 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.nav-item:hover {
  background: #e5e6eb;
  color: #1d2129;
  transform: translateX(2px);
}

.nav-item.active {
  background: #e8f3ff;
  color: #165dff;
  font-weight: 500;
}

.nav-item.active::before {
  transform: translateY(-50%) scaleY(1);
  height: 20px;
  background: #165dff;
}

.nav-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  color: currentColor;
}

.nav-text {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: opacity 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.sidebar.collapsed .nav-text,
.sidebar.collapsed .nav-section-title {
  opacity: 0;
  width: 0;
  overflow: hidden;
}

.sidebar.collapsed .nav-item {
  justify-content: center;
  padding: 10px;
}

.sidebar-footer {
  padding: 12px;
  margin-top: auto;
  display: flex;
  background: transparent;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
  flex-shrink: 0;
}

.user-details {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #111827;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-status {
  font-size: 12px;
  color: #10b981;
  margin-top: 2px;
}

.collapse-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  border-radius: 6px;
  cursor: pointer;
  color: #6b7280;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
}

.collapse-btn:hover {
  background: #e5e6eb;
  color: #1d2129;
  transform: scale(1.1);
}

.collapse-btn:active {
  transform: scale(0.95);
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

/* 滚动条样式 */
.sidebar-nav::-webkit-scrollbar {
  width: 6px;
}

.sidebar-nav::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar-nav::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 3px;
}

.sidebar-nav::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}
</style>

