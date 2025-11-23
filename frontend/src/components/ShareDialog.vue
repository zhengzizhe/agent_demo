<template>
  <div v-if="visible" class="share-dialog-overlay" @click.self="handleClose">
    <div class="share-dialog">
      <div class="share-dialog-header">
        <h3 class="share-dialog-title">分享文档</h3>
        <button class="share-dialog-close" @click="handleClose">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M5 5l10 10M15 5l-10 10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </button>
      </div>

      <div class="share-dialog-content">
        <!-- 链接分享 -->
        <div class="share-section">
          <div class="share-section-header">
            <h4 class="share-section-title">链接分享</h4>
            <label class="share-toggle">
              <input type="checkbox" v-model="shareLink.enabled" @change="handleLinkToggle" />
              <span class="toggle-slider"></span>
            </label>
          </div>
          
          <div v-if="shareLink.enabled" class="share-link-content">
            <div class="share-link-box">
              <input 
                type="text" 
                :value="shareLink.link" 
                readonly
                class="share-link-input"
                ref="linkInputRef"
              />
              <button class="share-link-copy" @click="copyLink">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <rect x="5" y="5" width="8" height="8" rx="1" stroke="currentColor" stroke-width="1.5" fill="none"/>
                  <path d="M3 3h8v8" stroke="currentColor" stroke-width="1.5" fill="none"/>
                </svg>
                复制链接
              </button>
            </div>
            
            <div class="share-permission-select">
              <label class="share-label">访问权限</label>
              <select v-model="shareLink.permission" class="share-select">
                <option value="view">仅查看</option>
                <option value="edit">可编辑</option>
                <option value="comment">可评论</option>
              </select>
            </div>
            
            <div class="share-options">
              <label class="share-option">
                <input type="checkbox" v-model="shareLink.password" />
                <span>设置密码</span>
              </label>
              <label class="share-option">
                <input type="checkbox" v-model="shareLink.expires" />
                <span>设置有效期</span>
              </label>
            </div>
            
            <div v-if="shareLink.password" class="share-password-input">
              <input 
                type="text" 
                v-model="shareLink.passwordValue"
                placeholder="输入访问密码"
                class="share-input"
              />
            </div>
            
            <div v-if="shareLink.expires" class="share-expires-input">
              <input 
                type="date" 
                v-model="shareLink.expiresValue"
                class="share-input"
              />
            </div>
          </div>
        </div>

        <!-- 协作者 -->
        <div class="share-section">
          <div class="share-section-header">
            <h4 class="share-section-title">协作者 ({{ collaborators.length }})</h4>
            <button class="share-add-btn" @click="showAddCollaborator = true">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M8 2v12M2 8h12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
              添加协作者
            </button>
          </div>
          
          <div class="collaborators-list">
            <div 
              v-for="collab in collaborators" 
              :key="collab.user.id"
              class="collaborator-item"
            >
              <div class="collaborator-avatar">{{ collab.user.avatar }}</div>
              <div class="collaborator-info">
                <div class="collaborator-name">{{ collab.user.name }}</div>
                <div class="collaborator-email">{{ collab.user.email }}</div>
              </div>
              <select 
                v-model="collab.role" 
                class="collaborator-role-select"
                @change="handleRoleChange(collab)"
              >
                <option value="viewer">仅查看</option>
                <option value="commenter">可评论</option>
                <option value="editor">可编辑</option>
              </select>
              <button 
                class="collaborator-remove"
                @click="removeCollaborator(collab)"
                title="移除"
              >
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <path d="M4 4l8 8M12 4l-8 8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
              </button>
            </div>
          </div>
        </div>

        <!-- 添加协作者对话框 -->
        <div v-if="showAddCollaborator" class="add-collaborator-dialog">
          <div class="add-collaborator-header">
            <h4>添加协作者</h4>
            <button @click="showAddCollaborator = false">×</button>
          </div>
          <div class="add-collaborator-search">
            <input 
              type="text" 
              v-model="searchUserQuery"
              placeholder="搜索用户或输入邮箱"
              class="add-collaborator-input"
            />
          </div>
          <div class="add-collaborator-list">
            <div 
              v-for="user in filteredUsers" 
              :key="user.id"
              class="add-collaborator-item"
              @click="addCollaborator(user)"
            >
              <div class="collaborator-avatar">{{ user.avatar }}</div>
              <div class="collaborator-info">
                <div class="collaborator-name">{{ user.name }}</div>
                <div class="collaborator-email">{{ user.email }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="share-dialog-footer">
        <button class="btn btn-secondary" @click="handleClose">取消</button>
        <button class="btn btn-primary" @click="handleSave">保存</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { users } from '../utils/mockData.js'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  document: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:visible', 'save'])

const shareLink = ref({
  enabled: false,
  link: '',
  permission: 'view',
  password: false,
  passwordValue: '',
  expires: false,
  expiresValue: ''
})

const collaborators = ref([])
const showAddCollaborator = ref(false)
const searchUserQuery = ref('')
const linkInputRef = ref(null)

// 过滤用户列表（排除已添加的协作者）
const filteredUsers = computed(() => {
  const query = searchUserQuery.value.toLowerCase()
  const existingIds = collaborators.value.map(c => c.user.id)
  
  return users
    .filter(user => !existingIds.includes(user.id))
    .filter(user => 
      user.name.toLowerCase().includes(query) ||
      user.email.toLowerCase().includes(query)
    )
})

// 初始化数据
watch(() => props.visible, (newVal) => {
  if (newVal && props.document) {
    // 初始化分享链接
    if (props.document.shareLink) {
      shareLink.value = {
        enabled: props.document.shareLink.enabled,
        link: props.document.shareLink.link,
        permission: props.document.shareLink.permission,
        password: !!props.document.shareLink.password,
        passwordValue: props.document.shareLink.password || '',
        expires: !!props.document.shareLink.expiresAt,
        expiresValue: props.document.shareLink.expiresAt ? new Date(props.document.shareLink.expiresAt).toISOString().split('T')[0] : ''
      }
    } else {
      shareLink.value = {
        enabled: false,
        link: `https://docs.example.com/share/${Date.now().toString(36)}`,
        permission: 'view',
        password: false,
        passwordValue: '',
        expires: false,
        expiresValue: ''
      }
    }
    
    // 初始化协作者
    collaborators.value = props.document.collaborators 
      ? props.document.collaborators.map(c => ({ ...c }))
      : []
  }
})

const handleClose = () => {
  emit('update:visible', false)
}

const handleLinkToggle = () => {
  if (shareLink.value.enabled && !shareLink.value.link) {
    shareLink.value.link = `https://docs.example.com/share/${Date.now().toString(36)}`
  }
}

const copyLink = async () => {
  if (linkInputRef.value) {
    linkInputRef.value.select()
    try {
      await navigator.clipboard.writeText(shareLink.value.link)
      // 可以添加toast提示
    } catch (err) {
      console.error('复制失败:', err)
    }
  }
}

const addCollaborator = (user) => {
  if (collaborators.value.some(c => c.user.id === user.id)) {
    return
  }
  
  collaborators.value.push({
    user,
    role: 'viewer',
    addedAt: Date.now()
  })
  
  showAddCollaborator.value = false
  searchUserQuery.value = ''
}

const removeCollaborator = (collab) => {
  const index = collaborators.value.findIndex(c => c.user.id === collab.user.id)
  if (index > -1) {
    collaborators.value.splice(index, 1)
  }
}

const handleRoleChange = (collab) => {
  // 可以在这里添加API调用
  console.log('更新协作者权限:', collab)
}

const handleSave = () => {
  const shareData = {
    shareLink: shareLink.value.enabled ? {
      enabled: true,
      link: shareLink.value.link,
      permission: shareLink.value.permission,
      password: shareLink.value.password ? shareLink.value.passwordValue : null,
      expiresAt: shareLink.value.expires && shareLink.value.expiresValue 
        ? new Date(shareLink.value.expiresValue).getTime() 
        : null
    } : null,
    collaborators: collaborators.value
  }
  
  emit('save', shareData)
  handleClose()
}
</script>

<style scoped>
.share-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  animation: feishuFadeIn 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  will-change: opacity;
}

@keyframes feishuFadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.share-dialog {
  background: #ffffff;
  border-radius: 8px;
  width: 90%;
  max-width: 640px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 
    0 24px 48px rgba(0, 0, 0, 0.12),
    0 8px 16px rgba(0, 0, 0, 0.08);
  animation: feishuSlideUp 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  transform: translateZ(0);
  will-change: opacity, transform;
}

@keyframes feishuSlideUp {
  from {
    opacity: 0;
    transform: translateY(24px) scale(0.96) translateZ(0);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1) translateZ(0);
  }
}

.share-dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 24px 20px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.share-dialog-title {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.share-dialog-close {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  color: #6b7280;
  cursor: pointer;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.share-dialog-close:hover {
  background: #f3f4f6;
  color: #111827;
}

.share-dialog-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: #ffffff;
}

.share-section {
  margin-bottom: 32px;
}

.share-section:last-child {
  margin-bottom: 0;
}

.share-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.share-section-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.share-toggle {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 24px;
  cursor: pointer;
}

.share-toggle input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #d1d5db;
  border-radius: 24px;
  transition: 0.3s;
}

.toggle-slider:before {
  content: '';
  position: absolute;
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background: white;
  border-radius: 50%;
  transition: 0.3s;
}

.share-toggle input:checked + .toggle-slider {
  background: #165dff;
}

.share-toggle input:checked + .toggle-slider:before {
  transform: translateX(20px);
}

.share-link-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.share-link-box {
  display: flex;
  gap: 8px;
}

.share-link-input {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  font-family: 'Monaco', 'Menlo', monospace;
}

.share-link-copy {
  padding: 10px 20px;
  background: #3370ff;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
  white-space: nowrap;
}

.share-link-copy:hover {
  background: #1e5eff;
  box-shadow: 0 2px 8px rgba(51, 112, 255, 0.3);
}

.share-link-copy:active {
  transform: scale(0.98);
}

.share-permission-select,
.share-password-input,
.share-expires-input {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.share-label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.share-select,
.share-input {
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

.share-options {
  display: flex;
  gap: 24px;
}

.share-option {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #374151;
  cursor: pointer;
}

.collaborators-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.collaborator-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
}

.collaborator-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  flex-shrink: 0;
}

.collaborator-info {
  flex: 1;
  min-width: 0;
}

.collaborator-name {
  font-size: 14px;
  font-weight: 500;
  color: #111827;
}

.collaborator-email {
  font-size: 12px;
  color: #6b7280;
}

.collaborator-role-select {
  padding: 6px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 13px;
  background: white;
}

.collaborator-remove {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  color: #6b7280;
  cursor: pointer;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.collaborator-remove:hover {
  background: #fee2e2;
  color: #dc2626;
}

.share-add-btn {
  padding: 8px 16px;
  background: #3370ff;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
}

.share-add-btn:hover {
  background: #1e5eff;
  box-shadow: 0 2px 8px rgba(51, 112, 255, 0.3);
}

.add-collaborator-dialog {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: white;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  z-index: 10;
}

.add-collaborator-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
}

.add-collaborator-header h4 {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.add-collaborator-header button {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  color: #6b7280;
  cursor: pointer;
  font-size: 24px;
  line-height: 1;
}

.add-collaborator-search {
  padding: 16px 24px;
  border-bottom: 1px solid #e5e7eb;
}

.add-collaborator-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

.add-collaborator-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.add-collaborator-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.add-collaborator-item:hover {
  background: #f3f4f6;
}

.share-dialog-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-secondary {
  background: #f3f4f6;
  color: #374151;
}

.btn-secondary:hover {
  background: #e5e7eb;
}

.btn-primary {
  background: #3370ff;
  color: white;
  border: none;
}

.btn-primary:hover {
  background: #1e5eff;
  box-shadow: 0 2px 8px rgba(51, 112, 255, 0.3);
}

.btn-primary:active {
  transform: scale(0.98);
}
</style>


