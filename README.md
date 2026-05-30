# SpendWise

SpendWise is a clean, modern, and **100% local-only** personal finance tracker for Android. It is designed to run entirely offline with zero cloud sync, zero SMS parsing, zero analytics, and zero trackers—keeping your financial data private.

Built using Modern Android Development (MAD) practices:
- **UI:** Jetpack Compose (Material 3)
- **Database:** Room (SQLite) with migrations & automated integrity checks
- **Dependency Injection:** Koin (Kotlin injection)
- **Background Tasks:** WorkManager for daily logging reminders
- **State Flow:** Kotlin Coroutines & Flow (reactive pipeline)

---

## 🔒 Design Philosophy: Local-Only
SpendWise is committed to privacy and simplicity. The following rules are strictly enforced:
- **No Cloud Integration:** Do not add Firebase, Supabase, AWS, or any remote sync mechanism.
- **No Analytics / Telemetry:** No user tracking, Google Analytics, Firebase Analytics, or Crashlytics.
- **No SMS Reading Permissions:** SMS permissions must not be added.
- **No Exact Alarms:** To comply with Google Play policies for non-alarm apps, daily logging reminders must use **WorkManager** instead of Exact Alarms (`SCHEDULE_EXACT_ALARM` or `USE_EXACT_ALARM`).
- **Standard Storage:** Backups and exports are managed locally using Android's Storage Access Framework (SAF) via CSV and SQLite raw `.db` files.

---

## 🤝 Contribution Guidelines

We welcome contributions! To ensure high quality, a clean git history, and automated releases, please follow these rules.

### 1. Branch Naming Rules
Always create a branch named according to the type of work and base it on the issue number:
- For features/enhancements: `feature/issue-<number>-<description>` or `feature/<description>`
- For bug fixes: `fix/issue-<number>-<description>` or `fix/<description>`

### 2. Pull Request Naming Rules (Crucial for CI/CD)
SpendWise uses GitHub Actions to automate versioning and releases. The release workflow parses the Pull Request name / merge commit message to determine the SemVer increment:
- **Feature Pull Requests:** The title **must** contain `feature` or `feat` (e.g. `feature: add dark theme support`). This triggers a **Minor** version bump (e.g., `2.1.0` -> `2.2.0`).
- **Fix Pull Requests:** If the title does not contain feature/feat (e.g. `fix: resolve chart tooltip crash`), the pipeline defaults to a **Patch** version bump (e.g., `2.1.0` -> `2.1.1`).

### 3. Commit Message Style (Conventional Commits)
Please format all commit messages according to Conventional Commits:
- `feat: <description>` (for new features)
- `fix: <description>` (for bug fixes)
- `chore: <description>` (for configuration, Gradle, or dependency updates)
- `docs: <description>` (for documentation updates)
- `ci: <description>` (for GitHub Actions or build scripts)

### 4. Code Quality and Testing
- Keep Room database singletons robust. If you change the database schema, add a corresponding `Migration` object to the migrations list in `AppDatabase` and increment the version.
- Never use destructive database fallbacks.
- Make sure all inputs are guarded. Clamp large numeric entries (e.g., income, budget) to `1,000,000,000` to prevent database and rendering overflows.
- Check code compilation locally using Gradle before pushing:
  ```bash
  ./gradlew compileDebugKotlin
  ```
- Test release minification locally to ensure ProGuard/R8 do not obfuscate needed dependencies:
  ```bash
  ./gradlew assembleRelease
  ```
