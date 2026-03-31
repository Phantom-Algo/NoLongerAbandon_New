# 开发日志

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
1. 设置模块前端已完整对接后端 API，但需启动后端才能实际验证 CRUD 操作。建议下一阶段联调测试。
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
