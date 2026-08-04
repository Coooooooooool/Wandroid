# 缺陷修复与小型优化记录

本文件记录不需要创建独立任务的缺陷修复、UI 调整和范围明确的小型优化。

每项记录应包括：问题、改动、涉及文件、验证方式及遗留风险。新功能、跨模块重构或需要明确依赖管理的工作，仍按正常任务流程进入 `TASK_QUEUE.md`。

## 2026-07-30 标题栏视觉一致性

- 问题：登录页和设置页标题栏使用绿色背景，与中性内容页样式不一致。
- 改动：两页 `MaterialToolbar` 复用 `surface_background` 与 `text_primary`，并将 Activity 的边到边处理切换为 `applySurfaceToolbar()`，避免运行时将标题栏重置为 `brand_primary`。标题和返回图标在日间、夜间模式中均跟随现有颜色资源。
- 涉及文件：`app/src/main/res/layout/activity_auth.xml`、`app/src/main/res/layout/activity_settings.xml`、`app/src/main/java/com/eric/wandroid/ui/auth/AuthActivity.kt`、`app/src/main/java/com/eric/wandroid/ui/settings/SettingsActivity.kt`。
- 验证：`git diff --check` 通过，并已核对 `values` 与 `values-night` 的颜色资源。
- 遗留：当前环境未配置 `JAVA_HOME` 且无 `java` 命令，尚未执行 Gradle 编译。

## 2026-07-30 退出登录确认与中文提示

- 问题：已登录用户点击“退出登录”后立即生效，且成功提示为英文。
- 改动：退出操作前增加确认对话框；确认后才发起退出请求。实际退出流程的成功提示改为中文资源。
- 涉及文件：`app/src/main/java/com/eric/wandroid/ui/shell/MyFragment.kt`、`app/src/main/res/values/strings.xml`。
- 验证：待编译与运行态复核。
- 遗留：当前环境未配置 `JAVA_HOME` 且无 `java` 命令，无法执行 Gradle 编译。

## 2026-07-30 首页热门卡片高度优化

- 问题：首页“最受欢迎”横向卡片中的学习路线、问答和专栏条目可显示两行，导致卡片高度偏高且不一致。
- 改动：将共用条目文本限制为单行，保留末尾省略号以展示被截断的长文本。
- 涉及文件：`app/src/main/res/layout/item_home_popular_text.xml`。
- 验证：已核对三类卡片均经 `item_home_popular_text.xml` 动态创建条目。
- 遗留：当前环境未配置 `JAVA_HOME` 且无 `java` 命令，无法执行 Gradle 编译。
