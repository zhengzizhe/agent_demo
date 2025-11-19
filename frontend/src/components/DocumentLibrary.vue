<template>
  <div class="document-library-page">
    <!-- 左侧边栏 -->
    <div class="dl-sidebar">
      <!-- 用户信息区域 -->
      <div class="sidebar-user">
        <div class="user-avatar">{{ userInitial }}</div>
        <div class="user-info">
          <div class="user-name">{{ currentSpace?.name || '个人空间' }}</div>
        </div>
      </div>

      <!-- 导航菜单 -->
      <div class="sidebar-nav">
        <div class="nav-section">
          <div 
            class="nav-item" 
            :class="{ active: currentView === 'home' }"
            @click="switchView('home')"
          >
            <svg class="nav-icon" width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M2 4h12M2 8h12M2 12h8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <span class="nav-text">文档库</span>
          </div>
        </div>

        <div class="nav-section">
          <div 
            class="nav-item" 
            :class="{ active: currentView === 'shared' }"
            @click="switchView('shared')"
          >
            <svg class="nav-icon" width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M8 2L2 5v6l6 3 6-3V5l-6-3z" stroke="currentColor" stroke-width="1.5" fill="none"/>
            </svg>
            <span class="nav-text">共享给我</span>
          </div>
        </div>

        <div class="nav-section">
          <div class="nav-item" @click="toggleSpaces">
            <svg class="nav-icon" width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M3 3h10v10H3V3z" stroke="currentColor" stroke-width="1.5" fill="none"/>
              <path d="M6 3v10M10 3v10" stroke="currentColor" stroke-width="1.5"/>
            </svg>
            <span class="nav-text">空间</span>
            <svg class="nav-arrow" :class="{ expanded: showSpaces }" width="12" height="12" viewBox="0 0 12 12" fill="none">
              <path d="M3 4.5l3 3 3-3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <div v-if="showSpaces" class="nav-submenu">
            <div 
              v-for="space in spaces" 
              :key="space.id"
              class="nav-subitem"
              :class="{ active: currentSpace?.id === space.id }"
              @click="selectSpace(space)"
            >
              <svg class="nav-icon" width="14" height="14" viewBox="0 0 14 14" fill="none">
                <circle cx="7" cy="7" r="6" stroke="currentColor" stroke-width="1.5" fill="none"/>
              </svg>
              <span class="nav-text">{{ space.name }}</span>
            </div>
          </div>
        </div>

        <div class="nav-section nav-section-bottom">
          <div 
            class="nav-item" 
            :class="{ active: currentView === 'favorites' }"
            @click="switchView('favorites')"
          >
            <svg class="nav-icon" width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M8 2l2 4 4.5.5-3.5 3.5 1 4.5L8 12.5 4 14.5l1-4.5L1.5 6.5 6 6l2-4z" stroke="currentColor" stroke-width="1.5" fill="none"/>
            </svg>
            <span class="nav-text">收藏</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="dl-main">
      <!-- 顶部标题栏 -->
      <div class="dl-header">
        <div class="dl-header-left">
          <h1 class="dl-title">{{ currentViewTitle }}</h1>
          <div class="dl-stats" v-if="!loading && documents.length > 0">
            <span class="stat-item">
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                <rect x="2" y="3" width="10" height="8" rx="1" stroke="currentColor" stroke-width="1.5" fill="none"/>
                <path d="M5 3v6M9 3v6" stroke="currentColor" stroke-width="1.5"/>
              </svg>
              {{ documents.length }} 个文档
            </span>
          </div>
        </div>
        <div class="dl-actions">
          <!-- 搜索框 -->
          <SearchBox v-model="searchQuery" placeholder="搜索文档..." />
          <!-- 排序 -->
          <div class="sort-wrapper">
            <select v-model="sortBy" class="sort-select" @change="handleSort">
              <option value="time">按时间</option>
              <option value="name">按名称</option>
              <option value="size">按大小</option>
            </select>
          </div>
          <!-- 视图切换 -->
          <ViewToggle v-model="viewMode" />
          <!-- 新建文档按钮 -->
          <button class="create-btn btn btn-primary" @click="showCreateDialog = true" title="新建文档">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M8 2v12M2 8h12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <span>新建</span>
          </button>
        </div>
      </div>

      <!-- 文档列表 -->
      <div class="dl-content">
        <!-- 加载状态 -->
        <LoadingState v-if="loading" />

        <!-- 空状态 -->
        <EmptyState 
          v-else-if="sortedAndFilteredDocuments.length === 0"
          title="暂无文档"
          description="创建第一个文档开始使用"
          :show-button="true"
          button-text="新建文档"
          @action="showCreateDialog = true"
        />

        <!-- 网格视图 -->
        <div v-else-if="viewMode === 'grid'" class="dl-grid-view">
          <div 
            v-for="doc in sortedAndFilteredDocuments" 
            :key="doc.id"
            class="doc-card"
            @click="openDocument(doc)"
            @contextmenu.prevent="showContextMenu($event, doc)"
          >
            <div class="doc-card-header">
              <div class="doc-icon">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                  <rect x="4" y="4" width="16" height="16" rx="2" stroke="currentColor" stroke-width="1.5" fill="none"/>
                  <path d="M8 8h8M8 12h8M8 16h4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
              </div>
              <div class="doc-actions">
                <button 
                  class="doc-action-btn"
                  :class="{ active: doc.favorite }"
                  @click.stop="toggleFavorite(doc)"
                  title="收藏"
                >
                  <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                    <path d="M7 2l1.5 3 3.5.5-2.5 2.5.5 3.5L7 10.5 4 11.5l.5-3.5L2 5.5l3.5-.5L7 2z" 
                          :fill="doc.favorite ? 'currentColor' : 'none'" 
                          stroke="currentColor" 
                          stroke-width="1.5"/>
                  </svg>
                </button>
                <button 
                  class="doc-action-btn"
                  @click.stop="showDocMenu($event, doc)"
                  title="更多"
                >
                  <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                    <circle cx="7" cy="3.5" r="1.5" fill="currentColor"/>
                    <circle cx="7" cy="7" r="1.5" fill="currentColor"/>
                    <circle cx="7" cy="10.5" r="1.5" fill="currentColor"/>
                  </svg>
                </button>
              </div>
            </div>
            <div class="doc-card-body">
              <h3 class="doc-title">{{ doc.name || '未命名文档' }}</h3>
              <p class="doc-desc" v-if="doc.description">{{ doc.description }}</p>
              <div class="doc-meta">
                <span class="doc-time">{{ formatDate(doc.updatedAt || doc.createdAt) }}</span>
                <span class="doc-size" v-if="doc.size">{{ formatSize(doc.size) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 列表视图 -->
        <div v-else class="dl-list-view">
          <table class="doc-table">
            <thead>
              <tr>
                <th class="col-check">
                  <input 
                    type="checkbox" 
                    :checked="selectedDocs.length === sortedAndFilteredDocuments.length && sortedAndFilteredDocuments.length > 0"
                    @change="toggleSelectAll"
                  />
                </th>
                <th class="col-name">名称</th>
                <th class="col-owner">所有者</th>
                <th class="col-updated">更新时间</th>
                <th class="col-size">大小</th>
                <th class="col-actions">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr 
                v-for="doc in sortedAndFilteredDocuments" 
                :key="doc.id"
                class="doc-row"
                :class="{ selected: selectedDocs.includes(doc.id) }"
                @click="toggleSelect(doc)"
                @dblclick="openDocument(doc)"
                @contextmenu.prevent="showContextMenu($event, doc)"
              >
                <td class="col-check">
                  <input 
                    type="checkbox" 
                    :checked="selectedDocs.includes(doc.id)"
                    @change.stop="toggleSelect(doc)"
                  />
                </td>
                <td class="col-name">
                  <div class="doc-name-cell">
                    <div class="doc-icon-small">
                      <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                        <rect x="4" y="4" width="16" height="16" rx="2" stroke="currentColor" stroke-width="1.5" fill="none"/>
                        <path d="M8 8h8M8 12h8M8 16h4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                      </svg>
                    </div>
                    <span class="doc-name">{{ doc.name || '未命名文档' }}</span>
                    <button 
                      v-if="doc.favorite"
                      class="favorite-badge"
                      @click.stop="toggleFavorite(doc)"
                    >
                      <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                        <path d="M6 1.5l1.5 3 3.5.5-2.5 2.5.5 3.5L6 9.5 3 10.5l.5-3.5L1 4.5l3.5-.5L6 1.5z" 
                              fill="currentColor"/>
                      </svg>
                    </button>
                  </div>
                </td>
                <td class="col-owner">
                  <div class="owner-cell">
                    <div class="owner-avatar">{{ getOwnerInitial(doc.owner) }}</div>
                    <span>{{ doc.owner?.name || '未知' }}</span>
                  </div>
                </td>
                <td class="col-updated">{{ formatDate(doc.updatedAt || doc.createdAt) }}</td>
                <td class="col-size">{{ doc.size ? formatSize(doc.size) : '-' }}</td>
                <td class="col-actions">
                  <button 
                    class="action-btn"
                    @click.stop="showDocMenu($event, doc)"
                  >
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                      <circle cx="8" cy="3" r="1.5" fill="currentColor"/>
                      <circle cx="8" cy="8" r="1.5" fill="currentColor"/>
                      <circle cx="8" cy="13" r="1.5" fill="currentColor"/>
                    </svg>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 新建文档对话框 -->
    <div v-if="showCreateDialog" class="dialog-overlay" @click="showCreateDialog = false">
      <div class="dialog" @click.stop>
        <div class="dialog-header">
          <h3 class="dialog-title">新建文档</h3>
          <button class="dialog-close" @click="showCreateDialog = false">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M4 4l8 8M12 4l-8 8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
          </button>
        </div>
        <div class="dialog-content">
          <div class="create-options">
            <div class="create-option" @click="createDocument('text')">
              <div class="option-icon">
                <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                  <rect x="6" y="6" width="20" height="20" rx="2" stroke="currentColor" stroke-width="2" fill="none"/>
                  <path d="M10 12h12M10 16h12M10 20h8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </div>
              <h4>空白文档</h4>
              <p>创建一个新的空白文档</p>
            </div>
            <div class="create-option" @click="createDocument('markdown')">
              <div class="option-icon">
                <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                  <rect x="6" y="6" width="20" height="20" rx="2" stroke="currentColor" stroke-width="2" fill="none"/>
                  <path d="M10 12h12M10 16h8M10 20h12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </div>
              <h4>Markdown文档</h4>
              <p>使用Markdown格式创建文档</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右键菜单 -->
    <div 
      v-if="contextMenu.show"
      class="context-menu"
      :style="{ top: contextMenu.y + 'px', left: contextMenu.x + 'px' }"
      @click.stop
    >
      <div class="context-menu-item" @click="handleContextAction('open')">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <path d="M8 2v12M2 8h12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
        打开
      </div>
      <div class="context-menu-item" @click="handleContextAction('rename')">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <path d="M8 2l6 6-6 6M2 2l6 6-6 6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
        重命名
      </div>
      <div class="context-menu-item" @click="handleContextAction('duplicate')">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <rect x="4" y="4" width="8" height="8" rx="1" stroke="currentColor" stroke-width="1.5" fill="none"/>
          <path d="M8 2v2M8 12v2M2 8h2M12 8h2" stroke="currentColor" stroke-width="1.5"/>
        </svg>
        复制
      </div>
      <div class="context-menu-divider"></div>
      <div class="context-menu-item" @click="handleContextAction('share')">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <path d="M8 2L2 5v6l6 3 6-3V5l-6-3z" stroke="currentColor" stroke-width="1.5" fill="none"/>
        </svg>
        分享
      </div>
      <div class="context-menu-item" @click="handleContextAction('favorite')">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <path d="M8 2l2 4 4.5.5-3.5 3.5 1 4.5L8 12.5 4 14.5l1-4.5L1.5 6.5 6 6l2-4z" 
                :fill="contextMenu.doc?.favorite ? 'currentColor' : 'none'" 
                stroke="currentColor" 
                stroke-width="1.5"/>
        </svg>
        {{ contextMenu.doc?.favorite ? '取消收藏' : '收藏' }}
      </div>
      <div class="context-menu-divider"></div>
      <div class="context-menu-item danger" @click="handleContextAction('delete')">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <path d="M4 4l8 8M12 4l-8 8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
        删除
      </div>
    </div>

    <!-- 重命名对话框 -->
    <div v-if="showRenameDialog" class="dialog-overlay" @click="showRenameDialog = false">
      <div class="dialog dialog-small" @click.stop>
        <div class="dialog-header">
          <h3 class="dialog-title">重命名</h3>
          <button class="dialog-close" @click="showRenameDialog = false">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M4 4l8 8M12 4l-8 8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
          </button>
        </div>
        <div class="dialog-content">
          <input 
            v-model="renameValue"
            type="text"
            class="rename-input"
            placeholder="输入新名称"
            @keyup.enter="confirmRename"
            @keyup.esc="showRenameDialog = false"
            ref="renameInputRef"
          />
        </div>
        <div class="dialog-footer">
          <button class="btn btn-secondary" @click="showRenameDialog = false">取消</button>
          <button class="btn btn-primary" @click="confirmRename">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import SearchBox from './SearchBox.vue'
import ViewToggle from './ViewToggle.vue'
import LoadingState from './LoadingState.vue'
import EmptyState from './EmptyState.vue'
import { formatDate, formatSizeBytes } from '../utils/format.js'

// 响应式数据
const currentView = ref('home') // home, shared, favorites
const currentSpace = ref(null)
const spaces = ref([])
const showSpaces = ref(false)
const documents = ref([])
const loading = ref(false)
const searchQuery = ref('')
const sortBy = ref('time') // time, name, size
const viewMode = ref('grid') // grid, list
const selectedDocs = ref([])
const showCreateDialog = ref(false)
const showRenameDialog = ref(false)
const renameValue = ref('')
const renameInputRef = ref(null)
const contextMenu = ref({
  show: false,
  x: 0,
  y: 0,
  doc: null
})

// 计算属性
const userInitial = computed(() => {
  return 'U'
})

const currentViewTitle = computed(() => {
  const titles = {
    home: '文档库',
    shared: '共享给我',
    favorites: '收藏'
  }
  return titles[currentView.value] || '文档库'
})

const sortedAndFilteredDocuments = computed(() => {
  let result = [...documents.value]
  
  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(doc => 
      doc.name?.toLowerCase().includes(query) ||
      doc.description?.toLowerCase().includes(query)
    )
  }
  
  // 视图过滤
  if (currentView.value === 'shared') {
    result = result.filter(doc => doc.shared)
  } else if (currentView.value === 'favorites') {
    result = result.filter(doc => doc.favorite)
  }
  
  // 排序
  result.sort((a, b) => {
    if (sortBy.value === 'name') {
      return (a.name || '').localeCompare(b.name || '')
    } else if (sortBy.value === 'size') {
      return (b.size || 0) - (a.size || 0)
    } else {
      // 按时间
      const timeA = a.updatedAt || a.createdAt || 0
      const timeB = b.updatedAt || b.createdAt || 0
      return timeB - timeA
    }
  })
  
  return result
})

// 方法
const switchView = (view) => {
  currentView.value = view
  loadDocuments()
}

const toggleSpaces = () => {
  showSpaces.value = !showSpaces.value
  if (showSpaces.value && spaces.value.length === 0) {
    loadSpaces()
  }
}

const selectSpace = (space) => {
  currentSpace.value = space
  loadDocuments()
}

const loadSpaces = async () => {
  try {
    // TODO: 调用API获取空间列表
    // const response = await fetch('/api/spaces')
    // spaces.value = await response.json()
    
    // 模拟数据
    spaces.value = [
      { id: '1', name: '个人空间', type: 'personal' },
      { id: '2', name: '工作空间', type: 'work' }
    ]
  } catch (error) {
    console.error('加载空间列表失败:', error)
  }
}

const loadDocuments = async () => {
  loading.value = true
  try {
    // TODO: 根据currentView和currentSpace调用不同的API
    // const endpoint = currentView.value === 'shared' 
    //   ? '/api/documents/shared' 
    //   : currentView.value === 'favorites'
    //   ? '/api/documents/favorites'
    //   : '/api/documents'
    // const response = await fetch(endpoint)
    // documents.value = await response.json()
    
    // 模拟数据
    documents.value = [
      {
        id: '1',
        name: '项目计划文档',
        description: '2024年Q1项目计划',
        createdAt: Date.now() - 86400000 * 7,
        updatedAt: Date.now() - 86400000 * 2,
        size: 1024 * 50,
        favorite: false,
        shared: false,
        owner: { id: '1', name: '张三' }
      },
      {
        id: '2',
        name: '技术方案',
        description: '系统架构设计文档',
        createdAt: Date.now() - 86400000 * 5,
        updatedAt: Date.now() - 86400000 * 1,
        size: 1024 * 120,
        favorite: true,
        shared: true,
        owner: { id: '2', name: '李四' }
      },
      {
        id: '3',
        name: '会议纪要',
        description: '周会记录',
        createdAt: Date.now() - 86400000 * 3,
        updatedAt: Date.now() - 3600000,
        size: 1024 * 30,
        favorite: false,
        shared: false,
        owner: { id: '1', name: '张三' }
      }
    ]
  } catch (error) {
    console.error('加载文档列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSort = () => {
  // 排序逻辑已在computed中处理
}

const toggleSelectAll = (event) => {
  if (event.target.checked) {
    selectedDocs.value = sortedAndFilteredDocuments.value.map(doc => doc.id)
  } else {
    selectedDocs.value = []
  }
}

const toggleSelect = (doc) => {
  const index = selectedDocs.value.indexOf(doc.id)
  if (index > -1) {
    selectedDocs.value.splice(index, 1)
  } else {
    selectedDocs.value.push(doc.id)
  }
}

const openDocument = (doc) => {
  // TODO: 打开文档编辑器
  console.log('打开文档:', doc)
  // 可以emit事件或路由跳转
  // emit('open-document', doc)
}

const createDocument = async (type) => {
  try {
    // TODO: 调用API创建文档
    // const response = await fetch('/api/documents', {
    //   method: 'POST',
    //   headers: { 'Content-Type': 'application/json' },
    //   body: JSON.stringify({ type, name: '未命名文档', spaceId: currentSpace.value?.id })
    // })
    // const newDoc = await response.json()
    
    // 模拟创建
    const newDoc = {
      id: Date.now().toString(),
      name: '未命名文档',
      type,
      createdAt: Date.now(),
      updatedAt: Date.now(),
      size: 0,
      favorite: false,
      shared: false,
      owner: { id: '1', name: '当前用户' }
    }
    
    documents.value.unshift(newDoc)
    showCreateDialog.value = false
    openDocument(newDoc)
  } catch (error) {
    console.error('创建文档失败:', error)
  }
}

const toggleFavorite = async (doc) => {
  try {
    // TODO: 调用API切换收藏状态
    // await fetch(`/api/documents/${doc.id}/favorite`, {
    //   method: doc.favorite ? 'DELETE' : 'POST'
    // })
    
    doc.favorite = !doc.favorite
  } catch (error) {
    console.error('切换收藏状态失败:', error)
  }
}

const showDocMenu = (event, doc) => {
  contextMenu.value = {
    show: true,
    x: event.clientX,
    y: event.clientY,
    doc
  }
}

const showContextMenu = (event, doc) => {
  showDocMenu(event, doc)
}

const handleContextAction = async (action) => {
  const doc = contextMenu.value.doc
  if (!doc) return
  
  contextMenu.value.show = false
  
  switch (action) {
    case 'open':
      openDocument(doc)
      break
    case 'rename':
      renameValue.value = doc.name || ''
      showRenameDialog.value = true
      nextTick(() => {
        renameInputRef.value?.focus()
        renameInputRef.value?.select()
      })
      break
    case 'duplicate':
      await duplicateDocument(doc)
      break
    case 'share':
      await shareDocument(doc)
      break
    case 'favorite':
      await toggleFavorite(doc)
      break
    case 'delete':
      await deleteDocument(doc)
      break
  }
}

const confirmRename = async () => {
  const doc = contextMenu.value.doc
  if (!doc || !renameValue.value.trim()) return
  
  try {
    // TODO: 调用API重命名
    // await fetch(`/api/documents/${doc.id}`, {
    //   method: 'PATCH',
    //   headers: { 'Content-Type': 'application/json' },
    //   body: JSON.stringify({ name: renameValue.value.trim() })
    // })
    
    doc.name = renameValue.value.trim()
    showRenameDialog.value = false
  } catch (error) {
    console.error('重命名失败:', error)
  }
}

const duplicateDocument = async (doc) => {
  try {
    // TODO: 调用API复制文档
    // const response = await fetch(`/api/documents/${doc.id}/duplicate`, {
    //   method: 'POST'
    // })
    // const newDoc = await response.json()
    
    const newDoc = {
      ...doc,
      id: Date.now().toString(),
      name: doc.name + ' (副本)',
      createdAt: Date.now(),
      updatedAt: Date.now()
    }
    
    documents.value.unshift(newDoc)
  } catch (error) {
    console.error('复制文档失败:', error)
  }
}

const shareDocument = async (doc) => {
  // TODO: 打开分享对话框
  console.log('分享文档:', doc)
}

const deleteDocument = async (doc) => {
  if (!confirm(`确定要删除文档"${doc.name}"吗？`)) {
    return
  }
  
  try {
    // TODO: 调用API删除文档
    // await fetch(`/api/documents/${doc.id}`, {
    //   method: 'DELETE'
    // })
    
    const index = documents.value.findIndex(d => d.id === doc.id)
    if (index > -1) {
      documents.value.splice(index, 1)
    }
    
    const selectedIndex = selectedDocs.value.indexOf(doc.id)
    if (selectedIndex > -1) {
      selectedDocs.value.splice(selectedIndex, 1)
    }
  } catch (error) {
    console.error('删除文档失败:', error)
  }
}

// 使用公共工具函数 formatDate 和 formatSizeBytes
const formatSize = formatSizeBytes

const getOwnerInitial = (owner) => {
  if (!owner || !owner.name) return '?'
  return owner.name.charAt(0).toUpperCase()
}

// 点击外部关闭菜单
const handleClickOutside = (event) => {
  if (contextMenu.value.show && !event.target.closest('.context-menu')) {
    contextMenu.value.show = false
  }
  if (showRenameDialog && !event.target.closest('.dialog')) {
    showRenameDialog.value = false
  }
}

// 刷新处理函数
const handleRefresh = (event) => {
  const { view } = event.detail || {}
  if (view === 'docs') {
    console.log('刷新文档库')
    loadDocuments()
    loadSpaces()
  }
}

onMounted(() => {
  loadDocuments()
  loadSpaces()
  document.addEventListener('click', handleClickOutside)
  // 监听刷新事件
  window.addEventListener('tab-refresh', handleRefresh)
})

// 清理
onUnmounted(() => {
  window.removeEventListener('tab-refresh', handleRefresh)
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.document-library-page {
  display: flex;
  height: 100vh;
  background: #f5f5f7;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 左侧边栏 */
.dl-sidebar {
  width: 240px;
  background: #ffffff;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.sidebar-user {
  padding: 20px 16px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 16px;
  flex-shrink: 0;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sidebar-nav {
  flex: 1;
  padding: 8px 0;
  display: flex;
  flex-direction: column;
}

.nav-section {
  padding: 4px 0;
}

.nav-section-bottom {
  margin-top: auto;
  border-top: 1px solid #e5e7eb;
  padding-top: 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.15s;
  position: relative;
}

.nav-item:hover {
  background: #f9fafb;
  color: #111827;
}

.nav-item.active {
  color: #2563eb;
  background: #eff6ff;
}

.nav-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: #2563eb;
}

.nav-icon {
  flex-shrink: 0;
  color: currentColor;
}

.nav-text {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
}

.nav-arrow {
  flex-shrink: 0;
  transition: transform 0.2s;
  color: currentColor;
}

.nav-arrow.expanded {
  transform: rotate(180deg);
}

.nav-submenu {
  padding-left: 44px;
  padding-top: 4px;
}

.nav-subitem {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.15s;
  font-size: 13px;
}

.nav-subitem:hover {
  background: #f9fafb;
  color: #111827;
}

.nav-subitem.active {
  color: #2563eb;
  background: #eff6ff;
}

/* 主内容区 */
.dl-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.dl-header {
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  padding: 16px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}

.dl-header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.dl-title {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.dl-stats {
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
}

.stat-item svg {
  color: #9ca3af;
}

.dl-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 搜索框、排序、视图切换样式已移至 common.css */

/* 内容区 */
.dl-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

/* 加载状态和空状态样式已移至 common.css */

/* 网格视图 */
.dl-grid-view {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

.doc-card {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  flex-direction: column;
}

.doc-card:hover {
  border-color: #2563eb;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.15);
  transform: translateY(-2px);
}

.doc-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.doc-icon {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.doc-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.15s;
}

.doc-card:hover .doc-actions {
  opacity: 1;
}

.doc-action-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: transparent;
  color: #6b7280;
  cursor: pointer;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s;
}

.doc-action-btn:hover {
  background: #f3f4f6;
  color: #111827;
}

.doc-action-btn.active {
  color: #f59e0b;
}

.doc-card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.doc-title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.doc-desc {
  font-size: 13px;
  color: #6b7280;
  margin: 0 0 12px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  flex: 1;
}

.doc-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #9ca3af;
  margin-top: auto;
}

/* 列表视图 */
.dl-list-view {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.doc-table {
  width: 100%;
  border-collapse: collapse;
}

.doc-table thead {
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
}

.doc-table th {
  padding: 12px 16px;
  text-align: left;
  font-size: 12px;
  font-weight: 600;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.col-check {
  width: 40px;
}

.col-name {
  width: 40%;
}

.col-owner {
  width: 15%;
}

.col-updated {
  width: 20%;
}

.col-size {
  width: 10%;
}

.col-actions {
  width: 60px;
}

.doc-row {
  border-bottom: 1px solid #f3f4f6;
  cursor: pointer;
  transition: background 0.15s;
}

.doc-row:hover {
  background: #f9fafb;
}

.doc-row.selected {
  background: #eff6ff;
}

.doc-row:last-child {
  border-bottom: none;
}

.doc-row td {
  padding: 12px 16px;
  font-size: 14px;
  color: #111827;
}

.doc-name-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.doc-icon-small {
  width: 32px;
  height: 32px;
  border-radius: 4px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.doc-name {
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.favorite-badge {
  width: 18px;
  height: 18px;
  border: none;
  background: transparent;
  color: #f59e0b;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.owner-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.owner-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 11px;
  font-weight: 600;
  flex-shrink: 0;
}

.action-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  color: #6b7280;
  cursor: pointer;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s;
}

.action-btn:hover {
  background: #f3f4f6;
  color: #111827;
}

/* 对话框样式已移至 common.css */

.create-options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.create-option {
  padding: 24px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  text-align: center;
}

.create-option:hover {
  border-color: #2563eb;
  background: #eff6ff;
}

.option-icon {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #2563eb;
}

.create-option h4 {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 8px 0;
}

.create-option p {
  font-size: 13px;
  color: #6b7280;
  margin: 0;
}

.rename-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.15s;
}

.rename-input:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

/* 对话框footer和按钮样式已移至 common.css */

/* 右键菜单 */
.context-menu {
  position: fixed;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  padding: 4px;
  z-index: 1001;
  min-width: 160px;
  animation: fadeIn 0.15s;
}

.context-menu-item {
  padding: 8px 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: #111827;
  cursor: pointer;
  border-radius: 4px;
  transition: background 0.15s;
}

.context-menu-item:hover {
  background: #f3f4f6;
}

.context-menu-item.danger {
  color: #dc2626;
}

.context-menu-item.danger:hover {
  background: #fef2f2;
}

.context-menu-item svg {
  flex-shrink: 0;
  color: currentColor;
}

.context-menu-divider {
  height: 1px;
  background: #e5e7eb;
  margin: 4px 0;
}
</style>

