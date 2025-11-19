<template>
  <div class="input-container">
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
      <input 
        type="number" 
        v-model.number="form.orchestratorId"
        placeholder="ID"
        class="orchestrator-input"
        :disabled="isExecuting"
      />
      <textarea 
        v-model="form.message"
        placeholder="输入您的任务描述..."
        class="message-input"
        :disabled="isExecuting"
        @keydown.enter.exact.prevent="handleEnter"
        @keydown.shift.enter.exact="handleShiftEnter"
        rows="1"
        ref="textareaRef"
      ></textarea>
      <button 
        class="send-button"
        :disabled="!canSend || isExecuting"
        @click="$emit('send')"
      >
        <span v-if="isExecuting">发送中...</span>
        <span v-else>发送</span>
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
  background: #ffffff;
  padding: 20px 32px;
  position: relative;
  z-index: 10;
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
  background: #eff6ff;
  border: 1px solid #dbeafe;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  color: #2563eb;
}

.planning-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  background: #2563eb;
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
  align-items: flex-end;
  max-width: 1200px;
  margin: 0 auto;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 12px 16px;
  transition: all 0.15s ease-out;
}

.input-wrapper:focus-within {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.orchestrator-input {
  width: 70px;
  padding: 8px 12px;
  background: transparent;
  border: none;
  border-radius: 6px;
  color: #111827;
  font-size: 13px;
  font-weight: 500;
}

.orchestrator-input:focus {
  outline: none;
  background: #f3f4f6;
}

.message-input {
  flex: 1;
  padding: 8px 12px;
  background: transparent;
  border: none;
  border-radius: 6px;
  color: #111827;
  font-size: 15px;
  font-family: inherit;
  resize: none;
  max-height: 200px;
  overflow-y: auto;
  line-height: 1.5;
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
  padding: 10px 20px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.15s ease-out;
  white-space: nowrap;
  min-width: 80px;
}

.send-button:hover:not(:disabled) {
  background-color: #1d4ed8;
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


