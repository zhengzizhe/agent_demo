<template>
  <div class="app">
    <!-- 导航栏 -->
    <nav class="app-nav">
      <div class="nav-content">
        <div class="nav-logo">
          <span class="logo-icon">AI</span>
          <span class="logo-text">Agent 系统</span>
        </div>
        <div class="nav-tabs">
          <button
            class="nav-tab"
            :class="{ active: currentView === 'chat' }"
            @click="currentView = 'chat'"
          >
            <span class="tab-icon">💬</span>
            <span class="tab-text">对话</span>
          </button>
          <button
            class="nav-tab"
            :class="{ active: currentView === 'rag' }"
            @click="currentView = 'rag'"
          >
            <span class="tab-icon">📚</span>
            <span class="tab-text">RAG知识库</span>
          </button>
        </div>
      </div>
    </nav>

    <!-- 对话视图 -->
    <div v-if="currentView === 'chat'" class="dialog-container">
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

    <!-- RAG知识库管理视图 -->
    <div v-if="currentView === 'rag'" class="rag-container">
      <RagManagement />
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import TaskList from './components/TaskList.vue'
import MessageItem from './components/MessageItem.vue'
import InputArea from './components/InputArea.vue'
import RagManagement from './components/RagManagement.vue'
import { useMessages } from './composables/useMessages.js'
import { useEventHandlers } from './composables/useEventHandlers.js'
import { useTaskExecution } from './composables/useTaskExecution.js'

// 当前视图
const currentView = ref('chat')

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
const taskExecution = useTaskExecution(messagesManager, eventHandlers, form, messagesContainerRef)
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
  background: 
    radial-gradient(circle at 20% 50%, rgba(33, 150, 243, 0.03) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(66, 165, 245, 0.03) 0%, transparent 50%),
    linear-gradient(to bottom, #ffffff 0%, #f7f7f8 100%);
  color: #353740;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  position: relative;
  overflow: hidden;
}

/* 导航栏样式 */
.app-nav {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(33, 150, 243, 0.1);
  box-shadow: 0 2px 8px rgba(33, 150, 243, 0.05);
}

.nav-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 72px;
}

.nav-logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: linear-gradient(135deg, #2196f3 0%, #42a5f5 50%, #1976d2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 800;
  color: white;
  box-shadow: 
    0 6px 20px rgba(33, 150, 243, 0.3),
    0 3px 8px rgba(33, 150, 243, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  border: 2px solid rgba(255, 255, 255, 0.2);
  position: relative;
  overflow: visible;
  transition: all 0.3s ease;
}

.logo-icon::before {
  content: '';
  position: absolute;
  inset: -4px;
  border-radius: 18px;
  background: linear-gradient(135deg, #2196f3, #42a5f5);
  opacity: 0.2;
  filter: blur(10px);
  z-index: -1;
}

.logo-icon:hover {
  transform: scale(1.05) rotate(5deg);
  box-shadow: 
    0 8px 24px rgba(33, 150, 243, 0.4),
    0 4px 10px rgba(33, 150, 243, 0.25);
}

.logo-text {
  font-size: 22px;
  font-weight: 700;
  color: #202123;
  letter-spacing: -0.02em;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  background: linear-gradient(135deg, #2196f3, #42a5f5);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav-tabs {
  display: flex;
  gap: 8px;
}

.nav-tab {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 28px;
  font-size: 15px;
  font-weight: 600;
  color: #565869;
  background: transparent;
  border: 2px solid transparent;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.nav-tab::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(33, 150, 243, 0.1), rgba(66, 165, 245, 0.1));
  opacity: 0;
  transition: opacity 0.3s;
  border-radius: 12px;
}

.nav-tab:hover {
  color: #2196f3;
  transform: translateY(-2px);
}

.nav-tab:hover::before {
  opacity: 1;
}

.nav-tab.active {
  background: linear-gradient(135deg, rgba(33, 150, 243, 0.15) 0%, rgba(66, 165, 245, 0.12) 100%);
  color: #2196f3;
  font-weight: 700;
  border-color: rgba(33, 150, 243, 0.2);
  box-shadow: 
    0 4px 12px rgba(33, 150, 243, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
}

.nav-tab.active::before {
  opacity: 1;
}

.tab-icon {
  font-size: 18px;
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.1));
  transition: transform 0.3s ease;
}

.nav-tab:hover .tab-icon {
  transform: scale(1.1) rotate(5deg);
}

.nav-tab.active .tab-icon {
  transform: scale(1.15);
  filter: drop-shadow(0 2px 4px rgba(33, 150, 243, 0.3));
}

.tab-text {
  font-size: 15px;
  letter-spacing: -0.01em;
}


.rag-container {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
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
  width: 100px;
  height: 100px;
  border-radius: 24px;
  background: linear-gradient(135deg, #2196f3 0%, #42a5f5 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  font-weight: 700;
  color: white;
  margin-bottom: 32px;
  position: relative;
  animation: welcomeIconFloat 3s ease-in-out infinite;
  box-shadow: 
    0 8px 24px rgba(33, 150, 243, 0.2),
    0 4px 8px rgba(33, 150, 243, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.welcome-icon::before {
  content: '';
  position: absolute;
  inset: -6px;
  border-radius: 26px;
  background: linear-gradient(135deg, rgba(33, 150, 243, 0.2), rgba(66, 165, 245, 0.15));
  opacity: 0;
  animation: welcomeIconGlow 3s ease-in-out infinite;
  z-index: -1;
  filter: blur(8px);
}

.welcome-icon-inner {
  animation: welcomeIconPulse 2s ease-in-out infinite;
  position: relative;
  z-index: 1;
}

.welcome-message h2 {
  margin: 0 0 16px 0;
  color: #202123;
  font-size: 40px;
  font-weight: 700;
  background: linear-gradient(135deg, #2196f3 0%, #42a5f5 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.02em;
}

.welcome-message p {
  margin: 0;
  font-size: 18px;
  color: #565869;
  line-height: 1.7;
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
  animation: messageSlideIn 0.5s cubic-bezier(0.16, 1, 0.3, 1) both;
  will-change: transform, opacity;
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
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}


@keyframes welcomeIconFloat {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-10px);
  }
}

@keyframes welcomeIconGlow {
  0%, 100% {
    opacity: 0;
  }
  50% {
    opacity: 1;
  }
}

@keyframes welcomeIconPulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
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
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.planning-fade-enter-from {
  opacity: 0;
  transform: translateY(-10px) scale(0.95);
}

.planning-fade-leave-to {
  opacity: 0;
  transform: translateY(-5px) scale(0.98);
}

.error-slide-enter-active {
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.error-slide-leave-active {
  transition: all 0.3s ease-in;
}

.error-slide-enter-from {
  opacity: 0;
  transform: translateX(-20px) scale(0.9);
}

.error-slide-leave-to {
  opacity: 0;
  transform: translateX(20px) scale(0.9);
}

.welcome-fade-enter-active {
  transition: all 0.8s cubic-bezier(0.16, 1, 0.3, 1);
}

.welcome-fade-enter-from {
  opacity: 0;
  transform: translateY(30px) scale(0.9);
}

.welcome-fade-enter-to {
  opacity: 1;
  transform: translateY(0) scale(1);
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
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2196f3 0%, #42a5f5 100%);
  animation: dotBounce 1.4s ease-in-out infinite;
  box-shadow: 
    0 2px 8px rgba(33, 150, 243, 0.3),
    0 1px 3px rgba(33, 150, 243, 0.2);
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
  animation: textFade 2s ease-in-out infinite;
}

@keyframes dotBounce {
  0%, 80%, 100% {
    transform: translateY(0) scale(1);
    opacity: 0.5;
  }
  40% {
    transform: translateY(-20px) scale(1.2);
    opacity: 1;
  }
}

@keyframes textFade {
  0%, 100% {
    opacity: 0.6;
  }
  50% {
    opacity: 1;
  }
}

.loading-fade-enter-active {
  transition: all 0.5s cubic-bezier(0.16, 1, 0.3, 1);
}

.loading-fade-leave-active {
  transition: all 0.3s ease-in;
}

.loading-fade-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.9);
}

.loading-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px) scale(0.95);
}

/* 用户消息后的等待动画（三个点跳动） */
.typing-indicator {
  display: flex;
  gap: 12px;
  align-self: flex-start;
  max-width: 85%;
  animation: typingSlideIn 0.4s cubic-bezier(0.16, 1, 0.3, 1) both;
  will-change: transform, opacity;
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
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2196f3 0%, #42a5f5 100%);
  animation: typingDotBounce 1.4s ease-in-out infinite;
  box-shadow: 
    0 2px 6px rgba(33, 150, 243, 0.3),
    0 1px 2px rgba(33, 150, 243, 0.2);
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

@keyframes typingSlideIn {
  from {
    opacity: 0;
    transform: translateY(10px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes typingDotBounce {
  0%, 80%, 100% {
    transform: translateY(0) scale(1);
    opacity: 0.5;
  }
  40% {
    transform: translateY(-12px) scale(1.15);
    opacity: 1;
  }
}

.typing-fade-enter-active {
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.typing-fade-leave-active {
  transition: all 0.3s ease-in;
}

.typing-fade-enter-from {
  opacity: 0;
  transform: translateY(10px) scale(0.95);
}

.typing-fade-leave-to {
  opacity: 0;
  transform: translateY(-5px) scale(0.98);
}
</style>
