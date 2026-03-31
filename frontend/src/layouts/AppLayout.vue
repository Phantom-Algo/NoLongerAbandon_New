<template>
  <div class="app-layout">
    <!-- 侧边栏 -->
    <aside class="app-sidebar">
      <!-- LOGO -->
      <div class="sidebar-logo">
        <img src="@static/img/LOGO.png" alt="NoLongerAbandon" />
      </div>

      <!-- 当前时间 -->
      <div class="sidebar-time">
        <div class="time-date">{{ currentDate }}</div>
        <div class="time-clock">{{ currentTime }}</div>
      </div>

      <!-- 导航项 -->
      <nav class="sidebar-nav">
        <router-link to="/word-search" class="nav-item" active-class="nav-item--active">
          <SvgIcon :content="icons.search" :size="18" />
          <span>单词搜索</span>
        </router-link>

        <router-link to="/notebook" class="nav-item" active-class="nav-item--active">
          <SvgIcon :content="icons.notebook" :size="18" />
          <span>单词本</span>
        </router-link>

        <router-link to="/dashboard" class="nav-item" active-class="nav-item--active">
          <SvgIcon :content="icons.dashboard" :size="18" />
          <span>总览</span>
        </router-link>
      </nav>

      <!-- 底部操作栏 -->
      <div class="sidebar-footer">
        <div class="sidebar-divider"></div>
        <router-link to="/settings" class="nav-item" active-class="nav-item--active">
          <SvgIcon :content="icons.settings" :size="18" />
          <span>设置</span>
        </router-link>
      </div>
    </aside>

    <!-- 内容区 -->
    <main class="app-content">
      <RouterView />
    </main>

    <!-- AI 对话悬浮气泡 -->
    <ChatBubble />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import SvgIcon from '@/components/SvgIcon.vue'
import ChatBubble from '@/components/ChatBubble.vue'
import * as icons from '@/assets/icons'

// 时间显示
const currentDate = ref('')
const currentTime = ref('')
let timer: ReturnType<typeof setInterval>

function updateTime() {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  const weekDays = ['日', '一', '二', '三', '四', '五', '六']
  const w = weekDays[now.getDay()]
  currentDate.value = `${y}年${m}月${d}日 星期${w}`
  currentTime.value = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 10000)
})

onUnmounted(() => {
  clearInterval(timer)
})


</script>

<style scoped>
/* 侧边栏 LOGO */
.sidebar-logo {
  padding: var(--spacing-sm) 0;
  margin-bottom: var(--spacing-xs);
}

.sidebar-logo img {
  display: block;
  width: 100%;
  max-width: 150px;
  height: auto;
}

/* 时间区域 */
.sidebar-time {
  padding: var(--spacing-xs) var(--spacing-xs);
  margin-bottom: var(--spacing-lg);
  color: var(--color-text-secondary);
  font-size: 13px;
  line-height: 1.6;
}

.time-clock {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text-primary);
  letter-spacing: 1px;
}

/* 导航区 */
.sidebar-nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow-y: auto;
}

/* 导航项 */
.nav-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: 10px 12px;
  border-radius: var(--radius-md);
  color: var(--color-text-regular);
  text-decoration: none;
  font-size: 14px;
  cursor: pointer;
  transition: all var(--transition-fast);
  user-select: none;
}

.nav-item:hover {
  background: var(--color-bg-hover);
  color: var(--color-text-primary);
}

.nav-item--active {
  background: var(--color-bg-active);
  color: var(--theme-color);
  font-weight: 500;
}

/* 底部操作栏 */
.sidebar-footer {
  margin-top: auto;
}

.sidebar-divider {
  height: 1px;
  background: var(--color-border-light);
  margin: var(--spacing-sm) 0;
}
</style>
