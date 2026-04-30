# Task Queue

## 1. 说明

本文件记录已确认进入项目范围的任务队列。
排序原则：

- `P0`：当前必须做
- `P1`：首版重要能力
- `P2`：可顺延能力

状态以 `TASK_STATE.md` 为准，本文件重点记录任务定义和依赖。

## 2. 当前任务队列

| Task ID | 标题 | 优先级 | 当前状态 | 依赖 | 任务文件 | 说明 |
| --- | --- | --- | --- | --- | --- | --- |
| `task_001` | 首页公开数据接入 | P0 | done | 无 | `agent/tasks/task_001_home_list.md` | 首页文章、置顶、Banner、热词、常用网站 |
| `task_002` | 体系与导航浏览能力 | P0 | done | `task_001` | `agent/tasks/task_002_system_navigation.md` | 体系树、体系文章、导航 |
| `task_003` | 项目与搜索能力 | P0 | done | `task_001` | `agent/tasks/task_003_project_search.md` | 项目分类、项目列表、搜索 |
| `task_004` | 登录与会话管理 | P1 | done | `task_001` | `agent/tasks/task_004_auth_session.md` | 登录、注册、退出、Cookie 持久化 |
| `task_005` | 收藏与用户信息 | P1 | done | `task_004` | `agent/tasks/task_005_collect_profile.md` | 收藏、取消收藏、收藏列表、用户信息 |
| `task_006` | Web 内容承载页 | P1 | done | `task_001` | `agent/tasks/task_006_web_container.md` | 文章打开、基础 WebView 容器 |
| `task_007` | 扩展内容模块 | P2 | done | `task_002`, `task_003` | `agent/tasks/task_007_extra_content.md` | 问答、广场、公众号、最新项目 |
| `task_008` | 登录态扩展模块 | P2 | done | `task_004` | `agent/tasks/task_008_account_extensions.md` | 积分、消息、TODO |

## 3. 拆解原则

- 首页相关能力优先，用于验证网络层、分页和列表状态
- 登录态能力单独拆分，避免与公开内容耦合过深
- 扩展接口只在前置基础能力稳定后接入

## 4. 新增任务要求

新增任务时必须同步以下信息：

- 任务编号
- 标题
- 优先级
- 前置依赖
- 一句话目标
- 对应任务文件
