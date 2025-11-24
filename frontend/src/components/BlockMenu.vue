<template>
  <Teleport to="body">
    <div v-if="visible" class="block-menu" :style="menuStyle" @click.stop>
    <div class="block-menu-content">
        <!-- 搜索提示 -->
        <div v-if="searchQuery" class="search-hint">
          <span class="search-icon">🔍</span>
          <span>搜索: {{ searchQuery }}</span>
        </div>
        
        <!-- 建议部分 -->
        <div v-if="suggestions.length > 0" class="menu-section">
          <div class="section-title">建议</div>
          <div
            v-for="(item, index) in suggestions"
            :key="item.type"
            class="block-menu-item"
            :class="{ 
              active: selectedIndex === getItemIndex(item),
              hovered: hoveredItem === item.type
            }"
            :style="{ animationDelay: `${index * 20}ms` }"
            @click="selectItem(item)"
            @mouseenter="hoveredItem = item.type; selectedIndex = getItemIndex(item)"
            @mouseleave="hoveredItem = null"
          >
            <div class="item-icon-wrapper">
              <div class="item-icon">{{ item.icon }}</div>
            </div>
            <div class="item-info">
              <div class="item-title-row">
                <span class="item-title">{{ item.title }}</span>
                <span v-if="item.badge" class="item-badge">{{ item.badge }}</span>
              </div>
              <div v-if="item.description" class="item-description">{{ item.description }}</div>
            </div>
            <div v-if="item.shortcut" class="item-shortcut">{{ item.shortcut }}</div>
          </div>
        </div>

        <!-- 分隔线 -->
        <div v-if="suggestions.length > 0 && basicBlocks.length > 0" class="menu-divider"></div>

        <!-- 基本区块部分 -->
        <div v-if="basicBlocks.length > 0" class="menu-section">
          <div class="section-title">基本区块</div>
          <div
            v-for="(item, index) in basicBlocks"
            :key="item.type"
            class="block-menu-item"
            :class="{ 
              active: selectedIndex === getItemIndex(item),
              hovered: hoveredItem === item.type
            }"
            :style="{ animationDelay: `${(suggestions.length + index) * 20}ms` }"
            @click="selectItem(item)"
            @mouseenter="hoveredItem = item.type; selectedIndex = getItemIndex(item)"
            @mouseleave="hoveredItem = null"
          >
            <div class="item-icon-wrapper">
              <div class="item-icon">{{ item.icon }}</div>
            </div>
            <div class="item-info">
              <div class="item-title-row">
                <span class="item-title">{{ item.title }}</span>
              </div>
              <div v-if="item.description" class="item-description">{{ item.description }}</div>
            </div>
            <div v-if="item.shortcut" class="item-shortcut">{{ item.shortcut }}</div>
          </div>
        </div>

        <!-- 分隔线 -->
        <div v-if="basicBlocks.length > 0 && lists.length > 0" class="menu-divider"></div>

        <!-- 列表部分 -->
        <div v-if="lists.length > 0" class="menu-section">
          <div class="section-title">列表</div>
          <div
            v-for="(item, index) in lists"
            :key="item.type"
            class="block-menu-item"
            :class="{ 
              active: selectedIndex === getItemIndex(item),
              hovered: hoveredItem === item.type
            }"
            :style="{ animationDelay: `${(suggestions.length + basicBlocks.length + index) * 20}ms` }"
            @click="selectItem(item)"
            @mouseenter="hoveredItem = item.type; selectedIndex = getItemIndex(item)"
            @mouseleave="hoveredItem = null"
          >
            <div class="item-icon-wrapper">
              <div class="item-icon">{{ item.icon }}</div>
            </div>
            <div class="item-info">
              <div class="item-title-row">
                <span class="item-title">{{ item.title }}</span>
              </div>
              <div v-if="item.description" class="item-description">{{ item.description }}</div>
            </div>
            <div v-if="item.shortcut" class="item-shortcut">{{ item.shortcut }}</div>
          </div>
        </div>

        <!-- 分隔线 -->
        <div v-if="lists.length > 0 && media.length > 0" class="menu-divider"></div>

        <!-- 媒体部分 -->
        <div v-if="media.length > 0" class="menu-section">
          <div class="section-title">媒体</div>
          <div
            v-for="(item, index) in media"
            :key="item.type"
          class="block-menu-item"
            :class="{ 
              active: selectedIndex === getItemIndex(item),
              hovered: hoveredItem === item.type
            }"
            :style="{ animationDelay: `${(suggestions.length + basicBlocks.length + lists.length + index) * 20}ms` }"
          @click="selectItem(item)"
            @mouseenter="hoveredItem = item.type; selectedIndex = getItemIndex(item)"
          @mouseleave="hoveredItem = null"
        >
            <div class="item-icon-wrapper">
          <div class="item-icon">{{ item.icon }}</div>
            </div>
          <div class="item-info">
              <div class="item-title-row">
                <span class="item-title">{{ item.title }}</span>
              </div>
              <div v-if="item.description" class="item-description">{{ item.description }}</div>
            </div>
            <div v-if="item.shortcut" class="item-shortcut">{{ item.shortcut }}</div>
          </div>
        </div>

        <!-- 分隔线 -->
        <div v-if="media.length > 0 && advanced.length > 0" class="menu-divider"></div>

        <!-- 高级功能部分 -->
        <div v-if="advanced.length > 0" class="menu-section">
          <div class="section-title">高级</div>
          <div
            v-for="(item, index) in advanced"
            :key="item.type"
            class="block-menu-item"
            :class="{ 
              active: selectedIndex === getItemIndex(item),
              hovered: hoveredItem === item.type
            }"
            :style="{ animationDelay: `${(suggestions.length + basicBlocks.length + lists.length + media.length + index) * 20}ms` }"
            @click="selectItem(item)"
            @mouseenter="hoveredItem = item.type; selectedIndex = getItemIndex(item)"
            @mouseleave="hoveredItem = null"
          >
            <div class="item-icon-wrapper">
              <div class="item-icon">{{ item.icon }}</div>
            </div>
            <div class="item-info">
              <div class="item-title-row">
                <span class="item-title">{{ item.title }}</span>
              </div>
              <div v-if="item.description" class="item-description">{{ item.description }}</div>
          </div>
          <div v-if="item.shortcut" class="item-shortcut">{{ item.shortcut }}</div>
          </div>
        </div>

        <!-- 底部提示 -->
        <div class="menu-footer">
          <span class="footer-hint">在页面上输入"/"</span>
          <span class="footer-esc">esc</span>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  position: {
    type: Object,
    default: () => ({ top: 0, left: 0 })
  },
  searchQuery: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['select', 'close'])

const hoveredItem = ref(null)
const selectedIndex = ref(0)

// 建议部分
const suggestions = computed(() => {
  const items = [
    { 
      type: 'aiShorthand', 
      icon: '📝', 
      title: 'AI 速记', 
      description: '使用AI快速记录和整理',
      badge: '测试版',
      shortcut: null
    },
    { 
      type: 'aiSummary', 
      icon: '✨', 
      title: 'AI 总结', 
      description: '智能总结当前内容',
      badge: '新',
      shortcut: null
    }
  ]
  
  return filterItems(items)
})

// 基本区块部分
const basicBlocks = computed(() => {
  const items = [
    { type: 'text', icon: 'T', title: '文本', description: '普通文本段落', shortcut: null },
    { type: 'heading1', icon: 'H1', title: '标题 1', description: '一级标题', shortcut: '#' },
    { type: 'heading2', icon: 'H2', title: '标题 2', description: '二级标题', shortcut: '##' },
    { type: 'heading3', icon: 'H3', title: '标题 3', description: '三级标题', shortcut: '###' },
    { type: 'heading4', icon: 'H4', title: '标题 4', description: '四级标题', shortcut: '####' },
    { type: 'codeBlock', icon: '{}', title: '代码块', description: '代码片段', shortcut: '```' },
    { type: 'blockquote', icon: '"', title: '引用', description: '引用文本块', shortcut: '>' },
    { type: 'horizontalRule', icon: '—', title: '分割线', description: '水平分割线', shortcut: '---' }
  ]
  
  return filterItems(items)
})

// 列表部分
const lists = computed(() => {
  const items = [
    { type: 'bulletList', icon: '•', title: '项目符号列表', description: '无序列表', shortcut: '-' },
    { type: 'orderedList', icon: '1.', title: '有序列表', description: '编号列表', shortcut: '1.' },
    { type: 'taskList', icon: '☐', title: '任务列表', description: '待办事项列表', shortcut: '- [ ]' }
  ]
  
  return filterItems(items)
})

// 媒体部分
const media = computed(() => {
  const items = [
    { type: 'image', icon: '🖼️', title: '图片', description: '插入图片', shortcut: null },
    { type: 'video', icon: '🎥', title: '视频', description: '插入视频', shortcut: null },
    { type: 'link', icon: '🔗', title: '链接', description: '插入链接', shortcut: 'Ctrl+K' },
    { type: 'documentLink', icon: '📄', title: '文档链接', description: '链接到其他文档', shortcut: null }
  ]
  
  return filterItems(items)
})

// 高级功能部分
const advanced = computed(() => {
  const items = [
    { type: 'table', icon: '⊞', title: '表格', description: '插入表格', shortcut: null },
    { type: 'callout', icon: '💡', title: '提示框', description: '重要提示信息', shortcut: null },
    { type: 'comment', icon: '💬', title: '评论', description: '添加评论', shortcut: null }
  ]
  
  return filterItems(items)
})

// 过滤函数 - 改进搜索逻辑
const filterItems = (items) => {
  if (!props.searchQuery || props.searchQuery.trim() === '') {
    return items
  }
  
  const query = props.searchQuery.toLowerCase().trim()
  
  return items.filter(item => {
    // 搜索标题
    if (item.title.toLowerCase().includes(query)) return true
    // 搜索类型
    if (item.type.toLowerCase().includes(query)) return true
    // 搜索描述
    if (item.description && item.description.toLowerCase().includes(query)) return true
    // 搜索快捷键
    if (item.shortcut && item.shortcut.toLowerCase().includes(query)) return true
    // 搜索别名（中文拼音首字母等）
    const aliases = getAliases(item)
    if (aliases.some(alias => alias.toLowerCase().includes(query))) return true
    
    return false
  })
}

// 获取搜索别名
const getAliases = (item) => {
  const aliasMap = {
    'heading1': ['标题1', 'h1', 'bt1'],
    'heading2': ['标题2', 'h2', 'bt2'],
    'heading3': ['标题3', 'h3', 'bt3'],
    'heading4': ['标题4', 'h4', 'bt4'],
    'bulletList': ['列表', 'liebiao', 'lb', 'ul'],
    'orderedList': ['有序', 'youxu', 'yx', 'ol'],
    'taskList': ['任务', 'renwu', 'rw', 'todo'],
    'image': ['图片', 'tupian', 'tp', 'img'],
    'link': ['链接', 'lianjie', 'lj'],
    'table': ['表格', 'biaoge', 'bg'],
    'codeBlock': ['代码块', 'daimakuai', 'dmk']
  }
  
  return aliasMap[item.type] || []
}

// 所有菜单项（扁平化，用于键盘导航）
const allItems = computed(() => {
  return [
    ...suggestions.value,
    ...basicBlocks.value,
    ...lists.value,
    ...media.value,
    ...advanced.value
  ]
})

// 获取项目在扁平列表中的索引
const getItemIndex = (item) => {
  let index = 0
  
  // 检查建议
  const suggestionIndex = suggestions.value.findIndex(i => i.type === item.type)
  if (suggestionIndex !== -1) return index + suggestionIndex
  index += suggestions.value.length
  
  // 检查基本区块
  const basicIndex = basicBlocks.value.findIndex(i => i.type === item.type)
  if (basicIndex !== -1) return index + basicIndex
  index += basicBlocks.value.length
  
  // 检查列表
  const listIndex = lists.value.findIndex(i => i.type === item.type)
  if (listIndex !== -1) return index + listIndex
  index += lists.value.length
  
  // 检查媒体
  const mediaIndex = media.value.findIndex(i => i.type === item.type)
  if (mediaIndex !== -1) return index + mediaIndex
  index += media.value.length
  
  // 检查高级
  const advancedIndex = advanced.value.findIndex(i => i.type === item.type)
  if (advancedIndex !== -1) return index + advancedIndex
  
  return 0
}

// 计算菜单位置，确保不超出视口
const menuStyle = computed(() => {
  const { top, left } = props.position
  const menuWidth = 360
  const menuHeight = 500
  const padding = 16
  
  let finalTop = top
  let finalLeft = left
  
  // 检查右边界
  if (left + menuWidth > window.innerWidth - padding) {
    finalLeft = window.innerWidth - menuWidth - padding
  }
  
  // 检查左边界
  if (finalLeft < padding) {
    finalLeft = padding
  }
  
  // 检查下边界
  if (top + menuHeight > window.innerHeight - padding) {
    finalTop = Math.max(padding, window.innerHeight - menuHeight - padding)
  }
  
  // 检查上边界
  if (finalTop < padding) {
    finalTop = padding
  }
  
  return {
    top: `${finalTop}px`,
    left: `${finalLeft}px`
  }
})

// 选择项目
const selectItem = (item) => {
  emit('select', item.type)
}

// 键盘导航
const handleKeyDown = (event) => {
  if (!props.visible) return
  
  const items = allItems.value
  
  // 如果菜单为空，不处理导航
  if (items.length === 0) {
    if (event.key === 'Escape') {
      event.preventDefault()
      emit('close')
    }
    return
  }
  
  switch (event.key) {
    case 'ArrowDown':
      event.preventDefault()
      event.stopPropagation()
      selectedIndex.value = (selectedIndex.value + 1) % items.length
      scrollToSelected()
      break
    case 'ArrowUp':
      event.preventDefault()
      event.stopPropagation()
      selectedIndex.value = selectedIndex.value <= 0 ? items.length - 1 : selectedIndex.value - 1
      scrollToSelected()
      break
    case 'Enter':
      event.preventDefault()
      event.stopPropagation()
      if (items[selectedIndex.value]) {
        selectItem(items[selectedIndex.value])
      }
      break
    case 'Escape':
      event.preventDefault()
      event.stopPropagation()
      emit('close')
      break
    default:
      // 其他按键不处理，让编辑器处理（用于搜索输入）
      break
  }
}

// 滚动到选中项
const scrollToSelected = () => {
  nextTick(() => {
    const menu = document.querySelector('.block-menu-content')
    const activeItem = menu?.querySelector('.block-menu-item.active')
    if (activeItem && menu) {
      activeItem.scrollIntoView({ block: 'nearest', behavior: 'smooth' })
    }
  })
}

// 监听可见性变化，重置选中索引
watch(() => props.visible, (newVal) => {
  if (newVal) {
    selectedIndex.value = 0
    hoveredItem.value = null
    nextTick(() => {
      document.addEventListener('keydown', handleKeyDown)
    })
  } else {
    document.removeEventListener('keydown', handleKeyDown)
  }
})

// 监听搜索查询变化，重置选中索引
watch(() => props.searchQuery, () => {
  if (props.visible) {
    selectedIndex.value = 0
  }
})

// 点击外部关闭
const handleClickOutside = (event) => {
  if (props.visible) {
    const menu = event.target.closest('.block-menu')
    if (!menu) {
      emit('close')
    }
  }
}

onMounted(() => {
  if (props.visible) {
    document.addEventListener('keydown', handleKeyDown)
  }
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeyDown)
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.block-menu {
  position: fixed;
  z-index: 10000;
  background: rgba(245, 245, 247, 0.95);
  /* 性能优化：降低blur值或移除backdrop-filter */
  backdrop-filter: blur(8px) saturate(120%);
  -webkit-backdrop-filter: blur(8px) saturate(120%);
  border-radius: 12px;
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.12),
    0 0 0 1px rgba(255, 255, 255, 0.5) inset,
    0 0 0 1px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  width: 360px;
  max-height: 500px;
  animation: menuSlideIn 0.15s cubic-bezier(0.16, 1, 0.3, 1);
  transform: translateZ(0);
  /* 移除will-change，减少内存占用 */
}

@keyframes menuSlideIn {
  from {
    opacity: 0;
    transform: translateY(-8px) translateZ(0);
  }
  to {
    opacity: 1;
    transform: translateY(0) translateZ(0);
  }
}

.block-menu-content {
  display: flex;
  flex-direction: column;
  max-height: 500px;
  overflow-y: auto;
  overflow-x: hidden;
}

/* 自定义滚动条 */
.block-menu-content::-webkit-scrollbar {
  width: 8px;
}

.block-menu-content::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.02);
  border-radius: 4px;
}

.block-menu-content::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.15);
  border-radius: 4px;
  transition: background 0.2s;
}

.block-menu-content::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.25);
}

.search-hint {
  padding: 10px 16px;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.6);
  background: rgba(255, 255, 255, 0.3);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  gap: 8px;
  animation: searchHintFadeIn 0.2s ease;
}

.search-icon {
  font-size: 14px;
}

@keyframes searchHintFadeIn {
  from {
    opacity: 0;
    transform: translateY(-4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.menu-section {
  padding: 8px 0;
}

.section-title {
  padding: 10px 16px 6px;
  font-size: 11px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.5);
  text-transform: uppercase;
  letter-spacing: 0.8px;
  animation: sectionTitleFadeIn 0.3s ease;
}

@keyframes sectionTitleFadeIn {
  from {
    opacity: 0;
    transform: translateX(-8px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.menu-divider {
  height: 1px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    rgba(0, 0, 0, 0.08) 20%, 
    rgba(0, 0, 0, 0.08) 80%, 
    transparent 100%
  );
  margin: 6px 16px;
  animation: dividerFadeIn 0.3s ease;
}

@keyframes dividerFadeIn {
  from {
    opacity: 0;
    transform: scaleX(0);
  }
  to {
    opacity: 1;
    transform: scaleX(1);
  }
}

.block-menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  cursor: pointer;
  /* 性能优化：只使用transform和opacity */
  transition: transform 0.15s cubic-bezier(0.4, 0, 0.2, 1), opacity 0.15s ease;
  position: relative;
  user-select: none;
  min-height: 48px;
  animation: itemSlideIn 0.2s cubic-bezier(0.16, 1, 0.3, 1) both;
  border-radius: 8px;
  margin: 2px 8px;
}

@keyframes itemSlideIn {
  from {
    opacity: 0;
    transform: translateX(-12px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.block-menu-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, 
    rgba(22, 93, 255, 0.08) 0%, 
    transparent 100%
  );
  opacity: 0;
  transition: opacity 0.2s ease;
  border-radius: 8px;
  pointer-events: none;
}

.block-menu-item:hover::before,
.block-menu-item.hovered::before,
.block-menu-item.active::before {
  opacity: 1;
}

.block-menu-item:hover,
.block-menu-item.hovered,
.block-menu-item.active {
  background: rgba(255, 255, 255, 0.4);
  transform: translateX(4px);
  /* 性能优化：移除box-shadow动画 */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.block-menu-item:active {
  transform: translateX(2px) scale(0.98);
  background: rgba(255, 255, 255, 0.5);
}

.item-icon-wrapper {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 8px;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.block-menu-item:hover .item-icon-wrapper,
.block-menu-item.active .item-icon-wrapper {
  background: rgba(255, 255, 255, 0.8);
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.item-icon {
  font-size: 16px;
  font-weight: 500;
  color: rgba(0, 0, 0, 0.7);
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s ease;
}

.block-menu-item:hover .item-icon,
.block-menu-item.active .item-icon {
  transform: scale(1.1);
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 2px;
}

.item-title {
  font-size: 14px;
  font-weight: 500;
  color: rgba(0, 0, 0, 0.85);
  line-height: 1.4;
  transition: color 0.2s ease;
}

.block-menu-item:hover .item-title,
.block-menu-item.active .item-title {
  color: rgba(0, 0, 0, 0.95);
}

.item-description {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.5);
  line-height: 1.3;
  margin-top: 2px;
}

.item-badge {
  font-size: 10px;
  font-weight: 600;
  color: #007aff;
  background: rgba(0, 122, 255, 0.15);
  padding: 2px 8px;
  border-radius: 10px;
  line-height: 1.4;
  white-space: nowrap;
  animation: badgePulse 2s ease-in-out infinite;
}

@keyframes badgePulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

.item-shortcut {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.4);
  font-family: -apple-system, BlinkMacSystemFont, 'SF Mono', 'Monaco', 'Courier New', monospace;
  flex-shrink: 0;
  font-weight: 400;
  background: rgba(0, 0, 0, 0.04);
  padding: 4px 8px;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.block-menu-item:hover .item-shortcut,
.block-menu-item.active .item-shortcut {
  color: rgba(0, 0, 0, 0.6);
  background: rgba(0, 0, 0, 0.08);
}

.menu-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  background: rgba(255, 255, 255, 0.3);
  font-size: 12px;
  color: rgba(0, 0, 0, 0.5);
  animation: footerFadeIn 0.3s ease;
}

@keyframes footerFadeIn {
  from {
    opacity: 0;
    transform: translateY(4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.footer-hint {
  flex: 1;
  color: rgba(0, 0, 0, 0.5);
}

.footer-esc {
  font-family: -apple-system, BlinkMacSystemFont, 'SF Mono', 'Monaco', 'Courier New', monospace;
  color: rgba(0, 0, 0, 0.4);
  font-size: 11px;
  background: rgba(0, 0, 0, 0.05);
  padding: 2px 6px;
  border-radius: 4px;
}
</style>
