import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '@/layouts/AppLayout.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: AppLayout,
      redirect: '/word-search',
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/dashboard/DashboardView.vue'),
          meta: { title: '总览' }
        },
        {
          path: 'word-search',
          name: 'word-search',
          component: () => import('@/views/word-search/WordSearchView.vue'),
          meta: { title: '单词搜索' }
        },
        {
          path: 'notebook',
          name: 'notebook',
          component: () => import('@/views/notebook/NotebookView.vue'),
          meta: { title: '单词本' }
        },
        {
          path: 'chat',
          name: 'chat',
          component: () => import('@/views/chat/ChatView.vue'),
          meta: { title: 'AI 会话' }
        },
        {
          path: 'settings',
          name: 'settings',
          component: () => import('@/views/settings/SettingsView.vue'),
          meta: { title: '设置' }
        }
      ]
    }
  ]
})

export default router