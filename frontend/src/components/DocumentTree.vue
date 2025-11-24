<template>
  <div class="document-tree" :class="{ collapsed: collapsed }">
    <div v-if="!collapsed" class="tree-content">
      <div v-if="treeData.length === 0" class="tree-empty">
        <svg width="48" height="48" viewBox="0 0 48 48" fill="none" class="empty-icon">
          <path d="M8 12C8 10.8954 8.89543 10 10 10H19L22 14H38C39.1046 14 40 14.8954 40 16V36C40 37.1046 39.1046 38 38 38H10C8.89543 38 8 37.1046 8 36V12Z" stroke="currentColor" stroke-width="2" fill="none" opacity="0.3"/>
          <path d="M8 12H22L19 16H8V12Z" fill="currentColor" opacity="0.1"/>
        </svg>
        <p class="empty-text">暂无文件夹</p>
      </div>
      
      <div v-else class="tree-list">
        <transition-group name="tree-node" tag="div">
          <TreeNode
            v-for="node in treeData"
            :key="node.id"
            :node="node"
            :selected-id="selectedId"
            :expanded-ids="expandedIds"
            :depth="0"
            @select="handleNodeSelect"
            @toggle="handleNodeToggle"
            @open="handleNodeOpen"
          />
        </transition-group>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import TreeNode from './TreeNode.vue'

const props = defineProps({
  documents: {
    type: Array,
    default: () => []
  },
  selectedId: {
    type: String,
    default: null
  },
  spaceId: {
    type: String,
    default: null
  }
})

const emit = defineEmits(['select', 'folder-select', 'open'])

const collapsed = ref(false)
const expandedIds = ref([])

// 构建树形结构
const treeData = computed(() => {
  if (!props.documents || !Array.isArray(props.documents)) {
    return []
  }
  
  const docs = props.documents.filter(doc => doc && !doc.deleted)
  const nodeMap = new Map()
  const rootNodes = []
  
  // 第一遍：创建所有节点
  docs.forEach(doc => {
    const node = {
      id: doc.id || String(Math.random()),
      name: doc.name || '未命名',
      parentId: doc.parentId || null,
      isFolder: doc.isFolder === true,
      data: doc,
      children: []
    }
    nodeMap.set(node.id, node)
  })
  
  // 第二遍：构建父子关系
  nodeMap.forEach(node => {
    if (node.parentId && nodeMap.has(node.parentId)) {
      const parent = nodeMap.get(node.parentId)
      parent.children.push(node)
    } else {
      rootNodes.push(node)
    }
  })
  
  // 排序：文件夹在前，文档在后
  const sortNodes = (nodes) => {
    return nodes.sort((a, b) => {
      if (a.isFolder && !b.isFolder) return -1
      if (!a.isFolder && b.isFolder) return 1
      return (a.name || '').localeCompare(b.name || '')
    }).map(node => {
      if (node.children.length > 0) {
        node.children = sortNodes(node.children)
      }
      return node
    })
  }
  
  return sortNodes(rootNodes)
})

// 处理节点选择
const handleNodeSelect = (node) => {
  emit('select', node.data)
  if (node.isFolder) {
    emit('folder-select', node.id)
  }
}

// 处理节点打开（双击文档时）
const handleNodeOpen = (node) => {
  emit('open', node.data)
}

// 处理节点展开/收起
const handleNodeToggle = (nodeId) => {
  const index = expandedIds.value.indexOf(nodeId)
  if (index > -1) {
    expandedIds.value.splice(index, 1)
  } else {
    expandedIds.value.push(nodeId)
  }
}

// 监听空间切换，重置展开状态
watch(() => props.spaceId, (newSpaceId, oldSpaceId) => {
  if (newSpaceId !== oldSpaceId && oldSpaceId !== undefined) {
    expandedIds.value = []
  }
}, { immediate: false })

// 监听选中项变化，自动展开父级
watch(() => props.selectedId, (newId) => {
  if (newId) {
    const findParent = (nodeId, nodes, parentIds = []) => {
      for (const node of nodes) {
        if (node.id === nodeId) {
          return true
        }
        if (node.children && node.children.length > 0) {
          if (findParent(nodeId, node.children, parentIds)) {
            parentIds.push(node.id)
            return true
          }
        }
      }
      return false
    }
    
    const parentIds = []
    findParent(newId, treeData.value, parentIds)
    
    // 展开所有父级
    parentIds.forEach(id => {
      if (!expandedIds.value.includes(id)) {
        expandedIds.value.push(id)
      }
    })
  }
}, { immediate: false })
</script>

<style scoped>
.document-tree {
  width: 280px;
  height: 100%;
  background: rgba(255, 255, 255, 0.01);
  /* 性能优化：移除backdrop-filter */
  /* backdrop-filter: blur(6px) saturate(120%); */
  /* -webkit-backdrop-filter: blur(6px) saturate(120%); */
  display: flex;
  flex-direction: column;
  transition: width 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  /* GPU加速 */
  transform: translateZ(0);
  contain: layout style paint;
  /* 移除will-change，减少内存占用 */
}

/* 树节点列表动画 - 性能优化 */
.tree-node-enter-active {
  transition: opacity 0.15s cubic-bezier(0.16, 1, 0.3, 1), transform 0.15s cubic-bezier(0.16, 1, 0.3, 1);
  /* 移除will-change */
}

.tree-node-leave-active {
  transition: opacity 0.12s cubic-bezier(0.4, 0, 1, 1), transform 0.12s cubic-bezier(0.4, 0, 1, 1);
  /* 移除will-change */
}

.tree-node-enter-from {
  opacity: 0;
  transform: translate3d(-12px, 0, 0);
}

.tree-node-leave-to {
  opacity: 0;
  transform: translate3d(-8px, 0, 0);
}

.tree-node-move {
  transition: transform 0.15s cubic-bezier(0.16, 1, 0.3, 1);
  /* 移除will-change */
  flex-shrink: 0;
  position: relative;
  overflow: hidden;
}

.document-tree::before {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: linear-gradient(180deg, 
    transparent 0%, 
    rgba(255, 255, 255, 0.1) 50%,
    rgba(255, 255, 255, 0.2) 100%);
  pointer-events: none;
  z-index: 0;
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
  position: relative;
  z-index: 1;
}

.tree-list {
  padding: 12px 8px;
  overflow-y: auto;
  overflow-x: hidden;
  height: 100%;
}

.tree-list::-webkit-scrollbar {
  width: 4px;
}

.tree-list::-webkit-scrollbar-track {
  background: transparent;
}

.tree-list::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 2px;
}

.tree-list::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.15);
}

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
