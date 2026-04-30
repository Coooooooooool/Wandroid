# Task 006 - Web Container

## 1. Goal

Build a unified in-app article and link container based on `WebView`.

## 2. Scope

- WebView page entry
- Basic loading and error states
- Title display and back behavior

## 3. Dependency

- `task_001`

## 4. Acceptance

- Article and link targets can be opened
- The page shows basic loading feedback
- Back behavior works correctly

## 5. Progress

- 2026-04-23: Added `WebContainerActivity` and its layout
- 2026-04-23: Added WebView loading, title sync, error state, retry, and in-page back navigation
- 2026-04-23: Wired home, system, project/search, and collected-article links into the shared Web container
- 2026-04-23: Passed `:app:compileDebugKotlin`
- 2026-04-23: Task state updated to `done`
