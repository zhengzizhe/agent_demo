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
  border-top: 1px solid rgba(0, 0, 0, 0.08);
  background: linear-gradient(to top, #ffffff 0%, #fafafa 100%);
  padding: 28px 64px;
  box-shadow: 
    0 -4px 16px rgba(0, 0, 0, 0.04),
    0 -1px 2px rgba(0, 0, 0, 0.06);
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
  padding: 8px 16px;
  background: linear-gradient(135deg, rgba(33, 150, 243, 0.1) 0%, rgba(33, 150, 243, 0.05) 100%);
  border: 1px solid rgba(33, 150, 243, 0.2);
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  color: #2196f3;
  letter-spacing: 0.02em;
  box-shadow: 
    0 2px 8px rgba(33, 150, 243, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  animation: planningBlink 1.5s ease-in-out infinite;
}

.planning-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  background: #2196f3;
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
  border: 2px solid rgba(0, 0, 0, 0.1);
  border-radius: 28px;
  padding: 14px 18px;
  box-shadow: 
    0 4px 16px rgba(0, 0, 0, 0.08),
    0 2px 4px rgba(0, 0, 0, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  backdrop-filter: blur(10px);
}

.input-wrapper::before {
  content: '';
  position: absolute;
  inset: -2px;
  border-radius: 26px;
  padding: 2px;
  background: linear-gradient(135deg, #42a5f5, #1976d2);
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  opacity: 0;
  transition: opacity 0.3s;
  pointer-events: none;
  z-index: -1;
}

.input-wrapper:focus-within {
  border-color: transparent;
  box-shadow: 
    0 6px 20px rgba(66, 165, 245, 0.2),
    0 2px 6px rgba(66, 165, 245, 0.15);
  transform: translateY(-2px) scale(1.002);
  animation: inputFocusPulse 2s ease-in-out infinite;
}

.input-wrapper:focus-within::before {
  opacity: 1;
}

.orchestrator-input {
  width: 70px;
  padding: 8px 12px;
  background: transparent;
  border: none;
  border-radius: 16px;
  color: #353740;
  font-size: 13px;
  font-weight: 500;
  position: relative;
  z-index: 1;
}

.orchestrator-input:focus {
  outline: none;
  background: #f7f7f8;
}

.message-input {
  flex: 1;
  padding: 12px 16px;
  background: transparent;
  border: none;
  border-radius: 18px;
  color: #353740;
  font-size: 16px;
  font-family: inherit;
  resize: none;
  max-height: 200px;
  overflow-y: auto;
  line-height: 1.6;
  position: relative;
  z-index: 1;
}

.message-input:focus {
  outline: none;
}

.message-input::placeholder {
  color: #8e8ea0;
}

.message-input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.send-button {
  padding: 12px 24px;
  background: linear-gradient(135deg, #19c37d 0%, #16a570 100%);
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;
  min-width: 80px;
  box-shadow: 
    0 2px 8px rgba(66, 165, 245, 0.25),
    0 1px 2px rgba(66, 165, 245, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  position: relative;
  overflow: hidden;
  transform-origin: center;
}

.send-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s;
}

.send-button:hover:not(:disabled)::before {
  left: 100%;
}

.send-button:hover:not(:disabled) {
  background: linear-gradient(135deg, #16a570 0%, #138d5f 100%);
  transform: translateY(-2px);
  box-shadow: 
    0 4px 12px rgba(66, 165, 245, 0.35),
    0 2px 4px rgba(66, 165, 245, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.send-button:active:not(:disabled) {
  transform: translateY(0) scale(0.95);
  box-shadow: 
    0 2px 6px rgba(66, 165, 245, 0.3),
    inset 0 1px 2px rgba(0, 0, 0, 0.1);
  animation: buttonClick 0.2s ease-out;
}

.send-button:disabled {
  background: linear-gradient(135deg, #d1d1d6 0%, #b5b5b9 100%);
  cursor: not-allowed;
  opacity: 0.5;
  transform: none;
  box-shadow: none;
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


