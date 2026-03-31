<template>
  <!-- 悬浮按钮 -->
  <div class="chat-bubble-trigger" :class="{ 'chat-bubble-trigger--active': panelVisible }" @click="togglePanel">
    <SvgIcon :content="panelVisible ? icons.close : icons.chat" :size="22" />
  </div>

  <!-- 对话面板 -->
  <Transition name="chat-panel">
    <div v-if="panelVisible" class="chat-panel">
      <div class="chat-panel__header">
        <span class="chat-panel__title">AI 助手</span>
      </div>
      <div class="chat-panel__body">
        <!-- 静态骨架 -->
        <div class="chat-sessions">
          <div class="chat-session-item chat-session-item--active">新对话</div>
        </div>
        <div class="chat-messages">
          <div class="chat-empty">
            <SvgIcon :content="icons.chat" :size="36" />
            <p>AI 对话功能即将上线</p>
            <p class="chat-empty__sub">支持流式输出、上下文注入与多会话管理</p>
          </div>
        </div>
      </div>
      <div class="chat-panel__footer">
        <input class="chat-input" placeholder="输入消息..." disabled />
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import SvgIcon from '@/components/SvgIcon.vue'
import * as icons from '@/assets/icons'

const panelVisible = ref(false)

function togglePanel() {
  panelVisible.value = !panelVisible.value
}
</script>

<style scoped>
/* 悬浮触发按钮 */
.chat-bubble-trigger {
  position: fixed;
  right: 28px;
  bottom: 28px;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: var(--theme-color);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: var(--shadow-md);
  transition: all var(--transition-fast);
  z-index: 2000;
}

.chat-bubble-trigger:hover {
  transform: scale(1.08);
  box-shadow: var(--shadow-lg);
}

.chat-bubble-trigger--active {
  background: var(--color-text-regular);
}

/* 对话面板 */
.chat-panel {
  position: fixed;
  right: 28px;
  bottom: 92px;
  width: 380px;
  height: 520px;
  background: #fff;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 1999;
}

.chat-panel__header {
  padding: 14px 18px;
  border-bottom: 1px solid var(--color-border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.chat-panel__title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.chat-panel__body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 会话列表 */
.chat-sessions {
  width: 100px;
  border-right: 1px solid var(--color-border-light);
  padding: var(--spacing-sm);
  overflow-y: auto;
}

.chat-session-item {
  padding: 8px 10px;
  font-size: 12px;
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
  cursor: pointer;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: all var(--transition-fast);
}

.chat-session-item:hover {
  background: var(--color-bg-hover);
}

.chat-session-item--active {
  background: var(--color-bg-active);
  color: var(--theme-color);
  font-weight: 500;
}

/* 消息区域 */
.chat-messages {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-md);
}

.chat-empty {
  text-align: center;
  color: var(--color-text-placeholder);
}

.chat-empty p {
  margin: var(--spacing-sm) 0 0;
  font-size: 14px;
}

.chat-empty__sub {
  font-size: 12px !important;
  color: var(--color-text-placeholder);
}

/* 底部输入框 */
.chat-panel__footer {
  padding: 12px 14px;
  border-top: 1px solid var(--color-border-light);
}

.chat-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 13px;
  outline: none;
  background: var(--color-bg-page);
  color: var(--color-text-primary);
}

.chat-input:focus {
  border-color: var(--theme-color);
}

/* 面板弹出动画 */
.chat-panel-enter-active,
.chat-panel-leave-active {
  transition: all 0.3s ease;
}

.chat-panel-enter-from,
.chat-panel-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}
</style>
