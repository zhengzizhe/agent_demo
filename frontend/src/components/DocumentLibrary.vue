<template>
  <div class="document-library-page">
    <!-- 主内容区（作为主侧边栏的子视图） -->
    <div class="dl-main-wrapper">
      <div class="dl-main">
      <!-- Hero/Header 区域 -->
      <div class="dl-hero-section">
        <div class="dl-hero-content">
          <div class="dl-hero-left">
            <!-- 个人空间选择器 -->
            <div class="space-selector-modern" @click.stop="toggleSpaceDropdown">
              <div class="space-selector-icon-modern">
                <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                  <circle cx="10" cy="7" r="3.5" fill="currentColor" opacity="0.9"/>
                  <path d="M5 18c0-3 2.5-6 5-6s5 3 5 6" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" fill="none"/>
                </svg>
              </div>
              <div class="space-selector-content-modern">
                <div class="space-selector-name-modern">个人空间</div>
                <div class="space-selector-count-modern">{{ currentSpaceDocumentCount }}个文档</div>
              </div>
              <svg class="space-selector-arrow-modern" :class="{ expanded: showSpaceDropdown }" width="14" height="14" viewBox="0 0 14 14" fill="none">
                <path d="M3.5 5.25l3.5 3.5 3.5-3.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <!-- 空间切换下拉菜单 -->
              <transition name="space-dropdown">
                <div v-if="showSpaceDropdown" class="space-switcher-menu-modern">
                  <div 
                    class="space-switcher-item-modern"
                    :class="{ active: !currentSpace || currentSpace.id === 'all' }"
                    @click="selectSpace({ id: 'all', name: '全部' }, $event)"
                  >
                    <span class="space-switcher-item-text">全部</span>
                    <span class="space-switcher-item-count">{{ allDocuments.filter(doc => !doc.deleted).length }}</span>
                  </div>
                  <div 
                    v-for="space in spaces" 
                    :key="space.id"
                    class="space-switcher-item-modern"
                    :class="{ active: currentSpace?.id === space.id }"
                    @click="selectSpace(space, $event)"
                  >
                    <div class="space-switcher-item-icon" :style="{ background: space.color }">{{ space.icon }}</div>
                    <span class="space-switcher-item-text">{{ space.name }}</span>
                    <span class="space-switcher-item-count">{{ space.documentCount }}</span>
                  </div>
                </div>
              </transition>
            </div>
            
            <!-- 标题和统计 -->
            <div class="dl-hero-title-section">
              <h1 class="dl-hero-title">{{ currentViewTitle }}</h1>
              <div class="dl-hero-stats" v-if="!loading && sortedAndFilteredDocuments.length > 0">
                <span class="hero-stat-item">
                  {{ sortedAndFilteredDocuments.length }}个文档
                </span>
                <span class="hero-stat-divider">·</span>
                <span class="hero-stat-hint">可按标签筛选</span>
              </div>
            </div>
          </div>
          
          <div class="dl-hero-right">
            <!-- 主角式搜索框 -->
            <div class="dl-hero-search">
              <SearchBox v-model="searchQuery" placeholder="搜索文档、标签或内容..." />
            </div>
            
            <!-- 新建按钮 -->
            <button class="btn-modern btn-primary-modern" @click="showCreateDialog = true">
              <svg width="18" height="18" viewBox="0 0 16 16" fill="none">
                <path d="M8 2v12M2 8h12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
              <span>新建</span>
            </button>
          </div>
        </div>
      </div>

      <!-- 工具条区域（筛选、排序、视图切换） -->
      <div class="dl-toolbar-section">
        <div class="dl-toolbar-content">
          <!-- 左侧：筛选按钮组 -->
          <div class="toolbar-group toolbar-group-filters">
            <div class="toolbar-group-label">筛选</div>
            <div class="filter-buttons-modern">
              <div class="filter-slider" :style="filterSliderStyle"></div>
              <button 
                class="filter-btn-modern"
                :class="{ active: spaceSubView === 'all' }"
                @click="switchSpaceSubView('all')"
                data-filter="all"
                ref="filterBtnRefs"
              >
                全部
              </button>
              <button 
                class="filter-btn-modern"
                :class="{ active: spaceSubView === 'recent' }"
                @click="switchSpaceSubView('recent')"
                data-filter="recent"
                ref="filterBtnRefs"
              >
                最近
              </button>
              <button 
                class="filter-btn-modern"
                :class="{ active: spaceSubView === 'favorites' }"
                @click="switchSpaceSubView('favorites')"
                data-filter="favorites"
                ref="filterBtnRefs"
              >
                收藏
              </button>
              <button 
                class="filter-btn-modern"
                :class="{ active: spaceSubView === 'liked' }"
                @click="switchSpaceSubView('liked')"
                data-filter="liked"
                ref="filterBtnRefs"
              >
                点赞
              </button>
              <button 
                class="filter-btn-modern"
                :class="{ active: spaceSubView === 'shared' }"
                @click="switchSpaceSubView('shared')"
                data-filter="shared"
                ref="filterBtnRefs"
              >
                共享
              </button>
            </div>
          </div>
          
          <!-- 右侧：排序和视图切换 -->
          <div class="toolbar-group toolbar-group-actions">
            <div class="sort-wrapper-modern">
              <select v-model="sortBy" class="sort-select-modern" @change="handleSort">
                <option value="time">按时间</option>
                <option value="name">按名称</option>
                <option value="size">按大小</option>
              </select>
            </div>
            <ViewToggle v-model="viewMode" />
          </div>
        </div>
      </div>

      <!-- 内容区域（包含目录树和文档列表） -->
      <div class="dl-content-wrapper">
        <!-- 目录树 -->
        <DocumentTree
          :documents="treeDocuments"
          :selected-id="selectedTreeId"
          @select="handleTreeSelect"
          @folder-select="handleFolderSelect"
        />

        <!-- 文档列表 -->
        <div class="dl-content">
        <!-- 调试信息（开发时可见） -->
        <!-- <div style="padding: 10px; background: #f0f0f0; margin-bottom: 10px; font-size: 12px;">
          加载中: {{ loading }} | 
          总文档数: {{ allDocuments.length }} | 
          过滤后: {{ sortedAndFilteredDocuments.length }} | 
          当前视图: {{ currentView }} | 
          视图模式: {{ viewMode }}
        </div> -->
        
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
              
              <!-- 协作者和分享信息 -->
              <div class="doc-collaborators" v-if="doc.collaborators?.length > 0 || doc.shareLink">
                <div class="doc-collaborators-list" v-if="doc.collaborators?.length > 0">
                  <div 
                    v-for="(collab, idx) in doc.collaborators.slice(0, 3)" 
                    :key="collab.user.id"
                    class="doc-collaborator-avatar"
                    :title="collab.user.name"
                    :style="{ zIndex: 10 - idx, marginLeft: idx > 0 ? '-8px' : '0' }"
                  >
                    {{ collab.user.avatar }}
                  </div>
                  <span v-if="doc.collaborators.length > 3" class="doc-collaborator-more">
                    +{{ doc.collaborators.length - 3 }}
                  </span>
                </div>
                <div class="doc-share-badge" v-if="doc.shareLink">
                  <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                    <path d="M6 1L1 3v4l5 2 5-2V3L6 1z" stroke="currentColor" stroke-width="1.5" fill="none"/>
                  </svg>
                  已分享
                </div>
              </div>
              
              <div class="doc-meta">
                <span class="doc-time">{{ formatDate(doc.updatedAt || doc.createdAt) }}</span>
                <span class="doc-size" v-if="doc.size">{{ formatSize(doc.size) }}</span>
                <span class="doc-likes" v-if="doc.likeCount > 0">
                  <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                    <path d="M6 1l1 1 3 .5-2 2 .5 3L6 8 3 7.5l.5-3-2-2 3-.5L6 1z" 
                          :fill="doc.liked ? 'currentColor' : 'none'"
                          stroke="currentColor" 
                          stroke-width="1.5"/>
                  </svg>
                  {{ doc.likeCount }}
                </span>
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
      </div>
    </div>

    <!-- 新建文档对话框 -->
    <div v-if="showCreateDialog" class="dialog-overlay" @click="showCreateDialog = false">
      <div class="dialog create-doc-dialog" @click.stop>
        <div class="dialog-header">
          <h3 class="dialog-title">新建文档</h3>
          <button class="dialog-close" @click="closeCreateDialog">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M4 4l8 8M12 4l-8 8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
          </button>
        </div>
        <div class="dialog-content">
          <div class="form-group">
            <label class="form-label">文档名称</label>
            <input
              v-model="createDocName"
              type="text"
              class="form-input"
              placeholder="输入文档名称"
              @keyup.enter="handleCreateDocument"
              ref="createNameInputRef"
            />
          </div>
          
          <div class="form-group">
            <label class="form-label">文档类型</label>
            <div class="type-options">
              <button
                :class="['type-option', { active: createDocType === 'text' }]"
                @click="createDocType = 'text'"
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                  <rect x="4" y="4" width="16" height="16" rx="2" stroke="currentColor" stroke-width="1.5" fill="none"/>
                  <path d="M8 8h8M8 12h8M8 16h4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
                文本文档
              </button>
              <button
                :class="['type-option', { active: createDocType === 'markdown' }]"
                @click="createDocType = 'markdown'"
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                  <rect x="4" y="4" width="16" height="16" rx="2" stroke="currentColor" stroke-width="1.5" fill="none"/>
                  <path d="M8 8h8M8 12h8M8 16h12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
                Markdown
              </button>
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">文档内容</label>
            <textarea
              v-model="createDocContent"
              class="form-textarea"
              placeholder="输入文档内容（可选）"
              rows="8"
              ref="createContentInputRef"
            ></textarea>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-secondary" @click="closeCreateDialog">取消</button>
          <button class="btn btn-primary" @click="handleCreateDocument">创建</button>
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

    <!-- 消息提示 Toast -->
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

    <!-- 分享对话框 -->
    <ShareDialog
      :visible="showShareDialog"
      :document="shareDocument"
      @update:visible="showShareDialog = $event"
      @save="handleShareSave"
    />

    <!-- 文档编辑器（使用 Teleport 全屏显示） -->
    <DocumentEditorPage
      v-if="editingDocument"
      :document-id="editingDocument.id"
      :document="editingDocument"
      @close="closeEditor"
      @update="handleDocumentUpdate"
      @save="handleDocumentSave"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import SearchBox from './SearchBox.vue'
import ViewToggle from './ViewToggle.vue'
import LoadingState from './LoadingState.vue'
import EmptyState from './EmptyState.vue'
import MessageToast from './MessageToast.vue'
import ErrorDialog from './ErrorDialog.vue'
import ShareDialog from './ShareDialog.vue'
import DocumentTree from './DocumentTree.vue'
import DocumentEditorPage from './DocumentEditorPage.vue'
import { formatDate, formatSizeBytes } from '../utils/format.js'
import { useSession } from '../composables/useSession.js'
import { generateDocuments, generateSpaces, generateTasks, generateCalendarEvents } from '../utils/mockData.js'

// Props: 接收外部传入的空间ID或视图类型
const props = defineProps({
  spaceId: {
    type: String,
    default: null
  },
  viewType: {
    type: String,
    default: null
  }
})

// 会话管理
const session = useSession()

// 响应式数据
const currentView = ref('home') // home, recent, favorites, liked, shared, knowledge, tasks, calendar, trash
const currentSpace = ref(null)
const spaceSubView = ref('all') // all, recent, favorites, liked, shared (空间子视图)
const spaces = ref([])
const showSpaces = ref(false)
const showSpaceDropdown = ref(false)
const documents = ref([])
const allDocuments = ref([]) // 所有文档（包括假数据）
const tasks = ref([])
const calendarEvents = ref([])
const loading = ref(false)
const searchQuery = ref('')
const sortBy = ref('time') // time, name, size
const viewMode = ref('grid') // grid, list
const selectedDocs = ref([])
const selectedTreeId = ref(null)
const selectedFolderId = ref(null)
const showCreateDialog = ref(false)
const showRenameDialog = ref(false)
const showShareDialog = ref(false)
const shareDocument = ref(null)
const renameValue = ref('')
const renameInputRef = ref(null)
const editingDocument = ref(null)
const createDocName = ref('')
const createDocContent = ref('')
const createDocType = ref('text') // 'text' or 'markdown'
const createNameInputRef = ref(null)
const createContentInputRef = ref(null)
const contextMenu = ref({
  show: false,
  x: 0,
  y: 0,
  doc: null
})

// 消息提示相关
const showToast = ref(false)
const toastMessage = ref('')
const toastType = ref('info') // 'info', 'success'

// 错误对话框相关
const showErrorDialog = ref(false)
const errorDialogTitle = ref('操作失败')
const errorDialogMessage = ref('')

// 获取userId（返回字符串）
const getUserId = () => {
  const userIdStr = session.userId?.value
  if (!userIdStr) return '1'
  return userIdStr
}

// 计算属性
const userInitial = computed(() => {
  return 'U'
})

const currentViewTitle = computed(() => {
  if (currentView.value === 'trash') {
    return '回收站'
  }
  
  // 文档库视图：显示空间和子视图
  if (currentView.value === 'home') {
    const subViewTitles = {
      all: '全部',
      recent: '最近',
      favorites: '收藏',
      liked: '点赞',
      shared: '共享'
    }
    
    if (currentSpace.value && currentSpace.value.id !== 'all') {
      return `${currentSpace.value.name}·${subViewTitles[spaceSubView.value] || '全部'}`
    } else {
      // 默认显示个人空间
      return `个人空间·${subViewTitles[spaceSubView.value] || '全部'}`
    }
  }
  
  return '文档库'
})

// 当前空间的文档数量（实时计算）
const currentSpaceDocumentCount = computed(() => {
  if (!currentSpace.value || currentSpace.value.id === 'all') {
    return allDocuments.value.filter(doc => !doc.deleted).length
  }
  return allDocuments.value.filter(doc => 
    doc.spaceId === currentSpace.value.id && !doc.deleted
  ).length
})

// 统计数据
const recentCount = computed(() => {
  return allDocuments.value.filter(doc => {
    const daysSinceUpdate = (Date.now() - doc.updatedAt) / (24 * 60 * 60 * 1000)
    return daysSinceUpdate <= 7
  }).length
})

const favoriteCount = computed(() => {
  return allDocuments.value.filter(doc => doc.favorite).length
})

const likedCount = computed(() => {
  return allDocuments.value.filter(doc => doc.liked).length
})

const sharedCount = computed(() => {
  return allDocuments.value.filter(doc => doc.shareLink || doc.collaborators?.length > 0).length
})

const taskCount = computed(() => {
  return tasks.value.filter(task => task.status !== 'done').length
})

const trashCount = computed(() => {
  return allDocuments.value.filter(doc => doc.deleted).length
})

// 筛选按钮滑动指示器样式
const filterSliderStyle = computed(() => {
  const filterOptions = ['all', 'recent', 'favorites', 'liked', 'shared']
  const currentIndex = filterOptions.indexOf(spaceSubView.value)
  const index = currentIndex >= 0 ? currentIndex : 0
  
  return {
    transform: `translateX(${index * 100}%)`,
    width: `${100 / filterOptions.length}%`
  }
})

// 目录树使用的文档列表（仅按空间过滤，不应用其他过滤）
const treeDocuments = computed(() => {
  let result = [...allDocuments.value]
  
  // 只显示未删除的文档
  result = result.filter(doc => !doc.deleted)
  
  // 如果选择了特定空间，按空间过滤
  if (currentSpace.value && currentSpace.value.id !== 'all') {
    result = result.filter(doc => doc.spaceId === currentSpace.value.id)
  }
  
  return result
})

const sortedAndFilteredDocuments = computed(() => {
  let result = [...allDocuments.value]
  
  // 视图过滤
  if (currentView.value === 'trash') {
    result = result.filter(doc => doc.deleted)
  } else if (currentView.value === 'home') {
    // 文档库视图
    result = result.filter(doc => !doc.deleted)
    
    // 如果选择了特定空间，按空间过滤
    if (currentSpace.value && currentSpace.value.id !== 'all') {
      result = result.filter(doc => doc.spaceId === currentSpace.value.id)
    }
    
    // 应用空间子视图过滤（无论是否选择空间）
    if (spaceSubView.value === 'recent') {
      result = result.filter(doc => {
        const daysSinceUpdate = (Date.now() - doc.updatedAt) / (24 * 60 * 60 * 1000)
        return daysSinceUpdate <= 7
      })
    } else if (spaceSubView.value === 'favorites') {
      result = result.filter(doc => doc.favorite)
    } else if (spaceSubView.value === 'liked') {
      result = result.filter(doc => doc.liked)
    } else if (spaceSubView.value === 'shared') {
      result = result.filter(doc => doc.shareLink || doc.collaborators?.length > 0)
    }
    // spaceSubView.value === 'all' 时不过滤
    
    // 如果选择了文件夹，只显示该文件夹下的文档
    if (selectedFolderId.value) {
      result = result.filter(doc => doc.parentId === selectedFolderId.value)
    }
  }
  
  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(doc => 
      doc.name?.toLowerCase().includes(query) ||
      doc.description?.toLowerCase().includes(query) ||
      doc.owner?.name?.toLowerCase().includes(query)
    )
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
  // 切换到回收站时，重置空间选择和子视图
  if (view === 'trash') {
    currentSpace.value = null
    spaceSubView.value = 'all'
  } else if (view === 'home') {
    // 切换到文档库时，确保有默认空间
    if (!currentSpace.value) {
      currentSpace.value = null // 默认显示全部
    }
  }
  loadDocuments()
}

const toggleSpaces = () => {
  showSpaces.value = !showSpaces.value
  if (showSpaces.value && spaces.value.length === 0) {
    loadSpaces()
  }
}

const toggleSpaceDropdown = () => {
  showSpaceDropdown.value = !showSpaceDropdown.value
  if (showSpaceDropdown.value && spaces.value.length === 0) {
    loadSpaces()
  }
}

const selectSpace = (space, event) => {
  // 阻止事件冒泡，避免触发外部的view-change
  if (event) {
    event.stopPropagation()
  }
  
  // 如果选择"全部"，设置为null，并重置空间子视图
  if (space.id === 'all') {
    currentSpace.value = null
    spaceSubView.value = 'all'
  } else {
    // 添加切换动画效果
    const oldSpace = currentSpace.value
    currentSpace.value = space
    // 选择空间后，默认显示"全部"子视图
    spaceSubView.value = 'all'
  }
  
  // 切换空间时，清除文件夹筛选
  selectedFolderId.value = null
  selectedTreeId.value = null
  
  // 关闭下拉菜单
  showSpaceDropdown.value = false
  
  // 切换空间时，确保在文档库视图
  if (currentView.value !== 'home') {
    currentView.value = 'home'
  }
  
  // 计算该空间的文档数量
  const spaceDocCount = space.id === 'all' 
    ? allDocuments.value.filter(doc => !doc.deleted).length
    : allDocuments.value.filter(doc => doc.spaceId === space.id && !doc.deleted).length
  
  console.log(`切换到空间: ${space.name}, 文档数: ${spaceDocCount}`)
  
  // 切换空间时，重新过滤文档（不需要重新加载，只需要触发计算属性更新）
  // sortedAndFilteredDocuments 会自动更新
}

const switchSpaceSubView = (subView) => {
  spaceSubView.value = subView
  // 切换子视图时，清除文件夹筛选，避免冲突
  if (subView !== 'all') {
    selectedFolderId.value = null
    selectedTreeId.value = null
  }
  // sortedAndFilteredDocuments 会自动更新
}

const loadSpaces = async () => {
  try {
    // 使用假数据
    spaces.value = generateSpaces()
    
    // 如果没有当前空间，默认设置为个人空间
    if (!currentSpace.value) {
      const personalSpace = spaces.value.find(s => s.id === 'personal' || s.name === '个人空间')
      if (personalSpace) {
        currentSpace.value = personalSpace
      } else if (spaces.value.length > 0) {
        currentSpace.value = spaces.value[0]
      } else {
        currentSpace.value = { id: 'personal', name: '个人空间', type: 'personal', color: '#8b5cf6', icon: '个' }
      }
    }
  } catch (error) {
    console.error('加载空间列表失败:', error)
  }
}

const loadDocuments = async () => {
  loading.value = true
  try {
    // 确保有当前空间，默认使用个人空间
    if (!currentSpace.value) {
      const personalSpace = spaces.value.find(s => s.id === 'personal' || s.name === '个人空间')
      if (personalSpace) {
        currentSpace.value = personalSpace
      } else {
        currentSpace.value = { id: 'personal', name: '个人空间', type: 'personal', color: '#8b5cf6', icon: '个' }
      }
    }
    
    // 先尝试从API加载
    try {
      const userId = getUserId()
      const response = await fetch(`/doc/list/${userId}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json'
        }
      })
      
      if (response.ok) {
        const docList = await response.json()
        // 转换后端返回的数据格式
        const apiDocs = docList.map(doc => ({
          id: doc.id,
          name: doc.name || '未命名文档',
          description: doc.text || '',
          createdAt: Date.now(),
          updatedAt: Date.now(),
          size: (doc.text || '').length,
          favorite: false,
          liked: false,
          likeCount: 0,
          collaborators: [],
          shareLink: null,
          owner: { id: doc.owner || userId, name: '当前用户' },
          spaceId: currentSpace.value.id,
          deleted: false
        }))
        
        // 合并API数据和假数据
        allDocuments.value = [...apiDocs, ...generateDocuments(30)]
      } else {
        // API失败，使用假数据
        allDocuments.value = generateDocuments(50)
      }
    } catch (apiError) {
      // API调用失败，使用假数据
      console.log('使用假数据:', apiError)
      allDocuments.value = generateDocuments(50)
    }
    
    // 确保有数据，生成50个文档
    if (allDocuments.value.length === 0) {
      console.log('生成50个假数据文档')
      allDocuments.value = generateDocuments(50)
    }
    
    console.log('加载的文档数量:', allDocuments.value.length)
    console.log('当前视图:', currentView.value)
    console.log('当前空间:', currentSpace.value?.name)
    
    // 加载任务和日历事件
    tasks.value = generateTasks()
    calendarEvents.value = generateCalendarEvents()
    
  } catch (error) {
    console.error('加载文档列表失败:', error)
    // 即使出错也生成假数据
    if (allDocuments.value.length === 0) {
      console.log('错误后生成50个假数据文档')
      allDocuments.value = generateDocuments(50)
    }
  } finally {
    loading.value = false
    console.log('文档加载完成，总数:', allDocuments.value.length)
    console.log('当前空间:', currentSpace.value?.name, '过滤后数量:', sortedAndFilteredDocuments.value.length)
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
  // 打开文档编辑器
  editingDocument.value = {
    ...doc,
    content: doc.content || '<p></p>'
  }
}

// 关闭编辑器
const closeEditor = () => {
  editingDocument.value = null
}

// 处理文档更新
const handleDocumentUpdate = (updates) => {
  if (editingDocument.value) {
    Object.assign(editingDocument.value, updates)
    // 更新文档列表中的对应文档
    const index = allDocuments.value.findIndex(d => d.id === editingDocument.value.id)
    if (index > -1) {
      Object.assign(allDocuments.value[index], updates)
    }
  }
}

// 处理文档保存
const handleDocumentSave = async (data) => {
  try {
    // TODO: 调用 API 保存文档
    console.log('保存文档:', data)
    showMessage('文档已保存', 'success')
    
    // 更新文档列表
    const index = allDocuments.value.findIndex(d => d.id === editingDocument.value.id)
    if (index > -1) {
      Object.assign(allDocuments.value[index], {
        name: data.name,
        content: data.content,
        updatedAt: new Date()
      })
    }
  } catch (error) {
    console.error('保存文档失败:', error)
    showMessage(error.message || '保存文档失败', 'error')
  }
}

// 处理目录树选择
const handleTreeSelect = (node) => {
  selectedTreeId.value = node.id
  if (node.isFolder) {
    // 选择文件夹时，切换到"全部"子视图，避免与子视图筛选冲突
    spaceSubView.value = 'all'
    selectedFolderId.value = node.id
  } else {
    // 选择文档时，清除文件夹筛选并打开文档
    selectedFolderId.value = null
    openDocument(node)
  }
}

// 处理文件夹选择
const handleFolderSelect = (folderId) => {
  // 选择文件夹时，切换到"全部"子视图
  spaceSubView.value = 'all'
  selectedFolderId.value = folderId
}

// 关闭创建对话框并重置表单
const closeCreateDialog = () => {
  showCreateDialog.value = false
  createDocName.value = ''
  createDocContent.value = ''
  createDocType.value = 'text'
}

// 处理创建文档
const handleCreateDocument = async () => {
  await createDocument(createDocType.value)
}

const createDocument = async (type) => {
  try {
    // 确保在个人空间创建文档
    if (!currentSpace.value) {
      const personalSpace = spaces.value.find(s => s.id === 'personal' || s.name === '个人空间')
      if (personalSpace) {
        currentSpace.value = personalSpace
      } else {
        currentSpace.value = { id: 'personal', name: '个人空间', type: 'personal', color: '#8b5cf6', icon: '个' }
      }
    }
    
    const userId = getUserId()
    const docName = createDocName.value.trim() || '未命名文档'
    const docContent = createDocContent.value || ''
    
    const response = await fetch('/doc/add', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        name: docName,
        text: docContent,
        type: type === 'text' ? 0 : (type === 'markdown' ? 1 : 0),
        userId: userId
      })
    })
    
    if (!response.ok) {
      let errorMessage = '创建文档失败'
      try {
        const errorData = await response.json()
        errorMessage = errorData.message || errorData.error || errorMessage
      } catch (e) {
        errorMessage = `创建文档失败: ${response.status} ${response.statusText}`
      }
      throw new Error(errorMessage)
    }
    
    const doc = await response.json()
    
    // 转换后端返回的数据格式为前端需要的格式
    const newDoc = {
      id: doc.id,
      name: doc.name || '未命名文档',
      description: doc.text || '',
      createdAt: Date.now(),
      updatedAt: Date.now(),
      size: (doc.text || '').length,
      favorite: false,
      shared: false,
      owner: { id: doc.owner || userId, name: '当前用户' },
      spaceId: currentSpace.value.id
    }
    
    allDocuments.value.unshift(newDoc)
    closeCreateDialog()
    showMessage('文档创建成功', 'success')
    openDocument(newDoc)
  } catch (error) {
    console.error('创建文档失败:', error)
    showMessage(error.message || '创建文档失败', 'error')
  }
}

// 显示消息提示
const showMessage = (message, type = 'info') => {
  if (type === 'error') {
    // 错误使用错误对话框
    errorDialogMessage.value = message
    showErrorDialog.value = true
  } else {
    // 成功和信息使用右上角 Toast
    toastMessage.value = message
    toastType.value = type
    showToast.value = true
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
      await openShareDialog(doc)
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
    
    allDocuments.value.unshift(newDoc)
    showMessage('文档已复制', 'success')
  } catch (error) {
    console.error('复制文档失败:', error)
  }
}

const openShareDialog = async (doc) => {
  shareDocument.value = doc
  showShareDialog.value = true
}

const handleShareSave = (shareData) => {
  const doc = shareDocument.value
  if (!doc) return
  
  // 更新文档的分享信息
  const docIndex = allDocuments.value.findIndex(d => d.id === doc.id)
  if (docIndex > -1) {
    allDocuments.value[docIndex] = {
      ...allDocuments.value[docIndex],
      shareLink: shareData.shareLink,
      collaborators: shareData.collaborators
    }
    showMessage('分享设置已保存', 'success')
  }
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
    
    const index = allDocuments.value.findIndex(d => d.id === doc.id)
    if (index > -1) {
      // 标记为已删除，而不是真正删除
      allDocuments.value[index].deleted = true
      allDocuments.value[index].deletedAt = Date.now()
    }
    
    const selectedIndex = selectedDocs.value.indexOf(doc.id)
    if (selectedIndex > -1) {
      selectedDocs.value.splice(selectedIndex, 1)
    }
    
    showMessage('文档已移至回收站', 'success')
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
  if (showSpaceDropdown && !event.target.closest('.space-selector-dropdown')) {
    showSpaceDropdown.value = false
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

// 监听创建对话框打开，自动聚焦到名称输入框
watch(showCreateDialog, (newVal) => {
  if (newVal) {
    nextTick(() => {
      createNameInputRef.value?.focus()
    })
  }
})

onMounted(async () => {
  // 先加载空间，确保个人空间存在
  await loadSpaces()
  
  // 如果外部传入了spaceId，自动选择该空间
  if (props.spaceId) {
    const space = spaces.value.find(s => s.id === props.spaceId)
    if (space) {
      currentSpace.value = space
      currentView.value = 'home'
      console.log('外部传入空间ID:', props.spaceId, '空间名称:', space.name)
    }
  } else if (!currentSpace.value) {
    // 确保默认选中个人空间
    const personalSpace = spaces.value.find(s => s.id === 'personal' || s.name === '个人空间')
    if (personalSpace) {
      currentSpace.value = personalSpace
    } else {
      currentSpace.value = { id: 'personal', name: '个人空间', type: 'personal', color: '#8b5cf6', icon: '个' }
    }
  }
  
  // 如果外部传入了viewType，切换到对应视图
  if (props.viewType) {
    if (props.viewType.startsWith('docs-')) {
      const view = props.viewType.replace('docs-', '')
      if (['recent', 'favorites', 'shared', 'my', 'templates', 'trash'].includes(view)) {
        currentView.value = view === 'my' ? 'home' : view
      }
    }
  }
  
  // 加载文档列表
  await loadDocuments()
  
  // 确保有数据，生成50个文档
  if (allDocuments.value.length === 0) {
    console.warn('文档列表为空，生成50个假数据文档')
    allDocuments.value = generateDocuments(50)
  }
  
  console.log('组件挂载完成，文档总数:', allDocuments.value.length)
  console.log('当前空间:', currentSpace.value?.name)
  console.log('当前视图:', currentView.value)
  console.log('个人空间文档数:', allDocuments.value.filter(doc => doc.spaceId === 'personal').length)
  
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
  padding: 0;
}

.dl-main-wrapper {
  display: flex;
  flex: 1;
  overflow: hidden;
  min-width: 0;
  align-items: stretch;
  position: relative;
  gap: 0;
  height: 100vh;
}

/* 顶部导航栏 */
.dl-top-nav {
  background: rgba(255, 255, 255, 0.5);
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  padding: 12px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.dl-nav-left {
  display: flex;
  align-items: center;
  gap: 24px;
  flex: 1;
}

.dl-view-tabs {
  display: flex;
  align-items: center;
  gap: 4px;
}

.view-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border: none;
  background: transparent;
  color: #6b7280;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.view-tab:hover {
  background: rgba(0, 0, 0, 0.04);
  color: #111827;
}

.view-tab.active {
  background: rgba(22, 93, 255, 0.1);
  color: var(--theme-accent, #165dff);
}

.view-tab svg {
  flex-shrink: 0;
}

.tab-badge {
  font-size: 11px;
  font-weight: 600;
  color: #6b7280;
  background: #f3f4f6;
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
}

.view-tab.active .tab-badge {
  background: rgba(22, 93, 255, 0.15);
  color: var(--theme-accent, #165dff);
}

/* Hero/Header 区域 - 增强玻璃拟态 */
.dl-hero-section {
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.4) 0%, 
    rgba(255, 255, 255, 0.2) 50%,
    rgba(255, 255, 255, 0.3) 100%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.3);
  border-top: 1px solid rgba(255, 255, 255, 0.5);
  padding: 20px 32px 16px 32px;
  position: relative;
  z-index: 10;
  backdrop-filter: blur(40px) saturate(200%) brightness(1.1);
  -webkit-backdrop-filter: blur(40px) saturate(200%) brightness(1.1);
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.8),
    inset 0 -1px 0 rgba(255, 255, 255, 0.3);
}

.dl-hero-section::before {
  content: '';
  position: absolute;
  inset: 0;
  background: 
    radial-gradient(circle at 20% 50%, rgba(22, 93, 255, 0.08) 0%, transparent 50%),
    radial-gradient(circle at 80% 50%, rgba(76, 127, 255, 0.06) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
  opacity: 0;
  animation: heroGlow 8s ease-in-out infinite;
}

@keyframes heroGlow {
  0%, 100% { opacity: 0; }
  50% { opacity: 1; }
}

.dl-hero-content {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  max-width: 100%;
  position: relative;
  z-index: 1;
}

.dl-hero-left {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  flex: 1;
  min-width: 0;
}

.dl-hero-title-section {
  flex: 1;
  min-width: 0;
}

.dl-hero-title {
  font-size: 24px;
  font-weight: 700;
  background: linear-gradient(135deg, 
    #165dff 0%, 
    #4c7fff 50%,
    #6b8eff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 6px 0;
  letter-spacing: -0.02em;
  line-height: 1.2;
  position: relative;
  display: inline-block;
  filter: drop-shadow(0 2px 4px rgba(22, 93, 255, 0.2));
}

.dl-hero-stats {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #6b7280;
}

.hero-stat-item {
  font-weight: 500;
  color: #374151;
}

.hero-stat-divider {
  color: #d1d5db;
}

.hero-stat-hint {
  color: #9ca3af;
}

.dl-hero-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.dl-hero-search {
  min-width: 320px;
  max-width: 480px;
  flex: 1;
}

/* 工具条区域 - 增强玻璃拟态 */
.dl-toolbar-section {
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.3) 0%, 
    rgba(255, 255, 255, 0.2) 100%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  border-top: 1px solid rgba(255, 255, 255, 0.4);
  padding: 12px 32px;
  backdrop-filter: blur(30px) saturate(200%) brightness(1.05);
  -webkit-backdrop-filter: blur(30px) saturate(200%) brightness(1.05);
  box-shadow: 
    0 4px 16px rgba(0, 0, 0, 0.04),
    inset 0 1px 0 rgba(255, 255, 255, 0.6),
    inset 0 -1px 0 rgba(255, 255, 255, 0.2);
  position: relative;
}

.dl-toolbar-section::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, 
    transparent 0%,
    rgba(22, 93, 255, 0.03) 50%,
    transparent 100%);
  pointer-events: none;
  z-index: 0;
  animation: toolbarShimmer 3s ease-in-out infinite;
}

@keyframes toolbarShimmer {
  0%, 100% { transform: translateX(-100%); opacity: 0; }
  50% { transform: translateX(100%); opacity: 1; }
}

.dl-toolbar-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  position: relative;
  z-index: 1;
}

.toolbar-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.toolbar-group-label {
  font-size: 13px;
  font-weight: 600;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-right: 4px;
}

.toolbar-group-filters {
  flex: 1;
}

.toolbar-group-actions {
  flex-shrink: 0;
}

/* ViewToggle 在工具栏中的样式 */
.toolbar-group-actions :deep(.view-toggle) {
  display: inline-flex;
}

.toolbar-group-actions :deep(.view-toggle-container) {
  height: 36px;
}

.toolbar-group-actions :deep(.view-btn) {
  width: 32px;
  height: 32px;
}

/* 现代化个人空间选择器 - 增强玻璃拟态 */
.space-selector-modern {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 24px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.25) 0%, 
    rgba(255, 255, 255, 0.15) 50%,
    rgba(255, 255, 255, 0.2) 100%);
  border: 1.5px solid rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(50px) saturate(200%) brightness(1.15);
  -webkit-backdrop-filter: blur(50px) saturate(200%) brightness(1.15);
  box-shadow: 
    0 4px 16px rgba(0, 0, 0, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.9),
    inset 0 -1px 0 rgba(255, 255, 255, 0.3),
    0 0 0 0 rgba(22, 93, 255, 0);
  overflow: hidden;
}

.space-selector-modern::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 24px;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.4) 0%, 
    rgba(255, 255, 255, 0.15) 50%,
    rgba(255, 255, 255, 0.3) 100%);
  pointer-events: none;
  z-index: 0;
  opacity: 0.8;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

.space-selector-modern::after {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(22, 93, 255, 0.1) 0%, transparent 70%);
  pointer-events: none;
  z-index: 0;
  opacity: 0;
  transition: opacity 0.4s;
}

.space-selector-modern:hover {
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.35) 0%, 
    rgba(255, 255, 255, 0.25) 50%,
    rgba(255, 255, 255, 0.3) 100%);
  border-color: rgba(255, 255, 255, 0.7);
  box-shadow: 
    0 6px 20px rgba(22, 93, 255, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 1),
    inset 0 -1px 0 rgba(255, 255, 255, 0.4),
    0 0 15px rgba(22, 93, 255, 0.1);
  transform: translateY(-2px) scale(1.02);
  backdrop-filter: blur(60px) saturate(200%) brightness(1.2);
  -webkit-backdrop-filter: blur(60px) saturate(200%) brightness(1.2);
}

.space-selector-modern:hover::after {
  opacity: 1;
  animation: selectorPulse 2s ease-in-out infinite;
}

@keyframes selectorPulse {
  0%, 100% { transform: scale(1); opacity: 0.3; }
  50% { transform: scale(1.1); opacity: 0.6; }
}

.space-selector-icon-modern {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
  background: linear-gradient(135deg, 
    #165dff 0%, 
    #4c7fff 50%,
    #6b8eff 100%);
  border-radius: 12px;
  padding: 6px;
  position: relative;
  z-index: 1;
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 
    0 2px 8px rgba(22, 93, 255, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.4),
    inset 0 -1px 0 rgba(22, 93, 255, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.space-selector-icon-modern::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.3) 0%, 
    transparent 50%,
    rgba(255, 255, 255, 0.1) 100%);
  pointer-events: none;
  z-index: 0;
}

.space-selector-icon-modern svg {
  position: relative;
  z-index: 1;
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.2));
}

.space-selector-modern:hover .space-selector-icon-modern {
  transform: scale(1.05);
  box-shadow: 
    0 3px 10px rgba(22, 93, 255, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.5),
    inset 0 -1px 0 rgba(22, 93, 255, 0.4);
}

.space-selector-content-modern {
  display: flex;
  flex-direction: column;
  gap: 2px;
  position: relative;
  z-index: 1;
}

.space-selector-name-modern {
  font-size: 15px;
  font-weight: 600;
  background: linear-gradient(135deg, 
    #165dff 0%, 
    #4c7fff 50%,
    #6b8eff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1.2;
  display: block;
  width: fit-content;
}

.space-selector-count-modern {
  font-size: 12px;
  color: #6b7280;
  line-height: 1.2;
}

.space-selector-arrow-modern {
  width: 14px;
  height: 14px;
  color: #6b7280;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
}

.space-selector-arrow-modern.expanded {
  transform: rotate(180deg);
}

/* 现代化搜索框样式 - 增强玻璃拟态 */
.dl-hero-search :deep(.search-wrapper) {
  width: 100%;
  height: 40px;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.5) 0%, 
    rgba(255, 255, 255, 0.3) 100%);
  border: 1.5px solid rgba(255, 255, 255, 0.4);
  border-radius: 12px;
  padding: 0 18px;
  box-shadow: 
    0 4px 16px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.8),
    0 0 0 0 rgba(22, 93, 255, 0);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(40px) saturate(200%) brightness(1.1);
  -webkit-backdrop-filter: blur(40px) saturate(200%) brightness(1.1);
  position: relative;
  overflow: hidden;
}

.dl-hero-search :deep(.search-wrapper::before) {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.6) 0%, 
    rgba(255, 255, 255, 0.3) 50%,
    rgba(255, 255, 255, 0.5) 100%);
  pointer-events: none;
  z-index: 0;
  opacity: 0.7;
}

.dl-hero-search :deep(.search-wrapper:hover) {
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.6) 0%, 
    rgba(255, 255, 255, 0.4) 100%);
  border-color: rgba(22, 93, 255, 0.3);
  box-shadow: 
    0 6px 20px rgba(22, 93, 255, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.9),
    0 0 0 0 rgba(22, 93, 255, 0);
  transform: translateY(-1px);
}

.dl-hero-search :deep(.search-wrapper:focus-within) {
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.8) 0%, 
    rgba(255, 255, 255, 0.6) 100%);
  border-color: rgba(22, 93, 255, 0.5);
  box-shadow: 
    0 12px 32px rgba(22, 93, 255, 0.25),
    inset 0 1px 0 rgba(255, 255, 255, 1),
    0 0 0 4px rgba(22, 93, 255, 0.15),
    0 0 30px rgba(22, 93, 255, 0.2);
  transform: translateY(-2px) scale(1.01);
  animation: searchGlow 2s ease-in-out infinite;
}

@keyframes searchGlow {
  0%, 100% { 
    box-shadow: 
      0 12px 32px rgba(22, 93, 255, 0.25),
      inset 0 1px 0 rgba(255, 255, 255, 1),
      0 0 0 4px rgba(22, 93, 255, 0.15),
      0 0 30px rgba(22, 93, 255, 0.2);
  }
  50% { 
    box-shadow: 
      0 12px 32px rgba(22, 93, 255, 0.3),
      inset 0 1px 0 rgba(255, 255, 255, 1),
      0 0 0 4px rgba(22, 93, 255, 0.2),
      0 0 40px rgba(22, 93, 255, 0.3);
  }
}

.dl-hero-search :deep(.search-icon) {
  width: 18px;
  height: 18px;
  color: #6b7280;
  margin-right: 12px;
  position: relative;
  z-index: 1;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.dl-hero-search :deep(.search-wrapper:focus-within .search-icon) {
  color: #165dff;
  transform: scale(1.1);
  filter: drop-shadow(0 2px 4px rgba(22, 93, 255, 0.3));
}

.dl-hero-search :deep(.search-input) {
  font-size: 15px;
  font-weight: 400;
  position: relative;
  z-index: 1;
  background: transparent;
}

.dl-hero-search :deep(.search-clear) {
  width: 24px;
  height: 24px;
  border-radius: 6px;
}

/* 现代化筛选按钮 - 增强玻璃拟态 */
.filter-buttons-modern {
  position: relative;
  display: inline-flex;
  align-items: center;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.4) 0%, 
    rgba(255, 255, 255, 0.3) 100%);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 12px;
  padding: 4px;
  gap: 0;
  backdrop-filter: blur(40px) saturate(200%) brightness(1.1);
  -webkit-backdrop-filter: blur(40px) saturate(200%) brightness(1.1);
  box-shadow: 
    0 4px 16px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.8),
    inset 0 -1px 0 rgba(255, 255, 255, 0.3);
  overflow: hidden;
}

.filter-buttons-modern::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 12px;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.6) 0%, 
    rgba(255, 255, 255, 0.3) 50%,
    rgba(255, 255, 255, 0.5) 100%);
  pointer-events: none;
  z-index: 0;
  opacity: 0.7;
  animation: filterShimmer 4s ease-in-out infinite;
}

@keyframes filterShimmer {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 0.8; }
}

.filter-slider {
  position: absolute;
  top: 4px;
  left: 4px;
  height: calc(100% - 8px);
  background: linear-gradient(135deg, 
    #165dff 0%, 
    #4c7fff 50%,
    #6b8eff 100%);
  border-radius: 8px;
  transition: all 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
  box-shadow: 
    0 6px 20px rgba(22, 93, 255, 0.4),
    0 3px 10px rgba(22, 93, 255, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.6),
    0 0 20px rgba(22, 93, 255, 0.3);
  z-index: 1;
  overflow: hidden;
}

.filter-slider::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.4) 0%, 
    transparent 50%,
    rgba(255, 255, 255, 0.2) 100%);
  pointer-events: none;
  animation: sliderShine 3s ease-in-out infinite;
}

@keyframes sliderShine {
  0% { transform: translateX(-100%) translateY(-100%) rotate(45deg); }
  100% { transform: translateX(200%) translateY(200%) rotate(45deg); }
}

.filter-btn-modern {
  position: relative;
  z-index: 2;
  padding: 8px 16px;
  border: none;
  background: transparent;
  color: #6b7280;
  font-size: 13px;
  font-weight: 500;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  white-space: nowrap;
  flex: 1;
  min-width: 0;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.filter-btn-modern:hover {
  color: #111827;
  transform: translateY(-2px) scale(1.05);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.filter-btn-modern.active {
  color: white;
  font-weight: 600;
  text-shadow: 
    0 1px 3px rgba(0, 0, 0, 0.3),
    0 0 10px rgba(255, 255, 255, 0.3);
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

/* 现代化按钮系统 */
.btn-modern {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 0;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;
  position: relative;
  overflow: hidden;
  height: 40px;
  min-width: 40px;
}

.btn-modern::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.2) 0%, 
    rgba(255, 255, 255, 0) 100%);
  opacity: 0;
  transition: opacity 0.3s;
}

.btn-modern:hover::before {
  opacity: 1;
}

.btn-modern svg {
  flex-shrink: 0;
  width: 18px;
  height: 18px;
}

.btn-primary-modern {
  background: linear-gradient(135deg, 
    #165dff 0%, 
    #4c7fff 50%,
    #6b8eff 100%);
  color: white;
  padding: 0 24px;
  box-shadow: 
    0 6px 20px rgba(22, 93, 255, 0.4),
    0 3px 10px rgba(22, 93, 255, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.3),
    0 0 20px rgba(22, 93, 255, 0.2);
  position: relative;
  overflow: hidden;
}

.btn-primary-modern::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.3) 0%, 
    transparent 50%,
    rgba(255, 255, 255, 0.1) 100%);
  pointer-events: none;
  opacity: 0.6;
}

.btn-primary-modern::after {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.3) 0%, transparent 70%);
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.4s;
  animation: buttonShine 3s ease-in-out infinite;
}

@keyframes buttonShine {
  0%, 100% { opacity: 0; transform: translate(-50%, -50%) scale(0.8); }
  50% { opacity: 0.4; transform: translate(-50%, -50%) scale(1.2); }
}

.btn-primary-modern:hover {
  background: linear-gradient(135deg, 
    #0f4fd8 0%, 
    #3d6eff 50%,
    #5a7eff 100%);
  transform: translateY(-2px) scale(1.02);
  box-shadow: 
    0 10px 30px rgba(22, 93, 255, 0.5),
    0 5px 15px rgba(22, 93, 255, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.4),
    0 0 30px rgba(22, 93, 255, 0.3);
}

.btn-primary-modern:hover::after {
  opacity: 0.6;
}

.btn-primary-modern:active {
  transform: translateY(0) scale(0.98);
  box-shadow: 
    0 4px 15px rgba(22, 93, 255, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

/* 空间区域 */
.space-section {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.space-section-label {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  white-space: nowrap;
}

/* 空间切换器 */
.space-switcher {
  position: relative;
  z-index: 1000;
}

.space-current {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  border: 2px solid rgba(22, 93, 255, 0.2);
  background: rgba(255, 255, 255, 0.9);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  min-width: 180px;
}

.space-current:hover {
  border-color: var(--theme-accent, #165dff);
  background: rgba(255, 255, 255, 1);
  box-shadow: 0 4px 12px rgba(22, 93, 255, 0.15);
  transform: translateY(-1px);
}

.space-current-icon {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  flex-shrink: 0;
  color: white;
  font-weight: 600;
}

.space-current-name {
  flex: 1;
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  text-align: left;
}

.space-current-count {
  font-size: 12px;
  color: #6b7280;
  font-weight: 400;
}

.space-switcher-arrow {
  flex-shrink: 0;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: #6b7280;
}

.space-switcher-arrow.expanded {
  transform: rotate(180deg);
}

/* 现代化空间切换菜单 */
.space-switcher-menu-modern {
  position: absolute;
  top: calc(100% + 12px);
  left: 0;
  min-width: 280px;
  max-width: 360px;
  max-height: 400px;
  overflow-y: auto;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.3) 0%, 
    rgba(255, 255, 255, 0.2) 50%,
    rgba(255, 255, 255, 0.25) 100%);
  border: 1.5px solid rgba(255, 255, 255, 0.5);
  border-radius: 24px;
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.08),
    0 4px 16px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.9),
    inset 0 -1px 0 rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(50px) saturate(200%) brightness(1.15);
  -webkit-backdrop-filter: blur(50px) saturate(200%) brightness(1.15);
  z-index: 1000;
  padding: 8px;
  overflow: hidden;
}

.space-switcher-menu-modern::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 24px;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.4) 0%, 
    rgba(255, 255, 255, 0.2) 50%,
    rgba(255, 255, 255, 0.3) 100%);
  pointer-events: none;
  z-index: 0;
  opacity: 0.7;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

.space-selector-modern {
  position: relative;
}

.space-switcher-item-modern {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 14px;
  color: #111827;
  position: relative;
  z-index: 1;
}

.space-switcher-item-modern:hover {
  background: linear-gradient(135deg, rgba(22, 93, 255, 0.1) 0%, rgba(22, 93, 255, 0.05) 100%);
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(22, 93, 255, 0.1);
}

.space-switcher-item-modern.active {
  background: linear-gradient(135deg, rgba(22, 93, 255, 0.15) 0%, rgba(22, 93, 255, 0.1) 100%);
  color: #165dff;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(22, 93, 255, 0.2);
}

.space-switcher-item-icon {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  flex-shrink: 0;
  color: white;
  font-weight: 600;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.space-switcher-item-modern:hover .space-switcher-item-icon {
  transform: scale(1.1) rotate(5deg);
}

.space-switcher-item-text {
  flex: 1;
  text-align: left;
}

.space-switcher-item-count {
  font-size: 12px;
  color: #9ca3af;
  font-weight: 400;
}

.space-switcher-item.active .space-switcher-item-count {
  color: var(--theme-accent, #165dff);
}

/* 下拉菜单动画 */
.space-dropdown-enter-active,
.space-dropdown-leave-active {
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.space-dropdown-enter-from {
  opacity: 0;
  transform: translateY(-12px) scale(0.96);
}

.space-dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px) scale(0.98);
}

.space-nav-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}

.space-nav-search {
  min-width: 200px;
  max-width: 320px;
  flex: 1;
}

.space-nav-search :deep(.search-wrapper) {
  width: 100%;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.space-nav-search :deep(.search-wrapper:focus-within) {
  background: rgba(255, 255, 255, 0.95);
  border-color: var(--theme-accent, #165dff);
  box-shadow: 0 0 0 3px rgba(22, 93, 255, 0.1);
}

.space-nav-action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  background: rgba(255, 255, 255, 0.6);
  color: #6b7280;
  font-size: 13px;
  font-weight: 500;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;
}

.space-nav-action-btn:hover {
  background: rgba(255, 255, 255, 0.8);
  border-color: rgba(0, 0, 0, 0.15);
  color: #111827;
}

.space-nav-action-btn.active {
  background: rgba(22, 93, 255, 0.1);
  border-color: var(--theme-accent, #165dff);
  color: var(--theme-accent, #165dff);
}

.space-nav-action-btn.primary {
  background: var(--theme-accent, #165dff);
  border-color: var(--theme-accent, #165dff);
  color: white;
}

.space-nav-action-btn.primary:hover {
  background: color-mix(in srgb, var(--theme-accent, #165dff) 90%, black);
  border-color: color-mix(in srgb, var(--theme-accent, #165dff) 90%, black);
}

.space-nav-action-btn svg {
  flex-shrink: 0;
}

.action-badge {
  font-size: 11px;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.8);
  color: #6b7280;
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
}

.space-nav-action-btn.active .action-badge {
  background: rgba(22, 93, 255, 0.15);
  color: var(--theme-accent, #165dff);
}


.dl-nav-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nav-action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  background: var(--theme-accent, #165dff);
  color: white;
  font-size: 14px;
  font-weight: 500;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.nav-action-btn:hover {
  background: color-mix(in srgb, var(--theme-accent, #165dff) 90%, black);
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(22, 93, 255, 0.2);
}

/* 主内容区 */
.dl-main {
  flex: 1;
  min-width: 0;
  height: calc(100vh - 24px);
  margin: 0px 0px 0px 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
  z-index: 1;
  background: var(--theme-background-gradient, none);
  background-color: var(--theme-background, #f5f5f7);
  border: none;
  border-radius: 16px;
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  box-shadow: 
    0 0 0 1px rgba(0, 0, 0, 0.03) inset,
    0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

/* 现代化排序选择器 */
.sort-wrapper-modern {
  position: relative;
  display: inline-flex;
  align-items: center;
}

.sort-select-modern {
  padding: 10px 44px 10px 18px;
  font-size: 13px;
  font-weight: 500;
  color: #111827;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.5) 0%, 
    rgba(255, 255, 255, 0.3) 100%);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 10px;
  cursor: pointer;
  outline: none;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 
    0 4px 16px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(30px) saturate(200%) brightness(1.1);
  -webkit-backdrop-filter: blur(30px) saturate(200%) brightness(1.1);
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg width='12' height='12' viewBox='0 0 12 12' fill='none' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M3 4.5l3 3 3-3' stroke='%236b7280' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 14px center;
  min-width: 130px;
  height: 36px;
  position: relative;
  overflow: hidden;
}

.sort-select-modern::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.6) 0%, 
    rgba(255, 255, 255, 0.3) 50%,
    rgba(255, 255, 255, 0.5) 100%);
  pointer-events: none;
  z-index: 0;
  opacity: 0.7;
}

.sort-select-modern:hover {
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.7) 0%, 
    rgba(255, 255, 255, 0.5) 100%);
  border-color: rgba(22, 93, 255, 0.3);
  box-shadow: 
    0 6px 20px rgba(22, 93, 255, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  transform: translateY(-1px);
}

.sort-select-modern:focus {
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.8) 0%, 
    rgba(255, 255, 255, 0.6) 100%);
  border-color: rgba(22, 93, 255, 0.5);
  box-shadow: 
    0 8px 24px rgba(22, 93, 255, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 1),
    0 0 0 3px rgba(22, 93, 255, 0.15),
    0 0 20px rgba(22, 93, 255, 0.2);
}


/* 搜索框、排序、视图切换样式已移至 common.css */

/* 内容区域包装器 */
.dl-content-wrapper {
  flex: 1;
  display: flex;
  overflow: hidden;
  gap: 0;
  min-width: 0;
  background: transparent;
}

/* 内容区 */
.dl-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px 32px;
  background: transparent;
  min-width: 0;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
}

/* 隐藏滚动条但保持滚动功能 */
.dl-content::-webkit-scrollbar {
  width: 0;
  display: none;
}

/* 加载状态和空状态样式已移至 common.css */

/* 网格视图 */
.dl-grid-view {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.doc-card {
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 20px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  box-shadow: 
    0 2px 8px rgba(0, 0, 0, 0.06),
    0 1px 4px rgba(0, 0, 0, 0.04);
  position: relative;
  overflow: hidden;
}

.doc-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--theme-accent, #165dff) 0%, #4c7fff 100%);
  opacity: 0;
  transition: opacity 0.3s;
}

.doc-card:hover {
  background: rgba(255, 255, 255, 1);
  border-color: rgba(22, 93, 255, 0.3);
  box-shadow: 
    0 12px 32px rgba(0, 0, 0, 0.12),
    0 6px 16px rgba(22, 93, 255, 0.2),
    0 0 0 1px rgba(22, 93, 255, 0.1);
  transform: translateY(-6px) scale(1.02);
}

.doc-card:hover::before {
  opacity: 1;
}

.doc-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.doc-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--theme-accent, #165dff) 0%, #4c7fff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
  box-shadow: 
    0 4px 12px rgba(22, 93, 255, 0.25),
    0 2px 4px rgba(22, 93, 255, 0.15);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  transform: translateZ(0);
  will-change: transform;
  position: relative;
  overflow: hidden;
}

.doc-icon::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.3) 0%, 
    rgba(255, 255, 255, 0) 100%);
  opacity: 0;
  transition: opacity 0.3s;
}

.doc-card:hover .doc-icon {
  transform: scale(1.08) rotate(3deg);
  box-shadow: 
    0 8px 20px rgba(22, 93, 255, 0.4),
    0 4px 8px rgba(22, 93, 255, 0.25);
}

.doc-card:hover .doc-icon::before {
  opacity: 1;
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
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(255, 255, 255, 0.8);
  color: #6b7280;
  cursor: pointer;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.doc-action-btn:hover {
  background: rgba(255, 255, 255, 1);
  color: #111827;
  transform: translateY(-2px) scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.doc-action-btn:active {
  transform: translateY(0) scale(1.05);
}

.doc-action-btn.active {
  color: #f59e0b;
  background: rgba(245, 158, 11, 0.1);
}

.doc-card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.doc-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 10px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
  letter-spacing: -0.01em;
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

.doc-collaborators {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 8px 0;
  min-height: 24px;
}

.doc-collaborators-list {
  display: flex;
  align-items: center;
}

.doc-collaborator-avatar {
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
  border: 2px solid white;
  flex-shrink: 0;
}

.doc-collaborator-more {
  font-size: 11px;
  color: #6b7280;
  margin-left: 4px;
}

.doc-share-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #165dff;
  background: #eff6ff;
  padding: 4px 8px;
  border-radius: 4px;
}

.doc-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #9ca3af;
  margin-top: auto;
}

.doc-likes {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #f59e0b;
}

/* 列表视图 */
.dl-list-view {
  background: transparent;
  border: none;
  border-radius: 0;
  overflow: visible;
}

.doc-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
}

.doc-table thead {
  background: transparent;
  backdrop-filter: none;
  -webkit-backdrop-filter: none;
  position: sticky;
  top: 0;
  z-index: 10;
}

.doc-table th {
  padding: 14px 20px;
  text-align: left;
  font-size: 12px;
  font-weight: 600;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-bottom: 2px solid rgba(0, 0, 0, 0.06);
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
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: transform 0.2s cubic-bezier(0.4, 0, 0.2, 1), background 0.2s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  background: rgba(255, 255, 255, 0.5);
  position: relative;
  transform: translateZ(0);
  will-change: transform, background;
}

.doc-row::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: linear-gradient(180deg, var(--theme-accent, #165dff) 0%, #4c7fff 100%);
  opacity: 0;
  transition: opacity 0.2s;
}

.doc-row:hover {
  background: rgba(255, 255, 255, 0.95);
  transform: translateX(4px);
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.08),
    0 2px 4px rgba(22, 93, 255, 0.1);
}

.doc-row:hover::before {
  opacity: 1;
}

.doc-row.selected {
  background: rgba(22, 93, 255, 0.08);
}

.doc-row.selected::before {
  opacity: 1;
}

.doc-row td {
  padding: 16px 20px;
  font-size: 14px;
  color: #111827;
  vertical-align: middle;
}

.doc-name-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.doc-icon-small {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, var(--theme-accent, #165dff) 0%, #4c7fff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
  box-shadow: 
    0 2px 8px rgba(22, 93, 255, 0.2),
    0 1px 3px rgba(22, 93, 255, 0.1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.doc-row:hover .doc-icon-small {
  transform: scale(1.15) rotate(2deg);
  box-shadow: 
    0 6px 16px rgba(22, 93, 255, 0.35),
    0 3px 6px rgba(22, 93, 255, 0.2);
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
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--theme-accent, #165dff) 0%, #4c7fff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
  box-shadow: 0 2px 4px rgba(22, 93, 255, 0.2);
}

.action-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: rgba(255, 255, 255, 0.8);
  color: #6b7280;
  cursor: pointer;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.action-btn:hover {
  background: rgba(255, 255, 255, 1);
  color: #111827;
  transform: translateY(-2px) scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.action-btn:active {
  transform: translateY(0) scale(1.05);
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
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  text-align: center;
}

.create-option:hover {
  border-color: var(--theme-accent, #165dff);
  background: color-mix(in srgb, var(--theme-accent, #165dff) 10%, transparent);
}

.option-icon {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--theme-accent, #165dff);
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

/* 创建文档表单样式 */
.create-doc-dialog {
  max-width: 600px;
  width: 90%;
}

.form-group {
  margin-bottom: 20px;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #111827;
  margin-bottom: 8px;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  font-family: inherit;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  background: var(--theme-background-gradient, none);
  background-color: var(--theme-background, #f5f5f7);
  color: var(--theme-text, #111827);
  box-sizing: border-box;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: var(--theme-accent, #165dff);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--theme-accent, #165dff) 15%, transparent);
}

.form-textarea {
  resize: vertical;
  min-height: 120px;
  line-height: 1.5;
}

.type-options {
  display: flex;
  gap: 12px;
}

.type-option {
  flex: 1;
  padding: 12px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 6px;
  background: var(--theme-background-gradient, none);
  background-color: var(--theme-background, #f5f5f7);
  color: #6b7280;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.type-option:hover {
  border-color: var(--theme-accent, #165dff);
  color: var(--theme-accent, #165dff);
}

.type-option.active {
  border-color: var(--theme-accent, #165dff);
  background: color-mix(in srgb, var(--theme-accent, #165dff) 10%, transparent);
  color: var(--theme-accent, #165dff);
}

.type-option svg {
  flex-shrink: 0;
}

.rename-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.rename-input:focus {
  outline: none;
  border-color: var(--theme-accent, #165dff);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--theme-accent, #165dff) 15%, transparent);
}

/* 对话框footer和按钮样式已移至 common.css */

/* 右键菜单 */
.context-menu {
  position: fixed;
  background: var(--theme-background-gradient, none);
  background-color: var(--theme-background, #f5f5f7);
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

