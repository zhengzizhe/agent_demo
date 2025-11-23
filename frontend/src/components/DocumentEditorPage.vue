<template>
  <Teleport to="body">
    <Transition name="editor-page">
      <div class="document-editor-page">
    <!-- 顶部导航栏（最小化） -->
    <div class="editor-header">
      <button @click="$emit('close')" class="back-btn">
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
          <path d="M12 4l-6 6 6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
      <div class="header-spacer"></div>
      <div class="save-status" :class="{ saved: isSaved }">
        <svg v-if="isSaved" width="14" height="14" viewBox="0 0 14 14" fill="none">
          <path d="M11.5 3.5L5.5 9.5L2.5 6.5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        {{ isSaved ? '已保存' : '保存中...' }}
      </div>
      <button @click="showShareDialog = true" class="header-btn-icon" title="分享">
        <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
          <path d="M9 2v4M9 12v4M4 9h4M10 9h4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          <circle cx="9" cy="9" r="2" stroke="currentColor" stroke-width="1.5" fill="none"/>
        </svg>
      </button>
      <button @click="exportDocument" class="header-btn-icon" title="导出">
        <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
          <path d="M9 2v8M5 7l4-4 4 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M2 12h14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
      </button>
    </div>


    <!-- 编辑器（包含三栏布局） -->
    <DocumentEditor
      :document-id="documentId"
      :initial-content="documentContent"
      :read-only="readOnly"
      :document-title="documentTitle"
      :cover-image="coverImage"
      :author="documentAuthor"
      :last-modified="lastModified"
      @update="handleUpdate"
      @save="handleSave"
      @cover-change="handleCoverChange"
      ref="editorRef"
    />

    <!-- 分享对话框（飞书风格） -->
    <ShareDialog
      :visible="showShareDialog"
      :document="{ id: documentId, name: documentTitle }"
      @update:visible="showShareDialog = $event"
      @save="saveShareSettings"
    />
    </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import DocumentEditor from './DocumentEditor.vue'
import ShareDialog from './ShareDialog.vue'
import { formatDate } from '../utils/format.js'

const props = defineProps({
  documentId: {
    type: String,
    required: true
  },
  document: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['close', 'update', 'save'])

const documentTitle = ref(props.document?.name || '未命名文档')
const editingTitle = ref('')
const isEditingTitle = ref(false)
const titleInputRef = ref(null)
const documentContent = ref(props.document?.content || '<p></p>')
const lastModified = ref(props.document?.updatedAt || new Date())
const documentAuthor = ref(props.document?.owner?.name || '未知用户')
const isSaved = ref(true)
const showShareDialog = ref(false)
const shareLink = ref('')
const sharePermission = ref('view')
const readOnly = ref(false)
const editorRef = ref(null)
const coverImage = ref(props.document?.coverImage || null)
const authorColor = ref('#165dff')

// 开始编辑标题
const startEditTitle = () => {
  if (readOnly.value) return
  editingTitle.value = documentTitle.value
  isEditingTitle.value = true
  nextTick(() => {
    titleInputRef.value?.focus()
    titleInputRef.value?.select()
  })
}

// 保存标题
const saveTitle = () => {
  if (editingTitle.value.trim()) {
    documentTitle.value = editingTitle.value.trim()
    emit('update', { name: documentTitle.value })
  }
  isEditingTitle.value = false
}

// 取消编辑标题
const cancelEditTitle = () => {
  isEditingTitle.value = false
}

// 处理内容更新
const handleUpdate = (content) => {
  documentContent.value = content
  isSaved.value = false
  emit('update', { content })
}

// 处理保存
const handleSave = (content) => {
  isSaved.value = true
  lastModified.value = new Date()
  emit('save', { content, name: documentTitle.value })
}

// 格式化时间
const formatTime = (time) => {
  return formatDate(time)
}

// 导出文档
const exportDocument = () => {
  const content = editorRef.value?.editor?.getHTML() || documentContent.value
  const blob = new Blob([`<html><head><meta charset="utf-8"><title>${documentTitle.value}</title></head><body>${content}</body></html>`], {
    type: 'text/html'
  })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${documentTitle.value}.html`
  a.click()
  URL.revokeObjectURL(url)
}

// 复制链接
const copyLink = () => {
  navigator.clipboard.writeText(shareLink.value)
  alert('链接已复制到剪贴板')
}

// 保存分享设置
const saveShareSettings = () => {
  // TODO: 调用 API 保存分享设置
  showShareDialog.value = false
}

// 设置封面图
const setCoverImage = () => {
  const url = window.prompt('请输入封面图片URL:')
  if (url) {
    coverImage.value = url
    emit('update', { coverImage: url })
  }
}

// 移除封面图
const removeCoverImage = () => {
  coverImage.value = null
  emit('update', { coverImage: null })
}

// 处理封面变化
const handleCoverChange = (url) => {
  coverImage.value = url
  emit('update', { coverImage: url })
}

// 生成作者颜色
const generateAuthorColor = (name) => {
  const colors = [
    '#165dff', '#10b981', '#f59e0b', '#ef4444',
    '#8b5cf6', '#06b6d4', '#ec4899', '#14b8a6'
  ]
  const index = (name?.charCodeAt(0) || 0) % colors.length
  return colors[index]
}

// 初始化分享链接
onMounted(() => {
  shareLink.value = `${window.location.origin}/doc/${props.documentId}`
  authorColor.value = generateAuthorColor(documentAuthor.value)
})
</script>

<style scoped>
.document-editor-page {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100vw;
  background: var(--theme-background-gradient, none);
  background-color: var(--theme-background, #f5f5f7);
  z-index: 10000;
  overflow: hidden;
  animation: pageFadeIn 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  backdrop-filter: blur(40px) saturate(200%);
  -webkit-backdrop-filter: blur(40px) saturate(200%);
}

.document-editor-page::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 30%, rgba(22, 93, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(139, 92, 246, 0.1) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

@keyframes pageFadeIn {
  0% {
    opacity: 0;
    transform: translateY(20px) scale(0.98);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.editor-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 24px;
  background: rgba(250, 250, 252, 0.8);
  backdrop-filter: blur(30px) saturate(180%);
  -webkit-backdrop-filter: blur(30px) saturate(180%);
  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  animation: headerSlideDown 0.3s cubic-bezier(0.16, 1, 0.3, 1) 0.1s both;
}

@keyframes headerSlideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.header-spacer {
  flex: 1;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  min-width: 0;
}

.back-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  border-radius: 8px;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.back-btn:hover {
  background: rgba(0, 0, 0, 0.05);
  color: #111827;
}

.document-info {
  flex: 1;
  min-width: 0;
}

/* 页面头部 */
.page-header {
  position: relative;
  margin-bottom: 0;
}

.page-cover {
  width: 100%;
  height: 280px;
  background: linear-gradient(135deg, rgba(22, 93, 255, 0.1) 0%, rgba(139, 92, 246, 0.1) 100%);
  background-size: cover;
  background-position: center;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: rgba(0, 0, 0, 0.4);
  cursor: pointer;
  padding: 24px;
  border-radius: 12px;
  transition: all 0.2s;
}

.cover-placeholder:hover {
  background: rgba(255, 255, 255, 0.5);
  color: rgba(0, 0, 0, 0.6);
}

.cover-remove-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(10px);
  border-radius: 8px;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.cover-remove-btn:hover {
  background: rgba(0, 0, 0, 0.7);
  transform: scale(1.1);
}

.page-info {
  max-width: 900px;
  margin: 0 auto;
  padding: 48px 80px 32px;
}

.page-title-section {
  margin-bottom: 24px;
}

.page-title {
  font-size: 48px;
  font-weight: 700;
  color: #111827;
  margin: 0;
  cursor: text;
  line-height: 1.2;
  letter-spacing: -0.02em;
  min-height: 60px;
  border: 2px solid transparent;
  border-radius: 8px;
  padding: 8px 0;
  transition: all 0.2s;
}

.page-title:hover {
  background: rgba(0, 0, 0, 0.02);
}

.title-input {
  font-size: 48px;
  font-weight: 700;
  color: #111827;
  border: 2px solid var(--theme-accent, #165dff);
  border-radius: 8px;
  padding: 8px 16px;
  outline: none;
  width: 100%;
  background: var(--theme-background, #ffffff);
  line-height: 1.2;
  letter-spacing: -0.02em;
}

.page-meta {
  display: flex;
  align-items: center;
  gap: 16px;
}

.meta-author {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
  font-weight: 600;
  flex-shrink: 0;
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.author-name {
  font-size: 14px;
  font-weight: 500;
  color: #111827;
}

.meta-time {
  font-size: 12px;
  color: #9ca3af;
}

.document-title:hover {
  background: rgba(0, 0, 0, 0.02);
  border-radius: 4px;
}

.title-input {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  border: 2px solid var(--theme-accent, #165dff);
  border-radius: 6px;
  padding: 4px 8px;
  outline: none;
  width: 100%;
  background: var(--theme-background, #ffffff);
}

.document-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #9ca3af;
}

.meta-item {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-btn-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  border-radius: 8px;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s;
}

.header-btn-icon:hover {
  background: rgba(0, 0, 0, 0.06);
  color: #111827;
}

.header-btn:hover {
  background: rgba(0, 0, 0, 0.02);
  border-color: rgba(0, 0, 0, 0.15);
  color: #111827;
}

.save-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #9ca3af;
  padding: 8px 16px;
  background: rgba(0, 0, 0, 0.02);
  border-radius: 8px;
  transition: all 0.2s;
}

.save-status.saved {
  color: #10b981;
  background: rgba(16, 185, 129, 0.1);
}

.save-status svg {
  flex-shrink: 0;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  backdrop-filter: blur(4px);
}

.modal-content {
  background: var(--theme-background, #ffffff);
  border-radius: 16px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: modalSlideUp 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes modalSlideUp {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.modal-close {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  border-radius: 8px;
  color: #6b7280;
  font-size: 24px;
  line-height: 1;
  cursor: pointer;
  transition: all 0.2s;
}

.modal-close:hover {
  background: rgba(0, 0, 0, 0.05);
}

.modal-body {
  padding: 24px;
  overflow-y: auto;
}

.share-option {
  margin-bottom: 24px;
}

.share-option:last-child {
  margin-bottom: 0;
}

.share-option label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 8px;
  color: #374151;
}

.share-link {
  display: flex;
  gap: 8px;
}

.link-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 6px;
  font-size: 14px;
  background: rgba(0, 0, 0, 0.02);
}

.copy-btn {
  padding: 8px 16px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  background: var(--theme-background, #ffffff);
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.copy-btn:hover {
  background: rgba(0, 0, 0, 0.02);
}

.permission-select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 6px;
  font-size: 14px;
  background: var(--theme-background, #ffffff);
  cursor: pointer;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
}

.btn-secondary,
.btn-primary {
  padding: 10px 20px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.btn-secondary {
  background: rgba(0, 0, 0, 0.05);
  color: #374151;
}

.btn-secondary:hover {
  background: rgba(0, 0, 0, 0.08);
}

.btn-primary {
  background: var(--theme-accent, #165dff);
  color: white;
}

.btn-primary:hover {
  opacity: 0.9;
}

/* 编辑器页面过渡动画 - 优化性能 */
.editor-page-enter-active {
  transition: opacity 0.35s cubic-bezier(0.16, 1, 0.3, 1), transform 0.35s cubic-bezier(0.16, 1, 0.3, 1);
  will-change: opacity, transform;
}

.editor-page-leave-active {
  transition: opacity 0.25s cubic-bezier(0.4, 0, 1, 1), transform 0.25s cubic-bezier(0.4, 0, 1, 1);
  will-change: opacity, transform;
}

.editor-page-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.98);
}

.editor-page-enter-to {
  opacity: 1;
  transform: translateY(0) scale(1);
}

.editor-page-leave-to {
  opacity: 0;
  transform: translateY(15px) scale(0.99);
}
</style>

