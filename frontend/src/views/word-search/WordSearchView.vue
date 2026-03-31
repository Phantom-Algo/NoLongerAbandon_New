<template>
  <div class="page-grid">
    <el-card shadow="never">
      <template #header>
        <span>搜索入口骨架</span>
      </template>
      <el-form label-position="top">
        <el-form-item label="英文或中文关键词">
          <el-input placeholder="例如：abandon / 放弃" />
        </el-form-item>
        <el-button type="primary">查询</el-button>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <span>后续扩展点</span>
      </template>
      <ul class="plain-list">
        <li>英文直查命中缓存后直接展示单词卡</li>
        <li>中文检索先返回候选词，再进入单词卡流程</li>
        <li>支持模块级重新生成与上下文注入</li>
      </ul>
    </el-card>
  </div>
</template>

<style scoped>
.page-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(280px, 0.7fr);
  gap: 18px;
}

.plain-list {
  margin: 0;
  padding-left: 18px;
  line-height: 1.8;
  color: #5b4c3e;
}

@media (max-width: 960px) {
  .page-grid {
    grid-template-columns: 1fr;
  }
}
</style>