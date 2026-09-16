# API Specification

## Phase F - Points Extension

| Capability | Method | Path | Auth | Params | Response |
| --- | --- | --- | --- | --- | --- |
| Points overview | GET | `/lg/coin/userinfo/json` | Yes | None | `ApiResponse<CoinUserInfoDto>` |
| Points history | GET | `/lg/coin/list/{page}/json` | Yes | `page` starts from `1` | `ApiResponse<PageDto<CoinRecordDto>>` |

- This phase ships the points module as the first closed-loop login-state extension.
- Paging starts from `1` and should be normalized in the repository layer.
- Login-expired handling stays aligned with the existing `-1001` repository contract.

## 1. 文档定位

`WANANDROID_API_CHECKLIST.md` 是全量接口清单。
本文档是当前项目的实现级接口规范，回答以下问题：

- 当前项目优先接哪些接口
- 客户端如何统一建模
- 分页、鉴权、错误码如何处理
- 新增接口时应遵循什么格式

如果清单与本文档冲突，以本文档的“当前项目约束”部分为准；如果本文档未覆盖，则回到清单核对原始信息。

## 2. 基础信息

- Base URL：`https://www.wanandroid.com`
- Content Type：默认表单或查询参数，按接口原始定义处理
- 统一响应外层：

```json
{
  "data": "...",
  "errorCode": 0,
  "errorMsg": ""
}
```
## 3. 客户端统一约定

### 3.1 通用响应模型

建议客户端统一建模：

```kotlin
data class ApiResponse<T>(
    val data: T?,
    val errorCode: Int,
    val errorMsg: String
)
```

约定：

- `errorCode == 0` 视为成功
- `errorCode == -1001` 视为未登录或登录失效
- 其余非 `0` 状态统一视为业务失败
- `data` 必须允许为空

### 3.2 分页模型

建议客户端统一分页模型：

```kotlin
data class PageDto<T>(
    val curPage: Int,
    val datas: List<T>,
    val offset: Int,
    val over: Boolean,
    val pageCount: Int,
    val size: Int,
    val total: Int
)
```

注意：

- 实际字段以返回为准
- 个别登录态接口可能存在字段差异，落地时需要单独核对

### 3.3 鉴权

- 登录态依赖服务端 Cookie
- 登录和注册成功后，必须保存会话
- 退出后，必须清理本地登录态缓存
- 所有需要登录的接口在 Repository 层处理 `-1001`

## 4. 当前阶段接口分组

### 4.1 Phase A：公开内容首屏

| 能力 | 方法 | 路径 | 鉴权 | 请求参数 | 返回 |
| --- | --- | --- | --- | --- | --- |
| 首页文章 | GET | `/article/list/{page}/json` | 否 | `page`，从 `0` 开始 | `ApiResponse<PageDto<ArticleDto>>` |
| Banner | GET | `/banner/json` | 否 | 无 | `ApiResponse<List<BannerDto>>` |
| 常用网站 | GET | `/friend/json` | 否 | 无 | `ApiResponse<List<WebsiteDto>>` |
| 搜索热词 | GET | `/hotkey/json` | 否 | 无 | `ApiResponse<List<HotKeyDto>>` |
| 置顶文章 | GET | `/article/top/json` | 否 | 无 | `ApiResponse<List<ArticleDto>>` |

客户端要求：

- 首页文章与置顶文章的拼接规则由 Repository 统一处理
- Banner、热词、常用网站可视为首屏并行请求

### 4.2 Phase B：内容浏览

| 能力 | 方法 | 路径 | 鉴权 | 请求参数 | 返回 |
| --- | --- | --- | --- | --- | --- |
| 体系树 | GET | `/tree/json` | 否 | 无 | `ApiResponse<List<ChapterDto>>` |
| 体系文章 | GET | `/article/list/{page}/json?cid={cid}` | 否 | `page` 从 `0` 开始，`cid` 必填 | `ApiResponse<PageDto<ArticleDto>>` |
| 导航 | GET | `/navi/json` | 否 | 无 | `ApiResponse<List<NavigationDto>>` |
| 项目分类 | GET | `/project/tree/json` | 否 | 无 | `ApiResponse<List<ChapterDto>>` |
| 项目列表 | GET | `/project/list/{page}/json?cid={cid}` | 否 | `page` 从 `1` 开始，`cid` 必填 | `ApiResponse<PageDto<ArticleDto>>` |
| 搜索 | POST | `/article/query/{page}/json` | 否 | `page` 从 `0` 开始，表单 `k` | `ApiResponse<PageDto<ArticleDto>>` |

客户端要求：

- 搜索请求使用表单参数，不自行改为 JSON Body
- 项目分页起始页与首页不同，必须在 Repository 层屏蔽差异

### 4.3 Phase C：登录态

| 能力 | 方法 | 路径 | 鉴权 | 请求参数 | 返回 |
| --- | --- | --- | --- | --- | --- |
| 登录 | POST | `/user/login` | 否 | `username`, `password` | `ApiResponse<UserDto>` |
| 注册 | POST | `/user/register` | 否 | `username`, `password`, `repassword` | `ApiResponse<UserDto>` |
| 退出 | GET | `/user/logout/json` | 是 | 无 | `ApiResponse<Any>` |
| 用户信息 | GET | `/user/lg/userinfo/json` | 是 | 无 | `ApiResponse<UserInfoDto>` |

客户端要求：

- 登录和注册后都要更新会话状态
- 退出后要清理用户态页面缓存

### 4.4 Phase D：收藏与个人能力

| 能力 | 方法 | 路径 | 鉴权 | 请求参数 | 返回 |
| --- | --- | --- | --- | --- | --- |
| 收藏文章 | POST | `/lg/collect/{id}/json` | 是 | `id` | `ApiResponse<Any>` |
| 取消收藏 | POST | `/lg/uncollect_originId/{id}/json` | 是 | `id` | `ApiResponse<Any>` |
| 收藏列表 | GET | `/lg/collect/list/{page}/json` | 是 | `page` 从 `0` 开始 | `ApiResponse<PageDto<ArticleDto>>` |

说明：

- 登录态列表字段需以后续实际采样为准
- 在字段未完全确认前，优先保证通路、错误处理和最小展示

## 5. DTO 命名规范

- 接口原始对象统一以 `Dto` 结尾
- 分页对象统一用 `PageDto<T>`
- 统一响应外层统一用 `ApiResponse<T>`

示例：

- `ArticleDto`
- `BannerDto`
- `HotKeyDto`
- `ChapterDto`
- `UserDto`

## 6. 新增接口模板

新增接口时按以下格式补充：

```md
### 功能名

| 字段 | 内容 |
| --- | --- |
| 能力 | 例如：问答列表 |
| 方法 | GET |
| 路径 | /wenda/list/{page}/json |
| 鉴权 | 否 |
| 参数 | page 从 1 开始 |
| 返回 | ApiResponse<PageDto<ArticleDto>> |
| 备注 | 页码从 1 开始，与首页不同 |
```

## 7. 接入原则

- 先接 `API_SPEC.md` 已列的 Phase A 到 Phase D
- 其余接口在进入任务队列后再补进本文档
- 未经文档确认，不直接在代码里临时新增接口
## Phase E - Extra Content

| 能力 | 方法 | 路径 | 鉴权 | 请求参数 | 返回 |
| --- | --- | --- | --- | --- | --- |
| 问答列表 | GET | `/wenda/list/{page}/json` | 否 | `page` 从 `1` 开始 | `ApiResponse<PageDto<ArticleDto>>` |
| 广场列表 | GET | `/user_article/list/{page}/json` | 否 | `page` 从 `0` 开始 | `ApiResponse<PageDto<ArticleDto>>` |
| 公众号分类 | GET | `/wxarticle/chapters/json` | 否 | 无 | `ApiResponse<List<ChapterDto>>` |
| 公众号文章 | GET | `/wxarticle/list/{id}/{page}/json` | 否 | `id`, `page` 从 `1` 开始 | `ApiResponse<PageDto<ArticleDto>>` |
| 最新项目 | GET | `/article/listproject/{page}/json` | 否 | `page` 从 `0` 开始 | `ApiResponse<PageDto<ArticleDto>>` |

- 问答和公众号分页从 `1` 开始，广场和最新项目从 `0` 开始，页码差异由 Repository 层统一处理。
- 公众号模块本阶段实现“分类 + 文章列表”闭环，历史搜索留待后续任务。
- 扩展内容模块复用现有文章模型、收藏动作和 Web 容器跳转。
## Phase L - 摸鱼直播

直播源不是 WanAndroid API，当前通过用户提供或内置的 M3U URL 获取。

| 能力 | 方法 | 地址 | 鉴权 | 返回 |
| --- | --- | --- | --- | --- |
| 默认直播源 | GET | `https://iptv-org.github.io/iptv/countries/cn.m3u` | 否 | M3U 文本 |
| 自定义直播源 | GET | 用户输入的 HTTP(S) M3U 地址 | 由源决定 | M3U 文本 |

- Repository 负责网络错误、HTTP 错误、空列表和常见 M3U 解析。
- 当前最多展示 100 个频道。
- 播放由 Android `VideoView` 负责，第三方直播地址可用性不由应用保证。
