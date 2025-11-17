<template>
  <div class="app">
    <!-- 主对话框区域 -->
    <div class="dialog-container">
      <!-- 对话框消息区域 -->
      <div class="dialog-messages" ref="messagesContainerRef">
        <!-- 欢迎消息 -->
        <transition name="welcome-fade">
          <div v-if="messages.length === 0" class="welcome-message">
            <div class="welcome-icon">
              <div class="welcome-icon-inner">AI</div>
            </div>
            <h2>Agent 助手</h2>
            <p>请输入您的任务描述，我将为您生成执行计划并开始执行</p>
          </div>
        </transition>

        <!-- 消息列表 -->
        <template v-for="(msg, index) in messages" :key="`msg-${index}-${msg.timestamp?.getTime() || index}`">
          <!-- 任务列表（显示在AI消息上方） -->
          <TaskList 
            v-if="msg.role === 'assistant' && msg.tasks && Array.isArray(msg.tasks) && msg.tasks.length > 0" 
            :key="`tasklist-${index}-${msg.tasks?.length || 0}-${msg.timestamp?.getTime() || index}`"
            :tasks="msg.tasks"
            :animation-delay="index * 0.08"
          />

          <!-- 消息内容 -->
          <MessageItem 
            :message="msg"
            :animation-delay="index * 0.08"
          />
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
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import TaskList from './components/TaskList.vue'
import MessageItem from './components/MessageItem.vue'
import InputArea from './components/InputArea.vue'
import { useMessages } from './composables/useMessages.js'
import { useEventHandlers } from './composables/useEventHandlers.js'
import { useTaskExecution } from './composables/useTaskExecution.js'

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
    radial-gradient(circle at 20% 50%, rgba(16, 163, 127, 0.03) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(25, 195, 125, 0.03) 0%, transparent 50%),
    linear-gradient(to bottom, #ffffff 0%, #f7f7f8 100%);
  color: #353740;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  position: relative;
  overflow: hidden;
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
    radial-gradient(circle at 10% 20%, rgba(16, 163, 127, 0.02) 0%, transparent 40%),
    radial-gradient(circle at 90% 60%, rgba(25, 195, 125, 0.02) 0%, transparent 40%);
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
  padding: 32px 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-width: 768px;
  margin: 0 auto;
  width: 100%;
  scroll-behavior: smooth;
  position: relative;
  min-height: 0;
}

.dialog-messages > * {
  flex-shrink: 0;
}

.welcome-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  text-align: center;
  color: #8e8ea0;
}

.welcome-icon {
  width: 80px;
  height: 80px;
  border-radius: 20px;
  background: linear-gradient(135deg, #10a37f 0%, #19c37d 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 700;
  color: white;
  margin-bottom: 24px;
  position: relative;
  animation: welcomeIconFloat 3s ease-in-out infinite;
  box-shadow: 
    0 8px 24px rgba(16, 163, 127, 0.2),
    0 4px 8px rgba(16, 163, 127, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.welcome-icon::before {
  content: '';
  position: absolute;
  inset: -6px;
  border-radius: 26px;
  background: linear-gradient(135deg, rgba(16, 163, 127, 0.2), rgba(25, 195, 125, 0.15));
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
  margin: 0 0 12px 0;
  color: #202123;
  font-size: 32px;
  font-weight: 700;
  background: linear-gradient(135deg, #10a37f 0%, #19c37d 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.02em;
}

.welcome-message p {
  margin: 0;
  font-size: 16px;
  color: #565869;
  line-height: 1.6;
  max-width: 500px;
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
</style>
