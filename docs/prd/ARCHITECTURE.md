# NoLongerAbandon 架构设计文档

## 1. 文档目标

本文档用于约束 NoLongerAbandon 项目的前后端基础架构，确保后续功能开发遵循统一的模块边界、目录规范、接口约定和演进路线。

当前阶段目标不是一次性完成全部业务，而是先把以下底座固定下来：

1. 前后端工程可独立启动与扩展。
2. 后端严格遵循 controller-service-mapper 分层。
3. 前端严格遵循 Vue3 领域拆分与页面布局规范。
4. 数据库初始化、AI 接入位、统一响应格式、异常处理先行落地。
5. 后续功能开发优先走纵向切片，而不是横向堆文件。

## 2. 总体架构

项目采用前后端分离 + RESTful API 模式。

### 2.1 前端职责

前端负责：

1. 搜索入口与检索结果承载。
2. 单词卡、单词本、AI 会话、设置模块的交互编排。
3. 上下文选择、模块重生成、流式输出等交互体验。
4. 本地页面状态管理与路由切换。

技术选型：

1. Vue 3
2. Vite
3. TypeScript
4. Element Plus
5. Pinia

### 2.2 后端职责

后端负责：

1. 单词搜索、单词卡缓存、单词本管理、AI 会话、设置管理等 REST API。
2. AI 工作流编排与模型路由。
3. 本地 sqlite 持久化。
4. 数据初始化、统一异常、统一响应结构。

技术选型：

1. Spring Boot 3
2. Spring AI
3. MyBatis Plus
4. sqlite

## 3. 模块拆分

### 3.1 后端领域模块

后端按业务域拆分为以下模块：

1. `system`
用于系统状态、健康检查、基础配置读取。

2. `wordcard`
用于英文搜索、中文候选词、单词卡生成、模块级重生成、缓存复用。

3. `notebook`
用于单词本 CRUD、单词加入单词本、列表分页排序。

4. `chat`
用于 AI 会话、消息记录、流式输出、上下文注入。

5. `settings`
用于模型配置、默认模型切换、用户资料与 AI 读取权限设置。

6. `infrastructure`
用于 AI 供应商接入、持久化公共能力、通用适配器。

### 3.2 前端页面模块

前端按页面域拆分为以下模块：

1. `dashboard`
项目总览和开发引导页。

2. `word-search`
英文搜索、中文搜索、单词卡展示入口。

3. `notebook`
单词本列表、单词列表、单词卡跳转。

4. `chat`
会话列表、聊天窗口、上下文引用展示。

5. `settings`
模型配置、默认模型、用户资料设置。

## 4. 后端分层规范

后端必须遵循以下铁律：

1. Controller 只负责接收参数、调用 Service、返回响应。
2. Service 负责业务编排，不允许直接写 SQL。
3. Mapper 只负责数据库访问，不允许承担业务逻辑。
4. DTO 只用于请求入参。
5. VO 只用于响应出参。
6. Entity 只映射持久化对象。

标准目录模板如下：

```text
modules/
  wordcard/
    controller/
    service/
      impl/
    mapper/
    entity/
    dto/
    vo/
```

任何新业务模块都必须沿用这套目录，不允许出现以下情况：

1. 把 SQL 写到 Service 中。
2. 把业务逻辑塞进 Controller 中。
3. 一个模块没有 DTO/VO 边界，直接把 Entity 透出给前端。

## 5. 前端分层规范

前端采用页面域驱动结构，核心目录如下：

```text
src/
  assets/
  components/
  layouts/
  router/
  stores/
  styles/
  types/
  views/
```

约束如下：

1. 页面级内容放在 `views`。
2. 跨页面复用组件放在 `components`。
3. 页面外壳和导航布局放在 `layouts`。
4. 路由集中在 `router` 中统一管理。
5. 全局状态统一收敛到 `stores`。
6. 样式变量和全局样式放在 `styles`。
7. 图标必须使用 svg 资源，不使用字体图标。

## 6. 数据库设计原则

当前已预留以下核心表：

1. `word`
记录单词本体、来源语言、搜索次数。

2. `word_card`
记录单词卡整体内容与生成模型。

3. `word_card_section`
记录单词卡的各个模块，支持预设模块和自定义模块。

4. `vocabulary_book`
记录用户创建的单词本。

5. `vocabulary_book_word`
记录单词与单词本多对多关系。

6. `ai_chat_session`
记录会话。

7. `ai_chat_message`
记录会话消息与上下文引用。

8. `model_config`
记录模型配置与默认模型。

9. `user_profile`
记录用户资料和是否允许 AI 读取资料。

### 6.1 初始化策略

考虑到这是本地应用，数据库初始化采用：

1. 应用启动时自动执行 `schema.sql`。
2. 表结构全部使用 `CREATE TABLE IF NOT EXISTS`。
3. 首次启动自动建表，后续启动自动跳过已存在表。

这种策略适合当前阶段的本地 sqlite 场景，简单直接，便于快速迭代。后续若迁移到更复杂的数据演进模式，再引入独立迁移方案。

## 7. AI 架构设计

### 7.1 AI 功能分层

AI 能力必须拆分为两条线：

1. 单词卡生成能力
这是结构化生成流程，必须严格遵循预设模块，不等同于普通聊天。

2. AI Chat 能力
这是通用对话流程，支持流式输出、模型选择、会话管理。

这两个能力禁止混用实现，否则后续会导致提示词、上下文和数据结构全部耦合。

当前基础工程阶段，为了保证项目在未配置 API Key 时也能正常启动，默认关闭了 OpenAI 自动装配。后续完成设置模块与模型配置落库后，再由受控配置显式启用 AI 能力。

### 7.2 单词卡生成工作流建议

建议采用 Plan-Execute 模式：

1. Plan 节点解析用户需求、确定需要输出的模块列表。
2. Execute 节点按模块并行或串行生成内容。
3. Review 节点检查输出是否完整覆盖预设模块。
4. Persist 节点将结果写入 `word_card` 与 `word_card_section`。

### 7.3 AI Chat 流程建议

AI Chat 流程建议拆为：

1. 会话加载
2. 模型选择
3. 上下文注入
4. 流式输出
5. 消息持久化

## 8. API 约定

所有接口统一返回结构：

```json
{
  "success": true,
  "code": "OK",
  "message": "success",
  "data": {}
}
```

错误响应同样遵循统一结构，便于前端统一拦截与提示。

当前已落地的基础接口：

1. `GET /api/system/status`
用于查看系统基础状态。

2. `POST /api/word-cards/preview`
用于演示标准业务分层，后续可自然演进为真实单词卡查询接口。

## 9. 当前工程骨架

### 9.1 后端

```text
backend/
  pom.xml
  src/main/java/com/nolongerabandon/backend/
    common/
    infrastructure/
    modules/
      chat/
      notebook/
      settings/
      system/
      wordcard/
  src/main/resources/
    application.yml
    schema.sql
```

### 9.2 前端

```text
frontend/
  package.json
  vite.config.ts
  tsconfig.json
  src/
    assets/icons/
    components/
    layouts/
    router/
    stores/
    styles/
    types/
    views/
      dashboard/
      word-search/
      notebook/
      chat/
      settings/
```

## 10. 后续开发顺序建议

不建议多个模块同时平推，建议严格按纵向链路推进：

1. 第一阶段
完成单词搜索主链路：搜索输入 -> 本地缓存查询 -> AI 生成 -> 单词卡入库 -> 单词卡展示。

2. 第二阶段
完成单词本主链路：单词本 CRUD -> 单词加入单词本 -> 分页与排序。

3. 第三阶段
完成 AI Chat 主链路：会话创建 -> 模型选择 -> 流式输出 -> 消息持久化。

4. 第四阶段
完成设置模块：模型配置、默认模型、用户资料注入开关。

## 11. 代码约束总结

1. 后端必须保持 controller-service-mapper 清晰边界。
2. Mapper 不得侵入 Service。
3. 前端必须使用 Element Plus，图标必须使用 svg。
4. 页面和业务模块必须按领域拆分，不允许出现单文件堆砌。
5. AI 单词卡生成与 AI Chat 必须保持两套独立实现。
6. 数据结构优先服务后续扩展，而不是只满足当前页面。

这份文档是当前阶段的实现基线。后续每新增一个完整业务链路，都应该同步更新此文档，确保架构和代码持续一致。