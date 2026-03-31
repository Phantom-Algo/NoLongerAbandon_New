<template>
  <div class="notebook-page">
    <!-- ====== 单词本列表视图 ====== -->
    <template v-if="!selectedNotebook">
      <div class="page-header">
        <h2 class="page-title">单词本</h2>
        <el-button type="primary" :icon="Plus" @click="openCreateDialog">新建单词本</el-button>
      </div>

      <div v-loading="notebooksLoading" class="notebook-list">
        <el-empty v-if="!notebooksLoading && notebooks.length === 0" description="暂无单词本，点击上方按钮创建" />

        <div
          v-for="item in notebooks"
          :key="item.id"
          class="notebook-card"
          @click="enterNotebook(item)"
        >
          <div class="notebook-card__icon">
            <SvgIcon :content="icons.notebook" :size="24" />
          </div>
          <div class="notebook-card__info">
            <div class="notebook-card__name">{{ item.name }}</div>
            <div class="notebook-card__meta">
              {{ item.wordCount }} 个单词
              <span v-if="item.description"> · {{ item.description }}</span>
            </div>
          </div>
          <div class="notebook-card__actions" @click.stop>
            <el-button text type="primary" size="small" @click="openEditDialog(item)">编辑</el-button>
            <el-popconfirm
              title="确定删除此单词本？关联的单词也将移除。"
              confirm-button-text="删除"
              cancel-button-text="取消"
              @confirm="handleDeleteNotebook(item)"
            >
              <template #reference>
                <el-button text type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </div>
        </div>
      </div>
    </template>

    <!-- ====== 单词列表视图 ====== -->
    <template v-else>
      <div class="page-header">
        <div class="page-header__left">
          <el-button text :icon="ArrowLeft" @click="backToList">返回</el-button>
          <h2 class="page-title">{{ selectedNotebook.name }}</h2>
          <el-tag size="small" type="info">{{ selectedNotebook.wordCount }} 个单词</el-tag>
        </div>
        <div class="page-header__right">
          <el-radio-group v-model="sortMode" size="small" @change="loadWords">
            <el-radio-button value="time">按加入时间</el-radio-button>
            <el-radio-button value="search_count">按搜索次数</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <div v-loading="wordsLoading" class="words-section">
        <el-empty v-if="!wordsLoading && words.length === 0" description="此单词本暂无单词" />

        <el-table v-if="words.length > 0" :data="words" stripe style="width: 100%">
          <el-table-column prop="word" label="单词" min-width="160" />
          <el-table-column prop="sourceLanguage" label="语言" width="80" />
          <el-table-column prop="searchCount" label="搜索次数" width="100" sortable />
          <el-table-column prop="addedAt" label="加入时间" width="180" />
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-popconfirm
                title="确定从单词本中移除此单词？"
                confirm-button-text="移除"
                cancel-button-text="取消"
                @confirm="handleRemoveWord(row)"
              >
                <template #reference>
                  <el-button text type="danger" size="small">移除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </template>

    <!-- ====== 新建/编辑单词本弹窗 ====== -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑单词本' : '新建单词本'"
      width="440px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="notebookForm" :rules="formRules" label-position="top">
        <el-form-item label="名称" prop="name">
          <el-input v-model="notebookForm.name" placeholder="输入单词本名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="notebookForm.description"
            type="textarea"
            :rows="3"
            placeholder="简短描述（选填）"
            maxlength="200"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="formSaving" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, ArrowLeft } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import SvgIcon from '@/components/SvgIcon.vue'
import * as icons from '@/assets/icons'
import type { NotebookVO, NotebookWordVO } from '@/types/notebook'
import * as notebookApi from '@/services/notebook'

// ====== 单词本列表 ======
const notebooks = ref<NotebookVO[]>([])
const notebooksLoading = ref(false)

async function loadNotebooks() {
  notebooksLoading.value = true
  try {
    const { data } = await notebookApi.getNotebooks()
    notebooks.value = data.data
  } finally {
    notebooksLoading.value = false
  }
}

// ====== 单词本详情（单词列表） ======
const selectedNotebook = ref<NotebookVO | null>(null)
const words = ref<NotebookWordVO[]>([])
const wordsLoading = ref(false)
const sortMode = ref('time')

function enterNotebook(notebook: NotebookVO) {
  selectedNotebook.value = notebook
  sortMode.value = 'time'
  loadWords()
}

function backToList() {
  selectedNotebook.value = null
  words.value = []
  loadNotebooks()
}

async function loadWords() {
  if (!selectedNotebook.value) return
  wordsLoading.value = true
  try {
    const { data } = await notebookApi.getNotebookWords(selectedNotebook.value.id, sortMode.value)
    words.value = data.data
  } finally {
    wordsLoading.value = false
  }
}

async function handleRemoveWord(row: NotebookWordVO) {
  if (!selectedNotebook.value) return
  try {
    await notebookApi.removeWordFromNotebook(selectedNotebook.value.id, row.wordId)
    ElMessage.success('已移除')
    selectedNotebook.value = {
      ...selectedNotebook.value,
      wordCount: selectedNotebook.value.wordCount - 1
    }
    await loadWords()
  } catch { /* 拦截器已处理 */ }
}

// ====== 新建/编辑弹窗 ======
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const formSaving = ref(false)
const formRef = ref<FormInstance>()

const notebookForm = reactive({
  name: '',
  description: ''
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入单词本名称', trigger: 'blur' }]
}

function openCreateDialog() {
  isEdit.value = false
  editingId.value = null
  Object.assign(notebookForm, { name: '', description: '' })
  dialogVisible.value = true
}

function openEditDialog(notebook: NotebookVO) {
  isEdit.value = true
  editingId.value = notebook.id
  Object.assign(notebookForm, {
    name: notebook.name,
    description: notebook.description || ''
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  formSaving.value = true
  try {
    const payload = {
      name: notebookForm.name,
      description: notebookForm.description || undefined
    }

    if (isEdit.value && editingId.value !== null) {
      await notebookApi.updateNotebook(editingId.value, payload)
      ElMessage.success('单词本已更新')
    } else {
      await notebookApi.createNotebook(payload)
      ElMessage.success('单词本已创建')
    }
    dialogVisible.value = false
    await loadNotebooks()
  } finally {
    formSaving.value = false
  }
}

async function handleDeleteNotebook(notebook: NotebookVO) {
  try {
    await notebookApi.deleteNotebook(notebook.id)
    ElMessage.success('单词本已删除')
    await loadNotebooks()
  } catch { /* 拦截器已处理 */ }
}

// ====== 初始化 ======
onMounted(() => {
  loadNotebooks()
})
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

.page-header__left {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.page-header__left .page-title {
  margin: 0;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
}

/* ====== 单词本卡片列表 ====== */
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
  flex-shrink: 0;
}

.notebook-card__info {
  flex: 1;
  min-width: 0;
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
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notebook-card__actions {
  flex-shrink: 0;
  display: flex;
  gap: 4px;
}

/* ====== 单词列表表格 ====== */
.words-section {
  min-height: 200px;
}
</style>
