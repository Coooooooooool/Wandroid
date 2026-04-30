# WanAndroid Android Client - Project Spec

## 1. 文档目的

本文档定义项目目标、首版范围、技术约束和交付边界。
它回答三个问题：

1. 这个项目第一阶段要做什么。
2. 这个项目暂时不做什么。
3. Agent 和开发者应基于什么原则推进实现。

相关文档：

- `agent/docs/MODULES.md`：模块职责与依赖规则
- `agent/docs/API_SPEC.md`：当前项目实际接入的接口规范
- `agent/docs/AI_RULES.md`：Agent 行为约束
- `agent/docs/WORKFLOW.md`：任务拆解与执行流程
- `agent/tasks/TASK_QUEUE.md`：任务队列
- `agent/tasks/TASK_STATE.md`：任务状态板
- `WANANDROID_API_CHECKLIST.md`：WanAndroid 全量接口清单

## 2. 项目目标

基于 WanAndroid 开放 API 开发一个 Android 客户端，逐步实现以下能力：

- 浏览公开内容：首页、Banner、体系、导航、项目、搜索
- 账户能力：登录、退出、会话维持
- 用户能力：收藏、个人信息、积分、消息、TODO
- 工程能力：清晰架构、可追踪任务、可控 Agent 开发流程

## 3. 当前阶段定位

当前仓库仍处于项目初始化阶段，业务代码几乎未开始实现。
因此当前阶段的主要目标不是“快速堆页面”，而是先建立稳定的开发约束：

- 明确目录和模块边界
- 明确 API 接入标准
- 明确 Agent 的可执行边界
- 明确任务拆解与状态流转方式

## 4. 首版范围

### 4.1 Must Have

- 首页文章列表
- 首页 Banner
- 置顶文章
- 常用网站
- 搜索热词
- 体系
- 导航
- 项目分类与项目列表
- 搜索结果列表
- 登录、退出、会话保持

### 4.2 Should Have

- 收藏文章
- 收藏列表
- 用户信息
- WebView 打开文章
- 列表分页、刷新、加载更多

### 4.3 Nice to Have

- 问答
- 广场
- 公众号
- 积分
- 消息
- TODO
- 工具和 Google Maven 查询

## 5. 非目标

首轮开发阶段暂不追求以下内容：

- 离线缓存完整方案
- 多模块 Gradle 重构
- 多端共享代码
- 复杂动画和视觉打磨
- 后台同步和推送系统
- 一次性覆盖全部 WanAndroid 接口

## 6. 技术基线

基于当前仓库现状，先采用以下基线：

- 语言：Kotlin
- 平台：Android，`minSdk = 24`
- UI：当前以 Android View/XML 为基线，不预设 Compose 必选
- 架构：MVVM
- 并发：Kotlin Coroutines + Flow
- 网络：Retrofit + OkHttp
- JSON：待引入统一序列化库后确定
- 状态：`StateFlow`

说明：

- 文档中会描述目标架构，但实现必须以当前仓库实际依赖和阶段为准。
- 如果未来切换到 Compose，需要单独更新本文档和 `MODULES.md`。

## 7. 关键约束

- UI 不直接请求接口
- ViewModel 不直接依赖 Retrofit
- Repository 负责业务编排和错误归一
- API 接入先遵循 `API_SPEC.md`，再落实现
- 每个任务必须有验收标准
- Agent 不得跨任务同时修改多个不相关模块

## 8. 里程碑

### M1：工程骨架

- 目录结构稳定
- 基础依赖接入
- 网络层和通用结果模型建立

### M2：公开内容浏览

- 首页、体系、导航、项目、搜索打通

### M3：登录态能力

- 登录、退出、会话、收藏、用户信息

### M4：扩展能力

- 积分、消息、TODO、问答、公众号等模块按优先级增量接入

## 9. 完成定义

一个功能只有满足以下条件才算完成：

- 需求范围在任务文档中明确
- 实现符合模块依赖规则
- 接口接入符合 `API_SPEC.md`
- UI 状态、错误态、空态有明确定义
- 必要文档已同步更新
- 任务状态从 `doing` 或 `review` 正式流转到 `done`
