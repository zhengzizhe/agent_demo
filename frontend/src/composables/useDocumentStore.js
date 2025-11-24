/**
 * useDocumentStore Composable
 * 提供文档库的响应式接口
 */

import { ref, computed, watch } from 'vue'
import { getDocumentStore } from '../utils/DocumentStore.js'
import {
  Document,
  DocumentSegment,
  DocumentPage,
  CollaborationOperation,
  OperationType
} from '../utils/documentTypes.js'

export function useDocumentStore() {
  const store = getDocumentStore()
  
  // 响应式状态
  const currentDocument = ref(null)
  const currentPage = ref(null)
  const documents = ref([])
  const collaborators = ref([])
  const pendingOperations = ref([])
  const isLoading = ref(false)
  const error = ref(null)

  /**
   * 加载文档列表
   */
  const loadDocuments = () => {
    isLoading.value = true
    try {
      documents.value = store.getAllDocuments()
        .filter(doc => !doc.deleted)
        .sort((a, b) => b.updatedAt - a.updatedAt)
      error.value = null
    } catch (err) {
      error.value = err.message
      console.error('加载文档列表失败:', err)
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 加载文档详情
   */
  const loadDocument = (documentId) => {
    isLoading.value = true
    try {
      const doc = store.getDocument(documentId)
      if (doc) {
        currentDocument.value = doc
        collaborators.value = doc.collaborators || []
        pendingOperations.value = doc.pendingOperations || []
        
        // 默认加载第一页
        if (doc.pages.length > 0) {
          currentPage.value = doc.pages[0]
        }
        error.value = null
      } else {
        error.value = '文档不存在'
      }
    } catch (err) {
      error.value = err.message
      console.error('加载文档失败:', err)
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 创建文档
   */
  const createDocument = (name, type = 'rich_text') => {
    try {
      const userId = store.currentUserId || 'anonymous'
      const doc = store.createDocument(name, userId, type)
      loadDocuments()
      return doc
    } catch (err) {
      error.value = err.message
      console.error('创建文档失败:', err)
      return null
    }
  }

  /**
   * 更新文档
   */
  const updateDocument = (documentId, updates) => {
    try {
      const doc = store.updateDocument(documentId, updates)
      if (currentDocument.value?.id === documentId) {
        currentDocument.value = doc
      }
      loadDocuments()
      return doc
    } catch (err) {
      error.value = err.message
      console.error('更新文档失败:', err)
      return null
    }
  }

  /**
   * 删除文档
   */
  const deleteDocument = (documentId) => {
    try {
      const success = store.deleteDocument(documentId)
      if (success && currentDocument.value?.id === documentId) {
        currentDocument.value = null
        currentPage.value = null
      }
      loadDocuments()
      return success
    } catch (err) {
      error.value = err.message
      console.error('删除文档失败:', err)
      return false
    }
  }

  /**
   * 切换页面
   */
  const switchPage = (pageId) => {
    if (!currentDocument.value) return null
    
    const page = currentDocument.value.pages.find(p => p.id === pageId)
    if (page) {
      currentPage.value = page
      return page
    }
    return null
  }

  /**
   * 创建新页面
   */
  const createPage = (title = '') => {
    if (!currentDocument.value) return null
    
    try {
      const page = store.createPage(currentDocument.value.id, title)
      if (page) {
        currentPage.value = page
        loadDocument(currentDocument.value.id)
      }
      return page
    } catch (err) {
      error.value = err.message
      console.error('创建页面失败:', err)
      return null
    }
  }

  /**
   * 获取当前页面的分段
   */
  const getCurrentPageSegments = computed(() => {
    if (!currentDocument.value || !currentPage.value) return []
    return store.getDocument(currentDocument.value.id)
      ?.getSegmentsByPage(currentPage.value.id) || []
  })

  /**
   * 创建分段
   */
  const createSegment = (type = 'paragraph', content = '', metadata = {}) => {
    if (!currentDocument.value || !currentPage.value) return null
    
    try {
      const segment = store.createSegment(
        currentDocument.value.id,
        currentPage.value.id,
        type,
        content,
        metadata
      )
      if (segment) {
        loadDocument(currentDocument.value.id)
      }
      return segment
    } catch (err) {
      error.value = err.message
      console.error('创建分段失败:', err)
      return null
    }
  }

  /**
   * 更新分段
   */
  const updateSegment = (segmentId, updates) => {
    if (!currentDocument.value) return null
    
    try {
      const segment = store.updateSegment(
        currentDocument.value.id,
        segmentId,
        updates
      )
      if (segment) {
        loadDocument(currentDocument.value.id)
      }
      return segment
    } catch (err) {
      error.value = err.message
      console.error('更新分段失败:', err)
      return null
    }
  }

  /**
   * 删除分段
   */
  const deleteSegment = (segmentId) => {
    if (!currentDocument.value) return false
    
    try {
      const success = store.deleteSegment(currentDocument.value.id, segmentId)
      if (success) {
        loadDocument(currentDocument.value.id)
      }
      return success
    } catch (err) {
      error.value = err.message
      console.error('删除分段失败:', err)
      return false
    }
  }

  /**
   * 移动分段
   */
  const moveSegment = (segmentId, targetPageId, targetIndex) => {
    if (!currentDocument.value) return false
    
    try {
      const success = store.moveSegment(
        currentDocument.value.id,
        segmentId,
        targetPageId,
        targetIndex
      )
      if (success) {
        loadDocument(currentDocument.value.id)
      }
      return success
    } catch (err) {
      error.value = err.message
      console.error('移动分段失败:', err)
      return false
    }
  }

  /**
   * 应用协同编辑操作
   */
  const applyOperation = (operationData) => {
    if (!currentDocument.value) return null
    
    try {
      const operation = new CollaborationOperation({
        ...operationData,
        documentId: currentDocument.value.id,
        userId: store.currentUserId || 'anonymous',
        timestamp: Date.now()
      })
      
      const result = store.applyOperation(currentDocument.value.id, operation)
      if (result) {
        loadDocument(currentDocument.value.id)
      }
      return result
    } catch (err) {
      error.value = err.message
      console.error('应用操作失败:', err)
      return null
    }
  }

  /**
   * 插入文本操作
   */
  const insertText = (segmentId, position, text) => {
    return applyOperation({
      type: OperationType.INSERT,
      targetId: segmentId,
      targetType: 'segment',
      position,
      content: text
    })
  }

  /**
   * 删除文本操作
   */
  const deleteText = (segmentId, position, length) => {
    return applyOperation({
      type: OperationType.DELETE,
      targetId: segmentId,
      targetType: 'segment',
      position,
      content: '', // 删除的内容长度
      metadata: { length }
    })
  }

  /**
   * 添加协作者
   */
  const addCollaborator = (collaboratorData) => {
    if (!currentDocument.value) return null
    
    try {
      const collaborator = store.addCollaborator(
        currentDocument.value.id,
        collaboratorData
      )
      if (collaborator) {
        loadDocument(currentDocument.value.id)
      }
      return collaborator
    } catch (err) {
      error.value = err.message
      console.error('添加协作者失败:', err)
      return null
    }
  }

  /**
   * 更新协作者光标
   */
  const updateCollaboratorCursor = (userId, cursorPosition, selectionRange = null) => {
    if (!currentDocument.value) return null
    
    try {
      return store.updateCollaboratorCursor(
        currentDocument.value.id,
        userId,
        cursorPosition,
        selectionRange
      )
    } catch (err) {
      console.error('更新协作者光标失败:', err)
      return null
    }
  }

  /**
   * 创建快照
   */
  const createSnapshot = (description = '') => {
    if (!currentDocument.value) return null
    
    try {
      return store.createSnapshot(currentDocument.value.id, description)
    } catch (err) {
      error.value = err.message
      console.error('创建快照失败:', err)
      return null
    }
  }

  /**
   * 获取快照列表
   */
  const getSnapshots = computed(() => {
    if (!currentDocument.value) return []
    return store.getDocumentSnapshots(currentDocument.value.id)
  })

  /**
   * 恢复快照
   */
  const restoreSnapshot = (snapshotId) => {
    try {
      const doc = store.restoreSnapshot(snapshotId)
      if (doc) {
        loadDocument(doc.id)
      }
      return doc
    } catch (err) {
      error.value = err.message
      console.error('恢复快照失败:', err)
      return null
    }
  }

  /**
   * 监听文档更新事件
   */
  const setupEventListeners = () => {
    store.on('documentUpdated', (data) => {
      if (currentDocument.value?.id === data.document.id) {
        currentDocument.value = data.document
      }
      loadDocuments()
    })

    store.on('segmentUpdated', (data) => {
      if (currentDocument.value?.id === data.document.id) {
        loadDocument(data.document.id)
      }
    })

    store.on('operationApplied', (data) => {
      if (currentDocument.value?.id === data.document.id) {
        loadDocument(data.document.id)
      }
    })

    store.on('conflictDetected', (data) => {
      console.warn('检测到冲突:', data)
      // 可以在这里显示冲突提示
    })

    store.on('collaboratorJoined', (data) => {
      if (currentDocument.value?.id === data.document.id) {
        loadDocument(data.document.id)
      }
    })

    store.on('collaboratorLeft', (data) => {
      if (currentDocument.value?.id === data.document.id) {
        loadDocument(data.document.id)
      }
    })
  }

  // 初始化
  setupEventListeners()
  loadDocuments()

  return {
    // 状态
    currentDocument,
    currentPage,
    documents,
    collaborators,
    pendingOperations,
    isLoading,
    error,
    
    // 计算属性
    getCurrentPageSegments,
    getSnapshots,
    
    // 方法
    loadDocuments,
    loadDocument,
    createDocument,
    updateDocument,
    deleteDocument,
    switchPage,
    createPage,
    createSegment,
    updateSegment,
    deleteSegment,
    moveSegment,
    applyOperation,
    insertText,
    deleteText,
    addCollaborator,
    updateCollaboratorCursor,
    createSnapshot,
    restoreSnapshot,
    
    // 直接访问store（用于高级操作）
    store
  }
}

export default useDocumentStore



