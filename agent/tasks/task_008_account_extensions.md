# Task 008 - Account Extensions

## 1. Goal

Add the first closed-loop login-only extension module after auth is stable.

## 2. Scope

- Points overview
- Points history list
- Follow the existing login-expired error handling
- Keep message and TODO capability for a later task

## 3. Dependency

- `task_004`

## 4. Acceptance

- At least one login-state extension module forms a complete closed loop
- Login-state error handling stays consistent
- `agent/docs/API_SPEC.md` is updated

## 5. Progress

- 2026-04-23: Added points APIs for overview and history
- 2026-04-23: Added repository support and tests for points overview/history
- 2026-04-23: Built a dedicated points screen with refresh, paging, and empty/error states
- 2026-04-23: Added a logged-in entry from the auth screen to the points screen
- 2026-04-23: Updated `agent/docs/API_SPEC.md` with the points extension endpoints
- 2026-04-23: Passed `:app:compileDebugKotlin` and `:app:testDebugUnitTest`
- 2026-04-23: Task state updated to `done`
