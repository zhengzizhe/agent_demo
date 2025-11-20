/**
 * 文档库管理 Composable
 */
import { ref, computed } from 'vue'

export function useDocumentLibrary() {
  // 状态
  const documents = ref([])
  const spaces = ref([])
  const currentSpace = ref(null)
  const loading = ref(false)
  const error = ref(null)

  /**
   * 加载空间列表
   */
  const loadSpaces = async () => {
    try {
      loading.value = true
      error.value = null
      
      const response = await fetch('/api/spaces', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        }
      })
      
      if (!response.ok) {
        throw new Error('加载空间列表失败')
      }
      
      const data = await response.json()
      spaces.value = data.items || []
      
      // 如果没有当前空间，设置第一个为当前空间
      if (!currentSpace.value && spaces.value.length > 0) {
        currentSpace.value = spaces.value[0]
      }
    } catch (err) {
      console.error('加载空间列表失败:', err)
      error.value = err.message
    } finally {
      loading.value = false
    }
  }

  /**
   * 加载文档列表
   */
  const loadDocuments = async (params = {}) => {
    try {
      loading.value = true
      error.value = null
      
      const queryParams = {
        spaceId: currentSpace.value?.id,
        pageNumber: params.pageNumber || 1,
        pageSize: params.pageSize || 50,
        sortBy: params.sortBy || 'time',
        view: params.view || 'home', // home, shared, favorites
        keyword: params.keyword || ''
      }
      
      let endpoint = '/api/documents'
      if (queryParams.view === 'shared') {
        endpoint = '/api/documents/shared'
      } else if (queryParams.view === 'favorites') {
        endpoint = '/api/documents/favorites'
      }
      
      const response = await fetch(endpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(queryParams)
      })
      
      if (!response.ok) {
        throw new Error('加载文档列表失败')
      }
      
      const data = await response.json()
      documents.value = data.items || []
    } catch (err) {
      console.error('加载文档列表失败:', err)
      error.value = err.message
    } finally {
      loading.value = false
    }
  }

  /**
   * 创建文档
   */
  const createDocument = async (documentData) => {
    try {
      loading.value = true
      error.value = null
      
      const response = await fetch('/api/documents', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          name: documentData.name || '未命名文档',
          type: documentData.type || 'text',
          spaceId: currentSpace.value?.id,
          description: documentData.description || '',
          content: documentData.content || ''
        })
      })
      
      if (!response.ok) {
        throw new Error('创建文档失败')
      }
      
      const newDoc = await response.json()
      documents.value.unshift(newDoc)
      return newDoc
    } catch (err) {
      console.error('创建文档失败:', err)
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新文档
   */
  const updateDocument = async (docId, updates) => {
    try {
      loading.value = true
      error.value = null
      
      const response = await fetch(`/api/documents/${docId}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(updates)
      })
      
      if (!response.ok) {
        throw new Error('更新文档失败')
      }
      
      const updatedDoc = await response.json()
      const index = documents.value.findIndex(doc => doc.id === docId)
      if (index > -1) {
        documents.value[index] = updatedDoc
      }
      return updatedDoc
    } catch (err) {
      console.error('更新文档失败:', err)
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 删除文档
   */
  const deleteDocument = async (docId) => {
    try {
      loading.value = true
      error.value = null
      
      const response = await fetch(`/api/documents/${docId}`, {
        method: 'DELETE'
      })
      
      if (!response.ok) {
        throw new Error('删除文档失败')
      }
      
      const index = documents.value.findIndex(doc => doc.id === docId)
      if (index > -1) {
        documents.value.splice(index, 1)
      }
    } catch (err) {
      console.error('删除文档失败:', err)
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 复制文档
   */
  const duplicateDocument = async (docId) => {
    try {
      loading.value = true
      error.value = null
      
      const response = await fetch(`/api/documents/${docId}/duplicate`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          spaceId: currentSpace.value?.id
        })
      })
      
      if (!response.ok) {
        throw new Error('复制文档失败')
      }
      
      const newDoc = await response.json()
      documents.value.unshift(newDoc)
      return newDoc
    } catch (err) {
      console.error('复制文档失败:', err)
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 切换收藏状态
   */
  const toggleFavorite = async (docId) => {
    try {
      const doc = documents.value.find(d => d.id === docId)
      if (!doc) return
      
      const isFavorite = doc.favorite
      
      const response = await fetch(`/api/documents/${docId}/favorite`, {
        method: isFavorite ? 'DELETE' : 'POST'
      })
      
      if (!response.ok) {
        throw new Error('切换收藏状态失败')
      }
      
      doc.favorite = !isFavorite
    } catch (err) {
      console.error('切换收藏状态失败:', err)
      error.value = err.message
      throw err
    }
  }

  /**
   * 搜索文档
   */
  const searchDocuments = async (keyword) => {
    try {
      loading.value = true
      error.value = null
      
      const response = await fetch('/api/documents/search', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          keyword,
          spaceId: currentSpace.value?.id,
          pageNumber: 1,
          pageSize: 50
        })
      })
      
      if (!response.ok) {
        throw new Error('搜索文档失败')
      }
      
      const data = await response.json()
      return data.items || []
    } catch (err) {
      console.error('搜索文档失败:', err)
      error.value = err.message
      return []
    } finally {
      loading.value = false
    }
  }

  /**
   * 设置当前空间
   */
  const setCurrentSpace = (space) => {
    currentSpace.value = space
    loadDocuments()
  }

  return {
    // 状态
    documents,
    spaces,
    currentSpace,
    loading,
    error,
    
    // 方法
    loadSpaces,
    loadDocuments,
    createDocument,
    updateDocument,
    deleteDocument,
    duplicateDocument,
    toggleFavorite,
    searchDocuments,
    setCurrentSpace
  }
}



