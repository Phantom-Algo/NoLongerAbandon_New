<template>
  <div class="chat-grid">
    <el-card shadow="never">
      <template #header>
        <span>会话列表</span>
      </template>
      <el-empty description="后续接入会话创建、切换与模型选择" />
    </el-card>

    <el-card shadow="never">
      <template #header>
        <span>流式对话区</span>
      </template>
      <el-empty description="后续接入 SSE / WebFlux 流式输出" />
    </el-card>
  </div>
</template>

<style scoped>
.chat-grid {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 18px;
}

@media (max-width: 960px) {
  .chat-grid {
    grid-template-columns: 1fr;
  }
}
</style>