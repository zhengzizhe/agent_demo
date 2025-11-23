<template>
  <div class="document-editor-container">
    <!-- Craft 风格三栏布局 -->
    <div class="editor-layout">
      <!-- 左侧：目录/大纲（侧边栏） -->
      <Transition name="slide-left">
        <OutlinePanel
          v-if="showOutline"
          :document-title="documentTitle"
          :editor="editor"
          @close="showOutline = false"
        />
      </Transition>
      <button v-if="!showOutline" @click="showOutline = true" class="sidebar-toggle left-toggle" title="显示目录">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <path d="M6 4l4 4-4 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
      
      <!-- 中间：编辑器内容区 -->
      <div class="editor-main-area">
        <!-- 格式面板（悬浮框，出现在按钮左边） -->
        <Teleport to="body">
          <Transition name="slide-right">
            <FormatPanel
              v-if="showFormatPanel"
              :editor="editor"
              @close="showFormatPanel = false"
            />
          </Transition>
        </Teleport>
        <!-- 协同编辑开关和格式按钮 - 使用 Teleport 传送到 body，确保固定在视口 -->
        <Teleport to="body">
          <!-- 协同编辑开关 -->
          <button 
            @click="toggleCollaboration" 
            class="collaboration-toggle" 
            :class="{ active: collaborationEnabled }"
            :title="collaborationEnabled ? '关闭协同编辑' : '开启协同编辑'"
          >
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path 
                v-if="collaborationEnabled"
                d="M8 2C10.2091 2 12 3.79086 12 6C12 7.31371 11.4813 8.50314 10.6063 9.36396L8 12L5.39373 9.36396C4.51872 8.50314 4 7.31371 4 6C4 3.79086 5.79086 2 8 2Z" 
                fill="currentColor"
              />
              <path 
                v-else
                d="M8 2C10.2091 2 12 3.79086 12 6C12 7.31371 11.4813 8.50314 10.6063 9.36396L8 12L5.39373 9.36396C4.51872 8.50314 4 7.31371 4 6C4 3.79086 5.79086 2 8 2Z" 
                stroke="currentColor" 
                stroke-width="1.5" 
                fill="none"
              />
              <circle v-if="collaborationEnabled && isConnected" cx="8" cy="6" r="1.5" fill="#10b981"/>
              <circle v-else-if="collaborationEnabled && !isConnected" cx="8" cy="6" r="1.5" fill="#f59e0b"/>
            </svg>
            <span class="collaboration-toggle-label">{{ collaborationEnabled ? '协同' : '单机' }}</span>
          </button>
          
          <!-- 格式面板切换按钮（常驻显示） -->
          <button @click="toggleFormatPanel" class="format-panel-toggle" :class="{ active: showFormatPanel }" :title="showFormatPanel ? '隐藏格式面板' : '显示格式面板'">
            <div class="format-toggle-icon">
              <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
                <path d="M3 6h12M3 9h12M3 12h9" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                <circle cx="13.5" cy="6" r="1.5" fill="currentColor"/>
                <circle cx="13.5" cy="9" r="1.5" fill="currentColor"/>
                <circle cx="12" cy="12" r="1.5" fill="currentColor"/>
              </svg>
            </div>
            <span class="format-toggle-label">格式</span>
          </button>
        </Teleport>
        
        <!-- 内层突出的"页" -->
        <div class="editor-page-container">
          <!-- 封面图 -->
          <div class="page-cover" :style="{ backgroundImage: props.coverImage ? `url(${props.coverImage})` : 'none' }">
            <div v-if="!props.coverImage" class="cover-placeholder" @click="handleAddCover">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                <rect x="3" y="4" width="18" height="16" rx="2" stroke="currentColor" stroke-width="1.5" fill="none"/>
                <circle cx="9" cy="10" r="2" fill="currentColor"/>
                <path d="M3 16l6-4 4 4 8-6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
              <span>添加封面</span>
            </div>
            <button v-else @click="handleRemoveCover" class="cover-remove-btn" title="移除封面">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M4 4l8 8M12 4l-8 8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </button>
          </div>

          <!-- 文档标题和作者信息 -->
          <div class="document-header">
            <h1 class="document-title-main">{{ props.documentTitle || '未命名文档' }}</h1>
            <div class="document-author-info">
              <div class="author-avatar-small" :style="{ background: authorColor }">
                {{ props.author[0] || 'U' }}
              </div>
              <span class="author-name-small">{{ props.author }}</span>
              <span class="time-text">{{ formatTime(props.lastModified) }}</span>
            </div>
          </div>

          <!-- 编辑器内容 -->
          <div class="editor-wrapper">
            <div class="editor-content" :class="{ 
              'typing-active': isTyping,
              'inserting-active': isInserting,
              'linebreak-active': isLineBreak
            }">
              <EditorContent :editor="editor" class="editor" />
              <BlockMenu
                v-if="showBlockMenu"
                :visible="showBlockMenu"
                :position="blockMenuPosition"
                :search-query="blockMenuSearchQuery"
                @select="handleBlockMenuSelect"
                @close="showBlockMenu = false"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 评论和版本历史通过浮动面板显示 -->
    <Teleport to="body">
      <!-- 评论面板 -->
      <Transition name="panel">
        <div v-if="showComments" class="floating-panel comments-panel">
          <div class="panel-header">
            <h3>评论</h3>
            <button @click="showComments = false" class="panel-close">×</button>
          </div>
          <div class="panel-content">
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <div class="comment-author">{{ comment.author }}</div>
              <div class="comment-text">{{ comment.text }}</div>
              <div class="comment-time">{{ formatTime(comment.time) }}</div>
            </div>
          </div>
          <div class="panel-footer">
            <input
              v-model="newComment"
              @keyup.enter="addComment"
              placeholder="添加评论..."
              class="comment-input-field"
            />
            <button @click="addComment" class="comment-submit">发送</button>
          </div>
        </div>
      </Transition>

      <!-- 版本历史面板 -->
      <Transition name="panel">
        <div v-if="showHistory" class="floating-panel history-panel">
          <div class="panel-header">
            <h3>版本历史</h3>
            <button @click="showHistory = false" class="panel-close">×</button>
          </div>
          <div class="panel-content">
            <div
              v-for="version in versions"
              :key="version.id"
              class="history-item"
              :class="{ active: version.id === currentVersion }"
              @click="restoreVersion(version.id)"
            >
              <div class="history-author">{{ version.author }}</div>
              <div class="history-time">{{ formatTime(version.time) }}</div>
              <div class="history-description">{{ version.description }}</div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- 协同编辑用户列表 -->
    <div v-if="collaborators.length > 0" class="collaborators-bar">
      <span class="collaborators-label">正在编辑：</span>
      <div
        v-for="user in collaborators"
        :key="user.id"
        class="collaborator-avatar"
        :style="{ background: user.color }"
        :title="user.name"
      >
        {{ user.name[0] }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onBeforeUnmount, watch, onMounted, nextTick } from 'vue'
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Image from '@tiptap/extension-image'
import Table from '@tiptap/extension-table'
import TableRow from '@tiptap/extension-table-row'
import TableCell from '@tiptap/extension-table-cell'
import TableHeader from '@tiptap/extension-table-header'
import Link from '@tiptap/extension-link'
import TaskList from '@tiptap/extension-task-list'
import TaskItem from '@tiptap/extension-task-item'
import Placeholder from '@tiptap/extension-placeholder'
import Mention from '@tiptap/extension-mention'
import { DocumentLink } from '../extensions/DocumentLink.js'
import { useCollaboration } from '../composables/useCollaboration.js'
import { CollaborationOperation, OperationType } from '../utils/documentTypes.js'
import BlockMenu from './BlockMenu.vue'
import FormatPanel from './FormatPanel.vue'
import OutlinePanel from './OutlinePanel.vue'

const props = defineProps({
  documentId: {
    type: String,
    default: null
  },
  initialContent: {
    type: String,
    default: ''
  },
  readOnly: {
    type: Boolean,
    default: false
  },
  documentTitle: {
    type: String,
    default: '未命名文档'
  },
  coverImage: {
    type: String,
    default: null
  },
  author: {
    type: String,
    default: '未知用户'
  },
  lastModified: {
    type: [Date, String],
    default: () => new Date()
  }
})

const emit = defineEmits(['update', 'save', 'cover-change'])

const showComments = ref(false)
const showHistory = ref(false)
const comments = ref([])
const newComment = ref('')
const versions = ref([])
const currentVersion = ref(null)
const showBlockMenu = ref(false)
const blockMenuPosition = ref({ top: 0, left: 0 })
const blockMenuSearchQuery = ref('')
const showOutline = ref(true)
const showFormatPanel = ref(false)

// 保存滚动位置的变量
let savedScrollPosition = null

// 切换格式面板，防止页面滚动
const toggleFormatPanel = (event) => {
  event.preventDefault()
  event.stopPropagation()
  
  // 保存当前滚动位置
  const scrollContainer = document.querySelector('.editor-main-area')
  savedScrollPosition = scrollContainer ? scrollContainer.scrollTop : window.pageYOffset
  
  showFormatPanel.value = !showFormatPanel.value
  
  // 移除按钮焦点，防止浏览器自动滚动
  if (event.target) {
    event.target.blur()
  }
}

// 监听格式面板状态变化，防止页面滚动
watch(showFormatPanel, (isOpen) => {
  nextTick(() => {
    const scrollContainer = document.querySelector('.editor-main-area')
    const body = document.body
    const html = document.documentElement
    
    if (isOpen) {
      // 打开时保存滚动位置
      savedScrollPosition = scrollContainer ? scrollContainer.scrollTop : window.pageYOffset
      
      // 防止页面滚动
      body.style.overflowX = 'hidden'
      html.style.overflowX = 'hidden'
    } else {
      // 关闭时恢复滚动设置
      body.style.overflowX = ''
      html.style.overflowX = ''
    }
    
    // 恢复滚动位置，防止页面跳动
    if (savedScrollPosition !== null) {
      // 使用 requestAnimationFrame 确保在下一帧恢复
      requestAnimationFrame(() => {
        if (scrollContainer) {
          scrollContainer.scrollTop = savedScrollPosition
        } else {
          window.scrollTo(0, savedScrollPosition)
        }
      })
    }
  })
})

// 协同编辑开关（默认关闭）
const collaborationEnabled = ref(false)

// 协同编辑
const { 
  collaborators, 
  isConnected, 
  sendContentUpdate, 
  sendCursorUpdate,
  setEnabled: setCollaborationEnabled
} = useCollaboration(props.documentId, collaborationEnabled.value)

// 操作记录（用于打印调试）
const operationLog = ref([])

// 切换协同编辑
const toggleCollaboration = () => {
  collaborationEnabled.value = !collaborationEnabled.value
  setCollaborationEnabled(collaborationEnabled.value)
  console.log(`协同编辑已${collaborationEnabled.value ? '开启' : '关闭'}`)
}

// 分析事务，提取操作数据
const analyzeTransaction = (editor, transaction) => {
  if (!transaction.steps || transaction.steps.length === 0) {
    return null
  }

  const { selection } = editor.state
  const { from, to } = selection
  
  // 分析每个步骤
  for (const step of transaction.steps) {
    // 插入操作
    if (step.slice && step.slice.content && step.slice.content.size > 0) {
      // 提取插入的文本内容
      let insertedText = ''
      const slice = step.slice
      
      if (slice.content) {
        slice.content.forEach(node => {
          if (node.type && node.type.name === 'text') {
            insertedText += node.text || ''
          } else if (node.type && node.type.name === 'hardBreak') {
            insertedText += '\n'
          } else if (node.type && node.type.name === 'paragraph') {
            // 段落节点，提取其中的文本
            if (node.content) {
              node.content.forEach(child => {
                if (child.type && child.type.name === 'text') {
                  insertedText += child.text || ''
                }
              })
            }
          }
        })
      }
      
      // 如果没有提取到文本，使用 textContent
      if (!insertedText && slice.textContent) {
        insertedText = slice.textContent
      }
      
      // 获取插入位置
      const insertPos = step.from !== undefined ? step.from : from
      
      return {
        type: OperationType.INSERT,
        position: insertPos,
        content: insertedText,
        targetType: 'segment',
        metadata: {
          html: slice.toString(),
          nodeType: slice.content?.content?.[0]?.type?.name || 'text',
          contentSize: slice.content?.size || 0,
          timestamp: Date.now()
        }
      }
    }
    
    // 删除操作
    if (step.from !== undefined && step.to !== undefined && step.from < step.to && (!step.slice || !step.slice.content || step.slice.content.size === 0)) {
      try {
        // 尝试获取删除前的内容（需要访问旧文档状态）
        const deletedLength = step.to - step.from
        
        return {
          type: OperationType.DELETE,
          position: step.from,
          content: '', // 删除的内容在事务中可能无法直接获取
          targetType: 'segment',
          metadata: {
            length: deletedLength,
            from: step.from,
            to: step.to,
            timestamp: Date.now()
          }
        }
      } catch (e) {
        console.warn('无法获取删除内容:', e)
      }
    }
    
    // 格式操作（通过 transaction meta 检测）
    const formatChanges = []
    if (transaction.getMeta('bold')) {
      formatChanges.push('bold')
    }
    if (transaction.getMeta('italic')) {
      formatChanges.push('italic')
    }
    if (transaction.getMeta('strike')) {
      formatChanges.push('strike')
    }
    if (transaction.getMeta('code')) {
      formatChanges.push('code')
    }
    
    if (formatChanges.length > 0) {
      return {
        type: OperationType.FORMAT,
        position: from,
        content: '',
        targetType: 'segment',
        metadata: {
          formats: formatChanges,
          selection: { from, to },
          timestamp: Date.now()
        }
      }
    }
  }
  
  return null
}

// 打印操作数据
const printOperationData = (operationData) => {
  const typeEmoji = {
    [OperationType.INSERT]: '✏️',
    [OperationType.DELETE]: '🗑️',
    [OperationType.FORMAT]: '🎨',
    [OperationType.MOVE]: '↔️',
    [OperationType.SPLIT]: '✂️',
    [OperationType.MERGE]: '🔗'
  }
  
  const emoji = typeEmoji[operationData.type] || '📝'
  
  console.group(`${emoji} 操作数据 [${operationData.type}]`)
  console.log('📍 位置:', operationData.position)
  console.log('📄 内容:', operationData.content || '(空)')
  console.log('🎯 目标类型:', operationData.targetType)
  console.log('📊 元数据:', operationData.metadata)
  console.log('📦 完整数据:', JSON.stringify(operationData, null, 2))
  console.groupEnd()
}

// 发送操作到协同服务器
const sendOperation = (operationData) => {
  const collaborationOp = new CollaborationOperation({
    documentId: props.documentId,
    userId: 'current-user', // TODO: 从用户服务获取
    type: operationData.type,
    position: operationData.position,
    content: operationData.content,
    targetType: operationData.targetType,
    metadata: operationData.metadata
  })
  
  // 通过 WebSocket 发送
  sendContentUpdate(JSON.stringify(collaborationOp.toJSON()))
}

// 创建键盘事件处理函数（使用闭包访问响应式变量）
const createKeyDownHandler = () => {
  return (view, event) => {
    // 如果菜单已显示，处理搜索输入
    if (showBlockMenu.value) {
      // 处理普通字符输入（用于搜索）
      if (event.key.length === 1 && 
          !event.ctrlKey && 
          !event.metaKey && 
          !event.altKey &&
          event.key !== 'Escape' && 
          event.key !== 'Enter' && 
          event.key !== 'ArrowUp' && 
          event.key !== 'ArrowDown' &&
          event.key !== 'Tab' &&
          event.key !== '/' &&
          !event.shiftKey) {
        // 更新搜索查询
        blockMenuSearchQuery.value += event.key
        event.preventDefault()
        return true
      }
      
      // 处理退格键删除搜索字符
      if (event.key === 'Backspace' && blockMenuSearchQuery.value.length > 0) {
        blockMenuSearchQuery.value = blockMenuSearchQuery.value.slice(0, -1)
        event.preventDefault()
        return true
      }
      
      // 如果输入"/"，关闭菜单
      if (event.key === '/') {
        showBlockMenu.value = false
        blockMenuSearchQuery.value = ''
        // 不阻止默认行为，让编辑器正常插入"/"
        return false
      }
      
      // 方向键、Enter、Esc等让菜单组件处理
      // 这里不处理，让菜单组件的handleKeyDown处理
      return false
    }
    
    // 处理 "/" 命令
    if (event.key === '/' && !showBlockMenu.value) {
      const { state } = view
      const { selection } = state
      const { $anchor } = selection
      
      // 获取光标位置（使用视口坐标，菜单将使用 fixed 定位）
      try {
        const coords = view.coordsAtPos($anchor.pos)
        const menuWidth = 360
        const menuHeight = 500
        const padding = 16
        
        // 计算菜单位置，确保不超出视口
        let menuTop = coords.top + 20
        let menuLeft = coords.left
        
        // 检查右边界
        if (menuLeft + menuWidth > window.innerWidth - padding) {
          menuLeft = window.innerWidth - menuWidth - padding
        }
        
        // 检查左边界
        if (menuLeft < padding) {
          menuLeft = padding
        }
        
        // 检查下边界，如果下方空间不足，显示在上方
        if (menuTop + menuHeight > window.innerHeight - padding) {
          menuTop = coords.top - menuHeight - 4
          // 如果上方也不够，则调整到视口内
          if (menuTop < padding) {
            menuTop = padding
          }
        }
        
        // 检查上边界
        if (menuTop < padding) {
          menuTop = padding
        }
        
        blockMenuPosition.value = {
          top: menuTop,
          left: menuLeft
        }
        blockMenuSearchQuery.value = ''
        showBlockMenu.value = true
        event.preventDefault()
        return true
      } catch (e) {
        console.error('获取光标位置失败:', e)
      }
    }
    
    return false
  }
}

// 初始化编辑器
const editor = useEditor({
  extensions: [
    StarterKit.configure({
      heading: {
        levels: [1, 2, 3, 4, 5, 6]
      }
    }),
    Image.configure({
      inline: true,
      allowBase64: true
    }),
    Table.configure({
      resizable: true
    }),
    TableRow,
    TableHeader,
    TableCell,
    Link.configure({
      openOnClick: false
    }),
    TaskList,
    TaskItem.configure({
      nested: true
    }),
    Placeholder.configure({
      placeholder: '开始输入 "/" 插入内容...'
    }),
    Mention.configure({
      HTMLAttributes: {
        class: 'mention'
      }
    }),
    DocumentLink
  ],
  content: props.initialContent || '<p></p>',
  editable: !props.readOnly,
  onUpdate: ({ editor, transaction }) => {
    const html = editor.getHTML()
    emit('update', html)
    autoSave(html)
    
    // 分析操作并记录
    const operationData = analyzeTransaction(editor, transaction)
    if (operationData) {
      // 打印操作数据
      printOperationData(operationData)
      
      // 如果开启协同编辑，发送操作
      if (collaborationEnabled.value && isConnected.value) {
        sendOperation(operationData)
      }
      
      // 记录操作日志
      operationLog.value.push(operationData)
      if (operationLog.value.length > 100) {
        operationLog.value.shift() // 只保留最近100条
      }
    } else {
      // 如果没有详细操作，发送完整内容更新
      if (collaborationEnabled.value && isConnected.value) {
        sendContentUpdate(html)
      }
    }
    
    // 检测操作类型（用于动画效果）
    const isInsert = transaction.steps.some(step => {
      return step.slice && step.slice.content && step.slice.content.size > 0
    })
    const isNewLine = transaction.steps.some(step => {
      if (step.slice && step.slice.content) {
        const content = step.slice.content
        return content.content && content.content.some(node => 
          node.type && (node.type.name === 'hardBreak' || node.type.name === 'paragraph')
        )
      }
      return false
    })
    
    // 打字动画效果
    if (isInsert) {
      isTyping.value = true
      if (typingTimer) {
        clearTimeout(typingTimer)
      }
      typingTimer = setTimeout(() => {
        isTyping.value = false
      }, 300)
    }
    
    // 插入内容动画
    if (isInsert && !isNewLine) {
      isInserting.value = true
      if (insertTimer) {
        clearTimeout(insertTimer)
      }
      insertTimer = setTimeout(() => {
        isInserting.value = false
      }, 400)
    }
    
    // 换行动画
    if (isNewLine) {
      isLineBreak.value = true
      if (lineBreakTimer) {
        clearTimeout(lineBreakTimer)
      }
      lineBreakTimer = setTimeout(() => {
        isLineBreak.value = false
      }, 500)
    }
  },
  onSelectionUpdate: ({ editor }) => {
    // 发送光标位置更新
    if (collaborationEnabled.value && isConnected.value && editor.state.selection) {
      const { from, to } = editor.state.selection
      sendCursorUpdate({ from, to })
    }
  },
  editorProps: {
    handleKeyDown: createKeyDownHandler()
  }
})

// 自动保存
let saveTimer = null
const autoSave = (content) => {
  if (saveTimer) {
    clearTimeout(saveTimer)
  }
  saveTimer = setTimeout(() => {
    emit('save', content)
  }, 2000)
}

// 插入图片
const insertImage = () => {
  const url = window.prompt('请输入图片URL:')
  if (url) {
    editor.value.chain().focus().setImage({ src: url }).run()
  }
}

// 插入表格
const insertTable = () => {
  editor.value.chain().focus().insertTable({ rows: 3, cols: 3, withHeaderRow: true }).run()
}

// 设置链接
const setLink = () => {
  const previousUrl = editor.value.getAttributes('link').href
  const url = window.prompt('URL', previousUrl)

  if (url === null) {
    return
  }

  if (url === '') {
    editor.value.chain().focus().extendMarkRange('link').unsetLink().run()
    return
  }

  editor.value.chain().focus().extendMarkRange('link').setLink({ href: url }).run()
}

// 插入文档链接
const insertDocumentLink = () => {
  const documentId = window.prompt('请输入文档ID:')
  const documentName = window.prompt('请输入文档名称:', '未命名文档')
  
  if (documentId) {
    editor.value.chain().focus().setDocumentLink({
      documentId,
      documentName: documentName || '未命名文档'
    }).run()
  }
}

// 添加评论
const addComment = () => {
  if (!newComment.value.trim()) return
  
  comments.value.push({
    id: Date.now(),
    author: '当前用户',
    text: newComment.value,
    time: new Date()
  })
  newComment.value = ''
}

// 恢复版本
const restoreVersion = (versionId) => {
  const version = versions.value.find(v => v.id === versionId)
  if (version && editor.value) {
    editor.value.commands.setContent(version.content)
    currentVersion.value = versionId
  }
}

// 键盘事件处理已移到 editorProps.handleKeyDown

// 处理块菜单选择
const handleBlockMenuSelect = (type) => {
  if (!editor.value) return
  
  showBlockMenu.value = false
  blockMenuSearchQuery.value = ''
  
  // 删除触发菜单的 "/" 和搜索查询（如果存在）
  const { from, $anchor } = editor.value.state.selection
  const textBefore = $anchor.parent.textContent
  const deleteLength = 1 + blockMenuSearchQuery.value.length
  
  if (from >= deleteLength) {
    editor.value.chain().focus().deleteRange({ from: from - deleteLength, to: from }).run()
  }
  
  // 根据类型插入内容
  switch (type) {
    // 基本区块
    case 'text':
    case 'paragraph':
      editor.value.chain().focus().setParagraph().run()
      break
    case 'heading1':
      editor.value.chain().focus().toggleHeading({ level: 1 }).run()
      break
    case 'heading2':
      editor.value.chain().focus().toggleHeading({ level: 2 }).run()
      break
    case 'heading3':
      editor.value.chain().focus().toggleHeading({ level: 3 }).run()
      break
    case 'heading4':
      editor.value.chain().focus().toggleHeading({ level: 4 }).run()
      break
    case 'codeBlock':
      editor.value.chain().focus().toggleCodeBlock().run()
      break
    case 'blockquote':
      editor.value.chain().focus().toggleBlockquote().run()
      break
    case 'horizontalRule':
      editor.value.chain().focus().setHorizontalRule().run()
      break
    
    // 文本格式
    case 'bold':
      editor.value.chain().focus().toggleBold().run()
      break
    case 'italic':
      editor.value.chain().focus().toggleItalic().run()
      break
    case 'strike':
      editor.value.chain().focus().toggleStrike().run()
      break
    case 'code':
      editor.value.chain().focus().toggleCode().run()
      break
    case 'underline':
      // TipTap默认不支持下划线，需要扩展
      console.log('下划线功能需要扩展支持')
      break
    // 列表
    case 'bulletList':
      editor.value.chain().focus().toggleBulletList().run()
      break
    case 'orderedList':
      editor.value.chain().focus().toggleOrderedList().run()
      break
    case 'taskList':
      editor.value.chain().focus().toggleTaskList().run()
      break
    case 'checkList':
      // 检查列表（使用任务列表代替）
      editor.value.chain().focus().toggleTaskList().run()
      break
    
    // 媒体
    case 'image':
      insertImage()
      break
    case 'video':
      const videoUrl = window.prompt('请输入视频URL:')
      if (videoUrl) {
        // TipTap需要扩展支持视频
        console.log('视频功能需要扩展支持:', videoUrl)
      }
      break
    case 'audio':
      const audioUrl = window.prompt('请输入音频URL:')
      if (audioUrl) {
        // TipTap需要扩展支持音频
        console.log('音频功能需要扩展支持:', audioUrl)
      }
      break
    case 'link':
      setLink()
      break
    case 'documentLink':
      insertDocumentLink()
      break
    case 'embed':
      const embedUrl = window.prompt('请输入嵌入内容URL:')
      if (embedUrl) {
        // TipTap需要扩展支持嵌入
        console.log('嵌入功能需要扩展支持:', embedUrl)
      }
      break
    
    // 高级功能
    case 'table':
      insertTable()
      break
    case 'math':
      // LaTeX数学公式（需要扩展）
      console.log('数学公式功能需要扩展支持')
      break
    case 'diagram':
      // 图表（需要扩展）
      console.log('图表功能需要扩展支持')
      break
    case 'collapsible':
      // 折叠块（需要扩展）
      console.log('折叠块功能需要扩展支持')
      break
    case 'callout':
      // 提示框（使用引用代替）
      editor.value.chain().focus().toggleBlockquote().run()
      break
    case 'comment':
      showComments.value = true
      break
    case 'history':
      showHistory.value = true
      break
    case 'export':
      // 导出功能
      console.log('导出功能')
      break
    
    // AI功能
    case 'aiShorthand':
      // AI 速记功能（待实现）
      console.log('AI 速记功能')
      break
    case 'aiSummary':
      // AI 总结功能（待实现）
      console.log('AI 总结功能')
      break
  }
}

// 监听内容变化
watch(() => props.initialContent, (newContent) => {
  if (editor.value && newContent !== editor.value.getHTML()) {
    editor.value.commands.setContent(newContent || '<p></p>')
  }
})

// 监听协同编辑消息
watch(() => isConnected.value, (connected) => {
  if (connected) {
    console.log('协同编辑已连接')
  }
})

// 生成作者颜色
const authorColor = ref('#165dff')
watch(() => props.author, (author) => {
  const colors = [
    '#165dff', '#10b981', '#f59e0b', '#ef4444',
    '#8b5cf6', '#06b6d4', '#ec4899', '#14b8a6'
  ]
  const index = (author?.charCodeAt(0) || 0) % colors.length
  authorColor.value = colors[index]
}, { immediate: true })

// 格式化时间
const formatTime = (time) => {
  if (!time) return '刚刚'
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}小时前`
  return date.toLocaleDateString('zh-CN')
}

// 处理添加封面
const handleAddCover = () => {
  const url = window.prompt('请输入封面图片URL:')
  if (url) {
    emit('cover-change', url)
  }
}

// 处理移除封面
const handleRemoveCover = () => {
  emit('cover-change', null)
}

// 点击外部关闭菜单
const handleClickOutside = (event) => {
  if (showBlockMenu.value) {
    const menu = event.target.closest('.block-menu')
    if (!menu) {
      showBlockMenu.value = false
      blockMenuSearchQuery.value = ''
    }
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
  if (editor.value) {
    editor.value.destroy()
  }
  if (saveTimer) {
    clearTimeout(saveTimer)
  }
  if (typingTimer) {
    clearTimeout(typingTimer)
  }
  if (insertTimer) {
    clearTimeout(insertTimer)
  }
  if (lineBreakTimer) {
    clearTimeout(lineBreakTimer)
  }
})

// 初始化版本历史
versions.value = [
  {
    id: 1,
    author: '系统',
    time: new Date(),
    description: '初始版本',
    content: props.initialContent || '<p></p>'
  }
]
currentVersion.value = 1

// onBeforeUnmount 已在上面定义
</script>

<style scoped>
.document-editor-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--theme-background-gradient, none);
  background-color: var(--theme-background, #f5f5f7);
  overflow: hidden;
  position: relative;
  /* 移除backdrop-filter以提升性能，达到60fps */
  /* backdrop-filter: blur(16px) saturate(180%); */
  /* -webkit-backdrop-filter: blur(16px) saturate(180%); */
}

.document-editor-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 30%, rgba(22, 93, 255, 0.08) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(139, 92, 246, 0.08) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

.editor-layout {
  display: flex;
  height: 100%;
  overflow: hidden;
  overflow-x: hidden;
  width: 100%;
  max-width: 100%;
  background: transparent;
  position: relative;
  z-index: 1;
  /* 优化布局性能 */
  transform: translateZ(0);
  contain: strict;
}

/* 侧边栏切换按钮 */
.sidebar-toggle {
  position: fixed;
  left: 0;
  top: 50%;
  transform: translateY(-50%) translateZ(0);
  width: 32px;
  height: 64px;
  background: rgba(250, 250, 252, 0.85);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-left: none;
  border-radius: 0 8px 8px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 100;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.08);
  transition: transform 0.08s cubic-bezier(0.16, 1, 0.3, 1), background 0.08s cubic-bezier(0.16, 1, 0.3, 1), box-shadow 0.08s cubic-bezier(0.16, 1, 0.3, 1);
  color: #6b7280;
  animation: toggleSlideIn 0.12s cubic-bezier(0.16, 1, 0.3, 1) both;
  will-change: transform, opacity;
}

@keyframes toggleSlideIn {
  from {
    opacity: 0;
    transform: translateY(-50%) translateX(-10px) translateZ(0);
  }
  to {
    opacity: 1;
    transform: translateY(-50%) translateX(0) translateZ(0);
  }
}

.sidebar-toggle:hover {
  background: rgba(245, 245, 247, 0.95);
  color: #111827;
  box-shadow: 4px 0 12px rgba(0, 0, 0, 0.12);
  transform: translateY(-50%) translateX(2px) translateZ(0);
}

.sidebar-toggle:active {
  transform: translateY(-50%) translateX(2px) scale(0.95) translateZ(0);
}

/* 协同编辑开关 */
.collaboration-toggle {
  position: fixed;
  right: 20px;
  top: calc(50% - 60px);
  transform: translateY(-50%) translateZ(0);
  z-index: 10000;
  min-width: 64px;
  height: 48px;
  padding: 8px 12px;
  background: rgba(245, 245, 247, 0.85);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  cursor: pointer;
  pointer-events: auto;
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  will-change: transform, opacity;
  color: #6b7280;
}

.collaboration-toggle:hover {
  background: rgba(245, 245, 247, 0.85);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  color: #111827;
  transform: translateY(-50%) translateX(-4px) translateZ(0);
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.12),
    0 8px 24px rgba(0, 0, 0, 0.08);
}

.collaboration-toggle:active {
  transform: translateY(-50%) translateX(-6px) scale(0.96) translateZ(0);
}

.collaboration-toggle.active {
  color: #165dff;
  border-color: rgba(22, 93, 255, 0.2);
  background: rgba(22, 93, 255, 0.05);
}

.collaboration-toggle.active:hover {
  background: rgba(22, 93, 255, 0.08);
}

.collaboration-toggle-label {
  font-size: 11px;
  font-weight: 500;
  color: inherit;
  line-height: 1;
  transform: translateZ(0);
}

.format-panel-toggle {
  position: fixed;
  right: 20px;
  top: 50%;
  transform: translateY(-50%) translateZ(0);
  z-index: 10001;
  min-width: 64px;
  height: 48px;
  padding: 8px 12px;
  background: rgba(245, 245, 247, 0.85);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  cursor: pointer;
  pointer-events: auto;
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  will-change: transform, opacity;
  color: #6b7280;
}

.format-panel-toggle.active {
  color: #165dff;
  border-color: rgba(22, 93, 255, 0.2);
  background: rgba(22, 93, 255, 0.1);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
}

.format-panel-toggle.active:hover {
  background: rgba(22, 93, 255, 0.15);
}

.format-panel-toggle:hover {
  background: rgba(245, 245, 247, 0.85);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  color: #111827;
  transform: translateY(-50%) translateX(-4px) translateZ(0);
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.12),
    0 8px 24px rgba(0, 0, 0, 0.08);
}

.format-panel-toggle:active {
  transform: translateY(-50%) translateX(-6px) scale(0.96) translateZ(0);
}

.format-panel-toggle:focus {
  outline: none;
  box-shadow: 
    0 2px 8px rgba(0, 0, 0, 0.1),
    0 4px 16px rgba(0, 0, 0, 0.06);
}


.format-toggle-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  transform: translateZ(0);
}

.format-toggle-label {
  font-size: 11px;
  font-weight: 500;
  color: inherit;
  line-height: 1;
  transform: translateZ(0);
}

.format-panel-toggle:hover {
  background: rgba(245, 245, 247, 0.85);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  color: #111827;
  transform: translateY(-50%) translateX(-4px) translateZ(0);
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.12),
    0 8px 24px rgba(0, 0, 0, 0.08);
}

.format-panel-toggle:active {
  transform: translateY(-50%) translateX(-6px) scale(0.96) translateZ(0);
}

/* 侧边栏动画 - 极致性能优化，90fps流畅体验 */
.slide-left-enter-active {
  transition: transform 0.08s cubic-bezier(0.16, 1, 0.3, 1), opacity 0.08s cubic-bezier(0.16, 1, 0.3, 1);
  will-change: transform, opacity;
}

.slide-left-leave-active {
  transition: transform 0.06s cubic-bezier(0.4, 0, 1, 1), opacity 0.06s cubic-bezier(0.4, 0, 1, 1);
  will-change: transform, opacity;
}

.slide-left-enter-from {
  transform: translateX(-100%) translateZ(0);
  opacity: 0;
}

.slide-left-leave-to {
  transform: translateX(-100%) translateZ(0);
  opacity: 0;
}

.slide-right-enter-active {
  transition: transform 0.08s cubic-bezier(0.16, 1, 0.3, 1), opacity 0.08s cubic-bezier(0.16, 1, 0.3, 1);
  will-change: transform, opacity;
}

.slide-right-leave-active {
  transition: transform 0.06s cubic-bezier(0.4, 0, 1, 1), opacity 0.06s cubic-bezier(0.4, 0, 1, 1);
  will-change: transform, opacity;
}

.slide-right-enter-from {
  transform: translateX(20px) translateY(-50%) translateZ(0);
  opacity: 0;
}

.slide-right-leave-to {
  transform: translateX(20px) translateY(-50%) translateZ(0);
  opacity: 0;
}

/* 工具栏已移除，所有功能通过 / 命令菜单访问 */

.editor-main-area {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  background: transparent;
  padding: 20px;
  position: relative;
  z-index: 5;
  /* 滚动性能优化 - 90fps极致性能 */
  -webkit-overflow-scrolling: touch;
  transform: translateZ(0);
  /* 优化滚动性能 */
  contain: strict;
  /* 使用 GPU 加速滚动 */
  backface-visibility: hidden;
  /* 优化滚动性能 */
  will-change: scroll-position;
  /* 减少重绘 */
  transform-style: preserve-3d;
  perspective: 1000px;
  /* 隐藏滚动条但保留滚动功能 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE 10+ */
}

/* 格式面板容器已移除，格式面板使用 Teleport 直接渲染到 body */

/* 隐藏滚动条但保留滚动功能 */
.editor-main-area {
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE 10+ */
}

.editor-main-area::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Edge */
  width: 0;
  height: 0;
}

/* 内层突出的"页" - 内容和页是一体的，像AI对话泡泡一样 */
.editor-page-container {
  width: 100%;
  max-width: 1000px;
  margin: 0 auto;
  background: rgba(250, 250, 252, 0.95);
  /* 移除backdrop-filter以提升性能，达到60fps */
  /* backdrop-filter: blur(16px) saturate(180%); */
  /* -webkit-backdrop-filter: blur(16px) saturate(180%); */
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 20px;
  box-shadow: 
    0 0 0 1px rgba(0, 0, 0, 0.04),
    0 8px 24px rgba(0, 0, 0, 0.08),
    0 16px 48px rgba(0, 0, 0, 0.06);
  /* 内容驱动高度，不设置任何高度限制 */
  min-height: calc(100vh - 200px);
  animation: pageContainerFadeIn 0.15s cubic-bezier(0.16, 1, 0.3, 1) 0.01s both;
  will-change: transform, opacity;
}

@keyframes pageContainerFadeIn {
  from {
    opacity: 0;
    transform: translateY(4px) translateZ(0);
  }
  to {
    opacity: 1;
    transform: translateY(0) translateZ(0);
  }
}

.page-cover {
  width: 100%;
  height: 200px;
  background: linear-gradient(135deg, rgba(22, 93, 255, 0.08) 0%, rgba(139, 92, 246, 0.08) 100%);
  background-size: cover;
  background-position: center;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 20px 20px 0 0;
  animation: coverFadeIn 0.35s cubic-bezier(0.16, 1, 0.3, 1) 0.2s both;
  will-change: opacity, transform;
}

@keyframes coverFadeIn {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.cover-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: rgba(0, 0, 0, 0.4);
  cursor: pointer;
  padding: 16px;
  border-radius: 8px;
  transition: all 0.2s;
}

.cover-placeholder:hover {
  background: rgba(245, 245, 247, 0.6);
  backdrop-filter: blur(10px) saturate(180%);
  -webkit-backdrop-filter: blur(10px) saturate(180%);
  color: rgba(0, 0, 0, 0.6);
}

.cover-placeholder svg {
  opacity: 0.6;
}

.cover-placeholder span {
  font-size: 13px;
}

.cover-remove-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 28px;
  height: 28px;
  border: none;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(8px);
  border-radius: 6px;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.08s cubic-bezier(0.16, 1, 0.3, 1);
  opacity: 0.8;
}

.cover-remove-btn:hover {
  background: rgba(0, 0, 0, 0.7);
  transform: scale(1.15) rotate(90deg);
  opacity: 1;
}

.cover-remove-btn:active {
  transform: scale(1.05) rotate(90deg);
}

.document-header {
  max-width: 900px;
  margin: 0 auto;
  padding: 32px 80px 0;
  width: 100%;
  box-sizing: border-box;
  animation: headerContentFadeIn 0.15s cubic-bezier(0.16, 1, 0.3, 1) 0.05s both;
  will-change: opacity, transform;
}

@keyframes headerContentFadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.document-title-main {
  font-size: 42px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 16px 0;
  line-height: 1.2;
  letter-spacing: -0.02em;
}

.document-author-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 32px;
}

.author-avatar-small {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.author-name-small {
  font-size: 13px;
  font-weight: 500;
  color: #374151;
}

.time-text {
  font-size: 13px;
  color: #9ca3af;
}

.editor-wrapper {
  width: 100%;
  box-sizing: border-box;
}

.editor-content {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 80px;
  box-sizing: border-box;
  word-wrap: break-word;
  overflow-wrap: break-word;
  animation: editorContentFadeIn 0.15s cubic-bezier(0.16, 1, 0.3, 1) 0.1s both;
  /* 滚动性能优化 */
  transform: translateZ(0);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  /* 打字动画状态 */
  transition: transform 0.06s cubic-bezier(0.16, 1, 0.3, 1);
}

.editor-content.typing-active {
  transform: translateZ(0);
}

/* 打字时的光标高亮效果 */
.editor-content.typing-active :deep(.ProseMirror) {
  caret-color: #165dff;
  animation: typingCaretPulse 0.6s ease-in-out infinite;
}

/* 插入内容时的光标效果 */
.editor-content.inserting-active :deep(.ProseMirror) {
  caret-color: #4c7fff;
  animation: insertCaretGlow 0.4s ease-in-out;
}

/* 换行时的光标效果 */
.editor-content.linebreak-active :deep(.ProseMirror) {
  caret-color: #165dff;
  animation: lineBreakCaret 0.3s ease-in-out;
}

@keyframes typingCaretPulse {
  0%, 100% {
    caret-color: #165dff;
  }
  50% {
    caret-color: #4c7fff;
  }
}

@keyframes insertCaretGlow {
  0%, 100% {
    caret-color: #4c7fff;
  }
  50% {
    caret-color: #6b9aff;
  }
}

@keyframes lineBreakCaret {
  0% {
    caret-color: #165dff;
  }
  50% {
    caret-color: #4c7fff;
  }
  100% {
    caret-color: #165dff;
  }
}

@keyframes editorContentFadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 自定义滚动条 */
/* 隐藏滚动条但保留滚动功能 */
.editor-content {
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE 10+ */
}

.editor-content::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Edge */
  width: 0;
  height: 0;
}

.editor {
  outline: none;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Helvetica Neue', Arial, sans-serif;
  position: relative;
  z-index: 1;
  padding: 40px 0 120px;
  box-sizing: border-box;
  word-wrap: break-word;
  overflow-wrap: break-word;
  max-width: 100%;
  /* 内容驱动，自动扩展 */
  min-height: 0;
  /* 滚动性能优化 */
  transform: translateZ(0);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* TipTap 样式 */
:deep(.ProseMirror) {
  outline: none;
  color: #1d2129;
  font-size: 16px;
  line-height: 1.7;
  word-wrap: break-word;
  overflow-wrap: break-word;
  max-width: 100%;
  box-sizing: border-box;
  /* 滚动性能优化 */
  transform: translateZ(0);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  contain: layout style paint;
  /* 优化打字体验 */
  caret-color: #165dff;
  text-rendering: optimizeLegibility;
}

/* 打字输入动画 - 新输入的文字有淡入效果（仅在输入时触发） */
.editor-content.typing-active :deep(.ProseMirror p),
.editor-content.typing-active :deep(.ProseMirror h1),
.editor-content.typing-active :deep(.ProseMirror h2),
.editor-content.typing-active :deep(.ProseMirror h3),
.editor-content.typing-active :deep(.ProseMirror h4),
.editor-content.typing-active :deep(.ProseMirror h5),
.editor-content.typing-active :deep(.ProseMirror h6),
.editor-content.typing-active :deep(.ProseMirror li),
.editor-content.typing-active :deep(.ProseMirror blockquote) {
  animation: textTypeIn 0.06s cubic-bezier(0.16, 1, 0.3, 1) both;
  transform: translateZ(0);
}

@keyframes textTypeIn {
  from {
    opacity: 0.7;
    transform: translateY(1px) translateZ(0);
  }
  to {
    opacity: 1;
    transform: translateY(0) translateZ(0);
  }
}

/* 插入内容动画 - 插入新块时的动画 */
.editor-content.inserting-active :deep(.ProseMirror p:last-child),
.editor-content.inserting-active :deep(.ProseMirror h1:last-child),
.editor-content.inserting-active :deep(.ProseMirror h2:last-child),
.editor-content.inserting-active :deep(.ProseMirror h3:last-child),
.editor-content.inserting-active :deep(.ProseMirror h4:last-child),
.editor-content.inserting-active :deep(.ProseMirror h5:last-child),
.editor-content.inserting-active :deep(.ProseMirror h6:last-child),
.editor-content.inserting-active :deep(.ProseMirror li:last-child),
.editor-content.inserting-active :deep(.ProseMirror blockquote:last-child) {
  animation: contentInsert 0.1s cubic-bezier(0.16, 1, 0.3, 1) both;
  transform: translateZ(0);
}

/* 换行动画 - 切换行时的动画 */
.editor-content.linebreak-active :deep(.ProseMirror p:last-child) {
  animation: lineBreakSlide 0.12s cubic-bezier(0.16, 1, 0.3, 1) both;
  transform: translateZ(0);
}

.editor-content.linebreak-active :deep(.ProseMirror p:last-child::after) {
  content: '';
  display: block;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(22, 93, 255, 0.3), transparent);
  margin-top: 0.5em;
  animation: lineIndicator 0.15s cubic-bezier(0.16, 1, 0.3, 1) both;
  transform: translateZ(0);
}

@keyframes contentInsert {
  from {
    opacity: 0;
    transform: translateY(4px) scale(0.98) translateZ(0);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1) translateZ(0);
  }
}

@keyframes lineBreakSlide {
  from {
    opacity: 0;
    transform: translateY(6px) translateZ(0);
  }
  50% {
    opacity: 0.8;
    transform: translateY(-1px) translateZ(0);
  }
  to {
    opacity: 1;
    transform: translateY(0) translateZ(0);
  }
}

@keyframes lineIndicator {
  from {
    opacity: 0;
    transform: scaleX(0) translateZ(0);
  }
  50% {
    opacity: 1;
    transform: scaleX(1.1) translateZ(0);
  }
  to {
    opacity: 0;
    transform: scaleX(1) translateZ(0);
  }
}

/* 光标闪烁动画 - 更流畅的闪烁效果 */
:deep(.ProseMirror) {
  caret-color: #165dff;
  animation: caretBlink 1.2s step-end infinite;
}

@keyframes caretBlink {
  0%, 45% {
    caret-color: #165dff;
  }
  46%, 100% {
    caret-color: transparent;
  }
}

/* 选中文本样式优化 */
:deep(.ProseMirror ::selection) {
  background: rgba(22, 93, 255, 0.2);
  color: inherit;
}

:deep(.ProseMirror ::-moz-selection) {
  background: rgba(22, 93, 255, 0.2);
  color: inherit;
}

:deep(.ProseMirror p) {
  margin: 0.75em 0;
  line-height: 1.8;
  color: #374151;
  font-size: 16px;
  transform: translateZ(0);
  /* 优化打字时的文本渲染 */
  text-rendering: optimizeLegibility;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  /* 平滑的文本输入 */
  transition: none;
}

:deep(.ProseMirror p.is-editor-empty:first-child::before) {
  content: attr(data-placeholder);
  float: left;
  color: #9ca3af;
  pointer-events: none;
  height: 0;
  font-size: 16px;
  animation: placeholderPulse 2s ease-in-out infinite;
  opacity: 0.6;
}

@keyframes placeholderPulse {
  0%, 100% {
    opacity: 0.5;
  }
  50% {
    opacity: 0.8;
  }
}

:deep(.ProseMirror h1) {
  font-size: 2.5em;
  font-weight: 700;
  margin: 1.2em 0 0.6em;
  line-height: 1.2;
  color: #111827;
  letter-spacing: -0.02em;
}

:deep(.ProseMirror h2) {
  font-size: 1.875em;
  font-weight: 600;
  margin: 1em 0 0.5em;
  line-height: 1.3;
  color: #111827;
  letter-spacing: -0.01em;
  transform: translateZ(0);
}

:deep(.ProseMirror h3) {
  font-size: 1.5em;
  font-weight: 600;
  margin: 0.9em 0 0.4em;
  line-height: 1.4;
  color: #1d2129;
  transform: translateZ(0);
}

:deep(.ProseMirror ul),
:deep(.ProseMirror ol) {
  padding-left: 1.5em;
  margin: 0.75em 0;
}

:deep(.ProseMirror li) {
  margin: 0.25em 0;
}

:deep(.ProseMirror blockquote) {
  border-left: 4px solid var(--theme-accent, #165dff);
  padding-left: 1.5em;
  margin: 1.5em 0;
  color: #6b7280;
  font-style: italic;
  background: rgba(22, 93, 255, 0.03);
  padding-top: 0.75em;
  padding-bottom: 0.75em;
  border-radius: 0 8px 8px 0;
  transform: translateZ(0);
}

:deep(.ProseMirror code) {
  background: rgba(0, 0, 0, 0.05);
  padding: 0.2em 0.4em;
  border-radius: 3px;
  font-family: 'Monaco', 'Courier New', monospace;
  font-size: 0.9em;
  transform: translateZ(0);
}

:deep(.ProseMirror pre) {
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 1.25em;
  border-radius: 12px;
  overflow-x: auto;
  margin: 1.5em 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transform: translateZ(0);
  will-change: scroll-position;
}

/* 插入代码块时的动画 */
.editor-content.inserting-active :deep(.ProseMirror pre:last-child) {
  animation: codeBlockInsert 0.12s cubic-bezier(0.16, 1, 0.3, 1) both;
}

@keyframes codeBlockInsert {
  from {
    opacity: 0;
    transform: translateY(6px) scale(0.98) translateZ(0);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1) translateZ(0);
  }
}

:deep(.ProseMirror pre code) {
  background: transparent;
  padding: 0;
  color: inherit;
}

:deep(.ProseMirror img) {
  max-width: 100%;
  height: auto;
  border-radius: 12px;
  margin: 1.5em 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: transform 0.08s cubic-bezier(0.16, 1, 0.3, 1);
}

:deep(.ProseMirror img:hover) {
  transform: scale(1.01);
}

:deep(.ProseMirror table) {
  border-collapse: collapse;
  margin: 1.5em 0;
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transform: translateZ(0);
  will-change: scroll-position;
}

/* 插入表格时的动画 */
.editor-content.inserting-active :deep(.ProseMirror table:last-child) {
  animation: tableInsert 0.12s cubic-bezier(0.16, 1, 0.3, 1) both;
}

@keyframes tableInsert {
  from {
    opacity: 0;
    transform: scale(0.96) translateY(6px) translateZ(0);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0) translateZ(0);
  }
}

:deep(.ProseMirror table td),
:deep(.ProseMirror table th) {
  border: 1px solid #e5e7eb;
  padding: 12px 16px;
  text-align: left;
  transition: background 0.15s;
}

:deep(.ProseMirror table th) {
  background: rgba(22, 93, 255, 0.05);
  font-weight: 600;
  color: #111827;
}

:deep(.ProseMirror table tr:hover td) {
  background: rgba(0, 0, 0, 0.02);
}

:deep(.ProseMirror a) {
  color: var(--theme-accent, #165dff);
  text-decoration: underline;
}

:deep(.ProseMirror .mention) {
  background: rgba(22, 93, 255, 0.1);
  color: var(--theme-accent, #165dff);
  padding: 0 4px;
  border-radius: 3px;
}

:deep(.ProseMirror .document-link) {
  background: rgba(22, 93, 255, 0.1);
  color: var(--theme-accent, #165dff);
  padding: 2px 6px;
  border-radius: 4px;
  cursor: pointer;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: all 0.2s;
}

:deep(.ProseMirror .document-link:hover) {
  background: rgba(22, 93, 255, 0.2);
}

/* 浮动面板 */
.floating-panel {
  position: fixed;
  top: 50%;
  right: 24px;
  transform: translateY(-50%);
  width: 380px;
  max-height: 80vh;
  background: rgba(245, 245, 247, 0.85);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  backdrop-filter: blur(30px) saturate(180%);
  -webkit-backdrop-filter: blur(30px) saturate(180%);
  border-radius: 16px;
  box-shadow: 
    0 20px 60px rgba(0, 0, 0, 0.15),
    0 8px 24px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  z-index: 10001;
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.panel-enter-active,
.panel-leave-active {
  transition: all 0.08s cubic-bezier(0.16, 1, 0.3, 1);
}

.panel-enter-from,
.panel-leave-to {
  opacity: 0;
  transform: translateY(-50%) translateX(20px) scale(0.95);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.panel-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.panel-close {
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

.panel-close:hover {
  background: rgba(0, 0, 0, 0.05);
  color: #111827;
}

.panel-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px 24px;
}

.panel-footer {
  padding: 16px 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  display: flex;
  gap: 8px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
}

.sidebar-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.close-btn {
  width: 24px;
  height: 24px;
  border: none;
  background: transparent;
  border-radius: 4px;
  cursor: pointer;
  color: #6b7280;
  font-size: 20px;
  line-height: 1;
}

.close-btn:hover {
  background: rgba(0, 0, 0, 0.05);
}

.comments-list,
.history-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.comment-item {
  padding: 14px;
  margin-bottom: 12px;
  background: rgba(0, 0, 0, 0.02);
  border-radius: 10px;
  border: 1px solid rgba(0, 0, 0, 0.04);
  transition: all 0.2s;
}

.comment-item:hover {
  background: rgba(0, 0, 0, 0.04);
  transform: translateX(2px);
}

.comment-author {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 4px;
}

.comment-text {
  font-size: 14px;
  color: #374151;
  margin-bottom: 4px;
}

.comment-time {
  font-size: 12px;
  color: #9ca3af;
}

.comment-input {
  padding: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
  display: flex;
  gap: 8px;
}

.comment-input-field {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 6px;
  font-size: 14px;
  outline: none;
}

.comment-input-field:focus {
  border-color: var(--theme-accent, #165dff);
}

.comment-submit {
  padding: 8px 16px;
  background: var(--theme-accent, #165dff);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.comment-submit:hover {
  opacity: 0.9;
}

.history-item {
  padding: 14px;
  margin-bottom: 8px;
  border-radius: 10px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.08s cubic-bezier(0.16, 1, 0.3, 1);
  background: rgba(0, 0, 0, 0.01);
}

.history-item:hover {
  background: rgba(0, 0, 0, 0.02);
}

.history-item.active {
  background: rgba(22, 93, 255, 0.12);
  border-color: var(--theme-accent, #165dff);
  box-shadow: 0 2px 8px rgba(22, 93, 255, 0.15);
}

.history-author {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 4px;
}

.history-time {
  font-size: 12px;
  color: #9ca3af;
  margin-bottom: 4px;
}

.history-description {
  font-size: 13px;
  color: #6b7280;
}

.collaborators-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
  background: rgba(0, 0, 0, 0.02);
  flex-shrink: 0;
}

.collaborators-label {
  font-size: 12px;
  color: #6b7280;
}

.collaborator-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
  font-weight: 600;
  border: 2px solid white;
}
</style>

