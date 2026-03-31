<template>
  <div class="word-search-container">
    <!-- ====== 搜索区域 ====== -->
    <div class="search-header">
      <h1>单词搜索</h1>
      <p>输入英文单词或中文释义，AI 为你生成详细单词卡</p>
    </div>

    <div class="search-bar">
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
      <el-button type="primary" size="large" :loading="searching" @click="handleSearch">
        查询
      </el-button>
    </div>

    <div class="search-actions">
      <el-button size="small" @click="openModuleDialog">+ 模块</el-button>
      <div class="search-hints">
        <span>英文直查</span>
        <span>中文反查</span>
        <span>AI 单词卡生成</span>
      </div>
    </div>

    <!-- ====== 加载状态 ====== -->
    <div v-if="searching" class="search-loading">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
      <p>{{ isChinese(keyword) ? 'AI 正在查找候选词...' : 'AI 正在生成单词卡...' }}</p>
    </div>

    <!-- ====== 中文候选词列表 ====== -->
    <div v-if="chineseResult && !wordCardResult" class="chinese-candidates">
      <h3>「{{ chineseResult.chinese }}」的相关英文单词</h3>
      <div class="candidate-list">
        <div
          v-for="(c, idx) in chineseResult.candidates"
          :key="idx"
          class="candidate-item"
          :class="{ selected: selectedCandidate === c.word }"
          @click="selectCandidate(c.word)"
        >
          <span class="candidate-word">{{ c.word }}</span>
          <span class="candidate-def">{{ c.definition }}</span>
        </div>
      </div>
    </div>

    <!-- ====== 单词卡 ====== -->
    <div v-if="wordCardResult" class="word-card">
      <div class="word-card-header">
        <h2>{{ wordCardResult.word }}</h2>
        <div class="word-meta">
          <span>搜索 {{ wordCardResult.searchCount }} 次</span>
          <el-tag v-if="wordCardResult.cached" size="small" effect="dark" type="info">缓存</el-tag>
          <el-tag v-else size="small" effect="dark" type="success">新生成</el-tag>
        </div>
      </div>

      <div class="word-card-actions">
        <el-button size="small" type="primary" plain @click="openRegenDialog">重新生成</el-button>
        <el-button size="small" plain @click="openNotebookDialog">加入单词本</el-button>
      </div>

      <div class="word-card-body">
        <!-- 按 section 展示 -->
        <div
          v-for="section in wordCardResult.wordCard.sections"
          :key="section.id"
          class="section-block"
        >
          <div class="section-header">
            <span class="section-title">{{ section.sectionTitle }}</span>
            <div>
              <el-tag v-if="section.preset" class="section-tag" size="small">预设</el-tag>
              <el-button
                class="section-regen-btn"
                size="small"
                text
                type="primary"
                :loading="regenSectionId === section.id"
                @click="openSectionRegenDialog(section)"
              >
                重新生成此模块
              </el-button>
            </div>
          </div>
          <div class="section-content markdown-content" v-html="renderMarkdown(section.sectionContent)"></div>
        </div>
      </div>
    </div>

    <!-- ====== 空状态 ====== -->
    <el-empty v-if="!searching && !wordCardResult && !chineseResult" description="输入关键词开始搜索" />

    <!-- ====== 自定义模块管理弹窗 ====== -->
    <el-dialog v-model="moduleDialogVisible" title="自定义模块管理" width="520px" destroy-on-close>
      <p style="font-size: 13px; color: var(--text-secondary); margin-bottom: 12px;">
        勾选需要附加到单词卡的模块，搜索时 AI 将额外生成这些内容。
      </p>

      <div class="custom-section-list">
        <el-empty v-if="customSections.length === 0" description="暂无自定义模块" :image-size="60" />
        <div v-for="item in customSections" :key="item.id" class="custom-section-item">
          <el-checkbox
            :model-value="selectedCustomIds.includes(item.id)"
            @change="(val: boolean) => toggleCustomId(item.id, val)"
          />
          <div class="custom-section-info">
            <div class="custom-section-title">{{ item.title }}</div>
            <div class="custom-section-prompt">{{ item.prompt }}</div>
          </div>
          <div class="custom-section-ops">
            <el-button text size="small" type="primary" @click="openEditCustom(item)">编辑</el-button>
            <el-popconfirm title="确定删除此模块？" @confirm="handleDeleteCustom(item.id)">
              <template #reference>
                <el-button text size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </div>
        </div>
      </div>

      <el-divider />
      <h4 style="margin-bottom: 8px;">{{ editingCustom ? '编辑模块' : '新增模块' }}</h4>
      <el-form :model="customForm" label-position="top" size="small">
        <el-form-item label="模块名称">
          <el-input v-model="customForm.title" placeholder="如：词根分析" maxlength="30" />
        </el-form-item>
        <el-form-item label="AI 提示词">
          <el-input
            v-model="customForm.prompt"
            type="textarea"
            :rows="3"
            placeholder="如：请分析这个单词的词根和词缀"
            maxlength="500"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="customSaving" @click="handleSaveCustom">
            {{ editingCustom ? '保存修改' : '添加' }}
          </el-button>
          <el-button v-if="editingCustom" @click="resetCustomForm">取消编辑</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>

    <!-- ====== 整卡重新生成弹窗 ====== -->
    <el-dialog v-model="regenDialogVisible" title="重新生成单词卡" width="480px" destroy-on-close class="regen-dialog">
      <p style="font-size: 13px; color: var(--text-secondary);">可选择附加自定义模块并输入额外提示词</p>
      <div style="margin: 12px 0;">
        <el-checkbox
          v-for="item in customSections"
          :key="item.id"
          :model-value="regenCustomIds.includes(item.id)"
          :label="item.title"
          @change="(val: boolean) => toggleRegenCustomId(item.id, val)"
        />
      </div>
      <el-input
        v-model="regenPrompt"
        type="textarea"
        :rows="3"
        placeholder="额外提示词（选填）"
      />
      <template #footer>
        <el-button @click="regenDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="regenerating" @click="handleRegenerate">确认重新生成</el-button>
      </template>
    </el-dialog>

    <!-- ====== 单模块重新生成弹窗 ====== -->
    <el-dialog v-model="sectionRegenDialogVisible" title="重新生成此模块" width="440px" destroy-on-close class="regen-dialog">
      <p style="font-size: 13px; color: var(--text-secondary);">
        正在重新生成「{{ regenSectionTitle }}」模块
      </p>
      <el-input
        v-model="sectionRegenPrompt"
        type="textarea"
        :rows="3"
        placeholder="额外提示词（选填）"
      />
      <template #footer>
        <el-button @click="sectionRegenDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="regenSectionId !== null" @click="handleSectionRegenerate">确认</el-button>
      </template>
    </el-dialog>

    <!-- ====== 加入单词本弹窗 ====== -->
    <el-dialog v-model="notebookDialogVisible" title="加入单词本" width="440px" destroy-on-close>
      <div v-loading="notebooksLoading" class="notebook-select-list">
        <el-empty v-if="!notebooksLoading && notebookList.length === 0" description="暂无单词本" :image-size="60" />
        <div
          v-for="nb in notebookList"
          :key="nb.id"
          class="notebook-select-item"
          :class="{ selected: selectedNotebookId === nb.id }"
          @click="selectedNotebookId = nb.id"
        >
          <SvgIcon :content="icons.notebook" :size="18" />
          <span>{{ nb.name }}</span>
          <span style="margin-left: auto; font-size: 12px; color: var(--text-secondary);">
            {{ nb.wordCount }} 个单词
          </span>
        </div>
      </div>
      <template #footer>
        <el-button @click="notebookDialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!selectedNotebookId" :loading="addingToNotebook" @click="handleAddToNotebook">
          加入
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { marked } from 'marked'
import SvgIcon from '@/components/SvgIcon.vue'
import * as icons from '@/assets/icons'
import type {
  WordCardVO,
  ChineseLookupVO,
  CustomSectionTemplateVO,
  SectionVO
} from '@/types/wordcard'
import type { NotebookVO } from '@/types/notebook'
import * as wordcardApi from '@/services/wordcard'
import * as notebookApi from '@/services/notebook'

// ===== localStorage key =====
const LS_KEY = 'nla_selected_custom_sections'

// ===== 搜索状态 =====
const keyword = ref('')
const searching = ref(false)
const wordCardResult = ref<WordCardVO | null>(null)
const chineseResult = ref<ChineseLookupVO | null>(null)
const selectedCandidate = ref('')

// ===== 自定义模块 =====
const moduleDialogVisible = ref(false)
const customSections = ref<CustomSectionTemplateVO[]>([])
const selectedCustomIds = ref<number[]>([])
const customForm = ref({ title: '', prompt: '' })
const editingCustom = ref<CustomSectionTemplateVO | null>(null)
const customSaving = ref(false)

// ===== 整卡重新生成 =====
const regenDialogVisible = ref(false)
const regenCustomIds = ref<number[]>([])
const regenPrompt = ref('')
const regenerating = ref(false)

// ===== 单模块重新生成 =====
const sectionRegenDialogVisible = ref(false)
const sectionRegenPrompt = ref('')
const regenSectionTitle = ref('')
const regenSectionId = ref<number | null>(null)

// ===== 加入单词本 =====
const notebookDialogVisible = ref(false)
const notebookList = ref<NotebookVO[]>([])
const notebooksLoading = ref(false)
const selectedNotebookId = ref<number | null>(null)
const addingToNotebook = ref(false)

// ===== 初始化 =====
onMounted(() => {
  loadCustomSections()
  // 从 localStorage 恢复选中的自定义模块
  try {
    const saved = localStorage.getItem(LS_KEY)
    if (saved) selectedCustomIds.value = JSON.parse(saved)
  } catch { /* 忽略 */ }
})

// ===== 工具函数 =====
/** 判断是否包含中文字符 */
function isChinese(text: string): boolean {
  return /[\u4e00-\u9fa5]/.test(text)
}

/** 渲染 markdown 为 HTML */
function renderMarkdown(content: string): string {
  return marked.parse(content, { async: false }) as string
}

// ===== 搜索逻辑 =====
async function handleSearch() {
  const word = keyword.value.trim()
  if (!word) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  // 重置结果
  wordCardResult.value = null
  chineseResult.value = null
  selectedCandidate.value = ''
  searching.value = true

  try {
    if (isChinese(word)) {
      // 中文流程：先查候选词
      const { data } = await wordcardApi.chineseLookup({ chinese: word })
      chineseResult.value = data.data
      if (!chineseResult.value.candidates || chineseResult.value.candidates.length === 0) {
        ElMessage.info('未找到相关英文单词')
      }
    } else {
      // 英文流程：直接搜索
      await doEnglishSearch(word)
    }
  } catch {
    // 错误已在拦截器中处理
  } finally {
    searching.value = false
  }
}

/** 选择中文候选词后执行英文搜索 */
async function selectCandidate(word: string) {
  selectedCandidate.value = word
  searching.value = true
  try {
    await doEnglishSearch(word)
    // 搜索成功后清除候选词面板
    chineseResult.value = null
  } catch {
    // 错误已在拦截器中处理
  } finally {
    searching.value = false
  }
}

/** 英文搜索核心逻辑 */
async function doEnglishSearch(word: string) {
  const { data } = await wordcardApi.searchWord({
    word,
    customSectionIds: selectedCustomIds.value.length > 0 ? selectedCustomIds.value : undefined
  })
  wordCardResult.value = data.data
}

// ===== 自定义模块管理 =====
async function loadCustomSections() {
  try {
    const { data } = await wordcardApi.listCustomSections()
    customSections.value = data.data
    // 清理已不存在的选中 ID
    const existingIds = new Set(customSections.value.map(s => s.id))
    selectedCustomIds.value = selectedCustomIds.value.filter(id => existingIds.has(id))
    saveSelectedIds()
  } catch { /* 首次加载失败不影响搜索 */ }
}

function openModuleDialog() {
  loadCustomSections()
  moduleDialogVisible.value = true
}

function toggleCustomId(id: number, checked: boolean) {
  if (checked) {
    selectedCustomIds.value.push(id)
  } else {
    selectedCustomIds.value = selectedCustomIds.value.filter(i => i !== id)
  }
  saveSelectedIds()
}

function saveSelectedIds() {
  localStorage.setItem(LS_KEY, JSON.stringify(selectedCustomIds.value))
}

function openEditCustom(item: CustomSectionTemplateVO) {
  editingCustom.value = item
  customForm.value = { title: item.title, prompt: item.prompt }
}

function resetCustomForm() {
  editingCustom.value = null
  customForm.value = { title: '', prompt: '' }
}

async function handleSaveCustom() {
  if (!customForm.value.title.trim() || !customForm.value.prompt.trim()) {
    ElMessage.warning('请填写模块名称和提示词')
    return
  }
  customSaving.value = true
  try {
    if (editingCustom.value) {
      await wordcardApi.updateCustomSection(editingCustom.value.id, customForm.value)
      ElMessage.success('已更新')
    } else {
      await wordcardApi.createCustomSection(customForm.value)
      ElMessage.success('已添加')
    }
    resetCustomForm()
    await loadCustomSections()
  } finally {
    customSaving.value = false
  }
}

async function handleDeleteCustom(id: number) {
  await wordcardApi.deleteCustomSection(id)
  ElMessage.success('已删除')
  await loadCustomSections()
}

// ===== 整卡重新生成 =====
function openRegenDialog() {
  regenCustomIds.value = [...selectedCustomIds.value]
  regenPrompt.value = ''
  regenDialogVisible.value = true
}

function toggleRegenCustomId(id: number, checked: boolean) {
  if (checked) {
    regenCustomIds.value.push(id)
  } else {
    regenCustomIds.value = regenCustomIds.value.filter(i => i !== id)
  }
}

async function handleRegenerate() {
  if (!wordCardResult.value) return
  regenerating.value = true
  try {
    const { data } = await wordcardApi.regenerateWordCard(wordCardResult.value.wordId, {
      customSectionIds: regenCustomIds.value.length > 0 ? regenCustomIds.value : undefined,
      userPrompt: regenPrompt.value.trim() || undefined
    })
    wordCardResult.value = data.data
    regenDialogVisible.value = false
    ElMessage.success('已重新生成')
  } finally {
    regenerating.value = false
  }
}

// ===== 单模块重新生成 =====
function openSectionRegenDialog(section: SectionVO) {
  regenSectionTitle.value = section.sectionTitle
  regenSectionId.value = section.id
  sectionRegenPrompt.value = ''
  sectionRegenDialogVisible.value = true
}

async function handleSectionRegenerate() {
  if (!wordCardResult.value || regenSectionId.value === null) return
  const sectionId = regenSectionId.value
  try {
    const { data } = await wordcardApi.regenerateSection(
      wordCardResult.value.wordId,
      sectionId,
      { userPrompt: sectionRegenPrompt.value.trim() || undefined }
    )
    wordCardResult.value = data.data
    sectionRegenDialogVisible.value = false
    ElMessage.success('已重新生成此模块')
  } finally {
    regenSectionId.value = null
  }
}

// ===== 加入单词本 =====
async function openNotebookDialog() {
  selectedNotebookId.value = null
  notebookDialogVisible.value = true
  notebooksLoading.value = true
  try {
    const { data } = await notebookApi.getNotebooks()
    notebookList.value = data.data
  } finally {
    notebooksLoading.value = false
  }
}

async function handleAddToNotebook() {
  if (!selectedNotebookId.value || !wordCardResult.value) return
  addingToNotebook.value = true
  try {
    await notebookApi.addWordToNotebook(selectedNotebookId.value, {
      wordId: wordCardResult.value.wordId
    })
    notebookDialogVisible.value = false
    ElMessage.success('已加入单词本')
  } catch {
    // 错误已在拦截器中处理
  } finally {
    addingToNotebook.value = false
  }
}
</script>