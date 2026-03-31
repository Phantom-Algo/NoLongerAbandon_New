<template>
  <div class="search-page">
    <div class="search-hero">
      <h1 class="search-title">单词搜索</h1>
      <p class="search-subtitle">输入英文单词或中文释义，AI 为你生成详细单词卡</p>

      <div class="search-box">
        <el-input
          v-model="keyword"
          size="large"
          placeholder="输入英文单词或中文关键词..."
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <SvgIcon :content="icons.search" :size="18" />
          </template>
        </el-input>
        <el-button type="primary" size="large" @click="handleSearch">查询</el-button>
      </div>

      <div class="search-hints">
        <span class="hint-item">英文直查</span>
        <span class="hint-dot"></span>
        <span class="hint-item">中文反查</span>
        <span class="hint-dot"></span>
        <span class="hint-item">AI 单词卡生成</span>
      </div>
    </div>

    <div class="search-result-area">
      <el-empty description="输入关键词开始搜索" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import SvgIcon from '@/components/SvgIcon.vue'
import * as icons from '@/assets/icons'

const keyword = ref('')

function handleSearch() {
  if (!keyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  ElMessage.info('搜索功能即将上线')
}
</script>

<style scoped>
.search-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 60px;
}

.search-hero {
  text-align: center;
  max-width: 580px;
  width: 100%;
}

.search-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0 0 8px;
}

.search-subtitle {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0 0 32px;
}

.search-box {
  display: flex;
  gap: var(--spacing-sm);
}

.search-box .el-input {
  flex: 1;
}

.search-hints {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  margin-top: var(--spacing-md);
  font-size: 12px;
  color: var(--color-text-placeholder);
}

.hint-dot {
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: var(--color-text-placeholder);
}

.search-result-area {
  margin-top: 60px;
  width: 100%;
  max-width: 700px;
}
</style>