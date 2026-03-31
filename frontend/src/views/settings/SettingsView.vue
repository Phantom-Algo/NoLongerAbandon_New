<template>
  <div class="settings-grid">
    <el-card shadow="never">
      <template #header>
        <span>模型配置</span>
      </template>
      <el-form label-position="top">
        <el-form-item label="Base URL">
          <el-input placeholder="https://api.openai.com" />
        </el-form-item>
        <el-form-item label="Model Name">
          <el-input placeholder="gpt-4.1-mini" />
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <span>个人信息与权限</span>
      </template>
      <el-form label-position="top">
        <el-form-item label="个人信息">
          <el-input type="textarea" :rows="6" placeholder="支持 Markdown" />
        </el-form-item>
        <el-switch active-text="允许 AI 读取个人信息" />
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.settings-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

@media (max-width: 960px) {
  .settings-grid {
    grid-template-columns: 1fr;
  }
}
</style>