<template>
  <div class="dashboard-page">
    <h2 class="page-title">总览</h2>

    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-value">0</div>
        <div class="stat-label">搜索次数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">0</div>
        <div class="stat-label">单词卡数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">0</div>
        <div class="stat-label">单词本数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">0</div>
        <div class="stat-label">AI 会话数</div>
      </div>
    </div>

    <div class="section-card">
      <h3 class="section-card__title">最近搜索</h3>
      <el-empty description="暂无搜索记录" :image-size="80" />
    </div>

    <div class="section-card">
      <h3 class="section-card__title">开发状态</h3>
      <div class="dev-status">
        <el-tag effect="plain" type="success">已完成</el-tag>
        <span>前后端基础骨架、数据库迁移、设置模块 API、前端页面布局</span>
      </div>
      <div class="dev-status">
        <el-tag effect="plain" type="warning">下一步</el-tag>
        <span>单词搜索 → 单词卡缓存 → 单词本关联</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard-page {
  width: 100%;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 var(--spacing-lg);
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.stat-card {
  background: #fff;
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg) var(--spacing-md);
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text-primary);
}

.stat-label {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-top: var(--spacing-xs);
}

.section-card {
  background: #fff;
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-md);
}

.section-card__title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 var(--spacing-md);
}

.dev-status {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: 14px;
  color: var(--color-text-regular);
  margin-bottom: var(--spacing-sm);
}

@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
