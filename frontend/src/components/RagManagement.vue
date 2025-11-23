<template>
  <div class="knowledge-base-page">
    <!-- 左侧边栏 -->
    <div class="kb-sidebar">
      <!-- 导航菜单 -->
      <div class="sidebar-nav">
        <div class="nav-section">
          <div class="nav-section-title">知识库</div>
          <div 
            class="nav-item" 
            :class="{ active: currentView === 'personal' }"
            @click="switchView('personal')"
          >
            <svg class="nav-icon" width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M8 2L2 5v6l6 3 6-3V5l-6-3z" stroke="currentColor" stroke-width="1.5" fill="none"/>
              <path d="M8 5v6M5 8h6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <span class="nav-text">个人知识库</span>
            <span v-if="currentView === 'personal' && documents.length > 0" class="nav-badge">{{ documents.length }}</span>
          </div>
          
          <div 
            class="nav-item" 
            :class="{ active: currentView === 'shared' }"
            @click="switchView('shared')"
          >
            <svg class="nav-icon" width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M8 2L2 5v6l6 3 6-3V5l-6-3z" stroke="currentColor" stroke-width="1.5" fill="none"/>
              <circle cx="8" cy="8" r="2" fill="currentColor"/>
            </svg>
            <span class="nav-text">共享知识库</span>
            <span v-if="currentView === 'shared' && sharedDocuments.length > 0" class="nav-badge">{{ sharedDocuments.length }}</span>
          </div>
        </div>

        <div class="nav-section">
          <div class="nav-section-title">发现</div>
          <div 
            class="nav-item" 
            :class="{ active: currentView === 'knowledge-graph' }"
            @click="switchView('knowledge-graph')"
          >
            <svg class="nav-icon" width="16" height="16" viewBox="0 0 16 16" fill="none">
              <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.5" fill="none"/>
              <path d="M8 2v6M8 8v6M2 8h6M8 8h6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <span class="nav-text">知识图谱</span>
          </div>
          
          <div 
            class="nav-item" 
            :class="{ active: currentView === 'square' }"
            @click="switchView('square')"
          >
            <svg class="nav-icon" width="16" height="16" viewBox="0 0 16 16" fill="none">
              <rect x="2" y="2" width="12" height="12" rx="2" stroke="currentColor" stroke-width="1.5" fill="none"/>
              <path d="M6 6h4M6 10h4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <span class="nav-text">知识库广场</span>
          </div>
          
          <div 
            class="nav-item" 
            :class="{ active: currentView === 'recent' }"
            @click="switchView('recent')"
          >
            <svg class="nav-icon" width="16" height="16" viewBox="0 0 16 16" fill="none">
              <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.5" fill="none"/>
              <path d="M8 4v4l3 2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <span class="nav-text">最近使用</span>
          </div>
          
          <div 
            class="nav-item" 
            :class="{ active: currentView === 'favorites' }"
            @click="switchView('favorites')"
          >
            <svg class="nav-icon" width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M8 2l2 4 4 1-3 3 1 4-4-2-4 2 1-4-3-3 4-1 2-4z" stroke="currentColor" stroke-width="1.5" fill="none" stroke-linejoin="round"/>
            </svg>
            <span class="nav-text">收藏</span>
            <span v-if="favoriteCount > 0" class="nav-badge">{{ favoriteCount }}</span>
          </div>
        </div>

        <div class="nav-section nav-section-bottom">
          <div class="nav-section-title">管理</div>
          <div 
            class="nav-item" 
            :class="{ active: currentView === 'tags' }"
            @click="switchView('tags')"
          >
            <svg class="nav-icon" width="16" height="16" viewBox="0 0 16 16" fill="none">
              <rect x="2" y="2" width="12" height="12" rx="2" stroke="currentColor" stroke-width="1.5" fill="none"/>
              <path d="M6 6h4M6 10h2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <span class="nav-text">标签管理</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="kb-main">
      <!-- 顶部标题栏 -->
      <div class="kb-header">
        <div class="kb-header-left">
          <h1 class="kb-title">{{ getViewTitle() }}</h1>
          <div class="kb-stats" v-if="!loading && getCurrentDocuments().length > 0">
            <span class="stat-item">
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                <rect x="2" y="3" width="10" height="8" rx="1" stroke="currentColor" stroke-width="1.5" fill="none"/>
                <path d="M5 3v6M9 3v6" stroke="currentColor" stroke-width="1.5"/>
              </svg>
              {{ getCurrentDocuments().length }} 个文档
            </span>
          </div>
        </div>
        <div class="kb-actions">
          <!-- 搜索框 -->
          <SearchBox v-model="searchQuery" placeholder="搜索文档..." />
          <!-- 排序 -->
          <div class="sort-wrapper">
            <select v-model="sortBy" class="sort-select">
              <option value="time">按时间</option>
              <option value="name">按名称</option>
            </select>
          </div>
          <!-- 视图切换 -->
          <ViewToggle v-model="viewMode" />
          <button class="upload-btn" @click="showUploadDialog = true" title="上传文档">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M8 2v8M4 6l4-4 4 4M2 12h12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
      </div>

        <!-- 知识图谱视图 -->
        <div v-if="currentView === 'knowledge-graph'" class="kb-content kg-content-full">
          <KnowledgeGraph :ragId="selectedRagId" />
        </div>

        <!-- 文档列表 -->
        <div v-else class="kb-content">
          <!-- 加载状态 -->
          <LoadingState v-if="loading" />

          <!-- 空状态 -->
          <EmptyState 
            v-else-if="sortedAndFilteredDocuments.length === 0"
            :title="getEmptyTitle()"
            :description="getEmptyDesc()"
            :show-button="currentView === 'personal'"
            button-text="上传文档"
            @action="showUploadDialog = true"
          />

          <!-- 文档网格 -->
          <div v-else :class="['kb-documents', viewMode === 'grid' ? 'kb-documents-grid' : 'kb-documents-list']">
            <div
              v-for="(doc, index) in sortedAndFilteredDocuments"
              :key="doc.embeddingId || index"
              class="document-card"
              @click="openDocument(doc)"
              @mouseenter="hoveredDoc = doc.embeddingId"
              @mouseleave="hoveredDoc = null"
            >
              <div class="doc-icon" :class="getFileIconClass(doc)">
                {{ getFileIcon(doc) }}
              </div>
              <div class="doc-info">
                <div class="doc-title">{{ getDocumentTitle(doc) }}</div>
                <div class="doc-meta">
                  <span class="doc-time">{{ formatTime(doc.createdAt) }}</span>
                  <span v-if="doc.text" class="doc-size">{{ formatSize(doc.text.length) }}</span>
                  <span v-if="currentView === 'shared'" class="doc-shared-badge">共享</span>
                </div>
              </div>
              <div class="doc-actions" v-if="hoveredDoc === doc.embeddingId">
                <button 
                  v-if="currentView === 'personal'"
                  class="doc-action-btn" 
                  @click.stop="deleteDocument(doc)"
                  title="删除"
                >
                  <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                    <path d="M3.5 3.5l7 7M10.5 3.5l-7 7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                </button>
                <button 
                  v-if="currentView !== 'personal'"
                  class="doc-action-btn" 
                  @click.stop="toggleFavorite(doc)"
                  :title="isFavorite(doc) ? '取消收藏' : '收藏'"
                >
                  <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                    <path 
                      d="M7 2l1.5 3 3.5 0.5-2.5 2.5 0.5 3.5L7 10l-3 1.5 0.5-3.5L2 6l3.5-0.5L7 2z" 
                      :fill="isFavorite(doc) ? 'currentColor' : 'none'"
                      stroke="currentColor" 
                      stroke-width="1.5" 
                      stroke-linejoin="round"
                    />
                  </svg>
                </button>
              </div>
            </div>
          </div>
        </div>
    </div>

    <!-- 上传对话框 -->
    <transition name="dialog-fade">
      <div v-if="showUploadDialog" class="dialog-overlay" @click="showUploadDialog = false">
        <div class="dialog-content" @click.stop>
          <div class="dialog-header">
            <h3>上传文档</h3>
            <button class="dialog-close" @click="showUploadDialog = false">×</button>
          </div>
          <div class="dialog-body">
            <div class="upload-tabs">
              <button class="upload-tab" :class="{ active: uploadMode === 'text' }" @click="uploadMode = 'text'">
                文本输入
              </button>
              <button class="upload-tab" :class="{ active: uploadMode === 'file' }" @click="uploadMode = 'file'">
                文件上传
              </button>
              <button class="upload-tab" :class="{ active: uploadMode === 'import' }" @click="uploadMode = 'import'">
                导入文档
              </button>
            </div>

            <!-- 文本输入 -->
            <div v-if="uploadMode === 'text'" class="upload-content">
              <textarea
                v-model="documentText"
                class="upload-textarea"
                placeholder="请输入文档内容..."
                rows="10"
              ></textarea>
              <div class="upload-actions">
                <button class="btn-secondary" @click="showUploadDialog = false">取消</button>
                <button class="btn-primary" @click="addDocument" :disabled="!documentText.trim() || loading">
                  添加文档
                </button>
              </div>
            </div>

            <!-- 文件上传 -->
            <div v-if="uploadMode === 'file'" class="upload-content">
              <div class="file-upload-zone" @click="triggerFileInput" @dragover.prevent @drop.prevent="handleDrop">
                <input ref="fileInput" type="file" class="file-input" @change="handleFileSelect" multiple />
                <div class="upload-zone-content">
                  <svg class="upload-zone-icon" width="48" height="48" viewBox="0 0 48 48" fill="none">
                    <rect x="8" y="12" width="32" height="28" rx="2" stroke="currentColor" stroke-width="2" fill="none"/>
                    <path d="M16 20h16M16 26h12M16 32h16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  </svg>
                  <p>点击选择文件或拖拽文件到此处</p>
                  <p class="upload-hint">支持文本文件 (.txt, .md, .json 等)</p>
                </div>
              </div>
              <div v-if="selectedFiles.length > 0" class="selected-files">
                <div v-for="(file, index) in selectedFiles" :key="index" class="file-item">
                  <span>{{ file.name }}</span>
                  <button @click="removeFile(index)">×</button>
                </div>
              </div>
              <div class="upload-actions">
                <button class="btn-secondary" @click="showUploadDialog = false">取消</button>
                <button class="btn-primary" @click="uploadFiles" :disabled="selectedFiles.length === 0 || loading">
                  上传文件
                </button>
              </div>
            </div>

            <!-- 导入文档 -->
            <div v-if="uploadMode === 'import'" class="upload-content">
              <div v-if="importDocsLoading" class="loading-container">
                <div class="loading-spinner"></div>
                <p>加载文档列表...</p>
              </div>
              <div v-else-if="importDocuments.length === 0" class="empty-import-docs">
                <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
                  <rect x="8" y="8" width="32" height="32" rx="2" stroke="currentColor" stroke-width="2" fill="none"/>
                  <path d="M16 18h16M16 24h12M16 30h16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
                <p>暂无文档可导入</p>
              </div>
              <div v-else class="import-docs-list">
                <div 
                  v-for="doc in importDocuments" 
                  :key="doc.id" 
                  class="import-doc-item"
                  :class="{ selected: selectedImportDocs.includes(doc.id) }"
                  @click="toggleImportDoc(doc.id)"
                >
                  <div class="doc-item-checkbox">
                    <input 
                      type="checkbox" 
                      :checked="selectedImportDocs.includes(doc.id)"
                      @change.stop="toggleImportDoc(doc.id)"
                    />
                  </div>
                  <div class="doc-item-info">
                    <div class="doc-item-name">{{ doc.name || '未命名文档' }}</div>
                    <div class="doc-item-meta">
                      <span v-if="doc.text">{{ doc.text.length }} 字符</span>
                      <span v-else>空文档</span>
                    </div>
                  </div>
                </div>
              </div>
              <div class="upload-actions">
                <button class="btn-secondary" @click="showUploadDialog = false">取消</button>
                <button 
                  class="btn-primary" 
                  @click="importDocumentsToRag" 
                  :disabled="selectedImportDocs.length === 0 || loading"
                >
                  导入文档 ({{ selectedImportDocs.length }})
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </transition>

    <!-- 确认对话框 -->
    <ConfirmDialog
      :visible="showConfirmDialog"
      :title="confirmDialogTitle"
      :message="confirmDialogMessage"
      confirm-text="删除"
      @confirm="handleConfirmDelete"
      @cancel="showConfirmDialog = false"
    />

    <!-- 消息提示（成功和信息） -->
    <MessageToast
      :visible="showToast"
      :message="toastMessage"
      :type="toastType"
      @close="showToast = false"
    />

    <!-- 错误对话框 -->
    <ErrorDialog
      :visible="showErrorDialog"
      :title="errorDialogTitle"
      :message="errorDialogMessage"
      @close="showErrorDialog = false"
    />

    <!-- 右下角聊天窗口 -->
    <div class="chat-window" :class="{ pinned: chatPinned, minimized: chatMinimized }">
      <div class="chat-header">
        <div class="chat-title">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none" style="margin-right: 8px;">
            <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.5" fill="none"/>
            <path d="M8 5v3M8 10h.01" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          AI 助手
        </div>
        <div class="chat-actions">
          <button class="chat-action-btn" @click="chatPinned = !chatPinned" title="固定">
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
              <path d="M7 2v10M4 5h6M4 9h6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
          </button>
          <button class="chat-action-btn" @click="saveChat" title="保存">
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
              <path d="M3 3h8v8H3V3z" stroke="currentColor" stroke-width="1.5" fill="none"/>
              <path d="M5 3v5h4V3" stroke="currentColor" stroke-width="1.5" fill="none"/>
            </svg>
          </button>
          <button class="chat-action-btn" @click="chatMinimized = !chatMinimized" title="最小化">
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
              <path d="M3.5 7h7M7 3.5v7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" v-if="chatMinimized"/>
              <path d="M3.5 7h7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" v-else/>
            </svg>
          </button>
          <button class="chat-action-btn" @click="closeChat" title="关闭">
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
              <path d="M3.5 3.5l7 7M10.5 3.5l-7 7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
          </button>
        </div>
      </div>

      <div v-if="!chatMinimized" class="chat-body">
        <div class="chat-messages" ref="chatMessagesRef">
          <div v-if="chatMessages.length === 0" class="chat-empty">
            <p>开始与AI对话，询问知识库相关问题</p>
          </div>
          <div
            v-for="(msg, index) in chatMessages"
            :key="index"
            class="chat-message"
            :class="msg.role"
          >
            <div class="message-content">{{ msg.content }}</div>
            <div class="message-actions">
              <button class="msg-action">分享</button>
              <button class="msg-action">刷新</button>
              <button class="msg-action">复制</button>
              <button class="msg-action">保存</button>
            </div>
          </div>
        </div>
        <div class="chat-input-area">
          <input
            v-model="chatInput"
            class="chat-input"
            placeholder="基于知识库提问"
            @keyup.enter="sendChatMessage"
            :disabled="chatLoading"
          />
          <div class="chat-model-selector">
            <span>DeepSeek R1</span>
            <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
              <path d="M3 4.5l3 3 3-3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <button class="chat-send-btn" @click="sendChatMessage" :disabled="!chatInput.trim() || chatLoading">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M2 8l12-6-6 6 6 6L2 8z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, nextTick, watch } from 'vue'
import ConfirmDialog from './ConfirmDialog.vue'
import MessageToast from './MessageToast.vue'
import ErrorDialog from './ErrorDialog.vue'
import SearchBox from './SearchBox.vue'
import ViewToggle from './ViewToggle.vue'
import LoadingState from './LoadingState.vue'
import EmptyState from './EmptyState.vue'
import KnowledgeGraph from './KnowledgeGraph.vue'
import { formatTime, formatSize } from '../utils/format.js'
import { useSession } from '../composables/useSession.js'

// 会话管理
const session = useSession()

const selectedRagId = ref(1)
const ragList = ref([
  { id: 1, name: 'Default RAG' }
])
const documents = ref([])
const sharedDocuments = ref([])
const recentDocuments = ref([])
const favoriteDocuments = ref([])
const loading = ref(false)
const showUploadDialog = ref(false)
const uploadMode = ref('text')
const documentText = ref('')
const selectedFiles = ref([])
const fileInput = ref(null)
const importDocuments = ref([])
const importDocsLoading = ref(false)
const selectedImportDocs = ref([])
const filterMode = ref('uploaded')
const currentView = ref('personal') // 'personal', 'shared', 'square', 'recent', 'favorites', 'tags', 'knowledge-graph'
const searchQuery = ref('')
const sortBy = ref('time')
const viewMode = ref('grid')
const hoveredDoc = ref(null)

// 聊天相关
const chatPinned = ref(false)
const chatMinimized = ref(false)
const chatMessages = ref([])
const chatInput = ref('')
const chatLoading = ref(false)
const chatMessagesRef = ref(null)

// 确认对话框相关
const showConfirmDialog = ref(false)
const confirmDialogTitle = ref('确认删除')
const confirmDialogMessage = ref('')
let pendingDeleteDoc = null

// 消息提示相关
const showToast = ref(false)
const toastMessage = ref('')
const toastType = ref('info') // 'info', 'success'

// 错误对话框相关
const showErrorDialog = ref(false)
const errorDialogTitle = ref('操作失败')
const errorDialogMessage = ref('')

// 获取当前视图的文档列表
const getCurrentDocuments = () => {
  switch (currentView.value) {
    case 'personal':
      return documents.value
    case 'shared':
      return sharedDocuments.value
    case 'recent':
      return recentDocuments.value
    case 'favorites':
      return favoriteDocuments.value
    case 'square':
      return [] // 知识库广场显示的是知识库列表，不是文档
    case 'tags':
      return [] // 标签管理不显示文档
    default:
      return documents.value
  }
}

// 过滤和排序文档
const sortedAndFilteredDocuments = computed(() => {
  let result = getCurrentDocuments()
  
  // 过滤
  if (filterMode.value === 'uploaded') {
    result = result.filter(doc => doc.metadata?.source === 'file_upload' || doc.metadata?.source === 'manual_input')
  }
  
  // 搜索
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(doc => {
      const title = getDocumentTitle(doc).toLowerCase()
      const text = (doc.text || '').toLowerCase()
      return title.includes(query) || text.includes(query)
    })
  }
  
  // 排序
  if (sortBy.value === 'time') {
    result = [...result].sort((a, b) => (b.createdAt || 0) - (a.createdAt || 0))
  } else if (sortBy.value === 'name') {
    result = [...result].sort((a, b) => {
      const nameA = getDocumentTitle(a).toLowerCase()
      const nameB = getDocumentTitle(b).toLowerCase()
      return nameA.localeCompare(nameB)
    })
  }
  
  return result
})

// 切换视图
const switchView = (view) => {
  currentView.value = view
  if (view === 'shared') {
    loadSharedDocuments()
  } else if (view === 'recent') {
    loadRecentDocuments()
  } else if (view === 'favorites') {
    loadFavoriteDocuments()
  } else if (view === 'personal') {
    loadDocuments()
  }
}

// 获取视图标题
const getViewTitle = () => {
  if (currentView.value === 'knowledge-graph') {
    return '知识图谱'
  }
  const titles = {
    personal: '个人知识库',
    shared: '共享知识库',
    square: '知识库广场',
    recent: '最近使用',
    favorites: '收藏',
    tags: '标签管理'
  }
  return titles[currentView.value] || '知识库'
}

// 获取空状态标题
const getEmptyTitle = () => {
  const titles = {
    personal: '暂无文档',
    shared: '暂无共享文档',
    square: '暂无知识库',
    recent: '暂无最近使用',
    favorites: '暂无收藏',
    tags: '暂无标签'
  }
  return titles[currentView.value] || '暂无内容'
}

// 获取空状态描述
const getEmptyDesc = () => {
  const descs = {
    personal: '上传第一个文档开始使用',
    shared: '还没有人分享文档给你',
    square: '知识库广场暂时为空',
    recent: '您还没有使用过任何文档',
    favorites: '收藏的文档将显示在这里',
    tags: '创建标签来组织您的文档'
  }
  return descs[currentView.value] || ''
}

// 收藏相关
const favoriteCount = computed(() => favoriteDocuments.value.length)

const isFavorite = (doc) => {
  return favoriteDocuments.value.some(fav => fav.embeddingId === doc.embeddingId)
}

const toggleFavorite = (doc) => {
  const index = favoriteDocuments.value.findIndex(fav => fav.embeddingId === doc.embeddingId)
  if (index > -1) {
    favoriteDocuments.value.splice(index, 1)
    showMessage('已取消收藏', 'info')
  } else {
    favoriteDocuments.value.push(doc)
    showMessage('已收藏', 'success')
  }
  // 保存到 localStorage
  localStorage.setItem('rag_favorites', JSON.stringify(favoriteDocuments.value.map(d => d.embeddingId)))
}

// 加载共享文档
const loadSharedDocuments = async () => {
  // 模拟数据，实际应该从后端获取
  sharedDocuments.value = []
}

// 加载最近使用
const loadRecentDocuments = async () => {
  // 从 localStorage 读取最近使用的文档 ID，然后从 documents 中查找
  const recentIds = JSON.parse(localStorage.getItem('rag_recent') || '[]')
  recentDocuments.value = documents.value.filter(doc => recentIds.includes(doc.embeddingId))
}

// 加载收藏
const loadFavoriteDocuments = async () => {
  const favoriteIds = JSON.parse(localStorage.getItem('rag_favorites') || '[]')
  favoriteDocuments.value = documents.value.filter(doc => favoriteIds.includes(doc.embeddingId))
}

// 获取文件图标
const getFileIcon = (doc) => {
  const fileName = doc.metadata?.fileName || ''
  if (fileName.endsWith('.md')) return 'MD'
  if (fileName.endsWith('.pdf')) return 'PDF'
  if (fileName.endsWith('.txt')) return 'TXT'
  return 'DOC'
}

// 获取文件图标样式类
const getFileIconClass = (doc) => {
  const fileName = doc.metadata?.fileName || ''
  if (fileName.endsWith('.md')) return 'icon-md'
  if (fileName.endsWith('.pdf')) return 'icon-pdf'
  if (fileName.endsWith('.txt')) return 'icon-txt'
  return 'icon-doc'
}

// 获取文档标题
const getDocumentTitle = (doc) => {
  return doc.metadata?.fileName || `文档 ${doc.embeddingId?.substring(0, 8)}`
}

// 使用公共工具函数 formatTime 和 formatSize

// 删除文档
const deleteDocument = (doc) => {
  pendingDeleteDoc = doc
  confirmDialogTitle.value = '确认删除'
  confirmDialogMessage.value = `确定要删除文档 "${getDocumentTitle(doc)}" 吗？此操作不可撤销。`
  showConfirmDialog.value = true
}

// 确认删除
const handleConfirmDelete = async () => {
  showConfirmDialog.value = false
  if (!pendingDeleteDoc) return
  
  const doc = pendingDeleteDoc
  pendingDeleteDoc = null
  
  loading.value = true
  try {
    const response = await fetch(`/rag/${selectedRagId.value}/documents/${doc.embeddingId}`, {
      method: 'DELETE'
    })
    const result = await response.json()
    if (result.success) {
      showMessage('删除成功', 'success')
      loadDocuments()
    } else {
      showMessage('删除失败: ' + result.message, 'error')
    }
  } catch (error) {
    console.error('删除文档失败:', error)
    showMessage('删除失败: ' + error.message, 'error')
  } finally {
    loading.value = false
  }
}

// 显示消息提示
const showMessage = (message, type = 'info') => {
  if (type === 'error') {
    // 错误使用居中对话框
    errorDialogMessage.value = message
    showErrorDialog.value = true
  } else {
    // 成功和信息使用右上角 Toast
    toastMessage.value = message
    toastType.value = type
    showToast.value = true
  }
}

// 打开文档
const openDocument = (doc) => {
  // 记录到最近使用
  const recentIds = JSON.parse(localStorage.getItem('rag_recent') || '[]')
  const index = recentIds.indexOf(doc.embeddingId)
  if (index > -1) {
    recentIds.splice(index, 1)
  }
  recentIds.unshift(doc.embeddingId)
  // 只保留最近20个
  if (recentIds.length > 20) {
    recentIds.splice(20)
  }
  localStorage.setItem('rag_recent', JSON.stringify(recentIds))
  
  // 可以在这里实现文档详情查看
  console.log('打开文档:', doc)
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
      documentText.value = ''
      showUploadDialog.value = false
      showMessage('文档添加成功', 'success')
      loadDocuments()
    } else {
      showMessage('添加失败: ' + result.message, 'error')
    }
  } catch (error) {
    console.error('添加文档失败:', error)
    showMessage('添加失败: ' + error.message, 'error')
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
        showMessage(`文件 ${file.name} 上传失败: ${result.message}`, 'error')
      }
    }
    
    selectedFiles.value = []
    showUploadDialog.value = false
    showMessage('文件上传完成', 'success')
    loadDocuments()
  } catch (error) {
    console.error('上传文件失败:', error)
    showMessage('上传失败: ' + error.message, 'error')
  } finally {
    loading.value = false
  }
}

// 加载文档库文档列表
const loadImportDocuments = async () => {
  importDocsLoading.value = true
  try {
    // 从session获取userId
    const userId = session.userId?.value || '1'
    const response = await fetch(`/doc/list/${userId}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    if (!response.ok) {
      throw new Error('加载文档列表失败')
    }
    
    const docList = await response.json()
    importDocuments.value = docList || []
  } catch (error) {
    console.error('加载文档列表失败:', error)
    importDocuments.value = []
    showMessage('加载文档列表失败: ' + error.message, 'error')
  } finally {
    importDocsLoading.value = false
  }
}

// 切换导入文档选择
const toggleImportDoc = (docId) => {
  const index = selectedImportDocs.value.indexOf(docId)
  if (index > -1) {
    selectedImportDocs.value.splice(index, 1)
  } else {
    selectedImportDocs.value.push(docId)
  }
}

// 导入文档到RAG
const importDocumentsToRag = async () => {
  if (selectedImportDocs.value.length === 0) return
  
  loading.value = true
  try {
    for (const docId of selectedImportDocs.value) {
      const response = await fetch(`/rag/${selectedRagId.value}/documents/import`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          docId: docId
        })
      })
      
      const result = await response.json()
      if (!result.success) {
        showMessage(`文档 ${docId} 导入失败: ${result.message}`, 'error')
      }
    }
    
    selectedImportDocs.value = []
    showUploadDialog.value = false
    showMessage('文档导入完成', 'success')
    loadDocuments()
  } catch (error) {
    console.error('导入文档失败:', error)
    showMessage('导入失败: ' + error.message, 'error')
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
    console.log('loadDocuments response:', result)
    if (result.success) {
      documents.value = result.documents || []
      console.log('documents loaded:', documents.value.length)
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

// 发送聊天消息
const sendChatMessage = async () => {
  if (!chatInput.value.trim() || chatLoading.value) return

  const userMessage = chatInput.value.trim()
  chatMessages.value.push({
    role: 'user',
    content: userMessage
  })

  const aiMessageIndex = chatMessages.value.length
  chatMessages.value.push({
    role: 'assistant',
    content: '',
    streaming: true
  })

  chatInput.value = ''
  chatLoading.value = true

  await nextTick()
  scrollChatToBottom()

  try {
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
      const relevantDocs = result.results.map(r => r.text).join('\n\n')
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

// 保存聊天
const saveChat = () => {
  console.log('保存聊天记录')
  // 可以实现保存功能
}

// 关闭聊天
const closeChat = () => {
  chatMinimized.value = true
}

// 刷新处理函数
const handleRefresh = (event) => {
  const { view } = event.detail || {}
  if (view === 'rag') {
    console.log('刷新RAG知识库')
    // 根据当前视图刷新对应的数据
    switch (currentView.value) {
      case 'personal':
        loadDocuments()
        break
      case 'shared':
        loadSharedDocuments()
        break
      case 'recent':
        loadRecentDocuments()
        break
      case 'favorites':
        loadFavoriteDocuments()
        break
      default:
        loadDocuments()
    }
  }
}

// 初始化
// 监听上传模式变化，切换到导入文档时加载文档列表
watch(uploadMode, (newMode) => {
  if (newMode === 'import' && importDocuments.value.length === 0) {
    loadImportDocuments()
  }
})

onMounted(() => {
  console.log('RagManagement mounted, selectedRagId:', selectedRagId.value)
  loadDocuments()
  loadFavoriteDocuments()
  // 监听刷新事件
  window.addEventListener('tab-refresh', handleRefresh)
})

// 清理
onUnmounted(() => {
  window.removeEventListener('tab-refresh', handleRefresh)
})
</script>

<style scoped>
.knowledge-base-page {
  display: flex;
  height: 100%;
  width: 100%;
  background: var(--theme-background-gradient, none);
  background-color: var(--theme-background, #ffffff);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Helvetica Neue', Arial, sans-serif;
}

/* 左侧边栏 */
.kb-sidebar {
  width: 240px;
  background: var(--theme-background-gradient, none);
  background-color: var(--theme-background, #ffffff);
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-nav {
  flex: 1;
  padding: 12px 8px;
  overflow-y: auto;
}

.nav-section-title {
  font-size: 12px;
  font-weight: 600;
  color: #9ca3af;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 8px 12px;
  margin-bottom: 4px;
}

.nav-section {
  margin-bottom: 4px;
}

.nav-section-bottom {
  margin-top: auto;
  margin-bottom: 0;
  padding-top: 8px;
  border-top: 1px solid #e5e7eb;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.15s ease-out;
  color: #6b7280;
  font-size: 14px;
  font-weight: 500;
}

.nav-item:hover {
  background-color: rgba(0, 0, 0, 0.04);
  color: #111827;
}

.nav-item.active {
  background-color: color-mix(in srgb, var(--theme-accent, #165dff) 10%, transparent);
  color: var(--theme-accent, #165dff);
  font-weight: 600;
}

.nav-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
  color: currentColor;
}

.nav-text {
  flex: 1;
}

.nav-badge {
  font-size: 11px;
  font-weight: 600;
  color: #ffffff;
  background: var(--theme-accent, #165dff);
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
  line-height: 1.4;
}

.nav-arrow {
  width: 12px;
  height: 12px;
  flex-shrink: 0;
  transition: transform 0.15s ease-out;
  color: #9ca3af;
}

.nav-item:hover .nav-arrow {
  color: #6b7280;
}

.nav-arrow.expanded {
  transform: rotate(180deg);
}

.nav-submenu {
  padding-left: 38px;
  margin-top: 4px;
}

.nav-subitem {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.15s ease-out;
  color: #6b7280;
  font-size: 13px;
  font-weight: 500;
}

.nav-subitem:hover {
  background-color: rgba(0, 0, 0, 0.03);
  color: #111827;
}

.nav-subitem .nav-icon {
  width: 14px;
  height: 14px;
}

/* 主内容区 */
.kb-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--theme-background-gradient, none);
  background-color: var(--theme-background, #ffffff);
}

.kb-header {
  padding: 20px 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--theme-background-gradient, none);
  background-color: var(--theme-background, #ffffff);
  gap: 24px;
  border-bottom: 1px solid #e5e6eb;
}

.kb-header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.kb-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #111827;
  letter-spacing: -0.025em;
}

.kb-stats {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6b7280;
  font-weight: 500;
}

.stat-item svg {
  color: #9ca3af;
}

.kb-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 搜索框、排序、视图切换样式已移至 common.css */

.action-btn {
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 400;
  color: #4e5969;
  background: var(--theme-background-gradient, none);
  background-color: var(--theme-background, #ffffff);
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.action-btn:hover {
  background-color: rgba(0, 0, 0, 0.04);
  color: #1d2129;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06);
}

.action-btn.active {
  background-color: var(--theme-accent, #165dff);
  color: #ffffff;
  border-color: var(--theme-accent, #165dff);
}

.upload-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--theme-accent, #165dff);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.upload-btn::before {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.2);
  opacity: 0;
  transform: scale(0);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 6px;
}

.upload-btn:hover {
  background-color: var(--theme-accent, #165dff);
  opacity: 0.9;
  box-shadow: 0 2px 8px color-mix(in srgb, var(--theme-accent, #165dff) 30%, transparent);
}

.upload-btn:active {
  transform: translateY(0) scale(0.95);
}

.upload-btn svg {
  width: 16px;
  height: 16px;
}

.kb-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px 32px;
  background: var(--theme-background-gradient, none);
  background-color: var(--theme-background, #ffffff);
}

.kg-content-full {
  padding: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 加载状态和空状态样式已移至 common.css */

.kb-documents {
  display: grid;
  gap: 16px;
}

.kb-documents-grid {
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
}

.kb-documents-list {
  grid-template-columns: 1fr;
}

.kb-documents-list .document-card {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
}

.kb-documents-list .doc-icon {
  margin-bottom: 0;
  flex-shrink: 0;
}

.kb-documents-list .doc-info {
  flex: 1;
  min-width: 0;
}

.kb-documents-list .doc-title {
  margin-bottom: 4px;
}

.kb-documents-list .doc-meta {
  display: flex;
  gap: 12px;
}

.document-card {
  background: var(--theme-background-gradient, none);
  background-color: var(--theme-background, #ffffff);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  will-change: transform, box-shadow;
  box-shadow: 
    0 2px 4px rgba(0, 0, 0, 0.06),
    0 1px 2px rgba(0, 0, 0, 0.04);
}

.document-card:hover {
  background: var(--theme-background-gradient, none);
  background-color: var(--theme-background, #ffffff);
  border-color: color-mix(in srgb, var(--theme-accent, #165dff) 30%, transparent);
  box-shadow: 
    0 8px 16px rgba(0, 0, 0, 0.12),
    0 4px 8px color-mix(in srgb, var(--theme-accent, #165dff) 15%, transparent);
  transform: translateY(-2px);
}

.document-card:active {
  transform: translateY(0);
}

.doc-icon {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  color: white;
  margin-bottom: 12px;
}

.icon-md {
  background: #10b981;
}

.icon-pdf {
  background: #ef4444;
}

.icon-txt {
  background: #3b82f6;
}

.icon-doc {
  background: #6b7280;
}

.doc-title {
  font-size: 14px;
  font-weight: 500;
  color: #111827;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
}

.doc-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.doc-time {
  font-size: 12px;
  color: #9ca3af;
  font-weight: 400;
}

.doc-size {
  font-size: 12px;
  color: #9ca3af;
  font-weight: 400;
}

.doc-shared-badge {
  font-size: 11px;
  color: #10a37f;
  background: rgba(16, 163, 127, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 500;
}

.doc-actions {
  position: absolute;
  top: 12px;
  right: 12px;
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.15s ease-out;
}

.document-card:hover .doc-actions {
  opacity: 1;
}

.doc-action-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--theme-background-gradient, none);
  background-color: var(--theme-background, #ffffff);
  border: none;
  border-radius: 4px;
  color: #495057;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.doc-action-btn::before {
  content: '';
  position: absolute;
  inset: 0;
  background: currentColor;
  opacity: 0;
  border-radius: 4px;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.doc-action-btn:hover {
  background: #fee2e2;
  color: #dc2626;
  transform: scale(1.1);
  box-shadow: 0 2px 6px rgba(239, 68, 68, 0.2);
}

.doc-action-btn:active {
  transform: scale(0.95);
}

/* 上传对话框 */
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog-content {
  background: white;
  border-radius: 8px;
  width: 600px;
  max-width: 90vw;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  transform-origin: center;
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

.dialog-header {
  padding: 20px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--theme-background-gradient, none);
  background-color: var(--theme-background, #ffffff);
  border-radius: 8px 8px 0 0;
  border-bottom: 1px solid #e5e6eb;
}

.dialog-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #202123;
}

.dialog-close {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: 6px;
  font-size: 24px;
  color: #9b9a97;
  cursor: pointer;
  transition: background 0.15s ease-out;
}

.dialog-close:hover {
  background: #f5f5f5;
}

.dialog-body {
  padding: 24px;
  overflow-y: auto;
}

.upload-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
}

.upload-tab {
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  color: #565869;
  background: transparent;
  border: 1px solid #e5e5e5;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s ease-out;
}

.upload-tab:hover {
  background: #f5f5f5;
}

.upload-tab.active {
  background: var(--theme-accent, #165dff);
  color: white;
  border-color: var(--theme-accent, #165dff);
}

.upload-textarea {
  width: 100%;
  padding: 12px;
  font-size: 14px;
  border: 1px solid #e5e5e5;
  border-radius: 6px;
  resize: vertical;
  font-family: inherit;
  margin-bottom: 16px;
}

.upload-textarea:focus {
  outline: none;
  border-color: var(--theme-accent, #165dff);
}

.file-upload-zone {
  border: 2px dashed #d0d0d0;
  border-radius: 8px;
  padding: 40px;
  text-align: center;
  cursor: pointer;
  transition: border-color 0.2s;
  margin-bottom: 16px;
}

.file-upload-zone:hover {
  border-color: var(--theme-accent, #165dff);
}

.upload-zone-content {
  pointer-events: none;
}

.upload-zone-icon {
  width: 48px;
  height: 48px;
  margin-bottom: 12px;
  color: #9ca3af;
}

.upload-hint {
  font-size: 12px;
  color: #9b9a97;
  margin-top: 4px;
}

.file-input {
  display: none;
}

.selected-files {
  margin-bottom: 16px;
}

.file-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: #f5f5f5;
  border-radius: 6px;
  margin-bottom: 8px;
  font-size: 14px;
}

.file-item button {
  background: transparent;
  border: none;
  color: #9b9a97;
  cursor: pointer;
  font-size: 18px;
}

.upload-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

/* 导入文档样式 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #9b9a97;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #e5e5e5;
  border-top-color: var(--theme-accent, #165dff);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-import-docs {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #9b9a97;
}

.empty-import-docs svg {
  margin-bottom: 12px;
  color: #d1d5db;
}

.empty-import-docs p {
  margin: 0;
  font-size: 14px;
}

.import-docs-list {
  max-height: 400px;
  overflow-y: auto;
  margin-bottom: 16px;
}

.import-doc-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border: 1px solid #e5e5e5;
  border-radius: 6px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.15s;
}

.import-doc-item:hover {
  border-color: var(--theme-accent, #165dff);
  background: color-mix(in srgb, var(--theme-accent, #165dff) 5%, transparent);
}

.import-doc-item.selected {
  border-color: var(--theme-accent, #165dff);
  background: color-mix(in srgb, var(--theme-accent, #165dff) 10%, transparent);
}

.doc-item-checkbox {
  margin-right: 12px;
}

.doc-item-checkbox input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.doc-item-info {
  flex: 1;
}

.doc-item-name {
  font-size: 14px;
  font-weight: 500;
  color: #202123;
  margin-bottom: 4px;
}

.doc-item-meta {
  font-size: 12px;
  color: #9b9a97;
}

/* 按钮样式已移至 common.css */

/* 聊天窗口 */
.chat-window {
  position: fixed;
  bottom: 20px;
  right: 20px;
  width: 400px;
  max-width: calc(100vw - 40px);
  background: var(--theme-background-gradient, none);
  background-color: var(--theme-background, #ffffff);
  border-radius: 8px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.12);
  display: flex;
  flex-direction: column;
  z-index: 999;
  transition: all 0.15s ease-out;
  border: none;
  overflow: hidden;
}

.chat-window.pinned {
  position: fixed;
}

.chat-window.minimized {
  height: 48px;
}

.chat-header {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--theme-background-gradient, none);
  background-color: var(--theme-background, #ffffff);
  border-bottom: 1px solid #e5e6eb;
}

.chat-title {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  display: flex;
  align-items: center;
  gap: 8px;
}

.chat-title svg {
  width: 16px;
  height: 16px;
  color: var(--theme-accent, #165dff);
}

.chat-actions {
  display: flex;
  gap: 8px;
}

.chat-action-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: 4px;
  color: #6b7280;
  cursor: pointer;
  transition: background-color 0.15s ease-out;
}

.chat-action-btn:hover {
  background-color: #f3f4f6;
}

.chat-action-btn svg {
  width: 14px;
  height: 14px;
}

.chat-body {
  display: flex;
  flex-direction: column;
  height: 500px;
  max-height: calc(100vh - 100px);
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.chat-empty {
  text-align: center;
  color: #9b9a97;
  font-size: 13px;
  padding: 20px;
}

.chat-message {
  margin-bottom: 16px;
}

.chat-message.user .message-content {
  background: var(--theme-accent, #165dff);
  color: white;
  padding: 10px 14px;
  border-radius: 6px;
  margin-bottom: 4px;
  font-size: 14px;
  line-height: 1.5;
}

.chat-message.assistant .message-content {
  background: rgba(0, 0, 0, 0.03);
  color: #111827;
  padding: 10px 14px;
  border-radius: 6px;
  margin-bottom: 4px;
  font-size: 14px;
  line-height: 1.5;
}

.message-actions {
  display: flex;
  gap: 8px;
  padding: 4px 0;
}

.msg-action {
  font-size: 12px;
  color: #9b9a97;
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 2px 4px;
}

.msg-action:hover {
  color: var(--theme-accent, #165dff);
}

.chat-input-area {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--theme-background-gradient, none);
  background-color: var(--theme-background, #ffffff);
  border-radius: 0 0 8px 8px;
  border-top: 1px solid #e5e6eb;
}

.chat-input {
  flex: 1;
  padding: 8px 12px;
  font-size: 14px;
  border: none;
  border-radius: 8px;
  outline: none;
  background: var(--theme-background-gradient, none);
  background-color: var(--theme-background, #ffffff);
  color: #1d2129;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.chat-input:focus {
  box-shadow: 0 0 0 2px color-mix(in srgb, var(--theme-accent, #165dff) 15%, transparent);
}

.chat-model-selector {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #6b7280;
  padding: 6px 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.15s ease-out;
}

.chat-model-selector:hover {
  background-color: #f3f4f6;
}

.chat-model-selector svg {
  width: 12px;
  height: 12px;
}

.chat-send-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--theme-accent, #165dff);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.15s ease-out;
}

.chat-send-btn:hover:not(:disabled) {
  background-color: var(--theme-accent, #165dff);
  opacity: 0.9;
}

.chat-send-btn svg {
  width: 16px;
  height: 16px;
}

.chat-send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 过渡动画 */
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

.dialog-fade-enter-from .dialog-content {
  opacity: 0;
  transform: translateY(30px) scale(0.95);
}

.dialog-fade-leave-to .dialog-content {
  opacity: 0;
  transform: translateY(20px) scale(0.98);
}
</style>
