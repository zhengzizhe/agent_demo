<template>
  <div class="knowledge-graph-container">
    <div class="kg-header">
      <h3 class="kg-title">知识图谱</h3>
      <div class="kg-controls">
        <button @click="resetView" class="kg-reset-btn">重置视图</button>
      </div>
    </div>
    
    <div v-if="loading" class="kg-loading">
      <div class="loading-spinner"></div>
      <p>正在加载知识图谱...</p>
    </div>
    
    <div v-else-if="nodes.length === 0" class="kg-empty">
      <svg width="64" height="64" viewBox="0 0 64 64" fill="none">
        <circle cx="32" cy="32" r="30" stroke="currentColor" stroke-width="2" fill="none" opacity="0.3"/>
        <path d="M32 16v16M32 32v16M20 32h16M32 32h16" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.5"/>
      </svg>
      <p>暂无数据</p>
    </div>
    
    <div v-else ref="graphContainer" class="kg-canvas"></div>
    
    <!-- 节点详情面板（仅显示文档节点） -->
    <div v-if="selectedNode && selectedNode.nodeType === 'document'" class="kg-detail-panel" @click.stop>
      <div class="detail-header">
        <h4>{{ selectedNode.docName || selectedNode.label }}</h4>
        <button @click="selectedNode = null; selectedDoc = null; entityCardData = null" class="close-btn">×</button>
      </div>
      <div class="detail-content">
        <div class="detail-item">
          <span class="detail-label">文档ID:</span>
          <span class="detail-value">{{ selectedNode.docId || selectedNode.id }}</span>
        </div>
        <div v-if="selectedNode.docOwner" class="detail-item">
          <span class="detail-label">所有者:</span>
          <span class="detail-value">{{ selectedNode.docOwner }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">提示:</span>
          <span class="detail-value">点击关联的实体节点可查看该实体在此文档中的关系图谱</span>
        </div>
      </div>
    </div>

    <!-- 实体基本信息卡片（未选中文档时显示） -->
    <div v-if="entityInfoData && !selectedDoc" class="entity-info-panel" @click.stop>
      <div class="card-header">
        <h4>{{ entityInfoData.entityName }}</h4>
        <button @click="entityInfoData = null" class="close-btn">×</button>
      </div>
      <div class="card-content">
        <div class="card-item">
          <span class="card-label">实体类型:</span>
          <span class="card-value type-badge" :class="`type-${entityInfoData.entityType?.toLowerCase()}`">
            {{ entityInfoData.entityType }}
          </span>
        </div>
        <div v-if="entityInfoData.description" class="card-item">
          <span class="card-label">描述:</span>
          <span class="card-value">{{ entityInfoData.description }}</span>
        </div>
        <div class="card-item">
          <span class="card-label">关联文档数:</span>
          <span class="card-value">{{ entityInfoData.docCount }}</span>
        </div>
      </div>
    </div>

    <!-- 实体卡片（显示实体在该文档中的关系图谱，仅在选中文档后显示） -->
    <div v-if="entityCardData && selectedDoc" class="entity-card-panel" @click.stop>
      <div class="card-header">
        <h4>{{ entityCardData.entityName }}</h4>
        <button @click="entityCardData = null" class="close-btn">×</button>
      </div>
      <div class="card-content">
        <div class="card-item">
          <span class="card-label">文档:</span>
          <span class="card-value">{{ entityCardData.docId }}</span>
        </div>
        <div class="card-item">
          <span class="card-label">实体类型:</span>
          <span class="card-value type-badge" :class="`type-${entityCardData.entityType?.toLowerCase()}`">
            {{ entityCardData.entityType }}
          </span>
        </div>
        <div v-if="entityCardData.sentences && entityCardData.sentences.length > 0" class="card-item">
          <span class="card-label">出现的句子:</span>
          <div class="sentences-list">
            <div v-for="(sentence, idx) in entityCardData.sentences" :key="idx" class="sentence-item">
              {{ sentence }}
            </div>
          </div>
        </div>
        <div v-if="entityCardData.graph" class="card-graph">
          <h5>该文档中的实体关系图谱</h5>
          <div ref="entityGraphContainer" class="entity-graph-canvas"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { Network } from 'vis-network'

// ==================== Props & Refs ====================
const props = defineProps({
  ragId: {
    type: Number,
    default: null
  }
})

const loading = ref(false)
const nodes = ref([])
const edges = ref([])
const graphContainer = ref(null)
const entityGraphContainer = ref(null)
const selectedNode = ref(null)
const selectedDoc = ref(null)
const entityCardData = ref(null)
const entityInfoData = ref(null)

let network = null
let entityGraphNetwork = null

// ==================== 常量配置 ====================
// 节点类型颜色映射 - 飞书风格
const typeColors = {
  tech: { 
    background: '#E8F4FF', 
    border: '#3370FF', 
    iconColor: '#3370FF',
    highlight: { background: '#D4E8FF', border: '#3370FF' } 
  },
  system: { 
    background: '#F0F5FF', 
    border: '#4D5DFF', 
    iconColor: '#4D5DFF',
    highlight: { background: '#E0EBFF', border: '#4D5DFF' } 
  },
  service: { 
    background: '#E8F4FF', 
    border: '#3370FF', 
    iconColor: '#3370FF',
    highlight: { background: '#D4E8FF', border: '#3370FF' } 
  },
  api: { 
    background: '#FFF4E6', 
    border: '#FF9500', 
    iconColor: '#FF9500',
    highlight: { background: '#FFE8CC', border: '#FF9500' } 
  },
  db: { 
    background: '#FFE8E8', 
    border: '#F53F3F', 
    iconColor: '#F53F3F',
    highlight: { background: '#FFD1D1', border: '#F53F3F' } 
  },
  person: { 
    background: '#E8F4FF', 
    border: '#3370FF', 
    iconColor: '#3370FF',
    highlight: { background: '#D4E8FF', border: '#3370FF' } 
  },
  org: { 
    background: '#F0F5FF', 
    border: '#4D5DFF', 
    iconColor: '#4D5DFF',
    highlight: { background: '#E0EBFF', border: '#4D5DFF' } 
  },
  concept: { 
    background: '#E6F7FF', 
    border: '#00A6FB', 
    iconColor: '#00A6FB',
    highlight: { background: '#CCEFFF', border: '#00A6FB' } 
  },
  event: { 
    background: '#FFF4E6', 
    border: '#FF9500', 
    iconColor: '#FF9500',
    highlight: { background: '#FFE8CC', border: '#FF9500' } 
  },
  module: {
    background: '#E6F7FF',
    border: '#00A6FB',
    iconColor: '#00A6FB',
    highlight: { background: '#CCEFFF', border: '#00A6FB' }
  },
  document: { 
    background: '#FFFFFF', 
    border: '#E8EAED', 
    highlight: { background: '#F7F8FA', border: '#3370FF' }, 
    shape: 'box' 
  },
  default: { 
    background: '#F7F8FA', 
    border: '#E8EAED', 
    iconColor: '#86909C',
    highlight: { background: '#E8EAED', border: '#86909C' } 
  }
}

// 网络配置常量 - 飞书风格
const NETWORK_CONFIG = {
  nodes: {
    shape: 'dot',
    size: 12,
    font: {
      size: 11,
      color: '#1D2129',
      face: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
      bold: { size: 12 }
    },
    borderWidth: 1.5,
    borderColor: '#FFFFFF',
    shadow: {
      enabled: false
    },
    chosen: {
      node: function(values, id, selected, hovering) {
        if (selected) {
          values.borderWidth = 2
        } else if (hovering) {
          values.borderWidth = 2
        }
      }
    },
    fixed: { x: false, y: false },
    mass: 1,
    margin: 15
  },
  edges: {
    width: 1.5,
    color: {
      color: '#E8EAED',
      highlight: '#3370FF',
      hover: '#4D5DFF',
      opacity: 0.6
    },
    arrows: { to: { enabled: false } },
    smooth: {
      type: 'continuous',
      roundness: 0.2
    },
    shadow: { enabled: false },
    font: {
      size: 10,
      color: '#86909C',
      align: 'middle',
      background: '#FFFFFF',
      strokeWidth: 2,
      strokeColor: '#FFFFFF'
    },
    labelHighlightBold: false,
    selectionWidth: 2,
    endPointOffset: { from: 0, to: 0 },
    length: 200
  },
  physics: {
    enabled: true,
    stabilization: {
      enabled: true,
      iterations: 400,
      fit: true,
      updateInterval: 25,
      onlyDynamicEdges: false
    },
    barnesHut: {
      gravitationalConstant: -4000,
      centralGravity: 0.03,
      springLength: 350,
      springConstant: 0.02,
      damping: 0.3,
      avoidOverlap: 1.5
    },
    maxVelocity: 30,
    minVelocity: 0.5,
    solver: 'barnesHut',
    timestep: 0.5
  },
  configure: { enabled: false },
  interaction: {
    hover: true,
    tooltipDelay: 100,
    zoomView: true,
    dragView: false,
    dragNodes: true,
    selectConnectedEdges: true,
    keyboard: false,
    multiselect: false,
    navigationButtons: false
  },
  layout: {
    improvedLayout: true,
    hierarchical: { enabled: false }
  }
}

// 文档节点尺寸配置
const DOC_NODE_CONFIG = {
  width: 45,
  height: 25,
  iconSize: 7,
  iconMargin: 2
}

// 实体节点尺寸配置
const ENTITY_NODE_CONFIG = {
  maxWidth: 23,
  height: 13,
  iconSize: 6,
  iconMargin: 2,
  maxChars: 4
}

// ==================== 工具函数 ====================
// 禁用物理引擎（统一管理）
const disablePhysics = () => {
  if (network) {
    network.setOptions({
      physics: { enabled: false },
      interaction: {
        dragView: false,
        dragNodes: true
      }
    })
  }
}

// 获取实体类型对应的形状
const getEntityShape = (typeKey) => {
  const shapeMap = {
    person: 'circle',
    org: 'rounded-rectangle',
    organization: 'rounded-rectangle',
    system: 'hexagon',
    tech: 'octagon',
    technology: 'octagon',
    api: 'star',
    concept: 'pentagon',
    event: 'triangle',
    service: 'diamond',
    db: 'rectangle',
    database: 'rectangle',
    module: 'hexagon'
  }
  return shapeMap[typeKey] || 'hexagon'
}

// 生成实体图标SVG
const generateEntityIcon = (typeKey, iconColor) => {
  const icons = {
    person: `<circle cx="5" cy="4" r="2" fill="${iconColor}"/>
             <path d="M 3 8 Q 3 6.5, 5 6.5 Q 7 6.5, 7 8" stroke="${iconColor}" stroke-width="1" fill="none"/>`,
    org: `<rect x="3" y="3" width="4" height="4" rx="0.3" fill="${iconColor}"/>
          <rect x="3.5" y="4.5" width="0.8" height="0.8" fill="#ffffff"/>
          <rect x="5" y="4.5" width="0.8" height="0.8" fill="#ffffff"/>
          <rect x="3.5" y="6" width="0.8" height="0.8" fill="#ffffff"/>
          <rect x="5" y="6" width="0.8" height="0.8" fill="#ffffff"/>
          <path d="M 3 3 L 5 2 L 7 3" stroke="#ffffff" stroke-width="0.5" fill="none"/>`,
    organization: `<rect x="3" y="3" width="4" height="4" rx="0.3" fill="${iconColor}"/>
                    <rect x="3.5" y="4.5" width="0.8" height="0.8" fill="#ffffff"/>
                    <rect x="5" y="4.5" width="0.8" height="0.8" fill="#ffffff"/>
                    <rect x="3.5" y="6" width="0.8" height="0.8" fill="#ffffff"/>
                    <rect x="5" y="6" width="0.8" height="0.8" fill="#ffffff"/>
                    <path d="M 3 3 L 5 2 L 7 3" stroke="#ffffff" stroke-width="0.5" fill="none"/>`,
    tech: `<rect x="3" y="3" width="4" height="2.5" rx="0.3" fill="${iconColor}"/>
           <rect x="3.5" y="3.5" width="3" height="1.5" fill="#ffffff"/>
           <rect x="4" y="6" width="2" height="0.5" rx="0.2" fill="${iconColor}"/>`,
    technology: `<rect x="3" y="3" width="4" height="2.5" rx="0.3" fill="${iconColor}"/>
                  <rect x="3.5" y="3.5" width="3" height="1.5" fill="#ffffff"/>
                  <rect x="4" y="6" width="2" height="0.5" rx="0.2" fill="${iconColor}"/>`,
    api: `<circle cx="5" cy="5" r="2.5" fill="${iconColor}"/>
          <circle cx="5" cy="5" r="1.5" fill="#ffffff"/>
          <path d="M 5 2 L 5 1.5 M 5 8.5 L 5 9 M 2.5 5 L 2 5 M 8.5 5 L 9 5" stroke="#ffffff" stroke-width="0.5"/>`,
    concept: `<rect x="3" y="2.5" width="4" height="5" rx="0.3" fill="${iconColor}"/>
              <line x1="5" y1="2.5" x2="5" y2="7.5" stroke="#ffffff" stroke-width="0.5"/>
              <line x1="3" y1="4.5" x2="7" y2="4.5" stroke="#ffffff" stroke-width="0.5"/>
              <line x1="3" y1="6" x2="7" y2="6" stroke="#ffffff" stroke-width="0.5"/>`,
    event: `<path d="M 5 2 L 5 6 L 3 7 L 5 6 L 7 7 L 5 6 Z" fill="${iconColor}"/>
            <circle cx="5" cy="2" r="1.5" fill="${iconColor}"/>
            <circle cx="5" cy="2" r="0.8" fill="#ffffff"/>`
  }
  return icons[typeKey] || `<circle cx="5" cy="5" r="2.5" fill="${iconColor}"/>`
}

// 生成文档节点SVG - 飞书风格
const createDocNodeSvg = (docId, docName, docOwner) => {
  const { width, height, iconSize, iconMargin } = DOC_NODE_CONFIG
  const textStartX = iconMargin + iconSize + 2
  const textWidth = width - textStartX - 2
  
  const maxNameChars = Math.floor(textWidth / 2.5)
  const displayName = docName.length > maxNameChars ? docName.substring(0, maxNameChars) + '...' : docName
  const maxOwnerChars = Math.floor(textWidth / 2)
  const displayOwner = docOwner && docOwner.length > maxOwnerChars ? docOwner.substring(0, maxOwnerChars) + '...' : docOwner
  
  return `<svg width="${width}" height="${height}" xmlns="http://www.w3.org/2000/svg">
    <rect x="0" y="0" width="${width}" height="${height}" 
          rx="4" ry="4" fill="#FFFFFF" 
          stroke="#E8EAED" stroke-width="1"/>
    <g transform="translate(${iconMargin}, ${iconMargin + 1})">
      <rect x="0" y="0" width="${iconSize}" height="${iconSize}" rx="2" fill="#E8F4FF"/>
      <g transform="translate(1.5, 2)">
        <rect x="0" y="0" width="4" height="0.5" rx="0.25" fill="#3370FF"/>
        <rect x="0" y="1.2" width="4" height="0.5" rx="0.25" fill="#3370FF"/>
        <rect x="0" y="2.4" width="4" height="0.5" rx="0.25" fill="#3370FF"/>
      </g>
    </g>
    <g transform="translate(${textStartX}, 2)">
      <text x="0" y="4.5" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, sans-serif" 
            font-size="2" font-weight="500" fill="#1D2129">${displayName}</text>
      ${displayOwner ? `<text x="0" y="9.5" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, sans-serif" 
            font-size="1.8" fill="#86909C">${displayOwner}</text>` : ''}
    </g>
  </svg>`
}

// 生成实体节点SVG - 飞书风格
const createEntityNodeSvg = (entityId, entityName, entityType) => {
  const { maxWidth, height, iconSize, iconMargin, maxChars } = ENTITY_NODE_CONFIG
  const typeKey = (entityType || 'default').toLowerCase()
  const colorConfig = typeColors[typeKey] || typeColors.default
  
  const displayName = entityName.length > maxChars ? entityName.substring(0, maxChars) + '...' : entityName
  const labelWidth = maxWidth
  const textStartX = iconMargin + iconSize + 2
  
  const iconColor = colorConfig.iconColor || colorConfig.border
  
  return `<svg width="${labelWidth}" height="${height}" xmlns="http://www.w3.org/2000/svg">
    <rect x="0.5" y="0.5" width="${labelWidth - 1}" height="${height - 1}" 
          rx="4" ry="4" 
          fill="${colorConfig.background}" 
          stroke="${colorConfig.border}" 
          stroke-width="1"/>
    <circle cx="${iconMargin + iconSize / 2}" cy="${height / 2}" r="2.5" fill="${iconColor}"/>
    <text x="${textStartX}" y="${height/2 + 0.5}" 
          font-family="-apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif" 
          font-size="1.8" font-weight="400" 
          fill="#1D2129" 
          text-anchor="start" 
          dominant-baseline="middle">${displayName}</text>
  </svg>`
}

// ==================== 生命周期 ====================
onMounted(() => {
  nextTick(() => {
    if (graphContainer.value) {
      initGraph()
    }
    if (props.ragId) {
      console.log('KnowledgeGraph mounted, ragId:', props.ragId)
      loadDocGraph()
    } else {
      console.warn('KnowledgeGraph mounted but no ragId provided')
    }
  })
})

onUnmounted(() => {
  if (network) {
    network.destroy()
  }
  if (entityGraphNetwork) {
    entityGraphNetwork.destroy()
  }
})

// ==================== 图谱初始化 ====================
// 初始化vis-network图谱
const initGraph = () => {
  if (!graphContainer.value) return
  
  const data = {
    nodes: nodes.value,
    edges: edges.value
  }
  
  network = new Network(graphContainer.value, data, NETWORK_CONFIG)
  setupNetworkEvents()
}

// 设置网络事件监听
const setupNetworkEvents = () => {
  if (!network) return
  
  // 稳定化完成后禁用物理引擎
  network.once('stabilizationEnd', () => {
    disablePhysics()
    network.setOptions({
      nodes: {
        fixed: { x: false, y: false }
      }
    })
  })
  
  // 监听稳定化进度
  network.on('stabilizationProgress', (params) => {
    if (params.iterations === params.total) {
      disablePhysics()
    }
  })
  
  // 拖拽事件：确保物理引擎始终禁用
  network.on('dragStart', disablePhysics)
  network.on('drag', disablePhysics)
  network.on('dragEnd', disablePhysics)
  
  // 节点点击事件
  network.on('click', handleNodeClick)
  
  // 节点悬停事件
  network.on('hoverNode', () => {
    if (graphContainer.value) {
      graphContainer.value.style.cursor = 'pointer'
    }
  })
  
  network.on('blurNode', () => {
    if (graphContainer.value) {
      graphContainer.value.style.cursor = 'default'
    }
  })
}

// ==================== 节点交互处理 ====================
// 处理节点点击事件
const handleNodeClick = async (params) => {
  disablePhysics()
  
  if (params.nodes.length > 0) {
    const nodeId = params.nodes[0]
    const node = nodes.value.find(n => n.id === nodeId)
    if (node) {
      selectedNode.value = node
      
      if (node.nodeType === 'document') {
        selectedDoc.value = node.docId || nodeId
        entityCardData.value = null
        entityInfoData.value = null
        highlightDocRelated(nodeId)
      } else if (node.nodeType === 'entity') {
        if (selectedDoc.value) {
          await showEntityCard(nodeId, selectedDoc.value)
        } else {
          await showEntityInfo(nodeId)
        }
      }
    }
  } else {
    clearSelection()
    resetHighlight()
  }
  
  disablePhysics()
}

// 清除选择
const clearSelection = () => {
  selectedNode.value = null
  selectedDoc.value = null
  entityCardData.value = null
  entityInfoData.value = null
}

// ==================== 高亮处理 ====================
// 重置高亮
const resetHighlight = () => {
  if (!network || nodes.value.length === 0) return
  
  disablePhysics()
  
  // 恢复节点样式
  nodes.value.forEach(n => {
    const updateData = {
      borderWidth: n.nodeType === 'document' ? 1 : 1.5
    }
    
    if (n.nodeType === 'document') {
      updateData.color = {
        border: '#E8EAED',
        highlight: n.color?.highlight || {}
      }
    } else {
      const typeKey = (n.type || 'default').toLowerCase()
      const colorConfig = typeColors[typeKey] || typeColors.default
      updateData.color = {
        background: colorConfig.background,
        border: colorConfig.border,
        highlight: colorConfig.highlight,
        opacity: 1
      }
    }
    network.updateNode(n.id, updateData)
  })
  
  // 恢复边样式
  edges.value.forEach(e => {
    network.updateEdge(e.id, {
      width: 1.5,
      color: {
        color: '#E8EAED',
        highlight: '#3370FF',
        hover: '#4D5DFF',
        opacity: 0.6
      }
    })
  })
  
  disablePhysics()
}

// 高亮文档相关的实体和边
const highlightDocRelated = (docId) => {
  if (!network) return
  
  disablePhysics()
  
  // 找到所有与该文档相关的边和实体
  const relatedEdges = edges.value.filter(e => e.from === docId || e.docId === docId)
  const relatedEntityIds = new Set()
  relatedEdges.forEach(e => {
    if (e.to) relatedEntityIds.add(e.to)
    if (e.entityId) relatedEntityIds.add(e.entityId)
  })
  
  // 更新节点样式
  nodes.value.forEach(n => {
    const updateData = {}
    if (n.id === docId) {
      // 文档节点：蓝色高亮
      updateData.borderWidth = 2
      updateData.color = { 
        border: '#3370FF',
        highlight: { border: '#3370FF', background: n.color?.background || '#ffffff' }
      }
    } else if (relatedEntityIds.has(n.id)) {
      // 相关实体节点：蓝色高亮
      updateData.borderWidth = 2
      updateData.color = { 
        border: '#3370FF',
        highlight: { border: '#3370FF', background: n.color?.background || '#ffffff' }
      }
    } else {
      // 其他节点：淡化
      updateData.borderWidth = 1
      updateData.color = { 
        opacity: 0.3,
        highlight: { opacity: 0.3 }
      }
    }
    network.updateNode(n.id, updateData)
  })
  
  // 更新边样式
  edges.value.forEach(e => {
    const updateData = {}
    if (e.from === docId || e.docId === docId) {
      // 相关边：蓝色高亮
      updateData.width = 2
      updateData.color = { 
        color: '#3370FF',
        highlight: '#3370FF',
        hover: '#4D5DFF'
      }
    } else {
      // 其他边：淡化
      updateData.width = 1
      updateData.color = { 
        color: '#E8EAED',
        highlight: '#E8EAED',
        hover: '#E8EAED',
        opacity: 0.3
      }
    }
    network.updateEdge(e.id, updateData)
  })
  
  disablePhysics()
}

// ==================== 实体信息处理 ====================
// 显示实体基本信息（未选中文档时）
const showEntityInfo = async (entityId) => {
    try {
      // 统计有多少个文档指向该实体
      const relatedDocs = new Set()
      edges.value.forEach(e => {
        if (e.to === entityId || e.entityId === entityId) {
          if (e.docId) relatedDocs.add(e.docId)
          if (e.from && nodes.value.find(n => n.id === e.from && n.nodeType === 'document')) {
            relatedDocs.add(e.from)
          }
        }
      })
      
      // 从节点中获取实体信息
      const entityNode = nodes.value.find(n => n.id === entityId && n.nodeType === 'entity')
      
      entityInfoData.value = {
        entityId: entityId,
        entityName: entityNode?.label || `实体${entityId}`,
        entityType: entityNode?.type || 'default',
        description: '', // 可以从后端获取，暂时为空
        docCount: relatedDocs.size
      }
      
      // 清除实体卡片
      entityCardData.value = null
    } catch (error) {
      console.error('加载实体信息失败:', error)
    }
  }
  
// 显示实体卡片
const showEntityCard = async (entityId, docId) => {
    try {
      const response = await fetch(`/rag/kg/doc/${docId}/entity/${entityId}/graph`)
      const data = await response.json()
      
      if (data.success) {
        // 获取实体信息
        const entityResponse = await fetch(`/rag/kg/doc/${docId}/entities`)
        const entityData = await entityResponse.json()
        const entity = entityData.entities?.find(e => e.entityId === entityId)
        
        entityCardData.value = {
          entityId: entityId,
          entityName: entity?.entityName || `实体${entityId}`,
          entityType: entity?.entityType || 'default',
          docId: docId,
          sentences: entity?.sentences || [],
          graph: data.graph
        }
        
        // 在卡片中渲染实体关系图谱
        renderEntityGraph(data.graph)
      }
    } catch (error) {
      console.error('加载实体卡片数据失败:', error)
    }
  }
  
// 在卡片中渲染实体关系图谱
const renderEntityGraph = (graphData) => {
    if (!graphData || (!graphData.nodes || graphData.nodes.length === 0)) {
      return
    }
    
    // 等待DOM更新
    nextTick(() => {
      if (!entityGraphContainer.value) {
        console.warn('entityGraphContainer未找到')
        return
      }
      
      // 销毁之前的图谱
      if (entityGraphNetwork) {
        entityGraphNetwork.destroy()
        entityGraphNetwork = null
      }
      
      const graphNodes = (graphData.nodes || []).map(node => ({
        id: node.id,
        label: node.name || `实体${node.id}`,
        type: node.type || 'default',
        color: typeColors[node.type?.toLowerCase()] || typeColors.default,
        shape: 'dot',
        size: 7
      }))
      
      const graphEdges = (graphData.edges || []).map((edge, index) => ({
        id: edge.id || `edge-${index}`,
        from: edge.source,
        to: edge.target,
        label: edge.type || '',
        title: edge.evidence || edge.type || '',
        color: {
          color: '#3370FF',
          highlight: '#4D5DFF'
        }
      }))
      
      const data = {
        nodes: graphNodes,
        edges: graphEdges
      }
      
      const options = {
        nodes: {
          shape: 'dot',
          size: 7,
          font: { size: 9, color: '#1D2129' }
        },
        edges: {
          width: 1.5,
          arrows: { to: { enabled: true } },
          color: { color: '#3370FF' }
        },
        physics: {
          enabled: true,
          stabilization: { iterations: 100 }
        },
        interaction: {
          hover: true,
          zoomView: true,
          dragView: true
        }
      }
      
      try {
        entityGraphNetwork = new Network(entityGraphContainer.value, data, options)
      } catch (error) {
        console.error('渲染实体关系图谱失败:', error)
      }
    })
  }

// ==================== 图谱数据更新 ====================
// 更新图谱数据
const updateGraph = () => {
  if (!network) {
    initGraph()
    return
  }
  
  const data = {
    nodes: nodes.value,
    edges: edges.value
  }
  
  network.setData(data)
  // 不自动适应视图，避免错位
  // network.fit() 会导致视图移动，已禁用
}

// 监听数据变化
watch([nodes, edges], () => {
  if (nodes.value.length > 0) {
    nextTick(() => {
      updateGraph()
    })
  }
}, { deep: true })

// 监听 ragId 变化，自动加载文档图谱
watch(() => props.ragId, (newRagId) => {
  if (newRagId) {
    console.log('ragId changed to:', newRagId)
    loadDocGraph()
  }
}, { immediate: false })

// ==================== 节点和边创建 ====================
// 创建文档节点
const createDocNode = (docId, docName, docOwner) => {
  const { width, height } = DOC_NODE_CONFIG
  const svgContent = createDocNodeSvg(docId, docName, docOwner)
  const svgDataUrl = 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svgContent)
  const docNodeSize = Math.max(width, height)
  
  return {
    id: docId,
    label: docName,
    type: 'DOCUMENT',
    nodeType: 'document',
    docId: docId,
    docName: docName,
    docOwner: docOwner,
    shape: 'image',
    image: svgDataUrl,
    brokenImage: svgDataUrl,
    size: docNodeSize + 15,
    originalSize: docNodeSize,
    font: { size: 0 },
    widthConstraint: { maximum: width + 10 },
    heightConstraint: { maximum: height + 10 },
    margin: 10
  }
}

// 创建实体节点
const createEntityNode = (entityId, entityName, entityType) => {
  const { maxWidth, height } = ENTITY_NODE_CONFIG
  const svgContent = createEntityNodeSvg(entityId, entityName, entityType)
  const svgDataUrl = 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svgContent)
  const nodeSize = Math.max(maxWidth, height)
  
  return {
    id: entityId,
    label: entityName,
    type: entityType || 'default',
    nodeType: 'entity',
    shape: 'image',
    image: svgDataUrl,
    brokenImage: svgDataUrl,
    size: nodeSize + 8,
    originalSize: nodeSize,
    font: { size: 0 },
    widthConstraint: { maximum: maxWidth + 8 },
    heightConstraint: { maximum: height + 8 },
    margin: 6
  }
}

// 创建文档到实体的边
const createDocEntityEdge = (docId, entityId, sentences) => {
  return {
    id: `doc-${docId}-entity-${entityId}`,
    from: docId,
    to: entityId,
    label: '',
    title: sentences.join('\n'),
    sentences: sentences,
    docId: docId,
    entityId: entityId,
    color: {
      color: '#3370FF',
      highlight: '#4D5DFF',
      hover: '#4D5DFF',
      opacity: 0.8
    },
    dashes: false,
    width: 2,
    shadow: { enabled: false }
  }
}

// ==================== 数据加载 ====================
// 加载文档知识图谱（以文档为节点，连接实体）
const loadDocGraph = async () => {
  if (!props.ragId) {
    console.warn('ragId未提供，无法加载文档图谱')
    return
  }
  
  loading.value = true
  try {
    const url = `/rag/${props.ragId}/documents/kg`
    const docResponse = await fetch(url)
    const docData = await docResponse.json()
    
    if (!docData.success || !docData.docs || docData.docs.length === 0) {
      nodes.value = []
      edges.value = []
      return
    }
    
    const allNodes = []
    const allEdges = []
    const entityMap = new Map()
    
    // 遍历每个文档，添加文档节点、实体节点和边
    docData.docs.forEach(doc => {
      const docId = doc.docId
      const docName = doc.name || docId
      const docOwner = doc.owner || ''
      const entities = doc.entities || []
      
      // 添加文档节点
      allNodes.push(createDocNode(docId, docName, docOwner))
      
      // 添加实体节点和文档->实体的边
      entities.forEach(entity => {
        const entityId = entity.entityId
        
        // 如果实体还未添加，添加实体节点
        if (!entityMap.has(entityId)) {
          allNodes.push(createEntityNode(entityId, entity.entityName, entity.entityType))
          entityMap.set(entityId, entity)
        }
        
        // 添加文档->实体的边
        const sentences = entity.sentences || []
        allEdges.push(createDocEntityEdge(docId, entityId, sentences))
      })
    })
    
    nodes.value = allNodes
    edges.value = allEdges
  } catch (error) {
    console.error('加载文档知识图谱失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载全部知识图谱
const loadAllGraph = async () => {
  loading.value = true
  try {
    const response = await fetch('/rag/kg/all?maxDepth=1')
    const data = await response.json()
    if (data.success && data.graph) {
      // 转换节点数据格式
      nodes.value = (data.graph.nodes || []).map(node => {
        const nodeType = node.nodeType || 'entity'
        const typeKey = nodeType === 'document' ? 'document' : (node.type?.toLowerCase() || 'default')
        const colorConfig = typeColors[typeKey] || typeColors.default
        
        return {
          id: node.id,
          label: node.name || (nodeType === 'document' ? `文档${node.id}` : `实体${node.id}`),
          type: node.type || 'default',
          nodeType: nodeType,
          description: node.description,
          color: colorConfig,
          shape: nodeType === 'document' ? 'box' : 'dot',
          size: nodeType === 'document' ? 25 : (20 + Math.random() * 10)
        }
      })
      
      // 转换边数据格式
      edges.value = (data.graph.edges || []).map((edge, index) => {
        const edgeType = edge.edgeType || 'entity_relation'
        const isDocEntity = edgeType === 'doc_entity'
        
        return {
          id: edge.id || `edge-${index}`,
          from: edge.source,
          to: edge.target,
          label: edge.type || '',
          title: edge.evidence || edge.type || '',
          color: {
            color: isDocEntity ? '#14b8a6' : '#60a5fa',
            highlight: isDocEntity ? '#2dd4bf' : '#93c5fd',
            hover: isDocEntity ? '#2dd4bf' : '#93c5fd'
          },
          dashes: isDocEntity ? [5, 5] : false // 文档-实体关系使用虚线
        }
      })
    }
  } catch (error) {
    console.error('加载全部知识图谱失败:', error)
  } finally {
    loading.value = false
  }
}

// 重置视图
const resetView = () => {
  if (network) {
    network.fit({
      animation: {
        duration: 750,
        easingFunction: 'easeInOutQuad'
      }
    })
  }
  selectedNode.value = null
  selectedDoc.value = null
  entityCardData.value = null
  entityInfoData.value = null
  resetHighlight()
}
</script>

<style scoped>
.knowledge-graph-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #FFFFFF;
  border-radius: 8px;
  overflow: hidden;
}

.kg-header {
  padding: 16px 20px;
  background: #FFFFFF;
  border-bottom: 1px solid #E8EAED;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.kg-title {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #1D2129;
}

.kg-controls {
  display: flex;
  gap: 8px;
  align-items: center;
}

.kg-reset-btn {
  padding: 6px 12px;
  background: #F7F8FA;
  border: 1px solid #E8EAED;
  border-radius: 4px;
  color: #1D2129;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 400;
}

.kg-reset-btn:hover {
  background: #E8EAED;
  border-color: #D1D5DB;
  color: #3370FF;
}

.kg-canvas {
  flex: 1;
  width: 100%;
  min-height: 500px;
  position: relative;
  background: #F7F8FA;
  border-radius: 0 0 8px 8px;
  overflow: hidden;
}

.kg-loading,
.kg-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #86909C;
  gap: 16px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #E8EAED;
  border-top-color: #3370FF;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.kg-detail-panel,
.entity-info-panel {
  position: absolute;
  top: 80px;
  right: 20px;
  width: 320px;
  max-height: calc(100% - 100px);
  background: #FFFFFF;
  border: 1px solid #E8EAED;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  z-index: 1000;
  overflow-y: auto;
  padding: 16px;
}

.entity-card-panel {
  position: absolute;
  top: 80px;
  right: 16px;
  width: 400px;
  max-height: 80vh;
  overflow-y: auto;
  background: #FFFFFF;
  border: 1px solid #E8EAED;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  z-index: 1000;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.detail-header,
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #E8EAED;
}

.card-header h4 {
  margin: 0;
  color: #1D2129;
  font-size: 15px;
  font-weight: 500;
}

.detail-header h4 {
  margin: 0;
  color: #1D2129;
  font-size: 15px;
  font-weight: 500;
}

.close-btn {
  background: none;
  border: none;
  color: #86909C;
  font-size: 20px;
  cursor: pointer;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s;
  line-height: 1;
  border-radius: 4px;
}

.close-btn:hover {
  color: #1D2129;
  background: #F7F8FA;
}

.detail-content,
.card-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.card-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.card-label {
  font-size: 12px;
  color: #86909C;
  text-transform: none;
  letter-spacing: 0;
  font-weight: 400;
}

.card-value {
  font-size: 14px;
  color: #1D2129;
}

.sentences-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 200px;
  overflow-y: auto;
  padding: 8px;
  background: #F7F8FA;
  border-radius: 6px;
}

.sentence-item {
  font-size: 13px;
  color: #1D2129;
  line-height: 1.6;
  padding: 8px;
  background: #FFFFFF;
  border-left: 2px solid #3370FF;
  border-radius: 4px;
}

.card-graph {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #E8EAED;
}

.card-graph h5 {
  margin: 0 0 12px 0;
  color: #1D2129;
  font-size: 14px;
  font-weight: 500;
}

.entity-graph-canvas {
  width: 100%;
  height: 300px;
  background: #F7F8FA;
  border-radius: 6px;
  border: 1px solid #E8EAED;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-label {
  font-size: 12px;
  color: #86909C;
  text-transform: none;
  letter-spacing: 0;
  font-weight: 400;
}

.detail-value {
  font-size: 14px;
  color: #1D2129;
}

.detail-text-content {
  font-size: 13px;
  color: #1D2129;
  line-height: 1.6;
  max-height: 300px;
  overflow-y: auto;
  padding: 12px;
  background: #F7F8FA;
  border-radius: 6px;
  border: 1px solid #E8EAED;
  white-space: pre-wrap;
  word-wrap: break-word;
  margin-top: 4px;
}

.type-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.type-tech { background: #E8F4FF; color: #3370FF; }
.type-system { background: #F0F5FF; color: #4D5DFF; }
.type-service { background: #E8F4FF; color: #3370FF; }
.type-api { background: #FFF4E6; color: #FF9500; }
.type-db { background: #FFE8E8; color: #F53F3F; }
.type-person { background: #E8F4FF; color: #3370FF; }
.type-org { background: #F0F5FF; color: #4D5DFF; }
.type-concept { background: #E6F7FF; color: #00A6FB; }
.type-event { background: #FFF4E6; color: #FF9500; }
</style>
