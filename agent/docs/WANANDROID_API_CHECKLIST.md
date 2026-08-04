# WanAndroid API 清单

整理时间：2026-04-21  
工作目录：`D:\Alex\Project\Wandroid`

## 1. 来源与整理范围

本清单按官网目录整理，来源包括：

- 主文档：`https://www.wanandroid.com/api`
- TODO v2 子文档：`https://www.wanandroid.com/blog/show/2442`
- Google Maven 子文档：`https://www.wanandroid.com/blog/show/2751`

说明：

- `文档给出`：官网正文直接写明了接口或返回结构。
- `示例推断`：官网正文未写清楚，我通过示例地址抓取真实返回做了结构补齐。
- `需登录，未采样`：接口需要 Cookie，且文档未给出完整返回结构；此类接口先保留为“清单级描述”，后续接入登录后再二次核对字段。

## 2. 全局约定

- Base URL：`https://www.wanandroid.com`
- 统一包裹结构：

```json
{
  "data": "...",
  "errorCode": 0,
  "errorMsg": ""
}
```

- `errorCode = 0` 表示成功。
- `errorCode = -1001` 表示未登录或登录状态失效。
- 许多字段可能为 `null`，客户端模型要允许空值。
- 分页页码不统一：
  - 文章列表、广场列表等很多接口从 `0` 开始。
  - 项目列表、公众号列表、问答列表、积分排行等不少接口从 `1` 开始。
- 需要登录的接口依赖 Cookie，会话通常由登录/注册接口下发。

## 3. 常用返回模型

| 模型 | 结构摘要 | 来源 |
| --- | --- | --- |
| `CommonResponse<T>` | `{ data, errorCode, errorMsg }` | 文档给出 |
| `Page<T>` | 常见字段：`curPage, datas, offset, over, pageCount, size, total` | 文档给出 + 示例推断 |
| `Article` | 常见字段：`id, title, link, author, shareUser, chapterId, chapterName, superChapterId, superChapterName, publishTime, niceDate, collect, tags, envelopePic, desc` | 示例推断 |
| `Banner` | `id, title, desc, imagePath, url, type, order, isVisible` | 示例推断 |
| `Website` | `id, name, link, category, order, visible, icon` | 示例推断 |
| `HotKey` | `id, name, link, order, visible` | 示例推断 |
| `Chapter` | `id, name, parentChapterId, children, author, cover, desc, visible, order` | 示例推断 |
| `NavigationItem` | `{ name, articles: Article[] }` | 示例推断 |
| `CoinInfo` | `coinCount, level, rank, userId, username, nickname` | 示例推断 |
| `WendaComment` | `id, articleId, userId, userName, content, contentMd, publishDate, niceDate, zan, replyComments` | 示例推断 |
| `Tool` | `id, name, desc, icon, link, tabName, showInTab, isNew, order, visible` | 示例推断 |
| `PopularColumn` | `id, name, columnId, subChapterId, url, userId` | 示例推断 |
| `ShareUserArticles` | `{ coinInfo: CoinInfo, shareArticles: Page<Article> }` | 示例推断 |

## 4. API 目录清单

### 4.1 2024-05-26 新增接口

| 接口 | 方法 | 鉴权 | 参数 | 返回摘要 | 来源 |
| --- | --- | --- | --- | --- | --- |
| `/harmony/index/json` | GET | 否 | 无 | 鸿蒙首页聚合对象，至少包含 `links` 区块；区块内是文章/链接集合 | 示例推断 |
| `/popular/wenda/json` | GET | 否 | 无 | `Article[]`，热门问答 | 示例推断 |
| `/popular/column/json` | GET | 否 | 无 | `PopularColumn[]`，热门专栏标签 | 示例推断 |
| `/popular/route/json` | GET | 否 | 无 | `Chapter[]`，热门学习路径 | 示例推断 |

### 4.2 首页

| 目录项 | 接口 | 方法 | 鉴权 | 参数 | 返回摘要 | 来源 |
| --- | --- | --- | --- | --- | --- | --- |
| 1.1 首页文章列表 | `/article/list/{page}/json` | GET | 否 | `page` 从 `0` 开始 | `Page<Article>` | 示例推断 |
| 1.2 首页 Banner | `/banner/json` | GET | 否 | 无 | `Banner[]` | 示例推断 |
| 1.3 常用网站 | `/friend/json` | GET | 否 | 无 | `Website[]` | 示例推断 |
| 1.4 搜索热词 | `/hotkey/json` | GET | 否 | 无 | `HotKey[]` | 示例推断 |
| 1.5 置顶文章 | `/article/top/json` | GET | 否 | 无 | `Article[]` | 示例推断 |

### 4.3 体系

| 目录项 | 接口 | 方法 | 鉴权 | 参数 | 返回摘要 | 来源 |
| --- | --- | --- | --- | --- | --- | --- |
| 2.1 体系数据 | `/tree/json` | GET | 否 | 无 | `Chapter[]`，每个一级分类带 `children` | 示例推断 |
| 2.2 知识体系下文章 | `/article/list/{page}/json?cid={cid}` | GET | 否 | `page` 从 `0` 开始，`cid` 为二级分类 id | `Page<Article>` | 文档给出 + 示例推断 |
| 2.3 按作者名称搜索文章 | `/article/list/{page}/json?author={author}` | GET | 否 | `page` 从 `0` 开始，`author` 为作者名 | `Page<Article>` | 文档给出 |

### 4.4 导航

| 目录项 | 接口 | 方法 | 鉴权 | 参数 | 返回摘要 | 来源 |
| --- | --- | --- | --- | --- | --- | --- |
| 3.1 导航数据 | `/navi/json` | GET | 否 | 无 | `NavigationItem[]` | 示例推断 |

### 4.5 项目

| 目录项 | 接口 | 方法 | 鉴权 | 参数 | 返回摘要 | 来源 |
| --- | --- | --- | --- | --- | --- | --- |
| 4.1 项目分类 | `/project/tree/json` | GET | 否 | 无 | `Chapter[]` | 示例推断 |
| 4.2 项目列表 | `/project/list/{page}/json?cid={cid}` | GET | 否 | `page` 从 `1` 开始，`cid` 为分类 id | `Page<Article>` | 文档给出 + 示例推断 |

### 4.6 登录与注册

| 目录项 | 接口 | 方法 | 鉴权 | 参数 | 返回摘要 | 来源 |
| --- | --- | --- | --- | --- | --- | --- |
| 5.1 登录 | `/user/login` | POST | 否 | `username, password` | 用户信息对象，且服务端写入登录 Cookie | 文档给出 |
| 5.2 注册 | `/user/register` | POST | 否 | `username, password, repassword` | 用户信息对象，且服务端写入登录 Cookie | 文档给出 |
| 5.3 退出 | `/user/logout/json` | GET | 是 | 无 | 常规成功包裹，`data` 通常可忽略 | 文档给出 |

### 4.7 收藏

| 目录项 | 接口 | 方法 | 鉴权 | 参数 | 返回摘要 | 来源 |
| --- | --- | --- | --- | --- | --- | --- |
| 6.1 收藏文章列表 | `/lg/collect/list/{page}/json` | GET | 是 | `page` 从 `0` 开始 | 预期为 `Page<Article>`；需登录，未采样 | 文档给出 |
| 6.2 收藏站内文章 | `/lg/collect/{id}/json` | POST | 是 | `id` 为文章 id | 常规成功包裹 | 文档给出 |
| 6.3 收藏站外文章 | `/lg/collect/addtool/json` | POST | 是 | `name, link` | 常规成功包裹 | 文档给出 |
| 6.4 编辑收藏网站 | `/lg/collect/updatetool/json` | POST | 是 | `id, name, link` | 常规成功包裹 | 文档给出 |
| 6.5 删除收藏网站 | `/lg/collect/deletetool/json` | POST | 是 | `id` | 常规成功包裹 | 文档给出 |
| 6.6 取消收藏文章列表里的文章 | `/lg/uncollect/{id}/json` | POST | 是 | `id, originId=-1` | 常规成功包裹 | 文档给出 |
| 6.7 取消收藏站内文章 | `/lg/uncollect_originId/{id}/json` | POST | 是 | `id` 为原始文章 id | 常规成功包裹 | 文档给出 |
| 6.8 收藏网站列表 | `/lg/collect/usertools/json` | GET | 是 | 无 | 预期为 `Website[]`；需登录，未采样 | 文档给出 |

### 4.8 搜索

| 目录项 | 接口 | 方法 | 鉴权 | 参数 | 返回摘要 | 来源 |
| --- | --- | --- | --- | --- | --- | --- |
| 7.1 搜索 | `/article/query/{page}/json` | POST | 否 | `page` 从 `0` 开始；提交 `k=关键词` | `Page<Article>` | 文档给出 |

### 4.9 TODO 工具 v2

来自单独文档：`https://www.wanandroid.com/blog/show/2442`

| 目录项 | 接口 | 方法 | 鉴权 | 参数 | 返回摘要 | 来源 |
| --- | --- | --- | --- | --- | --- | --- |
| 8.1 新增 TODO | `/lg/todo/add/json` | POST | 是 | `title, content, date, type, priority` | 常规成功包裹，预期返回新建 TODO 或成功状态 | 文档给出 |
| 8.2 更新 TODO 内容 | `/lg/todo/update/{id}/json` | POST | 是 | `id, title, content, date, type, priority` | 常规成功包裹 | 文档给出 |
| 8.3 删除 TODO | `/lg/todo/delete/{id}/json` | POST | 是 | `id` | 常规成功包裹 | 文档给出 |
| 8.4 完成状态切换 | `/lg/todo/done/{id}/json` | POST | 是 | `id, status`；`1` 完成，`0` 未完成 | 常规成功包裹 | 文档给出 |
| 8.5 TODO 列表 | `/lg/todo/v2/list/{page}/json` | GET | 是 | `page` 从 `1` 开始；支持 `status, type, priority, orderby` | 预期为分页 TODO 列表；需登录，示例接口未成功采样 | 文档给出 |

补充说明：

- 文档明确说明 `date` 需要传 `yyyy-MM-dd`。
- `type` 对应工作分类：`1` 仅工作，`2` 仅生活，`3` 工作生活均有，`4` 全部。
- `priority` 对应优先级：`1` 重要，`2` 普通。
- `orderby` 可用值：`1` 完成日期顺序，`2` 完成日期逆序，`3` 创建日期顺序，`4` 创建日期逆序。

### 4.10 积分

| 目录项 | 接口 | 方法 | 鉴权 | 参数 | 返回摘要 | 来源 |
| --- | --- | --- | --- | --- | --- | --- |
| 9.1 积分排行榜 | `/coin/rank/{page}/json` | GET | 否 | `page` 从 `1` 开始 | `Page<CoinInfo>` | 示例推断 |
| 9.2 个人积分 | `/lg/coin/userinfo/json` | GET | 是 | 无 | `CoinInfo` 或带用户信息的积分对象；需登录，未采样 | 文档给出 |
| 9.3 积分获取列表 | `/lg/coin/list/{page}/json` | GET | 是 | `page` 从 `1` 开始 | 预期为 `Page<CoinRecord>`；未登录时返回 `-1001` | 文档给出 |

### 4.11 广场

| 目录项 | 接口 | 方法 | 鉴权 | 参数 | 返回摘要 | 来源 |
| --- | --- | --- | --- | --- | --- | --- |
| 10.1 广场列表 | `/user_article/list/{page}/json` | GET | 否 | `page` 从 `0` 开始 | `Page<Article>` | 示例推断 |
| 10.2 分享人文章列表 | `/user/{id}/share_articles/{page}/json` | GET | 否 | `id` 为用户 id，`page` 从 `1` 开始 | `ShareUserArticles` | 示例推断 |
| 10.3 自己分享的文章列表 | `/user/lg/private_articles/{page}/json` | GET | 是 | `page` 从 `1` 开始 | 预期为分页文章列表；需登录，未采样 | 文档给出 |
| 10.4 删除自己分享的文章 | `/lg/user_article/delete/{id}/json` | POST | 是 | `id` 为文章 id | 常规成功包裹 | 文档给出 |
| 10.5 分享文章 | `/lg/user_article/add/json` | POST | 是 | `title, link` | 常规成功包裹 | 文档给出 |

### 4.12 问答

| 目录项 | 接口 | 方法 | 鉴权 | 参数 | 返回摘要 | 来源 |
| --- | --- | --- | --- | --- | --- | --- |
| 11.1 问答列表 | `/wenda/list/{page}/json` | GET | 否 | `page` 从 `1` 开始 | `Page<Article>` | 示例推断 |

### 4.13 用户信息

| 目录项 | 接口 | 方法 | 鉴权 | 参数 | 返回摘要 | 来源 |
| --- | --- | --- | --- | --- | --- | --- |
| 12.1 用户信息 | `/user/lg/userinfo/json` | GET | 是 | 无 | 用户信息对象，至少与登录态相关；未登录时返回 `-1001` | 文档给出 |

### 4.14 问答评论

| 目录项 | 接口 | 方法 | 鉴权 | 参数 | 返回摘要 | 来源 |
| --- | --- | --- | --- | --- | --- | --- |
| 13.1 评论列表 | `/wenda/comments/{id}/json` | GET | 否 | `id` 为问答文章 id | `Page<WendaComment>` | 示例推断 |

### 4.15 消息

| 目录项 | 接口 | 方法 | 鉴权 | 参数 | 返回摘要 | 来源 |
| --- | --- | --- | --- | --- | --- | --- |
| 14.1 未读消息数量 | `/message/lg/count_unread/json` | GET | 是 | 无 | 预期为整数计数；未登录时返回 `-1001` | 文档给出 |
| 14.2 已读消息列表 | `/message/lg/readed_list/{page}/json` | GET | 是 | `page` 从 `1` 开始 | 预期为分页消息列表；未登录时返回 `-1001` | 文档给出 |
| 14.3 未读消息列表 | `/message/lg/unread_list/{page}/json` | GET | 是 | `page` 从 `1` 开始 | 预期为分页消息列表；未登录时返回 `-1001` | 文档给出 |

### 4.16 Google Maven 仓库查询

来自单独文档：`https://www.wanandroid.com/blog/show/2751`

| 目录项 | 接口 | 方法 | 鉴权 | 参数 | 返回摘要 | 来源 |
| --- | --- | --- | --- | --- | --- | --- |
| 15.1 通过 groupId / artifactId 查询 | `/maven_pom/package/json?package={groupId}:{artifactId}` | GET | 否 | `package` 例如 `com.squareup.okhttp3:okhttp` | 返回匹配结果集合；我抓到的示例为字符串数组 | 文档给出 + 示例推断 |
| 15.2 模糊搜索 | `/maven_pom/search/json?k={keyword}` | GET | 否 | `k` 关键字 | 返回搜索结果数组；以 `okhttp`、`androidx.hilt:hilt` 测试时为空数组 | 文档给出 + 示例推断 |

说明：

- 这组接口官网本身描述较少，建议后续真正接入时再针对目标查询词补采样一次。

### 4.17 公众号

| 目录项 | 接口 | 方法 | 鉴权 | 参数 | 返回摘要 | 来源 |
| --- | --- | --- | --- | --- | --- | --- |
| 16.1 公众号列表 | `/wxarticle/chapters/json` | GET | 否 | 无 | `Chapter[]` | 示例推断 |
| 16.2 查看某个公众号文章列表 | `/wxarticle/list/{id}/{page}/json` | GET | 否 | `id` 为公众号 id，`page` 从 `1` 开始 | `Page<Article>` | 示例推断 |
| 16.3 在某个公众号中搜索历史文章 | `/wxarticle/list/{id}/{page}/json?k={keyword}` | GET | 否 | `id, page, keyword` | `Page<Article>` | 文档给出 |

### 4.18 最新项目

| 目录项 | 接口 | 方法 | 鉴权 | 参数 | 返回摘要 | 来源 |
| --- | --- | --- | --- | --- | --- | --- |
| 17.1 最新项目列表 | `/article/listproject/{page}/json` | GET | 否 | `page` 从 `0` 开始 | `Page<Article>` | 示例推断 |

### 4.19 工具

| 目录项 | 接口 | 方法 | 鉴权 | 参数 | 返回摘要 | 来源 |
| --- | --- | --- | --- | --- | --- | --- |
| 18.1 工具列表 | `/tools/list/json` | GET | 否 | 无 | `Tool[]` | 示例推断 |

### 4.20 教程

| 目录项 | 接口 | 方法 | 鉴权 | 参数 | 返回摘要 | 来源 |
| --- | --- | --- | --- | --- | --- | --- |
| 19.1 教程分类列表 | `/chapter/547/sublist/json` | GET | 否 | 无 | `Chapter[]` | 示例推断 |
| 19.2 某教程下文章列表 | `/article/list/{page}/json?cid={tutorialId}&order_type=1` | GET | 否 | `page` 从 `0` 开始，`tutorialId` 为教程 id | `Page<Article>` | 文档给出 |

## 5. 接入阶段建议

如果下一步要开始做 App，我建议把接口先分成 4 组实现：

1. 首屏公开数据：首页、Banner、热词、置顶、常用网站、导航、体系、项目。
2. 内容型列表：公众号、广场、问答、最新项目、教程。
3. 登录态能力：登录/注册、收藏、积分、消息、用户信息、TODO。
4. 次级工具页：Google Maven、工具列表、鸿蒙首页、热门聚合接口。

## 6. 当前清单中的已知空白

- `TODO v2` 的真实返回字段未登录态采样成功，当前只保留了文档中明确写出的入参与过滤条件。
- 收藏列表、收藏站点列表、个人积分、积分明细、消息列表、个人分享列表、用户信息等登录态接口，当前只确认了路径、方法和登录要求，未完整展开字段。
- Google Maven 查询接口官方说明偏少，当前只确认了路由和一个可工作的查询示例。

这些空白不影响先做第一轮接口分层和数据模型设计，但在真正开发登录态模块前，建议先用测试账号再补一轮采样。
