<template>
  <div class="input-container" :class="{ centered: centered }">
    <!-- 生成计划提示框 -->
    <transition name="planning-fade">
      <div v-if="isPlanning" class="planning-indicator">
        <span class="planning-text">
          <span class="planning-dot"></span>
          Generating plan...
        </span>
      </div>
    </transition>
    
    <div class="input-wrapper">
      <div class="input-content">
        <!-- 顶部：@ 添加背景信息 -->
        <div class="input-header">
          <span class="at-symbol">@</span>
          <span class="context-hint">添加背景信息</span>
        </div>
        
        <!-- 中间：输入区域 -->
        <div class="input-main">
          <input 
            type="number" 
            v-model.number="form.orchestratorId"
            placeholder="ID"
            class="orchestrator-input"
            :disabled="isExecuting"
          />
          <textarea 
            v-model="form.message"
            placeholder="询问、搜索或制作任何内容..."
            class="message-input"
            :disabled="isExecuting"
            @keydown.enter.exact.prevent="handleEnter"
            @keydown.shift.enter.exact="handleShiftEnter"
            rows="1"
            ref="textareaRef"
          ></textarea>
        </div>
        
      </div>
      
      <!-- 右侧：发送按钮 -->
      <button 
        class="send-button"
        :disabled="!canSend || isExecuting"
        @click="$emit('send')"
        title="发送"
      >
        <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
          <path d="M9 3L9 15M9 3L3 9M9 3L15 9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'

const props = defineProps({
  form: {
    type: Object,
    required: true
  },
  isExecuting: {
    type: Boolean,
    default: false
  },
  isPlanning: {
    type: Boolean,
    default: false
  },
  canSend: {
    type: Boolean,
    default: false
  },
  centered: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['send'])

const textareaRef = ref(null)

const handleEnter = () => {
  if (props.canSend && !props.isExecuting) {
    emit('send')
  }
}

const handleShiftEnter = () => {
  // Shift+Enter 换行，不需要特殊处理
}

// 自动调整 textarea 高度
watch(() => props.form.message, () => {
  nextTick(() => {
    if (textareaRef.value) {
      textareaRef.value.style.height = 'auto'
      textareaRef.value.style.height = textareaRef.value.scrollHeight + 'px'
    }
  })
})
</script>

<style scoped>
/* 输入区域 */
.input-container {
  border-top: 1px solid #e5e7eb;
  background: var(--theme-background, #ffffff);
  padding: 20px 32px;
  position: relative;
  z-index: 10;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 居中显示（初始状态） */
.input-container.centered {
  position: static;
  border-top: none;
  background: transparent;
  padding: 0;
  width: 100%;
  max-width: 100%;
  z-index: 2;
  display: flex;
  justify-content: center;
}

.input-container.centered .input-wrapper {
  margin: 0;
  width: 100%;
  max-width: 100%;
}

/* 生成计划提示框 */
.planning-indicator {
  max-width: 1200px;
  margin: 0 auto 16px auto;
  display: flex;
  justify-content: center;
  align-items: center;
  animation: fadeInDown 0.3s ease-out;
}

.planning-text {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: color-mix(in srgb, var(--theme-accent, #165dff) 10%, transparent);
  border: 1px solid color-mix(in srgb, var(--theme-accent, #165dff) 20%, transparent);
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  color: var(--theme-accent, #165dff);
}

.planning-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  background: var(--theme-accent, #165dff);
  border-radius: 50%;
  animation: planningDot 1.5s ease-in-out infinite;
  flex-shrink: 0;
}

.input-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(to right, transparent, rgba(0, 0, 0, 0.08), transparent);
}

.input-wrapper {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  max-width: 800px;
  margin: 0 auto;
  background: var(--theme-background, #ffffff);
  border: 2px solid var(--theme-accent, #165dff);
  border-radius: 16px;
  padding: 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px color-mix(in srgb, var(--theme-accent, #165dff) 20%, transparent);
}

/* 居中状态时更宽 */
.input-container.centered .input-wrapper {
  max-width: 900px;
  width: 100%;
}

.input-wrapper:focus-within {
  background: var(--theme-background, #ffffff);
  border-color: var(--theme-accent, #165dff);
  box-shadow: 
    0 4px 12px color-mix(in srgb, var(--theme-accent, #165dff) 25%, transparent),
    0 0 0 3px color-mix(in srgb, var(--theme-accent, #165dff) 15%, transparent);
}

.input-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6b7280;
  padding-bottom: 4px;
}

.at-symbol {
  color: var(--theme-accent, #165dff);
  font-weight: 600;
}

.context-hint {
  color: #9ca3af;
}

.input-main {
  display: flex;
  gap: 8px;
  align-items: flex-start;
  min-height: 40px;
}


.orchestrator-input {
  width: 60px;
  padding: 8px 10px;
  background: transparent;
  border: none;
  border-radius: 8px;
  color: #111827;
  font-size: 13px;
  font-weight: 500;
  flex-shrink: 0;
}

.orchestrator-input:focus {
  outline: none;
  background: rgba(0, 0, 0, 0.04);
}

.message-input {
  flex: 1;
  padding: 8px 0;
  background: transparent;
  border: none;
  border-radius: 0;
  color: #111827;
  font-size: 15px;
  font-family: inherit;
  resize: none;
  max-height: 200px;
  overflow-y: auto;
  line-height: 1.6;
  min-height: 24px;
}

.message-input:focus {
  outline: none;
}

.message-input::placeholder {
  color: #9ca3af;
}

.message-input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.send-button {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--theme-accent, #165dff);
  color: white;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
  box-shadow: 0 2px 4px rgba(37, 99, 235, 0.2);
}

.send-button:hover:not(:disabled) {
  background-color: var(--theme-accent, #165dff);
  opacity: 0.9;
  transform: scale(1.05);
  box-shadow: 0 4px 8px rgba(37, 99, 235, 0.3);
}

.send-button svg {
  width: 18px;
  height: 18px;
}

.send-button:disabled {
  background-color: #d1d5db;
  cursor: not-allowed;
  opacity: 0.6;
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes planningBlink {
  0%, 100% {
    opacity: 0.6;
  }
  50% {
    opacity: 1;
  }
}

@keyframes planningDot {
  0%, 100% {
    opacity: 0.3;
    transform: scale(0.8);
  }
  50% {
    opacity: 1;
    transform: scale(1.2);
  }
}

@keyframes inputFocusPulse {
  0%, 100% {
    box-shadow: 
      0 6px 20px rgba(66, 165, 245, 0.2),
      0 2px 6px rgba(66, 165, 245, 0.15);
  }
  50% {
    box-shadow: 
      0 8px 24px rgba(66, 165, 245, 0.25),
      0 3px 8px rgba(66, 165, 245, 0.2);
  }
}

@keyframes buttonClick {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(0.92);
  }
  100% {
    transform: scale(0.95);
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
</style>


