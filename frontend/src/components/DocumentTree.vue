<template>
  <div class="document-tree" :class="{ collapsed: collapsed }">
    <div v-if="!collapsed" class="tree-content">
      <div v-if="filteredTreeNodes.length === 0" class="tree-empty">
        <svg width="48" height="48" viewBox="0 0 48 48" fill="none" class="empty-icon">
          <path d="M8 12C8 10.8954 8.89543 10 10 10H19L22 14H38C39.1046 14 40 14.8954 40 16V36C40 37.1046 39.1046 38 38 38H10C8.89543 38 8 37.1046 8 36V12Z" stroke="currentColor" stroke-width="2" fill="none" opacity="0.3"/>
          <path d="M8 12H22L19 16H8V12Z" fill="currentColor" opacity="0.1"/>
        </svg>
        <p class="empty-text">暂无文件夹</p>
      </div>
      <div v-else class="tree-list">
        <DocumentTreeNode
          v-for="node in filteredTreeNodes"
          :key="node.id"
          :node="node"
          :selected-id="selectedId"
          :expanded-ids="expandedIds"
          @select="handleSelect"
          @toggle="handleToggle"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import DocumentTreeNode from './DocumentTreeNode.vue'

const props = defineProps({
  documents: {
    type: Array,
    default: () => []
  },
  selectedId: {
    type: String,
    default: null
  }
})

const emit = defineEmits(['select', 'folder-select'])

const collapsed = ref(false)
const expandedIds = ref([])

// 构建树形结构
const treeNodes = computed(() => {
  if (!props.documents || !Array.isArray(props.documents)) {
    return []
  }
  const docs = props.documents.filter(doc => doc && !doc.deleted)
  const folders = docs.filter(doc => doc.isFolder === true)
  const files = docs.filter(doc => !doc.isFolder)
  
  // 构建文件夹映射
  const folderMap = new Map()
  folders.forEach(folder => {
    folderMap.set(folder.id, {
      ...folder,
      children: [],
      level: 0
    })
  })
  
  // 递归设置层级
  const setLevel = (node, level) => {
    node.level = level
    if (node.children && Array.isArray(node.children)) {
      node.children.forEach(child => {
        setLevel(child, level + 1)
      })
    }
  }
  
  // 处理文件夹的层级关系
  const rootFolders = []
  folderMap.forEach((folder, id) => {
    if (!folder || !folder.parentId) {
      rootFolders.push(folder)
    } else if (folderMap.has(folder.parentId)) {
      const parentFolder = folderMap.get(folder.parentId)
      if (parentFolder && Array.isArray(parentFolder.children)) {
        parentFolder.children.push(folder)
      }
    }
  })
  
  // 将文件添加到对应的文件夹
  files.forEach(file => {
    if (file && file.parentId && folderMap.has(file.parentId)) {
      const folder = folderMap.get(file.parentId)
      if (folder && Array.isArray(folder.children)) {
        folder.children.push({
          ...file,
          children: []
        })
      }
    }
  })
  
  // 设置所有节点的层级
  rootFolders.forEach(root => {
    setLevel(root, 0)
  })
  
  // 添加没有父级的文件
  const rootFiles = files.filter(file => file && !file.parentId)
  rootFiles.forEach(file => {
    if (file) {
      rootFolders.push({
        ...file,
        children: [],
        level: 0
      })
    }
  })
  
  // 排序：文件夹在前，文档在后，同类型按名称排序
  return rootFolders.sort((a, b) => {
    if (a.isFolder && !b.isFolder) return -1
    if (!a.isFolder && b.isFolder) return 1
    return (a.name || '').localeCompare(b.name || '')
  })
})

// 直接使用树节点，不再需要过滤
const filteredTreeNodes = computed(() => treeNodes.value)

const handleSelect = (node) => {
  emit('select', node)
  if (node.isFolder) {
    emit('folder-select', node.id)
  }
}

const handleToggle = (nodeId) => {
  const index = expandedIds.value.indexOf(nodeId)
  if (index > -1) {
    expandedIds.value.splice(index, 1)
  } else {
    expandedIds.value.push(nodeId)
  }
}

// 监听选中项变化，自动展开父级
watch(() => props.selectedId, (newId) => {
  if (newId) {
    const findParent = (nodes, targetId, parentIds = []) => {
      for (const node of nodes) {
        if (node.id === targetId) {
          // 使用 Set 优化查找性能
          const idsSet = new Set(expandedIds.value)
          parentIds.forEach(id => {
            if (!idsSet.has(id)) {
              idsSet.add(id)
            }
          })
          expandedIds.value = Array.from(idsSet)
          return true
        }
        if (node.children && node.children.length > 0) {
          if (findParent(node.children, targetId, [...parentIds, node.id])) {
            return true
          }
        }
      }
      return false
    }
    findParent(treeNodes.value, newId)
  }
}, { immediate: false })
</script>

<style scoped>
.document-tree {
  width: 280px;
  height: 100%;
  background: transparent;
  display: flex;
  flex-direction: column;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
  position: relative;
  overflow: hidden;
}

.document-tree.collapsed {
  width: 48px;
}

.document-tree.collapsed .tree-content {
  display: none;
}

.tree-content {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background: transparent;
}

.tree-list {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 6px 8px;
  scroll-behavior: smooth;
}

/* 隐藏滚动条但保持滚动功能 */
.tree-list::-webkit-scrollbar {
  width: 0;
  display: none;
}

.tree-list {
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
}

/* 空状态 */
.tree-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #9ca3af;
  text-align: center;
}

.empty-icon {
  margin-bottom: 12px;
  opacity: 0.4;
}

.empty-text {
  font-size: 13px;
  color: #6b7280;
  margin: 0;
}
</style>

