.selected-folder-banner {
  position: relative;
  margin-bottom: 18px;
  padding: 22px 26px;
  border-radius: 24px;
  border: 1px solid rgba(148, 163, 184, 0.3);
  background: rgba(248, 250, 252, 0.9);
  box-shadow: 0 18px 38px rgba(15, 23, 42, 0.12);
  backdrop-filter: blur(20px);
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(200px, 0.6fr) auto;
  gap: 24px;
  align-items: center;
}

.folder-banner-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.folder-banner-icon {
  width: 70px;
  height: 70px;
  border-radius: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow:
    0 14px 30px rgba(99, 102, 241, 0.35),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
}

.folder-banner-text {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}

.banner-chip-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.banner-chip {
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 999px;
  font-weight: 600;
  background: rgba(15, 23, 42, 0.08);
  color: #1f2937;
}

.banner-chip.accent {
  background: rgba(14, 165, 233, 0.2);
  color: #0ea5e9;
}

.folder-banner-title {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.folder-banner-title h3 {
  margin: 0;
  font-size: 20px;
  color: #0f172a;
}

.folder-status-dot {
  font-size: 12px;
  color: #16a34a;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.folder-status-dot::before {
  content: '';
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4ade80, #16a34a);
  box-shadow: 0 0 8px rgba(74, 222, 128, 0.6);
}

.folder-banner-desc {
  margin: 0;
  font-size: 13px;
  color: #475569;
  line-height: 1.5;
}

.folder-banner-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 12px;
  color: #64748b;
}

.folder-banner-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.folder-banner-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.banner-stat {
  padding: 12px 14px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(226, 232, 240, 0.9);
  display: flex;
  flex-direction: column;
  gap: 4px;
  text-align: center;
}

.banner-stat .stat-label {
  font-size: 11px;
  color: #94a3b8;
  letter-spacing: 0.6px;
}

.banner-stat .stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #0f172a;
}

.folder-banner-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.banner-btn {
  border: none;
  border-radius: 999px;
  padding: 10px 18px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: transform var(--motion-duration-fast) var(--motion-ease-soft);
}

.banner-btn.primary {
  background: linear-gradient(135deg, #4f46e5, #7c3aed);
  color: #fff;
  box-shadow: 0 12px 28px rgba(99, 102, 241, 0.35);
}

.banner-btn.ghost {
  border: 1px solid rgba(71, 85, 105, 0.3);
  background: transparent;
  color: #1d4ed8;
}

.banner-btn:hover {
  transform: translateY(-2px);
}

@media (max-width: 1200px) {
  .selected-folder-banner {
    grid-template-columns: 1fr;
  }
  .folder-banner-stats {
    width: 100%;
    grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  }
  .folder-banner-actions {
    align-items: flex-start;
    flex-direction: row;
    flex-wrap: wrap;
  }
}

<template>
  <div class="document-library-page" :class="pageStateClasses">
    <!-- 文档库首页 -->
    <DocumentLibraryHome 
      v-if="!currentSpace"
        :page-phase="pageTransitionPhase"
        :recent-space-id="lastVisitedSpaceId"
        :home-return-tick="homeReturnTick"
      @enter-space="handleEnterSpace"
      @create-document="handleCreateFromHome"
      @view-document="openDocument"
    />
    
    <!-- 空间页面 -->
    <div v-else class="dl-main-wrapper">
      <div class="dl-main" :style="collapseStyleVars">
      <!-- Hero/Header 区域 -->
      <div class="dl-hero-section">
        <div class="dl-hero-content">
          <div class="dl-hero-left">
            <!-- 个人空间选择器 -->
            <!-- 回首页按钮 -->
            <button class="back-to-home-btn" @click="goToHome" title="返回首页">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                  <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" stroke="currentColor" stroke-width="2"
                        fill="none"/>
                <polyline points="9 22 9 12 15 12 15 22" stroke="currentColor" stroke-width="2" fill="none"/>
              </svg>
              <span>首页</span>
            </button>
            <div class="space-selector-modern" @click.stop="toggleSpaceDropdown">
                <div class="space-selector-icon-modern" v-if="currentSpace && currentSpace.color"
                     :style="{ background: currentSpace.color }">
                  <span v-if="currentSpace.icon" class="space-icon-text">{{ currentSpace.icon }}</span>
                  <svg v-else width="24" height="24" viewBox="0 0 20 20" fill="none">
                  <circle cx="10" cy="7" r="3.5" fill="currentColor" opacity="0.9"/>
                    <path d="M5 18c0-3 2.5-6 5-6s5 3 5 6" stroke="currentColor" stroke-width="1.8"
                          stroke-linecap="round" fill="none"/>
                  </svg>
                </div>
                <div class="space-selector-icon-modern" v-else>
                  <svg width="24" height="24" viewBox="0 0 20 20" fill="none">
                    <circle cx="10" cy="7" r="3.5" fill="currentColor" opacity="0.9"/>
                    <path d="M5 18c0-3 2.5-6 5-6s5 3 5 6" stroke="currentColor" stroke-width="1.8"
                          stroke-linecap="round" fill="none"/>
                </svg>
              </div>
              <div class="space-selector-content-modern">
                  <div class="space-selector-name-modern">{{ currentSpace?.name || '个人空间' }}</div>
              </div>
                <svg class="space-selector-arrow-modern" :class="{ expanded: showSpaceDropdown }" width="16" height="16"
                     viewBox="0 0 14 14" fill="none">
                  <path d="M3.5 5.25l3.5 3.5 3.5-3.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"
                        stroke-linejoin="round"/>
              </svg>
              <!-- 空间切换下拉菜单 -->
              <transition name="space-dropdown">
                <div v-if="showSpaceDropdown" class="space-switcher-menu-modern">
                  <div 
                    class="space-switcher-item-modern"
                    :class="{ active: !currentSpace || currentSpace.id === 'all' }"
                    @click="selectSpace({ id: 'all', name: '全部' }, $event)"
                  >
                    <span class="space-switcher-item-text">全部</span>
                      <span class="space-switcher-item-count">{{ currentSpaceDocumentCount }}</span>
                  </div>
                  <div 
                        v-for="(space, index) in spaces"
                    :key="space.id"
                    class="space-switcher-item-modern"
                    :class="{ active: currentSpace?.id === space.id }"
                        :style="{ '--index': index }"
                    @click="selectSpace(space, $event)"
                  >
                    <div class="space-switcher-item-icon" :style="{ background: space.color }">{{ space.icon }}</div>
                    <span class="space-switcher-item-text">{{ space.name }}</span>
                    <span class="space-switcher-item-count">{{ space.documentCount }}</span>
                  </div>
                </div>
              </transition>
            </div>
            
            <!-- 标题和统计 -->
            <div class="dl-hero-title-section">
                <div class="title-row">
              <h1 class="dl-hero-title">{{ currentViewTitle }}</h1>
                </div>
                <div class="dl-hero-stats" v-if="!loading && filteredDocumentsCount > 0">
                <span class="hero-stat-item">
                  {{ filteredDocumentsCount }}个文档
                </span>
                <span class="hero-stat-divider">·</span>
                <span class="hero-stat-hint">可按标签筛选</span>
              </div>
                <!-- 活力提示文字 -->
                <div class="hero-vibrant-tips" v-if="vibrantTips.length > 0">
                  <div class="tips-container">
                  <span
                      v-for="(tip, index) in vibrantTips"
                      :key="`${tip.id}-${tip.text}`"
                      class="vibrant-tip"
                      :style="{ animationDelay: `${index * 0.1}s` }"
                  >
                    <span class="tip-emoji">{{ tip.emoji }}</span>
                    <span class="tip-text">{{ tip.text }}</span>
                  </span>
                  </div>
              </div>
            </div>
          </div>
          
          <div class="dl-hero-right">
            <!-- 主角式搜索框 -->
            <div class="dl-hero-search">
                <SearchBox v-model="searchQuery" placeholder="搜索文档、标签或内容..."/>
            </div>
            
            <!-- 新建按钮 -->
            <button class="btn-modern btn-primary-modern" @click="showCreateDialog = true">
              <svg width="18" height="18" viewBox="0 0 16 16" fill="none">
                <path d="M8 2v12M2 8h12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
              <span>新建</span>
            </button>
          </div>
        </div>
      </div>

      <!-- 工具条区域（筛选、排序、视图切换） -->
      <div class="dl-toolbar-section">
        <div class="dl-toolbar-content">
          <!-- 左侧：筛选按钮组 -->
          <div class="toolbar-group toolbar-group-filters">
            <div class="toolbar-group-label">筛选</div>
            <div class="filter-buttons-modern">
              <div class="filter-slider" :style="filterSliderStyle"></div>
              <button 
                class="filter-btn-modern"
                :class="{ active: spaceSubView === 'all' }"
                @click="switchSpaceSubView('all')"
                data-filter="all"
                ref="filterBtnRefs"
              >
                全部
              </button>
              <button 
                class="filter-btn-modern"
                :class="{ active: spaceSubView === 'recent' }"
                @click="switchSpaceSubView('recent')"
                data-filter="recent"
                ref="filterBtnRefs"
              >
                最近
              </button>
              <button 
                class="filter-btn-modern"
                :class="{ active: spaceSubView === 'favorites' }"
                @click="switchSpaceSubView('favorites')"
                data-filter="favorites"
                ref="filterBtnRefs"
              >
                收藏
              </button>
              <button 
                class="filter-btn-modern"
                :class="{ active: spaceSubView === 'liked' }"
                @click="switchSpaceSubView('liked')"
                data-filter="liked"
                ref="filterBtnRefs"
              >
                点赞
              </button>
              <button 
                class="filter-btn-modern"
                :class="{ active: spaceSubView === 'shared' }"
                @click="switchSpaceSubView('shared')"
                data-filter="shared"
                ref="filterBtnRefs"
              >
                共享
              </button>
            </div>
          </div>
          
          <!-- 右侧：排序和视图切换 -->
          <div class="toolbar-group toolbar-group-actions">
            <div class="sort-wrapper-modern">
                <button class="sort-select-trigger" :class="{ open: showSortMenu }" @click="toggleSortMenu">
                  <span>{{ currentSortLabel }}</span>
                  <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                    <path d="M3.5 5.25L7 8.5l3.5-3.25" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"
                          stroke-linejoin="round"/>
                  </svg>
                </button>
                <transition name="sort-dropdown">
                  <div v-if="showSortMenu" class="sort-dropdown-menu">
                    <button
                        v-for="option in sortOptions"
                        :key="option.value"
                        class="sort-dropdown-item"
                        :class="{ active: sortBy === option.value }"
                        @click="selectSortOption(option.value)"
                    >
                      <span>{{ option.label }}</span>
                      <span class="sort-item-hint" v-if="sortBy === option.value">当前</span>
                    </button>
            </div>
                </transition>
          </div>
              <ViewToggle v-model="viewMode"/>
        </div>
          </div>
          <transition name="bulk-bar">
            <div v-if="hasSelection" class="bulk-selection-bar">
              <div class="bulk-selection-info">
                已选择 {{ selectedDocs.length }} 个文档
              </div>
              <div class="bulk-selection-actions">
                <button class="bulk-action-btn" @click="handleBulkFavorite">
                  <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                    <path d="M7 2l1.5 3 3.5.5-2.5 2.5.5 3.5L7 10.5 4 11.5l.5-3.5L2 5.5l3.5-.5L7 2z"
                          stroke="currentColor" stroke-width="1.2" fill="none"/>
                  </svg>
                  收藏
                </button>
                <button class="bulk-action-btn danger" @click="handleBulkDelete">
                  <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                    <path d="M3 4h8M5 4V3h4v1m-.5 0v7a1 1 0 0 1-1 1h-1a1 1 0 0 1-1-1V4" stroke="currentColor" stroke-width="1.2"
                          stroke-linecap="round"/>
                  </svg>
                  删除
                </button>
                <button class="bulk-action-btn ghost" @click="clearSelection">取消</button>
              </div>
            </div>
          </transition>
      </div>

      <!-- 内容区域（包含目录树和文档列表） -->
      <div class="dl-content-wrapper">
        <!-- 目录树 -->
        <DocumentTree
          :documents="treeDocuments"
          :selected-id="selectedTreeId"
          @select="handleTreeSelect"
          @folder-select="handleFolderSelect"
        />

        <!-- 文档列表 -->
          <div class="dl-content" :class="contentAnimationClass" ref="contentRef">
        <!-- 调试信息（开发时可见） -->
        <!-- <div style="padding: 10px; background: #f0f0f0; margin-bottom: 10px; font-size: 12px;">
          加载中: {{ loading }} | 
          总文档数: {{ allDocuments.length }} | 
          过滤后: {{ sortedAndFilteredDocuments.length }} | 
          当前视图: {{ currentView }} | 
          视图模式: {{ viewMode }}
        </div> -->
        
            <!-- 当前文件夹信息 -->
            <div
              v-if="selectedFolderInfo && !loading"
              class="selected-folder-banner"
            >
              <div class="folder-banner-left">
                <div
                  class="folder-banner-icon"
                  :style="{ background: currentSpace?.color || '#8b5cf6' }"
                >
                  <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                    <path
                      d="M3.5 7.5c0-.552.448-1 1-1H10l1.5 2H19c.552 0 1 .448 1 1V17c0 .552-.448 1-1 1H4.5c-.552 0-1-.448-1-1V7.5Z"
                      stroke="currentColor"
                      stroke-width="1.5"
                      stroke-linejoin="round"
                    />
                    <path
                      d="M3.5 9.5h16"
                      stroke="currentColor"
                      stroke-width="1.5"
                      stroke-linecap="round"
                    />
                  </svg>
                </div>
                <div class="folder-banner-text">
                  <div class="banner-chip-row">
                    <span class="banner-chip">文件夹</span>
                    <span
                      class="banner-chip accent"
                      v-if="selectedFolderInfo.shareLink"
                    >已共享</span>
                  </div>
                  <div class="folder-banner-title">
                    <h3>{{ selectedFolderInfo.name }}</h3>
                    <span class="folder-status-dot">活跃</span>
                  </div>
                  <p class="folder-banner-desc">
                    {{ selectedFolderInfo.description || '暂无文件夹描述' }}
                  </p>
                  <div class="folder-banner-meta">
                    <span>{{ folderDocumentCount }} 个文档</span>
                    <span>创建于 {{ formatDate(selectedFolderInfo.createdAt) }}</span>
                    <span>更新于 {{ formatDate(selectedFolderInfo.updatedAt || selectedFolderInfo.createdAt) }}</span>
                    <span v-if="selectedFolderInfo.owner?.name">由 {{ selectedFolderInfo.owner.name }} 管理</span>
                  </div>
                  <div class="folder-banner-tags" v-if="selectedFolderTags.length">
                    <span
                      v-for="tag in selectedFolderTags"
                      :key="tag"
                      class="folder-tag"
                    >
                      #{{ tag }}
                    </span>
                  </div>
                </div>
              </div>
              <div class="folder-banner-stats">
                <div class="banner-stat">
                  <span class="stat-label">文档数</span>
                  <span class="stat-value">{{ folderDocumentCount }}</span>
                </div>
                <div class="banner-stat">
                  <span class="stat-label">负责人</span>
                  <span class="stat-value">
                    {{ selectedFolderInfo.owner?.name || '未指定' }}
                  </span>
                </div>
                <div class="banner-stat">
                  <span class="stat-label">大小</span>
                  <span class="stat-value">
                    {{ selectedFolderInfo.size ? formatSize(selectedFolderInfo.size) : '—' }}
                  </span>
                </div>
              </div>
              <div class="folder-banner-actions">
                <button
                  class="banner-btn primary"
                  @click="handleFolderCardClick(selectedFolderInfo)"
                >
                  查看该文件夹
                </button>
                <button class="banner-btn ghost" @click="clearFolderSelection">
                  清除筛选
                </button>
              </div>
            </div>

            <div v-if="loading" class="dl-skeleton">
              <div class="skeleton-hero shimmer"></div>
              <div class="skeleton-toolbar shimmer"></div>
              <div class="skeleton-grid">
                <div v-for="n in 8" :key="n" class="skeleton-card shimmer"></div>
              </div>
            </div>

        <!-- 空状态 -->
        <EmptyState 
                v-else-if="filteredDocumentsCount === 0"
          title="暂无文档"
          description="创建第一个文档开始使用"
          :show-button="true"
          button-text="新建文档"
          @action="showCreateDialog = true"
        />

        <!-- 网格视图 -->
            <div v-else-if="viewMode === 'grid'" class="dl-grid-view" :key="'grid-' + contentAnimationKey">
              <div class="grid-container">
          <div 
                    v-for="(doc, index) in paginatedDocuments"
            :key="doc.id"
                    v-memo="[doc.id, doc.name, doc.favorite, doc.updatedAt, doc.isFolder]"
            class="doc-card"
                    :class="{ selected: isDocSelected(doc.id), 'is-folder': doc.isFolder }"
                    :style="{ '--card-index': index }"
            @click="doc.isFolder ? handleFolderCardClick(doc) : openDocument(doc)"
            @contextmenu.prevent="showContextMenu($event, doc)"
          >
                  <button
                      class="card-select-toggle"
                      :class="{ active: isDocSelected(doc.id), visible: hasSelection || isDocSelected(doc.id) }"
                      @click.stop="toggleSelect(doc)"
                  >
                    <span class="card-select-ring"></span>
                    <svg v-if="isDocSelected(doc.id)" width="12" height="12" viewBox="0 0 12 12" fill="none">
                      <path d="M3 6l2 2 4-4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                  </button>
            <div class="doc-card-header">
              <div class="doc-icon" :class="{ 'is-folder': doc.isFolder }">
                <svg v-if="doc.isFolder" width="24" height="24" viewBox="0 0 24 24" fill="none">
                  <path d="M3.5 7.5c0-.552.448-1 1-1H10l1.5 2H19c.552 0 1 .448 1 1V17c0 .552-.448 1-1 1H4.5c-.552 0-1-.448-1-1V7.5Z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
                  <path d="M3.5 9.5h16" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
                <svg v-else width="24" height="24" viewBox="0 0 24 24" fill="none">
                  <rect x="4" y="4" width="16" height="16" rx="2" stroke="currentColor" stroke-width="1.5" fill="none"/>
                  <path d="M8 8h8M8 12h8M8 16h4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
              </div>
              <div class="doc-actions" :class="{ 'folder-actions': doc.isFolder }">
                <template v-if="doc.isFolder">
                  <button class="folder-enter-btn" @click.stop="handleFolderCardClick(doc)">
                    查看文件夹
                    <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                      <path d="M3 6h6M6 3l3 3-3 3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                  </button>
                  <button 
                    class="doc-action-btn"
                    @click.stop="showDocMenu($event, doc)"
                    title="更多"
                  >
                    <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                      <circle cx="7" cy="3.5" r="1.5" fill="currentColor"/>
                      <circle cx="7" cy="7" r="1.5" fill="currentColor"/>
                      <circle cx="7" cy="10.5" r="1.5" fill="currentColor"/>
                    </svg>
                  </button>
                </template>
                <template v-else>
                <button 
                  class="doc-action-btn"
                  :class="{ active: doc.favorite }"
                  @click.stop="toggleFavorite(doc)"
                  title="收藏"
                >
                  <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                    <path d="M7 2l1.5 3 3.5.5-2.5 2.5.5 3.5L7 10.5 4 11.5l.5-3.5L2 5.5l3.5-.5L7 2z" 
                          :fill="doc.favorite ? 'currentColor' : 'none'" 
                          stroke="currentColor" 
                          stroke-width="1.5"/>
                  </svg>
                </button>
                <button 
                  class="doc-action-btn"
                  @click.stop="showDocMenu($event, doc)"
                  title="更多"
                >
                  <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                    <circle cx="7" cy="3.5" r="1.5" fill="currentColor"/>
                    <circle cx="7" cy="7" r="1.5" fill="currentColor"/>
                    <circle cx="7" cy="10.5" r="1.5" fill="currentColor"/>
                  </svg>
                </button>
                </template>
              </div>
            </div>
            <div class="doc-card-body" :class="{ 'folder-card-body': doc.isFolder }">
              <template v-if="doc.isFolder">
                <div class="folder-card-top">
                  <span class="folder-chip-badge">文件夹</span>
                  <span class="folder-card-count">{{ folderChildCountMap[doc.id] || 0 }} 个文档</span>
                </div>
                <h3 class="doc-title">{{ doc.name || '未命名文件夹' }}</h3>
                <p class="doc-desc">{{ doc.description || '暂无文件夹描述' }}</p>
                <div class="folder-card-meta">
                  <span>更新于 {{ formatDate(doc.updatedAt || doc.createdAt) }}</span>
                  <span v-if="doc.owner?.name">由 {{ doc.owner.name }} 管理</span>
                </div>
              </template>
              <template v-else>
              <h3 class="doc-title">{{ doc.name || '未命名文档' }}</h3>
              <p class="doc-desc" v-if="doc.description">{{ doc.description }}</p>
              
              <!-- 协作者和分享信息 -->
                      <div class="doc-collaborators"
                           v-if="(doc.collaborators && doc.collaborators.length > 0) || doc.shareLink">
                        <div class="doc-collaborators-list" v-if="doc.collaborators && doc.collaborators.length > 0">
                          <template v-for="(collab, idx) in (doc.collaborators || []).slice(0, 3)" :key="collab.user.id">
                            <div
                    class="doc-collaborator-avatar"
                    :title="collab.user.name"
                    :style="{ zIndex: 10 - idx, marginLeft: idx > 0 ? '-8px' : '0' }"
                  >
                    {{ collab.user.avatar }}
                  </div>
                          </template>
                          <span v-if="doc.collaborators && doc.collaborators.length > 3" class="doc-collaborator-more">
                    +{{ doc.collaborators.length - 3 }}
                  </span>
                </div>
                <div class="doc-share-badge" v-if="doc.shareLink">
                  <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                    <path d="M6 1L1 3v4l5 2 5-2V3L6 1z" stroke="currentColor" stroke-width="1.5" fill="none"/>
                  </svg>
                  已分享
                </div>
              </div>
              
              <div class="doc-meta">
                <span class="doc-time">{{ formatDate(doc.updatedAt || doc.createdAt) }}</span>
                <span class="doc-size" v-if="doc.size">{{ formatSize(doc.size) }}</span>
                <span class="doc-likes" v-if="doc.likeCount > 0">
                  <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                    <path d="M6 1l1 1 3 .5-2 2 .5 3L6 8 3 7.5l.5-3-2-2 3-.5L6 1z" 
                          :fill="doc.liked ? 'currentColor' : 'none'"
                          stroke="currentColor" 
                          stroke-width="1.5"/>
                  </svg>
                  {{ doc.likeCount }}
                </span>
              </div>
              </template>
            </div>
            <div class="doc-card-footer" :class="{ 'folder-card-footer': doc.isFolder }">
              <template v-if="doc.isFolder">
                <button class="folder-link" @click.stop="handleFolderCardClick(doc)">
                  进入文件夹
                  <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                    <path d="M4 3l4 3-4 3" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </button>
              </template>
              <template v-else>
                <div class="doc-meta">
                  <span>{{ formatDate(doc.updatedAt || doc.createdAt) }}</span>
                  <span v-if="doc.size">{{ formatSize(doc.size) }}</span>
                </div>
                <div class="doc-tags" v-if="doc.tags && doc.tags.length">
                  <span v-for="tag in doc.tags" :key="tag">{{ tag }}</span>
                </div>
              </template>
            </div>
            </div>
          </div>
        </div>

        <!-- 列表视图 -->
            <div v-else class="dl-list-view" :key="'list-' + contentAnimationKey">
          <table class="doc-table">
            <thead>
              <tr>
                <th class="col-check">
                  <input 
                    type="checkbox" 
                        :checked="selectedDocs.length === filteredDocumentsCount && filteredDocumentsCount > 0"
                    @change="toggleSelectAll"
                  />
                </th>
                <th class="col-name">名称</th>
                <th class="col-owner">所有者</th>
                <th class="col-updated">更新时间</th>
                <th class="col-size">大小</th>
                <th class="col-actions">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr 
                    v-for="(doc, index) in paginatedDocuments"
                :key="doc.id"
                    v-memo="[doc.id, doc.name, doc.favorite, doc.updatedAt, isDocSelected(doc.id)]"
                class="doc-row"
                    :class="{ selected: isDocSelected(doc.id), 'is-folder': doc.isFolder }"
                    :style="{ '--card-index': index }"
                @click="toggleSelect(doc)"
                @dblclick="doc.isFolder ? handleFolderCardClick(doc) : openDocument(doc)"
                @contextmenu.prevent="showContextMenu($event, doc)"
              >
                <td class="col-check">
                  <input 
                    type="checkbox" 
                        :checked="isDocSelected(doc.id)"
                    @change.stop="toggleSelect(doc)"
                  />
                </td>
                <td class="col-name">
                  <div class="doc-name-cell">
                    <div class="doc-icon-small">
                      <svg v-if="doc.isFolder" width="20" height="20" viewBox="0 0 24 24" fill="none">
                        <path d="M3.5 7.5c0-.552.448-1 1-1H10l1.5 2H19c.552 0 1 .448 1 1V17c0 .552-.448 1-1 1H4.5c-.552 0-1-.448-1-1V7.5Z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
                        <path d="M3.5 9.5h16" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                      </svg>
                      <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none">
                          <rect x="4" y="4" width="16" height="16" rx="2" stroke="currentColor" stroke-width="1.5"
                                fill="none"/>
                          <path d="M8 8h8M8 12h8M8 16h4" stroke="currentColor" stroke-width="1.5"
                                stroke-linecap="round"/>
                      </svg>
                    </div>
                    <span class="doc-name">{{ doc.name || '未命名文档' }}</span>
                    <button 
                      v-if="doc.favorite"
                      class="favorite-badge"
                      @click.stop="toggleFavorite(doc)"
                    >
                      <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                        <path d="M6 1.5l1.5 3 3.5.5-2.5 2.5.5 3.5L6 9.5 3 10.5l.5-3.5L1 4.5l3.5-.5L6 1.5z" 
                              fill="currentColor"/>
                      </svg>
                    </button>
                  </div>
                </td>
                <td class="col-owner">
                  <div class="owner-cell">
                    <div class="owner-avatar">{{ getOwnerInitial(doc.owner) }}</div>
                    <span>{{ doc.owner?.name || '未知' }}</span>
                  </div>
                </td>
                <td class="col-updated">{{ formatDate(doc.updatedAt || doc.createdAt) }}</td>
                <td class="col-size">{{ doc.size ? formatSize(doc.size) : '-' }}</td>
                <td class="col-actions">
                  <button 
                    class="action-btn"
                    @click.stop="showDocMenu($event, doc)"
                  >
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                      <circle cx="8" cy="3" r="1.5" fill="currentColor"/>
                      <circle cx="8" cy="8" r="1.5" fill="currentColor"/>
                      <circle cx="8" cy="13" r="1.5" fill="currentColor"/>
                    </svg>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

            <!-- 分页控件 -->
            <div v-if="totalPages > 1" class="pagination-wrapper">
              <button
                  class="pagination-btn"
                  :disabled="currentPage === 1"
                  @click="currentPage = Math.max(1, currentPage - 1)"
              >
                上一页
              </button>
              <span class="pagination-info">
            第 {{ currentPage }} / {{ totalPages }} 页（共 {{ filteredDocumentsCount }} 条）
          </span>
              <button
                  class="pagination-btn"
                  :disabled="currentPage >= totalPages"
                  @click="currentPage = Math.min(totalPages, currentPage + 1)"
              >
                下一页
              </button>
        </div>
      </div>
        </div>
      </div>
      <div v-if="floatingMenuVisible" class="floating-tool-stack">
        <button class="floating-tool-btn primary" @click="showCreateDialog = true">
          <svg width="18" height="18" viewBox="0 0 16 16" fill="none">
            <path d="M8 2v12M2 8h12" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
          </svg>
          新建
        </button>
        <button class="floating-tool-btn" @click="handleFloatingFeedback">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <path d="M8 2a5 5 0 0 0-5 5c0 1.8.9 3.4 2.4 4.3v2.7l2.3-1.4c.1 0 .2.1.3.1a5 5 0 0 0 0-10Z" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          反馈
        </button>
        <button
            class="back-to-top-btn"
            :class="{ visible: showBackToTop, pulse: backToTopPulse }"
            @click="scrollToTop"
            aria-label="回到顶部"
        >
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <path d="M8 3l4 4M8 3 4 7M8 3v10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </button>
      </div>
    </div>

    <!-- 新建文档对话框 -->
    <div v-if="showCreateDialog" class="dialog-overlay" @click="showCreateDialog = false">
      <div class="dialog create-doc-dialog" @click.stop>
        <div class="dialog-header">
          <h3 class="dialog-title">新建文档</h3>
          <button class="dialog-close" @click="closeCreateDialog">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M4 4l8 8M12 4l-8 8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
          </button>
        </div>
        <div class="dialog-content">
          <div class="form-group">
            <label class="form-label">文档名称</label>
            <input
              v-model="createDocName"
              type="text"
              class="form-input"
              placeholder="输入文档名称"
              @keyup.enter="handleCreateDocument"
              ref="createNameInputRef"
            />
          </div>
          
          <div class="form-group">
            <label class="form-label">文档类型</label>
            <div class="type-options">
              <button
                :class="['type-option', { active: createDocType === 'text' }]"
                @click="createDocType = 'text'"
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                  <rect x="4" y="4" width="16" height="16" rx="2" stroke="currentColor" stroke-width="1.5" fill="none"/>
                  <path d="M8 8h8M8 12h8M8 16h4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
                文本文档
              </button>
              <button
                :class="['type-option', { active: createDocType === 'markdown' }]"
                @click="createDocType = 'markdown'"
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                  <rect x="4" y="4" width="16" height="16" rx="2" stroke="currentColor" stroke-width="1.5" fill="none"/>
                  <path d="M8 8h8M8 12h8M8 16h12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
                Markdown
              </button>
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">文档内容</label>
            <textarea
              v-model="createDocContent"
              class="form-textarea"
              placeholder="输入文档内容（可选）"
              rows="8"
              ref="createContentInputRef"
            ></textarea>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-secondary" @click="closeCreateDialog">取消</button>
          <button class="btn btn-primary" @click="handleCreateDocument">创建</button>
        </div>
      </div>
    </div>

    <!-- 右键菜单 -->
    <div 
      v-if="contextMenu.show"
      class="context-menu"
      :style="{ top: contextMenu.y + 'px', left: contextMenu.x + 'px' }"
      @click.stop
    >
      <div class="context-menu-item" @click="handleContextAction('open')">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <path d="M8 2v12M2 8h12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
        打开
      </div>
      <div class="context-menu-item" @click="handleContextAction('rename')">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <path d="M8 2l6 6-6 6M2 2l6 6-6 6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
        重命名
      </div>
      <div class="context-menu-item" @click="handleContextAction('duplicate')">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <rect x="4" y="4" width="8" height="8" rx="1" stroke="currentColor" stroke-width="1.5" fill="none"/>
          <path d="M8 2v2M8 12v2M2 8h2M12 8h2" stroke="currentColor" stroke-width="1.5"/>
        </svg>
        复制
      </div>
      <div class="context-menu-divider"></div>
      <div class="context-menu-item" @click="handleContextAction('share')">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <path d="M8 2L2 5v6l6 3 6-3V5l-6-3z" stroke="currentColor" stroke-width="1.5" fill="none"/>
        </svg>
        分享
      </div>
      <div class="context-menu-item" @click="handleContextAction('favorite')">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <path d="M8 2l2 4 4.5.5-3.5 3.5 1 4.5L8 12.5 4 14.5l1-4.5L1.5 6.5 6 6l2-4z" 
                :fill="contextMenu.doc?.favorite ? 'currentColor' : 'none'" 
                stroke="currentColor" 
                stroke-width="1.5"/>
        </svg>
        {{ contextMenu.doc?.favorite ? '取消收藏' : '收藏' }}
      </div>
      <div class="context-menu-divider"></div>
      <div class="context-menu-item danger" @click="handleContextAction('delete')">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <path d="M4 4l8 8M12 4l-8 8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
        删除
      </div>
    </div>

    <!-- 重命名对话框 -->
    <div v-if="showRenameDialog" class="dialog-overlay" @click="showRenameDialog = false">
      <div class="dialog dialog-small" @click.stop>
        <div class="dialog-header">
          <h3 class="dialog-title">重命名</h3>
          <button class="dialog-close" @click="showRenameDialog = false">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M4 4l8 8M12 4l-8 8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
          </button>
        </div>
        <div class="dialog-content">
          <input 
            v-model="renameValue"
            type="text"
            class="rename-input"
            placeholder="输入新名称"
            @keyup.enter="confirmRename"
            @keyup.esc="showRenameDialog = false"
            ref="renameInputRef"
          />
        </div>
        <div class="dialog-footer">
          <button class="btn btn-secondary" @click="showRenameDialog = false">取消</button>
          <button class="btn btn-primary" @click="confirmRename">确定</button>
        </div>
      </div>
    </div>

    <!-- 消息提示 Toast -->
    <MessageToast
      :visible="showToast"
      :message="toastMessage"
      :type="toastType"
      @close="showToast = false"
    />

    <!-- 错误对话框 -->
    <ErrorDialog
      :visible="showErrorDialog"
      :title="errorDialogTitle"
      :message="errorDialogMessage"
      @close="showErrorDialog = false"
    />

    <!-- 分享对话框 -->
    <ShareDialog
      :visible="showShareDialog"
      :document="shareDocument"
      @update:visible="showShareDialog = $event"
      @save="handleShareSave"
    />

    <!-- 文档编辑器（使用 Teleport 全屏显示） -->
    <DocumentEditorPage
      v-if="editingDocument"
      :document-id="editingDocument.id"
      :document="editingDocument"
      @close="closeEditor"
      @update="handleDocumentUpdate"
      @save="handleDocumentSave"
    />
  </div>
</template>

<script setup>
import {computed, nextTick, onMounted, onUnmounted, ref, shallowRef, watch} from 'vue'
import SearchBox from './SearchBox.vue'
import ViewToggle from './ViewToggle.vue'
import EmptyState from './EmptyState.vue'
import MessageToast from './MessageToast.vue'
import ErrorDialog from './ErrorDialog.vue'
import ShareDialog from './ShareDialog.vue'
import DocumentTree from './DocumentTree.vue'
import DocumentEditorPage from './DocumentEditorPage.vue'
import DocumentLibraryHome from './DocumentLibraryHome.vue'
import {formatDate, formatSizeBytes} from '../utils/format.js'
import {useSession} from '../composables/useSession.js'
import {generateCalendarEvents, generateDocuments, generateSpaces, generateTasks} from '../utils/mockData.js'

// Props: 接收外部传入的空间ID或视图类型
const props = defineProps({
  spaceId: {
    type: String,
    default: null
  },
  viewType: {
    type: String,
    default: null
  }
})

// 会话管理
const session = useSession()

// 响应式数据
const currentView = ref('home') // home, recent, favorites, liked, shared, knowledge, tasks, calendar, trash
const currentSpace = ref(null) // null 表示显示首页，有值时显示空间页面
const pageTransitionPhase = ref('home') // home, space-enter, space-active, space-leave
const lastVisitedSpaceId = ref(null)
const homeReturnTick = ref(0)
let pagePhaseTimer = null
let pendingSpace = null
const PAGE_PHASE_DURATION = 360
const spaceSubView = ref('all') // all, recent, favorites, liked, shared (空间子视图)
const spaces = ref([])
const showSpaces = ref(false)
const showSpaceDropdown = ref(false)
const documents = ref([])
// 使用 shallowRef 减少深度响应式追踪，提高大数据量时的性能
const allDocuments = shallowRef([]) // 所有文档（包括假数据）
const tasks = ref([])
const calendarEvents = ref([])
const loading = ref(false)
const searchQuery = ref('')
const sortBy = ref('time') // time, name, size
const viewMode = ref('grid') // grid, list
const selectedDocs = ref([])
// 使用 Set 缓存选中状态，避免频繁的 includes 调用导致重新渲染
const selectedDocsSet = computed(() => new Set(selectedDocs.value))
const selectedTreeId = ref(null)
const selectedFolderId = ref(null)
const contentAnimationKey = ref(0)
const contentAnimationReason = ref('initial')
const showSortMenu = ref(false)
const contentRef = ref(null)
const headerCollapseProgress = ref(0)
const showBackToTop = ref(false)
const backToTopPulse = ref(false)
const floatingMenuVisible = computed(() => !!currentSpace.value)
let scrollRaf = null
let collapseRafId = null
let collapseTarget = 0
const selectedFolderInfo = computed(() => {
  if (!selectedFolderId.value) return null
  return allDocuments.value.find(doc => doc.isFolder && doc.id === selectedFolderId.value) || null
})

const folderDocumentCount = computed(() => {
  if (!selectedFolderId.value) return 0
  return allDocuments.value.filter(doc =>
    !doc.deleted &&
    !doc.isFolder &&
    doc.parentId === selectedFolderId.value
  ).length
})

const selectedFolderTags = computed(() => {
  if (!selectedFolderInfo.value) return []
  const tags = selectedFolderInfo.value.tags
  return Array.isArray(tags) ? tags.filter(Boolean) : []
})

const folderChildCountMap = computed(() => {
  const map = Object.create(null)
  allDocuments.value.forEach(doc => {
    if (!doc.deleted && !doc.isFolder && doc.parentId) {
      map[doc.parentId] = (map[doc.parentId] || 0) + 1
    }
  })
  return map
})
const sortOptions = [
  {value: 'time', label: '按时间'},
  {value: 'name', label: '按名称'},
  {value: 'size', label: '按大小'}
]

const pageStateClasses = computed(() => ({
  'state-home': pageTransitionPhase.value === 'home',
  'state-entering-space': pageTransitionPhase.value === 'space-enter',
  'state-space-active': pageTransitionPhase.value === 'space-active',
  'state-leaving-space': pageTransitionPhase.value === 'space-leave'
}))

const contentAnimationClass = computed(() => `refresh-${contentAnimationReason.value}`)
const hasSelection = computed(() => selectedDocs.value.length > 0)
const currentSortLabel = computed(() => {
  const match = sortOptions.find(opt => opt.value === sortBy.value)
  return match ? match.label : '按时间'
})

const collapseStyleVars = computed(() => ({
  '--collapse-progress': headerCollapseProgress.value
}))

const triggerContentAnimation = (reason = 'default') => {
  contentAnimationReason.value = reason
  contentAnimationKey.value = Date.now()
}

const toggleSortMenu = () => {
  showSortMenu.value = !showSortMenu.value
}

const selectSortOption = (value) => {
  if (sortBy.value !== value) {
    sortBy.value = value
    triggerContentAnimation('sort')
  }
  showSortMenu.value = false
}

const clearSelection = () => {
  selectedDocs.value = []
}

const handleBulkFavorite = () => {
  selectedDocs.value.forEach((id) => {
    const doc = allDocuments.value.find(d => d.id === id)
    if (doc) {
      doc.favorite = true
    }
  })
  triggerContentAnimation('bulk')
}

const handleBulkDelete = () => {
  selectedDocs.value.forEach((id) => {
    const doc = allDocuments.value.find(d => d.id === id)
    if (doc) {
      doc.deleted = true
    }
  })
  selectedDocs.value = []
  triggerContentAnimation('bulk')
}

const clearFolderSelection = () => {
  selectedFolderId.value = null
}

const animateCollapse = () => {
  const current = headerCollapseProgress.value
  const diff = collapseTarget - current
  if (Math.abs(diff) < 0.003) {
    headerCollapseProgress.value = collapseTarget
    collapseRafId = null
    return
  }
  headerCollapseProgress.value = current + diff * 0.25
  collapseRafId = requestAnimationFrame(animateCollapse)
}

const setCollapseTarget = (value) => {
  collapseTarget = value
  if (!collapseRafId) {
    collapseRafId = requestAnimationFrame(animateCollapse)
  }
}

const updateScrollState = () => {
  if (!contentRef.value) {
    return
  }
  const top = contentRef.value.scrollTop
  const progress = Math.min(top / 80, 1)
  setCollapseTarget(progress)
  showBackToTop.value = top > 400
}

const handleContentScroll = () => {
  if (scrollRaf) {
    cancelAnimationFrame(scrollRaf)
  }
  scrollRaf = requestAnimationFrame(() => {
    updateScrollState()
    scrollRaf = null
  })
}

const attachScrollListener = () => {
  if (!contentRef.value) {
    return
  }
  contentRef.value.addEventListener('scroll', handleContentScroll, {passive: true})
  updateScrollState()
}

const detachScrollListener = () => {
  if (contentRef.value) {
    contentRef.value.removeEventListener('scroll', handleContentScroll)
  }
  if (scrollRaf) {
    cancelAnimationFrame(scrollRaf)
    scrollRaf = null
  }
  if (collapseRafId) {
    cancelAnimationFrame(collapseRafId)
    collapseRafId = null
  }
}

const scrollToTop = () => {
  if (!contentRef.value) {
    return
  }
  backToTopPulse.value = true
  contentRef.value.scrollTo({top: 0, behavior: 'smooth'})
  setTimeout(() => {
    backToTopPulse.value = false
  }, 400)
}

const handleFloatingFeedback = () => {
  showMessage('已收到你的反馈，感谢支持 ✨', 'info')
}

// 分页相关
const currentPage = ref(1)
const pageSize = ref(20) // 每页显示20条

const showCreateDialog = ref(false)
const showRenameDialog = ref(false)
const showShareDialog = ref(false)
const shareDocument = ref(null)
const renameValue = ref('')
const renameInputRef = ref(null)
const editingDocument = ref(null)
const createDocName = ref('')
const createDocContent = ref('')
const createDocType = ref('text') // 'text' or 'markdown'
const createNameInputRef = ref(null)
const createContentInputRef = ref(null)
const contextMenu = ref({
  show: false,
  x: 0,
  y: 0,
  doc: null
})

// 消息提示相关
const showToast = ref(false)
const toastMessage = ref('')
const toastType = ref('info') // 'info', 'success'

// 活力提示文字
const vibrantTips = ref([])
const tipMessages = [
  {emoji: '🚀', text: '让想法起飞'},
  {emoji: '✨', text: '灵感正在闪烁'},
  {emoji: '💡', text: '创意无限'},
  {emoji: '🎯', text: '专注创作'},
  {emoji: '🔥', text: '保持热情'},
  {emoji: '⭐', text: '记录每一个闪光点'},
  {emoji: '🌈', text: '让知识多彩'},
  {emoji: '🎨', text: '创作从这里开始'}
]

// 移除动画缓存，不再使用进入动画

// 错误对话框相关
const showErrorDialog = ref(false)
const errorDialogTitle = ref('操作失败')
const errorDialogMessage = ref('')

// 获取userId（返回字符串）
const getUserId = () => {
  const userIdStr = session.userId?.value
  if (!userIdStr) return '1'
  return userIdStr
}

// 计算属性
const userInitial = computed(() => {
  return 'U'
})

const currentViewTitle = computed(() => {
  if (currentView.value === 'trash') {
    return '回收站'
  }
  
  // 文档库视图：显示空间和子视图
  if (currentView.value === 'home') {
    const subViewTitles = {
      all: '全部',
      recent: '最近',
      favorites: '收藏',
      liked: '点赞',
      shared: '共享'
    }
    
    if (currentSpace.value && currentSpace.value.id !== 'all') {
      return `${currentSpace.value.name}·${subViewTitles[spaceSubView.value] || '全部'}`
    } else {
      // 默认显示个人空间
      return `个人空间·${subViewTitles[spaceSubView.value] || '全部'}`
    }
  }
  
  return '文档库'
})

// 当前空间的文档数量（数据已经由后端过滤好，直接使用长度）
const currentSpaceDocumentCount = computed(() => {
  return allDocuments.value.length
})

// 统计数据 - 数据已经由后端提供，直接使用长度（如果后端提供统计信息的话）
// 如果后端不提供统计，这些计算属性可以返回固定值或从后端获取
const recentCount = computed(() => {
  // 后端应该提供统计信息，这里暂时返回0或从后端数据获取
  return 0
})

const favoriteCount = computed(() => {
  return 0
})

const likedCount = computed(() => {
  return 0
})

const sharedCount = computed(() => {
  return 0
})

const taskCount = computed(() => {
  return tasks.value.filter(task => task.status !== 'done').length
})

const trashCount = computed(() => {
  return 0
})

// 筛选按钮滑动指示器样式
const filterSliderStyle = computed(() => {
  const filterOptions = ['all', 'recent', 'favorites', 'liked', 'shared']
  const currentIndex = filterOptions.indexOf(spaceSubView.value)
  const index = currentIndex >= 0 ? currentIndex : 0
  
  return {
    transform: `translateX(${index * 100}%)`,
    width: `${100 / filterOptions.length}%`
  }
})

// 目录树使用的文档列表（数据已经由后端过滤好，直接使用）
const treeDocuments = computed(() => {
  return allDocuments.value
})

// 移除时间缓存，因为数据已经由后端处理

// 移除缓存机制，因为数据已经由后端处理，不需要复杂缓存

// 数据已经由后端完全处理（过滤、排序、搜索），前端直接使用，不做任何处理
const sortedAndFilteredDocuments = computed(() => {
  // 直接返回后端提供的数据，不做任何过滤、排序或搜索
  return allDocuments.value
})

// 缓存文档数量，避免频繁访问 length 属性
const filteredDocumentsCount = computed(() => sortedAndFilteredDocuments.value.length)

// 总页数
const totalPages = computed(() => Math.ceil(filteredDocumentsCount.value / pageSize.value))

// 分页后的文档列表（使用 ref 而不是 computed，减少依赖追踪）
const paginatedDocuments = ref([])

// 更新分页数据（只在必要时调用）
// 使用 requestAnimationFrame 延迟更新，减少频繁渲染
let updateTimer = null
const updatePaginatedDocuments = () => {
  if (updateTimer) {
    cancelAnimationFrame(updateTimer)
  }
  updateTimer = requestAnimationFrame(() => {
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    paginatedDocuments.value = sortedAndFilteredDocuments.value.slice(start, end)
    updateTimer = null
  })
}

// 监听分页相关变化，更新分页数据
// 使用 flush: 'post' 和 deep: false 减少不必要的更新
watch([sortedAndFilteredDocuments, currentPage], () => {
  updatePaginatedDocuments()
}, { immediate: true, flush: 'post' })

// 更新活力提示（只在切换空间时更新，不受筛选影响）
let updateTipsTimer = null
let lastSpaceId = null

const updateVibrantTips = () => {
  // 清除之前的定时器
  if (updateTipsTimer) {
    clearTimeout(updateTipsTimer)
  }

  // 使用防抖，避免频繁更新
  updateTipsTimer = setTimeout(() => {
    if (!currentSpace.value) {
      if (vibrantTips.value.length > 0) {
        vibrantTips.value = []
      }
      lastSpaceId = null
      return
    }

    const currentSpaceId = currentSpace.value?.id

    // 只在空间真正变化时才更新
    if (currentSpaceId === lastSpaceId) {
      return
    }

    lastSpaceId = currentSpaceId

    // 数据已经由后端过滤好，直接使用长度
    const spaceDocCount = allDocuments.value.length

    const tips = []

    // 根据空间的文档总数显示不同的提示
    if (spaceDocCount === 0) {
      tips.push({id: 1, emoji: '🎉', text: '新空间，新开始！'})
      tips.push({id: 2, emoji: '✨', text: '创建第一个文档吧'})
    } else if (spaceDocCount < 5) {
      tips.push({id: 1, emoji: '🌱', text: '正在成长中'})
      tips.push({id: 2, emoji: '💪', text: '继续加油'})
    } else if (spaceDocCount < 20) {
      tips.push({id: 1, emoji: '🚀', text: '势头不错'})
      tips.push({id: 2, emoji: '⭐', text: '保持节奏'})
    } else {
      tips.push({id: 1, emoji: '🔥', text: '知识库满满'})
      tips.push({id: 2, emoji: '🎯', text: '高效创作中'})
    }

    // 只在内容真正变化时才更新，避免不必要的重新渲染
    const currentTips = vibrantTips.value
    const needsUpdate = currentTips.length !== tips.length ||
        currentTips.some((tip, index) => tip.id !== tips[index]?.id || tip.text !== tips[index]?.text)

    if (needsUpdate) {
      // 使用 nextTick 确保在 DOM 更新后设置，减少闪烁
      nextTick(() => {
        vibrantTips.value = tips
      })
    }
  }, 300) // 300ms 防抖
}

// 只监听空间变化，不监听筛选
watch(
    () => currentSpace.value?.id,
    (newSpaceId, oldSpaceId) => {
      // 只在空间真正变化时才更新
      if (newSpaceId !== oldSpaceId) {
        updateVibrantTips()
        // 切换空间时重置到第一页
        currentPage.value = 1
      }
    },
    {immediate: false}
)

// 监听筛选条件变化，重置到第一页
watch([searchQuery, spaceSubView, sortBy, selectedFolderId], () => {
  currentPage.value = 1
})

watch(viewMode, () => {
  triggerContentAnimation('view')
})

watch(loading, (isLoading) => {
  if (!isLoading) {
    nextTick(() => {
      updateScrollState()
    })
  }
})

watch(currentSpace, () => {
  nextTick(() => {
    detachScrollListener()
    if (currentSpace.value) {
      attachScrollListener()
    }
  })
})

// 方法
const switchView = (view) => {
  currentView.value = view
  // 切换到回收站时，重置空间选择和子视图
  if (view === 'trash') {
    currentSpace.value = null
    spaceSubView.value = 'all'
  } else if (view === 'home') {
    // 切换到文档库时，确保有默认空间
    if (!currentSpace.value) {
      currentSpace.value = null // 默认显示全部
    }
  }
  loadDocuments()
}

const toggleSpaces = () => {
  showSpaces.value = !showSpaces.value
  if (showSpaces.value && spaces.value.length === 0) {
    loadSpaces()
  }
}

const toggleSpaceDropdown = () => {
  showSpaceDropdown.value = !showSpaceDropdown.value
  if (showSpaceDropdown.value && spaces.value.length === 0) {
    loadSpaces()
  }
}

const selectSpace = (space, event) => {
  // 阻止事件冒泡，避免触发外部的view-change
  if (event) {
    event.stopPropagation()
  }
  
  // 如果选择"全部"，设置为null，并重置空间子视图
  if (space.id === 'all') {
    currentSpace.value = null
    spaceSubView.value = 'all'
  } else {
    // 添加切换动画效果
    const oldSpace = currentSpace.value
    currentSpace.value = space
    // 选择空间后，默认显示"全部"子视图
    spaceSubView.value = 'all'
  }
  
  // 切换空间时，清除文件夹筛选
  selectedFolderId.value = null
  selectedTreeId.value = null
  
  // 关闭下拉菜单
  showSpaceDropdown.value = false
  
  // 切换空间时，确保在文档库视图
  if (currentView.value !== 'home') {
    currentView.value = 'home'
  }
  
  // 计算该空间的文档数量
  const spaceDocCount = space.id === 'all' 
    ? allDocuments.value.filter(doc => !doc.deleted).length
    : allDocuments.value.filter(doc => doc.spaceId === space.id && !doc.deleted).length
  
  console.log(`切换到空间: ${space.name}, 文档数: ${spaceDocCount}`)
  
  // 切换空间时，重新过滤文档（不需要重新加载，只需要触发计算属性更新）
  // sortedAndFilteredDocuments 会自动更新
}

const switchSpaceSubView = (subView) => {
  spaceSubView.value = subView
  // 切换子视图时，清除文件夹筛选，避免冲突
  if (subView !== 'all') {
    selectedFolderId.value = null
    selectedTreeId.value = null
  }
  // sortedAndFilteredDocuments 会自动更新
  triggerContentAnimation('filter')
}

const loadSpaces = async () => {
  try {
    // 使用假数据
    spaces.value = generateSpaces()
    
    // 不再自动设置默认空间，保持 currentSpace 为 null 以显示首页
    // 只有在明确传入 spaceId 或用户选择空间时才设置
  } catch (error) {
    console.error('加载空间列表失败:', error)
  }
}

const loadDocuments = async () => {
  loading.value = true
  try {
    // 不再自动设置默认空间，允许 currentSpace 为 null 以显示首页
    
    // 先尝试从API加载
    try {
      const userId = getUserId()
      const response = await fetch(`/doc/list/${userId}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json'
        }
      })
      
      if (response.ok) {
        const docList = await response.json()
        // 转换后端返回的数据格式
        const apiDocs = docList.map(doc => ({
          id: doc.id,
          name: doc.name || '未命名文档',
          description: doc.text || '',
          createdAt: Date.now(),
          updatedAt: Date.now(),
          size: (doc.text || '').length,
          favorite: false,
          liked: false,
          likeCount: 0,
          collaborators: [],
          shareLink: null,
          owner: {id: doc.owner || userId, name: '当前用户'},
          spaceId: currentSpace.value.id,
          deleted: false
        }))
        
        // 合并API数据和假数据
        allDocuments.value = [...apiDocs, ...generateDocuments(30)]
      } else {
        // API失败，使用假数据
        allDocuments.value = generateDocuments(50)
      }
    } catch (apiError) {
      // API调用失败，使用假数据
      console.log('使用假数据:', apiError)
      allDocuments.value = generateDocuments(50)
    }
    
    // 确保有数据，生成50个文档
    if (allDocuments.value.length === 0) {
      console.log('生成50个假数据文档')
      allDocuments.value = generateDocuments(50)
    }
    
    console.log('加载的文档数量:', allDocuments.value.length)
    console.log('当前视图:', currentView.value)
    console.log('当前空间:', currentSpace.value?.name)
    
    // 加载任务和日历事件
    tasks.value = generateTasks()
    calendarEvents.value = generateCalendarEvents()
    
  } catch (error) {
    console.error('加载文档列表失败:', error)
    // 即使出错也生成假数据
    if (allDocuments.value.length === 0) {
      console.log('错误后生成50个假数据文档')
      allDocuments.value = generateDocuments(50)
    }
  } finally {
    loading.value = false
    console.log('文档加载完成，总数:', allDocuments.value.length)
    console.log('当前空间:', currentSpace.value?.name, '过滤后数量:', sortedAndFilteredDocuments.value.length)
  }
}

const toggleSelectAll = (event) => {
  if (event.target.checked) {
    // 只选中当前页的文档，避免选中所有文档导致性能问题
    selectedDocs.value = paginatedDocuments.value.map(doc => doc.id)
  } else {
    // 取消选中时，只取消当前页的选中状态
    const currentPageIds = new Set(paginatedDocuments.value.map(doc => doc.id))
    selectedDocs.value = selectedDocs.value.filter(id => !currentPageIds.has(id))
  }
}

// 优化的选中状态检查函数
const isDocSelected = (docId) => {
  return selectedDocsSet.value.has(docId)
}

const toggleSelect = (doc) => {
  const index = selectedDocs.value.indexOf(doc.id)
  if (index > -1) {
    selectedDocs.value.splice(index, 1)
  } else {
    selectedDocs.value.push(doc.id)
  }
}

const openDocument = (doc) => {
  // 打开文档编辑器
  editingDocument.value = {
    ...doc,
    content: doc.content || '<p></p>'
  }
}

// 关闭编辑器
const closeEditor = () => {
  editingDocument.value = null
}

// 处理文档更新
const handleDocumentUpdate = (updates) => {
  if (editingDocument.value) {
    Object.assign(editingDocument.value, updates)
    // 更新文档列表中的对应文档
    const index = allDocuments.value.findIndex(d => d.id === editingDocument.value.id)
    if (index > -1) {
      Object.assign(allDocuments.value[index], updates)
    }
  }
}

// 处理文档保存
const handleDocumentSave = async (data) => {
  try {
    // TODO: 调用 API 保存文档
    console.log('保存文档:', data)
    showMessage('文档已保存', 'success')
    
    // 更新文档列表
    const index = allDocuments.value.findIndex(d => d.id === editingDocument.value.id)
    if (index > -1) {
      Object.assign(allDocuments.value[index], {
        name: data.name,
        content: data.content,
        updatedAt: new Date()
      })
    }
  } catch (error) {
    console.error('保存文档失败:', error)
    showMessage(error.message || '保存文档失败', 'error')
  }
}

// 处理目录树选择
const handleTreeSelect = (node) => {
  selectedTreeId.value = node.id
  if (node.isFolder) {
    // 选择文件夹时，切换到"全部"子视图，避免与子视图筛选冲突
    spaceSubView.value = 'all'
    selectedFolderId.value = node.id
  } else {
    // 选择文档时，清除文件夹筛选并打开文档
    selectedFolderId.value = null
    openDocument(node)
  }
}

// 处理文件夹选择
const handleFolderSelect = (folderId) => {
  // 选择文件夹时，切换到"全部"子视图
  spaceSubView.value = 'all'
  selectedFolderId.value = folderId
}

const handleFolderCardClick = (folder) => {
  spaceSubView.value = 'all'
  selectedFolderId.value = folder.id
  selectedTreeId.value = folder.id
  triggerContentAnimation('filter')
}

// 关闭创建对话框并重置表单
const closeCreateDialog = () => {
  showCreateDialog.value = false
  createDocName.value = ''
  createDocContent.value = ''
  createDocType.value = 'text'
}

// 处理创建文档
const handleCreateDocument = async () => {
  await createDocument(createDocType.value)
}

const createDocument = async (type) => {
  try {
    // 确保在个人空间创建文档
    if (!currentSpace.value) {
      const personalSpace = spaces.value.find(s => s.id === 'personal' || s.name === '个人空间')
      if (personalSpace) {
        currentSpace.value = personalSpace
      } else {
        currentSpace.value = {id: 'personal', name: '个人空间', type: 'personal', color: '#8b5cf6', icon: '个'}
      }
    }
    
    const userId = getUserId()
    const docName = createDocName.value.trim() || '未命名文档'
    const docContent = createDocContent.value || ''
    
    const response = await fetch('/doc/add', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        name: docName,
        text: docContent,
        type: type === 'text' ? 0 : (type === 'markdown' ? 1 : 0),
        userId: userId
      })
    })
    
    if (!response.ok) {
      let errorMessage = '创建文档失败'
      try {
        const errorData = await response.json()
        errorMessage = errorData.message || errorData.error || errorMessage
      } catch (e) {
        errorMessage = `创建文档失败: ${response.status} ${response.statusText}`
      }
      throw new Error(errorMessage)
    }
    
    const doc = await response.json()
    
    // 转换后端返回的数据格式为前端需要的格式
    const newDoc = {
      id: doc.id,
      name: doc.name || '未命名文档',
      description: doc.text || '',
      createdAt: Date.now(),
      updatedAt: Date.now(),
      size: (doc.text || '').length,
      favorite: false,
      shared: false,
      owner: {id: doc.owner || userId, name: '当前用户'},
      spaceId: currentSpace.value.id
    }
    
    allDocuments.value.unshift(newDoc)
    closeCreateDialog()
    showMessage('文档创建成功', 'success')
    openDocument(newDoc)
  } catch (error) {
    console.error('创建文档失败:', error)
    showMessage(error.message || '创建文档失败', 'error')
  }
}

// 显示消息提示
const showMessage = (message, type = 'info') => {
  if (type === 'error') {
    // 错误使用错误对话框
    errorDialogMessage.value = message
    showErrorDialog.value = true
  } else {
    // 成功和信息使用右上角 Toast
    toastMessage.value = message
    toastType.value = type
    showToast.value = true
  }
}

const toggleFavorite = async (doc) => {
  try {
    // TODO: 调用API切换收藏状态
    // await fetch(`/api/documents/${doc.id}/favorite`, {
    //   method: doc.favorite ? 'DELETE' : 'POST'
    // })
    
    doc.favorite = !doc.favorite
  } catch (error) {
    console.error('切换收藏状态失败:', error)
  }
}

const showDocMenu = (event, doc) => {
  contextMenu.value = {
    show: true,
    x: event.clientX,
    y: event.clientY,
    doc
  }
}

const showContextMenu = (event, doc) => {
  showDocMenu(event, doc)
}

const handleContextAction = async (action) => {
  const doc = contextMenu.value.doc
  if (!doc) return
  
  contextMenu.value.show = false
  
  switch (action) {
    case 'open':
      openDocument(doc)
      break
    case 'rename':
      renameValue.value = doc.name || ''
      showRenameDialog.value = true
      nextTick(() => {
        renameInputRef.value?.focus()
        renameInputRef.value?.select()
      })
      break
    case 'duplicate':
      await duplicateDocument(doc)
      break
    case 'share':
      await openShareDialog(doc)
      break
    case 'favorite':
      await toggleFavorite(doc)
      break
    case 'delete':
      await deleteDocument(doc)
      break
  }
}

const confirmRename = async () => {
  const doc = contextMenu.value.doc
  if (!doc || !renameValue.value.trim()) return
  
  try {
    // TODO: 调用API重命名
    // await fetch(`/api/documents/${doc.id}`, {
    //   method: 'PATCH',
    //   headers: { 'Content-Type': 'application/json' },
    //   body: JSON.stringify({ name: renameValue.value.trim() })
    // })
    
    doc.name = renameValue.value.trim()
    showRenameDialog.value = false
  } catch (error) {
    console.error('重命名失败:', error)
  }
}

const duplicateDocument = async (doc) => {
  try {
    // TODO: 调用API复制文档
    // const response = await fetch(`/api/documents/${doc.id}/duplicate`, {
    //   method: 'POST'
    // })
    // const newDoc = await response.json()
    
    const newDoc = {
      ...doc,
      id: Date.now().toString(),
      name: doc.name + ' (副本)',
      createdAt: Date.now(),
      updatedAt: Date.now()
    }
    
    allDocuments.value.unshift(newDoc)
    showMessage('文档已复制', 'success')
  } catch (error) {
    console.error('复制文档失败:', error)
  }
}

const openShareDialog = async (doc) => {
  shareDocument.value = doc
  showShareDialog.value = true
}

const handleShareSave = (shareData) => {
  const doc = shareDocument.value
  if (!doc) return
  
  // 更新文档的分享信息
  const docIndex = allDocuments.value.findIndex(d => d.id === doc.id)
  if (docIndex > -1) {
    allDocuments.value[docIndex] = {
      ...allDocuments.value[docIndex],
      shareLink: shareData.shareLink,
      collaborators: shareData.collaborators
    }
    showMessage('分享设置已保存', 'success')
  }
}

const deleteDocument = async (doc) => {
  if (!confirm(`确定要删除文档"${doc.name}"吗？`)) {
    return
  }
  
  try {
    // TODO: 调用API删除文档
    // await fetch(`/api/documents/${doc.id}`, {
    //   method: 'DELETE'
    // })
    
    const index = allDocuments.value.findIndex(d => d.id === doc.id)
    if (index > -1) {
      // 标记为已删除，而不是真正删除
      allDocuments.value[index].deleted = true
      allDocuments.value[index].deletedAt = Date.now()
    }
    
    const selectedIndex = selectedDocs.value.indexOf(doc.id)
    if (selectedIndex > -1) {
      selectedDocs.value.splice(selectedIndex, 1)
    }
    
    showMessage('文档已移至回收站', 'success')
  } catch (error) {
    console.error('删除文档失败:', error)
  }
}

// 使用公共工具函数 formatDate 和 formatSizeBytes
const formatSize = formatSizeBytes

const getOwnerInitial = (owner) => {
  if (!owner || !owner.name) return '?'
  return owner.name.charAt(0).toUpperCase()
}

// 点击外部关闭菜单
const handleClickOutside = (event) => {
  const target = event.target
  if (!(target instanceof Element)) {
    return
  }
  if (contextMenu.value.show && !target.closest('.context-menu')) {
    contextMenu.value.show = false
  }
  if (showRenameDialog && !target.closest('.dialog')) {
    showRenameDialog.value = false
  }
  if (showSpaceDropdown && !target.closest('.space-selector-dropdown')) {
    showSpaceDropdown.value = false
  }
  if (showSortMenu.value && !target.closest('.sort-wrapper-modern')) {
    showSortMenu.value = false
  }
}

// 刷新处理函数
const handleRefresh = (event) => {
  const {view} = event.detail || {}
  if (view === 'docs') {
    console.log('刷新文档库')
    loadDocuments()
    loadSpaces()
  }
}

// 返回首页
const goToHome = () => {
  if (pageTransitionPhase.value === 'space-leave' || !currentSpace.value) return
  pageTransitionPhase.value = 'space-leave'
  if (pagePhaseTimer) {
    clearTimeout(pagePhaseTimer)
  }
  pagePhaseTimer = setTimeout(() => {
  currentSpace.value = null
  currentView.value = 'home'
  spaceSubView.value = 'all'
    pageTransitionPhase.value = 'home'
    homeReturnTick.value = Date.now()
  }, PAGE_PHASE_DURATION)
}

// 处理从首页进入空间
const handleEnterSpace = (spaceId) => {
  if (pageTransitionPhase.value === 'space-enter') return
  // 根据 spaceId 查找对应的空间对象
  const space = spaces.value.find(s => s.id === spaceId)
  if (!space) {
    console.error('未找到空间:', spaceId)
    return
  }
  pendingSpace = space
  lastVisitedSpaceId.value = space.id
  pageTransitionPhase.value = 'space-enter'
  if (pagePhaseTimer) {
    clearTimeout(pagePhaseTimer)
  }
  pagePhaseTimer = setTimeout(() => {
    currentSpace.value = pendingSpace
    currentView.value = 'home'
    spaceSubView.value = 'all'
    pendingSpace = null
    loadDocuments()
    pageTransitionPhase.value = 'space-active'
  }, PAGE_PHASE_DURATION)
}

// 处理从首页创建文档
const handleCreateFromHome = () => {
  showCreateDialog.value = true
}

// 监听创建对话框打开，自动聚焦到名称输入框
watch(showCreateDialog, (newVal) => {
  if (newVal) {
    nextTick(() => {
      createNameInputRef.value?.focus()
    })
  }
})

onMounted(async () => {
  // 默认显示首页，除非有spaceId
  currentSpace.value = null
  
  // 先加载空间
  await loadSpaces()
  
  // 如果外部传入了spaceId，自动选择该空间
  if (props.spaceId) {
    const space = spaces.value.find(s => s.id === props.spaceId)
    if (space) {
      currentSpace.value = space
      currentView.value = 'home'
      spaceSubView.value = 'all'
      console.log('外部传入空间ID:', props.spaceId, '空间名称:', space.name)
    }
  }
  
  // 如果外部传入了viewType，切换到对应视图
  if (props.viewType) {
    if (props.viewType.startsWith('docs-')) {
      const view = props.viewType.replace('docs-', '')
      if (['recent', 'favorites', 'shared', 'my', 'templates', 'trash'].includes(view)) {
        currentView.value = view === 'my' ? 'home' : view
      }
    }
  }
  
  // 加载文档列表（首页和空间页面都需要）
  await loadDocuments()
  
  // 确保有数据，生成50个文档
  if (allDocuments.value.length === 0) {
    console.warn('文档列表为空，生成50个假数据文档')
    allDocuments.value = generateDocuments(50)
  }

  // 更新活力提示
  updateVibrantTips()
  
  console.log('组件挂载完成，文档总数:', allDocuments.value.length)
  console.log('当前空间:', currentSpace.value?.name || '首页')
  console.log('当前视图:', currentView.value)
  console.log('个人空间文档数:', allDocuments.value.filter(doc => doc.spaceId === 'personal').length)
  
  document.addEventListener('click', handleClickOutside)
  // 监听刷新事件
  window.addEventListener('tab-refresh', handleRefresh)
  nextTick(() => {
    attachScrollListener()
  })
})

// 清理
onUnmounted(() => {
  window.removeEventListener('tab-refresh', handleRefresh)
  document.removeEventListener('click', handleClickOutside)
  // 清理防抖定时器
  if (updateTipsTimer) {
    clearTimeout(updateTipsTimer)
  }
  if (pagePhaseTimer) {
    clearTimeout(pagePhaseTimer)
    pagePhaseTimer = null
  }
  pendingSpace = null
  detachScrollListener()
})
</script>

<style scoped>
.document-library-page {
  align-items: stretch;
  position: relative;
  gap: 0;
  height: 100%; /* 改为 100%，让外层布局控制高度 */
  background: #f5f5f7;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  --collapse-progress: 0;
}

.document-library-page.state-space-active .dl-hero-section {
  animation: heroSlideIn var(--motion-duration-normal) var(--motion-ease-enter);
}

.document-library-page.state-space-active .dl-toolbar-section {
  animation: toolbarFadeIn var(--motion-duration-normal) var(--motion-ease-enter);
  animation-delay: 80ms;
}

.document-library-page.state-space-active .document-tree {
  animation: treeListReveal var(--motion-duration-normal) var(--motion-ease-enter);
  animation-delay: 120ms;
  animation-fill-mode: both;
}

.document-library-page.state-leaving-space .dl-main-wrapper {
  opacity: 0;
  transform: translateX(12px);
  transition:
    opacity var(--motion-duration-normal) var(--motion-ease-exit),
    transform var(--motion-duration-normal) var(--motion-ease-exit);
}

.document-library-page.state-space-active .doc-card {
  animation: cardCascade var(--motion-duration-slow) var(--motion-ease-enter);
  animation-delay: calc(0.1s + (var(--card-index, 0) * 40ms));
  animation-fill-mode: both;
}

.document-library-page.state-space-active .doc-row {
  animation: rowCascade var(--motion-duration-normal) var(--motion-ease-enter);
  animation-delay: calc(0.12s + (var(--card-index, 0) * 30ms));
  animation-fill-mode: both;
}

@keyframes heroSlideIn {
  from {
    opacity: 0;
    transform: translateY(-12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes toolbarFadeIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes treeListReveal {
  from {
    opacity: 0;
    transform: translateX(-12px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes cardCascade {
  0% {
    opacity: 0;
    transform: translateY(12px) scale(0.96);
  }
  60% {
    opacity: 1;
    transform: translateY(-2px) scale(1.02);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes rowCascade {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 主容器 */
.dl-main-wrapper {
  display: flex;
  flex: 1;
  overflow: hidden;
  min-width: 0;
  height: 100%; /* 改为 100%，使用 flex 布局 */
  position: relative;
}

.dl-main {
  flex: 1;
  min-width: 0;
  /* 移除 height: 100vh，使用 flex 布局即可 */
  margin: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #f5f5f7;
}


.dl-hero-section {
  position: relative;
  overflow: hidden;
  border-bottom: none;
  box-shadow: 0 calc(1px + 5px * var(--collapse-progress)) calc(12px * var(--collapse-progress)) rgba(15, 23, 42, 0.08);
  padding: calc(24px - 10px * var(--collapse-progress)) 20px calc(24px - 6px * var(--collapse-progress)) 20px;
  position: sticky;
  top: 0;
  z-index: 10;
  transition:
    padding var(--motion-duration-normal) var(--motion-ease-soft),
    box-shadow var(--motion-duration-normal) var(--motion-ease-soft);
}

/* 将 backdrop-filter 放到独立的背景层，不参与 hover 交互 */
.dl-hero-section::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg,
    rgba(255, 255, 255, 0.78) 0%,
    rgba(255, 255, 255, 0.58) 55%,
    rgba(240, 244, 255, 0.45) 100%);
  backdrop-filter: blur(26px) saturate(170%);
  -webkit-backdrop-filter: blur(26px) saturate(170%);
  pointer-events: none;
  z-index: -1;
  border-bottom: 1px solid rgba(255, 255, 255, 0.3);
  opacity: calc(0.85 + 0.1 * var(--collapse-progress));
  transition: opacity var(--motion-duration-normal) var(--motion-ease-soft);
}

.dl-hero-content {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  width: 100%;
  margin: 0;
  transform: translateY(calc(-4px * var(--collapse-progress)));
  transition: transform var(--motion-duration-normal) var(--motion-ease-soft);
}

.dl-hero-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  min-width: 0;
}

.dl-hero-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
  margin-left: auto;
}

.dl-hero-search {
  min-width: 280px;
  max-width: 500px;
}

.dl-hero-stats {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #6b7280;
  margin-top: 4px;
}

.hero-stat-item {
  font-weight: 500;
}

.hero-stat-divider {
  opacity: 0.5;
}

.hero-stat-hint {
  opacity: 0.7;
}

/* 活力提示文字 */
.hero-vibrant-tips {
  margin-top: 8px;
}

.tips-container {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.vibrant-tip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: linear-gradient(135deg,
  rgba(22, 93, 255, 0.1) 0%,
  rgba(139, 92, 246, 0.08) 100%);
  border: 1px solid rgba(22, 93, 255, 0.15);
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  color: #165dff;
  animation: tipPulse 2s ease-in-out infinite;
  cursor: default;
  position: relative;
  overflow: hidden;
}

.vibrant-tip::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg,
  transparent 0%,
  rgba(255, 255, 255, 0.4) 50%,
  transparent 100%);
  animation: tipShine 3s ease-in-out infinite;
}

/* 删除 hover 动画以诊断闪烁问题 */
/* .vibrant-tip:hover {
  transform: translateY(-2px) scale(1.05);
  background: linear-gradient(135deg,
  rgba(22, 93, 255, 0.15) 0%,
  rgba(139, 92, 246, 0.12) 100%);
  border-color: rgba(22, 93, 255, 0.25);
} */

.tip-emoji {
  font-size: 16px;
  animation: tipBounce 2s ease-in-out infinite;
  display: inline-block;
}

.tip-text {
  font-weight: 600;
  letter-spacing: 0.3px;
}

@keyframes tipPulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.02);
    opacity: 0.95;
  }
}

@keyframes tipBounce {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  25% {
    transform: translateY(-3px) rotate(-5deg);
  }
  75% {
    transform: translateY(-3px) rotate(5deg);
  }
}

@keyframes tipShine {
  0% {
    left: -100%;
  }
  50%, 100% {
    left: 100%;
  }
}

.tip-fade-enter-active,
.tip-fade-leave-active {
  transition:
    opacity 0.5s cubic-bezier(0.4, 0, 0.2, 1),
    transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.tip-fade-enter-from {
  opacity: 0;
  transform: translateY(-10px) scale(0.9);
}

.tip-fade-leave-to {
  opacity: 0;
  transform: translateY(10px) scale(0.9);
}

/* 空间选择器 - 玻璃拟态风格，与标题协调 */
.space-selector-modern {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 18px;
  height: 44px;
  /* 移除 backdrop-filter，改用普通半透明背景 */
  background: linear-gradient(135deg,
  rgba(255, 255, 255, 0.85) 0%,
  rgba(255, 255, 255, 0.75) 50%,
  rgba(255, 255, 255, 0.7) 100%);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 20px;
  cursor: pointer;
  /* 静态阴影，hover 时不变 */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08),
  inset 0 1px 0 rgba(255, 255, 255, 0.7);
  overflow: hidden;
  /* 只动画 transform，不动画背景/边框/阴影 */
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.space-selector-modern::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 24px;
  background: linear-gradient(135deg,
  rgba(255, 255, 255, 0.4) 0%,
  transparent 50%,
  rgba(255, 255, 255, 0.2) 100%);
  opacity: 0;
  transition: opacity 0.3s;
  pointer-events: none;
}

/* 删除 hover 动画以诊断闪烁问题 */
/* .space-selector-modern:hover {
  transform: translateY(-2px) scale(1.02) translateZ(0);
} */

.space-selector-modern:active {
  transform: translateY(0) scale(0.98) translateZ(0);
  transition-duration: 0.15s;
}

/* 删除 hover 动画以诊断闪烁问题 */
/* .space-selector-modern:hover::before {
  opacity: 1;
} */

.space-selector-icon-modern {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
  font-size: 14px;
  font-weight: 600;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12),
  inset 0 1px 0 rgba(255, 255, 255, 0.3);
  will-change: transform;
  transform: translateZ(0);
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1),
  box-shadow 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 删除 hover 动画以诊断闪烁问题 */
/* .space-selector-modern:hover .space-selector-icon-modern {
  transform: scale(1.1) rotate(5deg) translateZ(0);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15),
  inset 0 1px 0 rgba(255, 255, 255, 0.4);
} */

.space-selector-icon-modern svg {
  width: 24px;
  height: 24px;
  color: #6b7280;
}

.space-icon-text {
  font-size: 18px;
  line-height: 1;
}

.space-selector-content-modern {
  display: flex;
  flex-direction: column;
  min-width: 0;
  flex: 1;
}

.space-selector-name-modern {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  white-space: nowrap;
  letter-spacing: 0.1px;
  transition: color 0.3s;
  line-height: 1.4;
}

.space-selector-arrow-modern {
  width: 16px;
  height: 16px;
  color: #6b7280;
  flex-shrink: 0;
  will-change: transform;
  transform: translateZ(0);
  transition: transform 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}

/* 删除 hover 动画以诊断闪烁问题 */
/* .space-selector-modern:hover .space-selector-arrow-modern {
  transform: translateY(2px) translateZ(0);
} */

.space-selector-arrow-modern.expanded {
  transform: rotate(180deg);
}

.space-switcher-menu-modern {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  min-width: 240px;
  /* 移除 backdrop-filter，改用普通半透明背景 */
  background: rgba(255, 255, 255, 0.98);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  /* 静态阴影 */
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  padding: 8px;
  z-index: 1000;
  max-height: 400px;
  overflow-y: auto;
  transform: translateZ(0);
}

.space-switcher-item-modern {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  border-radius: 8px;
  cursor: pointer;
  color: #111827;
  font-size: 14px;
  transform: translateZ(0);
  /* 只动画 transform，不动画 background */
  transition: transform 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  /* 进入动画 */
  animation: menuItemSlideIn 0.3s cubic-bezier(0.16, 1, 0.3, 1) backwards;
  animation-delay: calc(var(--index, 0) * 0.05s);
}

@keyframes menuItemSlideIn {
  from {
    opacity: 0;
    transform: translateX(-10px) translateZ(0);
  }
  to {
    opacity: 1;
    transform: translateX(0) translateZ(0);
  }
}

/* 删除 hover 动画以诊断闪烁问题 */
/* .space-switcher-item-modern:hover {
  background: rgba(22, 93, 255, 0.08);
  transform: translateX(4px) scale(1.02) translateZ(0);
} */

.space-switcher-item-modern:active {
  transform: translateX(2px) scale(0.98) translateZ(0);
  transition-duration: 0.15s;
}

.space-switcher-item-modern.active {
  background: linear-gradient(135deg, #165dff 0%, #4c7fff 100%);
  color: white;
  animation: activePulse 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes activePulse {
  0%, 100% {
    transform: scale(1) translateZ(0);
  }
  50% {
    transform: scale(1.05) translateZ(0);
  }
}

.space-switcher-item-icon {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.space-switcher-item-text {
  flex: 1;
  font-weight: 500;
}

.space-switcher-item-count {
  font-size: 12px;
  opacity: 0.7;
  font-weight: 500;
}


.dl-toolbar-section {
  position: relative;
  overflow: hidden;
  /* 移除 backdrop-filter，改用普通半透明背景 */
  background: rgba(255, 255, 255, 0.78);
  border-bottom: none;
  box-shadow: 0 1px 0 rgba(0, 0, 0, 0.02);
  padding: calc(16px - 6px * var(--collapse-progress)) 20px calc(16px - 2px * var(--collapse-progress)) 20px;
  transition:
    padding var(--motion-duration-normal) var(--motion-ease-soft),
    box-shadow var(--motion-duration-normal) var(--motion-ease-soft);
}

.dl-toolbar-section::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg,
    rgba(255, 255, 255, 0.85) 0%,
    rgba(255, 255, 255, 0.62) 60%,
    rgba(240, 244, 255, 0.45) 100%);
  backdrop-filter: blur(22px) saturate(160%);
  -webkit-backdrop-filter: blur(22px) saturate(160%);
  pointer-events: none;
  z-index: 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.35);
  opacity: calc(0.85 + 0.1 * var(--collapse-progress));
  transition: opacity var(--motion-duration-normal) var(--motion-ease-soft);
}

.dl-toolbar-content {
  position: relative;
  z-index: 1;
  transform: translateY(calc(-4px * var(--collapse-progress)));
  transition: transform var(--motion-duration-normal) var(--motion-ease-soft);
}

.dl-toolbar-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  width: 100%;
  margin: 0;
}

.toolbar-group {
  display: flex;
  align-items: center;
  gap: 16px;
}

.toolbar-group-label {
  font-size: 13px;
  font-weight: 600;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.toolbar-group-filters {
  flex: 1;
}

.toolbar-group-actions {
  flex-shrink: 0;
  margin-left: auto;
}

/* 筛选按钮组 */
.filter-buttons-modern {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  /* 移除 backdrop-filter，改用普通半透明背景 */
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  padding: 4px;
}

.filter-slider {
  position: absolute;
  top: 4px;
  left: 4px;
  height: calc(100% - 8px);
  background: linear-gradient(135deg, #165dff 0%, #4c7fff 100%);
  border-radius: 8px;
  z-index: 0;
  box-shadow: 0 2px 8px rgba(22, 93, 255, 0.3);
  will-change: transform, width;
  transform: translateZ(0);
  transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1),
  width 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.filter-btn-modern {
  position: relative;
  z-index: 1;
  padding: 8px 20px;
  border: none;
  background: transparent;
  color: #6b7280;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border-radius: 8px;
  white-space: nowrap;
  will-change: transform, color;
  transform: translateZ(0);
  transition: color 0.3s cubic-bezier(0.4, 0, 0.2, 1),
  transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.filter-btn-modern:hover {
  color: #111827;
  transform: translateY(-2px) scale(1.03) translateZ(0);
}

.filter-btn-modern:active {
  transform: translateY(0) scale(0.98) translateZ(0);
  transition-duration: 0.15s;
}

.filter-btn-modern.active {
  color: white;
  font-weight: 600;
  animation: filterActive 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes filterActive {
  0% {
    transform: scale(0.95) translateZ(0);
  }
  50% {
    transform: scale(1.08) translateZ(0);
  }
  100% {
    transform: scale(1) translateZ(0);
  }
}

/* 排序选择器 */
.sort-wrapper-modern {
  position: relative;
}

.sort-select-trigger {
  padding: 10px 32px 10px 16px;
  font-size: 14px;
  font-weight: 500;
  color: #111827;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  cursor: pointer;
  outline: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  min-width: 150px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  transition:
    transform var(--motion-duration-fast) var(--motion-ease-soft),
    color var(--motion-duration-fast) var(--motion-ease-soft);
}

.sort-select-trigger svg {
  color: #9ca3af;
  transition: transform var(--motion-duration-fast) var(--motion-ease-soft);
}

.sort-select-trigger:hover {
  transform: translateY(-2px);
}

.sort-select-trigger.open svg {
  transform: rotate(180deg);
  color: #165dff;
}

.sort-dropdown-menu {
  position: absolute;
  right: 0;
  top: calc(100% + 8px);
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 14px;
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.15);
  padding: 8px;
  width: 200px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  z-index: 30;
}

.sort-dropdown-item {
  width: 100%;
  border: none;
  background: transparent;
  border-radius: 10px;
  padding: 10px 12px;
  text-align: left;
  font-size: 14px;
  color: #374151;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  transition:
    background var(--motion-duration-fast) var(--motion-ease-soft),
    color var(--motion-duration-fast) var(--motion-ease-soft),
    transform var(--motion-duration-fast) var(--motion-ease-soft);
}

.sort-dropdown-item:hover {
  background: rgba(22, 93, 255, 0.08);
  color: #165dff;
  transform: translateX(2px);
}

.sort-dropdown-item.active {
  background: rgba(22, 93, 255, 0.12);
  color: #165dff;
  font-weight: 600;
}

.sort-item-hint {
  font-size: 12px;
  color: #6b7280;
}

.sort-dropdown-enter-active,
.sort-dropdown-leave-active {
  transition:
    opacity var(--motion-duration-fast) var(--motion-ease-enter),
    transform var(--motion-duration-fast) var(--motion-ease-enter);
}

.sort-dropdown-enter-from,
.sort-dropdown-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

.sort-dropdown-enter-to,
.sort-dropdown-leave-from {
  opacity: 1;
  transform: translateY(0);
}

.bulk-selection-bar {
  margin-top: 12px;
  padding: 12px 16px;
  background: rgba(22, 93, 255, 0.08);
  border: 1px solid rgba(22, 93, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  animation: bulkBarDrop var(--motion-duration-normal) var(--motion-ease-enter);
}

.bulk-selection-info {
  font-size: 14px;
  font-weight: 600;
  color: #165dff;
}

.bulk-selection-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.bulk-action-btn {
  border: none;
  background: rgba(22, 93, 255, 0.12);
  color: #165dff;
  border-radius: 8px;
  padding: 8px 12px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: transform var(--motion-duration-fast) var(--motion-ease-soft);
}

.bulk-action-btn svg {
  width: 14px;
  height: 14px;
}

.bulk-action-btn:hover {
  transform: translateY(-2px);
}

.bulk-action-btn.danger {
  background: rgba(239, 68, 68, 0.12);
  color: #dc2626;
}

.bulk-action-btn.ghost {
  background: transparent;
  border: 1px dashed rgba(22, 93, 255, 0.3);
}

.bulk-bar-enter-active,
.bulk-bar-leave-active {
  transition: opacity var(--motion-duration-fast) var(--motion-ease-exit),
  transform var(--motion-duration-fast) var(--motion-ease-exit);
}

.bulk-bar-enter-from,
.bulk-bar-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.bulk-bar-enter-to,
.bulk-bar-leave-from {
  opacity: 1;
  transform: translateY(0);
}

@keyframes bulkBarDrop {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 内容包装器 */
.dl-content-wrapper {
  flex: 1;
  display: flex;
  overflow: hidden;
  min-height: 0;
}

/* 顶部导航栏 */
.dl-top-nav {
  background: rgba(255, 255, 255, 0.5);
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  padding: 12px 24px;
}

.back-to-home-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  height: 40px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.5) 0%, rgba(255, 255, 255, 0.3) 100%);
  border: none;
  border-radius: 12px;
  color: #6b7280;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  /* 静态阴影 */
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08),
  inset 0 1px 0 rgba(255, 255, 255, 0.6);
  white-space: nowrap;
  flex-shrink: 0;
  /* 只动画 transform 和 color */
  transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1),
  color 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 删除 hover 动画以诊断闪烁问题 */
/* .back-to-home-btn:hover {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.7) 0%, rgba(255, 255, 255, 0.5) 100%);
  color: #165dff;
  transform: translateY(-3px) scale(1.05) translateZ(0);
} */

.back-to-home-btn:active {
  transform: translateY(-1px) scale(0.98) translateZ(0);
  transition-duration: 0.15s;
}

.back-to-home-btn svg {
  flex-shrink: 0;
  transition: transform 0.3s;
}

.back-to-home-btn:hover {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.7) 0%, rgba(255, 255, 255, 0.5) 100%);
  color: #165dff;
  transform: translateY(-3px) scale(1.05) translateZ(0);
}

.dl-hero-title-section {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
  min-width: 0;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.dl-hero-title {
  font-size: calc(24px - 4px * var(--collapse-progress));
  font-weight: 700;
  background: linear-gradient(135deg, 
    #165dff 0%, 
    #4c7fff 50%,
    #6b8eff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
  letter-spacing: -0.02em;
  line-height: 1.2;
  position: relative;
  display: inline-block;
  filter: drop-shadow(0 2px 4px rgba(22, 93, 255, 0.2));
  transition: font-size var(--motion-duration-normal) var(--motion-ease-soft);
}

.title-divider {
  width: 1px;
  height: 20px;
  background: linear-gradient(180deg,
  transparent 0%,
  rgba(0, 0, 0, 0.1) 50%,
  transparent 100%);
  opacity: 0.3;
}

.dl-hero-stats {
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

/* 删除 hover 动画以诊断闪烁问题 */
/* .filter-btn-modern:hover {
  color: #111827;
  transform: translateY(-2px) scale(1.05);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
} */

.filter-btn-modern.active {
  color: white;
  font-weight: 600;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3),
    0 0 10px rgba(255, 255, 255, 0.3);
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

/* 现代化按钮系统 */
.btn-modern {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 0;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition:
    transform 0.3s cubic-bezier(0.4, 0, 0.2, 1),
    opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;
  position: relative;
  overflow: hidden;
  height: 40px;
  min-width: 40px;
}

.btn-modern::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.2) 0%, 
    rgba(255, 255, 255, 0) 100%);
  opacity: 0;
  transition: opacity 0.3s;
}

/* 删除 hover 动画以诊断闪烁问题 */
/* .btn-modern:hover::before {
  opacity: 1;
} */

.btn-modern svg {
  flex-shrink: 0;
  width: 18px;
  height: 18px;
}

.btn-primary-modern {
  background: linear-gradient(135deg, 
    #165dff 0%, 
    #4c7fff 50%,
    #6b8eff 100%);
  color: white;
  padding: 0 24px;
  /* 静态阴影，hover 时不变 */
  box-shadow: 0 6px 20px rgba(22, 93, 255, 0.35),
  0 3px 10px rgba(22, 93, 255, 0.25),
  inset 0 1px 0 rgba(255, 255, 255, 0.3);
  position: relative;
  overflow: hidden;
  /* 只动画 transform */
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.btn-primary-modern::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.3) 0%, 
    transparent 50%,
    rgba(255, 255, 255, 0.1) 100%);
  pointer-events: none;
  opacity: 0.6;
}

.btn-primary-modern::after {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.3) 0%, transparent 70%);
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.4s;
  animation: buttonShine 3s ease-in-out infinite;
}

@keyframes buttonShine {
  0%, 100% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0.8);
  }
  50% {
    opacity: 0.4;
    transform: translate(-50%, -50%) scale(1.2);
  }
}

.btn-primary-modern:hover {
  background: linear-gradient(135deg, 
    #0f4fd8 0%, 
    #3d6eff 50%,
    #5a7eff 100%);
  transform: translateY(-3px) scale(1.05) translateZ(0);
}

.btn-primary-modern:active {
  transform: translateY(0) scale(0.98) translateZ(0);
  transition-duration: 0.15s;
}

.btn-primary-modern:hover::after {
  opacity: 0.6;
}

/* 空间区域 */
.space-section {
  height: calc(100vh - 24px);
  margin: 0px 0px 0px 0;
  background: transparent;
}

/* 内容区 */
.dl-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px 24px 24px 20px;
  background: transparent;
  min-width: 0;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
  /* 使用 contain 限定重绘范围 */
  contain: layout paint;
}

.dl-content.refresh-sort .doc-card,
.dl-content.refresh-sort .doc-row {
  animation: sortFlash 0.2s ease-out;
}

.dl-content.refresh-view .doc-card,
.dl-content.refresh-view .doc-row {
  animation: contentRefresh var(--motion-duration-normal) var(--motion-ease-enter);
}

.dl-content.refresh-bulk .doc-card.selected {
  animation: bulkGlow 0.4s var(--motion-ease-enter);
}

@keyframes sortFlash {
  0% {
    filter: brightness(1.15);
  }
  100% {
    filter: brightness(1);
  }
}

@keyframes bulkGlow {
  0% {
    box-shadow: 0 0 0 rgba(22, 93, 255, 0);
  }
  50% {
    box-shadow: 0 0 24px rgba(22, 93, 255, 0.35);
  }
  100% {
    box-shadow: 0 0 0 rgba(22, 93, 255, 0);
  }
}

.dl-content.refresh-filter .dl-grid-view,
.dl-content.refresh-filter .dl-list-view,
.dl-content.refresh-sort .dl-grid-view,
.dl-content.refresh-sort .dl-list-view,
.dl-content.refresh-bulk .dl-grid-view,
.dl-content.refresh-bulk .dl-list-view {
  animation: contentRefresh var(--motion-duration-normal) var(--motion-ease-enter);
  animation-fill-mode: both;
}

@keyframes contentRefresh {
  0% {
    opacity: 0;
    transform: translateY(8px);
  }
.dl-skeleton {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.skeleton-hero,
.skeleton-toolbar {
  height: 64px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.7);
}

.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

.skeleton-card {
  height: 180px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.6);
}


.shimmer {
  position: relative;
  overflow: hidden;
}

.shimmer::after {
  content: '';
  position: absolute;
  top: 0;
  left: -150%;
  width: 50%;
  height: 100%;
  background: linear-gradient(120deg, transparent 0%, rgba(255, 255, 255, 0.4) 50%, transparent 100%);
  animation: shimmerMove 1.4s infinite;
}

@keyframes shimmerMove {
  0% {
    left: -150%;
  }
  100% {
    left: 150%;
  }
}

.floating-tool-stack {
  position: absolute;
  right: 24px;
  bottom: 32px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  z-index: 40;
}

.floating-tool-btn {
  border: none;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 999px;
  padding: 12px 16px;
  font-size: 13px;
  font-weight: 600;
  color: #111827;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.16);
  cursor: pointer;
  transition: transform var(--motion-duration-fast) var(--motion-ease-soft);
}

.floating-tool-btn svg {
  width: 16px;
  height: 16px;
}

.floating-tool-btn:hover {
  transform: translateY(-3px);
}

.floating-tool-btn.primary {
  background: linear-gradient(135deg, #2563eb 0%, #60a5fa 100%);
  color: #fff;
  animation: floatingBreath 2.4s ease-in-out infinite;
}

@keyframes floatingBreath {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 12px 30px rgba(37, 99, 235, 0.3);
  }
  50% {
    transform: scale(1.03);
    box-shadow: 0 16px 34px rgba(37, 99, 235, 0.4);
  }
}

.back-to-top-btn {
  border: none;
  background: rgba(15, 23, 42, 0.8);
  color: white;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transform: translateY(12px);
  transition:
    opacity var(--motion-duration-fast) var(--motion-ease-exit),
    transform var(--motion-duration-fast) var(--motion-ease-exit),
    background var(--motion-duration-fast) var(--motion-ease-exit);
}

.back-to-top-btn.visible {
  opacity: 1;
  transform: translateY(0);
}

.back-to-top-btn.pulse {
  animation: backTopPulse 0.4s var(--motion-ease-enter);
}

@keyframes backTopPulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(0.94);
  }
  100% {
    transform: scale(1.05);
  }
}
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 隐藏滚动条但保持滚动功能 */
.dl-content::-webkit-scrollbar {
  width: 0;
  display: none;
}

/* 加载状态和空状态样式已移至 common.css */

/* 网格视图 */
.dl-grid-view {
  width: 100%;
}

.grid-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.doc-card {
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 20px;
  padding: 24px;
  cursor: pointer;
  opacity: 1;
  transition: transform 0.2s cubic-bezier(0.4, 0, 0.2, 1),
  opacity 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  transform: translateZ(0);
  position: relative;
  overflow: hidden;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.06);
}

.doc-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.2), rgba(22, 93, 255, 0.08));
  opacity: 0;
  transition: opacity var(--motion-duration-fast) var(--motion-ease-soft);
  pointer-events: none;
}

.doc-card::after {
  content: '';
  position: absolute;
  inset: -2px;
  border-radius: 22px;
  border: 1px solid rgba(22, 93, 255, 0.25);
  opacity: 0;
  transition: opacity var(--motion-duration-fast) var(--motion-ease-soft);
  pointer-events: none;
}

.doc-card.is-folder {
  background: rgba(99, 102, 241, 0.08);
  border: 1px solid rgba(99, 102, 241, 0.35);
  box-shadow:
    0 18px 36px rgba(99, 102, 241, 0.14),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.doc-card.is-folder::before {
  background: linear-gradient(135deg, rgba(129, 140, 248, 0.5), rgba(59, 130, 246, 0.2));
  opacity: 0.25;
}

.doc-card.is-folder::after {
  border-color: rgba(99, 102, 241, 0.45);
  opacity: 1;
}

.doc-card:hover {
  transform: translateY(-4px) scale(1.02);
}

.doc-card:hover::before {
  opacity: 1;
}

.doc-card:active {
  transform: translateY(-2px) scale(0.98) translateZ(0);
  transition-duration: 0.15s;
}

.card-select-toggle {
  position: absolute;
  top: 14px;
  right: 14px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.7);
  background: rgba(15, 23, 42, 0.35);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transform: scale(0.8);
  transition:
    opacity var(--motion-duration-fast) var(--motion-ease-enter),
    transform var(--motion-duration-fast) var(--motion-ease-enter),
    background var(--motion-duration-fast) var(--motion-ease-enter);
  z-index: 2;
}

.card-select-toggle .card-select-ring {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.card-select-toggle.visible {
  opacity: 1;
  transform: scale(1);
}

.card-select-toggle.active {
  background: #165dff;
  border-color: transparent;
}

.doc-card:hover .card-select-toggle {
  opacity: 1;
  transform: scale(1);
}

.doc-card.selected::after {
  opacity: 1;
}

.doc-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.doc-icon {
  color: #165dff;
  transform: translateZ(0);
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  /* 移除 will-change，避免 hover 时触发重新渲染 */
}

.doc-icon.is-folder {
  color: #6366f1;
}

.doc-card:hover .doc-icon {
  transform: scale(1.1) rotate(4deg) translateZ(0);
}

.doc-actions {
  display: flex;
  gap: 8px;
  opacity: 0;
  transform: translateX(8px) translateZ(0);
  transition: opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1),
  transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  /* 移除 will-change，避免 hover 时触发重新渲染 */
}

.doc-card:hover .doc-actions {
  opacity: 1;
  transform: translateX(0) translateZ(0);
}

.doc-action-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: rgba(0, 0, 0, 0.04);
  border-radius: 6px;
  cursor: pointer;
  color: #6b7280;
  /* 移除 will-change，避免 hover 时触发重新渲染 */
  transform: translateZ(0);
  /* 优化：只使用 transform，避免 background 和 color 变化导致重绘 */
  transition: transform 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.doc-action-btn:hover {
  transform: translateY(-2px) scale(1.05) translateZ(0);
}

.doc-action-btn:active {
  transform: translateY(0) scale(0.95) translateZ(0);
}

.doc-action-btn.active {
  background: rgba(22, 93, 255, 0.15);
  color: #165dff;
  animation: pulseActive 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes pulseActive {
  0%, 100% {
    transform: scale(1) translateZ(0);
  }
  50% {
    transform: scale(1.15) translateZ(0);
  }
}

.doc-card-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.folder-card-body {
  gap: 10px;
}

.folder-card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.folder-chip-badge {
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.5);
  color: #4338ca;
  font-weight: 600;
}

.folder-card-count {
  font-size: 12px;
  color: #4c1d95;
  font-weight: 500;
}

.folder-card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  font-size: 12px;
  color: #4c1d95;
  opacity: 0.8;
}

.folder-actions {
  gap: 6px;
}

.folder-enter-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: none;
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 12px;
  background: rgba(255, 255, 255, 0.85);
  color: #4338ca;
  cursor: pointer;
  transition: transform var(--motion-duration-fast) var(--motion-ease-soft);
}

.folder-enter-btn svg {
  width: 12px;
  height: 12px;
}

.folder-enter-btn:hover {
  transform: translateY(-2px);
}

.folder-card-footer {
  display: flex;
  justify-content: flex-end;
}

.folder-link {
  border: none;
  background: transparent;
  color: #4338ca;
  font-weight: 600;
  font-size: 13px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  transition: transform var(--motion-duration-fast) var(--motion-ease-soft);
}

.folder-link svg {
  width: 12px;
  height: 12px;
}

.folder-link:hover {
  transform: translateX(4px);
}

.doc-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 0;
  line-height: 1.4;
  transform: translateZ(0);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1), color 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  /* 移除 will-change，避免 hover 时触发重新渲染 */
}

.doc-card:hover .doc-title {
  transform: translateY(-2px) translateZ(0);
}

.doc-desc {
  font-size: 13px;
  color: #6b7280;
  margin: 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.doc-collaborators {
  display: flex;
  align-items: center;
  gap: 8px;
}

.doc-collaborators-list {
  display: flex;
  align-items: center;
}

.doc-collaborator-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #165dff 0%, #4c7fff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 11px;
  font-weight: 600;
  border: 2px solid white;
  transform: translateZ(0);
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  /* 移除 will-change，避免 hover 时触发重新渲染 */
}

.doc-card:hover .doc-collaborator-avatar {
  transform: scale(1.08) translateZ(0);
}

.doc-collaborator-more {
  font-size: 11px;
  color: #6b7280;
  margin-left: 4px;
}

.doc-share-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #8b5cf6;
  background: rgba(139, 92, 246, 0.1);
  padding: 4px 8px;
  border-radius: 6px;
  font-weight: 500;
  transition: transform var(--motion-duration-fast) var(--motion-ease-soft);
}

.doc-share-badge:active,
.favorite-badge:active {
  animation: badgePop var(--motion-duration-fast) var(--motion-ease-enter);
}
.favorite-badge {
  transition: transform var(--motion-duration-fast) var(--motion-ease-soft);
}

@keyframes badgePop {
  from {
    transform: scale(1);
  }
  50% {
    transform: scale(1.2);
  }
  to {
    transform: scale(1);
  }
}

.doc-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #9ca3af;
}

.doc-likes {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #f59e0b;
}

/* 列表视图 */
.dl-list-view {
  width: 100%;
}

.doc-table {
  width: 100%;
  border-collapse: collapse;
  background: transparent;
}

.doc-table thead {
  background: rgba(255, 255, 255, 0.5);
  position: sticky;
  top: 0;
  z-index: 5;
}

.doc-table th {
  padding: 12px 16px;
  text-align: left;
  font-size: 12px;
  font-weight: 600;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.doc-table td {
  padding: 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
}

.doc-row {
  background: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  /* 初始状态 - 不透明，避免闪烁 */
  opacity: 1;
  transition: opacity 0.2s cubic-bezier(0.4, 0, 0.2, 1),
  transform 0.2s cubic-bezier(0.4, 0, 0.2, 1),
  background 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  /* GPU 加速 */
  transform: translateZ(0);
  /* 移除 contain，避免与 transform 动画冲突导致重绘 */
  /* contain: layout style paint; */
}

/* 移除进入动画，避免闪烁 */

/* 只在首次渲染时触发进入动画 */
.doc-row[data-animated="true"] {
  animation: rowSlideIn 0.4s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  animation-delay: var(--delay, 0s);
  animation-fill-mode: both;
}

@keyframes rowSlideIn {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.doc-row.is-folder {
  background: rgba(99, 102, 241, 0.08);
}

.doc-row:hover {
  transform: translateX(4px) translateZ(0);
  background: rgba(255, 255, 255, 0.8);
}

.doc-row.selected {
  background: rgba(22, 93, 255, 0.08);
  animation: selectPulse 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes selectPulse {
  0%, 100% {
    background: rgba(22, 93, 255, 0.08);
  }
  50% {
    background: rgba(22, 93, 255, 0.15);
  }
}

.col-check {
  width: 40px;
}

.col-name {
  min-width: 200px;
}

.col-owner {
  width: 150px;
}

.col-updated {
  width: 150px;
}

.col-size {
  width: 100px;
}

.col-actions {
  width: 60px;
}

.doc-name-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.doc-icon-small {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(22, 93, 255, 0.1);
  border-radius: 8px;
  color: #165dff;
  flex-shrink: 0;
  transform: translateZ(0);
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1), background 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  /* 移除 will-change，避免 hover 时触发重新渲染 */
}

.doc-row.is-folder .doc-icon-small {
  background: rgba(99, 102, 241, 0.15);
  color: #4338ca;
}

/* 删除 hover 动画以诊断闪烁问题 */
/* .doc-row:hover .doc-icon-small {
  transform: scale(1.1) rotate(5deg) translateZ(0);
} */

.doc-name {
  font-size: 14px;
  font-weight: 500;
  color: #111827;
}

.favorite-badge {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: #f59e0b;
  cursor: pointer;
  flex-shrink: 0;
  transform: translateZ(0);
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  /* 移除 will-change，避免 hover 时触发重新渲染 */
}

/* 删除 hover 动画以诊断闪烁问题 */
/* .favorite-badge:hover {
  transform: scale(1.3) rotate(15deg) translateZ(0);
} */

.favorite-badge:active {
  transform: scale(1.1) rotate(15deg) translateZ(0);
  transition-duration: 0.15s;
}

.owner-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.owner-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #165dff 0%, #4c7fff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 11px;
  font-weight: 600;
  flex-shrink: 0;
  transform: translateZ(0);
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  /* 移除 will-change，避免 hover 时触发重新渲染 */
}

/* 删除 hover 动画以诊断闪烁问题 */
/* .doc-row:hover .owner-avatar {
  transform: scale(1.15) rotate(5deg) translateZ(0);
} */

.action-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  border-radius: 6px;
  cursor: pointer;
  color: #9ca3af;
  transform: translateZ(0);
  /* 优化：只使用 transform，避免 background 和 color 变化导致重绘 */
  transition: transform 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  /* 移除 will-change，避免 hover 时触发重新渲染 */
}

/* 删除 hover 动画以诊断闪烁问题 */
/* .action-btn:hover {
  transform: scale(1.15) rotate(90deg) translateZ(0);
} */

.action-btn:active {
  transform: scale(0.9) rotate(90deg) translateZ(0);
  transition-duration: 0.15s;
}

/* 上下文菜单 */
.context-menu {
  position: fixed;
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  padding: 8px;
  z-index: 2000;
  min-width: 180px;
  transform-origin: top right;
  animation: contextMenuPop var(--motion-duration-fast) var(--motion-ease-enter);
}

.context-menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #111827;
  transition:
    transform var(--motion-duration-fast) var(--motion-ease-soft),
    background var(--motion-duration-fast) var(--motion-ease-soft),
    color var(--motion-duration-fast) var(--motion-ease-soft);
}

.context-menu-item:hover {
  background: rgba(22, 93, 255, 0.08);
  color: #165dff;
  transform: translateX(4px);
}

.context-menu-item.danger {
  color: #ef4444;
}

.context-menu-item.danger:hover {
  background: rgba(239, 68, 68, 0.1);
  color: #dc2626;
}

@keyframes contextMenuPop {
  from {
    opacity: 0;
    transform: translateY(6px) scale(0.94);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.context-menu-divider {
  height: 1px;
  background: rgba(0, 0, 0, 0.06);
  margin: 4px 0;
}

/* 分页控件样式 */
.pagination-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 24px;
  margin-top: 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

.pagination-btn {
  padding: 8px 16px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.8);
  color: #111827;
  font-size: 14px;
  cursor: pointer;
  transition: transform var(--motion-duration-fast) var(--motion-ease-soft),
  color var(--motion-duration-fast) var(--motion-ease-soft);
}

.pagination-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  color: #165dff;
}

.pagination-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.pagination-info {
  font-size: 14px;
  color: #6b7280;
}

/* 空间下拉菜单动画 */
.space-dropdown-enter-active,
.space-dropdown-leave-active {
  transition:
    opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1),
    transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.space-dropdown-enter-from {
  opacity: 0;
  transform: translateY(-8px) scale(0.95);
}

.space-dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px) scale(0.95);
}
</style>
