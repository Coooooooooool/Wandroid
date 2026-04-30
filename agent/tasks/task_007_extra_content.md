# Task 007 - Extra Content Modules

## 1. Goal

Add prioritized support for Wenda, Square, WeChat articles, and Latest Project content.

## 2. Scope

- Wenda list
- Square list
- WeChat article categories and article list
- Latest project list

## 3. Dependency

- `task_002`
- `task_003`

## 4. Acceptance

- At least one extra-content module forms a complete closed loop
- New APIs are added to `agent/docs/API_SPEC.md`

## 5. Progress

- 2026-04-23: Added extra-content APIs and repository support for Wenda, Square, WeChat, and Latest Project
- 2026-04-23: Built a unified extra-content screen with mode switching, WeChat category switching, paging, collect actions, and Web navigation
- 2026-04-23: Added a home entry for the extra-content module
- 2026-04-23: Updated `agent/docs/API_SPEC.md` with Phase E extra-content endpoints
- 2026-04-23: Passed `:app:compileDebugKotlin` and `:app:testDebugUnitTest`
- 2026-04-23: Task state updated to `done`
