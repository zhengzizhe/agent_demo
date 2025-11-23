/**
 * 文档数据类型定义
 * 支持协同编辑和分页分段存储
 */

/**
 * 文档类型枚举
 */
export const DocumentType = {
  TEXT: 'text',           // 文本文档
  MARKDOWN: 'markdown',   // Markdown文档
  RICH_TEXT: 'rich_text'  // 富文本文档
}

/**
 * 分段类型枚举
 */
export const SegmentType = {
  PARAGRAPH: 'paragraph',     // 段落
  HEADING: 'heading',         // 标题
  LIST: 'list',              // 列表
  CODE_BLOCK: 'code_block',   // 代码块
  TABLE: 'table',            // 表格
  IMAGE: 'image',            // 图片
  QUOTE: 'quote',            // 引用
  DIVIDER: 'divider',        // 分割线
  CUSTOM: 'custom'           // 自定义块
}

/**
 * 操作类型枚举（用于协同编辑）
 */
export const OperationType = {
  INSERT: 'insert',          // 插入
  DELETE: 'delete',          // 删除
  UPDATE: 'update',          // 更新
  FORMAT: 'format',          // 格式化
  MOVE: 'move',              // 移动
  SPLIT: 'split',            // 分段
  MERGE: 'merge'             // 合并
}

/**
 * 操作状态枚举
 */
export const OperationStatus = {
  PENDING: 'pending',        // 待处理
  APPLIED: 'applied',        // 已应用
  CONFLICT: 'conflict',      // 冲突
  REJECTED: 'rejected'       // 已拒绝
}

/**
 * 协同编辑状态枚举
 */
export const CollaborationStatus = {
  IDLE: 'idle',             // 空闲
  EDITING: 'editing',       // 编辑中
  CONFLICT: 'conflict',     // 冲突
  SYNCING: 'syncing'        // 同步中
}

/**
 * 文档分段（Document Segment）
 * 文档的最小存储单元，支持分页
 */
export class DocumentSegment {
  constructor(data = {}) {
    this.id = data.id || this.generateId()           // 分段ID（全局唯一）
    this.documentId = data.documentId || ''         // 所属文档ID
    this.pageId = data.pageId || null              // 所属页面ID（用于分页）
    this.index = data.index || 0                     // 在文档中的索引位置
    this.type = data.type || SegmentType.PARAGRAPH  // 分段类型
    this.content = data.content || ''               // 分段内容（HTML或Markdown）
    this.metadata = data.metadata || {}             // 元数据（格式、样式等）
    this.version = data.version || 1                // 版本号（用于冲突解决）
    this.createdAt = data.createdAt || Date.now()   // 创建时间
    this.updatedAt = data.updatedAt || Date.now()   // 更新时间
    this.createdBy = data.createdBy || ''           // 创建者ID
    this.updatedBy = data.updatedBy || ''            // 最后更新者ID
    this.deleted = data.deleted || false            // 是否已删除（软删除）
    this.deletedAt = data.deletedAt || null         // 删除时间
  }

  generateId() {
    return `seg_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  }

  /**
   * 更新分段内容
   */
  updateContent(content, userId) {
    this.content = content
    this.updatedAt = Date.now()
    this.updatedBy = userId
    this.version++
    return this
  }

  /**
   * 更新元数据
   */
  updateMetadata(metadata, userId) {
    this.metadata = { ...this.metadata, ...metadata }
    this.updatedAt = Date.now()
    this.updatedBy = userId
    this.version++
    return this
  }

  /**
   * 转换为JSON
   */
  toJSON() {
    return {
      id: this.id,
      documentId: this.documentId,
      pageId: this.pageId,
      index: this.index,
      type: this.type,
      content: this.content,
      metadata: this.metadata,
      version: this.version,
      createdAt: this.createdAt,
      updatedAt: this.updatedAt,
      createdBy: this.createdBy,
      updatedBy: this.updatedBy,
      deleted: this.deleted,
      deletedAt: this.deletedAt
    }
  }

  /**
   * 从JSON创建
   */
  static fromJSON(json) {
    return new DocumentSegment(json)
  }
}

/**
 * 文档页面（Document Page）
 * 用于分页管理
 */
export class DocumentPage {
  constructor(data = {}) {
    this.id = data.id || this.generateId()           // 页面ID
    this.documentId = data.documentId || ''         // 所属文档ID
    this.pageNumber = data.pageNumber || 1           // 页码
    this.title = data.title || ''                   // 页面标题
    this.segmentIds = data.segmentIds || []         // 分段ID列表（按顺序）
    this.metadata = data.metadata || {}             // 页面元数据
    this.createdAt = data.createdAt || Date.now()   // 创建时间
    this.updatedAt = data.updatedAt || Date.now()   // 更新时间
    this.createdBy = data.createdBy || ''           // 创建者ID
    this.updatedBy = data.updatedBy || ''            // 最后更新者ID
  }

  generateId() {
    return `page_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  }

  /**
   * 添加分段
   */
  addSegment(segmentId, index = -1) {
    if (index === -1) {
      this.segmentIds.push(segmentId)
    } else {
      this.segmentIds.splice(index, 0, segmentId)
    }
    this.updatedAt = Date.now()
    return this
  }

  /**
   * 移除分段
   */
  removeSegment(segmentId) {
    const index = this.segmentIds.indexOf(segmentId)
    if (index > -1) {
      this.segmentIds.splice(index, 1)
      this.updatedAt = Date.now()
    }
    return this
  }

  /**
   * 移动分段
   */
  moveSegment(segmentId, newIndex) {
    const oldIndex = this.segmentIds.indexOf(segmentId)
    if (oldIndex > -1) {
      this.segmentIds.splice(oldIndex, 1)
      this.segmentIds.splice(newIndex, 0, segmentId)
      this.updatedAt = Date.now()
    }
    return this
  }

  toJSON() {
    return {
      id: this.id,
      documentId: this.documentId,
      pageNumber: this.pageNumber,
      title: this.title,
      segmentIds: this.segmentIds,
      metadata: this.metadata,
      createdAt: this.createdAt,
      updatedAt: this.updatedAt,
      createdBy: this.createdBy,
      updatedBy: this.updatedBy
    }
  }

  static fromJSON(json) {
    return new DocumentPage(json)
  }
}

/**
 * 协同编辑操作（Collaboration Operation）
 * 用于记录和同步编辑操作
 */
export class CollaborationOperation {
  constructor(data = {}) {
    this.id = data.id || this.generateId()           // 操作ID
    this.documentId = data.documentId || ''         // 文档ID
    this.userId = data.userId || ''                  // 操作用户ID
    this.type = data.type || OperationType.INSERT    // 操作类型
    this.targetId = data.targetId || ''             // 目标分段ID或页面ID
    this.targetType = data.targetType || 'segment'   // 目标类型：segment | page
    this.position = data.position || 0               // 操作位置（索引或偏移）
    this.content = data.content || ''                // 操作内容
    this.metadata = data.metadata || {}             // 操作元数据
    this.status = data.status || OperationStatus.PENDING  // 操作状态
    this.timestamp = data.timestamp || Date.now()   // 操作时间戳
    this.vectorClock = data.vectorClock || {}        // 向量时钟（用于冲突检测）
    this.parentOperationId = data.parentOperationId || null  // 父操作ID（用于操作链）
    this.conflictResolution = data.conflictResolution || null  // 冲突解决策略
  }

  generateId() {
    return `op_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  }

  /**
   * 应用操作
   */
  apply() {
    this.status = OperationStatus.APPLIED
    return this
  }

  /**
   * 标记冲突
   */
  markConflict() {
    this.status = OperationStatus.CONFLICT
    return this
  }

  /**
   * 更新向量时钟
   */
  updateVectorClock(userId, clock) {
    this.vectorClock = { ...this.vectorClock, [userId]: clock }
    return this
  }

  toJSON() {
    return {
      id: this.id,
      documentId: this.documentId,
      userId: this.userId,
      type: this.type,
      targetId: this.targetId,
      targetType: this.targetType,
      position: this.position,
      content: this.content,
      metadata: this.metadata,
      status: this.status,
      timestamp: this.timestamp,
      vectorClock: this.vectorClock,
      parentOperationId: this.parentOperationId,
      conflictResolution: this.conflictResolution
    }
  }

  static fromJSON(json) {
    return new CollaborationOperation(json)
  }
}

/**
 * 协作者信息（Collaborator）
 */
export class Collaborator {
  constructor(data = {}) {
    this.userId = data.userId || ''                  // 用户ID
    this.name = data.name || ''                      // 用户名
    this.avatar = data.avatar || ''                  // 头像
    this.color = data.color || '#165dff'            // 用户颜色（用于光标显示）
    this.cursorPosition = data.cursorPosition || null  // 光标位置
    this.selectionRange = data.selectionRange || null   // 选择范围
    this.isActive = data.isActive || false          // 是否在线
    this.lastActiveAt = data.lastActiveAt || Date.now()  // 最后活跃时间
    this.permissions = data.permissions || ['read', 'write']  // 权限
  }

  toJSON() {
    return {
      userId: this.userId,
      name: this.name,
      avatar: this.avatar,
      color: this.color,
      cursorPosition: this.cursorPosition,
      selectionRange: this.selectionRange,
      isActive: this.isActive,
      lastActiveAt: this.lastActiveAt,
      permissions: this.permissions
    }
  }

  static fromJSON(json) {
    return new Collaborator(json)
  }
}

/**
 * 文档（Document）
 * 完整的文档数据结构
 */
export class Document {
  constructor(data = {}) {
    this.id = data.id || this.generateId()           // 文档ID
    this.name = data.name || '未命名文档'            // 文档名称
    this.type = data.type || DocumentType.RICH_TEXT  // 文档类型
    this.description = data.description || ''        // 文档描述
    this.coverImage = data.coverImage || null        // 封面图片
    this.owner = data.owner || ''                    // 所有者ID
    this.spaceId = data.spaceId || null              // 所属空间ID
    this.favorite = data.favorite || false           // 是否收藏
    this.shared = data.shared || false                // 是否共享
    this.tags = data.tags || []                      // 标签列表
    this.metadata = data.metadata || {}             // 文档元数据
    
    // 分页分段数据
    this.pages = (data.pages || []).map(p => DocumentPage.fromJSON(p))  // 页面列表
    this.segments = (data.segments || []).map(s => DocumentSegment.fromJSON(s))  // 分段列表（所有页面的分段）
    
    // 协同编辑数据
    this.collaborators = (data.collaborators || []).map(c => Collaborator.fromJSON(c))  // 协作者列表
    this.collaborationStatus = data.collaborationStatus || CollaborationStatus.IDLE  // 协同状态
    this.pendingOperations = (data.pendingOperations || []).map(o => CollaborationOperation.fromJSON(o))  // 待处理操作
    this.vectorClock = data.vectorClock || {}       // 向量时钟（用于冲突检测）
    
    // 版本控制
    this.version = data.version || 1                 // 文档版本号
    this.lastModified = data.lastModified || Date.now()  // 最后修改时间
    this.lastModifiedBy = data.lastModifiedBy || ''   // 最后修改者ID
    
    // 时间戳
    this.createdAt = data.createdAt || Date.now()     // 创建时间
    this.updatedAt = data.updatedAt || Date.now()    // 更新时间
    this.deleted = data.deleted || false             // 是否已删除
    this.deletedAt = data.deletedAt || null          // 删除时间
  }

  generateId() {
    return `doc_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  }

  /**
   * 获取指定页面的分段
   */
  getSegmentsByPage(pageId) {
    const page = this.pages.find(p => p.id === pageId)
    if (!page) return []
    return page.segmentIds
      .map(id => this.segments.find(s => s.id === id))
      .filter(s => s && !s.deleted)
  }

  /**
   * 获取所有分段（按页面和索引排序）
   */
  getAllSegments() {
    const allSegments = []
    this.pages.forEach(page => {
      const pageSegments = this.getSegmentsByPage(page.id)
      allSegments.push(...pageSegments)
    })
    return allSegments.sort((a, b) => {
      const pageA = this.pages.find(p => p.segmentIds.includes(a.id))
      const pageB = this.pages.find(p => p.segmentIds.includes(b.id))
      if (pageA.pageNumber !== pageB.pageNumber) {
        return pageA.pageNumber - pageB.pageNumber
      }
      return a.index - b.index
    })
  }

  /**
   * 添加分段到指定页面
   */
  addSegment(segment, pageId, index = -1) {
    // 确保分段在segments列表中
    const existingIndex = this.segments.findIndex(s => s.id === segment.id)
    if (existingIndex === -1) {
      this.segments.push(segment)
    } else {
      this.segments[existingIndex] = segment
    }

    // 添加到页面
    const page = this.pages.find(p => p.id === pageId)
    if (page) {
      page.addSegment(segment.id, index)
      segment.pageId = pageId
      segment.documentId = this.id
    }

    this.updatedAt = Date.now()
    this.version++
    return this
  }

  /**
   * 创建新页面
   */
  createPage(title = '', userId = '') {
    const page = new DocumentPage({
      documentId: this.id,
      pageNumber: this.pages.length + 1,
      title,
      createdBy: userId,
      updatedBy: userId
    })
    this.pages.push(page)
    this.updatedAt = Date.now()
    this.version++
    return page
  }

  /**
   * 添加协作者
   */
  addCollaborator(collaborator) {
    const existing = this.collaborators.find(c => c.userId === collaborator.userId)
    if (!existing) {
      this.collaborators.push(collaborator)
    } else {
      Object.assign(existing, collaborator)
    }
    return this
  }

  /**
   * 移除协作者
   */
  removeCollaborator(userId) {
    this.collaborators = this.collaborators.filter(c => c.userId !== userId)
    return this
  }

  /**
   * 添加操作到待处理队列
   */
  addOperation(operation) {
    this.pendingOperations.push(operation)
    this.updateVectorClock(operation.userId)
    return this
  }

  /**
   * 更新向量时钟
   */
  updateVectorClock(userId) {
    if (!this.vectorClock[userId]) {
      this.vectorClock[userId] = 0
    }
    this.vectorClock[userId]++
    return this
  }

  /**
   * 转换为JSON（用于存储）
   */
  toJSON() {
    return {
      id: this.id,
      name: this.name,
      type: this.type,
      description: this.description,
      coverImage: this.coverImage,
      owner: this.owner,
      spaceId: this.spaceId,
      favorite: this.favorite,
      shared: this.shared,
      tags: this.tags,
      metadata: this.metadata,
      pages: this.pages.map(p => p.toJSON()),
      segments: this.segments.map(s => s.toJSON()),
      collaborators: this.collaborators.map(c => c.toJSON()),
      collaborationStatus: this.collaborationStatus,
      pendingOperations: this.pendingOperations.map(o => o.toJSON()),
      vectorClock: this.vectorClock,
      version: this.version,
      lastModified: this.lastModified,
      lastModifiedBy: this.lastModifiedBy,
      createdAt: this.createdAt,
      updatedAt: this.updatedAt,
      deleted: this.deleted,
      deletedAt: this.deletedAt
    }
  }

  /**
   * 从JSON创建
   */
  static fromJSON(json) {
    return new Document(json)
  }

  /**
   * 创建空文档
   */
  static createEmpty(name = '未命名文档', userId = '', type = DocumentType.RICH_TEXT) {
    const doc = new Document({
      name,
      type,
      owner: userId,
      createdAt: Date.now(),
      updatedAt: Date.now()
    })

    // 创建第一个页面
    const firstPage = doc.createPage('', userId)
    
    // 创建第一个分段（空段落）
    const firstSegment = new DocumentSegment({
      documentId: doc.id,
      pageId: firstPage.id,
      index: 0,
      type: SegmentType.PARAGRAPH,
      content: '',
      createdBy: userId,
      updatedBy: userId
    })

    doc.addSegment(firstSegment, firstPage.id, 0)
    return doc
  }
}

/**
 * 文档快照（Document Snapshot）
 * 用于版本历史和冲突恢复
 */
export class DocumentSnapshot {
  constructor(data = {}) {
    this.id = data.id || this.generateId()           // 快照ID
    this.documentId = data.documentId || ''         // 文档ID
    this.version = data.version || 1                // 版本号
    this.snapshot = data.snapshot || null           // 文档快照数据（Document的JSON）
    this.description = data.description || ''        // 快照描述
    this.createdBy = data.createdBy || ''           // 创建者ID
    this.createdAt = data.createdAt || Date.now()    // 创建时间
  }

  generateId() {
    return `snap_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  }

  toJSON() {
    return {
      id: this.id,
      documentId: this.documentId,
      version: this.version,
      snapshot: this.snapshot,
      description: this.description,
      createdBy: this.createdBy,
      createdAt: this.createdAt
    }
  }

  static fromJSON(json) {
    return new DocumentSnapshot(json)
  }
}

// 导出所有类型
export default {
  DocumentType,
  SegmentType,
  OperationType,
  OperationStatus,
  CollaborationStatus,
  Document,
  DocumentSegment,
  DocumentPage,
  CollaborationOperation,
  Collaborator,
  DocumentSnapshot
}


