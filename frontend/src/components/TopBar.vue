<template>
  <div class="top-bar">
    <div class="top-bar-left">
      <nav class="breadcrumb-nav" v-if="breadcrumbs.length > 0" aria-label="面包屑导航">
        <ol class="breadcrumb-list">
          <li
            v-for="(crumb, index) in breadcrumbs"
            :key="index"
            class="breadcrumb-item"
          >
            <button
              v-if="index < breadcrumbs.length - 1"
              class="breadcrumb-link"
              @click="handleBreadcrumbClick(crumb, index)"
              :title="`返回 ${crumb}`"
            >
              {{ crumb }}
            </button>
            <span
              v-else
              class="breadcrumb-link current"
            >
              {{ crumb }}
            </span>
            <span
              v-if="index < breadcrumbs.length - 1"
              class="breadcrumb-separator"
              aria-hidden="true"
            >
              /
            </span>
          </li>
        </ol>
      </nav>
      <h1 class="page-title">{{ pageTitle }}</h1>
    </div>
    
    <div class="top-bar-right">
      <div class="top-bar-actions">
        <button class="action-btn" title="搜索">
          <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
            <circle cx="8" cy="8" r="5" stroke="currentColor" stroke-width="1.5" fill="none"/>
            <path d="M13 13l3 3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </button>
        <button class="action-btn" title="设置">
          <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
            <circle cx="9" cy="9" r="2" stroke="currentColor" stroke-width="1.5" fill="none"/>
            <path d="M9 1v2M9 15v2M17 9h-2M3 9H1M14.66 3.34l-1.41 1.41M4.75 13.25l-1.41 1.41M14.66 14.66l-1.41-1.41M4.75 4.75l-1.41-1.41" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  pageTitle: {
    type: String,
    default: '页面标题'
  },
  breadcrumbs: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['breadcrumb-click'])

const handleBreadcrumbClick = (crumb, index) => {
  emit('breadcrumb-click', { crumb, index })
}
</script>

<style scoped>
.top-bar {
  height: 56px;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 50;
  box-shadow: 0 1px 0 rgba(0, 0, 0, 0.06);
}

.top-bar-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  min-width: 0;
}

/* 飞书风格面包屑 - 更友好自然 */
.breadcrumb-nav {
  display: flex;
  align-items: center;
}

.breadcrumb-list {
  display: flex;
  align-items: center;
  gap: 0;
  flex-wrap: wrap;
  margin: 0;
  padding: 0;
  list-style: none;
}

.breadcrumb-item {
  display: flex;
  align-items: center;
  list-style: none;
}

.breadcrumb-link {
  display: inline-flex;
  align-items: center;
  padding: 6px 10px;
  border: none;
  background: transparent;
  font-size: 14px;
  font-weight: 400;
  color: #86909c;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;
  border-radius: 6px;
  line-height: 1.5;
}

.breadcrumb-link:hover {
  color: #165dff;
  background: #f2f3f5;
}

.breadcrumb-link:active {
  background: #e5e6eb;
}

.breadcrumb-link.current {
  color: #1d2129;
  font-weight: 500;
  cursor: default;
}

.breadcrumb-link.current:hover {
  background: transparent;
  color: #1d2129;
}

.breadcrumb-separator {
  display: inline-flex;
  align-items: center;
  color: #c9cdd4;
  margin: 0 6px;
  font-size: 12px;
  user-select: none;
  flex-shrink: 0;
  opacity: 0.6;
}

.page-title {
  font-size: 20px;
  font-weight: 500;
  color: #1d2129;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  letter-spacing: -0.01em;
}

@keyframes titleFadeIn {
  from {
    opacity: 0;
    transform: translateX(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.top-bar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.top-bar-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.action-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  border-radius: 6px;
  color: #86909c;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.action-btn:hover {
  background: #f2f3f5;
  color: #1d2129;
}

.action-btn:active {
  background: #e5e6eb;
  transform: scale(0.95);
}

.action-btn svg {
  width: 18px;
  height: 18px;
}
</style>

