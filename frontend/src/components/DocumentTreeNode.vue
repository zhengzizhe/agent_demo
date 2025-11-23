<template>
  <div class="tree-node" :class="{ selected: selected, 'is-folder': node.isFolder, 'is-expanded': isExpanded && node.isFolder }">
    <div 
      class="tree-node-content"
      @click="handleClick"
      :style="indentStyle"
    >
      <!-- 树形连接线 -->
      <div class="tree-lines">
        <div 
          v-for="(line, idx) in lines" 
          :key="idx"
          class="tree-line"
          :class="line.type"
        ></div>
      </div>
      
      <!-- 展开/收起按钮 -->
      <button 
        v-if="node.isFolder"
        class="tree-node-toggle"
        :class="{ 'has-children': hasChildren, 'is-expanded': isExpanded }"
        @click.stop="handleToggle"
        :disabled="!hasChildren"
      >
        <svg 
          width="14" 
          height="14" 
          viewBox="0 0 14 14" 
          fill="none"
          class="toggle-icon"
        >
          <path 
            d="M5.25 3.5l3.5 3.5-3.5 3.5" 
            stroke="currentColor" 
            stroke-width="1.5" 
            stroke-linecap="round" 
            stroke-linejoin="round"
          />
        </svg>
      </button>
      <div v-else class="tree-node-spacer"></div>
      
      <!-- 图标 -->
      <div class="tree-node-icon" :class="{ 'is-folder': node.isFolder }">
        <svg v-if="node.isFolder" width="16" height="16" viewBox="0 0 16 16" fill="none">
          <path 
            d="M2 4C2 3.44772 2.44772 3 3 3H6.5L8 5H13C13.5523 5 14 5.44772 14 6V12C14 12.5523 13.5523 13 13 13H3C2.44772 13 2 12.5523 2 12V4Z" 
            :fill="selected ? 'var(--theme-accent, #165dff)' : '#f59e0b'"
            :stroke="selected ? 'var(--theme-accent, #165dff)' : '#d97706'"
            stroke-width="1"
          />
          <path 
            d="M2 4H8L6.5 6H2V4Z" 
            :fill="selected ? 'rgba(255,255,255,0.25)' : 'rgba(255,255,255,0.35)'"
          />
        </svg>
        <svg v-else width="16" height="16" viewBox="0 0 16 16" fill="none">
          <rect 
            x="3" 
            y="3" 
            width="10" 
            height="10" 
            rx="1.5" 
            :fill="selected ? 'var(--theme-accent, #165dff)' : '#6b7280'"
            :stroke="selected ? 'var(--theme-accent, #165dff)' : '#4b5563'"
            stroke-width="1"
          />
          <path 
            d="M6 6.5h4M6 9h4M6 11.5h3" 
            stroke="white" 
            stroke-width="1" 
            stroke-linecap="round"
            opacity="0.95"
          />
        </svg>
      </div>
      
      <!-- 名称 -->
      <span 
        class="tree-node-name" 
        :title="node.name || '未命名'"
      >
        {{ node.name || '未命名' }}
      </span>
      
      <!-- 子项数量 -->
      <span v-if="node.isFolder && hasChildren" class="tree-node-count">
        {{ node.children.length }}
      </span>
    </div>
    
    <!-- 子节点 -->
    <transition name="tree-expand">
      <div v-if="node.isFolder && isExpanded && hasChildren" class="tree-node-children">
        <DocumentTreeNode
          v-for="(child, index) in node.children"
          :key="child.id"
          :node="child"
          :selected-id="selectedId"
          :expanded-ids="expandedIds"
          :parent-lines="computedParentLines"
          :is-last="index === node.children.length - 1"
          @select="$emit('select', $event)"
          @toggle="$emit('toggle', $event)"
        />
      </div>
    </transition>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  node: {
    type: Object,
    required: true
  },
  selectedId: {
    type: String,
    default: null
  },
  expandedIds: {
    type: Array,
    default: () => []
  },
  parentLines: {
    type: Array,
    default: () => []
  },
  isLast: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['select', 'toggle'])

const selected = computed(() => props.selectedId === props.node.id)
const hasChildren = computed(() => props.node.children && props.node.children.length > 0)
const isExpanded = computed(() => props.expandedIds.includes(props.node.id))

// 计算缩进样式
const indentStyle = computed(() => {
  const level = props.node.level || 0
  return {
    paddingLeft: `${level * 20 + 12}px`
  }
})

// 计算树形连接线
const lines = computed(() => {
  const level = props.node.level || 0
  const result = []
  
  // 添加父级连接线
  props.parentLines.forEach((line, idx) => {
    result.push({
      type: line === 'vertical' ? 'vertical' : 'empty'
    })
  })
  
  // 添加当前级别的连接线
  if (level > 0) {
    if (props.isLast) {
      result.push({ type: 'corner-last' })
    } else {
      result.push({ type: 'corner' })
    }
  }
  
  return result
})

// 传递给子节点的连接线
const computedParentLines = computed(() => {
  const level = props.node.level || 0
  const result = [...props.parentLines]
  
  if (level > 0) {
    if (props.isLast) {
      result.push('empty')
    } else {
      result.push('vertical')
    }
  }
  
  return result
})

const handleClick = (event) => {
  // 如果点击的是展开按钮区域，不触发选择
  if (event.target.closest('.tree-node-toggle')) {
    return
  }
  emit('select', props.node)
}

const handleToggle = () => {
  if (hasChildren.value) {
    emit('toggle', props.node.id)
  }
}
</script>

<style scoped>
.tree-node {
  user-select: none;
  position: relative;
}

.tree-node-content {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 12px;
  margin: 1px 0;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  min-height: 28px;
  position: relative;
}

.tree-node-content::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%) scaleY(0);
  width: 2.5px;
  height: 0;
  background: var(--theme-accent, #165dff);
  border-radius: 0 2px 2px 0;
  opacity: 0;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.tree-node-content:hover {
  background: rgba(255, 255, 255, 0.35);
  transform: translateX(1px);
}

.tree-node-content:hover::before {
  opacity: 1;
  height: 60%;
  transform: translateY(-50%) scaleY(1);
}

.tree-node.selected .tree-node-content {
  background: rgba(22, 93, 255, 0.12);
  color: var(--theme-accent, #165dff);
}

.tree-node.selected .tree-node-content::before {
  opacity: 1;
  height: 70%;
  transform: translateY(-50%) scaleY(1);
}

/* 树形连接线 */
.tree-lines {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  pointer-events: none;
}

.tree-line {
  width: 20px;
  height: 100%;
  position: relative;
}

.tree-line.vertical::after {
  content: '';
  position: absolute;
  left: 9px;
  top: 0;
  bottom: 0;
  width: 0.5px;
  background: rgba(0, 0, 0, 0.08);
}

.tree-line.corner::after {
  content: '';
  position: absolute;
  left: 9px;
  top: 0;
  bottom: 0;
  width: 0.5px;
  background: rgba(0, 0, 0, 0.08);
}

.tree-line.corner::before {
  content: '';
  position: absolute;
  left: 9px;
  top: 14px;
  width: 11px;
  height: 0.5px;
  background: rgba(0, 0, 0, 0.08);
}

.tree-line.corner-last::before {
  content: '';
  position: absolute;
  left: 9px;
  top: 14px;
  width: 11px;
  height: 0.5px;
  background: rgba(0, 0, 0, 0.08);
}

.tree-line.empty {
  /* 空连接线，不显示 */
}

/* 展开/收起按钮 */
.tree-node-toggle {
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: #9ca3af;
  cursor: pointer;
  border-radius: 3px;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
  position: relative;
  z-index: 1;
  margin-right: 2px;
}

.tree-node-toggle:not(.has-children) {
  opacity: 0.2;
  cursor: default;
}

.tree-node-toggle.has-children:hover {
  background: rgba(255, 255, 255, 0.6);
  color: #4b5563;
  transform: scale(1.08);
}

.tree-node-toggle.has-children:active {
  transform: scale(0.92);
}

.tree-node-toggle.is-expanded .toggle-icon {
  transform: rotate(90deg);
}

.toggle-icon {
  transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  transform-origin: center;
}

.tree-node-spacer {
  width: 18px;
  flex-shrink: 0;
  margin-right: 2px;
}

/* 图标 */
.tree-node-icon {
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 1;
  opacity: 0.85;
}

.tree-node-content:hover .tree-node-icon {
  transform: scale(1.05);
  opacity: 1;
}

.tree-node.selected .tree-node-icon {
  transform: scale(1.02);
  opacity: 1;
}

/* 名称 */
.tree-node-name {
  flex: 1;
  font-size: 13px;
  font-weight: 400;
  color: #374151;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 1;
  line-height: 1.4;
}

.tree-node.selected .tree-node-name {
  font-weight: 500;
  color: var(--theme-accent, #165dff);
}

.tree-node-content:hover .tree-node-name {
  color: #111827;
}

/* 子项数量 */
.tree-node-count {
  font-size: 10px;
  font-weight: 500;
  color: #6b7280;
  background: rgba(255, 255, 255, 0.45);
  padding: 2px 6px;
  border-radius: 8px;
  flex-shrink: 0;
  min-width: 18px;
  text-align: center;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 1;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  line-height: 1.3;
}

.tree-node.selected .tree-node-count {
  background: rgba(22, 93, 255, 0.18);
  color: var(--theme-accent, #165dff);
}

.tree-node-content:hover .tree-node-count {
  background: rgba(255, 255, 255, 0.65);
}

/* 子节点容器 */
.tree-node-children {
  margin-left: 0;
  position: relative;
}

/* 展开动画 - 使用更平滑的动画 */
.tree-expand-enter-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.tree-expand-leave-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.tree-expand-enter-from {
  opacity: 0;
  max-height: 0;
  transform: translateY(-6px);
}

.tree-expand-leave-to {
  opacity: 0;
  max-height: 0;
  transform: translateY(-3px);
}

.tree-expand-enter-to,
.tree-expand-leave-from {
  opacity: 1;
  max-height: 2000px;
  transform: translateY(0);
}
</style>

