# SpendWise — Android Expense Tracker Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a modern Android expense tracker app with manual + UPI auto-detection input, category-based grouping, monthly analytics with charts, configurable salary-day month boundaries, and budget alerts.

**Architecture:** MVVM + Clean Architecture (single module, 3-layer package split: ui/domain/data). Local-first with Room database, reactive data flow via Kotlin Coroutines + Flow, Koin DI. UPI sync via SMS parsing (primary) and notification listener (secondary).

**Tech Stack:** Kotlin, Jetpack Compose + Material 3, Room, Vico (charts), Koin, Compose Navigation (type-safe), DataStore, WorkManager, Glance (widget)

---

## File Structure

```
SpendWise/
  app/
    src/
      main/
        java/com/spendwise/app/
          SpendWiseApp.kt                          # Application class (Koin init)
          ui/
            MainActivity.kt                        # Single activity, Compose host
            theme/
              Color.kt                             # Color definitions + seed palette
              Theme.kt                             # Material 3 dynamic theming
              Type.kt                              # Typography scale
            navigation/
              Screen.kt                            # @Serializable route objects
              AppNavGraph.kt                       # NavHost with all destinations
              BottomNavBar.kt                      # Bottom navigation bar
            components/
              CategoryChipGrid.kt                  # Reusable category chip selector
              AmountInput.kt                       # Currency-formatted amount field
            home/
              HomeScreen.kt
              HomeViewModel.kt
              HomeUiState.kt
              components/
                MonthSummaryCard.kt
                RecentExpenseItem.kt
                TopCategoryChips.kt
            addexpense/
              AddExpenseSheet.kt                   # Quick entry bottom sheet
              AddExpenseDetailScreen.kt            # Full detailed form
              AddExpenseViewModel.kt
              AddExpenseUiState.kt
            analytics/
              AnalyticsScreen.kt
              AnalyticsViewModel.kt
              AnalyticsUiState.kt
              components/
                CategoryDonutChart.kt
                SpendingTrendChart.kt
                BudgetVsActualList.kt
            history/
              HistoryScreen.kt
              HistoryViewModel.kt
              HistoryUiState.kt
              components/
                FilterChipsRow.kt
                DateGroupHeader.kt
                ExpenseDetailSheet.kt
            categories/
              CategoriesScreen.kt
              CategoriesViewModel.kt
              CategoriesUiState.kt
              components/
                CategoryEditSheet.kt
                IconPicker.kt
                ColorPicker.kt
            settings/
              SettingsScreen.kt
              SettingsViewModel.kt
              SettingsUiState.kt
              components/
                SalaryDayPicker.kt
                ThemeSelector.kt
          domain/
            model/
              Expense.kt
              Category.kt
              Budget.kt
              MonthPeriod.kt
              ExpenseSummary.kt
            repository/
              ExpenseRepository.kt                 # Interface
              CategoryRepository.kt                # Interface
              BudgetRepository.kt                  # Interface
              UserPreferencesRepository.kt         # Interface
            usecase/
              GetMonthPeriodUseCase.kt
              GetMonthlySummaryUseCase.kt
              GetCategoryBreakdownUseCase.kt
              CheckBudgetAlertUseCase.kt
              ProcessRecurringExpensesUseCase.kt
          data/
            local/
              AppDatabase.kt
              Converters.kt
              UserPreferencesDataStore.kt
              entity/
                ExpenseEntity.kt
                CategoryEntity.kt
                BudgetEntity.kt
              dao/
                ExpenseDao.kt
                CategoryDao.kt
                BudgetDao.kt
            repository/
              ExpenseRepositoryImpl.kt
              CategoryRepositoryImpl.kt
              BudgetRepositoryImpl.kt
              UserPreferencesRepositoryImpl.kt
            export/
              CsvExporter.kt
            backup/
              DatabaseBackupManager.kt
          sms/
            SmsBroadcastReceiver.kt
            BankSmsParser.kt
            SmsTransaction.kt
            BankPatterns.kt
            VpaCategoryMapper.kt
            SmsPermissionHelper.kt
          notification/
            UpiNotificationListener.kt
            NotificationParser.kt
            NotificationPermissionHelper.kt
            BudgetAlertManager.kt
            NotificationChannelSetup.kt
          worker/
            RecurringExpenseWorker.kt
          widget/
            QuickExpenseWidget.kt
            QuickExpenseWidgetReceiver.kt
          di/
            AppModule.kt
            DatabaseModule.kt
            DomainModule.kt
        res/
          xml/
            quick_expense_widget_info.xml
      test/                                        # Unit tests (JVM)
      androidTest/                                 # Instrumented tests
    build.gradle.kts
  gradle/
    libs.versions.toml
  build.gradle.kts                                 # Root
  settings.gradle.kts
```

---

## Task Dependency Graph

```
Task 0 (Scaffolding)
  ├── Task 1 (Database) ──► Task 2 (Domain) ──► Task 3 (Data + DI)
  │                                                  │
  └── Task 4 (Theme + Nav) ─────────────────────────┤
                                                     │
                          ┌──────────────────────────┤
                          │                          │
                    Task 5 (Add Expense)       Task 12 (SMS Parser)
                          │                          │
                    Task 6 (Home) ◄──────┐     Task 13 (Notification)
                          │              │
                    Task 7 (Analytics)   Task 8 (History)
                          │              │
                    Task 9 (Categories)  Task 10 (Settings)
                          │              │
                    Task 11 (UPI SMS)    Task 14 (Budget Alerts)
                          │              │
                    Task 15 (Export)      │
                          │              │
                    Task 16 (Widget + Recurring + Polish)
```

---

### Task 0: Project Scaffolding & Gradle Setup

**Goal:** Create the Android project with Gradle version catalog, all dependencies, and package structure.

**Files:**
- Create: `build.gradle.kts` (root)
- Create: `app/build.gradle.kts`
- Create: `gradle/libs.versions.toml`
- Create: `settings.gradle.kts`
- Create: `gradle.properties`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/spendwise/app/SpendWiseApp.kt`

**Acceptance Criteria:**
- [ ] Project builds successfully with `./gradlew assembleDebug`
- [ ] All dependencies resolve (Compose, Room, Koin, Vico, Navigation)
- [ ] App launches to a blank Compose screen

**Verify:** `./gradlew assembleDebug` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Create version catalog**

```toml
# gradle/libs.versions.toml
[versions]
kotlin = "2.1.0"
agp = "8.8.0"
compose-bom = "2026.04.00"
room = "2.7.0"
koin = "4.1.0"
vico = "2.1.0"
coroutines = "1.10.0"
navigation = "2.8.5"
datastore = "1.1.2"
work = "2.10.0"
glance = "1.1.1"
kotlinx-datetime = "0.6.2"
kotlinx-serialization = "1.7.3"

[libraries]
# Compose
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }

# Navigation
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }

# Lifecycle
lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version = "2.8.7" }
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version = "2.8.7" }

# Room
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-testing = { group = "androidx.room", name = "room-testing", version.ref = "room" }

# Koin
koin-bom = { group = "io.insert-koin", name = "koin-bom", version.ref = "koin" }
koin-android = { group = "io.insert-koin", name = "koin-android" }
koin-compose = { group = "io.insert-koin", name = "koin-androidx-compose" }

# Charts
vico-compose-m3 = { group = "com.patrykandpatrick.vico", name = "compose-m3", version.ref = "vico" }

# DataStore
datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }

# WorkManager
work-runtime = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "work" }

# Glance (Widget)
glance-appwidget = { group = "androidx.glance", name = "glance-appwidget", version.ref = "glance" }
glance-material3 = { group = "androidx.glance", name = "glance-material3", version.ref = "glance" }

# KotlinX
coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }
kotlinx-datetime = { group = "org.jetbrains.kotlinx", name = "kotlinx-datetime", version.ref = "kotlinx-datetime" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinx-serialization" }

# Testing
junit = { group = "junit", name = "junit", version = "4.13.2" }
turbine = { group = "app.cash.turbine", name = "turbine", version = "1.2.0" }
mockk = { group = "io.mockk", name = "mockk", version = "1.13.13" }
compose-ui-test = { group = "androidx.compose.ui", name = "ui-test-junit4" }
compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
test-runner = { group = "androidx.test", name = "runner", version = "1.6.2" }
test-core = { group = "androidx.test", name = "core-ktx", version = "1.6.1" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version = "2.1.0-1.0.29" }
room = { id = "androidx.room", version.ref = "room" }
```

- [ ] **Step 2: Create root build.gradle.kts**

```kotlin
// build.gradle.kts (root)
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
}
```

- [ ] **Step 3: Create app/build.gradle.kts**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

android {
    namespace = "com.spendwise.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.spendwise.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }
}

dependencies {
    // Compose
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.icons.extended)
    debugImplementation(libs.compose.ui.tooling)

    // Navigation
    implementation(libs.navigation.compose)

    // Lifecycle
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Koin
    val koinBom = platform(libs.koin.bom)
    implementation(koinBom)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)

    // Charts
    implementation(libs.vico.compose.m3)

    // DataStore
    implementation(libs.datastore.preferences)

    // WorkManager
    implementation(libs.work.runtime)

    // Glance Widget
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)

    // KotlinX
    implementation(libs.coroutines.android)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    androidTestImplementation(composeBom)
    androidTestImplementation(libs.compose.ui.test)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.test.runner)
    androidTestImplementation(libs.test.core)
    debugImplementation(libs.compose.ui.test.manifest)
}
```

- [ ] **Step 4: Create settings.gradle.kts**

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolution {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SpendWise"
include(":app")
```

- [ ] **Step 5: Create AndroidManifest.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.READ_SMS" />
    <uses-permission android:name="android.permission.RECEIVE_SMS" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <application
        android:name=".SpendWiseApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="SpendWise"
        android:supportsRtl="true"
        android:theme="@style/Theme.SpendWise">

        <activity
            android:name=".ui.MainActivity"
            android:exported="true"
            android:theme="@style/Theme.SpendWise">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

    </application>

</manifest>
```

- [ ] **Step 6: Create SpendWiseApp.kt**

```kotlin
package com.spendwise.app

import android.app.Application
import com.spendwise.app.di.appModule
import com.spendwise.app.di.databaseModule
import com.spendwise.app.di.domainModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SpendWiseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@SpendWiseApp)
            modules(databaseModule, domainModule, appModule)
        }
    }
}
```

- [ ] **Step 7: Build and verify**

```bash
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```bash
git init
git add -A
git commit -m "feat: initial project scaffolding with Gradle version catalog and dependencies"
```

---

### Task 1: Room Database — Entities, DAOs, and Database

**Goal:** Create the complete Room database layer with Expense, Category, and Budget entities, DAOs with reactive Flow queries, and the AppDatabase.

**Files:**
- Create: `app/src/main/java/com/spendwise/app/data/local/entity/ExpenseEntity.kt`
- Create: `app/src/main/java/com/spendwise/app/data/local/entity/CategoryEntity.kt`
- Create: `app/src/main/java/com/spendwise/app/data/local/entity/BudgetEntity.kt`
- Create: `app/src/main/java/com/spendwise/app/data/local/dao/ExpenseDao.kt`
- Create: `app/src/main/java/com/spendwise/app/data/local/dao/CategoryDao.kt`
- Create: `app/src/main/java/com/spendwise/app/data/local/dao/BudgetDao.kt`
- Create: `app/src/main/java/com/spendwise/app/data/local/AppDatabase.kt`
- Create: `app/src/main/java/com/spendwise/app/data/local/Converters.kt`
- Test: `app/src/androidTest/java/com/spendwise/app/data/local/dao/ExpenseDaoTest.kt`
- Test: `app/src/androidTest/java/com/spendwise/app/data/local/dao/CategoryDaoTest.kt`

**Acceptance Criteria:**
- [ ] All entities compile with Room annotations
- [ ] DAOs return Flow for reactive queries
- [ ] Pre-populated default categories via RoomDatabase.Callback
- [ ] Instrumented tests pass for CRUD operations

**Verify:** `./gradlew connectedAndroidTest` → All DAO tests pass

**Steps:**

- [ ] **Step 1: Write ExpenseEntity**

```kotlin
package com.spendwise.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId"), Index("date"), Index("upiRefId")]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val categoryId: Long?,
    val description: String = "",
    val date: Long,                  // epoch millis
    val paymentMethod: String = "UPI", // UPI, Cash, Card, NetBanking, Other
    val tags: String = "",           // comma-separated
    val upiRefId: String? = null,    // 12-digit UPI ref for dedup
    val merchantVpa: String? = null,
    val source: String = "MANUAL",   // MANUAL, SMS, NOTIFICATION, IMPORT
    val isRecurring: Boolean = false,
    val recurringInterval: String? = null, // DAILY, WEEKLY, MONTHLY, YEARLY
    val createdAt: Long = System.currentTimeMillis()
)
```

- [ ] **Step 2: Write CategoryEntity**

```kotlin
package com.spendwise.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String,         // Material icon name
    val colorHex: String,     // e.g., "#4CAF50"
    val sortOrder: Int = 0,
    val isDefault: Boolean = false
)
```

- [ ] **Step 3: Write BudgetEntity**

```kotlin
package com.spendwise.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId")]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val monthlyLimit: Double,
    val isOverallBudget: Boolean = false // true = overall, false = per-category
)
```

- [ ] **Step 4: Write Converters**

```kotlin
package com.spendwise.app.data.local

import androidx.room.TypeConverter
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long): Instant = Instant.fromEpochMilliseconds(value)

    @TypeConverter
    fun toTimestamp(instant: Instant): Long = instant.toEpochMilliseconds()
}
```

- [ ] **Step 5: Write ExpenseDao**

```kotlin
package com.spendwise.app.data.local.dao

import androidx.room.*
import com.spendwise.app.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: ExpenseEntity): Long

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Delete
    suspend fun delete(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: Long): ExpenseEntity?

    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE categoryId = :categoryId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getByCategoryAndDateRange(categoryId: Long, startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE description LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' ORDER BY date DESC")
    fun search(query: String): Flow<List<ExpenseEntity>>

    @Query("SELECT SUM(amount) FROM expenses WHERE date BETWEEN :startDate AND :endDate")
    fun getTotalByDateRange(startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM expenses WHERE categoryId = :categoryId AND date BETWEEN :startDate AND :endDate")
    fun getTotalByCategoryAndDateRange(categoryId: Long, startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT * FROM expenses WHERE upiRefId = :upiRef LIMIT 1")
    suspend fun getByUpiRef(upiRef: String): ExpenseEntity?

    @Query("SELECT * FROM expenses WHERE isRecurring = 1")
    suspend fun getRecurringExpenses(): List<ExpenseEntity>

    @Query("SELECT * FROM expenses ORDER BY date DESC LIMIT :limit OFFSET :offset")
    fun getPaginated(limit: Int, offset: Int): Flow<List<ExpenseEntity>>

    @Query("SELECT COUNT(*) FROM expenses WHERE categoryId = :categoryId")
    suspend fun getCountByCategory(categoryId: Long): Int
}
```

- [ ] **Step 6: Write CategoryDao**

```kotlin
package com.spendwise.app.data.local.dao

import androidx.room.*
import com.spendwise.app.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
    fun getAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): CategoryEntity?

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): CategoryEntity?

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCount(): Int
}
```

- [ ] **Step 7: Write BudgetDao**

```kotlin
package com.spendwise.app.data.local.dao

import androidx.room.*
import com.spendwise.app.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity): Long

    @Update
    suspend fun update(budget: BudgetEntity)

    @Delete
    suspend fun delete(budget: BudgetEntity)

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId LIMIT 1")
    fun getByCategoryId(categoryId: Long): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets WHERE isOverallBudget = 1 LIMIT 1")
    fun getOverallBudget(): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets")
    fun getAll(): Flow<List<BudgetEntity>>
}
```

- [ ] **Step 8: Write AppDatabase with default categories**

```kotlin
package com.spendwise.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.spendwise.app.data.local.dao.BudgetDao
import com.spendwise.app.data.local.dao.CategoryDao
import com.spendwise.app.data.local.dao.ExpenseDao
import com.spendwise.app.data.local.entity.BudgetEntity
import com.spendwise.app.data.local.entity.CategoryEntity
import com.spendwise.app.data.local.entity.ExpenseEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ExpenseEntity::class, CategoryEntity::class, BudgetEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "spendwise.db"
            )
                .addCallback(DefaultCategoryCallback())
                .build()
        }
    }

    private class DefaultCategoryCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val defaults = listOf(
                "('Food & Dining', 'Restaurant', '#4CAF50', 0, 1)",
                "('Transport', 'DirectionsCar', '#2196F3', 1, 1)",
                "('Bills & Utilities', 'Receipt', '#FF9800', 2, 1)",
                "('Shopping', 'ShoppingBag', '#E91E63', 3, 1)",
                "('Health', 'LocalHospital', '#F44336', 4, 1)",
                "('Entertainment', 'Movie', '#9C27B0', 5, 1)",
                "('Groceries', 'ShoppingCart', '#8BC34A', 6, 1)",
                "('Other', 'MoreHoriz', '#607D8B', 7, 1)"
            )
            defaults.forEach { values ->
                db.execSQL("INSERT INTO categories (name, icon, colorHex, sortOrder, isDefault) VALUES $values")
            }
        }
    }
}
```

- [ ] **Step 9: Write DAO tests**

```kotlin
package com.spendwise.app.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.spendwise.app.data.local.AppDatabase
import com.spendwise.app.data.local.entity.CategoryEntity
import com.spendwise.app.data.local.entity.ExpenseEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ExpenseDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var expenseDao: ExpenseDao
    private lateinit var categoryDao: CategoryDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        expenseDao = db.expenseDao()
        categoryDao = db.categoryDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertAndRetrieveExpense() = runTest {
        val category = CategoryEntity(name = "Food", icon = "Restaurant", colorHex = "#4CAF50")
        val catId = categoryDao.insert(category)

        val expense = ExpenseEntity(
            amount = 450.0,
            categoryId = catId,
            description = "Swiggy order",
            date = System.currentTimeMillis(),
            paymentMethod = "UPI"
        )
        val id = expenseDao.insert(expense)
        val retrieved = expenseDao.getById(id)

        assertNotNull(retrieved)
        assertEquals(450.0, retrieved!!.amount, 0.01)
        assertEquals("Swiggy order", retrieved.description)
    }

    @Test
    fun getByDateRangeReturnsCorrectExpenses() = runTest {
        val now = System.currentTimeMillis()
        val dayAgo = now - 86_400_000
        val twoDaysAgo = now - 172_800_000

        expenseDao.insert(ExpenseEntity(amount = 100.0, categoryId = null, date = now))
        expenseDao.insert(ExpenseEntity(amount = 200.0, categoryId = null, date = dayAgo))
        expenseDao.insert(ExpenseEntity(amount = 300.0, categoryId = null, date = twoDaysAgo))

        expenseDao.getByDateRange(dayAgo, now).test {
            val expenses = awaitItem()
            assertEquals(2, expenses.size)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun deduplicateByUpiRef() = runTest {
        val expense1 = ExpenseEntity(amount = 500.0, categoryId = null, date = System.currentTimeMillis(), upiRefId = "123456789012")
        expenseDao.insert(expense1)

        val existing = expenseDao.getByUpiRef("123456789012")
        assertNotNull(existing)

        val nonExisting = expenseDao.getByUpiRef("999999999999")
        assertNull(nonExisting)
    }
}
```

- [ ] **Step 10: Run tests and commit**

```bash
./gradlew connectedAndroidTest
git add -A
git commit -m "feat: add Room database layer with entities, DAOs, and default categories"
```

---

### Task 2: Domain Layer — Models, Repository Interfaces, and Use Cases

**Goal:** Create domain models, repository interfaces, and key use cases that encapsulate business logic (month boundary calculation, category aggregation).

**Files:**
- Create: `app/src/main/java/com/spendwise/app/domain/model/Expense.kt`
- Create: `app/src/main/java/com/spendwise/app/domain/model/Category.kt`
- Create: `app/src/main/java/com/spendwise/app/domain/model/Budget.kt`
- Create: `app/src/main/java/com/spendwise/app/domain/model/MonthPeriod.kt`
- Create: `app/src/main/java/com/spendwise/app/domain/model/ExpenseSummary.kt`
- Create: `app/src/main/java/com/spendwise/app/domain/repository/*.kt` (4 interfaces)
- Create: `app/src/main/java/com/spendwise/app/domain/usecase/*.kt` (3 use cases)
- Test: `app/src/test/java/com/spendwise/app/domain/usecase/GetMonthPeriodUseCaseTest.kt`

**Acceptance Criteria:**
- [ ] MonthPeriod correctly calculates start/end for both calendar and salary-day modes
- [ ] Unit tests pass for month boundary edge cases (salary day = 31, February, leap year)

**Verify:** `./gradlew test` → All unit tests pass

**Steps:**

- [ ] **Step 1: Write domain models**

```kotlin
// domain/model/Expense.kt
package com.spendwise.app.domain.model

import kotlinx.datetime.LocalDateTime

data class Expense(
    val id: Long = 0,
    val amount: Double,
    val category: Category?,
    val description: String = "",
    val date: LocalDateTime,
    val paymentMethod: PaymentMethod = PaymentMethod.UPI,
    val tags: List<String> = emptyList(),
    val upiRefId: String? = null,
    val merchantVpa: String? = null,
    val source: ExpenseSource = ExpenseSource.MANUAL,
    val isRecurring: Boolean = false,
    val recurringInterval: RecurringInterval? = null
)

enum class PaymentMethod { UPI, CASH, CARD, NET_BANKING, OTHER }
enum class ExpenseSource { MANUAL, SMS, NOTIFICATION, IMPORT }
enum class RecurringInterval { DAILY, WEEKLY, MONTHLY, YEARLY }
```

```kotlin
// domain/model/Category.kt
package com.spendwise.app.domain.model

data class Category(
    val id: Long = 0,
    val name: String,
    val icon: String,
    val colorHex: String,
    val sortOrder: Int = 0
)
```

```kotlin
// domain/model/Budget.kt
package com.spendwise.app.domain.model

data class Budget(
    val id: Long = 0,
    val categoryId: Long,
    val monthlyLimit: Double,
    val isOverallBudget: Boolean = false
)
```

```kotlin
// domain/model/MonthPeriod.kt
package com.spendwise.app.domain.model

import kotlinx.datetime.LocalDate

data class MonthPeriod(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val label: String   // e.g., "May 2026"
)
```

```kotlin
// domain/model/ExpenseSummary.kt
package com.spendwise.app.domain.model

data class ExpenseSummary(
    val totalSpent: Double,
    val totalBudget: Double?,
    val categoryBreakdown: List<CategorySpend>,
    val daysRemaining: Int
)

data class CategorySpend(
    val category: Category,
    val amount: Double,
    val percentage: Double,
    val budgetLimit: Double?
)
```

- [ ] **Step 2: Write repository interfaces**

```kotlin
// domain/repository/ExpenseRepository.kt
package com.spendwise.app.domain.repository

import com.spendwise.app.domain.model.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface ExpenseRepository {
    suspend fun addExpense(expense: Expense): Long
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(expense: Expense)
    suspend fun getExpenseById(id: Long): Expense?
    fun getExpensesByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Expense>>
    fun getExpensesByCategoryAndDateRange(categoryId: Long, startDate: LocalDate, endDate: LocalDate): Flow<List<Expense>>
    fun searchExpenses(query: String): Flow<List<Expense>>
    fun getTotalByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<Double?>
    fun getTotalByCategoryAndDateRange(categoryId: Long, startDate: LocalDate, endDate: LocalDate): Flow<Double?>
    suspend fun getByUpiRef(upiRef: String): Expense?
    suspend fun getRecurringExpenses(): List<Expense>
    fun getPaginated(limit: Int, offset: Int): Flow<List<Expense>>
}
```

```kotlin
// domain/repository/CategoryRepository.kt
package com.spendwise.app.domain.repository

import com.spendwise.app.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
    suspend fun getCategoryById(id: Long): Category?
    suspend fun getCategoryByName(name: String): Category?
    suspend fun addCategory(category: Category): Long
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(category: Category)
    suspend fun getExpenseCountForCategory(categoryId: Long): Int
}
```

```kotlin
// domain/repository/BudgetRepository.kt
package com.spendwise.app.domain.repository

import com.spendwise.app.domain.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getBudgetForCategory(categoryId: Long): Flow<Budget?>
    fun getOverallBudget(): Flow<Budget?>
    fun getAllBudgets(): Flow<List<Budget>>
    suspend fun setBudget(budget: Budget): Long
    suspend fun deleteBudget(budget: Budget)
}
```

```kotlin
// domain/repository/UserPreferencesRepository.kt
package com.spendwise.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val salaryDay: Flow<Int>           // 1-31
    val isCalendarMode: Flow<Boolean>  // true = calendar, false = salary-based
    val themeMode: Flow<String>        // "system", "light", "dark"
    val isDynamicColor: Flow<Boolean>
    val isUpiSyncEnabled: Flow<Boolean>
    val monthlyIncome: Flow<Double?>

    suspend fun setSalaryDay(day: Int)
    suspend fun setCalendarMode(isCalendar: Boolean)
    suspend fun setThemeMode(mode: String)
    suspend fun setDynamicColor(enabled: Boolean)
    suspend fun setUpiSyncEnabled(enabled: Boolean)
    suspend fun setMonthlyIncome(income: Double?)
}
```

- [ ] **Step 3: Write GetMonthPeriodUseCase**

```kotlin
package com.spendwise.app.domain.usecase

import com.spendwise.app.domain.model.MonthPeriod
import com.spendwise.app.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.*

class GetMonthPeriodUseCase(
    private val prefsRepository: UserPreferencesRepository
) {
    suspend fun getCurrentPeriod(): MonthPeriod {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return getPeriodForDate(today)
    }

    suspend fun getPeriodForDate(date: LocalDate): MonthPeriod {
        val isCalendar = prefsRepository.isCalendarMode.first()
        return if (isCalendar) {
            getCalendarPeriod(date)
        } else {
            val salaryDay = prefsRepository.salaryDay.first()
            getSalaryPeriod(date, salaryDay)
        }
    }

    fun getCalendarPeriod(date: LocalDate): MonthPeriod {
        val start = LocalDate(date.year, date.month, 1)
        val end = start.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
        val label = "${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.year}"
        return MonthPeriod(startDate = start, endDate = end, label = label)
    }

    fun getSalaryPeriod(date: LocalDate, salaryDay: Int): MonthPeriod {
        val clampedSalaryDay = salaryDay.coerceIn(1, 28) // avoid month-end edge cases
        val dayOfMonth = date.dayOfMonth

        val periodStart: LocalDate
        val periodEnd: LocalDate

        if (dayOfMonth >= clampedSalaryDay) {
            // Current salary period: salaryDay of this month to salaryDay-1 of next month
            periodStart = LocalDate(date.year, date.month, clampedSalaryDay)
            val nextMonth = periodStart.plus(1, DateTimeUnit.MONTH)
            periodEnd = LocalDate(nextMonth.year, nextMonth.month, clampedSalaryDay)
                .minus(1, DateTimeUnit.DAY)
        } else {
            // Previous salary period: salaryDay of last month to salaryDay-1 of this month
            val prevMonth = LocalDate(date.year, date.month, 1).minus(1, DateTimeUnit.MONTH)
            periodStart = LocalDate(prevMonth.year, prevMonth.month, clampedSalaryDay)
            periodEnd = LocalDate(date.year, date.month, clampedSalaryDay)
                .minus(1, DateTimeUnit.DAY)
        }

        val label = "${periodStart.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${periodStart.year}"
        return MonthPeriod(startDate = periodStart, endDate = periodEnd, label = label)
    }

    suspend fun getNextPeriod(currentPeriod: MonthPeriod): MonthPeriod {
        val nextDate = currentPeriod.endDate.plus(1, DateTimeUnit.DAY)
        return getPeriodForDate(nextDate)
    }

    suspend fun getPreviousPeriod(currentPeriod: MonthPeriod): MonthPeriod {
        val prevDate = currentPeriod.startDate.minus(1, DateTimeUnit.DAY)
        return getPeriodForDate(prevDate)
    }
}
```

- [ ] **Step 4: Write GetMonthlySummaryUseCase**

```kotlin
package com.spendwise.app.domain.usecase

import com.spendwise.app.domain.model.CategorySpend
import com.spendwise.app.domain.model.ExpenseSummary
import com.spendwise.app.domain.model.MonthPeriod
import com.spendwise.app.domain.repository.BudgetRepository
import com.spendwise.app.domain.repository.CategoryRepository
import com.spendwise.app.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import kotlinx.datetime.*

class GetMonthlySummaryUseCase(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository
) {
    fun invoke(period: MonthPeriod): Flow<ExpenseSummary> {
        return combine(
            expenseRepository.getExpensesByDateRange(period.startDate, period.endDate),
            categoryRepository.getAllCategories(),
            budgetRepository.getAllBudgets(),
            budgetRepository.getOverallBudget()
        ) { expenses, categories, budgets, overallBudget ->
            val totalSpent = expenses.sumOf { it.amount }
            val budgetMap = budgets.associateBy { it.categoryId }

            val categoryBreakdown = categories.map { category ->
                val categoryTotal = expenses
                    .filter { it.category?.id == category.id }
                    .sumOf { it.amount }
                val percentage = if (totalSpent > 0) (categoryTotal / totalSpent) * 100 else 0.0
                CategorySpend(
                    category = category,
                    amount = categoryTotal,
                    percentage = percentage,
                    budgetLimit = budgetMap[category.id]?.monthlyLimit
                )
            }.filter { it.amount > 0 }
                .sortedByDescending { it.amount }

            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val daysRemaining = if (today <= period.endDate) {
                period.endDate.toEpochDays() - today.toEpochDays()
            } else 0

            ExpenseSummary(
                totalSpent = totalSpent,
                totalBudget = overallBudget?.monthlyLimit,
                categoryBreakdown = categoryBreakdown,
                daysRemaining = daysRemaining.toInt()
            )
        }
    }
}
```

- [ ] **Step 5: Write GetCategoryBreakdownUseCase**

```kotlin
package com.spendwise.app.domain.usecase

import com.spendwise.app.domain.model.CategorySpend
import com.spendwise.app.domain.model.MonthPeriod
import com.spendwise.app.domain.repository.BudgetRepository
import com.spendwise.app.domain.repository.CategoryRepository
import com.spendwise.app.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.*

class GetCategoryBreakdownUseCase(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository
) {
    fun invoke(period: MonthPeriod): Flow<List<CategorySpend>> {
        return combine(
            expenseRepository.getExpensesByDateRange(period.startDate, period.endDate),
            categoryRepository.getAllCategories(),
            budgetRepository.getAllBudgets()
        ) { expenses, categories, budgets ->
            val totalSpent = expenses.sumOf { it.amount }
            val budgetMap = budgets.associateBy { it.categoryId }

            categories.map { category ->
                val categoryTotal = expenses
                    .filter { it.category?.id == category.id }
                    .sumOf { it.amount }
                CategorySpend(
                    category = category,
                    amount = categoryTotal,
                    percentage = if (totalSpent > 0) (categoryTotal / totalSpent) * 100 else 0.0,
                    budgetLimit = budgetMap[category.id]?.monthlyLimit
                )
            }.filter { it.amount > 0 }
                .sortedByDescending { it.amount }
        }
    }
}
```

- [ ] **Step 6: Write unit tests for GetMonthPeriodUseCase**

```kotlin
package com.spendwise.app.domain.usecase

import kotlinx.datetime.LocalDate
import org.junit.Assert.*
import org.junit.Test

class GetMonthPeriodUseCaseTest {

    // Test the pure functions directly (no prefs dependency needed)
    private val useCase = GetMonthPeriodUseCase(
        prefsRepository = TODO("mock for integration tests only")
    )

    @Test
    fun `calendar period for May 2026`() {
        // We can test the pure getCalendarPeriod function
        val date = LocalDate(2026, 5, 15)
        val period = getCalendarPeriodDirect(date)

        assertEquals(LocalDate(2026, 5, 1), period.startDate)
        assertEquals(LocalDate(2026, 5, 31), period.endDate)
        assertEquals("May 2026", period.label)
    }

    @Test
    fun `salary period with day 25 when current day is after salary day`() {
        val date = LocalDate(2026, 5, 28)
        val period = getSalaryPeriodDirect(date, 25)

        assertEquals(LocalDate(2026, 5, 25), period.startDate)
        assertEquals(LocalDate(2026, 6, 24), period.endDate)
    }

    @Test
    fun `salary period with day 25 when current day is before salary day`() {
        val date = LocalDate(2026, 5, 10)
        val period = getSalaryPeriodDirect(date, 25)

        assertEquals(LocalDate(2026, 4, 25), period.startDate)
        assertEquals(LocalDate(2026, 5, 24), period.endDate)
    }

    @Test
    fun `salary period with day 1 behaves like calendar`() {
        val date = LocalDate(2026, 5, 15)
        val period = getSalaryPeriodDirect(date, 1)

        assertEquals(LocalDate(2026, 5, 1), period.startDate)
        assertEquals(LocalDate(2026, 5, 31), period.endDate)
    }

    @Test
    fun `february calendar period in non-leap year`() {
        val date = LocalDate(2026, 2, 15)
        val period = getCalendarPeriodDirect(date)

        assertEquals(LocalDate(2026, 2, 1), period.startDate)
        assertEquals(LocalDate(2026, 2, 28), period.endDate)
    }

    @Test
    fun `salary day clamped to 28 when set to 31`() {
        val date = LocalDate(2026, 3, 15)
        val period = getSalaryPeriodDirect(date, 31)

        // 31 clamped to 28
        assertEquals(LocalDate(2026, 2, 28), period.startDate)
        assertEquals(LocalDate(2026, 3, 27), period.endDate)
    }

    // Helper: call the pure function without needing prefs
    private fun getCalendarPeriodDirect(date: LocalDate) =
        GetMonthPeriodUseCase::class.java
            .getDeclaredMethod("getCalendarPeriod", LocalDate::class.java)
            .apply { isAccessible = true }
            .invoke(useCase, date) as com.spendwise.app.domain.model.MonthPeriod

    private fun getSalaryPeriodDirect(date: LocalDate, salaryDay: Int) =
        GetMonthPeriodUseCase::class.java
            .getDeclaredMethod("getSalaryPeriod", LocalDate::class.java, Int::class.java)
            .apply { isAccessible = true }
            .invoke(useCase, date, salaryDay) as com.spendwise.app.domain.model.MonthPeriod
}
```

> **Note to implementer:** The test above uses reflection to test public functions. A cleaner approach is to make `getCalendarPeriod` and `getSalaryPeriod` public (they have no side effects) and test them directly. Adjust the access modifier in the implementation accordingly.

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "feat: add domain layer with models, repository interfaces, and month period use case"
```

---

### Task 3: Data Layer — Repository Implementations and DI Setup

**Goal:** Implement repository interfaces with Room DAOs, set up DataStore for user preferences, and wire everything with Koin DI modules.

**Files:**
- Create: `app/src/main/java/com/spendwise/app/data/repository/ExpenseRepositoryImpl.kt`
- Create: `app/src/main/java/com/spendwise/app/data/repository/CategoryRepositoryImpl.kt`
- Create: `app/src/main/java/com/spendwise/app/data/repository/BudgetRepositoryImpl.kt`
- Create: `app/src/main/java/com/spendwise/app/data/repository/UserPreferencesRepositoryImpl.kt`
- Create: `app/src/main/java/com/spendwise/app/data/local/UserPreferencesDataStore.kt`
- Create: `app/src/main/java/com/spendwise/app/di/DatabaseModule.kt`
- Create: `app/src/main/java/com/spendwise/app/di/DomainModule.kt`
- Create: `app/src/main/java/com/spendwise/app/di/AppModule.kt`

**Acceptance Criteria:**
- [ ] All repositories correctly map between entities and domain models
- [ ] DataStore persists salary day, month mode, theme preferences
- [ ] Koin modules wire all dependencies without runtime errors

**Verify:** `./gradlew test` → Tests pass; app starts without Koin errors

**Steps:**

- [ ] **Step 1: Write UserPreferencesDataStore**

```kotlin
package com.spendwise.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "spendwise_prefs")

class UserPreferencesDataStore(private val dataStore: DataStore<Preferences>) {

    companion object {
        val SALARY_DAY = intPreferencesKey("salary_day")
        val IS_CALENDAR_MODE = booleanPreferencesKey("is_calendar_mode")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val IS_DYNAMIC_COLOR = booleanPreferencesKey("is_dynamic_color")
        val IS_UPI_SYNC_ENABLED = booleanPreferencesKey("is_upi_sync_enabled")
        val MONTHLY_INCOME = doublePreferencesKey("monthly_income")
    }

    val salaryDay: Flow<Int> = dataStore.data.map { it[SALARY_DAY] ?: 1 }
    val isCalendarMode: Flow<Boolean> = dataStore.data.map { it[IS_CALENDAR_MODE] ?: true }
    val themeMode: Flow<String> = dataStore.data.map { it[THEME_MODE] ?: "system" }
    val isDynamicColor: Flow<Boolean> = dataStore.data.map { it[IS_DYNAMIC_COLOR] ?: true }
    val isUpiSyncEnabled: Flow<Boolean> = dataStore.data.map { it[IS_UPI_SYNC_ENABLED] ?: false }
    val monthlyIncome: Flow<Double?> = dataStore.data.map { it[MONTHLY_INCOME] }

    suspend fun setSalaryDay(day: Int) { dataStore.edit { it[SALARY_DAY] = day } }
    suspend fun setCalendarMode(isCalendar: Boolean) { dataStore.edit { it[IS_CALENDAR_MODE] = isCalendar } }
    suspend fun setThemeMode(mode: String) { dataStore.edit { it[THEME_MODE] = mode } }
    suspend fun setDynamicColor(enabled: Boolean) { dataStore.edit { it[IS_DYNAMIC_COLOR] = enabled } }
    suspend fun setUpiSyncEnabled(enabled: Boolean) { dataStore.edit { it[IS_UPI_SYNC_ENABLED] = enabled } }
    suspend fun setMonthlyIncome(income: Double?) {
        dataStore.edit {
            if (income != null) it[MONTHLY_INCOME] = income
            else it.remove(MONTHLY_INCOME)
        }
    }
}
```

- [ ] **Step 2: Write ExpenseRepositoryImpl** (with entity-to-domain mapping)

```kotlin
package com.spendwise.app.data.repository

import com.spendwise.app.data.local.dao.CategoryDao
import com.spendwise.app.data.local.dao.ExpenseDao
import com.spendwise.app.data.local.entity.ExpenseEntity
import com.spendwise.app.domain.model.*
import com.spendwise.app.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.*

class ExpenseRepositoryImpl(
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao
) : ExpenseRepository {

    override suspend fun addExpense(expense: Expense): Long {
        return expenseDao.insert(expense.toEntity())
    }

    override suspend fun updateExpense(expense: Expense) {
        expenseDao.update(expense.toEntity())
    }

    override suspend fun deleteExpense(expense: Expense) {
        expenseDao.delete(expense.toEntity())
    }

    override suspend fun getExpenseById(id: Long): Expense? {
        return expenseDao.getById(id)?.toDomain()
    }

    override fun getExpensesByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Expense>> {
        return expenseDao.getByDateRange(
            startDate.toEpochMillis(),
            endDate.toEpochMillis()
        ).map { entities -> entities.map { it.toDomain() } }
    }

    override fun getExpensesByCategoryAndDateRange(
        categoryId: Long, startDate: LocalDate, endDate: LocalDate
    ): Flow<List<Expense>> {
        return expenseDao.getByCategoryAndDateRange(
            categoryId, startDate.toEpochMillis(), endDate.toEpochMillis()
        ).map { entities -> entities.map { it.toDomain() } }
    }

    override fun searchExpenses(query: String): Flow<List<Expense>> {
        return expenseDao.search(query).map { entities -> entities.map { it.toDomain() } }
    }

    override fun getTotalByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<Double?> {
        return expenseDao.getTotalByDateRange(startDate.toEpochMillis(), endDate.toEpochMillis())
    }

    override fun getTotalByCategoryAndDateRange(
        categoryId: Long, startDate: LocalDate, endDate: LocalDate
    ): Flow<Double?> {
        return expenseDao.getTotalByCategoryAndDateRange(
            categoryId, startDate.toEpochMillis(), endDate.toEpochMillis()
        )
    }

    override suspend fun getByUpiRef(upiRef: String): Expense? {
        return expenseDao.getByUpiRef(upiRef)?.toDomain()
    }

    override suspend fun getRecurringExpenses(): List<Expense> {
        return expenseDao.getRecurringExpenses().map { it.toDomain() }
    }

    override fun getPaginated(limit: Int, offset: Int): Flow<List<Expense>> {
        return expenseDao.getPaginated(limit, offset).map { entities -> entities.map { it.toDomain() } }
    }

    private suspend fun ExpenseEntity.toDomain(): Expense {
        val category = categoryId?.let { categoryDao.getById(it) }
        return Expense(
            id = id,
            amount = amount,
            category = category?.let {
                Category(it.id, it.name, it.icon, it.colorHex, it.sortOrder)
            },
            description = description,
            date = Instant.fromEpochMilliseconds(date)
                .toLocalDateTime(TimeZone.currentSystemDefault()),
            paymentMethod = PaymentMethod.valueOf(paymentMethod),
            tags = if (tags.isBlank()) emptyList() else tags.split(","),
            upiRefId = upiRefId,
            merchantVpa = merchantVpa,
            source = ExpenseSource.valueOf(source),
            isRecurring = isRecurring,
            recurringInterval = recurringInterval?.let { RecurringInterval.valueOf(it) }
        )
    }

    private fun Expense.toEntity(): ExpenseEntity {
        return ExpenseEntity(
            id = id,
            amount = amount,
            categoryId = category?.id,
            description = description,
            date = date.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
            paymentMethod = paymentMethod.name,
            tags = tags.joinToString(","),
            upiRefId = upiRefId,
            merchantVpa = merchantVpa,
            source = source.name,
            isRecurring = isRecurring,
            recurringInterval = recurringInterval?.name
        )
    }

    private fun LocalDate.toEpochMillis(): Long {
        return this.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    }
}
```

- [ ] **Step 3: Write CategoryRepositoryImpl and BudgetRepositoryImpl** (similar mapping pattern — see ExpenseRepositoryImpl for entity↔domain mapping approach)

- [ ] **Step 4: Write UserPreferencesRepositoryImpl**

```kotlin
package com.spendwise.app.data.repository

import com.spendwise.app.data.local.UserPreferencesDataStore
import com.spendwise.app.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow

class UserPreferencesRepositoryImpl(
    private val dataStore: UserPreferencesDataStore
) : UserPreferencesRepository {
    override val salaryDay: Flow<Int> = dataStore.salaryDay
    override val isCalendarMode: Flow<Boolean> = dataStore.isCalendarMode
    override val themeMode: Flow<String> = dataStore.themeMode
    override val isDynamicColor: Flow<Boolean> = dataStore.isDynamicColor
    override val isUpiSyncEnabled: Flow<Boolean> = dataStore.isUpiSyncEnabled
    override val monthlyIncome: Flow<Double?> = dataStore.monthlyIncome

    override suspend fun setSalaryDay(day: Int) = dataStore.setSalaryDay(day)
    override suspend fun setCalendarMode(isCalendar: Boolean) = dataStore.setCalendarMode(isCalendar)
    override suspend fun setThemeMode(mode: String) = dataStore.setThemeMode(mode)
    override suspend fun setDynamicColor(enabled: Boolean) = dataStore.setDynamicColor(enabled)
    override suspend fun setUpiSyncEnabled(enabled: Boolean) = dataStore.setUpiSyncEnabled(enabled)
    override suspend fun setMonthlyIncome(income: Double?) = dataStore.setMonthlyIncome(income)
}
```

- [ ] **Step 5: Write Koin modules**

```kotlin
// di/DatabaseModule.kt
package com.spendwise.app.di

import com.spendwise.app.data.local.AppDatabase
import com.spendwise.app.data.local.UserPreferencesDataStore
import com.spendwise.app.data.local.dataStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single { AppDatabase.create(androidContext()) }
    single { get<AppDatabase>().expenseDao() }
    single { get<AppDatabase>().categoryDao() }
    single { get<AppDatabase>().budgetDao() }
    single { UserPreferencesDataStore(androidContext().dataStore) }
}
```

```kotlin
// di/DomainModule.kt
package com.spendwise.app.di

import com.spendwise.app.data.repository.*
import com.spendwise.app.domain.repository.*
import com.spendwise.app.domain.usecase.*
import org.koin.dsl.module

val domainModule = module {
    single<ExpenseRepository> { ExpenseRepositoryImpl(get(), get()) }
    single<CategoryRepository> { CategoryRepositoryImpl(get()) }
    single<BudgetRepository> { BudgetRepositoryImpl(get()) }
    single<UserPreferencesRepository> { UserPreferencesRepositoryImpl(get()) }

    factory { GetMonthPeriodUseCase(get()) }
    factory { GetMonthlySummaryUseCase(get(), get(), get()) }
    factory { GetCategoryBreakdownUseCase(get(), get(), get()) }
}
```

```kotlin
// di/AppModule.kt
package com.spendwise.app.di

import com.spendwise.app.ui.addexpense.AddExpenseViewModel
import com.spendwise.app.ui.analytics.AnalyticsViewModel
import com.spendwise.app.ui.categories.CategoriesViewModel
import com.spendwise.app.ui.history.HistoryViewModel
import com.spendwise.app.ui.home.HomeViewModel
import com.spendwise.app.ui.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { AddExpenseViewModel(get(), get(), get()) }
    viewModel { AnalyticsViewModel(get(), get(), get()) }
    viewModel { HistoryViewModel(get(), get()) }
    viewModel { CategoriesViewModel(get(), get()) }
    viewModel { SettingsViewModel(get()) }
}
```

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat: add repository implementations, DataStore preferences, and Koin DI wiring"
```

---

### Task 4: Theme, Design System, and Navigation Shell

**Goal:** Create the Material 3 theme with dynamic colors, the bottom navigation shell, and the navigation graph connecting all screens.

**Files:**
- Create: `app/src/main/java/com/spendwise/app/ui/theme/Color.kt`
- Create: `app/src/main/java/com/spendwise/app/ui/theme/Theme.kt`
- Create: `app/src/main/java/com/spendwise/app/ui/theme/Type.kt`
- Create: `app/src/main/java/com/spendwise/app/ui/navigation/Screen.kt`
- Create: `app/src/main/java/com/spendwise/app/ui/navigation/AppNavGraph.kt`
- Create: `app/src/main/java/com/spendwise/app/ui/navigation/BottomNavBar.kt`
- Create: `app/src/main/java/com/spendwise/app/ui/MainActivity.kt`

**Acceptance Criteria:**
- [ ] Dynamic color theming on Android 12+ with fallback green palette
- [ ] Dark mode toggles correctly
- [ ] Bottom nav with 4 tabs navigates between placeholder screens
- [ ] Type-safe routes with @Serializable objects

**Verify:** Build and run → Bottom nav works, theme applies

**Steps:**

- [ ] **Step 1: Write Color.kt**

```kotlin
package com.spendwise.app.ui.theme

import androidx.compose.ui.graphics.Color

// Primary — Green (money/finance)
val Green40 = Color(0xFF2E7D32)
val Green80 = Color(0xFFA5D6A7)
val GreenGrey40 = Color(0xFF4E6E50)
val GreenGrey80 = Color(0xFFB8CCB9)

// Secondary — Blue (trust)
val Blue40 = Color(0xFF1565C0)
val Blue80 = Color(0xFF90CAF9)

// Tertiary — Orange (warnings)
val Orange40 = Color(0xFFE65100)
val Orange80 = Color(0xFFFFCC80)

// Error
val Red40 = Color(0xFFB71C1C)
val Red80 = Color(0xFFEF9A9A)

// Category colors (for charts and icons)
val CategoryColors = listOf(
    Color(0xFF4CAF50), // Green
    Color(0xFF2196F3), // Blue
    Color(0xFFFF9800), // Orange
    Color(0xFFE91E63), // Pink
    Color(0xFFF44336), // Red
    Color(0xFF9C27B0), // Purple
    Color(0xFF8BC34A), // Light Green
    Color(0xFF607D8B), // Blue Grey
    Color(0xFF00BCD4), // Cyan
    Color(0xFFFFEB3B), // Yellow
    Color(0xFF795548), // Brown
    Color(0xFF3F51B5), // Indigo
)
```

- [ ] **Step 2: Write Theme.kt**

```kotlin
package com.spendwise.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Green40,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = Blue40,
    tertiary = Orange40,
    error = Red40
)

private val DarkColorScheme = darkColorScheme(
    primary = Green80,
    secondary = Blue80,
    tertiary = Orange80,
    error = Red80
)

@Composable
fun SpendWiseTheme(
    themeMode: String = "system",
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SpendWiseTypography,
        content = content
    )
}
```

- [ ] **Step 3: Write Type.kt**

```kotlin
package com.spendwise.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val SpendWiseTypography = Typography(
    displayMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)
```

- [ ] **Step 4: Write Screen.kt (type-safe routes)**

```kotlin
package com.spendwise.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

@Serializable object HomeRoute
@Serializable object AnalyticsRoute
@Serializable object HistoryRoute
@Serializable object CategoriesRoute
@Serializable object SettingsRoute
@Serializable object AddExpenseRoute
@Serializable data class EditExpenseRoute(val expenseId: Long)

data class BottomNavItem(
    val label: String,
    val route: Any,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("Home", HomeRoute, Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem("Analytics", AnalyticsRoute, Icons.Filled.PieChart, Icons.Outlined.PieChart),
    BottomNavItem("History", HistoryRoute, Icons.Filled.Receipt, Icons.Outlined.Receipt),
    BottomNavItem("Categories", CategoriesRoute, Icons.Filled.Category, Icons.Outlined.Category)
)
```

- [ ] **Step 5: Write BottomNavBar.kt and AppNavGraph.kt**

```kotlin
// navigation/BottomNavBar.kt
package com.spendwise.app.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route::class.qualifiedName
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
```

```kotlin
// navigation/AppNavGraph.kt
package com.spendwise.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    var showAddExpenseSheet by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = { BottomNavBar(navController) },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = { showAddExpenseSheet = true }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add expense")
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<HomeRoute> {
                // HomeScreen(navController) — placeholder for now
                PlaceholderScreen("Home")
            }
            composable<AnalyticsRoute> {
                PlaceholderScreen("Analytics")
            }
            composable<HistoryRoute> {
                PlaceholderScreen("History")
            }
            composable<CategoriesRoute> {
                PlaceholderScreen("Categories")
            }
            composable<SettingsRoute> {
                PlaceholderScreen("Settings")
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(name: String) {
    Box(Modifier.fillMaxSize()) {
        Text(name, style = MaterialTheme.typography.displayMedium)
    }
}
```

- [ ] **Step 6: Write MainActivity.kt**

```kotlin
package com.spendwise.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.spendwise.app.domain.repository.UserPreferencesRepository
import com.spendwise.app.ui.navigation.AppNavGraph
import com.spendwise.app.ui.theme.SpendWiseTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val prefsRepository: UserPreferencesRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by prefsRepository.themeMode.collectAsState(initial = "system")
            val dynamicColor by prefsRepository.isDynamicColor.collectAsState(initial = true)

            SpendWiseTheme(themeMode = themeMode, dynamicColor = dynamicColor) {
                AppNavGraph()
            }
        }
    }
}
```

- [ ] **Step 7: Build, verify navigation, and commit**

```bash
./gradlew assembleDebug
git add -A
git commit -m "feat: add Material 3 theme with dynamic colors and bottom navigation shell"
```

---

### Task 5: Add Expense Screen — Quick Entry and Detailed Mode

**Goal:** Build the dual-mode Add Expense flow.

*(Full implementation code follows the same pattern as above — ViewModel with UiState, Compose screens, unit tests. Due to length, key architectural decisions are noted here. The implementer should follow the UI mockup from the design doc.)*

**Key implementation details:**
- `AddExpenseSheet.kt`: `ModalBottomSheet` with amount field (auto-focused, numeric keyboard), `FlowRow` of `FilterChip` for categories, Save button, "Detailed" `TextButton`
- `AddExpenseDetailScreen.kt`: Full screen form with all fields. `DatePickerDialog` for date. `SingleChoiceSegmentedButtonRow` for payment method. `InputChip` flow for tags.
- `AddExpenseViewModel.kt`: Holds `MutableStateFlow<AddExpenseUiState>`, validates inputs, calls `ExpenseRepository.addExpense()`, calls `CheckBudgetAlertUseCase` after save.

**Steps follow the same TDD pattern as previous tasks: write test → verify fail → implement → verify pass → commit.**

---

### Task 6–10: UI Screens

*(Tasks 6–10 follow identical structure: ViewModel with UiState, Compose screen, unit tests. Each screen's specific UI components and interactions are detailed in the UI/UX design section above. The implementer should reference the ASCII mockups for layout guidance.)*

**Task 6 (Home):** `MonthSummaryCard` with `LinearProgressIndicator`, `LazyRow` of category chips, `LazyColumn` of `ListItem` for recent expenses, swipe-to-delete via `SwipeToDismissBox`.

**Task 7 (Analytics):** Vico `CartesianChart` for bar charts, custom `Canvas` composable for donut chart (Vico's pie chart or custom draw), `LinearProgressIndicator` for budget vs actual.

**Task 8 (History):** Material 3 `SearchBar`, `LazyRow` of `FilterChip`, `LazyColumn` with `stickyHeader` for date groups, `ModalBottomSheet` for expense detail.

**Task 9 (Categories):** `LazyColumn` of `ElevatedCard`, `ModalBottomSheet` for add/edit with `LazyVerticalGrid` icon picker and color picker.

**Task 10 (Settings):** Preference-style layout with `RadioButton` groups, `Switch` toggles, `AnimatedVisibility` for salary day picker.

---

### Task 11: UPI SMS Parsing — BroadcastReceiver and Bank SMS Parser

**Goal:** Implement UPI transaction auto-detection via SMS parsing.

**Files:**
- Create: `app/src/main/java/com/spendwise/app/sms/SmsBroadcastReceiver.kt`
- Create: `app/src/main/java/com/spendwise/app/sms/BankSmsParser.kt`
- Create: `app/src/main/java/com/spendwise/app/sms/SmsTransaction.kt`
- Create: `app/src/main/java/com/spendwise/app/sms/BankPatterns.kt`
- Create: `app/src/main/java/com/spendwise/app/sms/VpaCategoryMapper.kt`
- Test: `app/src/test/java/com/spendwise/app/sms/BankSmsParserTest.kt`
- Test: `app/src/test/java/com/spendwise/app/sms/VpaCategoryMapperTest.kt`

**Acceptance Criteria:**
- [ ] Parser extracts amount, type, date, VPA, UPI ref from SMS of SBI, HDFC, ICICI, Axis, Kotak, Paytm
- [ ] VPA mapper auto-categorizes known merchants
- [ ] Deduplication by UPI reference number
- [ ] Unit tests cover all bank SMS patterns

**Verify:** `./gradlew test` → All SMS parser tests pass

**Steps:**

- [ ] **Step 1: Write SmsTransaction data class**

```kotlin
package com.spendwise.app.sms

data class SmsTransaction(
    val amount: Double,
    val type: TransactionType,
    val date: String?,
    val merchantVpa: String?,
    val upiRefId: String?,
    val bankName: String?,
    val accountLast4: String?,
    val rawText: String
)

enum class TransactionType { DEBIT, CREDIT }
```

- [ ] **Step 2: Write BankPatterns.kt with regex patterns**

```kotlin
package com.spendwise.app.sms

object BankPatterns {
    val KNOWN_SENDER_IDS = setOf(
        "SBIBNK", "SBIPSG", "SBIINB",
        "HDFCBK", "HDFCBN",
        "ICICIB", "ICICBA",
        "AXISBK", "AXBNKL",
        "KOTAKB", "KOTKBK",
        "PYTMPB", "PAYTMB",
        "PNBSMS", "BOBNKK"
    )

    val AMOUNT_PATTERN = Regex(
        """(?:Rs\.?|INR|₹)\s*([\d,]+\.?\d{0,2})""",
        RegexOption.IGNORE_CASE
    )

    val DEBIT_KEYWORDS = Regex(
        """(?:debited|sent|paid|transferred)""",
        RegexOption.IGNORE_CASE
    )

    val CREDIT_KEYWORDS = Regex(
        """(?:credited|received)""",
        RegexOption.IGNORE_CASE
    )

    val UPI_REF_PATTERN = Regex(
        """(?:UPI\s*)?(?:Ref|Reference)\s*(?:No\.?)?\s*[:.]?\s*(\d{12})""",
        RegexOption.IGNORE_CASE
    )

    val VPA_PATTERN = Regex(
        """([a-zA-Z0-9._-]+@[a-zA-Z]{2,})"""
    )

    val ACCOUNT_PATTERN = Regex(
        """(?:a/?c|acct?|account)\s*(?:no\.?\s*)?(?:XX?|\*{2})(\d{4})""",
        RegexOption.IGNORE_CASE
    )
}
```

- [ ] **Step 3: Write BankSmsParser.kt**

```kotlin
package com.spendwise.app.sms

object BankSmsParser {
    fun parse(smsBody: String, senderId: String?): SmsTransaction? {
        // Check if sender is a known bank
        val isKnownBank = senderId?.let { id ->
            BankPatterns.KNOWN_SENDER_IDS.any { id.contains(it, ignoreCase = true) }
        } ?: false

        // Extract amount
        val amountMatch = BankPatterns.AMOUNT_PATTERN.find(smsBody) ?: return null
        val amount = amountMatch.groupValues[1].replace(",", "").toDoubleOrNull() ?: return null

        // Determine transaction type
        val type = when {
            BankPatterns.DEBIT_KEYWORDS.containsMatchIn(smsBody) -> TransactionType.DEBIT
            BankPatterns.CREDIT_KEYWORDS.containsMatchIn(smsBody) -> TransactionType.CREDIT
            else -> return null
        }

        // Extract UPI reference
        val upiRef = BankPatterns.UPI_REF_PATTERN.find(smsBody)?.groupValues?.get(1)

        // Extract VPA
        val vpa = BankPatterns.VPA_PATTERN.find(smsBody)?.groupValues?.get(1)

        // Extract account last 4
        val accountLast4 = BankPatterns.ACCOUNT_PATTERN.find(smsBody)?.groupValues?.get(1)

        // Determine bank from sender ID
        val bankName = when {
            senderId?.contains("SBI", ignoreCase = true) == true -> "SBI"
            senderId?.contains("HDFC", ignoreCase = true) == true -> "HDFC"
            senderId?.contains("ICICI", ignoreCase = true) == true -> "ICICI"
            senderId?.contains("AXIS", ignoreCase = true) == true -> "Axis"
            senderId?.contains("KOTAK", ignoreCase = true) == true -> "Kotak"
            senderId?.contains("PAYTM", ignoreCase = true) == true -> "Paytm"
            else -> null
        }

        return SmsTransaction(
            amount = amount,
            type = type,
            date = null, // date parsing is bank-specific, handled by caller if needed
            merchantVpa = vpa,
            upiRefId = upiRef,
            bankName = bankName,
            accountLast4 = accountLast4,
            rawText = smsBody
        )
    }
}
```

- [ ] **Step 4: Write VpaCategoryMapper.kt**

```kotlin
package com.spendwise.app.sms

object VpaCategoryMapper {
    private val VPA_CATEGORY_MAP = mapOf(
        "swiggy" to "Food & Dining",
        "zomato" to "Food & Dining",
        "dominos" to "Food & Dining",
        "mcdonald" to "Food & Dining",
        "ola" to "Transport",
        "uber" to "Transport",
        "rapido" to "Transport",
        "metro" to "Transport",
        "amazon" to "Shopping",
        "flipkart" to "Shopping",
        "myntra" to "Shopping",
        "ajio" to "Shopping",
        "bigbasket" to "Groceries",
        "blinkit" to "Groceries",
        "zepto" to "Groceries",
        "dmart" to "Groceries",
        "jio" to "Bills & Utilities",
        "airtel" to "Bills & Utilities",
        "vi." to "Bills & Utilities",
        "bsnl" to "Bills & Utilities",
        "electricity" to "Bills & Utilities",
        "gas" to "Bills & Utilities",
        "netflix" to "Entertainment",
        "spotify" to "Entertainment",
        "hotstar" to "Entertainment",
        "prime" to "Entertainment",
        "irctc" to "Transport",
        "makemytrip" to "Transport",
        "pharmeasy" to "Health",
        "netmeds" to "Health",
        "apollo" to "Health"
    )

    fun categorize(vpa: String?): String? {
        if (vpa == null) return null
        val lower = vpa.lowercase()
        return VPA_CATEGORY_MAP.entries.firstOrNull { lower.contains(it.key) }?.value
    }
}
```

- [ ] **Step 5: Write SmsBroadcastReceiver.kt**

```kotlin
package com.spendwise.app.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.spendwise.app.domain.model.ExpenseSource
import com.spendwise.app.domain.model.PaymentMethod
import com.spendwise.app.domain.repository.CategoryRepository
import com.spendwise.app.domain.repository.ExpenseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SmsBroadcastReceiver : BroadcastReceiver(), KoinComponent {
    private val expenseRepository: ExpenseRepository by inject()
    private val categoryRepository: CategoryRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val fullBody = messages.joinToString("") { it.messageBody }
        val senderId = messages.firstOrNull()?.originatingAddress

        val transaction = BankSmsParser.parse(fullBody, senderId) ?: return
        if (transaction.type != TransactionType.DEBIT) return // only track expenses

        CoroutineScope(Dispatchers.IO).launch {
            // Dedup check
            if (transaction.upiRefId != null) {
                val existing = expenseRepository.getByUpiRef(transaction.upiRefId)
                if (existing != null) return@launch
            }

            // Auto-categorize
            val categoryName = VpaCategoryMapper.categorize(transaction.merchantVpa)
            val category = categoryName?.let { categoryRepository.getCategoryByName(it) }

            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val expense = com.spendwise.app.domain.model.Expense(
                amount = transaction.amount,
                category = category,
                description = transaction.merchantVpa ?: "",
                date = now,
                paymentMethod = PaymentMethod.UPI,
                upiRefId = transaction.upiRefId,
                merchantVpa = transaction.merchantVpa,
                source = ExpenseSource.SMS
            )
            expenseRepository.addExpense(expense)
        }
    }
}
```

- [ ] **Step 6: Write tests**

```kotlin
package com.spendwise.app.sms

import org.junit.Assert.*
import org.junit.Test

class BankSmsParserTest {

    @Test
    fun `parse SBI debit SMS`() {
        val sms = "Dear Customer, your A/c no. XXXXXXXX1234 is debited for Rs.500.00 on 02-05-2026 by UPI ref no 123456789012. If not done by you, call 1800111109."
        val result = BankSmsParser.parse(sms, "XX-SBIBNK")

        assertNotNull(result)
        assertEquals(500.0, result!!.amount, 0.01)
        assertEquals(TransactionType.DEBIT, result.type)
        assertEquals("123456789012", result.upiRefId)
        assertEquals("SBI", result.bankName)
    }

    @Test
    fun `parse HDFC debit SMS with VPA`() {
        val sms = "UPDATE: Rs 250.00 debited from a/c **1234 on 02-May-26 to VPA merchant@ybl. UPI Ref No 123456789012. Not you? Call on 18002586161"
        val result = BankSmsParser.parse(sms, "XX-HDFCBK")

        assertNotNull(result)
        assertEquals(250.0, result!!.amount, 0.01)
        assertEquals(TransactionType.DEBIT, result.type)
        assertEquals("merchant@ybl", result.merchantVpa)
        assertEquals("123456789012", result.upiRefId)
        assertEquals("1234", result.accountLast4)
    }

    @Test
    fun `parse ICICI debit SMS`() {
        val sms = "Dear Customer, Acct XX1234 debited with INR 750.00 on 02-May-26 Info: UPI/123456789012/Payment to merchant. Available bal: INR 15000.00. Call 18001200 to report."
        val result = BankSmsParser.parse(sms, "XX-ICICIB")

        assertNotNull(result)
        assertEquals(750.0, result!!.amount, 0.01)
        assertEquals(TransactionType.DEBIT, result.type)
    }

    @Test
    fun `parse credit SMS returns CREDIT type`() {
        val sms = "Your a/c XXXXXXXX1234 credited by Rs.1000.00 on 02-05-2026 by a/c linked to VPA user@sbi ref no 123456789012."
        val result = BankSmsParser.parse(sms, "XX-SBIBNK")

        assertNotNull(result)
        assertEquals(TransactionType.CREDIT, result!!.type)
        assertEquals(1000.0, result.amount, 0.01)
    }

    @Test
    fun `non-financial SMS returns null`() {
        val sms = "Your OTP is 123456. Valid for 5 minutes."
        val result = BankSmsParser.parse(sms, "XX-SBIBNK")
        assertNull(result)
    }

    @Test
    fun `parse Kotak debit SMS`() {
        val sms = "Sent Rs.450.00 from Kotak Bank AC X1234 to receiver@upi on 02-05-26. UPI Ref:123456789012. Call 18602662666 if not done by you"
        val result = BankSmsParser.parse(sms, "XX-KOTAKB")

        assertNotNull(result)
        assertEquals(450.0, result!!.amount, 0.01)
        assertEquals("receiver@upi", result.merchantVpa)
    }
}

class VpaCategoryMapperTest {
    @Test
    fun `swiggy VPA maps to Food`() {
        assertEquals("Food & Dining", VpaCategoryMapper.categorize("swiggy@ybl"))
    }

    @Test
    fun `uber VPA maps to Transport`() {
        assertEquals("Transport", VpaCategoryMapper.categorize("uber@axisbank"))
    }

    @Test
    fun `amazon VPA maps to Shopping`() {
        assertEquals("Shopping", VpaCategoryMapper.categorize("amazonpay@apl"))
    }

    @Test
    fun `unknown VPA returns null`() {
        assertNull(VpaCategoryMapper.categorize("randomuser@upi"))
    }

    @Test
    fun `null VPA returns null`() {
        assertNull(VpaCategoryMapper.categorize(null))
    }
}
```

- [ ] **Step 7: Run tests and commit**

```bash
./gradlew test
git add -A
git commit -m "feat: add UPI SMS parsing with bank-specific patterns and VPA category mapping"
```

---

### Task 12: UPI Notification Listener (Secondary Sync)

**Goal:** NotificationListenerService for Google Pay, PhonePe, Paytm notifications.

*(Follows same pattern as Task 11. Key: parse notification title/text, dedup against SMS transactions by amount + 2-minute time window when UPI ref unavailable.)*

---

### Task 13: Budget Tracking with Alerts

**Goal:** Push notifications at 80% and 100% budget thresholds.

*(CheckBudgetAlertUseCase runs after every expense save. Creates Android notification channel at app startup. NotificationCompat for push alerts.)*

---

### Task 14: Export CSV and Local Backup/Restore

**Goal:** CSV export with share intent, database file backup/restore via SAF.

*(CsvExporter writes to cache dir then shares via FileProvider. DatabaseBackupManager copies Room .db file.)*

---

### Task 15: Polish — Recurring Expenses, Home Widget, and Final Integration

**Goal:** WorkManager for recurring expense auto-creation. Glance widget for quick entry. End-to-end integration testing.

*(RecurringExpenseWorker runs daily via PeriodicWorkRequest. QuickExpenseWidget uses Glance API composables.)*

---

## Feature Priority Summary

| Priority | Features | Tasks |
|----------|----------|-------|
| **P0** | Manual expense entry, category grouping, monthly analytics, salary-day month mode, search/filter | 0-10 |
| **P0** | Budget limits with alerts | 13 |
| **P1** | UPI SMS auto-detection, notification listener | 11-12 |
| **P1** | Export CSV, backup/restore | 14 |
| **P1** | Recurring expenses, home widget, dark mode + dynamic theming | 15, 4 |
| **P2** (future) | Tags/labels, expense splitting, multi-currency | Not in v1 |
