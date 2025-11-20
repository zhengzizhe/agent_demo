<template>
  <div class="knowledge-graph-container">
    <div class="kg-header">
      <h3 class="kg-title">知识图谱</h3>
      <div class="kg-controls">
        <input
          v-model="searchQuery"
          @keyup.enter="searchGraph"
          placeholder="搜索知识图谱..."
          class="kg-search-input"
        />
        <button @click="searchGraph" class="kg-search-btn" :disabled="loading">
          <span v-if="loading">搜索中...</span>
          <span v-else>搜索</span>
        </button>
        <button @click="loadAllGraph" class="kg-all-btn" :disabled="loading">
          <span v-if="loading">加载中...</span>
          <span v-else>显示全部</span>
        </button>
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
      <p>暂无数据，请输入关键词搜索或点击"显示全部"</p>
    </div>
    
    <div v-else ref="graphContainer" class="kg-canvas"></div>
    
    <!-- 节点详情面板 -->
    <div v-if="selectedNode" class="kg-detail-panel" @click.stop>
      <div class="detail-header">
        <h4>{{ selectedNode.label }}</h4>
        <button @click="selectedNode = null" class="close-btn">×</button>
      </div>
      <div class="detail-content">
        <div class="detail-item">
          <span class="detail-label">类型:</span>
          <span class="detail-value type-badge" :class="`type-${selectedNode.type?.toLowerCase()}`">
            {{ selectedNode.type }}
          </span>
        </div>
        <div v-if="selectedNode.description" class="detail-item">
          <span class="detail-label">描述:</span>
          <span class="detail-value">{{ selectedNode.description }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { Network } from 'vis-network'

const props = defineProps({
  ragId: {
    type: Number,
    default: null
  }
})

const searchQuery = ref('')
const loading = ref(false)
const nodes = ref([])
const edges = ref([])
const graphContainer = ref(null)
const selectedNode = ref(null)

let network = null

// 节点类型颜色映射
const typeColors = {
  tech: { background: '#3b82f6', border: '#2563eb', highlight: { background: '#60a5fa', border: '#3b82f6' } },
  system: { background: '#8b5cf6', border: '#7c3aed', highlight: { background: '#a78bfa', border: '#8b5cf6' } },
  service: { background: '#10b981', border: '#059669', highlight: { background: '#34d399', border: '#10b981' } },
  api: { background: '#f59e0b', border: '#d97706', highlight: { background: '#fbbf24', border: '#f59e0b' } },
  db: { background: '#ef4444', border: '#dc2626', highlight: { background: '#f87171', border: '#ef4444' } },
  person: { background: '#ec4899', border: '#db2777', highlight: { background: '#f472b6', border: '#ec4899' } },
  org: { background: '#06b6d4', border: '#0891b2', highlight: { background: '#22d3ee', border: '#06b6d4' } },
  concept: { background: '#6366f1', border: '#4f46e5', highlight: { background: '#818cf8', border: '#6366f1' } },
  event: { background: '#f97316', border: '#ea580c', highlight: { background: '#fb923c', border: '#f97316' } },
  default: { background: '#6b7280', border: '#4b5563', highlight: { background: '#9ca3af', border: '#6b7280' } }
}

// 初始化图谱
onMounted(() => {
  if (graphContainer.value) {
    initGraph()
  }
})

onUnmounted(() => {
  if (network) {
    network.destroy()
  }
})

// 初始化vis-network图谱
const initGraph = () => {
  if (!graphContainer.value) return
  
  const data = {
    nodes: nodes.value,
    edges: edges.value
  }
  
  const options = {
    nodes: {
      shape: 'dot',
      size: 20,
      font: {
        size: 14,
        color: '#e2e8f0',
        face: 'Inter, system-ui, sans-serif'
      },
      borderWidth: 2,
      shadow: {
        enabled: true,
        color: 'rgba(96, 165, 250, 0.5)',
        size: 10,
        x: 0,
        y: 0
      },
      chosen: {
        node: function(values, id, selected, hovering) {
          values.size = 30
          values.shadow = {
            enabled: true,
            color: 'rgba(96, 165, 250, 0.8)',
            size: 15
          }
        }
      }
    },
    edges: {
      width: 2,
      color: {
        color: '#60a5fa',
        highlight: '#93c5fd',
        hover: '#93c5fd'
      },
      arrows: {
        to: {
          enabled: true,
          scaleFactor: 1.2
        }
      },
      smooth: {
        type: 'continuous',
        roundness: 0.5
      },
      shadow: {
        enabled: true,
        color: 'rgba(96, 165, 250, 0.3)',
        size: 5
      },
      font: {
        size: 11,
        color: '#94a3b8',
        align: 'middle'
      },
      labelHighlightBold: false
    },
    physics: {
      enabled: true,
      stabilization: {
        enabled: true,
        iterations: 200,
        fit: true
      },
      barnesHut: {
        gravitationalConstant: -2000,
        centralGravity: 0.1,
        springLength: 200,
        springConstant: 0.04,
        damping: 0.09,
        avoidOverlap: 1
      }
    },
    interaction: {
      hover: true,
      tooltipDelay: 100,
      zoomView: true,
      dragView: true,
      selectConnectedEdges: true
    },
    layout: {
      improvedLayout: true,
      hierarchical: {
        enabled: false
      }
    }
  }
  
  network = new Network(graphContainer.value, data, options)
  
  // 节点点击事件
  network.on('click', (params) => {
    if (params.nodes.length > 0) {
      const nodeId = params.nodes[0]
      const node = nodes.value.find(n => n.id === nodeId)
      if (node) {
        selectedNode.value = node
      }
    } else {
      selectedNode.value = null
    }
  })
  
  // 节点悬停事件
  network.on('hoverNode', (params) => {
    graphContainer.value.style.cursor = 'pointer'
  })
  
  network.on('blurNode', () => {
    graphContainer.value.style.cursor = 'default'
  })
}

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
  network.fit({
    animation: {
      duration: 1000,
      easingFunction: 'easeInOutQuad'
    }
  })
}

// 监听数据变化
watch([nodes, edges], () => {
  if (nodes.value.length > 0) {
    nextTick(() => {
      updateGraph()
    })
  }
}, { deep: true })

// 搜索知识图谱
const searchGraph = async () => {
  if (!searchQuery.value.trim()) {
    return
  }
  
  loading.value = true
  try {
    const response = await fetch('/rag/kg/search', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        queryText: searchQuery.value,
        limit: 10,
        similarityThreshold: 0.6,
        maxDepth: 1
      })
    })
    
    const data = await response.json()
    if (data.success && data.graph) {
      // 转换节点数据格式
      nodes.value = (data.graph.nodes || []).map(node => ({
        id: node.id,
        label: node.name || `实体${node.id}`,
        type: node.type || 'default',
        description: node.description,
        color: typeColors[node.type?.toLowerCase()] || typeColors.default,
        size: 20 + Math.random() * 10 // 随机大小增加视觉层次
      }))
      
      // 转换边数据格式
      edges.value = (data.graph.edges || []).map((edge, index) => ({
        id: edge.id || `edge-${index}`,
        from: edge.source,
        to: edge.target,
        label: edge.type || '',
        title: edge.evidence || edge.type || '',
        color: {
          color: '#60a5fa',
          highlight: '#93c5fd',
          hover: '#93c5fd'
        }
      }))
    }
  } catch (error) {
    console.error('搜索知识图谱失败:', error)
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
      nodes.value = (data.graph.nodes || []).map(node => ({
        id: node.id,
        label: node.name || `实体${node.id}`,
        type: node.type || 'default',
        description: node.description,
        color: typeColors[node.type?.toLowerCase()] || typeColors.default,
        size: 20 + Math.random() * 10
      }))
      
      // 转换边数据格式
      edges.value = (data.graph.edges || []).map((edge, index) => ({
        id: edge.id || `edge-${index}`,
        from: edge.source,
        to: edge.target,
        label: edge.type || '',
        title: edge.evidence || edge.type || '',
        color: {
          color: '#60a5fa',
          highlight: '#93c5fd',
          hover: '#93c5fd'
        }
      }))
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
}
</script>

<style scoped>
.knowledge-graph-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #0f172a;
  border-radius: 8px;
  overflow: hidden;
}

.kg-header {
  padding: 16px;
  background: rgba(30, 41, 59, 0.8);
  border-bottom: 1px solid rgba(96, 165, 250, 0.2);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  backdrop-filter: blur(10px);
}

.kg-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #e2e8f0;
}

.kg-controls {
  display: flex;
  gap: 8px;
  align-items: center;
}

.kg-search-input {
  padding: 8px 12px;
  background: rgba(15, 23, 42, 0.8);
  border: 1px solid rgba(96, 165, 250, 0.3);
  border-radius: 6px;
  color: #e2e8f0;
  font-size: 14px;
  min-width: 200px;
  transition: all 0.2s;
}

.kg-search-input:focus {
  outline: none;
  border-color: #60a5fa;
  box-shadow: 0 0 0 3px rgba(96, 165, 250, 0.1);
}

.kg-search-btn,
.kg-all-btn,
.kg-reset-btn {
  padding: 8px 16px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border: none;
  border-radius: 6px;
  color: white;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 500;
}

.kg-search-btn:hover:not(:disabled),
.kg-all-btn:hover:not(:disabled),
.kg-reset-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}

.kg-search-btn:disabled,
.kg-all-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.kg-all-btn {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
}

.kg-all-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #7c3aed 0%, #6d28d9 100%);
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.4);
}

.kg-reset-btn {
  background: linear-gradient(135deg, #6b7280 0%, #4b5563 100%);
}

.kg-canvas {
  flex: 1;
  width: 100%;
  min-height: 500px;
  position: relative;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
}

.kg-loading,
.kg-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  gap: 16px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(96, 165, 250, 0.2);
  border-top-color: #60a5fa;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.kg-detail-panel {
  position: absolute;
  top: 80px;
  right: 16px;
  width: 300px;
  background: rgba(15, 23, 42, 0.95);
  border: 1px solid rgba(96, 165, 250, 0.3);
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(10px);
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

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(96, 165, 250, 0.2);
}

.detail-header h4 {
  margin: 0;
  color: #e2e8f0;
  font-size: 16px;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  color: #94a3b8;
  font-size: 24px;
  cursor: pointer;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s;
  line-height: 1;
}

.close-btn:hover {
  color: #e2e8f0;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-label {
  font-size: 12px;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-weight: 500;
}

.detail-value {
  font-size: 14px;
  color: #e2e8f0;
}

.type-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.type-tech { background: rgba(59, 130, 246, 0.2); color: #60a5fa; }
.type-system { background: rgba(139, 92, 246, 0.2); color: #a78bfa; }
.type-service { background: rgba(16, 185, 129, 0.2); color: #34d399; }
.type-api { background: rgba(245, 158, 11, 0.2); color: #fbbf24; }
.type-db { background: rgba(239, 68, 68, 0.2); color: #f87171; }
.type-person { background: rgba(236, 72, 153, 0.2); color: #f472b6; }
.type-org { background: rgba(6, 182, 212, 0.2); color: #22d3ee; }
.type-concept { background: rgba(99, 102, 241, 0.2); color: #818cf8; }
.type-event { background: rgba(249, 115, 22, 0.2); color: #fb923c; }
</style>
