# Modules And Architecture

## 1. 目标

本文档定义目录结构、模块职责、依赖方向和禁止事项。
它是工程实现时的结构性约束文件。

## 2. 推荐目录

当前业务代码尚未建立，建议从以下结构开始：

```text
app/src/main/java/com/eric/wandroid/
  common/
    result/
    error/
    dispatchers/
    utils/
  data/
    remote/
      api/
      dto/
      datasource/
    repository/
    mapper/
  domain/
    model/
    repository/
    usecase/
  ui/
    home/
    system/
    navigation/
    project/
    search/
    login/
    collect/
    profile/
    web/
    components/
```

说明：

- `domain` 不是必须大而全，但推荐至少保留 `model` 和 `usecase` 的演进空间。
- 如果当前阶段想更轻量，可以先保留 `data / ui / common` 三层，但依赖方向不能乱。

## 3. 模块职责

### `common`

负责纯通用能力：

- `Result`、错误定义、通用扩展
- 调度器封装
- 时间、分页、字符串等工具

### `data/remote`

负责远程数据获取：

- Retrofit 接口定义
- DTO 定义
- 远程数据源封装

### `data/repository`

负责数据编排：

- 组织多个数据源
- 处理错误映射
- 将 DTO 转换为领域模型
- 对 UI 暴露稳定接口

### `domain`

负责业务抽象：

- 领域模型
- 仓储接口
- 用例封装

### `ui`

负责界面与状态消费：

- 页面
- ViewModel
- UI State
- 组件

## 4. 依赖方向

允许的依赖方向：

```text
ui -> domain -> data
ui -> common
data -> common
domain -> common
```

不允许的依赖方向：

- `ui -> data/remote/api`
- `ui -> dto`
- `ViewModel -> Retrofit`
- `ui` 之间跨功能直接互调 Repository
- 任意模块绕过 Repository 直连网络层

## 5. 页面拆分原则

每个功能页面至少包含以下元素：

- `Contract` 或等价的 `UiState`
- `ViewModel`
- 页面入口，如 `Activity`、`Fragment` 或 `Screen`
- 如需列表分页，应单独定义分页状态

建议每个功能目录遵循以下组织：

```text
ui/home/
  HomeUiState.kt
  HomeViewModel.kt
  HomeActivity.kt
  HomeAdapter.kt
```

## 6. 数据模型规则

模型分层必须明确：

- DTO：严格贴近接口返回
- Domain Model：面向业务和 UI
- UI Model：仅在页面特别复杂时引入

禁止：

- 直接把 DTO 暴露给 UI
- 为了省事在多个页面重复解析接口字段
- 在 Adapter 或 Activity 中做 DTO 到 UI 的转换逻辑

## 7. 分页规则

WanAndroid 的分页起始页不统一，必须在 Repository 层收口。

- 首页、搜索、体系文章等多数列表：页码从 `0` 开始
- 项目、问答、公众号、积分等部分列表：页码从 `1` 开始

规则：

- UI 层不关心真实起始页
- Repository 或 UseCase 负责处理页码差异
- 每个分页接口都要在实现注释或文档中标注起始页规则

## 8. 错误处理规则

错误统一在 Repository 层映射为稳定结果：

- 网络错误
- 服务端业务错误
- 未登录错误
- 空数据
- 参数错误

UI 只消费可渲染状态，不直接处理原始异常类型。

## 9. Session 规则

登录态依赖 Cookie。

- Cookie 持久化和读取必须由统一网络层管理
- UI 层不得直接拼接 Cookie
- 登录、退出后必须触发会话状态刷新

## 10. 文档同步规则

以下变化必须同步更新对应文档：

- 新增功能模块：更新 `TASK_QUEUE.md`
- 新增接口：更新 `API_SPEC.md`
- 变更模块边界：更新本文件
- 变更 Agent 执行原则：更新 `AI_RULES.md`
