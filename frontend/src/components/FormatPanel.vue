<template>
  <Teleport to="body">
    <div class="format-panel" @click.stop>
      <div class="format-panel-content">
        <!-- 头部 -->
        <div class="format-panel-header">
          <div class="header-left">
            <div class="header-icon-wrapper">
              <svg width="18" height="18" viewBox="0 0 18 18" fill="none" class="header-icon">
                <path d="M3 6h12M3 9h12M3 12h9" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                <circle cx="13.5" cy="6" r="1.5" fill="currentColor"/>
                <circle cx="13.5" cy="9" r="1.5" fill="currentColor"/>
                <circle cx="12" cy="12" r="1.5" fill="currentColor"/>
              </svg>
            </div>
            <h3 class="format-title">格式</h3>
          </div>
          <button @click="handleClose" class="format-close-btn" title="收起">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M4 4l8 8M12 4l-8 8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
          </button>
        </div>
        
        <!-- 标签页 -->
        <div class="format-tabs">
          <button 
            class="format-tab" 
            :class="{ active: activeTab === 'style' }"
            @click="activeTab = 'style'"
          >
            样式
          </button>
          <button 
            class="format-tab" 
            :class="{ active: activeTab === 'size' }"
            @click="activeTab = 'size'"
          >
            大小
          </button>
          <button 
            class="format-tab" 
            :class="{ active: activeTab === 'color' }"
            @click="activeTab = 'color'"
          >
            颜色
          </button>
          <button 
            class="format-tab" 
            :class="{ active: activeTab === 'effect' }"
            @click="activeTab = 'effect'"
          >
            效果
          </button>
        </div>
        
        <div class="format-sections">
          <!-- Style 标签页 -->
          <div v-if="activeTab === 'style'" class="format-tab-content">
            <!-- 文本样式 -->
            <div class="format-group">
              <div class="format-group-title">文本样式</div>
              <div class="format-select-wrapper">
                <select 
                  class="format-select" 
                  v-model="currentTextStyle"
                  @change="applyTextStyle"
                >
                  <option value="paragraph">正文</option>
                  <option value="heading1">标题 1</option>
                  <option value="heading2">标题 2</option>
                  <option value="heading3">标题 3</option>
                  <option value="heading4">标题 4</option>
                  <option value="heading5">标题 5</option>
                  <option value="heading6">标题 6</option>
                  <option value="blockquote">引用块</option>
                  <option value="codeBlock">代码块</option>
                </select>
              </div>
            </div>
            
            <!-- 文本格式工具栏 -->
            <div class="format-group">
              <div class="format-group-title">文本格式</div>
              <div class="format-icon-buttons">
                <button
                  class="format-icon-btn"
                  :class="{ active: isBold }"
                  @click="toggleBold"
                  title="粗体 (Ctrl+B)"
                >
                  <strong>B</strong>
                </button>
                <button
                  class="format-icon-btn"
                  :class="{ active: isItalic }"
                  @click="toggleItalic"
                  title="斜体 (Ctrl+I)"
                >
                  <em>I</em>
                </button>
                <button
                  class="format-icon-btn"
                  :class="{ active: isStrike }"
                  @click="toggleStrike"
                  title="删除线"
                >
                  <s>S</s>
                </button>
                <button
                  class="format-icon-btn"
                  :class="{ active: isCode }"
                  @click="toggleCode"
                  title="行内代码"
                >
                  &lt;/&gt;
                </button>
                <button
                  class="format-icon-btn"
                  :class="{ active: isUnderline }"
                  @click="toggleUnderline"
                  title="下划线"
                >
                  <u>U</u>
                </button>
                <button
                  class="format-icon-btn"
                  :class="{ active: isHighlight }"
                  @click="toggleHighlight"
                  title="高亮"
                >
                  🖍
                </button>
                <button
                  class="format-icon-btn"
                  :class="{ active: isSubscript }"
                  @click="toggleSubscript"
                  title="下标"
                >
                  <span style="font-size: 0.7em; vertical-align: sub;">x₂</span>
                </button>
                <button
                  class="format-icon-btn"
                  :class="{ active: isSuperscript }"
                  @click="toggleSuperscript"
                  title="上标"
                >
                  <span style="font-size: 0.7em; vertical-align: super;">x²</span>
                </button>
              </div>
            </div>
            
            <!-- 对齐方式 -->
            <div class="format-group">
              <div class="format-group-title">对齐方式</div>
              <div class="format-icon-buttons">
                <button
                  class="format-icon-btn"
                  :class="{ active: isAlignLeft }"
                  @click="alignLeft"
                  title="左对齐"
                >
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <path d="M2 4h12M2 8h10M2 12h8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                </button>
                <button
                  class="format-icon-btn"
                  :class="{ active: isAlignCenter }"
                  @click="alignCenter"
                  title="居中"
                >
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <path d="M3 4h10M2 8h12M4 12h8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                </button>
                <button
                  class="format-icon-btn"
                  :class="{ active: isAlignRight }"
                  @click="alignRight"
                  title="右对齐"
                >
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <path d="M2 4h12M4 8h10M6 12h8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                </button>
                <button
                  class="format-icon-btn"
                  :class="{ active: isAlignJustify }"
                  @click="alignJustify"
                  title="两端对齐"
                >
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <path d="M2 4h12M2 8h12M2 12h12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                </button>
              </div>
            </div>
            
            <!-- 列表 -->
            <div class="format-group">
              <div class="format-group-title">列表</div>
              <div class="format-icon-buttons">
                <button
                  class="format-icon-btn"
                  :class="{ active: isBulletList }"
                  @click="toggleBulletList"
                  title="无序列表"
                >
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <circle cx="4" cy="4" r="1.5" fill="currentColor"/>
                    <circle cx="4" cy="8" r="1.5" fill="currentColor"/>
                    <circle cx="4" cy="12" r="1.5" fill="currentColor"/>
                    <path d="M7 4h6M7 8h6M7 12h4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                </button>
                <button
                  class="format-icon-btn"
                  :class="{ active: isOrderedList }"
                  @click="toggleOrderedList"
                  title="有序列表"
                >
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <path d="M2 3h2M2 8h2M2 13h2M6 3h8M6 8h8M6 13h6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                </button>
                <button
                  class="format-icon-btn"
                  :class="{ active: isTaskList }"
                  @click="toggleTaskList"
                  title="任务列表"
                >
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <rect x="2" y="2" width="12" height="12" rx="2" stroke="currentColor" stroke-width="1.5" fill="none"/>
                    <path v-if="isTaskList" d="M5 8l2 2 4-4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </button>
              </div>
            </div>
            
            <!-- 缩进控制 -->
            <div class="format-group">
              <div class="format-group-title">缩进</div>
              <div class="format-icon-buttons">
                <button
                  class="format-icon-btn"
                  @click="decreaseIndent"
                  title="减少缩进"
                >
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <path d="M2 4h12M4 8h10M6 12h8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    <path d="M2 8l3-3M2 8l3 3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </button>
                <button
                  class="format-icon-btn"
                  @click="increaseIndent"
                  title="增加缩进"
                >
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <path d="M2 4h12M4 8h10M6 12h8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    <path d="M14 8l-3-3M14 8l-3 3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </button>
              </div>
            </div>
            
            <!-- 字体 -->
            <div class="format-group">
              <div class="format-group-title">字体</div>
              <div class="format-buttons">
                <button
                  v-for="font in fonts"
                  :key="font.value"
                  class="format-btn"
                  :class="{ active: currentFont === font.value }"
                  @click="setFont(font.value)"
                >
                  {{ font.label }}
                </button>
              </div>
            </div>
            
            <!-- 文本转换 -->
            <div class="format-group">
              <div class="format-group-title">文本转换</div>
              <div class="format-buttons">
                <button
                  class="format-btn"
                  @click="transformToUppercase"
                  title="转换为大写"
                >
                  <span style="text-transform: uppercase;">ABC</span> 大写
                </button>
                <button
                  class="format-btn"
                  @click="transformToLowercase"
                  title="转换为小写"
                >
                  <span style="text-transform: lowercase;">abc</span> 小写
                </button>
                <button
                  class="format-btn"
                  @click="transformToCapitalize"
                  title="首字母大写"
                >
                  <span style="text-transform: capitalize;">Abc</span> 首字母大写
                </button>
              </div>
            </div>
            
            <!-- 文本装饰 -->
            <div class="format-group">
              <div class="format-group-title">文本装饰</div>
              <div class="format-icon-buttons">
                <button
                  class="format-icon-btn"
                  :class="{ active: hasTextShadow }"
                  @click="toggleTextShadow"
                  title="文字阴影"
                >
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <path d="M3 8h10M8 3v10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    <circle cx="8" cy="8" r="2" stroke="currentColor" stroke-width="1.5" fill="none" opacity="0.5"/>
                  </svg>
                </button>
                <button
                  class="format-icon-btn"
                  :class="{ active: hasLetterSpacing }"
                  @click="toggleLetterSpacing"
                  title="字符间距"
                >
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <path d="M2 4h2M2 8h2M2 12h2M6 4h8M6 8h8M6 12h8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                </button>
              </div>
            </div>
          </div>
          
          <!-- Size 标签页 -->
          <div v-if="activeTab === 'size'" class="format-tab-content">
            <div class="format-group">
              <div class="format-group-title">字体大小</div>
              <div class="format-select-wrapper">
                <select 
                  class="format-select" 
                  v-model="fontSize"
                  @change="setFontSize"
                >
                  <option v-for="size in fontSizes" :key="size" :value="size">{{ size }} px</option>
                </select>
              </div>
            </div>
            
            <div class="format-group">
              <div class="format-group-title">行高</div>
              <div class="format-select-wrapper">
                <select 
                  class="format-select" 
                  v-model="lineHeight"
                  @change="setLineHeight"
                >
                  <option v-for="height in lineHeights" :key="height" :value="height">{{ height }}</option>
                </select>
              </div>
            </div>
            
            <div class="format-group">
              <div class="format-group-title">字间距</div>
              <div class="format-select-wrapper">
                <select 
                  class="format-select" 
                  v-model="letterSpacing"
                  @change="setLetterSpacing"
                >
                  <option v-for="spacing in letterSpacings" :key="spacing" :value="spacing">{{ spacing }}</option>
                </select>
              </div>
            </div>
            
            <div class="format-group">
              <div class="format-group-title">段落间距</div>
              <div class="format-select-wrapper">
                <select 
                  class="format-select" 
                  v-model="paragraphMargin"
                  @change="setParagraphMargin"
                >
                  <option v-for="margin in paragraphMargins" :key="margin" :value="margin">{{ margin }} px</option>
                </select>
              </div>
            </div>
            
            <div class="format-group">
              <div class="format-group-title">首行缩进</div>
              <div class="format-select-wrapper">
                <select 
                  class="format-select" 
                  v-model="textIndent"
                  @change="setTextIndent"
                >
                  <option v-for="indent in textIndents" :key="indent" :value="indent">{{ indent === 0 ? '无' : indent + ' px' }}</option>
                </select>
              </div>
            </div>
            
            <div class="format-group">
              <div class="format-group-title">字重</div>
              <div class="format-select-wrapper">
                <select 
                  class="format-select" 
                  v-model="fontWeight"
                  @change="setFontWeight"
                >
                  <option v-for="weight in fontWeights" :key="weight.value" :value="weight.value">{{ weight.label }}</option>
                </select>
              </div>
            </div>
          </div>
          
          <!-- Color 标签页 -->
          <div v-if="activeTab === 'color'" class="format-tab-content">
            <div class="format-group">
              <div class="format-group-title">文字颜色</div>
              <div class="color-grid">
                <button
                  v-for="color in textColors"
                  :key="color.value"
                  class="color-btn"
                  :class="{ active: currentTextColor === color.value }"
                  :style="{ background: color.value }"
                  @click="setTextColor(color.value)"
                  :title="color.name"
                >
                  <svg v-if="currentTextColor === color.value" width="14" height="14" viewBox="0 0 14 14" fill="none">
                    <path d="M3 7l2 2 6-6" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </button>
                <button
                  class="color-btn color-picker-btn"
                  @click="openColorPicker('text')"
                  title="自定义颜色"
                >
                  <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                    <path d="M7 2v10M2 7h10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    <circle cx="7" cy="7" r="2" stroke="currentColor" stroke-width="1.5" fill="none"/>
                  </svg>
                </button>
              </div>
            </div>
            
            <div class="format-group">
              <div class="format-group-title">背景颜色</div>
              <div class="color-grid">
                <button
                  v-for="color in backgroundColors"
                  :key="color.value"
                  class="color-btn"
                  :class="{ active: currentBackgroundColor === color.value }"
                  :style="{ background: color.value }"
                  @click="setBackgroundColor(color.value)"
                  :title="color.name"
                >
                  <svg v-if="currentBackgroundColor === color.value" width="14" height="14" viewBox="0 0 14 14" fill="none">
                    <path d="M3 7l2 2 6-6" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </button>
                <button
                  class="color-btn color-picker-btn"
                  @click="openColorPicker('background')"
                  title="自定义颜色"
                >
                  <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                    <path d="M7 2v10M2 7h10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    <circle cx="7" cy="7" r="2" stroke="currentColor" stroke-width="1.5" fill="none"/>
                  </svg>
                </button>
              </div>
            </div>
            
            <div class="format-group">
              <div class="format-group-title">高亮颜色</div>
              <div class="color-grid">
                <button
                  v-for="color in highlightColors"
                  :key="color.value"
                  class="color-btn"
                  :class="{ active: currentHighlightColor === color.value }"
                  :style="{ background: color.value }"
                  @click="setHighlightColor(color.value)"
                  :title="color.name"
                >
                  <svg v-if="currentHighlightColor === color.value" width="14" height="14" viewBox="0 0 14 14" fill="none">
                    <path d="M3 7l2 2 6-6" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </button>
                <button
                  class="color-btn color-picker-btn"
                  @click="openColorPicker('highlight')"
                  title="自定义颜色"
                >
                  <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                    <path d="M7 2v10M2 7h10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    <circle cx="7" cy="7" r="2" stroke="currentColor" stroke-width="1.5" fill="none"/>
                  </svg>
                </button>
              </div>
            </div>
            
            <!-- 渐变文字 -->
            <div class="format-group">
              <div class="format-group-title">渐变文字</div>
              <div class="format-buttons">
                <button
                  v-for="gradient in textGradients"
                  :key="gradient.name"
                  class="format-btn gradient-btn"
                  :style="{ background: gradient.value }"
                  @click="applyTextGradient(gradient.value)"
                  :title="gradient.name"
                >
                  {{ gradient.name }}
                </button>
              </div>
            </div>
          </div>
          
          <!-- Effect 标签页 -->
          <div v-if="activeTab === 'effect'" class="format-tab-content">
            <!-- 边框 -->
            <div class="format-group">
              <div class="format-group-title">边框</div>
              <div class="format-buttons">
                <button
                  v-for="border in borders"
                  :key="border.name"
                  class="format-btn"
                  :class="{ active: currentBorder === border.name }"
                  @click="applyBorder(border)"
                  :title="border.name"
                >
                  <span :style="{ border: border.style }" style="display: inline-block; padding: 4px 8px; border-radius: 4px;">
                    {{ border.name }}
                  </span>
                </button>
              </div>
            </div>
            
            <!-- 阴影 -->
            <div class="format-group">
              <div class="format-group-title">文字阴影</div>
              <div class="format-buttons">
                <button
                  v-for="shadow in textShadows"
                  :key="shadow.name"
                  class="format-btn"
                  :class="{ active: currentTextShadow === shadow.name }"
                  @click="applyTextShadow(shadow)"
                  :title="shadow.name"
                >
                  <span :style="{ textShadow: shadow.value }">
                    {{ shadow.name }}
                  </span>
                </button>
              </div>
            </div>
            
            <!-- 背景样式 -->
            <div class="format-group">
              <div class="format-group-title">背景样式</div>
              <div class="format-buttons">
                <button
                  v-for="bg in backgroundStyles"
                  :key="bg.name"
                  class="format-btn"
                  :class="{ active: currentBackgroundStyle === bg.name }"
                  @click="applyBackgroundStyle(bg)"
                  :title="bg.name"
                >
                  <span :style="{ background: bg.value, padding: '4px 8px', borderRadius: '4px', display: 'inline-block' }">
                    {{ bg.name }}
                  </span>
                </button>
              </div>
            </div>
            
            <!-- 文本效果 -->
            <div class="format-group">
              <div class="format-group-title">文本效果</div>
              <div class="format-icon-buttons">
                <button
                  class="format-icon-btn"
                  :class="{ active: hasOutline }"
                  @click="toggleOutline"
                  title="文字描边"
                >
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <text x="8" y="12" font-size="12" text-anchor="middle" stroke="currentColor" fill="none" stroke-width="0.5">A</text>
                    <text x="8" y="12" font-size="12" text-anchor="middle" fill="currentColor">A</text>
                  </svg>
                </button>
                <button
                  class="format-icon-btn"
                  :class="{ active: hasBlur }"
                  @click="toggleBlur"
                  title="模糊效果"
                >
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <circle cx="8" cy="8" r="4" fill="currentColor" opacity="0.5" style="filter: blur(2px);"/>
                  </svg>
                </button>
              </div>
            </div>
          </div>
          
          <!-- 清除格式按钮 -->
          <div class="format-clear-section">
            <button
              class="format-clear-btn"
              @click="clearFormat"
              title="清除格式"
            >
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                <path d="M2 2l10 10M12 2L2 12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
              <span>清除格式</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Teleport } from 'vue'

const emit = defineEmits(['close'])

const props = defineProps({
  editor: {
    type: Object,
    default: null
  }
})

const activeTab = ref('style')
const currentTextColor = ref(null)
const currentBackgroundColor = ref(null)
const currentHighlightColor = ref(null)
const currentTextStyle = ref('paragraph')
const currentFont = ref('system')

// 下拉选择框的值
const fontSize = ref(16)
const lineHeight = ref(1.5)
const letterSpacing = ref('normal')
const paragraphMargin = ref(16)
const textIndent = ref(0)
const fontWeight = ref('normal')

// 选项列表
const fonts = [
  { label: 'Aa System', value: 'system' },
  { label: 'Ss Serif', value: 'serif' },
  { label: '00 Mono', value: 'mono' },
  { label: 'Rr Round', value: 'round' }
]
const fontSizes = [10, 11, 12, 13, 14, 15, 16, 18, 20, 22, 24, 26, 28, 30, 32, 36, 40, 44, 48, 56, 64, 72]
const lineHeights = [1.0, 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 2.0, 2.5, 3.0]
const letterSpacings = ['normal', '-0.5px', '0px', '0.5px', '1px', '1.5px', '2px', '3px', '4px']
const paragraphMargins = [0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 24, 28, 32, 36, 40, 48]
const textIndents = [0, 16, 24, 32, 40, 48, 56, 64]
const fontWeights = [
  { label: '正常', value: 'normal' },
  { label: '细体', value: '300' },
  { label: '常规', value: '400' },
  { label: '中等', value: '500' },
  { label: '半粗', value: '600' },
  { label: '粗体', value: '700' },
  { label: '特粗', value: '800' },
  { label: '最粗', value: '900' }
]

const textColors = [
  { name: '黑色', value: '#000000' },
  { name: '深灰', value: '#374151' },
  { name: '灰色', value: '#6b7280' },
  { name: '浅灰', value: '#9ca3af' },
  { name: '蓝色', value: '#3b82f6' },
  { name: '绿色', value: '#10b981' },
  { name: '红色', value: '#ef4444' },
  { name: '橙色', value: '#f59e0b' },
  { name: '紫色', value: '#8b5cf6' },
  { name: '粉色', value: '#ec4899' },
  { name: '黄色', value: '#eab308' },
  { name: '青色', value: '#06b6d4' },
  { name: '深蓝', value: '#1e40af' },
  { name: '深绿', value: '#059669' },
  { name: '深红', value: '#dc2626' },
  { name: '棕色', value: '#92400e' },
  { name: '靛蓝', value: '#4f46e5' },
  { name: '青绿', value: '#14b8a6' },
  { name: '玫瑰', value: '#f43f5e' },
  { name: '琥珀', value: '#f97316' },
  { name: '酸橙', value: '#84cc16' },
  { name: '天蓝', value: '#0ea5e9' }
]

const backgroundColors = [
  { name: '白色', value: '#ffffff' },
  { name: '浅灰', value: '#f3f4f6' },
  { name: '灰色', value: '#e5e7eb' },
  { name: '浅蓝', value: '#dbeafe' },
  { name: '浅绿', value: '#d1fae5' },
  { name: '浅红', value: '#fee2e2' },
  { name: '浅黄', value: '#fef3c7' },
  { name: '浅紫', value: '#ede9fe' },
  { name: '浅粉', value: '#fce7f3' },
  { name: '浅青', value: '#cffafe' },
  { name: '浅橙', value: '#fed7aa' },
  { name: '浅棕', value: '#fef3c7' }
]

const highlightColors = [
  { name: '黄色', value: '#fef08a' },
  { name: '绿色', value: '#bbf7d0' },
  { name: '蓝色', value: '#bfdbfe' },
  { name: '粉色', value: '#fbcfe8' },
  { name: '紫色', value: '#e9d5ff' },
  { name: '橙色', value: '#fed7aa' },
  { name: '红色', value: '#fecaca' },
  { name: '青色', value: '#a5f3fc' }
]

const textGradients = [
  { name: '蓝紫渐变', value: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { name: '粉橙渐变', value: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
  { name: '青蓝渐变', value: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
  { name: '绿黄渐变', value: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)' },
  { name: '红粉渐变', value: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)' },
  { name: '紫蓝渐变', value: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)' }
]

const borders = [
  { name: '无边框', style: 'none' },
  { name: '细边框', style: '1px solid rgba(0, 0, 0, 0.1)' },
  { name: '中边框', style: '2px solid rgba(0, 0, 0, 0.2)' },
  { name: '粗边框', style: '3px solid rgba(0, 0, 0, 0.3)' },
  { name: '虚线', style: '2px dashed rgba(0, 0, 0, 0.2)' },
  { name: '点线', style: '2px dotted rgba(0, 0, 0, 0.2)' },
  { name: '蓝色边框', style: '2px solid #3b82f6' },
  { name: '圆角边框', style: '2px solid rgba(0, 0, 0, 0.2)', borderRadius: '8px' }
]

const textShadows = [
  { name: '无阴影', value: 'none' },
  { name: '轻微', value: '0 1px 2px rgba(0, 0, 0, 0.1)' },
  { name: '中等', value: '0 2px 4px rgba(0, 0, 0, 0.15)' },
  { name: '强烈', value: '0 4px 8px rgba(0, 0, 0, 0.2)' },
  { name: '发光', value: '0 0 8px rgba(22, 93, 255, 0.5)' },
  { name: '立体', value: '1px 1px 2px rgba(0, 0, 0, 0.3), -1px -1px 2px rgba(255, 255, 255, 0.3)' }
]

const backgroundStyles = [
  { name: '无背景', value: 'transparent' },
  { name: '浅灰', value: 'linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%)' },
  { name: '浅蓝', value: 'linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%)' },
  { name: '浅粉', value: 'linear-gradient(135deg, #fce7f3 0%, #fbcfe8 100%)' },
  { name: '浅绿', value: 'linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%)' },
  { name: '浅黄', value: 'linear-gradient(135deg, #fef3c7 0%, #fde68a 100%)' },
  { name: '条纹', value: 'repeating-linear-gradient(45deg, #f3f4f6, #f3f4f6 10px, #e5e7eb 10px, #e5e7eb 20px)' },
  { name: '网格', value: 'linear-gradient(rgba(0, 0, 0, 0.05) 1px, transparent 1px), linear-gradient(90deg, rgba(0, 0, 0, 0.05) 1px, transparent 1px)' }
]

const currentBorder = ref(null)
const currentTextShadow = ref(null)
const currentBackgroundStyle = ref(null)
const hasOutline = ref(false)
const hasBlur = ref(false)

// 处理关闭
const handleClose = (event) => {
  event.preventDefault()
  event.stopPropagation()
  emit('close')
  if (event.target) {
    event.target.blur()
  }
}

const isBold = computed(() => {
  return props.editor?.isActive('bold')
})

const isItalic = computed(() => {
  return props.editor?.isActive('italic')
})

const isStrike = computed(() => {
  return props.editor?.isActive('strike')
})

const isBulletList = computed(() => {
  return props.editor?.isActive('bulletList')
})

const isOrderedList = computed(() => {
  return props.editor?.isActive('orderedList')
})

const isTaskList = computed(() => {
  return props.editor?.isActive('taskList')
})

const isCode = computed(() => {
  return props.editor?.isActive('code')
})

const isUnderline = computed(() => {
  // 检查是否有下划线样式
  if (!props.editor) return false
  const { selection } = props.editor.state
  if (selection.empty) return false
  const { from, to } = selection
  const text = props.editor.state.doc.textBetween(from, to)
  // 简单检查：如果选中文本包含下划线标记，返回true
  return false // TipTap默认不支持下划线，需要扩展
})

const isHighlight = computed(() => {
  // 检查是否有高亮标记
  if (!props.editor) return false
  return false // TipTap默认不支持高亮，需要扩展
})

const isSubscript = computed(() => {
  // 检查是否有下标
  if (!props.editor) return false
  return false // TipTap默认不支持下标，需要扩展
})

const isSuperscript = computed(() => {
  // 检查是否有上标
  if (!props.editor) return false
  return false // TipTap默认不支持上标，需要扩展
})

const hasTextShadow = computed(() => {
  if (!props.editor) return false
  return false // 需要检查样式
})

const hasLetterSpacing = computed(() => {
  if (!props.editor) return false
  return letterSpacing.value !== 'normal'
})

const isAlignLeft = computed(() => {
  return props.editor?.isActive({ textAlign: 'left' })
})

const isAlignCenter = computed(() => {
  return props.editor?.isActive({ textAlign: 'center' })
})

const isAlignRight = computed(() => {
  return props.editor?.isActive({ textAlign: 'right' })
})

const isAlignJustify = computed(() => {
  return props.editor?.isActive({ textAlign: 'justify' })
})

const applyTextStyle = () => {
  if (!props.editor) return
  
  const style = currentTextStyle.value
  if (style === 'paragraph') {
    props.editor.chain().focus().setParagraph().run()
  } else if (style.startsWith('heading')) {
    const level = parseInt(style.replace('heading', ''))
    props.editor.chain().focus().toggleHeading({ level }).run()
  } else if (style === 'blockquote') {
    props.editor.chain().focus().toggleBlockquote().run()
  } else if (style === 'codeBlock') {
    props.editor.chain().focus().toggleCodeBlock().run()
  }
}

// 监听编辑器状态，更新文本样式下拉框
watch(() => props.editor?.state, () => {
  if (!props.editor) return
  
  if (props.editor.isActive('paragraph')) {
    currentTextStyle.value = 'paragraph'
  } else if (props.editor.isActive('heading', { level: 1 })) {
    currentTextStyle.value = 'heading1'
  } else if (props.editor.isActive('heading', { level: 2 })) {
    currentTextStyle.value = 'heading2'
  } else if (props.editor.isActive('heading', { level: 3 })) {
    currentTextStyle.value = 'heading3'
  } else if (props.editor.isActive('heading', { level: 4 })) {
    currentTextStyle.value = 'heading4'
  } else if (props.editor.isActive('heading', { level: 5 })) {
    currentTextStyle.value = 'heading5'
  } else if (props.editor.isActive('heading', { level: 6 })) {
    currentTextStyle.value = 'heading6'
  } else if (props.editor.isActive('blockquote')) {
    currentTextStyle.value = 'blockquote'
  } else if (props.editor.isActive('codeBlock')) {
    currentTextStyle.value = 'codeBlock'
  }
}, { deep: true, immediate: true })

const toggleBold = () => {
  props.editor?.chain().focus().toggleBold().run()
}

const toggleItalic = () => {
  props.editor?.chain().focus().toggleItalic().run()
}

const toggleStrike = () => {
  props.editor?.chain().focus().toggleStrike().run()
}

const toggleBulletList = () => {
  props.editor?.chain().focus().toggleBulletList().run()
}

const toggleOrderedList = () => {
  props.editor?.chain().focus().toggleOrderedList().run()
}

const toggleTaskList = () => {
  props.editor?.chain().focus().toggleTaskList().run()
}

const toggleCode = () => {
  props.editor?.chain().focus().toggleCode().run()
}

const toggleUnderline = () => {
  if (!props.editor) return
  const { selection } = props.editor.state
  const { from, to } = selection
  
  if (selection.empty) {
    // 插入带下划线的空格
    const html = `<span style="text-decoration: underline;">&nbsp;</span>`
    props.editor.chain().focus().insertContent(html).run()
  } else {
    // 应用下划线
    const text = props.editor.state.doc.textBetween(from, to)
    const escapedText = text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;')
    
    const html = `<span style="text-decoration: underline;">${escapedText}</span>`
    props.editor.chain()
      .focus()
      .deleteSelection()
      .insertContent(html)
      .run()
  }
}

const toggleHighlight = () => {
  if (!props.editor) return
  const { selection } = props.editor.state
  const { from, to } = selection
  
  if (selection.empty) {
    const html = `<mark style="background-color: #fef08a;">&nbsp;</mark>`
    props.editor.chain().focus().insertContent(html).run()
  } else {
    const text = props.editor.state.doc.textBetween(from, to)
    const escapedText = text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;')
    
    const html = `<mark style="background-color: #fef08a;">${escapedText}</mark>`
    props.editor.chain()
      .focus()
      .deleteSelection()
      .insertContent(html)
      .run()
  }
}

const toggleSubscript = () => {
  if (!props.editor) return
  const { selection } = props.editor.state
  const { from, to } = selection
  
  if (selection.empty) {
    const html = `<sub>&nbsp;</sub>`
    props.editor.chain().focus().insertContent(html).run()
  } else {
    const text = props.editor.state.doc.textBetween(from, to)
    const escapedText = text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;')
    
    const html = `<sub>${escapedText}</sub>`
    props.editor.chain()
      .focus()
      .deleteSelection()
      .insertContent(html)
      .run()
  }
}

const toggleSuperscript = () => {
  if (!props.editor) return
  const { selection } = props.editor.state
  const { from, to } = selection
  
  if (selection.empty) {
    const html = `<sup>&nbsp;</sup>`
    props.editor.chain().focus().insertContent(html).run()
  } else {
    const text = props.editor.state.doc.textBetween(from, to)
    const escapedText = text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;')
    
    const html = `<sup>${escapedText}</sup>`
    props.editor.chain()
      .focus()
      .deleteSelection()
      .insertContent(html)
      .run()
  }
}

const alignLeft = () => {
  if (props.editor?.can().setTextAlign) {
    props.editor.chain().focus().setTextAlign('left').run()
  } else {
    // 使用CSS样式
    const { $anchor } = props.editor.state.selection
    const node = $anchor.parent
    if (node) {
      const currentStyle = node.attrs.style || ''
      const newStyle = currentStyle.replace(/text-align:\s*[^;]+;?/g, '') + 'text-align: left;'
      props.editor.chain().focus().updateAttributes(node.type.name, { style: newStyle.trim() }).run()
    }
  }
}

const alignCenter = () => {
  if (props.editor?.can().setTextAlign) {
    props.editor.chain().focus().setTextAlign('center').run()
  } else {
    const { $anchor } = props.editor.state.selection
    const node = $anchor.parent
    if (node) {
      const currentStyle = node.attrs.style || ''
      const newStyle = currentStyle.replace(/text-align:\s*[^;]+;?/g, '') + 'text-align: center;'
      props.editor.chain().focus().updateAttributes(node.type.name, { style: newStyle.trim() }).run()
    }
  }
}

const alignRight = () => {
  if (props.editor?.can().setTextAlign) {
    props.editor.chain().focus().setTextAlign('right').run()
  } else {
    const { $anchor } = props.editor.state.selection
    const node = $anchor.parent
    if (node) {
      const currentStyle = node.attrs.style || ''
      const newStyle = currentStyle.replace(/text-align:\s*[^;]+;?/g, '') + 'text-align: right;'
      props.editor.chain().focus().updateAttributes(node.type.name, { style: newStyle.trim() }).run()
    }
  }
}

const alignJustify = () => {
  if (props.editor?.can().setTextAlign) {
    props.editor.chain().focus().setTextAlign('justify').run()
  } else {
    const { $anchor } = props.editor.state.selection
    const node = $anchor.parent
    if (node) {
      const currentStyle = node.attrs.style || ''
      const newStyle = currentStyle.replace(/text-align:\s*[^;]+;?/g, '') + 'text-align: justify;'
      props.editor.chain().focus().updateAttributes(node.type.name, { style: newStyle.trim() }).run()
    }
  }
}

const clearFormat = () => {
  props.editor?.chain().focus().clearNodes().unsetAllMarks().run()
}

// 颜色功能
const setTextColor = (color) => {
  if (!props.editor) return
  currentTextColor.value = color
  
  const { state } = props.editor
  const { selection } = state
  const { from, to } = selection
  
  if (selection.empty) {
    const html = `<span style="color: ${color};">&nbsp;</span>`
    props.editor.chain().focus().insertContent(html).run()
  } else {
    const text = state.doc.textBetween(from, to)
    if (!text) return
    
    const escapedText = text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;')
    
    const html = `<span style="color: ${color};">${escapedText}</span>`
    
    props.editor.chain()
      .focus()
      .deleteSelection()
      .insertContent(html)
      .run()
  }
}

const setBackgroundColor = (color) => {
  if (!props.editor) return
  currentBackgroundColor.value = color
  
  const { state } = props.editor
  const { selection } = state
  const { from, to } = selection
  
  if (selection.empty) {
    const html = `<span style="background-color: ${color};">&nbsp;</span>`
    props.editor.chain().focus().insertContent(html).run()
  } else {
    const text = state.doc.textBetween(from, to)
    if (!text) return
    
    const escapedText = text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;')
    
    const html = `<span style="background-color: ${color};">${escapedText}</span>`
    
    props.editor.chain()
      .focus()
      .deleteSelection()
      .insertContent(html)
      .run()
  }
}

const setHighlightColor = (color) => {
  if (!props.editor) return
  currentHighlightColor.value = color
  
  const { state } = props.editor
  const { selection } = state
  const { from, to } = selection
  
  if (selection.empty) {
    const html = `<mark style="background-color: ${color};">&nbsp;</mark>`
    props.editor.chain().focus().insertContent(html).run()
  } else {
    const text = state.doc.textBetween(from, to)
    if (!text) return
    
    const escapedText = text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;')
    
    const html = `<mark style="background-color: ${color};">${escapedText}</mark>`
    
    props.editor.chain()
      .focus()
      .deleteSelection()
      .insertContent(html)
      .run()
  }
}

const setFontSize = () => {
  if (!props.editor) return
  const selection = props.editor.state.selection
  
  if (!selection.empty) {
    const text = props.editor.state.doc.textBetween(selection.from, selection.to)
    const span = document.createElement('span')
    span.style.fontSize = `${fontSize.value}px`
    span.textContent = text
    
    props.editor.chain()
      .focus()
      .deleteSelection()
      .insertContent(span)
      .run()
  } else {
    const { $anchor } = props.editor.state.selection
    const paragraph = $anchor.parent
    if (paragraph) {
      const currentStyle = paragraph.attrs.style || ''
      const newStyle = currentStyle.replace(/font-size:\s*[^;]+;?/g, '') + `font-size: ${fontSize.value}px;`
      props.editor.chain().focus()
        .updateAttributes('paragraph', { style: newStyle.trim() })
        .run()
    }
  }
}

const setLineHeight = () => {
  if (!props.editor) return
  const { $anchor } = props.editor.state.selection
  const paragraph = $anchor.parent
  if (paragraph) {
    const currentStyle = paragraph.attrs.style || ''
    const newStyle = currentStyle.replace(/line-height:\s*[^;]+;?/g, '') + `line-height: ${lineHeight.value};`
    props.editor.chain().focus()
      .updateAttributes('paragraph', { style: newStyle.trim() })
      .run()
  }
}

const setLetterSpacing = () => {
  if (!props.editor) return
  const selection = props.editor.state.selection
  const spacing = letterSpacing.value === 'normal' ? 'normal' : letterSpacing.value
  
  if (!selection.empty) {
    const text = props.editor.state.doc.textBetween(selection.from, selection.to)
    const span = document.createElement('span')
    span.style.letterSpacing = spacing
    span.textContent = text
    
    props.editor.chain()
      .focus()
      .deleteSelection()
      .insertContent(span)
      .run()
  } else {
    const { $anchor } = props.editor.state.selection
    const paragraph = $anchor.parent
    if (paragraph) {
      const currentStyle = paragraph.attrs.style || ''
      const newStyle = currentStyle.replace(/letter-spacing:\s*[^;]+;?/g, '') + `letter-spacing: ${spacing};`
      props.editor.chain().focus()
        .updateAttributes('paragraph', { style: newStyle.trim() })
        .run()
    }
  }
}

const setParagraphMargin = () => {
  if (!props.editor) return
  const { $anchor } = props.editor.state.selection
  const paragraph = $anchor.parent
  if (paragraph) {
    const currentStyle = paragraph.attrs.style || ''
    const newStyle = currentStyle.replace(/margin-bottom:\s*[^;]+;?/g, '') + `margin-bottom: ${paragraphMargin.value}px;`
    props.editor.chain().focus()
      .updateAttributes('paragraph', { style: newStyle.trim() })
      .run()
  }
}

const setTextIndent = () => {
  if (!props.editor) return
  const { $anchor } = props.editor.state.selection
  const paragraph = $anchor.parent
  if (paragraph) {
    const currentStyle = paragraph.attrs.style || ''
    const newStyle = currentStyle.replace(/text-indent:\s*[^;]+;?/g, '') + (textIndent.value > 0 ? `text-indent: ${textIndent.value}px;` : '')
    props.editor.chain().focus()
      .updateAttributes('paragraph', { style: newStyle.trim() })
      .run()
  }
}

const setFontWeight = () => {
  if (!props.editor) return
  const selection = props.editor.state.selection
  
  if (!selection.empty) {
    const text = props.editor.state.doc.textBetween(selection.from, selection.to)
    const span = document.createElement('span')
    span.style.fontWeight = fontWeight.value
    span.textContent = text
    
    props.editor.chain()
      .focus()
      .deleteSelection()
      .insertContent(span)
      .run()
  } else {
    const { $anchor } = props.editor.state.selection
    const paragraph = $anchor.parent
    if (paragraph) {
      const currentStyle = paragraph.attrs.style || ''
      const newStyle = currentStyle.replace(/font-weight:\s*[^;]+;?/g, '') + `font-weight: ${fontWeight.value};`
      props.editor.chain().focus()
        .updateAttributes('paragraph', { style: newStyle.trim() })
        .run()
    }
  }
}

const setFont = (font) => {
  currentFont.value = font
  if (!props.editor) return
  const fontMap = {
    system: '-apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif',
    serif: '"Times New Roman", serif',
    mono: '"Monaco", "Courier New", monospace',
    round: '-apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif'
  }
  const fontFamily = fontMap[font] || fontMap.system
  const selection = props.editor.state.selection
  
  if (!selection.empty) {
    const text = props.editor.state.doc.textBetween(selection.from, selection.to)
    const span = document.createElement('span')
    span.style.fontFamily = fontFamily
    span.textContent = text
    
    props.editor.chain()
      .focus()
      .deleteSelection()
      .insertContent(span)
      .run()
  } else {
    const { $anchor } = props.editor.state.selection
    const paragraph = $anchor.parent
    if (paragraph) {
      const currentStyle = paragraph.attrs.style || ''
      const newStyle = currentStyle.replace(/font-family:\s*[^;]+;?/g, '') + `font-family: ${fontFamily};`
      props.editor.chain().focus()
        .updateAttributes('paragraph', { style: newStyle.trim() })
        .run()
    }
  }
}

const decreaseIndent = () => {
  if (!props.editor) return
  // 尝试减少列表项缩进
  props.editor.chain().focus().liftListItem('listItem').run()
  // 如果没有列表，减少段落缩进
  const { $anchor } = props.editor.state.selection
  const node = $anchor.parent
  if (node && node.type.name === 'paragraph') {
    const currentStyle = node.attrs.style || ''
    const indentMatch = currentStyle.match(/padding-left:\s*(\d+)px/)
    if (indentMatch) {
      const currentIndent = parseInt(indentMatch[1])
      const newIndent = Math.max(0, currentIndent - 20)
      const newStyle = currentStyle.replace(/padding-left:\s*\d+px;?/g, '') + (newIndent > 0 ? `padding-left: ${newIndent}px;` : '')
      props.editor.chain().focus()
        .updateAttributes('paragraph', { style: newStyle.trim() })
        .run()
    }
  }
}

const increaseIndent = () => {
  if (!props.editor) return
  // 尝试增加列表项缩进
  props.editor.chain().focus().sinkListItem('listItem').run()
  // 如果没有列表，增加段落缩进
  const { $anchor } = props.editor.state.selection
  const node = $anchor.parent
  if (node && node.type.name === 'paragraph') {
    const currentStyle = node.attrs.style || ''
    const indentMatch = currentStyle.match(/padding-left:\s*(\d+)px/)
    const currentIndent = indentMatch ? parseInt(indentMatch[1]) : 0
    const newIndent = currentIndent + 20
    const newStyle = currentStyle.replace(/padding-left:\s*\d+px;?/g, '') + `padding-left: ${newIndent}px;`
    props.editor.chain().focus()
      .updateAttributes('paragraph', { style: newStyle.trim() })
      .run()
  }
}

const transformToUppercase = () => {
  if (!props.editor) return
  const { selection } = props.editor.state
  if (selection.empty) return
  
  const text = props.editor.state.doc.textBetween(selection.from, selection.to)
  const upperText = text.toUpperCase()
  
  const escapedText = upperText
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
  
  props.editor.chain()
    .focus()
    .deleteSelection()
    .insertContent(escapedText)
    .run()
}

const transformToLowercase = () => {
  if (!props.editor) return
  const { selection } = props.editor.state
  if (selection.empty) return
  
  const text = props.editor.state.doc.textBetween(selection.from, selection.to)
  const lowerText = text.toLowerCase()
  
  const escapedText = lowerText
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
  
  props.editor.chain()
    .focus()
    .deleteSelection()
    .insertContent(escapedText)
    .run()
}

const transformToCapitalize = () => {
  if (!props.editor) return
  const { selection } = props.editor.state
  if (selection.empty) return
  
  const text = props.editor.state.doc.textBetween(selection.from, selection.to)
  const capitalizedText = text.split(' ').map(word => 
    word.charAt(0).toUpperCase() + word.slice(1).toLowerCase()
  ).join(' ')
  
  const escapedText = capitalizedText
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
  
  props.editor.chain()
    .focus()
    .deleteSelection()
    .insertContent(escapedText)
    .run()
}

const toggleTextShadow = () => {
  if (!props.editor) return
  const { $anchor } = props.editor.state.selection
  const node = $anchor.parent
  if (node) {
    const currentStyle = node.attrs.style || ''
    const hasShadow = currentStyle.includes('text-shadow')
    let newStyle = currentStyle.replace(/text-shadow:\s*[^;]+;?/g, '')
    
    if (!hasShadow) {
      newStyle += 'text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);'
    }
    
    props.editor.chain().focus()
      .updateAttributes(node.type.name, { style: newStyle.trim() })
      .run()
  }
}

const toggleLetterSpacing = () => {
  if (!props.editor) return
  const { $anchor } = props.editor.state.selection
  const node = $anchor.parent
  if (node) {
    const currentStyle = node.attrs.style || ''
    const hasSpacing = currentStyle.includes('letter-spacing')
    let newStyle = currentStyle.replace(/letter-spacing:\s*[^;]+;?/g, '')
    
    if (!hasSpacing) {
      newStyle += 'letter-spacing: 0.5px;'
    }
    
    props.editor.chain().focus()
      .updateAttributes(node.type.name, { style: newStyle.trim() })
      .run()
  }
}

const openColorPicker = (type) => {
  // 创建颜色选择器输入
  const color = window.prompt(`请输入${type === 'text' ? '文字' : type === 'background' ? '背景' : '高亮'}颜色（十六进制，如 #ff0000）:`)
  if (color) {
    if (type === 'text') {
      setTextColor(color)
    } else if (type === 'background') {
      setBackgroundColor(color)
    } else {
      setHighlightColor(color)
    }
  }
}

const applyTextGradient = (gradient) => {
  if (!props.editor) return
  const { selection } = props.editor.state
  const { from, to } = selection
  
  if (selection.empty) {
    const html = `<span style="background: ${gradient}; -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text;">&nbsp;</span>`
    props.editor.chain().focus().insertContent(html).run()
  } else {
    const text = props.editor.state.doc.textBetween(from, to)
    if (!text) return
    
    const escapedText = text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;')
    
    const html = `<span style="background: ${gradient}; -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text;">${escapedText}</span>`
    
    props.editor.chain()
      .focus()
      .deleteSelection()
      .insertContent(html)
      .run()
  }
}

const applyBorder = (border) => {
  if (!props.editor) return
  currentBorder.value = border.name
  const { $anchor } = props.editor.state.selection
  const node = $anchor.parent
  if (node) {
    const currentStyle = node.attrs.style || ''
    let newStyle = currentStyle.replace(/border[^:]*:\s*[^;]+;?/g, '')
    newStyle = newStyle.replace(/border-radius:\s*[^;]+;?/g, '')
    
    if (border.style !== 'none') {
      newStyle += `border: ${border.style};`
      if (border.borderRadius) {
        newStyle += `border-radius: ${border.borderRadius};`
      }
    }
    
    props.editor.chain().focus()
      .updateAttributes(node.type.name, { style: newStyle.trim() })
      .run()
  }
}

const applyTextShadow = (shadow) => {
  if (!props.editor) return
  currentTextShadow.value = shadow.name
  const { $anchor } = props.editor.state.selection
  const node = $anchor.parent
  if (node) {
    const currentStyle = node.attrs.style || ''
    const newStyle = currentStyle.replace(/text-shadow:\s*[^;]+;?/g, '') + (shadow.value !== 'none' ? `text-shadow: ${shadow.value};` : '')
    props.editor.chain().focus()
      .updateAttributes(node.type.name, { style: newStyle.trim() })
      .run()
  }
}

const applyBackgroundStyle = (bg) => {
  if (!props.editor) return
  currentBackgroundStyle.value = bg.name
  const { $anchor } = props.editor.state.selection
  const node = $anchor.parent
  if (node) {
    const currentStyle = node.attrs.style || ''
    let newStyle = currentStyle.replace(/background[^:]*:\s*[^;]+;?/g, '')
    
    if (bg.value !== 'transparent') {
      newStyle += `background: ${bg.value};`
      if (bg.value.includes('linear-gradient') || bg.value.includes('repeating')) {
        newStyle += 'background-size: 100% 100%;'
      }
    }
    
    props.editor.chain().focus()
      .updateAttributes(node.type.name, { style: newStyle.trim() })
      .run()
  }
}

const toggleOutline = () => {
  if (!props.editor) return
  hasOutline.value = !hasOutline.value
  const { selection } = props.editor.state
  const { from, to } = selection
  
  if (selection.empty) {
    const html = hasOutline.value 
      ? `<span style="-webkit-text-stroke: 1px currentColor; -webkit-text-fill-color: transparent;">&nbsp;</span>`
      : `<span>&nbsp;</span>`
    props.editor.chain().focus().insertContent(html).run()
  } else {
    const text = props.editor.state.doc.textBetween(from, to)
    const escapedText = text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;')
    
    const html = hasOutline.value
      ? `<span style="-webkit-text-stroke: 1px currentColor; -webkit-text-fill-color: transparent;">${escapedText}</span>`
      : `<span>${escapedText}</span>`
    
    props.editor.chain()
      .focus()
      .deleteSelection()
      .insertContent(html)
      .run()
  }
}

const toggleBlur = () => {
  if (!props.editor) return
  hasBlur.value = !hasBlur.value
  const { $anchor } = props.editor.state.selection
  const node = $anchor.parent
  if (node) {
    const currentStyle = node.attrs.style || ''
    let newStyle = currentStyle.replace(/filter:\s*[^;]+;?/g, '')
    
    if (hasBlur.value) {
      newStyle += 'filter: blur(2px);'
    }
    
    props.editor.chain().focus()
      .updateAttributes(node.type.name, { style: newStyle.trim() })
      .run()
  }
}
</script>

<style scoped>
.format-panel {
  position: fixed;
  right: 100px;
  top: 50%;
  transform: translateY(-50%) translateZ(0);
  z-index: 10001;
  width: 360px;
  max-height: 85vh;
  background: rgba(245, 245, 247, 0.85);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-radius: 12px;
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.12),
    0 0 0 1px rgba(255, 255, 255, 0.5) inset,
    0 0 0 1px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  animation: panelSlideIn 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  transform: translateY(-50%) translateZ(0);
  will-change: opacity, transform;
}

@keyframes panelSlideIn {
  from {
    opacity: 0;
    transform: translateY(-50%) translateX(20px) scale(0.96) translateZ(0);
  }
  to {
    opacity: 1;
    transform: translateY(-50%) translateX(0) scale(1) translateZ(0);
  }
}

.format-panel-content {
  display: flex;
  flex-direction: column;
  max-height: 85vh;
  overflow: hidden;
}

.format-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  background: rgba(255, 255, 255, 0.3);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon-wrapper {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 8px;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.header-icon {
  color: rgba(0, 0, 0, 0.7);
  flex-shrink: 0;
}

.format-title {
  font-size: 15px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.85);
  margin: 0;
}

.format-close-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 8px;
  color: rgba(0, 0, 0, 0.6);
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.format-close-btn:hover {
  background: rgba(255, 255, 255, 0.8);
  color: rgba(0, 0, 0, 0.85);
  transform: scale(1.05);
}

.format-close-btn:active {
  transform: scale(0.95);
}

.format-tabs {
  display: flex;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  background: rgba(255, 255, 255, 0.2);
  flex-shrink: 0;
}

.format-tab {
  flex: 1;
  padding: 12px 16px;
  border: none;
  background: transparent;
  color: rgba(0, 0, 0, 0.5);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  outline: none;
  border-bottom: 2px solid transparent;
}

.format-tab:hover {
  color: rgba(0, 0, 0, 0.85);
  background: rgba(255, 255, 255, 0.2);
}

.format-tab.active {
  color: #165dff;
  border-bottom-color: #165dff;
  background: rgba(255, 255, 255, 0.3);
}

.format-sections {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  min-height: 0;
}

/* 自定义滚动条 */
.format-sections::-webkit-scrollbar {
  width: 8px;
}

.format-sections::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.02);
  border-radius: 4px;
}

.format-sections::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.15);
  border-radius: 4px;
  transition: background 0.2s;
}

.format-sections::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.25);
}

.format-tab-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.format-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.format-group-title {
  font-size: 11px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.5);
  text-transform: uppercase;
  letter-spacing: 0.8px;
  padding: 0 4px;
}

.format-select-wrapper {
  position: relative;
}

.format-select {
  width: 100%;
  padding: 10px 14px;
  padding-right: 36px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(8px) saturate(180%);
  -webkit-backdrop-filter: blur(8px) saturate(180%);
  border-radius: 8px;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.85);
  outline: none;
  cursor: pointer;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg width='12' height='12' viewBox='0 0 12 12' fill='none' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M3 4.5l3 3 3-3' stroke='%236b7280' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.format-select:hover {
  background-color: rgba(255, 255, 255, 0.7);
  border-color: rgba(0, 0, 0, 0.15);
}

.format-select:focus {
  border-color: rgba(22, 93, 255, 0.4);
  background-color: rgba(255, 255, 255, 0.8);
}

.format-icon-buttons {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.format-icon-btn {
  width: 100%;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(0, 0, 0, 0.1);
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(8px) saturate(180%);
  -webkit-backdrop-filter: blur(8px) saturate(180%);
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.7);
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  outline: none;
}

.format-icon-btn:hover {
  background: rgba(255, 255, 255, 0.7);
  border-color: rgba(0, 0, 0, 0.15);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.format-icon-btn.active {
  background: rgba(22, 93, 255, 0.15);
  color: #165dff;
  border-color: rgba(22, 93, 255, 0.3);
}

.format-icon-btn:active {
  transform: translateY(0) scale(0.98);
}

.format-icon-btn svg {
  width: 16px;
  height: 16px;
}

.format-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.format-btn {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(8px) saturate(180%);
  -webkit-backdrop-filter: blur(8px) saturate(180%);
  border-radius: 8px;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.85);
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  text-align: left;
  display: flex;
  align-items: center;
  outline: none;
}

.format-btn:hover {
  background: rgba(255, 255, 255, 0.7);
  border-color: rgba(0, 0, 0, 0.15);
  transform: translateX(2px);
}

.format-btn.active {
  background: rgba(22, 93, 255, 0.15);
  color: #165dff;
  border-color: rgba(22, 93, 255, 0.3);
}

.color-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 8px;
}

.color-btn {
  width: 100%;
  aspect-ratio: 1;
  border: 2px solid rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.color-btn:hover {
  transform: scale(1.1);
  border-color: rgba(0, 0, 0, 0.2);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.color-btn.active {
  border-color: rgba(0, 0, 0, 0.3);
  box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.8), 0 0 0 4px rgba(22, 93, 255, 0.3);
}

.color-picker-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%) !important;
  border: 2px solid rgba(0, 0, 0, 0.1) !important;
  position: relative;
}

.color-picker-btn::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(45deg, transparent 30%, rgba(255, 255, 255, 0.3) 50%, transparent 70%);
  border-radius: 6px;
}

.gradient-btn {
  background-size: 200% 200% !important;
  color: white !important;
  font-weight: 600 !important;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2) !important;
  position: relative;
  overflow: hidden;
}

.gradient-btn::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(45deg, transparent 30%, rgba(255, 255, 255, 0.2) 50%, transparent 70%);
  border-radius: 6px;
}

.color-btn svg {
  width: 14px;
  height: 14px;
}

.format-clear-section {
  padding-top: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  margin-top: auto;
}

.format-clear-btn {
  width: 100%;
  padding: 10px 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 1px solid rgba(239, 68, 68, 0.2);
  background: rgba(254, 242, 242, 0.6);
  backdrop-filter: blur(8px) saturate(180%);
  -webkit-backdrop-filter: blur(8px) saturate(180%);
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  color: #dc2626;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  outline: none;
}

.format-clear-btn:hover {
  background: rgba(254, 242, 242, 0.8);
  border-color: rgba(239, 68, 68, 0.4);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.2);
}

.format-clear-btn:active {
  transform: translateY(0) scale(0.98);
}

.format-clear-btn svg {
  width: 14px;
  height: 14px;
}
</style>
