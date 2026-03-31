<template>
  <div class="settings-page">
    <!-- 模型配置 -->
    <section class="settings-section">
      <div class="section-header">
        <h2 class="section-title">模型配置</h2>
        <el-button type="primary" :icon="Plus" @click="openCreateDialog">添加模型</el-button>
      </div>

      <div v-loading="modelsLoading" class="model-list">
        <el-empty v-if="!modelsLoading && models.length === 0" description="暂无模型配置，点击上方按钮添加" />

        <div v-for="model in models" :key="model.id" class="model-card">
          <div class="model-card__header">
            <div class="model-card__title">
              <span class="model-display-name">{{ model.displayName }}</span>
              <el-tag v-if="model.wordGenerationDefault" type="success" size="small" effect="plain">默认</el-tag>
              <el-tag v-if="!model.enabled" type="info" size="small" effect="plain">已禁用</el-tag>
            </div>
            <div class="model-card__actions">
              <el-button text type="primary" size="small" @click="openEditDialog(model)">编辑</el-button>
              <el-button
                v-if="!model.wordGenerationDefault"
                text type="primary" size="small"
                @click="handleSetDefault(model)"
              >设为默认</el-button>
              <el-button text type="danger" size="small" @click="handleDelete(model)">删除</el-button>
            </div>
          </div>
          <div class="model-card__body">
            <div class="model-info-row">
              <span class="info-label">供应商</span>
              <span class="info-value">{{ model.providerName }}</span>
            </div>
            <div class="model-info-row">
              <span class="info-label">模型</span>
              <span class="info-value">{{ model.modelName }}</span>
            </div>
            <div v-if="model.baseUrl" class="model-info-row">
              <span class="info-label">Base URL</span>
              <span class="info-value">{{ model.baseUrl }}</span>
            </div>
            <div v-if="model.apiKeyMasked" class="model-info-row">
              <span class="info-label">API Key</span>
              <span class="info-value info-value--mono">{{ model.apiKeyMasked }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 个人档案 -->
    <section class="settings-section">
      <div class="section-header">
        <h2 class="section-title">个人档案</h2>
      </div>

      <div v-loading="profileLoading" class="profile-card">
        <el-form label-position="top">
          <el-form-item label="昵称">
            <el-input v-model="profileForm.nickname" placeholder="输入昵称" maxlength="50" />
          </el-form-item>
          <el-form-item label="个人信息">
            <el-input
              v-model="profileForm.profileMarkdown"
              type="textarea"
              :rows="5"
              placeholder="支持 Markdown 格式，AI 可根据此信息提供个性化服务"
            />
          </el-form-item>
          <el-form-item>
            <el-switch v-model="profileForm.allowAiReadProfile" active-text="允许 AI 读取个人信息" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="profileSaving" @click="handleSaveProfile">保存档案</el-button>
          </el-form-item>
        </el-form>
      </div>
    </section>

    <!-- 新增/编辑模型弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑模型' : '新增模型'"
      width="520px"
      destroy-on-close
    >
      <el-form ref="modelFormRef" :model="modelForm" :rules="modelRules" label-position="top">
        <el-form-item label="供应商" prop="providerName">
          <el-select v-model="modelForm.providerName" placeholder="选择供应商" filterable allow-create>
            <el-option v-for="p in providerOptions" :key="p" :label="p" :value="p" />
          </el-select>
        </el-form-item>
        <el-form-item label="显示名称" prop="displayName">
          <el-input v-model="modelForm.displayName" placeholder="例如：GPT-4o" />
        </el-form-item>
        <el-form-item label="Base URL" prop="baseUrl">
          <el-input v-model="modelForm.baseUrl" placeholder="https://api.openai.com（选填）" />
        </el-form-item>
        <el-form-item label="Completions Path" prop="completionsPath">
          <el-input v-model="modelForm.completionsPath" placeholder="/v1/chat/completions（选填，默认 /v1/chat/completions）" />
        </el-form-item>
        <el-form-item label="模型名称" prop="modelName">
          <el-input v-model="modelForm.modelName" placeholder="例如：gpt-4o" />
        </el-form-item>
        <el-form-item label="API Key" prop="apiKey">
          <el-input
            v-model="modelForm.apiKey"
            type="password"
            show-password
            :placeholder="isEdit ? '留空则保持原值' : '输入 API Key'"
          />
        </el-form-item>
        <div class="dialog-switches">
          <el-checkbox v-model="modelForm.enabled">启用</el-checkbox>
          <el-checkbox v-model="modelForm.wordGenerationDefault">设为单词卡生成默认模型</el-checkbox>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="modelSaving" @click="handleSubmitModel">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { ModelConfigVO } from '@/types/settings'
import * as settingsApi from '@/services/settings'

// ====== 模型配置 ======
const models = ref<ModelConfigVO[]>([])
const modelsLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const modelSaving = ref(false)
const modelFormRef = ref<FormInstance>()

const modelForm = reactive({
  providerName: '',
  displayName: '',
  baseUrl: '',
  completionsPath: '',
  modelName: '',
  apiKey: '',
  enabled: true,
  wordGenerationDefault: false
})

const providerOptions = [
  'OpenAI',
  'Anthropic',
  'Google',
  'DeepSeek',
  'Moonshot',
  'Zhipu',
  'Baichuan',
  'Qwen',
  'Ollama'
]

const modelRules: FormRules = {
  providerName: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  displayName: [{ required: true, message: '请输入显示名称', trigger: 'blur' }],
  modelName: [{ required: true, message: '请输入模型名称', trigger: 'blur' }]
}

async function loadModels() {
  modelsLoading.value = true
  try {
    const { data } = await settingsApi.getModelConfigs()
    models.value = data.data
  } finally {
    modelsLoading.value = false
  }
}

function openCreateDialog() {
  isEdit.value = false
  editingId.value = null
  Object.assign(modelForm, {
    providerName: '',
    displayName: '',
    baseUrl: '',
    completionsPath: '',
    modelName: '',
    apiKey: '',
    enabled: true,
    wordGenerationDefault: false
  })
  dialogVisible.value = true
}

function openEditDialog(model: ModelConfigVO) {
  isEdit.value = true
  editingId.value = model.id
  Object.assign(modelForm, {
    providerName: model.providerName,
    displayName: model.displayName,
    baseUrl: model.baseUrl || '',
    completionsPath: model.completionsPath || '',
    modelName: model.modelName,
    apiKey: '',
    enabled: model.enabled,
    wordGenerationDefault: model.wordGenerationDefault
  })
  dialogVisible.value = true
}

async function handleSubmitModel() {
  const valid = await modelFormRef.value?.validate().catch(() => false)
  if (!valid) return

  modelSaving.value = true
  try {
    const payload = {
      providerName: modelForm.providerName,
      displayName: modelForm.displayName,
      baseUrl: modelForm.baseUrl || undefined,
      completionsPath: modelForm.completionsPath || undefined,
      modelName: modelForm.modelName,
      apiKey: modelForm.apiKey || undefined,
      enabled: modelForm.enabled,
      wordGenerationDefault: modelForm.wordGenerationDefault
    }

    if (isEdit.value && editingId.value !== null) {
      await settingsApi.updateModelConfig(editingId.value, payload)
      ElMessage.success('模型已更新')
    } else {
      await settingsApi.createModelConfig(payload)
      ElMessage.success('模型已添加')
    }
    dialogVisible.value = false
    await loadModels()
  } finally {
    modelSaving.value = false
  }
}

async function handleSetDefault(model: ModelConfigVO) {
  try {
    await settingsApi.setWordGenerationDefault(model.id)
    ElMessage.success(`已将「${model.displayName}」设为默认模型`)
    await loadModels()
  } catch { /* 拦截器已处理 */ }
}

async function handleDelete(model: ModelConfigVO) {
  try {
    await ElMessageBox.confirm(
      `确定删除模型「${model.displayName}」？此操作不可撤销。`,
      '确认删除',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
    )
  } catch {
    return // 用户取消
  }
  try {
    await settingsApi.deleteModelConfig(model.id)
    ElMessage.success('模型已删除')
    await loadModels()
  } catch { /* 拦截器已处理 */ }
}

// ====== 用户档案 ======
const profileLoading = ref(false)
const profileSaving = ref(false)
const profileForm = reactive({
  nickname: '',
  profileMarkdown: '',
  allowAiReadProfile: false
})

async function loadProfile() {
  profileLoading.value = true
  try {
    const { data } = await settingsApi.getUserProfile()
    const p = data.data
    profileForm.nickname = p.nickname || ''
    profileForm.profileMarkdown = p.profileMarkdown || ''
    profileForm.allowAiReadProfile = p.allowAiReadProfile
  } finally {
    profileLoading.value = false
  }
}

async function handleSaveProfile() {
  profileSaving.value = true
  try {
    await settingsApi.updateUserProfile({
      nickname: profileForm.nickname || undefined,
      profileMarkdown: profileForm.profileMarkdown || undefined,
      allowAiReadProfile: profileForm.allowAiReadProfile
    })
    ElMessage.success('档案已保存')
  } finally {
    profileSaving.value = false
  }
}

// ====== 初始化 ======
onMounted(() => {
  loadModels()
  loadProfile()
})
</script>

<style scoped>
.settings-page {
  width: 100%;
}

.settings-section {
  margin-bottom: var(--spacing-xl);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-md);
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
}

/* 模型卡片列表 */
.model-list {
  min-height: 100px;
}

.model-card {
  background: #fff;
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md);
  padding: var(--spacing-md) var(--spacing-lg);
  margin-bottom: var(--spacing-sm);
  transition: box-shadow var(--transition-fast);
}

.model-card:hover {
  box-shadow: var(--shadow-sm);
}

.model-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-sm);
}

.model-card__title {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.model-display-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.model-card__actions {
  display: flex;
  gap: 0;
}

.model-card__body {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm) var(--spacing-lg);
}

.model-info-row {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: 13px;
}

.info-label {
  color: var(--color-text-secondary);
}

.info-value {
  color: var(--color-text-regular);
}

.info-value--mono {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 12px;
}

/* 用户档案 */
.profile-card {
  background: #fff;
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
}

/* 弹窗开关行 */
.dialog-switches {
  display: flex;
  gap: var(--spacing-lg);
  padding-top: var(--spacing-xs);
}
</style>