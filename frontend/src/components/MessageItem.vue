<template>
  <div 
    :class="['message', message.role]"
    :style="{ animationDelay: `${animationDelay}s` }"
  >
    <div class="message-content" :data-message-id="message.id || message.timestamp || Date.now()">
      <div class="message-card" :class="{ 'has-task-list': message.role === 'assistant' && message.tasks && Array.isArray(message.tasks) && message.tasks.length > 0 }">
        <div class="message-card-glow"></div>
        <!-- 任务列表（仅AI消息显示，放在消息泡泡顶部） -->
        <TaskList 
          v-if="message.role === 'assistant' && message.tasks && Array.isArray(message.tasks) && message.tasks.length > 0" 
          :tasks="message.tasks"
          :animation-delay="animationDelay"
          class="message-task-list"
        />
        <!-- 复制按钮（在任务列表下方，或消息文本右上角） -->
        <div
          v-if="!message.streaming"
          class="message-copy-wrapper"
          :class="{ 'in-task-list': message.role === 'assistant' && message.tasks && Array.isArray(message.tasks) && message.tasks.length > 0 }"
        >
          <button
            class="message-copy-btn"
            @click="handleCopyMessage"
            :title="isCopied ? '已复制' : '复制消息'"
          >
            <span v-if="isCopied">已复制</span>
            <span v-else>复制</span>
          </button>
        </div>
        <div class="message-text" v-html="formattedContent"></div>
        <div v-if="message.streaming" class="streaming-indicator">
          <div class="streaming-pulse"></div>
          <span class="typing-dot"></span>
          <span class="typing-dot"></span>
          <span class="typing-dot"></span>
        </div>
      </div>
      <!-- 导出按钮（仅AI消息显示，hover显示） -->
      <div
        v-if="message.role === 'assistant' && !message.streaming"
        class="export-pdf-wrapper"
      >
        <button
          class="export-pdf-btn"
          @click="handleExportMessage"
          :disabled="isExporting"
          :title="isExporting ? '导出中...' : '导出为PDF'"
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M14 2V8H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M16 13H8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M16 17H8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M10 9H9H8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span v-if="isExporting">导出中...</span>
          <span v-else>导出</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, nextTick, watch, ref } from 'vue'
import { getMessageContent } from '../utils/message.js'
import { formatMessage, createMarkdownRenderer } from '../utils/markdown.js'
import { usePdfExport } from '../composables/usePdfExport.js'
import TaskList from './TaskList.vue'
import hljs from 'highlight.js'

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
  nextTick(() => {
    initCodeBlockInteractions()
  })
})

// 监听消息内容变化，重新初始化代码块交互（优化：只在内容或streaming状态变化时触发）
watch(() => [props.message.content, props.message.streaming], () => {
  nextTick(() => {
    initCodeBlockInteractions()
  })
}, { flush: 'post' })

// 格式化消息内容（优化：减少不必要的重新渲染）
const formattedContent = computed(() => {
  if (!md) {
    md = createMarkdownRenderer()
  }
  const content = getMessageContent(props.message)
  return formatMessage(content, md)
})

// PDF 导出
const pdfExport = usePdfExport()
const isExporting = ref(false)

// 消息复制
const isCopied = ref(false)

// 复制消息内容
const handleCopyMessage = async (event) => {
  event.stopPropagation()
  try {
    const content = getMessageContent(props.message)
    // 移除 HTML 标签，获取纯文本
    const tempDiv = document.createElement('div')
    tempDiv.innerHTML = content
    const textContent = tempDiv.textContent || tempDiv.innerText || ''
    
    await navigator.clipboard.writeText(textContent)
    isCopied.value = true
    setTimeout(() => {
      isCopied.value = false
    }, 2000)
  } catch (err) {
    console.error('复制失败:', err)
    alert('复制失败，请稍后重试')
  }
}

// 导出单条消息
const handleExportMessage = async (event) => {
  event.stopPropagation()
  isExporting.value = true
  try {
    // 使用 ref 或直接查找当前消息元素
    const messageId = props.message.id || props.message.timestamp || Date.now()
    const messageElement = document.querySelector(`[data-message-id="${messageId}"]`)
    if (!messageElement) {
      console.error('消息元素不存在')
      return
    }
    
    // 导出整个消息内容区域（包含消息卡片）
    const messageCard = messageElement.querySelector('.message-card')
    if (!messageCard) {
      console.error('消息卡片不存在')
      return
    }
    
    // 检查消息文本内容是否存在
    const messageText = messageCard.querySelector('.message-text')
    if (!messageText) {
      console.error('消息文本不存在')
      return
    }
    
    console.log('准备导出，消息文本内容长度:', messageText.innerHTML.length)
    console.log('消息文本前200字符:', messageText.innerHTML.substring(0, 200))
    
    const date = new Date().toISOString().split('T')[0]
    const timestamp = props.message.timestamp ? new Date(props.message.timestamp).getTime() : Date.now()
    const filename = `message-${date}-${timestamp}.pdf`
    
    // 传递整个消息卡片元素，确保包含所有内容
    await pdfExport.exportSingleMessage(messageCard, filename)
  } catch (error) {
    console.error('导出 PDF 失败:', error)
    alert('导出 PDF 失败，请稍后重试')
  } finally {
    isExporting.value = false
  }
}

// 初始化代码块交互功能
function initCodeBlockInteractions() {
  // 使用 nextTick 确保 DOM 已更新
  nextTick(() => {
    // 查找当前消息的容器
    const messageElement = document.querySelector(`[data-message-id="${props.message.id || props.message.timestamp || Date.now()}"]`) || 
                           document.querySelector('.message-content')
    if (!messageElement) return
    
    // 初始化所有代码块（避免重复初始化）
    const codeBlocks = messageElement.querySelectorAll('.feishu-code-block-wrapper:not([data-initialized])')
    codeBlocks.forEach(block => {
      block.setAttribute('data-initialized', 'true')
      initCodeBlock(block)
    })
    
    // 初始化折叠块
    const collapseBlocks = messageElement.querySelectorAll('.feishu-collapse-block:not([data-initialized])')
    collapseBlocks.forEach(block => {
      block.setAttribute('data-initialized', 'true')
      initCollapseBlock(block)
    })
  })
}

// 初始化折叠块
function initCollapseBlock(block) {
  const header = block.querySelector('.feishu-collapse-header')
  const content = block.querySelector('.feishu-collapse-content')
  const icon = block.querySelector('.feishu-collapse-icon')
  
  if (!header || !content || !icon) return
  
  // 默认折叠
  content.style.display = 'none'
  block.classList.add('collapsed')
  
  header.addEventListener('click', () => {
    const isCollapsed = block.classList.contains('collapsed')
    if (isCollapsed) {
      block.classList.remove('collapsed')
      content.style.display = 'block'
      icon.style.transform = 'rotate(90deg)'
    } else {
      block.classList.add('collapsed')
      content.style.display = 'none'
      icon.style.transform = 'rotate(0deg)'
    }
  })
}

function initCodeBlock(block) {
  const codeId = block.getAttribute('data-code-id')
  if (!codeId) return
  
  // 语言选择器
  const langSelector = block.querySelector('.feishu-code-lang-selector')
  const langDropdown = block.querySelector('.feishu-code-lang-dropdown')
  const langText = block.querySelector('.feishu-code-lang-text')
  
  if (langSelector && langDropdown) {
    langSelector.addEventListener('click', (e) => {
      e.stopPropagation()
      const isVisible = langDropdown.style.display !== 'none'
      // 关闭所有其他下拉菜单
      document.querySelectorAll('.feishu-code-lang-dropdown').forEach(dd => {
        if (dd !== langDropdown) dd.style.display = 'none'
      })
      langDropdown.style.display = isVisible ? 'none' : 'block'
    })
    
    // 语言选项点击
    langDropdown.querySelectorAll('.feishu-code-lang-option').forEach(option => {
      option.addEventListener('click', (e) => {
        e.stopPropagation()
        const newLang = option.getAttribute('data-lang')
        changeCodeLanguage(block, newLang)
        langDropdown.style.display = 'none'
      })
    })
  }
  
  // 背景主题切换按钮
  const themeBtn = block.querySelector('.feishu-code-theme')
  const codeContent = block.querySelector('.feishu-code-content')
  const codePre = block.querySelector('.feishu-code-pre')
  if (themeBtn && codeContent && codePre) {
    // 默认黑色主题
    const currentTheme = themeBtn.getAttribute('data-theme') || 'dark'
    applyTheme(block, currentTheme)
    
    themeBtn.addEventListener('click', () => {
      const currentTheme = themeBtn.getAttribute('data-theme') || 'dark'
      const newTheme = currentTheme === 'dark' ? 'light' : 'dark'
      themeBtn.setAttribute('data-theme', newTheme)
      applyTheme(block, newTheme)
    })
  }
  
  // 自动换行按钮
  const wordWrapBtn = block.querySelector('.feishu-code-wordwrap')
  if (wordWrapBtn && codeContent) {
    wordWrapBtn.addEventListener('click', () => {
      const isActive = wordWrapBtn.classList.contains('active')
      if (isActive) {
        wordWrapBtn.classList.remove('active')
        codeContent.classList.remove('word-wrap')
      } else {
        wordWrapBtn.classList.add('active')
        codeContent.classList.add('word-wrap')
      }
    })
  }
  
  // 复制按钮
  const copyBtn = block.querySelector('.feishu-code-copy')
  if (copyBtn) {
    copyBtn.addEventListener('click', async (e) => {
      e.stopPropagation()
      // 优先使用 data-code-encoded，如果没有则从 code 元素获取
      let code = ''
      const encodedCode = block.getAttribute('data-code-encoded')
      if (encodedCode) {
        try {
          code = decodeURIComponent(escape(atob(encodedCode)))
        } catch (err) {
          console.error('解码失败:', err)
        }
      }
      
      // 如果编码方式失败，直接从 code 元素获取文本
      if (!code) {
        const codeElement = block.querySelector('.feishu-code-pre code')
        if (codeElement) {
          code = codeElement.textContent || codeElement.innerText || ''
        }
      }
      
      if (code) {
        try {
          await navigator.clipboard.writeText(code)
          const span = copyBtn.querySelector('span')
          const originalText = span ? span.textContent : '复制'
          copyBtn.classList.add('copied')
          if (span) {
            span.textContent = '已复制'
          }
          setTimeout(() => {
            copyBtn.classList.remove('copied')
            if (span) {
              span.textContent = originalText
            }
          }, 2000)
        } catch (err) {
          console.error('复制失败:', err)
          alert('复制失败，请稍后重试')
        }
      } else {
        console.error('无法获取代码内容')
      }
    })
  }
}

// 切换代码语言
function changeCodeLanguage(block, newLang) {
  const encodedCode = block.getAttribute('data-code-encoded')
  if (!encodedCode) return
  
  const code = decodeURIComponent(escape(atob(encodedCode)))
  const codeElement = block.querySelector('.feishu-code-pre code')
  const langText = block.querySelector('.feishu-code-lang-text')
  
  if (!codeElement || !langText) return
  
  // 更新语言
  block.setAttribute('data-lang', newLang)
  
  // 重新高亮代码
  let highlightedCode = ''
  if (newLang && hljs.getLanguage(newLang)) {
    try {
      const result = hljs.highlight(code, { language: newLang, ignoreIllegals: true })
      highlightedCode = result.value
    } catch (err) {
      highlightedCode = escapeHtml(code)
    }
  } else {
    highlightedCode = escapeHtml(code)
  }
  
  // 更新代码类名
  codeElement.className = newLang ? `hljs language-${newLang}` : 'hljs'
  codeElement.innerHTML = highlightedCode
  
  // 更新语言标签
  const langMap = {
    '': 'Plain Text',
    'javascript': 'JavaScript',
    'typescript': 'TypeScript',
    'python': 'Python',
    'java': 'Java',
    'cpp': 'C++',
    'c': 'C',
    'csharp': 'C#',
    'go': 'Go',
    'rust': 'Rust',
    'php': 'PHP',
    'ruby': 'Ruby',
    'swift': 'Swift',
    'kotlin': 'Kotlin',
    'html': 'HTML',
    'css': 'CSS',
    'scss': 'SCSS',
    'json': 'JSON',
    'xml': 'XML',
    'yaml': 'YAML',
    'markdown': 'Markdown',
    'sql': 'SQL',
    'bash': 'Bash',
    'shell': 'Shell',
    'dockerfile': 'Dockerfile',
    'nginx': 'Nginx',
    'vue': 'Vue',
    'react': 'React'
  }
  langText.textContent = langMap[newLang] || (newLang ? newLang.toUpperCase() : 'Plain Text')
  
  // 更新下拉菜单中的 active 状态
  const dropdown = block.querySelector('.feishu-code-lang-dropdown')
  if (dropdown) {
    dropdown.querySelectorAll('.feishu-code-lang-option').forEach(option => {
      if (option.getAttribute('data-lang') === newLang) {
        option.classList.add('active')
      } else {
        option.classList.remove('active')
      }
    })
  }
}

function escapeHtml(text) {
  const div = document.createElement('div')
  div.textContent = text
  return div.innerHTML
}

// 应用主题
function applyTheme(block, theme) {
  const codeContent = block.querySelector('.feishu-code-content')
  const codePre = block.querySelector('.feishu-code-pre')
  const themeBtn = block.querySelector('.feishu-code-theme')
  const themeIconDark = themeBtn?.querySelector('.theme-icon-dark')
  const themeIconLight = themeBtn?.querySelector('.theme-icon-light')
  
  if (!codeContent || !codePre) return
  
  // 移除之前的主题类
  block.classList.remove('theme-dark', 'theme-light')
  
  if (theme === 'dark') {
    // 黑色主题
    block.classList.add('theme-dark')
    codeContent.style.background = '#1e1e1e'
    codePre.style.color = '#d4d4d4'
    
    // 切换图标
    if (themeIconDark) themeIconDark.style.display = 'block'
    if (themeIconLight) themeIconLight.style.display = 'none'
  } else {
    // 浅色主题
    block.classList.add('theme-light')
    codeContent.style.background = '#fafafa'
    codePre.style.color = '#1d2129'
    
    // 切换图标
    if (themeIconDark) themeIconDark.style.display = 'none'
    if (themeIconLight) themeIconLight.style.display = 'block'
  }
}

// 点击外部关闭下拉菜单
document.addEventListener('click', (e) => {
  if (!e.target.closest('.feishu-code-lang-selector')) {
    document.querySelectorAll('.feishu-code-lang-dropdown').forEach(dd => {
      dd.style.display = 'none'
    })
  }
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
  max-width: 70%; /* 最大宽度限制 */
  width: fit-content; /* 根据内容自适应 */
  margin-left: auto; /* 右对齐 */
}

.message.assistant {
  align-self: center; /* AI消息居中 */
  max-width: 85%; /* 增加最大宽度 */
  min-width: fit-content; /* 根据内容自适应 */
  margin-left: auto; /* 水平居中 */
  margin-right: auto; /* 水平居中 */
}

/* 头像已移除 */

.message-content {
  flex: 0 1 auto; /* 不拉伸，根据内容自适应 */
  min-width: 0;
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: fit-content; /* 根据内容自适应宽度 */
  max-width: 100%; /* 不超过父容器 */
}

.message.user .message-content {
  align-items: flex-end; /* 用户消息内容右对齐 */
}

.message-content:hover .export-pdf-wrapper {
  opacity: 1;
  visibility: visible;
}

.message-card {
  position: relative;
  background: linear-gradient(135deg, #ffffff 0%, #fafafa 50%, #ffffff 100%);
  border-radius: 14px;
  padding: 2px;
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.1),
    0 4px 8px rgba(0, 0, 0, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.95);
  animation: messageCardSlideIn 0.6s cubic-bezier(0.16, 1, 0.3, 1) both;
  will-change: transform, opacity;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  width: fit-content; /* 根据内容自适应宽度 */
  max-width: 100%; /* 不超过父容器 */
}

/* 移除旧的hover样式，已在.message-copy-wrapper中处理 */

/* 当消息前面有任务列表时，调整消息气泡的顶部圆角 - 通过全局样式处理 */

.message-card-glow {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(33, 150, 243, 0.1) 0%, transparent 70%);
  opacity: 0;
  transition: opacity 0.5s;
  pointer-events: none;
}

.message-card:hover {
  transform: translateY(-2px);
  box-shadow: 
    0 12px 40px rgba(33, 150, 243, 0.15),
    0 6px 12px rgba(33, 150, 243, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.95);
}

.message-card:hover .message-card-glow {
  opacity: 1;
}

.message-text {
  background: #ffffff;
  padding: 20px 24px;
  border-radius: 14px;
  line-height: 1.75;
  word-wrap: break-word;
  word-break: break-word;
  font-size: 15px;
  color: #2d2d2d;
  letter-spacing: 0.01em;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-rendering: optimizeLegibility;
  position: relative;
  z-index: 1;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Microsoft YaHei', 'PingFang SC', 'Hiragino Sans GB', sans-serif;
  width: fit-content; /* 根据内容自适应宽度 */
  max-width: 100%; /* 不超过父容器 */
}

/* AI消息内容增加内边距，不要紧贴消息泡泡 */
.message.assistant .message-text {
  padding: 28px 32px; /* 增加内边距 */
}

/* 当有任务列表时，消息文本的顶部内边距可以正常 */
.message-card.has-task-list .message-text {
  padding-top: 28px; /* 正常内边距，因为复制按钮在任务列表下方 */
}

.message.user .message-text {
  min-width: auto; /* 用户消息不需要最小宽度限制 */
}

.message-text::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    rgba(33, 150, 243, 0.2) 20%, 
    rgba(66, 165, 245, 0.3) 50%, 
    rgba(33, 150, 243, 0.2) 80%, 
    transparent 100%);
  border-radius: 18px 18px 0 0;
}

.message-text p {
  margin: 0 0 12px 0;
  line-height: 1.75;
  text-align: justify;
  text-justify: inter-ideograph;
}

.message-text p:first-child {
  margin-top: 0;
}

.message-text p:last-child {
  margin-bottom: 0;
}

.message-text p + p {
  margin-top: 8px;
}

.message-text p::selection {
  background: rgba(33, 150, 243, 0.2);
}

.message-text *::selection {
  background: rgba(33, 150, 243, 0.2);
}

.message-text h1,
.message-text h2,
.message-text h3,
.message-text h4,
.message-text h5,
.message-text h6 {
  margin: 32px 0 16px 0;
  font-weight: 600;
  color: #1a1a1a;
  line-height: 1.5;
  letter-spacing: -0.01em;
}

.message-text h1:first-child,
.message-text h2:first-child,
.message-text h3:first-child,
.message-text h4:first-child {
  margin-top: 0;
}

.message-text h1 {
  font-size: 1.75em;
  font-weight: 700;
  border-bottom: 2px solid #e8e8e8;
  padding-bottom: 12px;
  margin-bottom: 20px;
  margin-top: 0;
  color: #0d0d0d;
}

.message-text h2 {
  font-size: 1.5em;
  font-weight: 650;
  border-bottom: 1px solid #e8e8e8;
  padding-bottom: 10px;
  margin-bottom: 18px;
  margin-top: 32px;
  color: #1a1a1a;
}

.message-text h3 {
  font-size: 1.25em;
  font-weight: 600;
  margin-bottom: 14px;
  margin-top: 28px;
  color: #2d2d2d;
}

.message-text h4 {
  font-size: 1.1em;
  font-weight: 600;
  margin-bottom: 12px;
  margin-top: 24px;
  color: #404040;
}

.message-text h5 {
  font-size: 1.05em;
  font-weight: 600;
  margin-bottom: 10px;
  margin-top: 20px;
  color: #565869;
}

.message-text h6 {
  font-size: 1em;
  font-weight: 600;
  margin-bottom: 8px;
  margin-top: 18px;
  color: #6b6b6b;
}

.message-text ul,
.message-text ol {
  margin: 20px 0;
  padding-left: 24px;
  padding-right: 16px;
  line-height: 1.85;
  list-style: none;
}

.message-text ul {
  counter-reset: list-counter;
}

.message-text ol {
  counter-reset: list-counter;
}

.message-text li {
  margin: 12px 0;
  line-height: 1.85;
  padding: 12px 20px 12px 40px;
  text-align: left;
  position: relative;
  background: linear-gradient(135deg, rgba(33, 150, 243, 0.04) 0%, rgba(66, 165, 245, 0.02) 100%);
  border-left: 3px solid rgba(33, 150, 243, 0.2);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.message-text li:hover {
  background: linear-gradient(135deg, rgba(33, 150, 243, 0.08) 0%, rgba(66, 165, 245, 0.04) 100%);
  border-left-color: #2196f3;
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(33, 150, 243, 0.1);
}

.message-text ul > li::before {
  content: '▸';
  position: absolute;
  left: 16px;
  top: 12px;
  color: #2196f3;
  font-weight: 700;
  font-size: 14px;
  line-height: 1.85;
}

.message-text ol > li {
  counter-increment: list-counter;
}

.message-text ol > li::before {
  content: counter(list-counter);
  position: absolute;
  left: 12px;
  top: 12px;
  width: 22px;
  height: 22px;
  background: linear-gradient(135deg, #10a37f 0%, #19c37d 100%);
  color: #ffffff;
  font-weight: 700;
  font-size: 12px;
  line-height: 22px;
  text-align: center;
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(16, 163, 127, 0.3);
}

.message-text ul ul,
.message-text ol ol,
.message-text ul ol,
.message-text ol ul {
  margin: 12px 0 12px 20px;
  padding-left: 24px;
  padding-right: 16px;
}

.message-text ul ul > li,
.message-text ol ol > li,
.message-text ul ol > li,
.message-text ol ul > li {
  padding-left: 36px;
  padding-right: 20px;
  background: linear-gradient(135deg, rgba(33, 150, 243, 0.02) 0%, rgba(66, 165, 245, 0.01) 100%);
  border-left-color: rgba(33, 150, 243, 0.15);
}

.message-text ul ul > li::before {
  content: '▪';
  font-size: 12px;
  left: 14px;
}

.message-text ol ol > li::before {
  width: 20px;
  height: 20px;
  font-size: 11px;
  line-height: 20px;
  left: 12px;
  top: 12px;
}

.message-text li > p {
  margin: 0;
}

.message-text li > ul,
.message-text li > ol {
  margin-top: 8px;
  margin-bottom: 0;
}

.message-text code {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 0.9em;
}

.message-text :deep(.inline-code) {
  background: #f0f4f8;
  color: #2196f3;
  padding: 2px 6px;
  border-radius: 4px;
  border: 1px solid #e0e8f0;
  font-size: 0.9em;
  font-weight: 500;
  font-family: 'Consolas', 'Monaco', 'Courier New', 'Fira Code', monospace;
  letter-spacing: 0;
}

/* 飞书风格代码块样式 */
.message-text :deep(.feishu-code-block-wrapper) {
  margin: 20px 0;
  border-radius: 8px;
  overflow: hidden;
  background: #f7f8fa;
  border: 1px solid #e5e6eb;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.message-text :deep(.feishu-code-header) {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #ffffff;
  border-bottom: 1px solid #e5e6eb;
  min-height: 36px;
}

.message-text :deep(.feishu-code-header-left) {
  display: flex;
  align-items: center;
  gap: 12px;
}

.message-text :deep(.feishu-code-icon) {
  font-size: 13px;
  color: #646a73;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
}

.message-text :deep(.feishu-code-icon)::before {
  content: '▼';
  font-size: 10px;
  color: #86909c;
}

.message-text :deep(.feishu-code-lang-selector) {
  position: relative;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
  user-select: none;
  transition: background-color 0.2s;
}

.message-text :deep(.feishu-code-lang-selector:hover) {
  background: #f2f3f5;
}

.message-text :deep(.feishu-code-lang-text) {
  font-size: 13px;
  color: #1d2129;
  font-weight: 500;
}

.message-text :deep(.feishu-code-dropdown-icon) {
  color: #86909c;
  transition: transform 0.2s;
}

.message-text :deep(.feishu-code-lang-selector:hover .feishu-code-dropdown-icon) {
  color: #1d2129;
}

.message-text :deep(.feishu-code-lang-dropdown) {
  position: absolute;
  top: 100%;
  left: 0;
  margin-top: 4px;
  background: #ffffff;
  border: 1px solid #e5e6eb;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  max-height: 300px;
  overflow-y: auto;
  min-width: 150px;
}

.message-text :deep(.feishu-code-lang-option) {
  padding: 8px 12px;
  font-size: 13px;
  color: #1d2129;
  cursor: pointer;
  transition: background-color 0.2s;
}

.message-text :deep(.feishu-code-lang-option:hover) {
  background: #f2f3f5;
}

.message-text :deep(.feishu-code-lang-option.active) {
  background: #e8f4fd;
  color: #165dff;
  font-weight: 500;
}

.message-text :deep(.feishu-code-header-right) {
  display: flex;
  align-items: center;
  gap: 0;
}

.message-text :deep(.feishu-code-divider) {
  width: 1px;
  height: 16px;
  background: #e5e6eb;
  margin: 0 8px;
}

.message-text :deep(.feishu-code-action-btn) {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border: none;
  background: transparent;
  color: #646a73;
  font-size: 13px;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s;
  font-weight: 500;
}

.message-text :deep(.feishu-code-action-btn:hover) {
  background: #f2f3f5;
  color: #1d2129;
}

.message-text :deep(.feishu-code-action-btn.active) {
  background: #e8f4fd;
  color: #165dff;
}

.message-text :deep(.feishu-code-action-btn.copied) {
  background: #e8f5e9;
  color: #00b42a;
}

.message-text :deep(.feishu-code-action-btn svg) {
  flex-shrink: 0;
}

.message-text :deep(.feishu-code-theme .theme-icon-dark),
.message-text :deep(.feishu-code-theme .theme-icon-light) {
  width: 16px;
  height: 16px;
}

.message-text :deep(.feishu-code-content) {
  background: #1e1e1e;
  overflow-x: auto;
  position: relative;
}

.message-text :deep(.feishu-code-content.word-wrap) {
  overflow-x: visible;
}

.message-text :deep(.feishu-code-content.word-wrap .feishu-code-pre) {
  white-space: pre-wrap;
  word-wrap: break-word;
  word-break: break-all;
}

.message-text :deep(.feishu-code-pre) {
  margin: 0;
  padding: 16px 20px;
  background: transparent;
  font-family: 'Consolas', 'Monaco', 'Courier New', 'Fira Code', monospace;
  font-size: 13px;
  line-height: 1.6;
  color: #d4d4d4;
  overflow-x: auto;
  white-space: pre;
}

.message-text :deep(.feishu-code-pre code) {
  display: block;
  background: transparent;
  padding: 0;
  margin: 0;
  font-family: inherit;
  font-size: inherit;
  line-height: inherit;
}

.message-text :deep(.feishu-code-pre code.hljs) {
  background: transparent;
  color: inherit;
}

.message-text :deep(.feishu-code-pre .hljs) {
  background: transparent !important;
}

.message-text :deep(.feishu-code-content)::-webkit-scrollbar {
  height: 8px;
}

/* 深色主题滚动条 */
.message-text :deep(.feishu-code-block-wrapper.theme-dark .feishu-code-content)::-webkit-scrollbar-track {
  background: #2d2d2d;
}

.message-text :deep(.feishu-code-block-wrapper.theme-dark .feishu-code-content)::-webkit-scrollbar-thumb {
  background: #555;
  border-radius: 4px;
}

.message-text :deep(.feishu-code-block-wrapper.theme-dark .feishu-code-content)::-webkit-scrollbar-thumb:hover {
  background: #666;
}

/* 浅色主题滚动条 */
.message-text :deep(.feishu-code-block-wrapper.theme-light .feishu-code-content)::-webkit-scrollbar-track {
  background: #f0f0f0;
}

.message-text :deep(.feishu-code-block-wrapper.theme-light .feishu-code-content)::-webkit-scrollbar-thumb {
  background: #c9cdd4;
  border-radius: 4px;
}

.message-text :deep(.feishu-code-block-wrapper.theme-light .feishu-code-content)::-webkit-scrollbar-thumb:hover {
  background: #a8adb8;
}

/* 默认深色主题滚动条 */
.message-text :deep(.feishu-code-content)::-webkit-scrollbar-track {
  background: #2d2d2d;
}

.message-text :deep(.feishu-code-content)::-webkit-scrollbar-thumb {
  background: #555;
  border-radius: 4px;
}

.message-text :deep(.feishu-code-content)::-webkit-scrollbar-thumb:hover {
  background: #666;
}

.message-text :deep(.feishu-code-lang-dropdown)::-webkit-scrollbar {
  width: 6px;
}

.message-text :deep(.feishu-code-lang-dropdown)::-webkit-scrollbar-track {
  background: #f7f8fa;
}

.message-text :deep(.feishu-code-lang-dropdown)::-webkit-scrollbar-thumb {
  background: #c9cdd4;
  border-radius: 3px;
}

.message-text :deep(.feishu-code-lang-dropdown)::-webkit-scrollbar-thumb:hover {
  background: #a8adb8;
}


/* ========== 飞书风格电子表格 ========== */
.message-text :deep(.feishu-spreadsheet-wrapper) {
  margin: 20px 0;
  overflow-x: auto;
  border-radius: 8px;
  border: 1px solid #e5e6eb;
  background: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.message-text :deep(.feishu-spreadsheet) {
  width: 100%;
  border-collapse: collapse;
  margin: 0;
  font-size: 13px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
}

.message-text :deep(.feishu-spreadsheet th) {
  background: #f2f3f5;
  color: #1d2129;
  font-weight: 600;
  padding: 10px 12px;
  text-align: left;
  border: 1px solid #e5e6eb;
  border-right: 2px solid #d9dce0;
  border-bottom: 2px solid #d9dce0;
  position: sticky;
  top: 0;
  z-index: 10;
}

.message-text :deep(.feishu-spreadsheet td) {
  padding: 10px 12px;
  border: 1px solid #e5e6eb;
  color: #1d2129;
  background: #ffffff;
}

.message-text :deep(.feishu-spreadsheet tr:nth-child(even) td) {
  background: #fafbfc;
}

.message-text :deep(.feishu-spreadsheet tr:hover td) {
  background: #f2f3f5;
}

/* ========== 飞书风格引用块 ========== */
.message-text :deep(.feishu-quote-block) {
  margin: 20px 0;
  padding: 16px 20px;
  background: #f7f8fa;
  border-left: 4px solid #165dff;
  border-radius: 6px;
  display: flex;
  gap: 12px;
  position: relative;
}

.message-text :deep(.feishu-quote-icon) {
  width: 4px;
  background: #165dff;
  border-radius: 2px;
  flex-shrink: 0;
}

.message-text :deep(.feishu-quote-content) {
  flex: 1;
  color: #646a73;
  line-height: 1.6;
}

.message-text :deep(.feishu-quote-content p) {
  margin: 0;
}

.message-text :deep(.feishu-quote-content p + p) {
  margin-top: 8px;
}

/* ========== 飞书风格分隔线 ========== */
.message-text :deep(.feishu-divider) {
  margin: 24px 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.message-text :deep(.feishu-divider-line) {
  width: 100%;
  height: 1px;
  background: linear-gradient(to right, transparent, #e5e6eb, transparent);
}

/* ========== 飞书风格信息块 ========== */
.message-text :deep(.feishu-info-block),
.message-text :deep(.feishu-tip-block),
.message-text :deep(.feishu-success-block),
.message-text :deep(.feishu-warning-block),
.message-text :deep(.feishu-error-block),
.message-text :deep(.feishu-note-block),
.message-text :deep(.feishu-question-block) {
  margin: 20px 0;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.message-text :deep(.feishu-info-block) {
  background: #e8f4fd;
  border-color: #165dff;
}

.message-text :deep(.feishu-tip-block) {
  background: #e8f5e9;
  border-color: #00b42a;
}

.message-text :deep(.feishu-success-block) {
  background: #e8f5e9;
  border-color: #00b42a;
}

.message-text :deep(.feishu-warning-block) {
  background: #fff7e6;
  border-color: #ff7d00;
}

.message-text :deep(.feishu-error-block) {
  background: #ffece8;
  border-color: #f53f3f;
}

.message-text :deep(.feishu-note-block) {
  background: #f2f3f5;
  border-color: #86909c;
}

.message-text :deep(.feishu-question-block) {
  background: #f0f5ff;
  border-color: #722ed1;
}

.message-text :deep(.feishu-block-header) {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  font-weight: 600;
  font-size: 14px;
}

.message-text :deep(.feishu-info-block .feishu-block-header) {
  color: #165dff;
  background: rgba(22, 93, 255, 0.08);
}

.message-text :deep(.feishu-tip-block .feishu-block-header),
.message-text :deep(.feishu-success-block .feishu-block-header) {
  color: #00b42a;
  background: rgba(0, 180, 42, 0.08);
}

.message-text :deep(.feishu-warning-block .feishu-block-header) {
  color: #ff7d00;
  background: rgba(255, 125, 0, 0.08);
}

.message-text :deep(.feishu-error-block .feishu-block-header) {
  color: #f53f3f;
  background: rgba(245, 63, 63, 0.08);
}

.message-text :deep(.feishu-note-block .feishu-block-header) {
  color: #86909c;
  background: rgba(134, 144, 156, 0.08);
}

.message-text :deep(.feishu-question-block .feishu-block-header) {
  color: #722ed1;
  background: rgba(114, 46, 209, 0.08);
}

.message-text :deep(.feishu-block-icon) {
  font-size: 16px;
  line-height: 1;
}

.message-text :deep(.feishu-block-title) {
  font-size: 14px;
}

.message-text :deep(.feishu-block-content) {
  padding: 16px;
  color: #1d2129;
  line-height: 1.6;
}

.message-text :deep(.feishu-block-content p) {
  margin: 0;
}

.message-text :deep(.feishu-block-content p + p) {
  margin-top: 8px;
}

/* ========== 飞书风格折叠块 ========== */
.message-text :deep(.feishu-collapse-block) {
  margin: 20px 0;
  border: 1px solid #e5e6eb;
  border-radius: 8px;
  background: #ffffff;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.message-text :deep(.feishu-collapse-header) {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  cursor: pointer;
  user-select: none;
  background: #f7f8fa;
  transition: background-color 0.2s;
}

.message-text :deep(.feishu-collapse-header:hover) {
  background: #f2f3f5;
}

.message-text :deep(.feishu-collapse-icon) {
  width: 16px;
  height: 16px;
  color: #646a73;
  transition: transform 0.3s ease;
  flex-shrink: 0;
}

.message-text :deep(.feishu-collapse-block.collapsed .feishu-collapse-icon) {
  transform: rotate(0deg);
}

.message-text :deep(.feishu-collapse-block:not(.collapsed) .feishu-collapse-icon) {
  transform: rotate(90deg);
}

.message-text :deep(.feishu-collapse-title) {
  font-size: 14px;
  font-weight: 500;
  color: #1d2129;
  flex: 1;
}

.message-text :deep(.feishu-collapse-content) {
  padding: 16px;
  color: #646a73;
  line-height: 1.6;
  border-top: 1px solid #f2f3f5;
}

.message-text :deep(.feishu-collapse-content p) {
  margin: 0;
}

.message-text :deep(.feishu-collapse-content p + p) {
  margin-top: 8px;
}

.message-text :deep(.code-block)::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.3);
  border-radius: 5px;
}

.message-text :deep(.code-block)::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #2196f3, #42a5f5);
  border-radius: 5px;
  border: 2px solid rgba(0, 0, 0, 0.2);
}

.message-text :deep(.code-block)::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #19c37d, #10a37f);
}

.message-text strong {
  color: #1a1a1a;
  font-weight: 600;
  letter-spacing: -0.01em;
}

.message-text em {
  color: #404040;
  font-style: italic;
}

.message-text del {
  color: #8e8ea0;
  text-decoration: line-through;
  text-decoration-color: #ef4444;
}

.message-text u {
  text-decoration: underline;
  text-decoration-color: #2196f3;
  text-underline-offset: 2px;
}

/* 链接卡片样式 */
.message-text :deep(.link-card-wrapper) {
  display: block;
  margin: 20px 0;
  animation: linkCardSlideIn 0.4s cubic-bezier(0.16, 1, 0.3, 1) both;
}

.message-text :deep(.link-card) {
  display: flex;
  flex-direction: column;
  padding: 20px 24px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 50%, #ffffff 100%);
  border: 2px solid rgba(33, 150, 243, 0.15);
  border-radius: 14px;
  text-decoration: none;
  color: #353740;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  box-shadow: 
    0 4px 16px rgba(33, 150, 243, 0.12),
    0 2px 8px rgba(33, 150, 243, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
}

.message-text :deep(.link-card)::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #2196f3 0%, #42a5f5 50%, #2196f3 100%);
  background-size: 200% 100%;
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  animation: gradientShift 3s ease infinite;
}

.message-text :deep(.link-card)::after {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(33, 150, 243, 0.08) 0%, transparent 70%);
  opacity: 0;
  transition: opacity 0.4s;
  pointer-events: none;
}

.message-text :deep(.link-card):hover {
  transform: translateY(-4px);
  border-color: rgba(33, 150, 243, 0.3);
  box-shadow: 
    0 8px 24px rgba(33, 150, 243, 0.2),
    0 4px 12px rgba(33, 150, 243, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.95);
  background: linear-gradient(135deg, #ffffff 0%, #f0f7f5 50%, #ffffff 100%);
}

.message-text :deep(.link-card):hover::before {
  transform: scaleX(1);
}

.message-text :deep(.link-card):hover::after {
  opacity: 1;
}

.message-text :deep(.link-card-title) {
  font-size: 16px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 10px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  position: relative;
  z-index: 1;
  transition: color 0.3s;
}

.message-text :deep(.link-card):hover .link-card-title {
  color: #2196f3;
}

.message-text :deep(.link-card-meta) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 2px solid rgba(33, 150, 243, 0.1);
  position: relative;
  z-index: 1;
}

.message-text :deep(.link-card):hover .link-card-meta {
  border-top-color: rgba(33, 150, 243, 0.2);
}

.message-text :deep(.link-card-domain) {
  font-size: 13px;
  color: #565869;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: color 0.3s;
}

.message-text :deep(.link-card-domain)::before {
  content: '';
  display: inline-block;
  width: 16px;
  height: 16px;
  background: linear-gradient(135deg, #10a37f 0%, #19c37d 100%);
  border-radius: 4px;
  flex-shrink: 0;
  box-shadow: 
    0 2px 6px rgba(33, 150, 243, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  transition: transform 0.3s;
}

.message-text :deep(.link-card):hover .link-card-domain {
  color: #2196f3;
}

.message-text :deep(.link-card):hover .link-card-domain::before {
  transform: scale(1.1) rotate(5deg);
}

.message-text :deep(.link-card-icon) {
  width: 18px;
  height: 18px;
  color: #8e8ea0;
  opacity: 0.7;
  transition: all 0.3s;
  flex-shrink: 0;
}

.message-text :deep(.link-card):hover .link-card-icon {
  color: #2196f3;
  opacity: 1;
  transform: translate(3px, -3px) scale(1.1);
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
  color: #2196f3;
  text-decoration: none;
  border-bottom: 1px solid rgba(33, 150, 243, 0.3);
  transition: all 0.2s;
  padding-bottom: 1px;
}

.message-text :deep(a:not(.link-card)):hover {
  color: #0d8f6e;
  border-bottom-color: #2196f3;
  background: rgba(33, 150, 243, 0.08);
  padding: 0 2px;
  margin: 0 -2px;
  border-radius: 3px;
}

.message-text :deep(hr) {
  border: none;
  border-top: 1px solid #e0e0e0;
  margin: 32px 0;
  height: 1px;
}

.message-text :deep(table) {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  margin: 24px 0;
  font-size: 0.95em;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 
    0 4px 16px rgba(0, 0, 0, 0.08),
    0 2px 8px rgba(33, 150, 243, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  border: 2px solid rgba(33, 150, 243, 0.15);
}

.message-text :deep(table th),
.message-text :deep(table td) {
  border: 1px solid rgba(33, 150, 243, 0.15);
  padding: 14px 18px;
  text-align: left;
  transition: all 0.3s ease;
}

.message-text :deep(table th) {
  background: linear-gradient(135deg, #10a37f 0%, #19c37d 100%);
  font-weight: 700;
  color: #ffffff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  border-color: rgba(33, 150, 243, 0.3);
  position: relative;
}

.message-text :deep(table th)::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: rgba(255, 255, 255, 0.3);
}

.message-text :deep(table th:first-child) {
  border-top-left-radius: 8px;
}

.message-text :deep(table th:last-child) {
  border-top-right-radius: 8px;
}

.message-text :deep(table tr:nth-child(even) td) {
  background: linear-gradient(135deg, #f8f9fa 0%, #f0f7f5 100%);
}

.message-text :deep(table tr:nth-child(odd) td) {
  background: #ffffff;
}

.message-text :deep(table tr:hover td) {
  background: linear-gradient(135deg, #e8f5f0 0%, #e0f2eb 100%);
  transform: scale(1.01);
  box-shadow: inset 0 0 0 2px rgba(33, 150, 243, 0.1);
}

.message-text :deep(table tr:last-child td:first-child) {
  border-bottom-left-radius: 8px;
}

.message-text :deep(table tr:last-child td:last-child) {
  border-bottom-right-radius: 8px;
}

/* 表格中的代码块样式 */
.message-text :deep(table .table-code-block) {
  display: block;
  background: #f7f8fa;
  border: 1px solid #e5e6eb;
  border-radius: 4px;
  padding: 8px 12px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.6;
  color: #1d2129;
  white-space: pre;
  word-break: normal;
  max-width: 100%;
  overflow-x: auto;
  margin: 4px 0;
  min-width: 0;
}

.message-text :deep(table .table-code-block.hljs) {
  background: #f7f8fa;
  padding: 8px 12px;
}

.message-text :deep(table .table-code-block code) {
  background: transparent;
  padding: 0;
  border: none;
  font-size: inherit;
  font-family: inherit;
}

.message-text :deep(table td),
.message-text :deep(table th) {
  vertical-align: top;
}

/* 复制按钮包装器 */
.message-copy-wrapper {
  position: absolute;
  top: 8px;
  right: 8px;
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.2s, visibility 0.2s;
  z-index: 100;
  pointer-events: none;
}

/* 当复制按钮不在任务列表下方时，hover显示 */
.message-card:hover .message-copy-wrapper:not(.in-task-list) {
  opacity: 1;
  visibility: visible;
  pointer-events: auto;
}

/* 复制按钮在任务列表下方时的样式 */
.message-copy-wrapper.in-task-list {
  position: static;
  opacity: 1 !important; /* 始终显示 */
  visibility: visible !important; /* 始终显示 */
  pointer-events: auto;
  display: flex;
  justify-content: flex-end;
  padding: 8px 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  background: #ffffff; /* 与消息文本区域一致 */
}

/* 消息复制按钮 */
.message-copy-btn {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(0, 0, 0, 0.08);
  color: #646a73;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Microsoft YaHei', 'PingFang SC', 'Hiragino Sans GB', sans-serif;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  pointer-events: auto;
}

.message-copy-wrapper:not(.in-task-list) .message-copy-btn {
  position: relative;
}

.message-copy-btn:hover {
  background: rgba(255, 255, 255, 1);
  color: #1d2129;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-1px);
}

.message-copy-btn:active {
  background: rgba(0, 0, 0, 0.08);
  transform: scale(0.95);
}

.message.user .message-copy-btn {
  color: rgba(255, 255, 255, 0.8);
}

.message.user .message-copy-btn:hover {
  background: rgba(255, 255, 255, 0.15);
  color: white;
}

/* 导出按钮包装器（hover显示） */
.export-pdf-wrapper {
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.2s, visibility 0.2s;
  align-self: flex-start;
  margin-top: 4px;
}

/* 导出按钮样式 */
.export-pdf-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  font-size: 12px;
  color: #646a73;
  background: #f7f8fa;
  border: 1px solid #e5e6eb;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 500;
}

.export-pdf-btn:hover:not(:disabled) {
  background: #f2f3f5;
  border-color: #d9dce0;
  color: #1d2129;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.export-pdf-btn:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.export-pdf-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.export-pdf-btn svg {
  flex-shrink: 0;
  stroke-width: 2;
}

/* 图片渲染样式 */
.message-text :deep(.image-wrapper) {
  margin: 24px 0;
  text-align: center;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 
    0 4px 16px rgba(0, 0, 0, 0.1),
    0 2px 8px rgba(33, 150, 243, 0.08);
  border: 2px solid rgba(33, 150, 243, 0.1);
  background: #f8f9fa;
  transition: all 0.3s ease;
}

.message-text :deep(.image-wrapper):hover {
  box-shadow: 
    0 8px 24px rgba(0, 0, 0, 0.15),
    0 4px 12px rgba(33, 150, 243, 0.12);
  border-color: rgba(33, 150, 243, 0.2);
  transform: translateY(-2px);
}

.message-text :deep(.markdown-image) {
  max-width: 100%;
  height: auto;
  display: block;
  margin: 0;
  border-radius: 0;
}

.message-text :deep(.image-caption) {
  padding: 12px 16px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  color: #565869;
  font-size: 13px;
  font-weight: 500;
  text-align: center;
  border-top: 1px solid rgba(33, 150, 243, 0.1);
}

/* 自定义信息框样式 */
.message-text :deep(.info-box),
.message-text :deep(.tip-box),
.message-text :deep(.warning-box),
.message-text :deep(.error-box),
.message-text :deep(.note-box) {
  margin: 20px 0;
  padding: 16px 20px;
  border-radius: 10px;
  border-left: 4px solid;
  position: relative;
  box-shadow: 
    0 2px 8px rgba(0, 0, 0, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
}

.message-text :deep(.info-box) {
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  border-left-color: #2196f3;
  color: #0d47a1;
}

.message-text :deep(.tip-box) {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  border-left-color: #2196f3;
  color: #1b5e20;
}

.message-text :deep(.warning-box) {
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
  border-left-color: #ff9800;
  color: #e65100;
}

.message-text :deep(.error-box) {
  background: linear-gradient(135deg, #ffebee 0%, #ffcdd2 100%);
  border-left-color: #f44336;
  color: #b71c1c;
}

.message-text :deep(.note-box) {
  background: linear-gradient(135deg, #f3e5f5 0%, #e1bee7 100%);
  border-left-color: #9c27b0;
  color: #4a148c;
}

.message-text :deep(.info-box):hover,
.message-text :deep(.tip-box):hover,
.message-text :deep(.warning-box):hover,
.message-text :deep(.error-box):hover,
.message-text :deep(.note-box):hover {
  transform: translateX(4px);
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
}

/* 折叠块样式 */
.message-text :deep(.collapse-box) {
  margin: 20px 0;
  border: 2px solid rgba(33, 150, 243, 0.15);
  border-radius: 10px;
  overflow: hidden;
  background: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.message-text :deep(.collapse-header) {
  padding: 14px 18px;
  background: linear-gradient(135deg, rgba(33, 150, 243, 0.08) 0%, rgba(66, 165, 245, 0.06) 100%);
  cursor: pointer;
  font-weight: 600;
  color: #2196f3;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s ease;
  user-select: none;
}

.message-text :deep(.collapse-header):hover {
  background: linear-gradient(135deg, rgba(33, 150, 243, 0.12) 0%, rgba(66, 165, 245, 0.08) 100%);
}

.message-text :deep(.collapse-content) {
  padding: 16px 18px;
  display: none;
}

.message-text :deep(.collapse-box.open .collapse-content) {
  display: block;
}

.message-text :deep(.collapse-icon) {
  transition: transform 0.3s ease;
  font-size: 14px;
}

.message-text :deep(.collapse-box.open .collapse-icon) {
  transform: rotate(90deg);
}

/* 任务列表样式（checkbox） */
.message-text :deep(.task-list) {
  list-style: none;
  padding-left: 0;
}

.message-text :deep(.task-list-item) {
  display: flex;
  align-items: flex-start;
  margin: 8px 0;
  padding: 8px 12px;
  background: linear-gradient(135deg, rgba(33, 150, 243, 0.03) 0%, rgba(66, 165, 245, 0.02) 100%);
  border-radius: 6px;
  border-left: 3px solid rgba(33, 150, 243, 0.2);
}

.message-text :deep(.task-list-item input[type="checkbox"]) {
  margin-right: 10px;
  margin-top: 4px;
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: #2196f3;
}

.message-text :deep(.task-list-item.checked) {
  background: linear-gradient(135deg, rgba(16, 163, 127, 0.08) 0%, rgba(25, 195, 125, 0.05) 100%);
  border-left-color: #2196f3;
  text-decoration: line-through;
  opacity: 0.7;
}

.message-text :deep(blockquote) {
  border-left: 4px solid #2196f3;
  background: #f8f9fa;
  padding: 20px 24px;
  margin: 20px 0;
  border-radius: 6px;
  color: #404040;
  font-style: normal;
  line-height: 1.85;
  position: relative;
  box-shadow: 
    0 1px 3px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
}

.message-text :deep(blockquote):hover {
  background: #f0f2f5;
  border-left-color: #42a5f5;
  box-shadow: 
    0 2px 6px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
}

.message-text :deep(blockquote) p {
  margin: 0 0 12px 0;
}

.message-text :deep(blockquote) p:last-child {
  margin-bottom: 0;
}

/* 用户消息泡泡简化 */
.message.user .message-card {
  background: #f0f0f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  width: fit-content; /* 根据内容自适应宽度 */
  max-width: 100%; /* 不超过父容器 */
}

.message.user .message-text {
  background: #f0f0f0;
  color: #2d2d2d;
  position: relative;
  width: fit-content; /* 根据内容自适应宽度 */
  max-width: 100%; /* 不超过父容器 */
  padding: 16px 20px; /* 简化后的内边距 */
}

.message.user .message-text::before {
  display: none;
}

/* 用户消息简化样式 */
.message.user .message-text * {
  color: #2d2d2d;
}

.message.user .message-text h1,
.message.user .message-text h2,
.message.user .message-text h3,
.message.user .message-text h4 {
  color: #1a1a1a;
  border-color: rgba(0, 0, 0, 0.1);
}

.message.user .message-text :deep(a:not(.link-card)) {
  color: #2196f3;
  border-bottom-color: rgba(33, 150, 243, 0.3);
}

.streaming-indicator {
  display: flex;
  gap: 6px;
  padding: 12px 20px;
  align-items: center;
  justify-content: center;
  position: relative;
}

.streaming-pulse {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(33, 150, 243, 0.3), transparent);
  animation: pulse 2s ease-in-out infinite;
}

.typing-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2196f3, #42a5f5);
  animation: typing 1.4s infinite;
  box-shadow: 0 2px 6px rgba(33, 150, 243, 0.4);
  position: relative;
  z-index: 1;
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
    transform: translateY(20px) scale(0.96);
    filter: blur(3px);
  }
  60% {
    transform: translateY(-3px) scale(1.01);
    filter: blur(1px);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
    filter: blur(0);
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

@keyframes gradientShift {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
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
    transform: translateY(0) scale(1);
    opacity: 0.6;
  }
  30% {
    transform: translateY(-12px) scale(1.2);
    opacity: 1;
  }
}

@keyframes pulse {
  0%, 100% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.5;
  }
  50% {
    transform: translate(-50%, -50%) scale(1.5);
    opacity: 0;
  }
}

@keyframes avatarGlow {
  0%, 100% {
    opacity: 0.3;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(1.1);
  }
}

@keyframes iconFloat {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-3px) rotate(5deg);
  }
}

@keyframes messageCardSlideIn {
  from {
    opacity: 0;
    transform: translateY(16px) scale(0.97);
    filter: blur(2px);
  }
  60% {
    transform: translateY(-2px) scale(1.01);
    filter: blur(0.5px);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
    filter: blur(0);
  }
}

/* 消息泡泡内的任务列表样式 */
.message-task-list {
  margin: 0;
  border-radius: 0;
  border: none;
  max-width: 100%;
  width: 100%;
  box-shadow: none;
}

.message-task-list :deep(.message-task-box) {
  margin: 0;
  border-radius: 0;
  border: none;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  max-width: 100%;
  width: 100%;
  box-shadow: none;
  background: transparent;
}

/* 当消息泡泡内有任务列表时，调整任务列表的圆角 */
.message-card.has-task-list .message-task-list :deep(.message-task-box) {
  border-radius: 14px 14px 0 0; /* 只有顶部圆角 */
  border-bottom: none; /* 移除底部边框，由复制按钮区域提供 */
}

/* 当消息泡泡内有任务列表时，复制按钮区域在中间 */
.message-card.has-task-list .message-copy-wrapper.in-task-list {
  border-top: none; /* 移除顶部边框，与任务列表连接 */
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

/* 当消息泡泡内有任务列表时，调整消息文本的顶部圆角 */
.message-card.has-task-list .message-text {
  border-radius: 0 0 14px 14px; /* 只有底部圆角 */
  border-top: none; /* 移除顶部边框，与复制按钮区域连接 */
}

/* 任务列表头部样式调整 */
.message-task-list :deep(.task-box-header) {
  background: #f7f8fa;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

/* 任务列表内容区域样式调整 */
.message-task-list :deep(.task-box-content) {
  background: #ffffff;
}
</style>

