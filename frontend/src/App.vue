<template>
  <div class="app" :class="{ 'electron-app': isElectron }">
    <!-- Electron 自定义标题栏 -->
    <TitleBar v-if="isElectron" />
    
    <!-- 主布局容器 -->
    <div class="app-layout">
      <!-- 左侧边栏 -->
      <Sidebar 
        :current-view="currentView"
        :show-debug-panel="showDebugPanel"
        @view-change="handleViewChange"
        @debug-toggle="showDebugPanel = !showDebugPanel"
      />

      <!-- 主内容区域 -->
      <div class="main-content">
        <!-- 顶部工具栏 -->
        <TopBar 
          :page-title="pageTitle"
          :breadcrumbs="breadcrumbs"
          @breadcrumb-click="handleBreadcrumbClick"
        />
        
        <!-- 内容区域 -->
        <div class="content-area">
          <!-- 对话视图 -->
          <transition name="view-transition" mode="out-in">
            <div v-if="currentView === 'chat'" key="chat" class="view-container chat-view">
            <!-- 调试面板 -->
            <transition name="debug-panel">
              <div v-if="showDebugPanel" class="debug-panel">
                <div class="debug-panel-header">
                  <h3>调试面板</h3>
                  <button class="debug-close" @click="showDebugPanel = false">×</button>
                </div>
                <div class="debug-panel-content">
                  <div class="debug-field">
                    <label>用户ID (userId)</label>
                    <div class="debug-input-group">
                      <input
                        v-model="debugUserId"
                        type="text"
                        placeholder="输入用户ID"
                        class="debug-input"
                        @blur="handleUserIdChange"
                      />
                      <button class="debug-btn" @click="generateNewUserId">生成新ID</button>
                    </div>
                    <div class="debug-value">当前: {{ session.userId?.value || debugUserId }}</div>
                  </div>
                  <div class="debug-field">
                    <label>会话ID (sessionId)</label>
                    <div class="debug-input-group">
                      <input
                        v-model="debugSessionId"
                        type="text"
                        placeholder="输入会话ID"
                        class="debug-input"
                        @blur="handleSessionIdChange"
                      />
                      <button class="debug-btn" @click="generateNewSessionId">生成新ID</button>
                    </div>
                    <div class="debug-value">当前: {{ session.sessionId?.value || debugSessionId }}</div>
                  </div>
                  <div class="debug-actions">
                    <button class="debug-btn-primary" @click="applyDebugSettings">应用设置</button>
                    <button class="debug-btn-secondary" @click="resetDebugSettings">重置</button>
                  </div>
                </div>
              </div>
            </transition>

            <!-- 对话容器 -->
            <div class="dialog-container">
      <!-- 对话框消息区域 -->
      <div class="dialog-messages" ref="messagesContainerRef">
        <!-- 欢迎消息 -->
        <transition name="welcome-fade">
          <div v-if="messages.length === 0 && !isPlanning" class="welcome-message">
            <div class="welcome-icon">
              <div class="welcome-icon-inner">AI</div>
            </div>
          <h2>Agent 助手</h2>
          <p>请输入您的任务描述，我将为您生成执行计划并开始执行</p>
        </div>
        </transition>

        <!-- 初始加载动画（3个点轮流跳动） -->
        <transition name="loading-fade">
          <div v-if="messages.length === 0 && isPlanning" class="loading-message">
            <div class="loading-dots">
              <span class="loading-dot"></span>
              <span class="loading-dot"></span>
              <span class="loading-dot"></span>
            </div>
            <p class="loading-text">正在思考中...</p>
          </div>
        </transition>

        <!-- 消息列表 -->
        <template v-for="(msg, index) in messages" :key="`msg-${index}-${msg.timestamp?.getTime() || index}`">
          <!-- 任务列表（先显示任务列表） -->
          <TaskList 
            v-if="msg.role === 'assistant' && msg.tasks && Array.isArray(msg.tasks) && msg.tasks.length > 0" 
            :key="`tasklist-${index}-${msg.tasks?.length || 0}-${msg.tasks?.map(t => t.id).join('-') || index}-${msg.timestamp?.getTime() || index}`"
            :tasks="msg.tasks"
            :animation-delay="index * 0.08"
          />

          <!-- 消息内容（在任务列表之后显示，如果有任务列表则延迟显示） -->
          <MessageItem 
            :message="msg"
            :animation-delay="msg.role === 'assistant' && msg.tasks && Array.isArray(msg.tasks) && msg.tasks.length > 0 
              ? index * 0.08 + 0.3 
              : index * 0.08"
          />

          <!-- 用户消息后的等待动画（三个点跳动） -->
          <transition name="typing-fade">
            <div 
              v-if="msg.role === 'user' && index === messages.length - 1 && (isExecuting || isPlanning)"
              class="typing-indicator"
            >
              <div class="typing-avatar">AI</div>
              <div class="typing-content">
                <div class="typing-dots-container">
                  <span class="typing-dot-item"></span>
                  <span class="typing-dot-item"></span>
                  <span class="typing-dot-item"></span>
            </div>
              </div>
            </div>
          </transition>

          <!-- execution_failed 错误框（显示在消息下方） -->
          <transition name="error-slide">
            <div 
              v-if="msg.executionError" 
              class="execution-error-box"
              :style="{ animationDelay: `${index * 0.08 + 0.4}s` }"
            >
              <div class="execution-error-icon">⚠️</div>
              <div class="execution-error-content">
                <div class="execution-error-title">执行异常</div>
                <div class="execution-error-text">{{ msg.executionError }}</div>
            </div>
              </div>
          </transition>
        </template>

        <!-- 错误消息 -->
        <transition name="error-slide">
        <div v-if="error" class="message error">
          <div class="message-avatar">⚠️</div>
          <div class="message-content">
            <div class="message-text error-text">{{ error }}</div>
          </div>
        </div>
        </transition>
      </div>

      <!-- 输入区域 -->
      <InputArea
        :form="form"
        :is-executing="isExecuting"
        :is-planning="isPlanning"
        :can-send="canSend"
        @send="executeTask"
      />
            </div>
            </div>

            <!-- RAG知识库管理视图 -->
            <div v-else-if="currentView === 'rag'" key="rag" class="view-container rag-view">
              <RagManagement />
            </div>

            <!-- 文档库视图 -->
            <div v-else-if="currentView === 'docs'" key="docs" class="view-container docs-view">
              <DocumentLibrary />
            </div>
          </transition>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, watch } from 'vue'
import TaskList from './components/TaskList.vue'
import MessageItem from './components/MessageItem.vue'
import InputArea from './components/InputArea.vue'
import RagManagement from './components/RagManagement.vue'
import DocumentLibrary from './components/DocumentLibrary.vue'
import TitleBar from './components/TitleBar.vue'
import Sidebar from './components/Sidebar.vue'
import TopBar from './components/TopBar.vue'
import { useMessages } from './composables/useMessages.js'
import { useEventHandlers } from './composables/useEventHandlers.js'
import { useTaskExecution } from './composables/useTaskExecution.js'
import { useSession } from './composables/useSession.js'

// 检测是否在 Electron 环境中
const isElectron = ref(typeof window !== 'undefined' && window.electronAPI !== undefined)

// 当前视图
const currentView = ref('chat')

// 页面标题和面包屑
const pageTitle = computed(() => {
  const titles = {
    chat: '对话',
    rag: 'RAG知识库',
    docs: '文档库'
  }
  return titles[currentView.value] || 'Agent 系统'
})

const breadcrumbs = computed(() => {
  return ['首页', pageTitle.value]
})

// 处理视图切换
const handleViewChange = (view) => {
  currentView.value = view
}

// 处理面包屑点击
const handleBreadcrumbClick = ({ crumb, index }) => {
  if (index === 0) {
    // 点击首页，切换到对话视图
    currentView.value = 'chat'
  }
  // 其他情况可以根据需要扩展
}

// 调试面板
const showDebugPanel = ref(false)
const session = useSession()
const debugUserId = ref('')
const debugSessionId = ref('')

// 初始化调试面板的值
watch(() => session.userId?.value, (newVal) => {
  if (newVal && typeof newVal === 'string') {
    debugUserId.value = newVal
  } else if (!debugUserId.value) {
    debugUserId.value = ''
  }
}, { immediate: true })

watch(() => session.sessionId?.value, (newVal) => {
  if (newVal && typeof newVal === 'string') {
    debugSessionId.value = newVal
  } else if (!debugSessionId.value) {
    debugSessionId.value = ''
  }
}, { immediate: true })

// 处理 userId 变化
const handleUserIdChange = () => {
  const value = typeof debugUserId.value === 'string' ? debugUserId.value : String(debugUserId.value || '')
  if (value && value.trim()) {
    session.setUserId(value.trim())
  } else {
    debugUserId.value = session.userId?.value || ''
  }
}

// 处理 sessionId 变化
const handleSessionIdChange = () => {
  const value = typeof debugSessionId.value === 'string' ? debugSessionId.value : String(debugSessionId.value || '')
  if (value && value.trim()) {
    session.setSessionId(value.trim())
  } else {
    debugSessionId.value = session.sessionId?.value || ''
  }
}

// 生成新的 userId
const generateNewUserId = () => {
  const newUserId = 'user_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
  debugUserId.value = newUserId
  session.setUserId(newUserId)
}

// 生成新的 sessionId
const generateNewSessionId = () => {
  const newSessionId = session.newSession()
  debugSessionId.value = newSessionId
}

// 应用调试设置
const applyDebugSettings = () => {
  handleUserIdChange()
  handleSessionIdChange()
  showDebugPanel.value = false
}

// 重置调试设置
const resetDebugSettings = () => {
  debugUserId.value = session.userId?.value || ''
  debugSessionId.value = session.sessionId?.value || ''
}

// 表单数据
const form = reactive({
  orchestratorId: 1,
  message: ''
})

// 消息管理
const messagesManager = useMessages()
const { messages, error } = messagesManager

// 事件处理
const eventHandlers = useEventHandlers(messagesManager)
const { isExecuting, isPlanning } = eventHandlers

// 任务执行
const messagesContainerRef = ref(null)
const taskExecution = useTaskExecution(messagesManager, eventHandlers, form, messagesContainerRef, session)
const { executeTask } = taskExecution


// 计算属性
const canSend = computed(() => {
  return form.orchestratorId && form.message.trim().length > 0
})
</script>

<style scoped>
.app {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #ffffff;
  color: #111827;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  position: relative;
  overflow: hidden;
}

/* Electron 应用样式 - 无边框窗口 */
.app.electron-app {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}

/* 主布局容器 */
.app-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

/* 主内容区域 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #fafbfc;
}

/* 内容区域 */
.content-area {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* 视图容器 */
.view-container {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: 100%;
}

/* 页面切换动画 - Notion/飞书风格 */
.view-transition-enter-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.view-transition-leave-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 1, 1);
}

.view-transition-enter-from {
  opacity: 0;
  transform: translateX(20px) scale(0.98);
}

.view-transition-leave-to {
  opacity: 0;
  transform: translateX(-20px) scale(0.98);
}

.tab-icon {
  font-size: 16px;
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-tab.debug-tab.active {
  background-color: #fef3c7;
  color: #d97706;
}

.tab-text {
  font-size: 14px;
}


/* 视图特定样式 */
.chat-view,
.rag-view,
.docs-view {
  width: 100%;
  height: 100%;
}

.app::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(to right, transparent, #e5e5e6, transparent);
  z-index: 1;
}

.app::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 10% 20%, rgba(33, 150, 243, 0.02) 0%, transparent 40%),
    radial-gradient(circle at 90% 60%, rgba(66, 165, 245, 0.02) 0%, transparent 40%);
  pointer-events: none;
  z-index: 0;
}

/* 对话框容器 */
.dialog-container {
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: hidden;
  background: transparent;
  position: relative;
  z-index: 1;
}

.dialog-messages {
  flex: 1;
  overflow-y: auto;
  padding: 48px 120px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-width: 1800px;
  margin: 0 auto;
  width: 100%;
  scroll-behavior: smooth;
  position: relative;
  min-height: 0;
  align-items: flex-start; /* 确保消息左对齐 */
}

.dialog-messages > .message {
  width: 100%;
  display: flex;
}

.dialog-messages > * {
  flex-shrink: 0;
}

/* 当任务列表后面跟着消息时，调整消息气泡的样式，形成一体效果 */
.dialog-messages > .message-task-box + .message.assistant {
  margin-top: 0; /* 移除顶部间距 */
}

.dialog-messages > .message-task-box + .message.assistant .message-card {
  border-radius: 0 0 14px 14px; /* 只有底部圆角，顶部直角，与任务列表连接 */
  border-top: none; /* 移除顶部边框，与任务列表无缝连接 */
  box-shadow: 
    0 4px 16px rgba(0, 0, 0, 0.08),
    0 2px 4px rgba(0, 0, 0, 0.04),
    inset 0 1px 0 rgba(255, 255, 255, 0.9),
    inset 0 -1px 0 rgba(0, 0, 0, 0.02); /* 统一阴影效果，与任务列表一致 */
  margin-top: 0;
  background: linear-gradient(135deg, #ffffff 0%, #fafafa 50%, #ffffff 100%); /* 统一背景渐变 */
}

.dialog-messages > .message-task-box + .message.assistant .message-text {
  border-radius: 0 0 14px 14px; /* 文本区域也使用相同的圆角 */
  background: #ffffff; /* 确保文本区域背景为纯白 */
}

/* 添加连接过渡效果 - 微妙的连接线 */
.dialog-messages > .message-task-box + .message.assistant .message-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    rgba(0, 0, 0, 0.04) 20%, 
    rgba(0, 0, 0, 0.06) 50%, 
    rgba(0, 0, 0, 0.04) 80%, 
    transparent 100%);
  pointer-events: none;
  z-index: 1;
}

.welcome-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  text-align: center;
  color: #8e8ea0;
  width: 100%; /* 确保占满宽度 */
  align-self: center; /* 覆盖父容器的 align-items: flex-start */
  margin: 0 auto; /* 水平居中 */
}

.welcome-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  background: #2563eb;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 600;
  color: white;
  margin-bottom: 24px;
}

.welcome-icon-inner {
  position: relative;
  z-index: 1;
}

.welcome-message h2 {
  margin: 0 0 12px 0;
  color: #111827;
  font-size: 32px;
  font-weight: 600;
  letter-spacing: -0.025em;
}

.welcome-message p {
  margin: 0;
  font-size: 16px;
  color: #6b7280;
  line-height: 1.6;
  max-width: 600px;
}

/* 错误消息样式 */
.message.error {
  display: flex;
  gap: 12px;
  align-self: flex-start;
  max-width: 85%;
}

.message.error .message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 4px;
  background: linear-gradient(135deg, #f7f7f8 0%, #e5e5e6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
  color: #565869;
  box-shadow: 
    0 2px 4px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.message.error .message-content {
  flex: 1;
  min-width: 0;
}

.message.error .message-text {
  background: #fef2f2;
  color: #ef4444;
  border: 1px solid #fecaca;
  padding: 18px 22px;
  border-radius: 12px;
  line-height: 1.75;
  word-wrap: break-word;
  font-size: 15px;
}

/* execution_failed 错误框样式 */
.execution-error-box {
  display: flex;
  gap: 12px;
  align-self: flex-start;
  max-width: 85%;
  margin-top: 8px;
  margin-left: 52px; /* 与消息内容对齐 */
}

.execution-error-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
  box-shadow: 
    0 2px 8px rgba(239, 68, 68, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  border: 1px solid #fecaca;
}

.execution-error-content {
  flex: 1;
  min-width: 0;
}

.execution-error-title {
  font-size: 13px;
  font-weight: 600;
  color: #dc2626;
  margin-bottom: 6px;
  letter-spacing: 0.01em;
}

.execution-error-text {
  background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
  color: #991b1b;
  border: 2px solid #fecaca;
  padding: 16px 20px;
  border-radius: 12px;
  line-height: 1.6;
  word-wrap: break-word;
  font-size: 14px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  box-shadow: 
    0 4px 12px rgba(239, 68, 68, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
}

/* 滚动条样式 */
.dialog-messages::-webkit-scrollbar {
  width: 8px;
}

.dialog-messages::-webkit-scrollbar-track {
  background: #ffffff;
}

.dialog-messages::-webkit-scrollbar-thumb {
  background: #d1d1d6;
  border-radius: 4px;
}

.dialog-messages::-webkit-scrollbar-thumb:hover {
  background: #b5b5b9;
}

/* 动画 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}



@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.7;
  }
  30% {
    transform: translateY(-10px);
    opacity: 1;
  }
}

/* Vue Transition 动画 */
.planning-fade-enter-active,
.planning-fade-leave-active {
  transition: opacity 0.2s ease;
}

.planning-fade-enter-from {
  opacity: 0;
}

.planning-fade-leave-to {
  opacity: 0;
}

.error-slide-enter-active {
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.error-slide-leave-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 1, 1);
}

.error-slide-enter-from {
  opacity: 0;
  transform: translateX(-20px) scale(0.95);
}

.error-slide-leave-to {
  opacity: 0;
  transform: translateX(20px) scale(0.98);
}

.welcome-fade-enter-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.welcome-fade-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

.welcome-fade-leave-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 1, 1);
}

.welcome-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px) scale(0.98);
}

/* 加载动画样式 */
.loading-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  text-align: center;
  color: #8e8ea0;
}

.loading-dots {
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.loading-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #2563eb;
  animation: dotFade 1.2s ease-in-out infinite;
  opacity: 0.4;
}

.loading-dot:nth-child(1) {
  animation-delay: 0s;
}

.loading-dot:nth-child(2) {
  animation-delay: 0.2s;
}

.loading-dot:nth-child(3) {
  animation-delay: 0.4s;
}

.loading-text {
  margin: 0;
  font-size: 14px;
  color: #565869;
  font-weight: 500;
}

@keyframes dotFade {
  0%, 100% {
    opacity: 0.4;
  }
  50% {
    opacity: 1;
  }
}

.loading-fade-enter-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.loading-fade-leave-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 1, 1);
}

.loading-fade-enter-from {
  opacity: 0;
  transform: scale(0.9);
}

.loading-fade-leave-to {
  opacity: 0;
  transform: scale(0.95);
}

/* 用户消息后的等待动画（三个点跳动） */
.typing-indicator {
  display: flex;
  gap: 12px;
  align-self: flex-start;
  max-width: 85%;
  margin-top: 8px;
}

.typing-avatar {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #2196f3 0%, #42a5f5 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  color: white;
  flex-shrink: 0;
  box-shadow: 
    0 4px 12px rgba(33, 150, 243, 0.25),
    0 2px 4px rgba(33, 150, 243, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.typing-content {
  flex: 1;
  min-width: 0;
}

.typing-dots-container {
  display: flex;
  gap: 6px;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border: 1.5px solid rgba(33, 150, 243, 0.15);
  border-radius: 12px;
  box-shadow: 
    0 4px 16px rgba(33, 150, 243, 0.08),
    0 2px 4px rgba(33, 150, 243, 0.04),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.typing-dot-item {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #2563eb;
  animation: typingDotFade 1.2s ease-in-out infinite;
  opacity: 0.4;
}

.typing-dot-item:nth-child(1) {
  animation-delay: 0s;
}

.typing-dot-item:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-dot-item:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typingDotFade {
  0%, 100% {
    opacity: 0.4;
  }
  50% {
    opacity: 1;
  }
}

.typing-fade-enter-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.typing-fade-leave-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 1, 1);
}

.typing-fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.typing-fade-leave-to {
  opacity: 0;
  transform: translateY(-5px);
}

/* 调试面板样式 */
.debug-panel {
  position: fixed;
  top: 72px;
  right: 20px;
  width: 420px;
  max-height: calc(100vh - 100px);
  background: #ffffff;
  border: 2px solid rgba(255, 152, 0, 0.2);
  border-radius: 16px;
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.12),
    0 4px 16px rgba(255, 152, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  z-index: 200;
  overflow: hidden;
  backdrop-filter: blur(10px);
  transform-origin: top right;
}

/* 调试面板动画 */
.debug-panel-enter-active {
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.debug-panel-leave-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 1, 1);
}

.debug-panel-enter-from {
  opacity: 0;
  transform: translateX(100%) scale(0.9);
}

.debug-panel-leave-to {
  opacity: 0;
  transform: translateX(100%) scale(0.95);
}

.debug-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  background: linear-gradient(135deg, rgba(255, 152, 0, 0.1) 0%, rgba(255, 152, 0, 0.05) 100%);
  border-bottom: 1px solid rgba(255, 152, 0, 0.2);
}

.debug-panel-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #ff9800;
  letter-spacing: -0.01em;
}

.debug-close {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  color: #565869;
  font-size: 24px;
  font-weight: 300;
  cursor: pointer;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  line-height: 1;
}

.debug-close:hover {
  background: rgba(255, 152, 0, 0.1);
  color: #ff9800;
}

.debug-panel-content {
  padding: 24px;
  max-height: calc(100vh - 200px);
  overflow-y: auto;
}

.debug-field {
  margin-bottom: 24px;
}

.debug-field:last-of-type {
  margin-bottom: 20px;
}

.debug-field label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #565869;
  margin-bottom: 8px;
  letter-spacing: 0.01em;
}

.debug-input-group {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.debug-input {
  flex: 1;
  padding: 10px 14px;
  border: 2px solid rgba(0, 0, 0, 0.1);
  border-radius: 10px;
  font-size: 14px;
  font-family: 'Monaco', 'Menlo', 'Courier New', monospace;
  color: #353740;
  background: #fafafa;
  transition: all 0.2s ease;
}

.debug-input:focus {
  outline: none;
  border-color: #ff9800;
  background: #ffffff;
  box-shadow: 0 0 0 3px rgba(255, 152, 0, 0.1);
}

.debug-value {
  font-size: 12px;
  color: #8e8ea0;
  font-family: 'Monaco', 'Menlo', 'Courier New', monospace;
  word-break: break-all;
  padding: 6px 10px;
  background: rgba(0, 0, 0, 0.02);
  border-radius: 6px;
  margin-top: 4px;
}

.debug-btn {
  padding: 10px 16px;
  background: linear-gradient(135deg, rgba(255, 152, 0, 0.1) 0%, rgba(255, 152, 0, 0.05) 100%);
  border: 2px solid rgba(255, 152, 0, 0.2);
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  color: #ff9800;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.debug-btn:hover {
  background: linear-gradient(135deg, rgba(255, 152, 0, 0.15) 0%, rgba(255, 152, 0, 0.1) 100%);
  border-color: rgba(255, 152, 0, 0.3);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(255, 152, 0, 0.2);
}

.debug-btn:active {
    transform: translateY(0);
  }

.debug-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
}

.debug-btn-primary {
  flex: 1;
  padding: 12px 24px;
  background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%);
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  color: white;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(255, 152, 0, 0.3);
}

.debug-btn-primary:hover {
  background: linear-gradient(135deg, #f57c00 0%, #e65100 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.4);
}

.debug-btn-primary:active {
    transform: translateY(0);
}

.debug-btn-secondary {
  flex: 1;
  padding: 12px 24px;
  background: transparent;
  border: 2px solid rgba(0, 0, 0, 0.1);
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  color: #565869;
  cursor: pointer;
  transition: all 0.2s ease;
}

.debug-btn-secondary:hover {
  background: rgba(0, 0, 0, 0.05);
  border-color: rgba(0, 0, 0, 0.15);
}

/* 调试面板过渡动画 */
.debug-panel-enter-active {
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.debug-panel-leave-active {
  transition: all 0.2s ease-in;
}

.debug-panel-enter-from {
    opacity: 0;
  transform: translateX(20px) scale(0.95);
}

.debug-panel-leave-to {
  opacity: 0;
  transform: translateX(20px) scale(0.95);
}
</style>
