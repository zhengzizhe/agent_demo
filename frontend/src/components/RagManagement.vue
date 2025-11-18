<template>
  <div class="flowus-page">
    <!-- 左侧边栏 -->
    <div class="flowus-sidebar">
      <div class="sidebar-header">
        <h2 class="sidebar-title">知识库</h2>
        <button class="sidebar-add-btn" @click="showCreateRagDialog = true" title="新建知识库">
          <span class="add-icon">+</span>
        </button>
      </div>
      <div class="sidebar-content">
        <div
          v-for="rag in ragList"
          :key="rag.id"
          class="sidebar-item"
          :class="{ active: selectedRagId === rag.id }"
          @click="selectRag(rag.id)"
        >
          <div class="sidebar-item-icon">📚</div>
          <div class="sidebar-item-content">
            <div class="sidebar-item-name">{{ rag.name }}</div>
            <div class="sidebar-item-meta">{{ getRagDocumentCount(rag.id) }} 个文档</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="flowus-main">
      <!-- 顶部工具栏 -->
      <div class="flowus-toolbar">
        <div class="toolbar-left">
          <h1 class="page-title">{{ getCurrentRagName() }}</h1>
          <div class="toolbar-stats">
            <span class="stat-badge">{{ documents.length }} 个文档块</span>
          </div>
        </div>
        <div class="toolbar-right">
          <button class="toolbar-btn" @click="showSearchPanel = !showSearchPanel">
            <span class="toolbar-icon">🔍</span>
            <span>搜索</span>
          </button>
          <button class="toolbar-btn" @click="showAddPanel = !showAddPanel">
            <span class="toolbar-icon">+</span>
            <span>添加文档</span>
          </button>
          <button class="toolbar-btn primary" @click="showChatPanel = !showChatPanel">
            <span class="toolbar-icon">💬</span>
            <span>AI对话</span>
          </button>
        </div>
      </div>

      <!-- 搜索面板 -->
      <transition name="slide-down">
        <div v-if="showSearchPanel" class="flowus-panel search-panel">
          <div class="panel-header">
            <h3>搜索文档</h3>
            <button class="panel-close" @click="showSearchPanel = false">×</button>
          </div>
          <div class="panel-content">
            <div class="search-input-wrapper">
              <input
                v-model="searchQuery"
                type="text"
                class="flowus-input"
                placeholder="输入搜索关键词..."
                @keyup.enter="searchDocuments"
              />
              <button class="search-action-btn" @click="searchDocuments" :disabled="!searchQuery.trim() || loading">
                搜索
              </button>
            </div>
            <div v-if="searchResults.length > 0" class="search-results">
              <div class="results-title">找到 {{ searchResults.length }} 条结果</div>
              <div class="results-list">
                <div
                  v-for="(result, index) in searchResults"
                  :key="index"
                  class="result-card"
                >
                  <div class="result-content">{{ result.content }}</div>
                  <div v-if="result.metadata" class="result-meta">
                    <span v-if="result.metadata.fileName">📄 {{ result.metadata.fileName }}</span>
                    <span v-if="result.metadata.chunkIndex">块 {{ result.metadata.chunkIndex }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </transition>

      <!-- 添加文档面板 -->
      <transition name="slide-down">
        <div v-if="showAddPanel" class="flowus-panel add-panel">
          <div class="panel-header">
            <h3>添加文档</h3>
            <button class="panel-close" @click="showAddPanel = false">×</button>
          </div>
          <div class="panel-content">
            <div class="add-tabs">
              <button
                class="add-tab"
                :class="{ active: addMode === 'text' }"
                @click="addMode = 'text'"
              >
                文本输入
              </button>
              <button
                class="add-tab"
                :class="{ active: addMode === 'file' }"
                @click="addMode = 'file'"
              >
                文件上传
              </button>
            </div>

            <!-- 文本输入模式 -->
            <div v-if="addMode === 'text'" class="add-content">
              <textarea
                v-model="documentText"
                class="flowus-textarea"
                placeholder="请输入文档内容..."
                rows="10"
              ></textarea>
              <div class="add-actions">
                <button
                  class="flowus-btn secondary"
                  @click="documentText = ''"
                  :disabled="loading"
                >
                  清空
                </button>
                <button
                  class="flowus-btn primary"
                  @click="addDocument"
                  :disabled="!documentText.trim() || loading"
                >
                  添加文档
                </button>
              </div>
            </div>

            <!-- 文件上传模式 -->
            <div v-if="addMode === 'file'" class="add-content">
              <div class="file-upload-zone" @click="triggerFileInput" @dragover.prevent @drop.prevent="handleDrop">
                <input
                  ref="fileInput"
                  type="file"
                  class="file-input"
                  @change="handleFileSelect"
                  multiple
                />
                <div class="upload-content">
                  <div class="upload-icon">📎</div>
                  <p class="upload-text">点击选择文件或拖拽文件到此处</p>
                  <p class="upload-hint">支持文本文件 (.txt, .md, .json 等)</p>
                </div>
              </div>
              <div v-if="selectedFiles.length > 0" class="selected-files-list">
                <div
                  v-for="(file, index) in selectedFiles"
                  :key="index"
                  class="file-tag"
                >
                  <span class="file-tag-name">{{ file.name }}</span>
                  <span class="file-tag-size">({{ formatFileSize(file.size) }})</span>
                  <button class="file-tag-remove" @click="removeFile(index)">×</button>
                </div>
              </div>
              <div class="add-actions">
                <button
                  class="flowus-btn secondary"
                  @click="clearFiles"
                  :disabled="loading"
                >
                  清空
                </button>
                <button
                  class="flowus-btn primary"
                  @click="uploadFiles"
                  :disabled="selectedFiles.length === 0 || loading"
                >
                  上传文件
                </button>
              </div>
            </div>
          </div>
        </div>
      </transition>

      <!-- 文档列表 -->
      <div class="flowus-content">
        <!-- 加载状态 -->
        <div v-if="loading" class="flowus-loading">
          <div class="loading-spinner"></div>
          <p>加载中...</p>
        </div>

        <!-- 空状态 -->
        <div v-else-if="documents.length === 0" class="flowus-empty">
          <div class="empty-icon">📄</div>
          <h3 class="empty-title">知识库为空</h3>
          <p class="empty-desc">添加第一个文档开始使用</p>
          <button class="flowus-btn primary" @click="showAddPanel = true">
            添加文档
          </button>
        </div>

        <!-- 文档卡片列表 -->
        <div v-else class="documents-grid">
          <div
            v-for="(doc, index) in documents"
            :key="index"
            class="document-card"
          >
            <div class="card-header">
              <div class="card-title">文档块 #{{ index + 1 }}</div>
              <button
                class="card-action"
                @click="deleteDocument(doc.id)"
                title="删除文档"
              >
                <span class="action-icon">🗑️</span>
              </button>
            </div>
            <div class="card-content">{{ doc.content }}</div>
            <div v-if="doc.metadata" class="card-footer">
              <span v-if="doc.metadata.fileName" class="card-meta">📄 {{ doc.metadata.fileName }}</span>
              <span v-if="doc.metadata.chunkIndex" class="card-meta">块 {{ doc.metadata.chunkIndex }}</span>
              <span v-if="doc.metadata.fileSize" class="card-meta">大小: {{ formatFileSize(doc.metadata.fileSize) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- AI对话侧拉面板 -->
    <transition name="slide-right">
      <div v-if="showChatPanel" class="chat-drawer-overlay" @click="showChatPanel = false">
        <div class="chat-drawer" @click.stop>
          <div class="chat-drawer-header">
            <h3 class="chat-drawer-title">AI 对话助手</h3>
            <button class="chat-drawer-close" @click="showChatPanel = false">×</button>
          </div>
          <div class="chat-drawer-content">
            <div class="chat-messages" ref="chatMessagesRef">
              <div v-if="chatMessages.length === 0" class="chat-empty">
                <div class="chat-empty-icon">💬</div>
                <p class="chat-empty-text">开始与AI对话，询问知识库相关问题</p>
              </div>
              <div
                v-for="(msg, index) in chatMessages"
                :key="index"
                :class="['chat-message', msg.role]"
              >
                <div v-if="msg.role === 'assistant'" class="chat-avatar">🤖</div>
                <div class="chat-bubble" :class="msg.role">
                  <div class="chat-text">{{ msg.content }}</div>
                  <div v-if="msg.streaming" class="chat-streaming">
                    <span class="streaming-dot"></span>
                    <span class="streaming-dot"></span>
                    <span class="streaming-dot"></span>
                  </div>
                </div>
                <div v-if="msg.role === 'user'" class="chat-avatar">👤</div>
              </div>
            </div>
            <div class="chat-input-area">
              <textarea
                v-model="chatInput"
                class="chat-input"
                placeholder="输入您的问题..."
                rows="1"
                @keydown.enter.exact.prevent="sendChatMessage"
                @keydown.shift.enter.exact="handleShiftEnter"
                :disabled="chatLoading"
                ref="chatInputRef"
              ></textarea>
              <button
                class="chat-send-btn"
                @click="sendChatMessage"
                :disabled="!chatInput.trim() || chatLoading"
              >
                <span v-if="chatLoading">发送中...</span>
                <span v-else>发送</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick, watch } from 'vue'

const selectedRagId = ref(1)
const ragList = ref([
  { id: 1, name: 'Default RAG' },
  { id: 2, name: 'Research RAG' }
])
const documents = ref([])
const searchResults = ref([])
const documentText = ref('')
const searchQuery = ref('')
const addMode = ref('text')
const selectedFiles = ref([])
const fileInput = ref(null)
const loading = ref(false)
const showSearchPanel = ref(false)
const showAddPanel = ref(false)
const showCreateRagDialog = ref(false)
const showChatPanel = ref(false)
const chatMessages = ref([])
const chatInput = ref('')
const chatLoading = ref(false)
const chatMessagesRef = ref(null)
const chatInputRef = ref(null)

// 获取当前知识库名称
const getCurrentRagName = () => {
  const rag = ragList.value.find(r => r.id === selectedRagId.value)
  return rag ? rag.name : '知识库'
}

// 获取知识库文档数量
const getRagDocumentCount = (ragId) => {
  if (ragId === selectedRagId.value) {
    return documents.value.length
  }
  return 0
}

// 选择知识库
const selectRag = (ragId) => {
  selectedRagId.value = ragId
  loadDocuments()
  showSearchPanel.value = false
  showAddPanel.value = false
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

// 触发文件选择
const triggerFileInput = () => {
  fileInput.value?.click()
}

// 处理文件选择
const handleFileSelect = (event) => {
  const files = Array.from(event.target.files)
  selectedFiles.value.push(...files)
}

// 处理拖拽
const handleDrop = (event) => {
  const files = Array.from(event.dataTransfer.files)
  selectedFiles.value.push(...files)
}

// 移除文件
const removeFile = (index) => {
  selectedFiles.value.splice(index, 1)
}

// 清空文件
const clearFiles = () => {
  selectedFiles.value = []
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

// 添加文档
const addDocument = async () => {
  if (!documentText.value.trim()) return
  
  loading.value = true
  try {
    const response = await fetch(`/rag/${selectedRagId.value}/documents`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        text: documentText.value,
        metadata: {
          source: 'manual_input',
          createdAt: Date.now()
        }
      })
    })
    
    const result = await response.json()
    if (result.success) {
      alert(`文档添加成功！共生成 ${result.segmentCount} 个文档块`)
      documentText.value = ''
      showAddPanel.value = false
      loadDocuments()
    } else {
      alert('添加失败: ' + result.message)
    }
  } catch (error) {
    console.error('添加文档失败:', error)
    alert('添加失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 上传文件
const uploadFiles = async () => {
  if (selectedFiles.value.length === 0) return
  
  loading.value = true
  try {
    for (const file of selectedFiles.value) {
      const formData = new FormData()
      formData.append('file', file)
      
      const response = await fetch(`/rag/${selectedRagId.value}/documents/upload`, {
        method: 'POST',
        body: formData
      })
      
      const result = await response.json()
      if (!result.success) {
        alert(`文件 ${file.name} 上传失败: ${result.message}`)
      }
    }
    
    alert('文件上传完成！')
    clearFiles()
    showAddPanel.value = false
    loadDocuments()
  } catch (error) {
    console.error('上传文件失败:', error)
    alert('上传失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 搜索文档
const searchDocuments = async () => {
  if (!searchQuery.value.trim()) return
  
  loading.value = true
  try {
    const response = await fetch('/rag/search', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        ragId: selectedRagId.value,
        queryText: searchQuery.value,
        limit: 10,
        similarityThreshold: 0.6
      })
    })
    
    const result = await response.json()
    if (result.success) {
      searchResults.value = result.results || []
    } else {
      alert('搜索失败: ' + result.message)
    }
  } catch (error) {
    console.error('搜索文档失败:', error)
    alert('搜索失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 加载文档列表
const loadDocuments = async () => {
  loading.value = true
  try {
    const response = await fetch(`/rag/documents/${selectedRagId.value}`)
    const result = await response.json()
    if (result.success) {
      documents.value = result.documents || []
    } else {
      console.error('加载文档列表失败:', result.message)
      documents.value = []
    }
  } catch (error) {
    console.error('加载文档列表失败:', error)
    documents.value = []
  } finally {
    loading.value = false
  }
}

// 删除文档
const deleteDocument = async (docId) => {
  if (!confirm('确定要删除这个文档吗？')) return
  
  loading.value = true
  try {
    const response = await fetch(`/rag/documents/doc/${docId}`, {
      method: 'DELETE'
    })
    const result = await response.json()
    if (result.success) {
      alert('文档删除成功')
      loadDocuments()
    } else {
      alert('删除失败: ' + result.message)
    }
  } catch (error) {
    console.error('删除文档失败:', error)
    alert('删除失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 处理Shift+Enter换行
const handleShiftEnter = () => {
  // Shift+Enter 换行，不需要特殊处理
}

// 自动调整输入框高度
watch(() => chatInput.value, () => {
  nextTick(() => {
    if (chatInputRef.value) {
      chatInputRef.value.style.height = 'auto'
      chatInputRef.value.style.height = chatInputRef.value.scrollHeight + 'px'
    }
  })
})

// 发送聊天消息
const sendChatMessage = async () => {
  if (!chatInput.value.trim() || chatLoading.value) return

  const userMessage = chatInput.value.trim()
  
  // 添加用户消息
  chatMessages.value.push({
    role: 'user',
    content: userMessage
  })

  // 添加AI回复占位
  const aiMessageIndex = chatMessages.value.length
  chatMessages.value.push({
    role: 'assistant',
    content: '',
    streaming: true
  })

  chatInput.value = ''
  chatLoading.value = true

  // 滚动到底部
  await nextTick()
  scrollChatToBottom()

  try {
    // 调用RAG搜索API，基于知识库回答问题
    const response = await fetch('/rag/search', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        ragId: selectedRagId.value,
        queryText: userMessage,
        limit: 5,
        similarityThreshold: 0.5
      })
    })

    const result = await response.json()
    
    if (result.success && result.results && result.results.length > 0) {
      // 基于搜索结果生成回答
      const relevantDocs = result.results.map(r => r.content).join('\n\n')
      const aiResponse = `基于知识库内容，我找到以下相关信息：\n\n${relevantDocs}\n\n如果您需要更详细的信息，请告诉我具体的问题。`
      
      chatMessages.value[aiMessageIndex] = {
        role: 'assistant',
        content: aiResponse,
        streaming: false
      }
    } else {
      chatMessages.value[aiMessageIndex] = {
        role: 'assistant',
        content: '抱歉，我在知识库中没有找到相关信息。请尝试使用其他关键词搜索，或者添加更多文档到知识库。',
        streaming: false
      }
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    chatMessages.value[aiMessageIndex] = {
      role: 'assistant',
      content: '抱歉，发生了错误：' + error.message,
      streaming: false
    }
  } finally {
    chatLoading.value = false
    await nextTick()
    scrollChatToBottom()
  }
}

// 滚动聊天到底部
const scrollChatToBottom = () => {
  if (chatMessagesRef.value) {
    chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
  }
}

// 初始化
onMounted(() => {
  loadDocuments()
})
</script>

<style scoped>
/* 知识库主容器 - 与应用整体风格一致 */
.flowus-page {
  display: flex;
  height: 100vh;
  background: linear-gradient(135deg, #ffffff 0%, #f7f7f8 50%, #ffffff 100%);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Helvetica Neue', Arial, sans-serif;
}

/* 左侧边栏 */
.flowus-sidebar {
  width: 260px;
  background: linear-gradient(135deg, #ffffff 0%, #fafafa 50%, #ffffff 100%);
  border-right: 1px solid rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.04);
}

.sidebar-header {
  padding: 20px 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.sidebar-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #202123;
}

.sidebar-add-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #202123;
}

.sidebar-add-btn:hover {
  background: rgba(0, 0, 0, 0.1);
  border-color: rgba(0, 0, 0, 0.2);
  transform: scale(1.05);
}

.add-icon {
  font-size: 20px;
  line-height: 1;
}

.sidebar-content {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.sidebar-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.15s ease;
  margin-bottom: 2px;
}

.sidebar-item:hover {
  background: rgba(0, 0, 0, 0.05);
}

.sidebar-item.active {
  background: rgba(0, 0, 0, 0.08);
  border-left: 3px solid #202123;
  padding-left: 7px;
}

.sidebar-item-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.sidebar-item-content {
  flex: 1;
  min-width: 0;
}

.sidebar-item-name {
  font-size: 14px;
  font-weight: 600;
  color: #202123;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar-item.active .sidebar-item-name {
  color: #202123;
  font-weight: 700;
}

.sidebar-item-meta {
  font-size: 12px;
  color: #9b9a97;
  margin-top: 2px;
}

/* 主内容区 */
.flowus-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: linear-gradient(135deg, #ffffff 0%, #fafafa 50%, #ffffff 100%);
}

/* 顶部工具栏 */
.flowus-toolbar {
  padding: 16px 24px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #ffffff 0%, #fafafa 50%, #ffffff 100%);
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #202123;
}

.toolbar-stats {
  display: flex;
  gap: 8px;
}

.stat-badge {
  padding: 6px 12px;
  background: rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  color: #202123;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.toolbar-right {
  display: flex;
  gap: 8px;
}

.toolbar-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  background: #ffffff;
  border: 2px solid rgba(0, 0, 0, 0.1);
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  color: #565869;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
}

.toolbar-btn:hover {
  background: rgba(0, 0, 0, 0.05);
  border-color: rgba(0, 0, 0, 0.15);
  color: #202123;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.08);
}

.toolbar-btn.primary {
  background: #202123;
  color: #ffffff;
  border-color: transparent;
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.15),
    0 2px 4px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.toolbar-btn.primary:hover {
  background: #000000;
  box-shadow: 
    0 6px 16px rgba(0, 0, 0, 0.2),
    0 3px 6px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.15);
}

.toolbar-icon {
  font-size: 16px;
}

/* 面板 */
.flowus-panel {
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  background: linear-gradient(135deg, #ffffff 0%, #fafafa 50%, #ffffff 100%);
  animation: slideDown 0.2s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.panel-header {
  padding: 16px 24px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #202123;
}

.panel-close {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: 4px;
  font-size: 20px;
  color: #787774;
  cursor: pointer;
  transition: all 0.15s ease;
}

.panel-close:hover {
  background: rgba(0, 0, 0, 0.05);
  color: #202123;
}

.panel-content {
  padding: 24px;
}

/* 搜索面板 */
.search-input-wrapper {
  display: flex;
  gap: 12px;
}

.flowus-input {
  flex: 1;
  padding: 12px 16px;
  font-size: 14px;
  border: 2px solid rgba(0, 0, 0, 0.1);
  border-radius: 10px;
  background: #ffffff;
  color: #202123;
  transition: all 0.3s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.flowus-input:focus {
  outline: none;
  border-color: #202123;
  box-shadow: 
    0 0 0 3px rgba(0, 0, 0, 0.05),
    0 2px 6px rgba(0, 0, 0, 0.08);
}

.search-action-btn {
  padding: 12px 24px;
  font-size: 14px;
  font-weight: 600;
  background: #202123;
  color: #ffffff;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.15),
    0 2px 4px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.search-action-btn:hover:not(:disabled) {
  background: #000000;
  transform: translateY(-2px);
  box-shadow: 
    0 6px 16px rgba(0, 0, 0, 0.2),
    0 3px 6px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.15);
}

.search-action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.search-results {
  margin-top: 20px;
}

.results-title {
  font-size: 14px;
  font-weight: 500;
  color: #787774;
  margin-bottom: 12px;
}

.results-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.result-card {
  padding: 14px 16px;
  background: linear-gradient(135deg, #ffffff 0%, #fafafa 50%, #ffffff 100%);
  border: 2px solid rgba(0, 0, 0, 0.08);
  border-radius: 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.result-card:hover {
  border-color: rgba(0, 0, 0, 0.15);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.result-content {
  font-size: 14px;
  color: #202123;
  line-height: 1.6;
  margin-bottom: 8px;
}

.result-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #9b9a97;
}

/* 添加文档面板 */
.add-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
}

.add-tab {
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 600;
  background: #ffffff;
  border: 2px solid rgba(0, 0, 0, 0.1);
  border-radius: 10px;
  color: #565869;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.add-tab:hover {
  background: rgba(0, 0, 0, 0.05);
  border-color: rgba(0, 0, 0, 0.15);
  color: #202123;
}

.add-tab.active {
  background: #202123;
  color: #ffffff;
  border-color: transparent;
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.15),
    0 2px 4px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.add-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.flowus-textarea {
  width: 100%;
  padding: 14px 16px;
  font-size: 14px;
  border: 2px solid rgba(0, 0, 0, 0.1);
  border-radius: 10px;
  background: #ffffff;
  color: #202123;
  font-family: inherit;
  resize: vertical;
  transition: all 0.3s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.flowus-textarea:focus {
  outline: none;
  border-color: #202123;
  box-shadow: 
    0 0 0 3px rgba(0, 0, 0, 0.05),
    0 2px 6px rgba(0, 0, 0, 0.08);
}

.file-upload-zone {
  position: relative;
  border: 2px dashed rgba(0, 0, 0, 0.2);
  border-radius: 12px;
  padding: 40px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(0, 0, 0, 0.02);
}

.file-upload-zone:hover {
  border-color: rgba(0, 0, 0, 0.3);
  background: rgba(0, 0, 0, 0.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.file-input {
  display: none;
}

.upload-content {
  pointer-events: none;
}

.upload-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.upload-text {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 500;
  color: #202123;
}

.upload-hint {
  margin: 0;
  font-size: 12px;
  color: #9b9a97;
}

.selected-files-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.file-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  background: rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 4px;
  font-size: 13px;
}

.file-tag-name {
  color: #202123;
}

.file-tag-size {
  color: #9b9a97;
}

.file-tag-remove {
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: 2px;
  font-size: 16px;
  color: #9b9a97;
  cursor: pointer;
  transition: all 0.15s ease;
}

.file-tag-remove:hover {
  background: rgba(0, 0, 0, 0.1);
  color: #202123;
}

.add-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.flowus-btn {
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  border: 1px solid #e5e5e5;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.flowus-btn.primary {
  background: #202123;
  color: #ffffff;
  border-color: transparent;
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.15),
    0 2px 4px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.flowus-btn.primary:hover:not(:disabled) {
  background: #000000;
  transform: translateY(-2px);
  box-shadow: 
    0 6px 16px rgba(0, 0, 0, 0.2),
    0 3px 6px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.15);
}

.flowus-btn.secondary {
  background: #ffffff;
  color: #202123;
  border-color: rgba(0, 0, 0, 0.1);
}

.flowus-btn.secondary:hover:not(:disabled) {
  background: rgba(0, 0, 0, 0.05);
  border-color: rgba(0, 0, 0, 0.15);
}

.flowus-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 内容区 */
.flowus-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: linear-gradient(135deg, #ffffff 0%, #f7f7f8 50%, #ffffff 100%);
}

/* 加载状态 */
.flowus-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #787774;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid rgba(0, 0, 0, 0.1);
  border-top-color: #202123;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.flowus-loading p {
  margin: 0;
  font-size: 14px;
}

/* 空状态 */
.flowus-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-title {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #37352f;
}

.empty-desc {
  margin: 0 0 20px 0;
  font-size: 14px;
  color: #787774;
}

/* 文档卡片网格 */
.documents-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.document-card {
  background: linear-gradient(135deg, #ffffff 0%, #fafafa 50%, #ffffff 100%);
  border: 2px solid rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  padding: 16px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
}

.document-card:hover {
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.08),
    0 2px 6px rgba(0, 0, 0, 0.05);
  border-color: rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #202123;
}

.card-action {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  opacity: 0;
  transition: all 0.15s ease;
}

.document-card:hover .card-action {
  opacity: 1;
}

.card-action:hover {
  background: #f1f1ef;
}

.action-icon {
  font-size: 14px;
}

.card-content {
  font-size: 14px;
  color: #202123;
  line-height: 1.6;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-footer {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #f1f1ef;
}

.card-meta {
  font-size: 12px;
  color: #9b9a97;
}

/* 过渡动画 */
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.2s ease;
}

.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* AI对话侧拉面板 */
.chat-drawer-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  backdrop-filter: blur(2px);
}

.chat-drawer {
  width: 480px;
  max-width: 90vw;
  height: 100%;
  background: linear-gradient(135deg, #ffffff 0%, #fafafa 50%, #ffffff 100%);
  box-shadow: -4px 0 24px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-drawer-header {
  padding: 20px 24px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #ffffff 0%, #fafafa 50%, #ffffff 100%);
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.chat-drawer-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #202123;
}

.chat-drawer-close {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: 4px;
  font-size: 24px;
  color: #787774;
  cursor: pointer;
  transition: all 0.15s ease;
}

.chat-drawer-close:hover {
  background: rgba(0, 0, 0, 0.05);
  color: #202123;
}

.chat-drawer-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f7f7f5;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chat-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
  color: #9b9a97;
}

.chat-empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.chat-empty-text {
  margin: 0;
  font-size: 14px;
}

.chat-message {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  animation: messageFadeIn 0.3s ease;
}

.chat-message.user {
  flex-direction: row-reverse;
}

.chat-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
  background: #f1f1ef;
}

.chat-bubble {
  max-width: 75%;
  padding: 12px 16px;
  border-radius: 12px;
  word-wrap: break-word;
  line-height: 1.5;
  font-size: 14px;
}

.chat-bubble.user {
  background: #202123;
  color: #ffffff;
  border-bottom-right-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.chat-bubble.assistant {
  background: linear-gradient(135deg, #ffffff 0%, #fafafa 50%, #ffffff 100%);
  color: #202123;
  border: 2px solid rgba(0, 0, 0, 0.08);
  border-bottom-left-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
}

.chat-text {
  white-space: pre-wrap;
  word-break: break-word;
}

.chat-streaming {
  display: flex;
  gap: 4px;
  margin-top: 8px;
  align-items: center;
}

.streaming-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #9b9a97;
  animation: streamingDot 1.4s ease-in-out infinite;
}

.streaming-dot:nth-child(2) {
  animation-delay: 0.2s;
}

.streaming-dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes streamingDot {
  0%, 60%, 100% {
    opacity: 0.3;
    transform: scale(0.8);
  }
  30% {
    opacity: 1;
    transform: scale(1.2);
  }
}

.chat-input-area {
  padding: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
  background: linear-gradient(135deg, #ffffff 0%, #fafafa 50%, #ffffff 100%);
  display: flex;
  gap: 12px;
  align-items: flex-end;
  flex-shrink: 0;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.04);
}

.chat-input {
  flex: 1;
  padding: 12px 16px;
  font-size: 14px;
  border: 2px solid rgba(0, 0, 0, 0.1);
  border-radius: 10px;
  background: #ffffff;
  color: #202123;
  font-family: inherit;
  resize: none;
  max-height: 120px;
  overflow-y: auto;
  line-height: 1.5;
  transition: all 0.3s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.chat-input:focus {
  outline: none;
  border-color: #202123;
  box-shadow: 
    0 0 0 3px rgba(0, 0, 0, 0.05),
    0 2px 6px rgba(0, 0, 0, 0.08);
}

.chat-input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.chat-input::placeholder {
  color: #9b9a97;
}

.chat-send-btn {
  padding: 12px 24px;
  font-size: 14px;
  font-weight: 600;
  background: #202123;
  color: #ffffff;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
  flex-shrink: 0;
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.15),
    0 2px 4px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.chat-send-btn:hover:not(:disabled) {
  background: #000000;
  transform: translateY(-2px);
  box-shadow: 
    0 6px 16px rgba(0, 0, 0, 0.2),
    0 3px 6px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.15);
}

.chat-send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@keyframes messageFadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 侧拉动画 */
.slide-right-enter-active,
.slide-right-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-right-enter-from {
  opacity: 0;
}

.slide-right-leave-to {
  opacity: 0;
}

.slide-right-enter-from .chat-drawer {
  transform: translateX(100%);
}

.slide-right-leave-to .chat-drawer {
  transform: translateX(100%);
}
</style>
