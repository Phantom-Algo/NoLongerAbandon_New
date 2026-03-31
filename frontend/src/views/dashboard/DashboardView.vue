<template>
  <div class="dashboard-grid">
    <el-card shadow="never" class="overview-card">
      <template #header>
        <div class="card-header">
          <span>当前阶段</span>
          <el-tag effect="plain">架构初始化</el-tag>
        </div>
      </template>
      <p>已完成前后端基础骨架、数据库初始化策略、统一返回格式和页面主布局。</p>
      <p>后续建议先落“单词搜索 -> 单词卡缓存 -> 单词本关联”这一条纵向链路。</p>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>后端约束</span>
        </div>
      </template>
      <ul class="plain-list">
        <li>严格使用 controller-service-mapper 分层</li>
        <li>数据库初始化使用 schema.sql + IF NOT EXISTS</li>
        <li>AI、持久化、业务模块分区隔离</li>
      </ul>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>前端约束</span>
        </div>
      </template>
      <ul class="plain-list">
        <li>页面按 views 领域拆分</li>
        <li>统一走 AppLayout 承载侧栏与内容区</li>
        <li>图标使用本地 SVG 资源</li>
      </ul>
    </el-card>
  </div>
</template>

<style scoped>
.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.overview-card {
  grid-column: span 3;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.plain-list {
  margin: 0;
  padding-left: 18px;
  color: #5b4c3e;
  line-height: 1.8;
}

@media (max-width: 960px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }

  .overview-card {
    grid-column: span 1;
  }
}
</style>