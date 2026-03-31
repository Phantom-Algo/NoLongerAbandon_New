<template>
  <div class="notebook-page">
    <div class="page-header">
      <h2 class="page-title">单词本</h2>
      <el-button type="primary" disabled>新建单词本</el-button>
    </div>

    <div class="notebook-list">
      <div v-for="item in notebooks" :key="item.name" class="notebook-card">
        <div class="notebook-card__icon">
          <SvgIcon :content="icons.notebook" :size="24" />
        </div>
        <div class="notebook-card__info">
          <div class="notebook-card__name">{{ item.name }}</div>
          <div class="notebook-card__meta">{{ item.count }} 个单词 · {{ item.sortMode }}</div>
        </div>
      </div>

      <el-empty v-if="notebooks.length === 0" description="暂无单词本" />
    </div>
  </div>
</template>

<script setup lang="ts">
import SvgIcon from '@/components/SvgIcon.vue'
import * as icons from '@/assets/icons'

const notebooks = [
  { name: '考研高频', count: 0, sortMode: '按加入时间' },
  { name: '易错词复盘', count: 0, sortMode: '按搜索次数' }
]
</script>

<style scoped>
.notebook-page {
  width: 100%;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-lg);
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
}

.notebook-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.notebook-card {
  background: #fff;
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md);
  padding: var(--spacing-md) var(--spacing-lg);
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.notebook-card:hover {
  box-shadow: var(--shadow-sm);
  border-color: var(--color-border);
}

.notebook-card__icon {
  color: var(--theme-color);
}

.notebook-card__name {
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text-primary);
}

.notebook-card__meta {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-top: 2px;
}
</style>
