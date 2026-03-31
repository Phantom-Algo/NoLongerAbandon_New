# 开发日志

---

## 2026.4.1 - "中英文单词搜索模块" - 状态：已完成

### 概述
实现完整的中英文单词搜索功能：后端 10 个 API 端点（英文搜索 + AI 单词卡生成、单词卡详情/整卡重新生成/单模块重新生成、中文反查候选词、自定义模块模板 CRUD），前端 WordSearchView 完整交互对接。新增数据库表 `custom_section_template`（V2 迁移）。

### 细节

#### 1. 数据库迁移
- 新增 `V2__AddCustomSectionTemplate.sql`：创建 `custom_section_template` 表（id, title, prompt, created_at, updated_at），用于用户自定义单词卡模块模板。

#### 2. 后端新增文件（共 22 个 Java 文件）
- **Entity**：`CustomSectionTemplate`、`WordCard`、`WordCardSection`，分别映射 `custom_section_template`、`word_card`、`word_card_section` 表。
- **Mapper**：`CustomSectionTemplateMapper`、`WordCardMapper`、`WordCardSectionMapper`，均继承 BaseMapper。
- **DTO**：`WordSearchRequest`（word @NotBlank + customSectionIds）、`WordCardRegenerateRequest`（customSectionIds + userPrompt）、`SectionRegenerateRequest`（userPrompt）、`ChineseLookupRequest`（chinese @NotBlank）、`CustomSectionCreateRequest`（title + prompt）、`CustomSectionUpdateRequest`（同）。
- **VO**：`WordCardVO`（wordId, word, searchCount, cached, wordCard）、`WordCardDetailVO`（id, title, rawMarkdown, generatedByModel, sections）、`SectionVO`（id, sectionKey, sectionTitle, sectionContent, sortOrder, preset）、`CustomSectionTemplateVO`（id, title, prompt, timestamps）、`ChineseLookupVO`（chinese, candidates）、`CandidateWordVO`（word, definition）。
- **Service**：`WordCardGenerationService` 接口 + `WordCardGenerationServiceImpl`（AI 生成核心）、`CustomSectionTemplateService` 接口 + `CustomSectionTemplateServiceImpl`（自定义模块 CRUD）。
- **Controller**：`WordCardController` 全面重写，10 个端点。

#### 3. AI 生成实现
- `WordCardGenerationServiceImpl` 从数据库读取用户配置的默认模型（`model_config` 表 `is_word_generation_default=1`），解密 API Key，手动构建 `OpenAiApi` + `OpenAiChatModel`（因 application.yml 排除了 OpenAI 自动配置）。
- 预设 5 个模块：基本释义、音标、例句、短语、记背方法（作为常量定义）。
- Prompt 设计：系统消息定义 AI 角色为英语学习助手，要求以 Markdown `##` 标题格式输出各模块内容，标题需与模块名称完全一致。
- Markdown 解析：使用正则 `^##\\s+(.+)$` 按 `##` 标题拆分为 sections。
- Spring AI 1.0.0-M6 特殊 API：`.model()` 而非 `.withModel()`，`.getText()` 而非 `.getContent()`。

#### 4. 搜索与缓存逻辑
- `WordCardQueryServiceImpl.search()`：normalize word → 查缓存 → 命中则 search_count+1 直接返回 → 未命中则调用 AI 生成 → 保存 word + word_card + sections → 返回。
- 一词一卡策略：每个 word 只保留最新一张 word_card，重新生成时删旧存新。
- 中文搜索不做缓存：每次调用 AI 获取候选词，返回 JSON 数组后解析。

#### 5. 前端对接（新增 3 个文件 + 重写 1 个文件）
- `types/wordcard.ts`：全部 TypeScript 接口。
- `services/wordcard.ts`：9 个 API 调用函数，AI 类接口设置 120 秒超时。使用 `ApiResponse<T>` 泛型包装。
- `styles/wordcard.css`：模块独立样式，覆盖搜索区域、中文候选词、单词卡（绿色渐变 header）、Markdown 内容、section 分块（hover 高亮）、自定义模块管理弹窗、重新生成弹窗、加入单词本弹窗、loading 状态。
- `WordSearchView.vue` 完全重写：
  - 搜索输入自动检测中文/英文（正则 `[\u4e00-\u9fa5]`）
  - "+模块"按钮 → 弹出自定义模块管理弹窗（CRUD + checkbox 选择，选中 ID 存 localStorage）
  - 中文流程：调用 `chinese-lookup` → 展示候选词列表 → 点击候选词触发英文搜索
  - 英文流程：直接调用 `search`
  - 单词卡展示：header（单词 + 搜索次数 + 缓存/新生成标签）+ section 分块渲染（ `marked` 渲染 Markdown）
  - 每个 section hover 显示"重新生成此模块"按钮 → 弹出提示词输入
  - "重新生成"按钮 → 弹出模块选择 + 提示词输入
  - "加入单词本"按钮 → 弹出单词本选择列表

#### 6. 文档
- 新增 `docs/openapi/wordcard-openapi.yml`，覆盖全部 10 个端点的请求体、响应体 schema。
- 更新 `index.css` 引入 `wordcard.css`。

#### 7. 后端 API 端点一览
- `POST /api/word-cards/preview` — 单词预览（不触发 AI）
- `POST /api/word-cards/search` — 英文搜索（AI 生成/缓存返回）
- `GET /api/word-cards/{wordId}` — 单词卡详情
- `POST /api/word-cards/{wordId}/regenerate` — 整卡重新生成
- `POST /api/word-cards/{wordId}/sections/{sectionId}/regenerate` — 单模块重新生成
- `POST /api/word-cards/chinese-lookup` — 中文反查候选词
- `GET /api/word-cards/custom-sections` — 查询全部自定义模块模板
- `POST /api/word-cards/custom-sections` — 新增自定义模块模板
- `PUT /api/word-cards/custom-sections/{id}` — 更新自定义模块模板
- `DELETE /api/word-cards/custom-sections/{id}` — 删除自定义模块模板

### 问题与建议
1. AI 生成耗时较长（取决于模型和网络），前端已设置 120 秒超时并显示 loading 状态。
2. 中文反查候选词每次都调用 AI，暂不做缓存（简化实现）。
3. 用户划定注入上下文和 AI chat 功能暂未实现，后续迭代。

---

## 2026.3.31 21:00 - "单词本模块增删改查 API + 前端对接" - 状态：已完成

### 概述
实现 notebook 模块完整的后端 REST API（7 个端点）与前端 NotebookView 页面对接，覆盖单词本管理（增删改查）和单词本内单词管理（添加/移除/列表查询）。

### 细节
1. **后端分层实现**（共新增 12 个 Java 文件）：
   - **Entity**：`VocabularyBook`、`VocabularyBookWord`，映射 `vocabulary_book` 和 `vocabulary_book_word` 表（表已在 V1 迁移中定义，无需新增迁移）。
   - **Mapper**：`VocabularyBookMapper` 继承 BaseMapper；`VocabularyBookWordMapper` 除 BaseMapper 外新增两个 `@Select` 联表查询方法（JOIN word 表），分别按加入时间和搜索次数排序，遵守"SQL 不入侵 Service"铁律。
   - **DTO**：`NotebookCreateRequest`（name @NotBlank + description）、`NotebookUpdateRequest`（同）、`NotebookAddWordRequest`（wordId @NotNull）。
   - **VO**：`NotebookVO`（含 wordCount 聚合字段）、`NotebookWordVO`（含联表查询结果，使用 @NoArgsConstructor + @AllArgsConstructor 配合 MyBatis 结果映射）。
   - **Service**：`NotebookService` 接口 + `NotebookServiceImpl` 实现。关键逻辑包括：创建时 trim + 唯一性校验、删除时 @Transactional 先删关联再删主表、添加单词校验 notebook 和 word 均存在且不重复、复用 wordcard 模块的 `WordMapper` 校验 wordId。
   - **Controller**：`NotebookController` @ `/api/notebooks`，7 个 RESTful 端点。

2. **API 端点一览**：
   - `GET /api/notebooks` — 单词本列表（含单词数量）
   - `POST /api/notebooks` — 创建单词本
   - `PUT /api/notebooks/{id}` — 更新单词本
   - `DELETE /api/notebooks/{id}` — 删除单词本（级联删除关联）
   - `GET /api/notebooks/{id}/words?sort=time|search_count` — 单词列表（支持排序）
   - `POST /api/notebooks/{id}/words` — 添加单词到单词本
   - `DELETE /api/notebooks/{id}/words/{wordId}` — 从单词本移除单词

3. **前端对接**（新增 2 个文件 + 重写 1 个文件）：
   - `types/notebook.ts`：NotebookVO、NotebookWordVO、请求类型定义。
   - `services/notebook.ts`：7 个 API 调用函数，参考 settings 模块模式。
   - `NotebookView.vue` 完全重写：同页面状态切换（单词本列表 ↔ 单词列表），支持新建/编辑单词本（el-dialog 表单）、删除（el-popconfirm）、点击卡片进入单词列表视图（el-table + 排序切换 radio-group）、移除单词（el-popconfirm）。

4. **OpenAPI 文档**：新增 `docs/openapi/notebook-openapi.yml`，覆盖全部 7 个端点的请求体、响应体 schema 及错误码说明。

### 问题与建议
1. 当前单词本内单词列表暂未实现分页，后续数据量增大后需要添加分页支持。
2. 向单词本添加单词的功能目前依赖后端接口，前端入口（如在单词搜索页面添加到单词本的按钮）将在后续单词搜索模块中实现。

---

## 2026.3.31 - 修复 API Key 解密失败 Bug - 状态：已完成

### 概述
修复设置页面加载时提示"API Key 解密失败"的 Bug。根因为加密密钥基于网络接口 MAC 地址派生，macOS 网络接口枚举顺序不稳定，导致每次 JVM 重启可能派生出不同密钥。

### 细节
1. **CryptoUtil 密钥派生方式重构**：移除基于 MAC 地址 + 主机名的 `deriveKey()` 方案，改为文件持久化随机密钥 (`./data/.encryption-key`)。首次启动生成 256 位随机密钥并写入文件，后续启动直接读取，确保密钥稳定。
2. **Service 层容错处理**：`SettingsServiceImpl.toModelConfigVO()` 中的 `CryptoUtil.decrypt()` 调用增加 try-catch，解密失败时不再抛异常导致整个模型列表加载崩溃，而是返回 `***解密失败，请重新编辑***` 的占位提示并输出 warn 日志。
3. 密钥文件位于 `backend/data/` 目录，已被该目录下 `.gitignore` 规则排除，不会被提交到版本库。

### 问题与建议
1. 由于密钥已更换，数据库中此前用旧密钥加密的 API Key 将无法解密，需要在设置页面重新编辑对应模型并填入 API Key。

---

## 2026.3.31 20:15 - "前端 UI 细节修正" - 状态：已完成

### 概述
修复侧边栏单词本导航交互、设置模块供应商选择方式、以及总览和单词本页面内容区不响应式拉伸的问题。

### 细节
1. **单词本导航**：移除下拉展开/收缩效果，改为直接 `router-link` 跳转，点击即进入单词本列表页面。删除了 `toggleNotebook`、`notebookExpanded`、`useRoute` 等关联代码和无用 CSS（nav-arrow / nav-sub-list / sub-menu 动画）。
2. **供应商选择**：新增/编辑模型弹窗中"供应商名称"改为"供应商"，输入方式从 `el-input` 改为 `el-select`（下拉选择），预置常见供应商（OpenAI / Anthropic / Google / DeepSeek / Moonshot / Zhipu / Baichuan / Qwen / Ollama），同时启用 `filterable allow-create` 支持用户自行输入不在列表中的供应商。校验触发从 `blur` 改为 `change`。
3. **响应式布局**：`DashboardView` 移除 `max-width: 900px`、`NotebookView` 移除 `max-width: 700px`，均改为 `width: 100%`，内容区随浏览器窗口宽度自动拉伸。

### 问题与建议
无。

---

## 2026.3.31 19:30 - "搭建前端主页面布局与设置模块对接后端 API" - 状态：已完成

### 概述
本阶段完成前端主页面"左二右八"布局搭建，实现侧边栏导航（LOGO / 实时时间 / 导航项 / 设置按钮），右侧内容区默认展示搜索页面。设置模块完整对接后端 7 个 API 端点（模型配置 CRUD + 用户档案）。其余模块仅实现静态 UI 骨架。AI 对话以右下角悬浮气泡形式呈现。

### 细节

#### 基础设施
1. `vite.config.ts` 新增 `/api` → `localhost:8080` 代理配置，前端开发模式下请求自动转发后端。
2. 新建 `services/request.ts`：axios 实例 + 响应拦截器，统一解析后端 `{ success, code, message, data }` 格式，失败自动 ElMessage 提示。
3. 新建 `types/api.ts`（通用 `ApiResponse<T>` 泛型）和 `types/settings.ts`（`ModelConfigVO` / `UserProfileVO` / `ModelConfigSaveRequest` / `UserProfileUpdateRequest`）。
4. CSS 按职责拆分：`styles/variables.css`（中性色梯度、间距、圆角、阴影等通用变量）、`styles/layout.css`（主布局容器样式），在 `index.css` 中统一 import。
5. SVG 图标通过 Vite `?raw` import，在 `assets/icons/index.ts` 统一导出（search / notebook / dashboard / settings / chat / chevron-down / close），通过 `SvgIcon` 组件使用。`SvgIcon` 新增 `size` prop 支持自定义尺寸。

#### 主布局 AppLayout
6. 新建 `layouts/AppLayout.vue`：左侧固定 220px 侧边栏 + 右侧 flex-1 内容区。
7. 侧边栏结构：LOGO 图片 → 实时时间（日期 + 时钟，setInterval 每 10 秒更新）→ 导航项（单词搜索 / 单词本（可展开收缩子菜单）/ 总览）→ 底部设置按钮。
8. 导航项高亮当前路由，使用 `router-link` 的 `active-class`。
9. 路由默认重定向从 `/dashboard` 改为 `/word-search`，用户进入即看到搜索页面。

#### 设置模块（完整对接后端 API）
10. 新建 `services/settings.ts`：封装 7 个 API 调用函数，对应 `settings-openapi.yml` 文档。
11. 重写 `views/settings/SettingsView.vue`：
    - **模型配置区**：模型卡片列表（displayName / providerName / modelName / apiKeyMasked / enabled / 默认标记），新增/编辑弹窗表单（含校验），删除确认弹窗，一键设为默认。
    - **用户档案区**：昵称输入、Markdown 个人信息文本域、AI 读取权限开关、保存按钮。
    - 所有操作含 loading 状态管理，表单校验，错误/成功提示。

#### AI 对话悬浮气泡
12. 新建 `components/ChatBubble.vue`：固定右下角绿色圆形按钮，点击弹出对话面板（含会话列表 + 对话区 + 输入框骨架），弹出/收起有过渡动画。本阶段为静态 UI。

#### 静态 UI 骨架
13. `WordSearchView.vue`：居中搜索框 + 查询按钮 + 功能提示（英文直查 · 中文反查 · AI 单词卡生成）。
14. `DashboardView.vue`：四列统计卡片 + 最近搜索（空状态）+ 开发状态。
15. `NotebookView.vue`：单词本卡片列表 + 新建按钮（禁用态）。

### 问题与建议
1. ✅ 设置模块前端已完整对接后端 API，但需启动后端才能实际验证 CRUD 操作。建议下一阶段联调测试。
2. 单词本侧边栏下拉目前仅展示"全部单词本"静态链接，后续需对接动态数据（用户实际的单词本列表）。
3. AI 对话气泡目前为静态骨架，后续需接入 SSE/WebFlux 流式对话、会话管理等功能。

---

## 2026.3.31 17:25 - "补充前后端联调启动脚本" - 状态：已完成

### 概述
本阶段在项目根目录新增联调启动脚本，用于一条命令同时拉起后端 Spring Boot 服务与前端 Vite 开发服务，便于本地联调测试。

### 细节
1. 新增 `start-dev.sh`，默认在根目录下分别进入 `backend` 与 `frontend` 执行启动命令。
2. 脚本在启动前检查 `mvn` 与 `npm` 是否存在，避免缺少基础命令时直接静默失败。
3. 脚本注册退出清理逻辑，按 `Ctrl+C` 或任一子进程退出时，会同步结束另一个进程，避免残留后台服务。
4. 启动信息中明确输出本地联调地址：后端 `http://localhost:8080`，前端 `http://localhost:5173`。

### 问题与建议
1. 当前脚本默认依赖本机已安装 Maven；若后续希望降低环境差异，建议补充 Maven Wrapper。

## 2026.3.31 16:20 - "设置模块 OpenAPI 文档补全" - 状态：已完成

### 概述
本阶段基于设置模块控制器与服务实现，补全了 settings 模块 OpenAPI 文档，覆盖模型配置与用户资料相关接口。

### 细节
1. 在 `docs/openapi/settings-openapi.yml` 中补充 7 个接口的 paths 定义。
2. 明确统一响应结构 `ApiResponse`，补充成功响应、空响应与错误响应 schema。
3. 结合服务实现补充接口行为说明，包括默认模型切换、删除默认模型后的回退策略、API Key 仅加密存储并返回脱敏值等规则。
4. 根据 `@Valid` 约束与全局异常处理补充 400/500 响应示例，确保文档与真实返回码保持一致。

### 问题与建议
1. 当前 OpenAPI 仅描述接口契约，若后续前端需要直接联调，建议继续补充公共 `servers`、鉴权方案与统一错误码说明。

## 2026.3.31 16:05 - "切换到标准 Flyway 数据库迁移" - 状态：已完成

### 概述
本阶段完成后端数据库初始化与迁移机制收口，移除之前绕路的自定义迁移实现，统一切换为标准 Flyway 方案，并进一步删除开发期无意义的旧结构兼容迁移。

### 细节
1. 删除 `spring.sql.init + schema.sql` 初始化路径，避免与 Flyway 重复执行。
2. 新增 `db/migration/V1__InitSchema.sql` 作为数据库基线脚本，承接原有建表语句。
3. 删除 `V2__MigrateModelConfigApiKeyEncryption` 兼容迁移，不再为开发阶段的旧表结构做向后兼容。
4. 删除无关的自定义迁移框架与测试，包括空壳 `DatabaseMigrationRunner`、`DatabaseJavaMigration`、`LegacyModelConfigApiKeyEncryptionMigration` 及对应测试。
5. 应用仅保留标准 Flyway 初始化配置，数据库结构以当前基线脚本为准。

### 问题与建议
1. 若你本机还保留 Flyway 收口前生成的旧 sqlite 数据库文件，需要手动删除后再启动；开发阶段建议直接清库，不再补兼容迁移。

## 2026.3.31 14:25 - "设置模块密钥加密与删除接口" - 状态：已完成

### 概述
本阶段完成了设置模块密钥存储方案升级，将 API Key 改为加密落库，并新增模型配置删除接口。

### 细节
1. `schema.sql` 中 `model_config` 表移除 `api_key_masked` 字段，新增 `api_key_encrypted` 字段。
2. 新增 `CryptoUtil` 工具类，使用 AES-256-GCM 进行加解密，密钥由本机 MAC 地址与主机名派生。
3. `SettingsService` 改为保存时加密、读取时解密，并按“前4后4”规则运行时动态脱敏。
4. 新增删除接口：`DELETE /api/settings/models/{id}`。
5. 删除默认模型时，自动回退到最新一条模型配置作为默认，避免默认模型为空。
6. 扩展 `SettingsControllerTest`，覆盖加密落库校验、动态脱敏、删除接口行为。

### 问题与建议
1. 由于密钥派生依赖机器信息，数据库文件迁移到其他机器后可能无法解密；建议后续支持用户自定义主密钥或导入导出流程。

---

## 2026.3.31 10:20 - "设置模块后端API与数据库接入" - 状态：已完成

### 概述
本阶段完成了设置模块后端 API，实现模型配置与用户资料的数据库读写能力，并补充了接口级集成测试。

### 细节
1. 新增 settings 模块完整分层：controller、service、mapper、entity、dto、vo。
2. 实现模型配置接口：查询模型列表、新增模型、更新模型、设置单词生成功能默认模型。
3. 实现用户资料接口：查询资料、更新资料，并在首次读取时自动初始化默认资料记录。
4. 模型配置支持 API Key 脱敏存储（只落库掩码值），避免明文保存。
5. 默认模型切换逻辑采用事务处理，确保任意时刻只有一个 `is_word_generation_default=1`。
6. 新增 `SettingsControllerTest`，覆盖默认模型唯一性与用户资料读写流程。

### 问题与建议
1. 当前 `model_config` 表仅有 `api_key_masked` 字段，暂不支持安全加密保存真实 API Key；若后续需要真正可用的模型调用，建议新增加密存储方案（如本地密钥 + 对称加密）。

---

## 2026.3.30 22:33 - "搭建前后端基础框架" - 状态：已完成

### 概述
本阶段主要搭建了前后端的基础框架，其中前端已完成`npm i`包下载

### 细节
无

### 问题与建议
无

---
