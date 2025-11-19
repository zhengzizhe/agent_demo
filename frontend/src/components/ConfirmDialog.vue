<template>
  <transition name="dialog-fade">
    <div v-if="visible" class="confirm-dialog-overlay" @click.self="handleCancel">
      <div class="confirm-dialog-content">
        <div class="confirm-dialog-header">
          <svg class="confirm-icon" width="24" height="24" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" fill="none"/>
            <path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <h3 class="confirm-dialog-title">{{ title }}</h3>
        </div>
        <div class="confirm-dialog-body">
          <p class="confirm-message">{{ message }}</p>
        </div>
        <div class="confirm-dialog-footer">
          <button class="btn-secondary" @click="handleCancel">取消</button>
          <button class="btn-primary" @click="handleConfirm">{{ confirmText }}</button>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '确认操作'
  },
  message: {
    type: String,
    required: true
  },
  confirmText: {
    type: String,
    default: '确定'
  }
})

const emit = defineEmits(['confirm', 'cancel'])

const handleConfirm = () => {
  emit('confirm')
}

const handleCancel = () => {
  emit('cancel')
}
</script>

<style scoped>
.confirm-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.confirm-dialog-content {
  background: white;
  border-radius: 8px;
  width: 400px;
  max-width: 90vw;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.confirm-dialog-header {
  padding: 20px 24px;
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fafbfc;
  border-radius: 8px 8px 0 0;
  border-bottom: 1px solid #e5e6eb;
}

.confirm-icon {
  width: 24px;
  height: 24px;
  color: #ef4444;
  flex-shrink: 0;
}

.confirm-dialog-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.confirm-dialog-body {
  padding: 24px;
}

.confirm-message {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
  line-height: 1.6;
}

.confirm-dialog-footer {
  padding: 16px 24px;
  border-top: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
}

.btn-secondary {
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  color: #565869;
  background: transparent;
  border: 1px solid #e5e5e5;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.btn-secondary::before {
  content: '';
  position: absolute;
  inset: 0;
  background: currentColor;
  opacity: 0;
  transform: scale(0);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 6px;
}

.btn-secondary:hover {
  background: #f5f5f5;
  border-color: #d1d5db;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.btn-secondary:active {
  transform: translateY(0) scale(0.98);
}

.btn-primary {
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 600;
  color: white;
  background: #ef4444;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.btn-primary::before {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.2);
  opacity: 0;
  transform: scale(0);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 6px;
}

.btn-primary:hover {
  background: #dc2626;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.btn-primary:active {
  transform: translateY(0) scale(0.98);
}

/* 过渡动画 - Notion/飞书风格 */
.dialog-fade-enter-active {
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.dialog-fade-leave-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 1, 1);
}

.dialog-fade-enter-from {
  opacity: 0;
}

.dialog-fade-leave-to {
  opacity: 0;
}

.confirm-dialog-content {
  animation: dialogSlideUp 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes dialogSlideUp {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}
</style>

