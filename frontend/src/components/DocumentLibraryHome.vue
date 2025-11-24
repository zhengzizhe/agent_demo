<template>
  <div class="document-library-home" :class="homePhaseClass">
    <div class="dl-main-wrapper">
      <div class="dl-main">
        <div class="dl-home-wrapper">
          <!-- 顶部标题区 -->
          <div class="dl-home-header" style="--section-delay: 0ms;">
            <div class="dl-home-header-inner">
              <div class="dl-home-header-left">
                <div class="title-wrapper">
                  <h1 class="dl-home-title">文档库</h1>
                  <p class="dl-home-subtitle">
                    <span class="subtitle-text" :class="{ typing: showTyping }">{{ currentSubtitle }}</span>
                    <span class="cursor" v-if="showTyping">|</span>
                  </p>
                </div>
              </div>
              <div class="dl-home-header-right">
                <div class="dl-home-search">
                  <SearchBox v-model="searchQuery" placeholder="搜索文档、标签或内容..." />
                </div>
                <button class="btn-new" @click="handleCreateDocument">
                  <svg width="18" height="18" viewBox="0 0 16 16" fill="none">
                    <path d="M8 2v12M2 8h12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                  <span>新建</span>
                  <span class="btn-shortcut">⌘N</span>
                </button>
              </div>
            </div>
          </div>

          <!-- 主内容区 - Grid布局 -->
          <div class="dl-home-main">
            <!-- 欢迎提示卡片 -->
            <section class="home-section welcome-tip" v-if="showWelcomeTip">
              <div class="welcome-card">
                <div class="welcome-icon">🎉</div>
                <div class="welcome-content">
                  <h3 class="welcome-title">{{ welcomeMessage.title }}</h3>
                  <p class="welcome-desc">{{ welcomeMessage.desc }}</p>
                </div>
                <button class="welcome-close" @click="showWelcomeTip = false">×</button>
              </div>
            </section>

            <!-- Hero区：继续您的工作 - 蓝色呼吸动画框 -->
            <section class="home-section hero-section" style="--section-delay: 80ms;">
              <div 
                v-if="lastEditedDocument"
                class="hero-card-breathing"
                @click="openDocument(lastEditedDocument)"
              >
                <div class="hero-card-inner">
                  <div class="hero-label-wrapper">
                    <h2 class="hero-label">继续您的工作</h2>
                    <span class="hero-hint">点击继续编辑</span>
                  </div>
                  <div class="hero-doc-info">
                    <div class="hero-doc-icon">
                      <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                        <rect x="4" y="4" width="16" height="16" rx="2" stroke="currentColor" stroke-width="1.5" fill="none"/>
                        <path d="M8 8h8M8 12h8M8 16h4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                      </svg>
                    </div>
                    <div class="hero-doc-content">
                      <h3 class="hero-doc-title">{{ lastEditedDocument.name || '未命名文档' }}</h3>
                      <p class="hero-doc-time">
                        <span>最近编辑于 {{ formatDate(lastEditedDocument.updatedAt || lastEditedDocument.createdAt) }}</span>
                        <span class="hero-doc-hint">按 Enter 打开</span>
                      </p>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="hero-card-breathing hero-empty">
                <div class="hero-card-inner">
                  <h2 class="hero-label">开始您的创作之旅 ✨</h2>
                  <p class="hero-empty-text">还没有文档？点击下方按钮创建第一个吧！</p>
                  <button class="hero-empty-action" @click.stop="handleCreateDocument">
                    <svg width="18" height="18" viewBox="0 0 16 16" fill="none">
                      <path d="M8 2v12M2 8h12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                    立即创建
                  </button>
                </div>
              </div>
            </section>

            <!-- 统计卡片 -->
            <section class="home-section stats-section" style="--section-delay: 120ms;">
              <div class="stats-grid">
                <div class="stat-card">
                  <div class="stat-icon">📄</div>
                  <div class="stat-content">
                    <div class="stat-value">{{ allDocuments.filter(d => !d.deleted).length }}</div>
                    <div class="stat-label">总文档</div>
                  </div>
                </div>
                <div class="stat-card">
                  <div class="stat-icon">⭐</div>
                  <div class="stat-content">
                    <div class="stat-value">{{ allDocuments.filter(d => d.favorite).length }}</div>
                    <div class="stat-label">收藏</div>
                  </div>
                </div>
                <div class="stat-card">
                  <div class="stat-icon">👥</div>
                  <div class="stat-content">
                    <div class="stat-value">{{ sharedDocuments.length }}</div>
                    <div class="stat-label">共享</div>
                  </div>
                </div>
                <div class="stat-card">
                  <div class="stat-icon">📁</div>
                  <div class="stat-content">
                    <div class="stat-value">{{ spaces.length }}</div>
                    <div class="stat-label">空间</div>
                  </div>
                </div>
              </div>
            </section>

            <!-- 快捷操作 - 2×2 Grid，占50% -->
            <section class="home-section quick-actions" style="--section-delay: 160ms;">
              <div class="section-header">
                <h2 class="section-title">快速操作</h2>
                <span class="section-hint">快速开始，高效创作</span>
              </div>
              <div class="actions-grid-2x2">
                <div class="action-card" @click="handleQuickAction('create')">
                  <div class="action-icon create">
                    <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
                      <path d="M12 4v16M4 12h16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                  </div>
                  <h3 class="action-title">新建文档</h3>
                  <p class="action-desc">从空白开始</p>
                </div>
                <div class="action-card" @click="handleQuickAction('template')">
                  <div class="action-icon template">
                    <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
                      <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2" fill="none"/>
                      <path d="M3 9h18M9 3v18" stroke="currentColor" stroke-width="2"/>
                    </svg>
                  </div>
                  <h3 class="action-title">从模板创建</h3>
                  <p class="action-desc">使用精美模板</p>
                </div>
                <div class="action-card" @click="handleQuickAction('upload')">
                  <div class="action-icon upload">
                    <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
                      <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M17 8l-5-5-5 5M12 3v12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                  </div>
                  <h3 class="action-title">上传文件</h3>
                  <p class="action-desc">拖拽即可上传</p>
                </div>
                <div class="action-card" @click="handleQuickAction('import')">
                  <div class="action-icon import">
                    <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
                      <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                  </div>
                  <h3 class="action-title">导入</h3>
                  <p class="action-desc">批量导入文档</p>
                </div>
              </div>
            </section>

            <!-- 空间列表 - 横向滚动，占100% -->
            <section class="home-section spaces-section" style="--section-delay: 200ms;">
              <div class="section-header">
                <div class="section-title-wrapper">
                  <h2 class="section-title">空间</h2>
                  <span class="section-badge">{{ spaces.length }} 个空间</span>
                </div>
                <button class="section-link" @click="viewAllSpaces">查看全部 →</button>
              </div>
              <div class="spaces-scroll">
                <div class="spaces-scroll-inner">
                  <div 
                    v-for="space in spaces.slice(0, 6)" 
                    :key="space.id"
                    class="space-card"
                    :class="{ 'return-highlight': returningSpaceId === space.id }"
                    @click="enterSpace(space)"
                  >
                    <div class="space-icon" :style="{ background: space.color }">
                      {{ space.icon }}
                    </div>
                  <div class="space-info">
                    <h3 class="space-name">{{ space.name }}</h3>
                    <p class="space-count">
                      <span>{{ space.documentCount }} 个文档</span>
                      <span class="space-badge" v-if="space.documentCount > 10">🔥 热门</span>
                    </p>
                  </div>
                  </div>
                  <div class="space-card create-space" @click="handleCreateSpace">
                    <div class="space-icon create">
                      <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
                        <path d="M12 4v16M4 12h16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                      </svg>
                    </div>
                    <div class="space-info">
                      <h3 class="space-name">创建新空间</h3>
                    </div>
                  </div>
                </div>
              </div>
            </section>

            <!-- 快捷提示 -->
            <section class="home-section tips-section" style="--section-delay: 220ms;">
              <div class="tips-card">
                <div class="tips-icon">💡</div>
                <div class="tips-content">
                  <h4 class="tips-title">小贴士</h4>
                  <p class="tips-text">{{ currentTip }}</p>
                </div>
                <button class="tips-next" @click="nextTip">→</button>
              </div>
            </section>

            <!-- 最近编辑 - 占60% -->
            <section class="home-section recent-docs" style="--section-delay: 260ms;">
              <div class="section-header">
                <h2 class="section-title">最近编辑</h2>
                <button class="section-link" @click="viewAllRecent">查看全部 →</button>
              </div>
              <div v-if="recentDocuments.length === 0" class="empty-state">
                <div class="empty-icon">📝</div>
                <p class="empty-title">还没有最近编辑的文档</p>
                <p class="empty-desc">开始创建您的第一个文档吧！</p>
              </div>
              <div v-else class="docs-list">
                <div 
                  v-for="(doc, index) in recentDocuments.slice(0, 6)" 
                  :key="doc.id"
                  class="doc-item"
                  :style="{ animationDelay: `${index * 0.05}s` }"
                  @click="openDocument(doc)"
                >
                  <div class="doc-highlight"></div>
                  <div class="doc-icon">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                      <rect x="4" y="4" width="16" height="16" rx="2" stroke="currentColor" stroke-width="1.5" fill="none"/>
                      <path d="M8 8h8M8 12h8M8 16h4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                  </div>
                  <div class="doc-info">
                    <h3 class="doc-name">{{ doc.name || '未命名文档' }}</h3>
                    <p class="doc-meta">
                      <span>{{ formatDate(doc.updatedAt || doc.createdAt) }}</span>
                      <span class="doc-badge" v-if="doc.favorite">⭐</span>
                      <span class="doc-badge" v-if="doc.shareLink">🔗</span>
                    </p>
                  </div>
                  <div class="doc-action-hint">点击打开</div>
                </div>
              </div>
            </section>

            <!-- 共享给我 - 占40%，不同展示方式 -->
            <section class="home-section shared-docs" style="--section-delay: 300ms;">
              <div class="section-header">
                <h2 class="section-title">共享给我</h2>
                <button class="section-link" @click="viewAllShared">查看全部 →</button>
              </div>
              <div v-if="sharedDocuments.length === 0" class="empty-state">
                <div class="empty-icon">👥</div>
                <p class="empty-title">还没有共享文档</p>
                <p class="empty-desc">与团队协作，让工作更高效</p>
              </div>
              <div v-else class="shared-list">
                <div 
                  v-for="doc in sharedDocuments.slice(0, 4)" 
                  :key="doc.id"
                  class="shared-item"
                  @click="openDocument(doc)"
                >
                  <div class="shared-indicator"></div>
                  <div class="shared-icon">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                      <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M13 7a4 4 0 1 1-8 0 4 4 0 0 1 8 0z" stroke="currentColor" stroke-width="1.5" fill="none"/>
                    </svg>
                  </div>
                  <div class="shared-info">
                    <div class="shared-header">
                      <h3 class="shared-name">{{ doc.name || '未命名文档' }}</h3>
                      <span class="shared-badge">Shared</span>
                    </div>
                    <p class="shared-meta">{{ formatDate(doc.updatedAt || doc.createdAt) }}</p>
                  </div>
                </div>
              </div>
            </section>
          </div>
        </div>
      </div>
    </div>

    <!-- 浮动提示 -->
    <transition-group name="floating" tag="div" class="floating-tips-container">
      <div
        v-for="tip in floatingTips"
        :key="tip.id"
        class="floating-tip"
        :class="tip.type"
      >
        <span class="floating-icon">{{ tip.type === 'success' ? '✨' : '💡' }}</span>
        <span class="floating-text">{{ tip.message }}</span>
      </div>
    </transition-group>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import SearchBox from './SearchBox.vue'
import { formatDate, formatSizeBytes } from '../utils/format.js'
import { generateDocuments, generateSpaces } from '../utils/mockData.js'

const props = defineProps({
  pagePhase: {
    type: String,
    default: 'home'
  },
  recentSpaceId: {
    type: String,
    default: null
  },
  homeReturnTick: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['enter-space', 'create-document', 'view-document'])

// 响应式数据
const searchQuery = ref('')
const spaces = ref([])
const allDocuments = ref([])
const loading = ref(false)
const currentSubtitle = ref('')
const showTyping = ref(false)
const showWelcomeTip = ref(true)
const currentTipIndex = ref(0)
const floatingTips = ref([])
const returningSpaceId = ref(null)
const isReturnHighlight = ref(false)
let returnTimer = null

const homePhaseClass = computed(() => ({
  'home-phase-base': true,
  'home-phase-leave': props.pagePhase === 'space-enter',
  'home-phase-return': isReturnHighlight.value
}))

// 动态副标题列表
const subtitles = [
  '让想法变成现实 ✨',
  '记录每一个灵感 💡',
  '协作让工作更高效 🚀',
  '知识需要被整理 📚',
  '创作从这里开始 🎨',
  '让文档更有价值 💎',
  '每一次编辑都是进步 📈',
  '让知识流动起来 🌊'
]

// 欢迎消息列表
const welcomeMessages = [
  { title: '欢迎回来！', desc: '今天也要加油创作哦 💪' },
  { title: '新的一天！', desc: '准备好记录今天的想法了吗？' },
  { title: '灵感时刻', desc: '捕捉每一个闪光的瞬间 ✨' },
  { title: '高效工作', desc: '让文档成为你的得力助手 🚀' }
]

// 小贴士列表
const tips = [
  '使用 ⌘K 快速搜索文档和内容',
  '拖拽文件到页面即可快速上传',
  '双击文档卡片可以快速打开',
  '使用标签可以更好地组织文档',
  '创建模板可以节省重复工作',
  '分享文档让团队协作更高效',
  '收藏常用文档，快速访问',
  '使用快捷键 ⌘N 快速创建新文档'
]

const welcomeMessage = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) {
    return { title: '夜深了', desc: '还在工作吗？注意休息哦 🌙' }
  } else if (hour < 12) {
    return { title: '早上好！', desc: '新的一天，新的开始 ☀️' }
  } else if (hour < 18) {
    return { title: '下午好！', desc: '继续加油，高效工作 💼' }
  } else {
    return { title: '晚上好！', desc: '今天过得怎么样？ 🌆' }
  }
})

const currentTip = computed(() => {
  return tips[currentTipIndex.value]
})

const nextTip = () => {
  currentTipIndex.value = (currentTipIndex.value + 1) % tips.length
}

// 显示浮动提示
const showFloatingTip = (message, type = 'info') => {
  const tip = {
    id: Date.now(),
    message,
    type,
    show: true
  }
  floatingTips.value.push(tip)
  setTimeout(() => {
    tip.show = false
    setTimeout(() => {
      const index = floatingTips.value.indexOf(tip)
      if (index > -1) {
        floatingTips.value.splice(index, 1)
      }
    }, 300)
  }, 3000)
}

// 随机显示鼓励消息
const showRandomEncouragement = () => {
  const messages = [
    { text: '做得很棒！继续加油 💪', type: 'success' },
    { text: '你的文档越来越多了 📚', type: 'info' },
    { text: '保持这个节奏！🚀', type: 'success' },
    { text: '创作力爆棚！✨', type: 'success' }
  ]
  const random = messages[Math.floor(Math.random() * messages.length)]
  showFloatingTip(random.text, random.type)
}

let subtitleIndex = 0
let typingTimer = null

// 计算属性
const recentDocuments = computed(() => {
  return allDocuments.value
    .filter(doc => !doc.deleted)
    .sort((a, b) => {
      const timeA = a.updatedAt || a.createdAt || 0
      const timeB = b.updatedAt || b.createdAt || 0
      return timeB - timeA
    })
    .slice(0, 10)
})

const sharedDocuments = computed(() => {
  return allDocuments.value
    .filter(doc => !doc.deleted && (doc.shareLink || doc.collaborators?.length > 0))
    .sort((a, b) => {
      const timeA = a.updatedAt || a.createdAt || 0
      const timeB = b.updatedAt || b.createdAt || 0
      return timeB - timeA
    })
})

const lastEditedDocument = computed(() => {
  const docs = allDocuments.value
    .filter(doc => !doc.deleted)
    .sort((a, b) => {
      const timeA = a.updatedAt || a.createdAt || 0
      const timeB = b.updatedAt || b.createdAt || 0
      return timeB - timeA
    })
  return docs.length > 0 ? docs[0] : null
})

// 方法
const loadData = async () => {
  loading.value = true
  try {
    spaces.value = generateSpaces()
    allDocuments.value = generateDocuments(30)
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

const enterSpace = (space) => {
  emit('enter-space', space.id)
}

const openDocument = (doc) => {
  emit('view-document', doc)
}

const handleCreateDocument = () => {
  emit('create-document')
}

const handleCreateSpace = () => {
  console.log('创建新空间')
}

const handleQuickAction = (action) => {
  switch (action) {
    case 'create':
      handleCreateDocument()
      break
    case 'template':
      console.log('从模板创建')
      break
    case 'upload':
      console.log('上传文件')
      break
    case 'import':
      console.log('导入文档')
      break
  }
}

const viewAllRecent = () => {
  console.log('查看全部最近编辑')
}

const viewAllSpaces = () => {
  console.log('查看全部空间')
}

const viewAllShared = () => {
  console.log('查看全部共享')
}

watch(() => props.homeReturnTick, (tick) => {
  if (!tick || !props.recentSpaceId) return
  returningSpaceId.value = props.recentSpaceId
  isReturnHighlight.value = true
  if (returnTimer) {
    clearTimeout(returnTimer)
  }
  returnTimer = setTimeout(() => {
    returningSpaceId.value = null
    isReturnHighlight.value = false
  }, 1200)
})

// 打字机效果
const typeSubtitle = () => {
  const subtitle = subtitles[subtitleIndex]
  let charIndex = 0
  currentSubtitle.value = ''
  showTyping.value = true
  
  const typeChar = () => {
    if (charIndex < subtitle.length) {
      currentSubtitle.value += subtitle[charIndex]
      charIndex++
      typingTimer = setTimeout(typeChar, 100)
    } else {
      showTyping.value = false
      setTimeout(() => {
        subtitleIndex = (subtitleIndex + 1) % subtitles.length
        typeSubtitle()
      }, 3000)
    }
  }
  
  typeChar()
}

onMounted(() => {
  loadData()
  // 延迟启动打字效果
  setTimeout(() => {
    typeSubtitle()
  }, 500)
  
  // 随机显示鼓励消息
  setTimeout(() => {
    if (Math.random() > 0.5) {
      showRandomEncouragement()
    }
  }, 2000)
  
  // 每30秒随机显示一次鼓励
  setInterval(() => {
    if (Math.random() > 0.7 && allDocuments.value.length > 0) {
      showRandomEncouragement()
    }
  }, 30000)
})

// 清理定时器
onUnmounted(() => {
  if (typingTimer) {
    clearTimeout(typingTimer)
  }
  if (returnTimer) {
    clearTimeout(returnTimer)
    returnTimer = null
  }
})
</script>

<style scoped>
.document-library-home {
  display: flex;
  flex: 1;
  width: 100%;
  height: 100vh;
  background: #f5f5f7;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  opacity: 1;
  transform: translateX(0);
  transition:
    opacity var(--motion-duration-page) var(--motion-ease-enter),
    transform var(--motion-duration-page) var(--motion-ease-enter);
}

.document-library-home.home-phase-leave {
  opacity: 0;
  transform: translateX(-16px);
}

.document-library-home.home-phase-return {
  animation: homeReturnWave var(--motion-duration-slow) var(--motion-ease-enter);
}

@keyframes homeReturnWave {
  0% {
    opacity: 0;
    transform: translateX(-12px);
  }
  60% {
    opacity: 1;
    transform: translateX(0);
  }
  100% {
    opacity: 1;
    transform: translateX(0);
  }
}

.document-library-home.home-phase-base .dl-home-header,
.document-library-home.home-phase-base .home-section {
  opacity: 0;
  transform: translateY(16px);
  animation: homeSectionFade var(--motion-duration-normal) var(--motion-ease-enter);
  animation-delay: var(--section-delay, 0ms);
  animation-fill-mode: both;
}

@keyframes homeSectionFade {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.dl-main-wrapper {
  display: flex;
  flex: 1;
  overflow: hidden;
  min-width: 0;
  height: 100vh;
}

.dl-main {
  flex: 1;
  min-width: 0;
  height: calc(100vh - 24px);
  margin: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #f5f5f7;
}

.dl-home-wrapper {
  flex: 1;
  overflow-y: auto;
  width: 100%;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.dl-home-wrapper::-webkit-scrollbar {
  display: none;
}

/* 顶部标题区 */
.dl-home-header {
  padding: 0;
  background: transparent;
  position: sticky;
  top: 0;
  z-index: 10;
  margin-bottom: 0;
}

.dl-home-header-inner {
  padding: 32px 40px;
  background: linear-gradient(180deg, 
    rgba(255, 255, 255, 0.9) 0%,
    rgba(255, 255, 255, 0.7) 100%);
  backdrop-filter: blur(30px) saturate(180%);
  -webkit-backdrop-filter: blur(30px) saturate(180%);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.dl-home-header-left {
  flex-shrink: 0;
}

.title-wrapper {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.dl-home-title {
  font-size: 28px;
  font-weight: 800;
  background: linear-gradient(135deg, #165dff 0%, #4c7fff 50%, #6b8eff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
  letter-spacing: -0.03em;
  line-height: 1.2;
}

.dl-home-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
  min-height: 20px;
  display: flex;
  align-items: center;
  gap: 2px;
}

.subtitle-text {
  font-weight: 500;
}

.subtitle-text.typing {
  border-right: 2px solid #165dff;
  animation: blink 1s infinite;
}

.cursor {
  display: inline-block;
  width: 2px;
  height: 16px;
  background: #165dff;
  animation: blink 1s infinite;
  margin-left: 2px;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

.dl-home-header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  justify-content: flex-end;
  max-width: 800px;
}

.dl-home-search {
  flex: 1;
  min-width: 280px;
  max-width: 500px;
}

.dl-home-search :deep(.search-wrapper) {
  height: 40px;
  background: rgba(255, 255, 255, 0.9);
  border: 1.5px solid rgba(0, 0, 0, 0.08);
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.dl-home-search :deep(.search-wrapper:hover) {
  border-color: rgba(22, 93, 255, 0.2);
  box-shadow: 0 4px 12px rgba(22, 93, 255, 0.1);
}

.dl-home-search :deep(.search-wrapper:focus-within) {
  background: rgba(255, 255, 255, 1);
  border-color: rgba(22, 93, 255, 0.4);
  box-shadow: 
    0 4px 16px rgba(22, 93, 255, 0.15),
    0 0 0 3px rgba(22, 93, 255, 0.1);
}

.btn-new {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  height: 40px;
  background: linear-gradient(135deg, #165dff 0%, #4c7fff 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 
    0 2px 8px rgba(22, 93, 255, 0.3),
    0 1px 3px rgba(22, 93, 255, 0.2);
  white-space: nowrap;
  flex-shrink: 0;
}

.btn-new:hover {
  background: linear-gradient(135deg, #0f4fd8 0%, #3d6eff 100%);
  transform: translateY(-1px);
  box-shadow: 
    0 4px 12px rgba(22, 93, 255, 0.4),
    0 2px 6px rgba(22, 93, 255, 0.3);
}

.btn-new:active {
  transform: translateY(0);
  box-shadow: 
    0 2px 6px rgba(22, 93, 255, 0.3),
    0 1px 3px rgba(22, 93, 255, 0.2);
}

.btn-new svg {
  flex-shrink: 0;
}

.btn-shortcut {
  font-size: 11px;
  font-weight: 500;
  opacity: 0.7;
  margin-left: 4px;
  padding: 2px 6px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 4px;
}

/* 主内容区 - Grid布局系统 */
.dl-home-main {
  padding: 40px;
  max-width: 1600px;
  margin: 0 auto;
  width: 100%;
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 24px;
  align-items: start;
}

/* 区块通用样式 */
.home-section {
  margin-bottom: 0;
}

/* 欢迎提示卡片 */
.welcome-tip {
  grid-column: 1 / -1;
  animation: slideDown 0.5s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.welcome-card {
  background: linear-gradient(135deg, 
    rgba(139, 92, 246, 0.1) 0%, 
    rgba(167, 139, 250, 0.08) 100%);
  border: 1px solid rgba(139, 92, 246, 0.2);
  border-radius: 16px;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  box-shadow: 
    0 4px 16px rgba(139, 92, 246, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
  will-change: transform;
  transform: translateZ(0);
  contain: layout style paint;
}

.welcome-icon {
  font-size: 32px;
  flex-shrink: 0;
  animation: bounce 2s ease-in-out infinite;
}

.welcome-content {
  flex: 1;
  min-width: 0;
}

.welcome-title {
  font-size: 16px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 4px 0;
}

.welcome-desc {
  font-size: 13px;
  color: #6b7280;
  margin: 0;
}

.welcome-close {
  width: 28px;
  height: 28px;
  border: none;
  background: rgba(255, 255, 255, 0.6);
  color: #6b7280;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  line-height: 1;
  transition: all 0.2s;
  flex-shrink: 0;
  will-change: transform;
  transform: translateZ(0);
}

.welcome-close:hover {
  background: rgba(255, 255, 255, 0.9);
  color: #111827;
  transform: translateZ(0) scale(1.1);
}

/* 统计卡片区域 */
.stats-section {
  grid-column: 5 / -1;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.stat-card {
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.5) 0%, 
    rgba(255, 255, 255, 0.3) 100%);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 
    0 2px 12px rgba(0, 0, 0, 0.04),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  will-change: transform;
  transform: translateZ(0);
  contain: layout style paint;
}

.stat-card:hover {
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.7) 0%, 
    rgba(255, 255, 255, 0.5) 100%);
  transform: translateZ(0) translateY(-4px);
  box-shadow: 
    0 8px 24px rgba(22, 93, 255, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.stat-icon {
  font-size: 32px;
  flex-shrink: 0;
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(22, 93, 255, 0.1);
  border-radius: 12px;
  transition: all 0.3s;
}

.stat-card:hover .stat-icon {
  transform: scale(1.1) rotate(5deg);
  background: rgba(22, 93, 255, 0.15);
}

.stat-content {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 24px;
  font-weight: 800;
  color: #111827;
  margin: 0 0 4px 0;
  line-height: 1.2;
  background: linear-gradient(135deg, #165dff 0%, #4c7fff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
  font-weight: 500;
  margin: 0;
}

/* 小贴士卡片 */
.tips-section {
  grid-column: 1 / -1;
}

.tips-card {
  background: linear-gradient(135deg, 
    rgba(245, 158, 11, 0.08) 0%, 
    rgba(251, 191, 36, 0.06) 100%);
  border: 1px solid rgba(245, 158, 11, 0.2);
  border-radius: 16px;
  padding: 18px 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 
    0 2px 12px rgba(245, 158, 11, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
  will-change: transform;
  transform: translateZ(0);
  contain: layout style paint;
}

.tips-icon {
  font-size: 28px;
  flex-shrink: 0;
  animation: rotate 3s ease-in-out infinite;
}

@keyframes rotate {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(-5deg); }
  75% { transform: rotate(5deg); }
}

.tips-content {
  flex: 1;
  min-width: 0;
}

.tips-title {
  font-size: 13px;
  font-weight: 700;
  color: #f59e0b;
  margin: 0 0 6px 0;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.tips-text {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
  line-height: 1.5;
}

.tips-next {
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  transition: all 0.2s;
  flex-shrink: 0;
  will-change: transform;
  transform: translateZ(0);
}

.tips-next:hover {
  background: rgba(245, 158, 11, 0.2);
  transform: translateZ(0) translateX(2px);
}

/* Hero区 - 蓝色呼吸动画框 */
.hero-section {
  grid-column: 1 / -1;
}

.hero-card-breathing {
  background: linear-gradient(135deg, 
    rgba(22, 93, 255, 0.15) 0%, 
    rgba(76, 127, 255, 0.1) 50%,
    rgba(22, 93, 255, 0.12) 100%);
  border: 2px solid rgba(22, 93, 255, 0.3);
  border-radius: 20px;
  padding: 24px 28px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  animation: breathing 3s ease-in-out infinite;
  box-shadow: 
    0 8px 32px rgba(22, 93, 255, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.3s cubic-bezier(0.4, 0, 0.2, 1), border-color 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  will-change: transform, box-shadow, border-color;
  transform: translateZ(0);
  contain: layout style paint;
}

@keyframes breathing {
  0%, 100% {
    border-color: rgba(22, 93, 255, 0.3);
    box-shadow: 
      0 8px 32px rgba(22, 93, 255, 0.15),
      inset 0 1px 0 rgba(255, 255, 255, 0.2);
    transform: translateZ(0) scale(1);
  }
  50% {
    border-color: rgba(22, 93, 255, 0.5);
    box-shadow: 
      0 12px 40px rgba(22, 93, 255, 0.25),
      inset 0 1px 0 rgba(255, 255, 255, 0.3);
    transform: translateZ(0) scale(1.005);
  }
}

.hero-card-breathing::before {
  content: '';
  position: absolute;
  inset: -2px;
  border-radius: 20px;
  background: linear-gradient(135deg, 
    rgba(22, 93, 255, 0.2) 0%, 
    rgba(76, 127, 255, 0.15) 50%,
    rgba(22, 93, 255, 0.18) 100%);
  opacity: 0;
  animation: pulse 3s ease-in-out infinite;
  z-index: 0;
  will-change: opacity, transform;
  transform: translateZ(0);
  contain: strict;
}

@keyframes pulse {
  0%, 100% {
    opacity: 0;
    transform: translateZ(0) scale3d(1, 1, 1);
  }
  50% {
    opacity: 0.6;
    transform: translateZ(0) scale3d(1.02, 1.02, 1);
  }
}

.hero-card-breathing:hover {
  border-color: rgba(22, 93, 255, 0.6);
  transform: translateZ(0) translateY(-2px);
  box-shadow: 
    0 16px 48px rgba(22, 93, 255, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
  animation: none;
}

.hero-card-breathing:hover::before {
  opacity: 0.8;
  animation: none;
}

.hero-card-inner {
  position: relative;
  z-index: 1;
}

.hero-label-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 8px;
}

.hero-label {
  font-size: 16px;
  font-weight: 700;
  color: #165dff;
  margin: 0;
  letter-spacing: 0.5px;
  text-shadow: 0 2px 4px rgba(22, 93, 255, 0.2);
}

.hero-hint {
  font-size: 12px;
  color: #9ca3af;
  font-weight: 500;
  padding: 4px 10px;
  background: rgba(22, 93, 255, 0.08);
  border-radius: 12px;
  animation: fadeInOut 2s ease-in-out infinite;
}

@keyframes fadeInOut {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 1; }
}

.hero-doc-info {
  display: flex;
  align-items: center;
  gap: 14px;
}

.hero-doc-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #165dff;
  flex-shrink: 0;
  box-shadow: 
    0 4px 12px rgba(22, 93, 255, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  will-change: transform;
  transform: translateZ(0);
  contain: layout style paint;
}

.hero-card-breathing:hover .hero-doc-icon {
  transform: translateZ(0) scale3d(1.1, 1.1, 1) rotate(5deg);
  box-shadow: 
    0 6px 16px rgba(22, 93, 255, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 1);
}

.hero-doc-content {
  flex: 1;
  min-width: 0;
}

.hero-doc-title {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 6px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hero-doc-time {
  font-size: 13px;
  color: #6b7280;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.hero-doc-hint {
  font-size: 11px;
  color: #9ca3af;
  padding: 2px 8px;
  background: rgba(22, 93, 255, 0.08);
  border-radius: 8px;
  font-weight: 500;
}

.hero-card-breathing.hero-empty {
  background: linear-gradient(135deg, 
    rgba(22, 93, 255, 0.1) 0%, 
    rgba(76, 127, 255, 0.08) 100%);
}

.hero-empty-text {
  color: #6b7280;
  font-size: 14px;
  margin: 0 0 16px 0;
  line-height: 1.6;
}

.hero-empty-action {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: linear-gradient(135deg, #165dff 0%, #4c7fff 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 
    0 4px 12px rgba(22, 93, 255, 0.3),
    0 2px 6px rgba(22, 93, 255, 0.2);
  will-change: transform;
  transform: translateZ(0);
}

.hero-empty-action:hover {
  background: linear-gradient(135deg, #0f4fd8 0%, #3d6eff 100%);
  transform: translateZ(0) translateY(-2px);
  box-shadow: 
    0 6px 16px rgba(22, 93, 255, 0.4),
    0 3px 8px rgba(22, 93, 255, 0.3);
}

.hero-empty-action:active {
  transform: translateZ(0) translateY(0);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.section-title-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-badge {
  font-size: 12px;
  font-weight: 600;
  color: #165dff;
  background: rgba(22, 93, 255, 0.1);
  padding: 4px 10px;
  border-radius: 12px;
}

.section-hint {
  font-size: 13px;
  color: #9ca3af;
  font-weight: 500;
}

.section-title {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #165dff 0%, #4c7fff 50%, #6b8eff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
  letter-spacing: -0.01em;
}

.section-link {
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: all 0.2s;
}

.section-link:hover {
  color: #165dff;
  background: rgba(22, 93, 255, 0.08);
}

/* 继续您的工作 - 玻璃拟态增强 */
.continue-card {
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.5) 0%, 
    rgba(255, 255, 255, 0.3) 100%);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 20px;
  padding: 28px;
  display: flex;
  align-items: center;
  gap: 20px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(18px) saturate(180%);
  -webkit-backdrop-filter: blur(18px) saturate(180%);
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.8),
    inset 0 -1px 0 rgba(255, 255, 255, 0.3);
  position: relative;
  overflow: hidden;
}

.continue-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: linear-gradient(135deg, 
    rgba(79, 139, 255, 0.4) 0%, 
    rgba(255, 255, 255, 0) 100%);
  border-radius: 0 2px 2px 0;
}

.continue-card:hover {
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.7) 0%, 
    rgba(255, 255, 255, 0.5) 100%);
  border-color: rgba(255, 255, 255, 0.6);
  transform: translateY(-2px) scale(1.01);
  box-shadow: 
    0 12px 40px rgba(22, 93, 255, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 1),
    inset 0 -1px 0 rgba(255, 255, 255, 0.4),
    0 0 0 1px rgba(22, 93, 255, 0.1);
}

.continue-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #165dff 0%, #4c7fff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.continue-info {
  flex: 1;
  min-width: 0;
}

.continue-title {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 4px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.continue-time {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.continue-arrow {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #165dff;
  flex-shrink: 0;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(22, 93, 255, 0.15);
}

.continue-card:hover .continue-arrow {
  transform: translateX(2px) rotate(5deg);
  background: rgba(255, 255, 255, 1);
  box-shadow: 0 4px 12px rgba(22, 93, 255, 0.2);
}

.continue-empty {
  padding: 40px;
  text-align: center;
  color: #9ca3af;
  font-size: 14px;
}

/* 快速操作 - 缩小宽度 (4列) */
.quick-actions {
  grid-column: 1 / 5;
}

/* 快速操作 - 统一玻璃拟态 */
.actions-grid-2x2 {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 18px;
}

.action-card {
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.5) 0%, 
    rgba(255, 255, 255, 0.3) 100%);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 16px;
  padding: 28px 20px;
  cursor: pointer;
  transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.25s cubic-bezier(0.4, 0, 0.2, 1), background 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  text-align: center;
  box-shadow: 
    0 4px 20px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.7),
    inset 0 -1px 0 rgba(255, 255, 255, 0.2);
  position: relative;
  overflow: hidden;
  min-height: 150px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  will-change: transform;
  transform: translateZ(0);
  contain: layout style paint;
}

.action-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.3) 0%, 
    transparent 50%,
    rgba(255, 255, 255, 0.1) 100%);
  opacity: 0;
  transition: opacity 0.25s;
}

.action-card:hover {
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.7) 0%, 
    rgba(255, 255, 255, 0.5) 100%);
  border-color: rgba(255, 255, 255, 0.6);
  transform: translateZ(0) translateY(-3px) scale3d(1.02, 1.02, 1);
  box-shadow: 
    0 12px 32px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.9),
    inset 0 -1px 0 rgba(255, 255, 255, 0.3);
}

.action-card:hover::before {
  opacity: 1;
}

.action-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 14px;
  color: white;
  transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  position: relative;
  z-index: 1;
  will-change: transform;
  transform: translateZ(0);
  contain: layout style paint;
}

.action-card:hover .action-icon {
  transform: translateZ(0) scale3d(1.1, 1.1, 1) rotate(8deg);
  box-shadow: 
    0 6px 20px rgba(0, 0, 0, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
  animation: actionIconWave 0.4s var(--motion-ease-enter);
}

@keyframes actionIconWave {
  0% {
    transform: scale(1) translateY(0);
  }
  50% {
    transform: scale(1.15) translateY(-3px);
  }
  100% {
    transform: scale(1.05) translateY(0);
  }
}

.action-icon.create {
  background: linear-gradient(135deg, #165dff 0%, #4c7fff 100%);
}

.action-icon.template {
  background: linear-gradient(135deg, #8b5cf6 0%, #a78bfa 100%);
}

.action-icon.upload {
  background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
}

.action-icon.import {
  background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%);
}

.action-title {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 4px 0;
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}

.action-desc {
  font-size: 12px;
  color: #9ca3af;
  margin: 0;
  line-height: 1.3;
}

/* 最近编辑 - 占60% (7列) */
.recent-docs {
  grid-column: 1 / 8;
}

/* 文档列表 - 独立卡片，行间间隔，hover时左侧蓝色竖条 */
.docs-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.doc-item {
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.5) 0%, 
    rgba(255, 255, 255, 0.3) 100%);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 16px;
  padding: 16px 18px;
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(14px) saturate(180%);
  -webkit-backdrop-filter: blur(14px) saturate(180%);
  box-shadow: 
    0 2px 12px rgba(0, 0, 0, 0.04),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  position: relative;
  overflow: hidden;
  animation: fadeInUp 0.4s ease-out backwards;
}

.doc-item:nth-child(1) { animation-delay: 0.05s; }
.doc-item:nth-child(2) { animation-delay: 0.1s; }
.doc-item:nth-child(3) { animation-delay: 0.15s; }
.doc-item:nth-child(4) { animation-delay: 0.2s; }
.doc-item:nth-child(5) { animation-delay: 0.25s; }
.doc-item:nth-child(6) { animation-delay: 0.3s; }

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.doc-highlight {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 0;
  background: linear-gradient(135deg, #165dff 0%, #4c7fff 100%);
  border-radius: 0 2px 2px 0;
  transition: width 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  opacity: 0;
}

.doc-item:hover {
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.75) 0%, 
    rgba(255, 255, 255, 0.55) 100%);
  border-color: rgba(255, 255, 255, 0.6);
  transform: translateY(-2px);
  box-shadow: 
    0 8px 24px rgba(22, 93, 255, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.doc-item:hover .doc-highlight {
  width: 4px;
  opacity: 1;
}

.doc-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, 
    rgba(22, 93, 255, 0.15) 0%, 
    rgba(76, 127, 255, 0.1) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #4c7fff;
  flex-shrink: 0;
  transition: all 0.25s;
}

.doc-item:hover .doc-icon {
  background: linear-gradient(135deg, 
    rgba(22, 93, 255, 0.2) 0%, 
    rgba(76, 127, 255, 0.15) 100%);
  transform: scale(1.05);
}

.doc-info {
  flex: 1;
  min-width: 0;
}

.doc-name {
  font-size: 15px;
  font-weight: 500;
  color: #111827;
  margin: 0 0 2px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.doc-meta {
  font-size: 13px;
  color: #6b7280;
  margin: 0;
}

/* 空间列表 - 横向滚动，占100% */
.spaces-section {
  grid-column: 1 / -1;
}

.spaces-scroll {
  overflow-x: auto;
  overflow-y: hidden;
  scrollbar-width: thin;
  scrollbar-color: rgba(22, 93, 255, 0.2) transparent;
  -webkit-overflow-scrolling: touch;
  padding-bottom: 8px;
}

.spaces-scroll::-webkit-scrollbar {
  height: 6px;
}

.spaces-scroll::-webkit-scrollbar-track {
  background: transparent;
}

.spaces-scroll::-webkit-scrollbar-thumb {
  background: rgba(22, 93, 255, 0.2);
  border-radius: 3px;
}

.spaces-scroll::-webkit-scrollbar-thumb:hover {
  background: rgba(22, 93, 255, 0.3);
}

.spaces-scroll-inner {
  display: inline-flex;
  gap: 16px;
  min-width: min-content;
}

.space-card {
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.5) 0%, 
    rgba(255, 255, 255, 0.3) 100%);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 18px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(16px) saturate(180%);
  -webkit-backdrop-filter: blur(16px) saturate(180%);
  box-shadow: 
    0 4px 20px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.7),
    inset 0 -1px 0 rgba(255, 255, 255, 0.2);
  position: relative;
  overflow: hidden;
  flex-shrink: 0;
  width: 200px;
  min-height: 160px;
  display: flex;
  flex-direction: column;
}

.space-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, 
    rgba(79, 139, 255, 0.1) 0%, 
    rgba(255, 255, 255, 0) 100%);
  opacity: 0;
  transition: opacity 0.25s;
}

.space-card:hover {
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.75) 0%, 
    rgba(255, 255, 255, 0.55) 100%);
  border-color: rgba(255, 255, 255, 0.6);
  transform: translateY(-3px) scale(1.02);
  box-shadow: 
    0 12px 32px rgba(22, 93, 255, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.9),
    inset 0 -1px 0 rgba(255, 255, 255, 0.3);
  animation: none;
}

.space-card:hover::before {
  opacity: 1;
}

.space-card:active {
  transform: translateY(-1px) scale(0.98);
}

.space-card.return-highlight {
  animation: returnPulse 0.8s var(--motion-ease-enter);
}

@keyframes returnPulse {
  0% {
    transform: scale(1);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  }
  40% {
    transform: scale(1.03);
    box-shadow: 0 12px 30px rgba(22, 93, 255, 0.2);
  }
  100% {
    transform: scale(1);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  }
}

.space-card.create-space {
  border: 1.5px dashed rgba(22, 93, 255, 0.4);
  background: linear-gradient(135deg, 
    rgba(22, 93, 255, 0.08) 0%, 
    rgba(76, 127, 255, 0.05) 100%);
  position: relative;
}

.space-card.create-space::after {
  content: '';
  position: absolute;
  inset: -2px;
  border-radius: 18px;
  padding: 2px;
  background: linear-gradient(135deg, 
    rgba(22, 93, 255, 0.3) 0%, 
    rgba(76, 127, 255, 0.2) 100%);
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  opacity: 0;
  transition: opacity 0.25s;
  animation: pulseBorder 2s ease-in-out infinite;
}

@keyframes pulseBorder {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 0.6; }
}

.space-card.create-space:hover {
  border-color: rgba(22, 93, 255, 0.6);
  background: linear-gradient(135deg, 
    rgba(22, 93, 255, 0.12) 0%, 
    rgba(76, 127, 255, 0.08) 100%);
}

.space-card.create-space:hover::after {
  opacity: 1;
}

.space-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
  margin-bottom: 16px;
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  transition: all 0.25s;
  position: relative;
  z-index: 1;
}

.space-card:hover .space-icon {
  transform: scale(1.1);
  box-shadow: 
    0 6px 20px rgba(0, 0, 0, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.space-icon.create {
  background: linear-gradient(135deg, #165dff 0%, #4c7fff 100%);
}

.space-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

.space-name {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 6px 0;
}

.space-count {
  font-size: 13px;
  color: #6b7280;
  margin: 0;
}

/* 共享给我 - 占40% (5列)，不同展示方式 */
.shared-docs {
  grid-column: 8 / -1;
}

.shared-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.shared-item {
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.4) 0%, 
    rgba(255, 255, 255, 0.25) 100%);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 14px;
  padding: 14px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(12px) saturate(180%);
  -webkit-backdrop-filter: blur(12px) saturate(180%);
  box-shadow: 
    0 2px 10px rgba(0, 0, 0, 0.03),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
  position: relative;
}

.shared-indicator {
  position: absolute;
  left: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #165dff;
  opacity: 0;
  transition: opacity 0.25s;
  box-shadow: 0 0 8px rgba(22, 93, 255, 0.5);
}

.shared-item:hover .shared-indicator {
  opacity: 1;
}

.shared-icon {
  width: 36px;
  height: 36px;
  border-radius: 9px;
  background: linear-gradient(135deg, 
    rgba(139, 92, 246, 0.15) 0%, 
    rgba(167, 139, 250, 0.1) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8b5cf6;
  flex-shrink: 0;
}

.shared-info {
  flex: 1;
  min-width: 0;
}

.shared-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.shared-name {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.shared-badge {
  font-size: 11px;
  font-weight: 600;
  color: #8b5cf6;
  background: rgba(139, 92, 246, 0.1);
  padding: 2px 8px;
  border-radius: 4px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  flex-shrink: 0;
}

.shared-meta {
  font-size: 12px;
  color: #6b7280;
  margin: 0;
}

.shared-item:hover {
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.6) 0%, 
    rgba(255, 255, 255, 0.4) 100%);
  transform: translateY(-1px);
  box-shadow: 
    0 4px 16px rgba(139, 92, 246, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.7);
}

.empty-state {
  padding: 48px 40px;
  text-align: center;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.3) 0%, 
    rgba(255, 255, 255, 0.2) 100%);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 8px;
  animation: bounce 2s ease-in-out infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.empty-title {
  color: #6b7280;
  font-size: 16px;
  font-weight: 600;
  margin: 0;
}

.empty-desc {
  color: #9ca3af;
  font-size: 13px;
  margin: 0;
  line-height: 1.5;
}

</style>
