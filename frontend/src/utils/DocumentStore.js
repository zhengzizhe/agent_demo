/**
 * 文档库（DocumentStore）
 * 管理文档的存储、检索和协同编辑
 */

import {
  Document,
  DocumentSegment,
  DocumentPage,
  CollaborationOperation,
  Collaborator,
  DocumentSnapshot,
  OperationType,
  OperationStatus,
  CollaborationStatus
} from './documentTypes.js'

/**
 * 文档库类
 */
export class DocumentStore {
  constructor(storageKey = 'document_store') {
    this.storageKey = storageKey
    this.documents = new Map()           // 文档Map：documentId -> Document
    this.operations = new Map()          // 操作Map：operationId -> CollaborationOperation
    this.snapshots = new Map()           // 快照Map：snapshotId -> DocumentSnapshot
    this.currentUserId = null            // 当前用户ID
    this.syncInterval = null             // 同步定时器
    this.operationQueue = []             // 操作队列（待同步）
    
    // 事件监听器
    this.eventListeners = {
      documentUpdated: [],
      segmentUpdated: [],
      operationApplied: [],
      conflictDetected: [],
      collaboratorJoined: [],
      collaboratorLeft: []
    }

    // 从本地存储加载
    this.loadFromStorage()
  }

  /**
   * 设置当前用户ID
   */
  setCurrentUserId(userId) {
    this.currentUserId = userId
    return this
  }

  /**
   * 从本地存储加载数据
   */
  loadFromStorage() {
    try {
      const stored = localStorage.getItem(this.storageKey)
      if (stored) {
        const data = JSON.parse(stored)
        if (data.documents) {
          data.documents.forEach(docJson => {
            const doc = Document.fromJSON(docJson)
            this.documents.set(doc.id, doc)
          })
        }
        if (data.operations) {
          data.operations.forEach(opJson => {
            const op = CollaborationOperation.fromJSON(opJson)
            this.operations.set(op.id, op)
          })
        }
        if (data.snapshots) {
          data.snapshots.forEach(snapJson => {
            const snap = DocumentSnapshot.fromJSON(snapJson)
            this.snapshots.set(snap.id, snap)
          })
        }
      }
    } catch (error) {
      console.error('加载文档库失败:', error)
    }
    return this
  }

  /**
   * 保存到本地存储
   */
  saveToStorage() {
    try {
      const data = {
        documents: Array.from(this.documents.values()).map(doc => doc.toJSON()),
        operations: Array.from(this.operations.values()).map(op => op.toJSON()),
        snapshots: Array.from(this.snapshots.values()).map(snap => snap.toJSON())
      }
      localStorage.setItem(this.storageKey, JSON.stringify(data))
    } catch (error) {
      console.error('保存文档库失败:', error)
    }
    return this
  }

  /**
   * ==================== 文档管理 ====================
   */

  /**
   * 创建文档
   */
  createDocument(name, userId, type = 'rich_text') {
    const doc = Document.createEmpty(name, userId, type)
    this.documents.set(doc.id, doc)
    this.saveToStorage()
    this.emit('documentUpdated', { document: doc, type: 'created' })
    return doc
  }

  /**
   * 获取文档
   */
  getDocument(documentId) {
    return this.documents.get(documentId) || null
  }

  /**
   * 获取所有文档
   */
  getAllDocuments() {
    return Array.from(this.documents.values())
  }

  /**
   * 更新文档
   */
  updateDocument(documentId, updates) {
    const doc = this.documents.get(documentId)
    if (!doc) return null

    Object.assign(doc, updates, {
      updatedAt: Date.now(),
      lastModified: Date.now(),
      lastModifiedBy: this.currentUserId
    })
    doc.version++

    this.saveToStorage()
    this.emit('documentUpdated', { document: doc, type: 'updated' })
    return doc
  }

  /**
   * 删除文档（软删除）
   */
  deleteDocument(documentId) {
    const doc = this.documents.get(documentId)
    if (!doc) return false

    doc.deleted = true
    doc.deletedAt = Date.now()
    doc.updatedAt = Date.now()

    this.saveToStorage()
    this.emit('documentUpdated', { document: doc, type: 'deleted' })
    return true
  }

  /**
   * 恢复文档
   */
  restoreDocument(documentId) {
    const doc = this.documents.get(documentId)
    if (!doc) return false

    doc.deleted = false
    doc.deletedAt = null
    doc.updatedAt = Date.now()

    this.saveToStorage()
    this.emit('documentUpdated', { document: doc, type: 'restored' })
    return true
  }

  /**
   * ==================== 分页分段管理 ====================
   */

  /**
   * 创建新页面
   */
  createPage(documentId, title = '', userId = null) {
    const doc = this.documents.get(documentId)
    if (!doc) return null

    const page = doc.createPage(title, userId || this.currentUserId)
    this.saveToStorage()
    this.emit('documentUpdated', { document: doc, type: 'pageCreated', page })
    return page
  }

  /**
   * 删除页面
   */
  deletePage(documentId, pageId) {
    const doc = this.documents.get(documentId)
    if (!doc) return false

    const pageIndex = doc.pages.findIndex(p => p.id === pageId)
    if (pageIndex === -1) return false

    // 删除页面中的所有分段
    const page = doc.pages[pageIndex]
    page.segmentIds.forEach(segmentId => {
      const segment = doc.segments.find(s => s.id === segmentId)
      if (segment) {
        segment.deleted = true
        segment.deletedAt = Date.now()
      }
    })

    doc.pages.splice(pageIndex, 1)
    // 重新编号页面
    doc.pages.forEach((p, index) => {
      p.pageNumber = index + 1
    })

    doc.updatedAt = Date.now()
    doc.version++

    this.saveToStorage()
    this.emit('documentUpdated', { document: doc, type: 'pageDeleted', pageId })
    return true
  }

  /**
   * 创建分段
   */
  createSegment(documentId, pageId, type = 'paragraph', content = '', metadata = {}) {
    const doc = this.documents.get(documentId)
    if (!doc) return null

    const page = doc.pages.find(p => p.id === pageId)
    if (!page) return null

    const segment = new DocumentSegment({
      documentId,
      pageId,
      index: page.segmentIds.length,
      type,
      content,
      metadata,
      createdBy: this.currentUserId,
      updatedBy: this.currentUserId
    })

    doc.addSegment(segment, pageId)
    this.saveToStorage()
    this.emit('segmentUpdated', { document: doc, segment, type: 'created' })
    return segment
  }

  /**
   * 更新分段
   */
  updateSegment(documentId, segmentId, updates) {
    const doc = this.documents.get(documentId)
    if (!doc) return null

    const segment = doc.segments.find(s => s.id === segmentId)
    if (!segment) return null

    if (updates.content !== undefined) {
      segment.updateContent(updates.content, this.currentUserId)
    }
    if (updates.metadata !== undefined) {
      segment.updateMetadata(updates.metadata, this.currentUserId)
    }
    if (updates.type !== undefined) {
      segment.type = updates.type
      segment.updatedAt = Date.now()
      segment.updatedBy = this.currentUserId
      segment.version++
    }

    doc.updatedAt = Date.now()
    doc.version++

    this.saveToStorage()
    this.emit('segmentUpdated', { document: doc, segment, type: 'updated' })
    return segment
  }

  /**
   * 删除分段（软删除）
   */
  deleteSegment(documentId, segmentId) {
    const doc = this.documents.get(documentId)
    if (!doc) return null

    const segment = doc.segments.find(s => s.id === segmentId)
    if (!segment) return null

    segment.deleted = true
    segment.deletedAt = Date.now()
    segment.updatedBy = this.currentUserId

    // 从页面中移除
    const page = doc.pages.find(p => p.id === segment.pageId)
    if (page) {
      page.removeSegment(segmentId)
    }

    doc.updatedAt = Date.now()
    doc.version++

    this.saveToStorage()
    this.emit('segmentUpdated', { document: doc, segment, type: 'deleted' })
    return true
  }

  /**
   * 移动分段
   */
  moveSegment(documentId, segmentId, targetPageId, targetIndex) {
    const doc = this.documents.get(documentId)
    if (!doc) return false

    const segment = doc.segments.find(s => s.id === segmentId)
    if (!segment) return false

    // 从原页面移除
    const sourcePage = doc.pages.find(p => p.id === segment.pageId)
    if (sourcePage) {
      sourcePage.removeSegment(segmentId)
    }

    // 添加到目标页面
    const targetPage = doc.pages.find(p => p.id === targetPageId)
    if (targetPage) {
      targetPage.addSegment(segmentId, targetIndex)
      segment.pageId = targetPageId
      segment.index = targetIndex
      segment.updatedAt = Date.now()
      segment.updatedBy = this.currentUserId
      segment.version++
    }

    doc.updatedAt = Date.now()
    doc.version++

    this.saveToStorage()
    this.emit('segmentUpdated', { document: doc, segment, type: 'moved' })
    return true
  }

  /**
   * 分割分段
   */
  splitSegment(documentId, segmentId, splitPosition) {
    const doc = this.documents.get(documentId)
    if (!doc) return null

    const segment = doc.segments.find(s => s.id === segmentId)
    if (!segment) return null

    const contentBefore = segment.content.substring(0, splitPosition)
    const contentAfter = segment.content.substring(splitPosition)

    // 更新原分段
    segment.updateContent(contentBefore, this.currentUserId)

    // 创建新分段
    const newSegment = new DocumentSegment({
      documentId,
      pageId: segment.pageId,
      index: segment.index + 1,
      type: segment.type,
      content: contentAfter,
      metadata: { ...segment.metadata },
      createdBy: this.currentUserId,
      updatedBy: this.currentUserId
    })

    doc.addSegment(newSegment, segment.pageId, segment.index + 1)

    // 更新后续分段的索引
    const page = doc.pages.find(p => p.id === segment.pageId)
    if (page) {
      page.segmentIds.forEach((id, index) => {
        const seg = doc.segments.find(s => s.id === id)
        if (seg && seg.index > segment.index) {
          seg.index = index
        }
      })
    }

    doc.updatedAt = Date.now()
    doc.version++

    this.saveToStorage()
    this.emit('segmentUpdated', { document: doc, segment: newSegment, type: 'split' })
    return newSegment
  }

  /**
   * 合并分段
   */
  mergeSegments(documentId, segmentId1, segmentId2) {
    const doc = this.documents.get(documentId)
    if (!doc) return null

    const segment1 = doc.segments.find(s => s.id === segmentId1)
    const segment2 = doc.segments.find(s => s.id === segmentId2)
    if (!segment1 || !segment2) return null

    // 合并内容
    const mergedContent = segment1.content + segment2.content
    segment1.updateContent(mergedContent, this.currentUserId)

    // 删除第二个分段
    this.deleteSegment(documentId, segmentId2)

    doc.updatedAt = Date.now()
    doc.version++

    this.saveToStorage()
    this.emit('segmentUpdated', { document: doc, segment: segment1, type: 'merged' })
    return segment1
  }

  /**
   * ==================== 协同编辑管理 ====================
   */

  /**
   * 添加协作者
   */
  addCollaborator(documentId, collaboratorData) {
    const doc = this.documents.get(documentId)
    if (!doc) return null

    const collaborator = new Collaborator(collaboratorData)
    doc.addCollaborator(collaborator)

    this.saveToStorage()
    this.emit('collaboratorJoined', { document: doc, collaborator })
    return collaborator
  }

  /**
   * 移除协作者
   */
  removeCollaborator(documentId, userId) {
    const doc = this.documents.get(documentId)
    if (!doc) return null

    doc.removeCollaborator(userId)

    this.saveToStorage()
    this.emit('collaboratorLeft', { document: doc, userId })
    return true
  }

  /**
   * 更新协作者光标位置
   */
  updateCollaboratorCursor(documentId, userId, cursorPosition, selectionRange = null) {
    const doc = this.documents.get(documentId)
    if (!doc) return null

    const collaborator = doc.collaborators.find(c => c.userId === userId)
    if (collaborator) {
      collaborator.cursorPosition = cursorPosition
      collaborator.selectionRange = selectionRange
      collaborator.lastActiveAt = Date.now()
      collaborator.isActive = true
    }

    this.saveToStorage()
    return collaborator
  }

  /**
   * 应用操作
   */
  applyOperation(documentId, operation) {
    const doc = this.documents.get(documentId)
    if (!doc) return null

    // 检测冲突
    const conflict = this.detectConflict(doc, operation)
    if (conflict) {
      operation.markConflict()
      this.emit('conflictDetected', { document: doc, operation, conflict })
      return null
    }

    // 应用操作
    let result = null
    switch (operation.type) {
      case OperationType.INSERT:
        result = this.applyInsertOperation(doc, operation)
        break
      case OperationType.DELETE:
        result = this.applyDeleteOperation(doc, operation)
        break
      case OperationType.UPDATE:
        result = this.applyUpdateOperation(doc, operation)
        break
      case OperationType.MOVE:
        result = this.applyMoveOperation(doc, operation)
        break
      case OperationType.SPLIT:
        result = this.applySplitOperation(doc, operation)
        break
      case OperationType.MERGE:
        result = this.applyMergeOperation(doc, operation)
        break
    }

    if (result) {
      operation.apply()
      doc.addOperation(operation)
      this.operations.set(operation.id, operation)
      doc.updatedAt = Date.now()
      doc.version++

      this.saveToStorage()
      this.emit('operationApplied', { document: doc, operation, result })
    }

    return result
  }

  /**
   * 应用插入操作
   */
  applyInsertOperation(doc, operation) {
    if (operation.targetType === 'segment') {
      const segment = doc.segments.find(s => s.id === operation.targetId)
      if (segment) {
        const newContent = segment.content.substring(0, operation.position) +
                          operation.content +
                          segment.content.substring(operation.position)
        segment.updateContent(newContent, operation.userId)
        return segment
      }
    }
    return null
  }

  /**
   * 应用删除操作
   */
  applyDeleteOperation(doc, operation) {
    if (operation.targetType === 'segment') {
      return this.deleteSegment(doc.id, operation.targetId)
    } else if (operation.targetType === 'page') {
      return this.deletePage(doc.id, operation.targetId)
    }
    return null
  }

  /**
   * 应用更新操作
   */
  applyUpdateOperation(doc, operation) {
    if (operation.targetType === 'segment') {
      return this.updateSegment(doc.id, operation.targetId, {
        content: operation.content,
        metadata: operation.metadata
      })
    }
    return null
  }

  /**
   * 应用移动操作
   */
  applyMoveOperation(doc, operation) {
    if (operation.targetType === 'segment') {
      const { targetPageId, targetIndex } = operation.metadata
      return this.moveSegment(doc.id, operation.targetId, targetPageId, targetIndex)
    }
    return null
  }

  /**
   * 应用分割操作
   */
  applySplitOperation(doc, operation) {
    if (operation.targetType === 'segment') {
      return this.splitSegment(doc.id, operation.targetId, operation.position)
    }
    return null
  }

  /**
   * 应用合并操作
   */
  applyMergeOperation(doc, operation) {
    if (operation.targetType === 'segment') {
      const { segmentId2 } = operation.metadata
      return this.mergeSegments(doc.id, operation.targetId, segmentId2)
    }
    return null
  }

  /**
   * 检测冲突
   */
  detectConflict(doc, operation) {
    // 检查向量时钟
    if (operation.vectorClock) {
      for (const [userId, clock] of Object.entries(operation.vectorClock)) {
        const localClock = doc.vectorClock[userId] || 0
        if (clock <= localClock) {
          // 操作可能已过期或冲突
          return {
            type: 'vector_clock',
            message: `操作来自用户 ${userId} 的时钟 ${clock}，本地时钟为 ${localClock}`
          }
        }
      }
    }

    // 检查目标分段版本
    if (operation.targetType === 'segment') {
      const segment = doc.segments.find(s => s.id === operation.targetId)
      if (segment) {
        const operationVersion = operation.metadata?.segmentVersion || 0
        if (segment.version > operationVersion) {
          return {
            type: 'version_conflict',
            message: `分段版本冲突：操作基于版本 ${operationVersion}，当前版本为 ${segment.version}`
          }
        }
      }
    }

    return null
  }

  /**
   * ==================== 版本管理 ====================
   */

  /**
   * 创建快照
   */
  createSnapshot(documentId, description = '') {
    const doc = this.documents.get(documentId)
    if (!doc) return null

    const snapshot = new DocumentSnapshot({
      documentId,
      version: doc.version,
      snapshot: doc.toJSON(),
      description,
      createdBy: this.currentUserId
    })

    this.snapshots.set(snapshot.id, snapshot)
    this.saveToStorage()
    return snapshot
  }

  /**
   * 恢复快照
   */
  restoreSnapshot(snapshotId) {
    const snapshot = this.snapshots.get(snapshotId)
    if (!snapshot) return null

    const doc = Document.fromJSON(snapshot.snapshot)
    this.documents.set(doc.id, doc)

    this.saveToStorage()
    this.emit('documentUpdated', { document: doc, type: 'restored', snapshot })
    return doc
  }

  /**
   * 获取文档的所有快照
   */
  getDocumentSnapshots(documentId) {
    return Array.from(this.snapshots.values())
      .filter(snap => snap.documentId === documentId)
      .sort((a, b) => b.createdAt - a.createdAt)
  }

  /**
   * ==================== 事件系统 ====================
   */

  /**
   * 监听事件
   */
  on(event, callback) {
    if (this.eventListeners[event]) {
      this.eventListeners[event].push(callback)
    }
    return this
  }

  /**
   * 取消监听
   */
  off(event, callback) {
    if (this.eventListeners[event]) {
      this.eventListeners[event] = this.eventListeners[event].filter(cb => cb !== callback)
    }
    return this
  }

  /**
   * 触发事件
   */
  emit(event, data) {
    if (this.eventListeners[event]) {
      this.eventListeners[event].forEach(callback => {
        try {
          callback(data)
        } catch (error) {
          console.error(`事件监听器错误 (${event}):`, error)
        }
      })
    }
    return this
  }

  /**
   * ==================== 同步管理 ====================
   */

  /**
   * 开始自动同步
   */
  startAutoSync(interval = 5000) {
    if (this.syncInterval) {
      clearInterval(this.syncInterval)
    }
    this.syncInterval = setInterval(() => {
      this.syncPendingOperations()
    }, interval)
    return this
  }

  /**
   * 停止自动同步
   */
  stopAutoSync() {
    if (this.syncInterval) {
      clearInterval(this.syncInterval)
      this.syncInterval = null
    }
    return this
  }

  /**
   * 同步待处理操作
   */
  async syncPendingOperations() {
    // 这里应该调用后端API同步操作
    // 暂时只保存到本地存储
    this.saveToStorage()
  }

  /**
   * 导出文档数据（用于备份或迁移）
   */
  exportDocument(documentId) {
    const doc = this.documents.get(documentId)
    if (!doc) return null

    return {
      document: doc.toJSON(),
      snapshots: this.getDocumentSnapshots(documentId).map(s => s.toJSON())
    }
  }

  /**
   * 导入文档数据
   */
  importDocument(data) {
    if (data.document) {
      const doc = Document.fromJSON(data.document)
      this.documents.set(doc.id, doc)
    }
    if (data.snapshots) {
      data.snapshots.forEach(snapJson => {
        const snap = DocumentSnapshot.fromJSON(snapJson)
        this.snapshots.set(snap.id, snap)
      })
    }
    this.saveToStorage()
    return this
  }
}

// 创建全局单例
let documentStoreInstance = null

/**
 * 获取文档库实例（单例模式）
 */
export function getDocumentStore() {
  if (!documentStoreInstance) {
    documentStoreInstance = new DocumentStore()
  }
  return documentStoreInstance
}

export default DocumentStore


