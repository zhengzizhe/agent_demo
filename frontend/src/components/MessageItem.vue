<template>
  <div 
    :class="['message', message.role]"
    :style="{ animationDelay: `${animationDelay}s` }"
  >
    <div class="message-avatar">
      <span v-if="message.role === 'user'">U</span>
      <span v-else>AI</span>
    </div>
    <div class="message-content">
      <div class="message-text" v-html="formattedContent"></div>
      <div v-if="message.streaming" class="streaming-indicator">
        <span class="typing-dot"></span>
        <span class="typing-dot"></span>
        <span class="typing-dot"></span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { getMessageContent } from '../utils/message.js'
import { formatMessage, createMarkdownRenderer } from '../utils/markdown.js'

const props = defineProps({
  message: {
    type: Object,
    required: true
  },
  animationDelay: {
    type: Number,
    default: 0
  }
})

// 创建 markdown 渲染器实例（每个组件实例一个，避免链接信息混乱）
let md = null
onMounted(() => {
  md = createMarkdownRenderer()
})

// 格式化消息内容
const formattedContent = computed(() => {
  if (!md) {
    md = createMarkdownRenderer()
  }
  const content = getMessageContent(props.message)
  return formatMessage(content, md)
})
</script>

<style scoped>
.message {
  display: flex;
  gap: 12px;
  animation: messageSlideIn 0.5s cubic-bezier(0.16, 1, 0.3, 1) both;
  will-change: transform, opacity;
}

.message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
  max-width: 85%;
}

.message.assistant {
  align-self: flex-start;
  max-width: 85%;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #10a37f 0%, #19c37d 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  flex-shrink: 0;
  color: white;
  box-shadow: 
    0 4px 12px rgba(16, 163, 127, 0.2),
    0 2px 4px rgba(16, 163, 127, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  border: none;
  letter-spacing: -0.02em;
  animation: avatarPopIn 0.5s cubic-bezier(0.34, 1.56, 0.64, 1) both;
  will-change: transform;
  position: relative;
  overflow: hidden;
}

.message-avatar::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.2), transparent);
  opacity: 0;
  transition: opacity 0.3s;
}

.message-avatar:hover::before {
  opacity: 1;
}

.message.user .message-avatar {
  background: linear-gradient(135deg, #19c37d 0%, #16a570 100%);
  color: white;
  box-shadow: 
    0 2px 6px rgba(25, 195, 125, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  border: none;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-text {
  background: linear-gradient(135deg, #ffffff 0%, #fafafa 100%);
  padding: 20px 24px;
  border-radius: 16px;
  line-height: 1.75;
  word-wrap: break-word;
  font-size: 15px;
  color: #353740;
  letter-spacing: -0.01em;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-rendering: optimizeLegibility;
  box-shadow: 
    0 4px 16px rgba(0, 0, 0, 0.08),
    0 2px 4px rgba(0, 0, 0, 0.04),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  border: 1.5px solid rgba(16, 163, 127, 0.1);
  position: relative;
  animation: messageTextFadeIn 0.5s ease-out both;
  will-change: opacity, transform;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.message-text:hover {
  box-shadow: 
    0 6px 20px rgba(16, 163, 127, 0.12),
    0 3px 6px rgba(16, 163, 127, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  border-color: rgba(16, 163, 127, 0.2);
  transform: translateY(-1px);
}

.message-text::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(to right, transparent, rgba(0, 0, 0, 0.05), transparent);
}

.message-text p {
  margin: 0.9em 0;
  line-height: 1.8;
}

.message-text p:first-child {
  margin-top: 0;
}

.message-text p:last-child {
  margin-bottom: 0;
}

.message-text p::selection {
  background: rgba(16, 163, 127, 0.2);
}

.message-text *::selection {
  background: rgba(16, 163, 127, 0.2);
}

.message-text h1,
.message-text h2,
.message-text h3,
.message-text h4 {
  margin: 1.2em 0 0.7em 0;
  font-weight: 600;
  color: #202123;
  line-height: 1.4;
}

.message-text h1 {
  font-size: 1.6em;
  border-bottom: 2px solid #e5e5e6;
  padding-bottom: 0.4em;
  margin-top: 0.5em;
}

.message-text h2 {
  font-size: 1.4em;
  border-bottom: 1px solid #e5e5e6;
  padding-bottom: 0.3em;
}

.message-text h3 {
  font-size: 1.2em;
}

.message-text h4 {
  font-size: 1.1em;
  color: #565869;
}

.message-text ul,
.message-text ol {
  margin: 1em 0;
  padding-left: 1.8em;
  line-height: 1.7;
}

.message-text ul {
  list-style-type: disc;
}

.message-text ol {
  list-style-type: decimal;
}

.message-text li {
  margin: 0.5em 0;
  line-height: 1.7;
  padding-left: 0.3em;
}

.message-text li::marker {
  color: #10a37f;
}

.message-text code {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 0.9em;
}

.message-text :deep(.inline-code) {
  background: rgba(16, 163, 127, 0.1);
  color: #10a37f;
  padding: 2px 6px;
  border-radius: 4px;
  border: 1px solid rgba(16, 163, 127, 0.2);
  font-size: 0.9em;
  font-weight: 500;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
}

.message-text :deep(.code-block) {
  background: linear-gradient(135deg, #f7f7f8 0%, #f0f0f0 100%);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-left: 4px solid #10a37f;
  border-radius: 8px;
  padding: 16px 20px;
  margin: 1.4em 0;
  overflow-x: auto;
  font-size: 0.9em;
  line-height: 1.7;
  position: relative;
  box-shadow: 
    0 2px 8px rgba(0, 0, 0, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.8),
    inset -1px 0 0 rgba(16, 163, 127, 0.1);
}

.message-text :deep(.code-block) code {
  color: #353740;
  display: block;
  white-space: pre;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 0.95em;
}

.message-text :deep(.code-block)::-webkit-scrollbar {
  height: 8px;
}

.message-text :deep(.code-block)::-webkit-scrollbar-track {
  background: #f7f7f8;
  border-radius: 4px;
}

.message-text :deep(.code-block)::-webkit-scrollbar-thumb {
  background: #d1d1d6;
  border-radius: 4px;
}

.message-text :deep(.code-block)::-webkit-scrollbar-thumb:hover {
  background: #b5b5b9;
}

.message-text strong {
  color: #202123;
  font-weight: 600;
  letter-spacing: -0.01em;
}

.message-text em {
  color: #565869;
  font-style: italic;
}

.message-text del {
  color: #8e8ea0;
  text-decoration: line-through;
  text-decoration-color: #ef4444;
}

/* 链接卡片样式 */
.message-text :deep(.link-card-wrapper) {
  display: block;
  margin: 1em 0;
  animation: linkCardSlideIn 0.4s cubic-bezier(0.16, 1, 0.3, 1) both;
}

.message-text :deep(.link-card) {
  display: flex;
  flex-direction: column;
  padding: 16px 18px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  text-decoration: none;
  color: #353740;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  box-shadow: 
    0 2px 8px rgba(0, 0, 0, 0.04),
    0 1px 2px rgba(0, 0, 0, 0.02);
}

.message-text :deep(.link-card)::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #10a37f, #19c37d);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.message-text :deep(.link-card):hover {
  transform: translateY(-2px);
  box-shadow: 
    0 6px 16px rgba(16, 163, 127, 0.15),
    0 2px 6px rgba(16, 163, 127, 0.1);
  border-color: rgba(16, 163, 127, 0.2);
}

.message-text :deep(.link-card):hover::before {
  transform: scaleX(1);
}

.message-text :deep(.link-card-title) {
  font-size: 15px;
  font-weight: 600;
  color: #202123;
  margin-bottom: 6px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.message-text :deep(.link-card-meta) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

.message-text :deep(.link-card-domain) {
  font-size: 12px;
  color: #8e8ea0;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
}

.message-text :deep(.link-card-domain)::before {
  content: '';
  display: inline-block;
  width: 12px;
  height: 12px;
  background: linear-gradient(135deg, #10a37f, #19c37d);
  border-radius: 2px;
  flex-shrink: 0;
}

.message-text :deep(.link-card-icon) {
  width: 14px;
  height: 14px;
  color: #8e8ea0;
  opacity: 0.6;
  transition: all 0.2s;
  flex-shrink: 0;
}

.message-text :deep(.link-card):hover .link-card-icon {
  color: #10a37f;
  opacity: 1;
  transform: translate(2px, -2px);
}

/* 用户消息中的链接卡片 */
.message.user .message-text :deep(.link-card) {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.2);
  color: white;
  backdrop-filter: blur(10px);
}

.message.user .message-text :deep(.link-card)::before {
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.3), rgba(255, 255, 255, 0.5));
}

.message.user .message-text :deep(.link-card):hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
}

.message.user .message-text :deep(.link-card-title) {
  color: white;
}

.message.user .message-text :deep(.link-card-domain) {
  color: rgba(255, 255, 255, 0.8);
}

.message.user .message-text :deep(.link-card-domain)::before {
  background: rgba(255, 255, 255, 0.6);
}

.message.user .message-text :deep(.link-card-icon) {
  color: rgba(255, 255, 255, 0.7);
}

.message.user .message-text :deep(.link-card):hover .link-card-icon {
  color: white;
}

/* 保留普通文本链接样式（非卡片） */
.message-text :deep(a:not(.link-card)) {
  color: #10a37f;
  text-decoration: none;
  border-bottom: 1px solid rgba(16, 163, 127, 0.3);
  transition: all 0.2s;
  padding-bottom: 1px;
}

.message-text :deep(a:not(.link-card)):hover {
  color: #0d8f6e;
  border-bottom-color: #10a37f;
  background: rgba(16, 163, 127, 0.08);
  padding: 0 2px;
  margin: 0 -2px;
  border-radius: 3px;
}

.message-text :deep(hr) {
  border: none;
  border-top: 1px solid #e5e5e6;
  margin: 1.8em 0;
  height: 1px;
}

.message-text :deep(blockquote) {
  border-left: 3px solid #10a37f;
  background: rgba(16, 163, 127, 0.05);
  padding: 12px 16px;
  margin: 1em 0;
  border-radius: 4px;
  color: #565869;
  font-style: italic;
  line-height: 1.7;
}

.message-text :deep(blockquote) p {
  margin: 0;
}

.message.user .message-text {
  background: linear-gradient(135deg, #19c37d 0%, #16a570 100%);
  color: white;
  box-shadow: 
    0 4px 12px rgba(25, 195, 125, 0.25),
    0 2px 4px rgba(25, 195, 125, 0.15);
  border: none;
  position: relative;
  animation: userMessageSlideIn 0.5s cubic-bezier(0.16, 1, 0.3, 1) both;
}

.message.user .message-text::before {
  display: none;
}

.message.user .message-text::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(to right, transparent, rgba(255, 255, 255, 0.3), transparent);
}

.message.user .message-text * {
  color: white;
}

.message.user .message-text h1,
.message.user .message-text h2,
.message.user .message-text h3,
.message.user .message-text h4 {
  color: white;
  border-color: rgba(255, 255, 255, 0.2);
}

.message.user .message-text :deep(a:not(.link-card)) {
  color: white;
  border-bottom-color: rgba(255, 255, 255, 0.5);
}

.streaming-indicator {
  display: flex;
  gap: 4px;
  padding: 8px 16px;
  align-items: center;
}

.typing-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #8e8ea0;
  animation: typing 1.4s infinite;
}

.typing-dot:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes messageSlideIn {
  from {
    opacity: 0;
    transform: translateY(25px) scale(0.94) rotate(-1deg);
  }
  50% {
    transform: translateY(-2px) scale(1.01) rotate(0.5deg);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1) rotate(0deg);
  }
}

@keyframes userMessageSlideIn {
  from {
    opacity: 0;
    transform: translateX(30px) scale(0.92) rotate(2deg);
  }
  50% {
    transform: translateX(-3px) scale(1.02) rotate(-0.5deg);
  }
  to {
    opacity: 1;
    transform: translateX(0) scale(1) rotate(0deg);
  }
}

@keyframes linkCardSlideIn {
  from {
    opacity: 0;
    transform: translateY(10px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes avatarPopIn {
  from {
    opacity: 0;
    transform: scale(0) rotate(-180deg);
  }
  60% {
    transform: scale(1.15) rotate(10deg);
  }
  80% {
    transform: scale(0.95) rotate(-5deg);
  }
  to {
    opacity: 1;
    transform: scale(1) rotate(0deg);
  }
}

@keyframes messageTextFadeIn {
  from {
    opacity: 0;
    transform: translateY(5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
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
</style>

