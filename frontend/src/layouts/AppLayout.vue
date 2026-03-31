<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand-card">
        <img class="brand-logo" :src="logo" alt="NoLongerAbandon logo" />
        <div>
          <p class="brand-name">{{ appStore.appName }}</p>
          <p class="brand-tagline">本地英语单词学习与记背助手</p>
        </div>
      </div>

      <nav class="nav-list">
        <RouterLink
          v-for="item in navigationItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ 'is-active': route.path === item.path }"
        >
          <SvgIcon :content="item.icon" />
          <div>
            <p class="nav-label">{{ item.label }}</p>
            <p class="nav-description">{{ item.description }}</p>
          </div>
        </RouterLink>
      </nav>
    </aside>

    <main class="content-area">
      <header class="content-header">
        <div>
          <p class="content-caption">开发骨架</p>
          <h1>{{ currentTitle }}</h1>
        </div>
        <el-tag type="success" effect="plain">Vue 3 + Element Plus</el-tag>
      </header>

      <section class="content-panel">
        <RouterView />
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import SvgIcon from '@/components/SvgIcon.vue'
import { useAppStore } from '@/stores/app'
import type { NavigationItem } from '@/types/navigation'
import logo from '@static/img/LOGO.png'
import dashboardIcon from '@/assets/icons/dashboard.svg?raw'
import searchIcon from '@/assets/icons/search.svg?raw'
import notebookIcon from '@/assets/icons/notebook.svg?raw'
import chatIcon from '@/assets/icons/chat.svg?raw'
import settingsIcon from '@/assets/icons/settings.svg?raw'

const appStore = useAppStore()
const route = useRoute()

const navigationItems: NavigationItem[] = [
  { label: '总览', path: '/dashboard', icon: dashboardIcon, description: '项目入口与里程碑' },
  { label: '单词搜索', path: '/word-search', icon: searchIcon, description: '英文/中文检索入口' },
  { label: '单词本', path: '/notebook', icon: notebookIcon, description: '单词本与分页列表' },
  { label: 'AI 会话', path: '/chat', icon: chatIcon, description: '独立流式会话能力' },
  { label: '设置', path: '/settings', icon: settingsIcon, description: '模型与用户偏好设置' }
]

const currentTitle = computed(() => String(route.meta.title ?? '总览'))
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  background:
    radial-gradient(circle at top left, rgba(201, 176, 141, 0.18), transparent 24%),
    linear-gradient(180deg, #f8f2e8 0%, #f3eee5 48%, #efe7da 100%);
}

.sidebar {
  padding: 28px 22px;
  border-right: 1px solid rgba(70, 57, 42, 0.08);
  background: rgba(255, 250, 244, 0.85);
  backdrop-filter: blur(12px);
}

.brand-card {
  display: grid;
  grid-template-columns: 56px 1fr;
  gap: 14px;
  align-items: center;
  padding: 14px;
  border-radius: 20px;
  background: #fffdf9;
  box-shadow: 0 12px 30px rgba(76, 58, 41, 0.08);
}

.brand-logo {
  width: 56px;
  height: 56px;
  object-fit: contain;
}

.brand-name,
.nav-label,
.nav-description,
.brand-tagline,
.content-caption,
h1 {
  margin: 0;
}

.brand-name {
  font-size: 18px;
  font-weight: 700;
  color: #2f261d;
}

.brand-tagline {
  margin-top: 4px;
  font-size: 12px;
  line-height: 1.6;
  color: #7b6d5d;
}

.nav-list {
  display: grid;
  gap: 10px;
  margin-top: 28px;
}

.nav-item {
  display: grid;
  grid-template-columns: 18px 1fr;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 18px;
  color: #5b4c3e;
  text-decoration: none;
  transition: transform 0.2s ease, background-color 0.2s ease, color 0.2s ease;
}

.nav-item:hover,
.nav-item.is-active {
  background: #2f261d;
  color: #fbf6ee;
  transform: translateX(4px);
}

.nav-label {
  font-size: 15px;
  font-weight: 600;
}

.nav-description {
  margin-top: 4px;
  font-size: 12px;
  line-height: 1.5;
  color: inherit;
  opacity: 0.72;
}

.content-area {
  padding: 28px;
}

.content-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.content-caption {
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #8f7e69;
}

h1 {
  margin-top: 6px;
  font-size: 32px;
  color: #2f261d;
}

.content-panel {
  min-height: calc(100vh - 126px);
  padding: 22px;
  border-radius: 28px;
  background: rgba(255, 252, 247, 0.85);
  box-shadow: 0 24px 60px rgba(76, 58, 41, 0.08);
}

@media (max-width: 960px) {
  .app-shell {
    grid-template-columns: 1fr;
  }

  .sidebar {
    border-right: none;
    border-bottom: 1px solid rgba(70, 57, 42, 0.08);
  }

  .content-area {
    padding: 18px;
  }

  .content-panel {
    min-height: auto;
  }
}
</style>