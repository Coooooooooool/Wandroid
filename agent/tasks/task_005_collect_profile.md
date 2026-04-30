# Task 005 - Collect And Profile

## 1. Goal

Implement collect, uncollect, collected list, and user profile features.

## 2. Scope

- Collect article
- Uncollect article
- Collected article list
- User profile

## 3. Dependency

- `task_004`

## 4. Acceptance

- Logged-in users can collect and uncollect articles
- The collected list can be loaded
- User profile data can be fetched
- Not-logged-in users receive a correct prompt

## 5. Progress

- 2026-04-23: Added collect, uncollect, collected-list, and user-profile API support plus account repository logic
- 2026-04-23: Wired collect actions into home, system, and project/search article lists
- 2026-04-23: Added a dedicated profile and favorites screen with an entry from the auth screen
- 2026-04-23: Added `AccountRepositoryTest`
- 2026-04-23: Passed `:app:testDebugUnitTest` and `:app:compileDebugKotlin`
- 2026-04-23: Task state updated to `done`
