<template>
  <div class="login-page">
    <div class="login-container">
      <!-- 语言切换 -->
      <div class="language-switcher">
        <button 
          v-for="lang in languages" 
          :key="lang.code"
          class="lang-btn"
          :class="{ active: currentLang === lang.code }"
          @click="switchLanguage(lang.code)"
        >
          {{ lang.name }}
        </button>
      </div>

      <!-- 主标题 -->
      <div class="login-header">
        <h1 class="login-title">{{ translate('title') }}</h1>
        <p class="login-subtitle" v-html="translate('subtitle')"></p>
      </div>

      <!-- 错误提示 -->
      <div v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </div>

      <!-- 邮箱输入 -->
      <div class="email-section">
        <label class="email-label">{{ translate('emailLabel') }}</label>
        <input
          v-model="form.email"
          type="email"
          class="email-input"
          :class="{ 'error': errorMessage }"
          :placeholder="translate('emailPlaceholder')"
          autocomplete="email"
          required
          :disabled="isLoading || showCodeInput"
        />
        <p class="email-hint">{{ translate('emailHint') }}</p>
      </div>

      <!-- 验证码输入 -->
      <div v-if="showCodeInput" class="code-section">
        <label class="email-label">验证码</label>
        <input
          v-model="form.code"
          type="text"
          class="email-input"
          :class="{ 'error': errorMessage }"
          placeholder="请输入6位验证码"
          maxlength="6"
          required
          :disabled="isLoading"
        />
        <p class="email-hint">验证码已发送到您的邮箱，请查收</p>
      </div>

      <!-- 继续按钮 -->
      <button 
        type="button" 
        class="continue-button"
        :disabled="!form.email || isLoading || (showCodeInput && !form.code)"
        @click="handleContinue"
      >
        <span v-if="!isLoading">
          {{ showCodeInput ? '登录' : translate('continue') }}
        </span>
        <span v-else class="loading-content">
          <span class="loading-spinner-small"></span>
          <span>{{ translate('loading') }}</span>
        </span>
      </button>

      <!-- 分隔线 -->
      <div class="divider">
        <span>{{ translate('or') }}</span>
      </div>

      <!-- 社交登录 -->
      <div class="social-login">
        <button class="social-button github" @click="handleSocialLogin('github')">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M10 0C4.477 0 0 4.484 0 10.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0110 4.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.203 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.942.359.31.678.921.678 1.856 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0020 10.017C20 4.484 15.522 0 10 0z" fill="currentColor" fill-rule="evenodd" clip-rule="evenodd"/>
          </svg>
          <span>{{ translate('continueWithGitHub') }}</span>
        </button>
        
        <button class="social-button google" @click="handleSocialLogin('google')">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M19.6 10.2C19.6 9.5 19.5 8.8 19.4 8.2H10V12H15.4C15.2 13 14.6 13.8 13.8 14.4L16.7 16.7C18.5 15.1 19.6 12.8 19.6 10.2Z" fill="#4285F4"/>
            <path d="M10 20C12.7 20 15 19 16.7 16.7L13.8 14.4C12.8 15.1 11.4 15.5 10 15.5C7.4 15.5 5.2 13.6 4.3 11.2L1.2 13.5C2.9 16.8 6.2 20 10 20Z" fill="#34A853"/>
            <path d="M4.3 11.2C4.1 10.6 4 10 4 9.5C4 9 4.1 8.4 4.3 7.8L1.2 5.5C0.4 6.8 0 8.3 0 9.5C0 10.7 0.4 12.2 1.2 13.5L4.3 11.2Z" fill="#FBBC05"/>
            <path d="M10 3.5C11.5 3.5 12.9 4 13.9 4.9L16.8 2C15 -0.3 12.7 -1 10 -1C6.2 -1 2.9 2.2 1.2 5.5L4.3 7.8C5.2 5.4 7.4 3.5 10 3.5Z" fill="#EA4335"/>
          </svg>
          <span>{{ translate('continueWithGoogle') }}</span>
        </button>
        
        <button class="social-button microsoft" @click="handleSocialLogin('microsoft')">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <rect x="1" y="1" width="8" height="8" fill="#F25022"/>
            <rect x="11" y="1" width="8" height="8" fill="#7FBA00"/>
            <rect x="1" y="11" width="8" height="8" fill="#00A4EF"/>
            <rect x="11" y="11" width="8" height="8" fill="#FFB900"/>
          </svg>
          <span>{{ translate('continueWithMicrosoft') }}</span>
        </button>
        
        <button class="social-button sso" @click="handleSocialLogin('sso')">
          <span>{{ translate('continueWithSSO') }}</span>
        </button>
      </div>

      <!-- 法律文本 -->
      <p class="legal-text" v-html="translate('legalText')"></p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useI18n } from '../composables/useI18n.js'
import { useAuth } from '../composables/useAuth.js'
import { sendVerificationCode } from '../api/auth.js'

const emit = defineEmits(['login-success'])

// 使用 i18n
const { currentLang, languages, t, switchLanguage } = useI18n()

// 使用认证
const { login } = useAuth()

// 翻译函数（用于模板）
const translate = (key) => {
  return t(`login.${key}`)
}

const form = reactive({
  email: '',
  code: '',
  password: '',
  loginType: 'email_code' // 默认使用验证码登录
})

const isLoading = ref(false)
const showCodeInput = ref(false)
const errorMessage = ref('')

const handleContinue = async () => {
  if (!form.email) {
    return
  }
  
  // 如果还没有发送验证码，先发送验证码
  if (!showCodeInput.value) {
    await handleSendCode()
  } else {
    // 如果已经发送验证码，执行登录
    await handleLogin()
  }
}

const handleSendCode = async () => {
  if (!form.email) {
    errorMessage.value = '请输入邮箱'
    return
  }
  
  isLoading.value = true
  errorMessage.value = ''
  
  try {
    await sendVerificationCode({
      email: form.email,
      purpose: 'login'
    })
    
    showCodeInput.value = true
  } catch (error) {
    console.error('发送验证码失败:', error)
    errorMessage.value = error.message || '发送验证码失败，请重试'
  } finally {
    isLoading.value = false
  }
}

const handleLogin = async () => {
  if (!form.email || !form.code) {
    errorMessage.value = '请输入邮箱和验证码'
    return
  }
  
  isLoading.value = true
  errorMessage.value = ''
  
  try {
    const response = await login({
      loginType: 'email_code',
      email: form.email,
      code: form.code
    })
    
    if (response && response.data) {
      emit('login-success', {
        email: form.email,
        token: response.data.token,
        userUuid: response.data.userUuid,
        displayName: response.data.displayName
      })
    }
  } catch (error) {
    console.error('登录失败:', error)
    errorMessage.value = error.message || '登录失败，请重试'
  } finally {
    isLoading.value = false
  }
}

const handleSocialLogin = (type) => {
  // TODO: 实现社交登录
  alert(`${type} 登录功能待实现`)
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--theme-background, #ffffff);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  padding: 20px 16px;
  position: relative;
  overflow: hidden;
}

/* 移动端 (375px - 767px) */
@media (min-width: 375px) {
  .login-page {
    padding: 24px 20px;
  }
}

/* 平板端 (768px - 1024px) */
@media (min-width: 768px) {
  .login-page {
    padding: 40px 32px;
  }
}

/* 桌面端 (1024px+) */
@media (min-width: 1024px) {
  .login-page {
    padding: 60px 40px;
  }
}

/* 背景渐变效果（与主应用一致） */
.login-page::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(to right, transparent, #e5e5e6, transparent);
  z-index: 1;
}

.login-page::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 10% 20%, rgba(33, 150, 243, 0.02) 0%, transparent 40%),
    radial-gradient(circle at 90% 60%, rgba(66, 165, 245, 0.02) 0%, transparent 40%);
  pointer-events: none;
  z-index: 0;
}

.login-container {
  width: 100%;
  max-width: 480px;
  position: relative;
  z-index: 1;
  background: var(--theme-background, #ffffff);
  border: 1px solid rgba(229, 231, 235, 0.4);
  border-radius: 12px;
  padding: 48px 40px;
  box-shadow: 
    0 4px 16px rgba(0, 0, 0, 0.08),
    0 2px 4px rgba(0, 0, 0, 0.04);
}

/* 平板端 (768px - 1024px) */
@media (min-width: 768px) and (max-width: 1024px) {
  .login-container {
    max-width: 520px;
    padding: 52px 48px;
  }
}

/* 桌面端 (1024px+) */
@media (min-width: 1024px) {
  .login-container {
    max-width: 560px;
    padding: 56px 52px;
  }
}

/* 语言切换 */
.language-switcher {
  position: absolute;
  top: -50px;
  right: 0;
  display: flex;
  gap: 8px;
}

.lang-btn {
  padding: 8px 14px;
  font-size: 13px;
  font-weight: 500;
  color: #6b7280;
  background: var(--theme-background, #ffffff);
  border: 1.5px solid rgba(229, 231, 235, 0.6);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.lang-btn:hover {
  background: #f9fafb;
  border-color: #d1d5db;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
}

.lang-btn.active {
  color: #165dff;
  background: linear-gradient(135deg, rgba(22, 93, 255, 0.1) 0%, rgba(22, 93, 255, 0.05) 100%);
  border-color: #165dff;
  box-shadow: 0 2px 8px rgba(22, 93, 255, 0.15);
}

/* 标题区域 */
.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-title {
  font-size: 32px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 12px 0;
  letter-spacing: -0.025em;
  line-height: 1.2;
}

.login-subtitle {
  font-size: 15px;
  color: #6b7280;
  margin: 0;
  line-height: 1.6;
}

.login-subtitle :deep(strong) {
  font-weight: 600;
  color: #111827;
}

/* 邮箱输入区域 */
.email-section {
  margin-bottom: 24px;
}

.email-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #111827;
  margin-bottom: 8px;
}

.email-input {
  width: 100%;
  padding: 14px 18px;
  font-size: 15px;
  color: #111827;
  background: var(--theme-background, #ffffff);
  border: 1.5px solid rgba(229, 231, 235, 0.6);
  border-radius: 10px;
  outline: none;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  box-sizing: border-box;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.email-input::placeholder {
  color: #9ca3af;
}

.email-input:focus {
  background: var(--theme-background, #ffffff);
  border-color: #165dff;
  box-shadow: 
    0 0 0 3px rgba(22, 93, 255, 0.1),
    0 2px 4px rgba(0, 0, 0, 0.08);
  transform: translateY(-1px);
}

.email-hint {
  font-size: 13px;
  color: #6b7280;
  margin: 8px 0 0 0;
  line-height: 1.4;
}

/* 验证码输入区域 */
.code-section {
  margin-bottom: 24px;
  animation: slideDown 0.3s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 错误提示 */
.error-message {
  padding: 14px 18px;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
  border: 1.5px solid #fecaca;
  border-radius: 10px;
  color: #dc2626;
  font-size: 14px;
  font-weight: 500;
  line-height: 1.5;
  box-shadow: 
    0 2px 8px rgba(239, 68, 68, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
}

.email-input.error {
  border-color: #ef4444;
  background: #fef2f2;
}

.email-input.error:focus {
  border-color: #ef4444;
  box-shadow: 
    0 0 0 3px rgba(239, 68, 68, 0.1),
    0 2px 4px rgba(0, 0, 0, 0.08);
}

/* 继续按钮 */
.continue-button {
  width: 100%;
  padding: 14px 24px;
  font-size: 15px;
  font-weight: 600;
  color: #ffffff;
  background: linear-gradient(135deg, #165dff 0%, #0e42d2 100%);
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  margin-bottom: 24px;
  box-shadow: 
    0 2px 8px rgba(22, 93, 255, 0.3),
    0 1px 2px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.continue-button:hover:not(:disabled) {
  background: linear-gradient(135deg, #0e42d2 0%, #0a35b8 100%);
  transform: translateY(-2px);
  box-shadow: 
    0 4px 12px rgba(22, 93, 255, 0.4),
    0 2px 4px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.continue-button:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 
    0 2px 4px rgba(22, 93, 255, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.continue-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.loading-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.loading-spinner-small {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #ffffff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 分隔线 */
.divider {
  display: flex;
  align-items: center;
  text-align: center;
  margin: 28px 0;
  color: #9ca3af;
  font-size: 13px;
  font-weight: 500;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: linear-gradient(to right, transparent, #e5e7eb, transparent);
}

.divider span {
  padding: 0 16px;
}

/* 社交登录按钮 */
.social-login {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 32px;
}

.social-button {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 14px 24px;
  font-size: 15px;
  font-weight: 500;
  background: var(--theme-background, #ffffff);
  border: 1.5px solid rgba(229, 231, 235, 0.6);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 
    0 1px 2px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.social-button:hover {
  background: #f9fafb;
  border-color: #d1d5db;
  transform: translateY(-1px);
  box-shadow: 
    0 2px 8px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.social-button:active {
  transform: translateY(0);
  box-shadow: 
    0 1px 2px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.social-button.github {
  background: linear-gradient(135deg, #24292e 0%, #1a1e22 100%);
  color: #ffffff;
  border-color: #24292e;
  box-shadow: 
    0 2px 8px rgba(36, 41, 46, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.social-button.github:hover {
  background: linear-gradient(135deg, #2f363d 0%, #24292e 100%);
  transform: translateY(-1px);
  box-shadow: 
    0 4px 12px rgba(36, 41, 46, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.social-button.google {
  color: #111827;
}

.social-button.microsoft {
  color: #111827;
}

.social-button.sso {
  color: #6b7280;
  text-decoration: none;
  background: transparent;
  border: none;
  box-shadow: none;
  padding: 10px;
  font-weight: 500;
}

.social-button.sso:hover {
  background: transparent;
  border: none;
  box-shadow: none;
  color: #165dff;
  text-decoration: underline;
}

.social-button svg {
  flex-shrink: 0;
}

/* 法律文本 */
.legal-text {
  font-size: 12px;
  color: #6b7280;
  text-align: center;
  line-height: 1.5;
  margin: 0;
}

.legal-text :deep(u) {
  text-decoration: underline;
  color: #111827;
  cursor: pointer;
}

.legal-text :deep(u):hover {
  color: #165dff;
}

/* ==================== 响应式设计 ==================== */

/* 小屏移动端 (最大 374px) */
@media (max-width: 374px) {
  .login-container {
    padding: 32px 24px;
    border-radius: 8px;
  }

  .login-title {
    font-size: 24px;
  }
  
  .login-subtitle {
    font-size: 14px;
  }

  .email-input,
  .continue-button,
  .social-button {
    padding: 12px 16px;
    font-size: 14px;
  }

  .language-switcher {
    top: -36px;
    gap: 6px;
  }

  .lang-btn {
    padding: 6px 10px;
    font-size: 12px;
  }
}

/* 移动端 (375px - 767px) */
@media (min-width: 375px) and (max-width: 767px) {
  .login-container {
    padding: 40px 32px;
  }

  .login-title {
    font-size: 28px;
  }
  
  .login-subtitle {
    font-size: 15px;
  }

  .language-switcher {
    top: -44px;
  }
}

/* 平板端 (768px - 1023px) */
@media (min-width: 768px) and (max-width: 1023px) {
  .login-title {
    font-size: 32px;
  }
  
  .login-subtitle {
    font-size: 16px;
  }

  .email-section,
  .code-section {
    margin-bottom: 28px;
  }

  .continue-button {
    margin-bottom: 28px;
  }

  .social-login {
    gap: 14px;
    margin-bottom: 36px;
  }

  .language-switcher {
    top: -48px;
  }
}

/* 桌面端 (1024px+) */
@media (min-width: 1024px) {
  .login-title {
    font-size: 36px;
  }
  
  .login-subtitle {
    font-size: 16px;
  }

  .email-section,
  .code-section {
    margin-bottom: 32px;
  }

  .continue-button {
    margin-bottom: 32px;
  }

  .social-login {
    gap: 16px;
    margin-bottom: 40px;
  }

  .language-switcher {
    top: -52px;
  }

  .divider {
    margin: 32px 0;
  }
}

/* 大屏桌面端 (1440px+) */
@media (min-width: 1440px) {
  .login-container {
    max-width: 600px;
    padding: 64px 56px;
  }

  .login-title {
    font-size: 40px;
    margin-bottom: 16px;
  }
  
  .login-subtitle {
    font-size: 17px;
  }
}

/* 横屏模式优化 */
@media (orientation: landscape) and (max-height: 600px) {
  .login-page {
    padding: 16px 20px;
    align-items: flex-start;
    padding-top: 20px;
  }

  .login-container {
    padding: 32px 40px;
    margin-top: 0;
  }

  .login-header {
    margin-bottom: 24px;
  }

  .login-title {
    font-size: 24px;
    margin-bottom: 8px;
  }

  .login-subtitle {
    font-size: 14px;
  }

  .email-section,
  .code-section {
    margin-bottom: 16px;
  }

  .continue-button {
    margin-bottom: 16px;
  }

  .social-login {
    margin-bottom: 20px;
  }

  .language-switcher {
    top: -36px;
  }
}
</style>
