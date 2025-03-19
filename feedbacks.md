# Part 3 Feedbacks

## Code Base

- Unused import statements, variables can be removed before merging to main
- Try to avoid using deprecated methods (`startActivityForResult` in `MoodHistoryActivity`)
- Code related to local files should be removed before merging to main (Line 37 in `build.gradle.kts`)
- Auto-generated comments are not cleaned up (Line 271 in `AddingMoodActivity`)

## Tests

- Unit tests should be placed in `src/test` not `src/androidTest`
- Instrumentation tests should be added

## Backlog

- Only 9 user stories are linked to the milestone which is less than 40%

## UI

- The menu bar should be at the bottom of the screen instead of the top during the login

## Tool Usage

- Issues for user stories is closed but related sub-tasks are not, which should be
- Issues can be linked and closed through PRs
- Milestones can be closed after completion
- Some PRs are merged by the creater without reviews
