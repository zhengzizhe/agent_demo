<template>
  <div class="tree-node-wrapper">
    <!-- 节点内容 -->
    <div 
      class="tree-node-custom" 
      :class="{ selected: isSelected, 'has-children': hasChildren }"
      :style="{ paddingLeft: `${16 + depth * 24}px` }"
      @click="handleClick"
      @dblclick="handleDoubleClick"
    >
      <!-- 左侧高光条（选中时显示） -->
      <div class="tree-node-highlight"></div>
      
      <!-- iOS风轻线性箭头（有子节点时显示） -->
      <button 
        v-if="hasChildren"
        class="tree-node-toggle"
        :class="{ expanded: isExpanded }"
        @click.stop="handleToggle"
      >
        <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
          <path d="M4 3l4 3-4 3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
      <span v-else class="tree-node-toggle-placeholder"></span>
      
      <!-- 彩色拟态图标 -->
      <div class="tree-node-icon-wrapper">
        <!-- 文件夹图标 - 渐变蓝绿 -->
        <svg v-if="node.isFolder" class="icon-folder" width="20" height="20" viewBox="0 0 20 20" fill="none">
          <defs>
            <linearGradient id="folderGradient" x1="3" y1="4" x2="17" y2="16">
              <stop offset="0%" stop-color="#10b981" stop-opacity="0.9"/>
              <stop offset="50%" stop-color="#14b8a6" stop-opacity="0.85"/>
              <stop offset="100%" stop-color="#06b6d4" stop-opacity="0.8"/>
            </linearGradient>
            <linearGradient id="folderGradientStroke" x1="3" y1="4" x2="17" y2="16">
              <stop offset="0%" stop-color="#059669"/>
              <stop offset="100%" stop-color="#0891b2"/>
            </linearGradient>
            <linearGradient id="folderGradientLight" x1="3" y1="5" x2="9" y2="7">
              <stop offset="0%" stop-color="#ffffff" stop-opacity="0.5"/>
              <stop offset="100%" stop-color="#ffffff" stop-opacity="0.2"/>
            </linearGradient>
          </defs>
          <path d="M3 5C3 4.44772 3.44772 4 4 4H7.5L9 6H16C16.5523 6 17 6.44772 17 7V15C17 15.5523 16.5523 16 16 16H4C3.44772 16 3 15.5523 3 15V5Z" 
                fill="url(#folderGradient)" 
                stroke="url(#folderGradientStroke)" 
                stroke-width="1.2"/>
          <path d="M3 5H9L7.5 7H3V5Z" fill="url(#folderGradientLight)"/>
          <!-- 有子文件夹时显示小点 -->
          <circle v-if="hasChildren" cx="15" cy="7" r="3" fill="#f59e0b" opacity="0.9">
            <animate attributeName="opacity" values="0.9;0.6;0.9" dur="2s" repeatCount="indefinite"/>
          </circle>
        </svg>
        
        <!-- 文档图标 - 蓝紫渐变 -->
        <svg v-else class="icon-document" width="20" height="20" viewBox="0 0 20 20" fill="none">
          <defs>
            <linearGradient id="docGradient" x1="3" y1="3" x2="17" y2="17">
              <stop offset="0%" stop-color="#6366f1" stop-opacity="0.9"/>
              <stop offset="100%" stop-color="#8b5cf6" stop-opacity="0.85"/>
            </linearGradient>
            <linearGradient id="docGradientStroke" x1="3" y1="3" x2="17" y2="17">
              <stop offset="0%" stop-color="#4f46e5"/>
              <stop offset="100%" stop-color="#7c3aed"/>
            </linearGradient>
          </defs>
          <rect x="4" y="4" width="12" height="12" rx="2" 
                fill="url(#docGradient)" 
                stroke="url(#docGradientStroke)" 
                stroke-width="1.2"/>
          <path d="M7 8h6M7 11h6M7 14h4" stroke="white" stroke-width="1.2" stroke-linecap="round" opacity="0.95"/>
        </svg>
      </div>
      
      <span class="tree-node-name">{{ node.name || '未命名' }}</span>
      
      <!-- 状态徽章 -->
      <div class="tree-node-badges">
        <!-- 新文档 - 蓝点 -->
        <span v-if="node.data?.isNew" class="status-badge new" title="新文档">
          <span class="status-dot blue"></span>
        </span>
        <!-- 未读更新 - 紫点 -->
        <span v-if="node.data?.hasUpdate" class="status-badge updated" title="有更新">
          <span class="status-dot purple"></span>
        </span>
        <!-- 收藏 - 橙色星标 -->
        <span v-if="node.data?.favorite" class="status-badge favorite" title="已收藏">
          <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
            <path d="M6 1.5l1.5 3 3.5.5-2.5 2.5.5 3.5L6 9.5 3 10.5l.5-3.5L1 4.5l3.5-.5L6 1.5z" fill="currentColor"/>
          </svg>
        </span>
        <!-- 分享 - 青色👥 -->
        <span v-if="node.data?.shared" class="status-badge shared" title="已共享">
          <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
            <path d="M6 1L1 3v4l5 2 5-2V3L6 1z" stroke="currentColor" stroke-width="1.2" fill="none"/>
            <circle cx="6" cy="6" r="1.5" fill="currentColor"/>
          </svg>
        </span>
        <!-- 锁定 - 🔒 -->
        <span v-if="node.data?.locked" class="status-badge locked" title="已锁定">
          <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
            <rect x="3" y="5" width="6" height="5" rx="1" fill="currentColor"/>
            <path d="M4 5V3a2 2 0 012-2h0a2 2 0 012 2v2" stroke="currentColor" stroke-width="1.2" fill="none"/>
          </svg>
        </span>
      </div>
    </div>
    
    <!-- 子节点（展开时显示） -->
    <transition name="tree-expand">
      <div v-if="isExpanded && hasChildren" class="tree-node-children">
        <TreeNode
          v-for="child in node.children"
          :key="child.id"
          :node="child"
          :selected-id="selectedId"
          :expanded-ids="expandedIds"
          :depth="depth + 1"
          @select="handleChildSelect"
          @toggle="handleChildToggle"
          @open="handleChildOpen"
        />
      </div>
    </transition>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import TreeNode from './TreeNode.vue'

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
  depth: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['select', 'toggle', 'open'])

const isSelected = computed(() => props.selectedId === props.node.id)
const isExpanded = computed(() => props.expandedIds.includes(props.node.id))
const hasChildren = computed(() => props.node.children && props.node.children.length > 0)

const handleClick = () => {
  // 文件夹：单击打开/选中
  // 文档：单击只选中，不打开
  if (props.node.isFolder) {
    emit('select', props.node)
  } else {
    // 文档单击只选中，不触发打开
    emit('select', props.node)
  }
}

const handleDoubleClick = () => {
  // 文档：双击打开
  if (!props.node.isFolder) {
    emit('select', props.node)
    // 触发打开事件
    emit('open', props.node)
  }
}

const handleToggle = () => {
  emit('toggle', props.node.id)
}

const handleChildSelect = (node) => {
  emit('select', node)
}

const handleChildToggle = (nodeId) => {
  emit('toggle', nodeId)
}

const handleChildOpen = (node) => {
  emit('open', node)
}
</script>

<style scoped>
.tree-node-wrapper {
  position: relative;
}

.tree-node-custom {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  margin: 1px 0;
  border-radius: 10px;
  cursor: pointer;
  transition: transform 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  min-height: 40px;
  position: relative;
  background: transparent;
  border: 1px solid transparent;
  width: 100%;
  box-sizing: border-box;
  /* GPU加速 */
  transform: translateZ(0);
}

/* 左侧高光条（选中时显示） */
.tree-node-highlight {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: linear-gradient(180deg, #6366f1 0%, #8b5cf6 100%);
  border-radius: 0 2px 2px 0;
  opacity: 0;
  transition: opacity 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  z-index: 1;
}

/* 玻璃拟态背景层 */
.tree-node-custom::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0);
  backdrop-filter: blur(0px);
  -webkit-backdrop-filter: blur(0px);
  transition: opacity 0.2s cubic-bezier(0.16, 1, 0.3, 1), backdrop-filter 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  z-index: 0;
  opacity: 0;
}

/* 四周光晕（选中时） */
.tree-node-custom::after {
  content: '';
  position: absolute;
  inset: -2px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0);
  opacity: 0;
  transition: opacity 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  z-index: -1;
  filter: blur(2px);
}

/* Hover: 白色10-15%透明 */
.tree-node-custom:hover {
  background: transparent;
  transform: translate3d(2px, 0, 0);
}

.tree-node-custom:hover::before {
  background: rgba(255, 255, 255, 0.12);
  /* 90fps性能优化：大幅降低blur值 */
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  opacity: 1;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

/* Active: 白色20%透明玻璃 + 四周光晕 + 左侧高光 */
.tree-node-custom.selected {
  background: transparent;
  transform: translate3d(3px, 0, 0);
  font-weight: 500;
}

.tree-node-custom.selected::before {
  background: rgba(255, 255, 255, 0.2);
  /* 90fps性能优化：大幅降低blur值 */
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
  opacity: 1;
  box-shadow: 
    0 2px 8px rgba(0, 0, 0, 0.06),
    0 0 0 1px rgba(255, 255, 255, 0.3) inset;
}

.tree-node-custom.selected::after {
  background: rgba(255, 255, 255, 0.3);
  opacity: 1;
}

.tree-node-custom.selected .tree-node-highlight {
  opacity: 1;
}

.tree-node-custom.selected .tree-node-name {
  color: #1f2937;
  font-weight: 500;
}

/* iOS风轻线性箭头 */
.tree-node-toggle {
  width: 20px;
  height: 20px;
  min-width: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  transition: all 0.16s ease;
  cursor: pointer;
  background: transparent;
  border: none;
  padding: 0;
  margin: 0;
  position: relative;
  z-index: 2;
  flex-shrink: 0;
}

.tree-node-toggle svg {
  width: 12px;
  height: 12px;
  color: #9ca3af;
  transition: all 0.16s ease;
  transform: rotate(-90deg);
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.1));
}

.tree-node-toggle:hover {
  background: rgba(22, 93, 255, 0.08);
  transform: scale(1.1);
}

.tree-node-toggle:hover svg {
  color: #165dff;
  filter: drop-shadow(0 0 4px rgba(22, 93, 255, 0.3));
}

.tree-node-toggle.expanded svg {
  transform: rotate(0deg);
  color: #165dff;
  filter: drop-shadow(0 0 6px rgba(22, 93, 255, 0.4));
}

.tree-node-toggle-placeholder {
  width: 20px;
  min-width: 20px;
  display: block;
  flex-shrink: 0;
}

/* 图标容器 */
.tree-node-icon-wrapper {
  position: relative;
  z-index: 2;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
}

.icon-folder,
.icon-document {
  width: 20px;
  height: 20px;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

.tree-node-name {
  flex: 1;
  font-size: 14px;
  font-weight: 400;
  color: #374151;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.5;
  letter-spacing: -0.01em;
  position: relative;
  z-index: 2;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

/* 状态徽章 */
.tree-node-badges {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
  margin-left: auto;
  position: relative;
  z-index: 2;
}

.status-badge {
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
  flex-shrink: 0;
}

.status-badge svg {
  width: 12px;
  height: 12px;
}

.status-badge .status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.status-badge .status-dot.blue {
  background: #165dff;
  box-shadow: 0 0 8px rgba(22, 93, 255, 0.6);
  animation: pulse 2s ease-in-out infinite;
}

.status-badge .status-dot.purple {
  background: #a855f7;
  box-shadow: 0 0 8px rgba(168, 85, 247, 0.6);
  animation: pulse 2s ease-in-out infinite;
}

.status-badge.favorite {
  color: #f59e0b;
  background: rgba(245, 158, 11, 0.12);
}

.status-badge.favorite:hover {
  background: rgba(245, 158, 11, 0.2);
  transform: scale(1.15);
}

.status-badge.shared {
  color: #06b6d4;
  background: rgba(6, 182, 212, 0.12);
}

.status-badge.shared:hover {
  background: rgba(6, 182, 212, 0.2);
  transform: scale(1.15);
}

.status-badge.locked {
  color: #6b7280;
  background: rgba(107, 114, 128, 0.1);
}

.status-badge.locked:hover {
  background: rgba(107, 114, 128, 0.15);
  transform: scale(1.15);
}

.status-badge.new,
.status-badge.updated {
  background: transparent;
}

/* 子节点容器 */
.tree-node-children {
  margin-left: 0;
  padding-left: 0;
}

/* 展开动画 */
.tree-expand-enter-active,
.tree-expand-leave-active {
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  overflow: hidden;
}

.tree-expand-enter-from {
  opacity: 0;
  max-height: 0;
}

.tree-expand-enter-to {
  opacity: 1;
  max-height: 1000px;
}

.tree-expand-leave-from {
  opacity: 1;
  max-height: 1000px;
}

.tree-expand-leave-to {
  opacity: 0;
  max-height: 0;
}

/* 脉冲动画 */
@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(1.1);
  }
}
</style>

