package com.spendwise.app.ui.settings

import com.spendwise.app.data.backup.DatabaseBackupManager
import com.spendwise.app.data.export.CsvExporter
import com.spendwise.app.data.export.CsvImporter
import com.spendwise.app.domain.model.Category
import com.spendwise.app.domain.model.Expense
import com.spendwise.app.domain.repository.CategoryRepository
import com.spendwise.app.domain.repository.ExpenseRepository
import com.spendwise.app.domain.repository.UserPreferencesRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import kotlinx.datetime.LocalDateTime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val prefsRepository: UserPreferencesRepository = mockk(relaxed = true)
    private val expenseRepository: ExpenseRepository = mockk(relaxed = true)
    private val categoryRepository: CategoryRepository = mockk(relaxed = true)
    private val csvExporter: CsvExporter = mockk(relaxed = true)
    private val csvImporter: CsvImporter = mockk(relaxed = true)
    private val backupManager: DatabaseBackupManager = mockk(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Mock default flow responses for init
        coEvery { prefsRepository.isCalendarMode } returns flowOf(false)
        coEvery { prefsRepository.salaryDay } returns flowOf(1)
        coEvery { prefsRepository.themeMode } returns flowOf("system")
        coEvery { prefsRepository.isDynamicColor } returns flowOf(true)
        coEvery { prefsRepository.reminderEnabled } returns flowOf(false)
        coEvery { prefsRepository.reminderHour } returns flowOf(20)
        coEvery { prefsRepository.reminderMinute } returns flowOf(0)
        coEvery { prefsRepository.monthlyIncome } returns flowOf(null)

        viewModel = SettingsViewModel(
            prefsRepository,
            expenseRepository,
            categoryRepository,
            csvExporter,
            csvImporter,
            backupManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `confirmImport should create missing categories and import expenses`() = runTest {
        // Arrange
        val existingCategories = listOf(
            Category(id = 1, name = "Food", icon = "Restaurant", colorHex = "#FF5722")
        )
        coEvery { categoryRepository.getAllCategories() } returns flowOf(existingCategories)

        // We'll return 10 for the new category id
        coEvery { categoryRepository.addCategory(any()) } returns 10L

        // No duplicates
        coEvery { expenseRepository.findDuplicate(any(), any(), any(), any()) } returns null

        val validRows = listOf(
            // Existing category
            CsvImporter.ImportRow(
                date = LocalDateTime(2025, 5, 1, 12, 0, 0),
                amount = 100.0,
                categoryName = "Food",
                description = "Lunch",
                lineNumber = 1
            ),
            // New category (first time)
            CsvImporter.ImportRow(
                date = LocalDateTime(2025, 5, 2, 12, 0, 0),
                amount = 200.0,
                categoryName = "Entertainment",
                description = "Movie",
                lineNumber = 2
            ),
            // New category (repeated - should not create category twice)
            CsvImporter.ImportRow(
                date = LocalDateTime(2025, 5, 3, 12, 0, 0),
                amount = 300.0,
                categoryName = "Entertainment",
                description = "Game",
                lineNumber = 3
            ),
            // Uncategorized category
            CsvImporter.ImportRow(
                date = LocalDateTime(2025, 5, 4, 12, 0, 0),
                amount = 400.0,
                categoryName = "Uncategorized",
                description = "Other",
                lineNumber = 4
            )
        )
        val mockResult = CsvImporter.ImportResult(
            validRows = validRows,
            invalidRows = emptyList(),
            totalRows = 4
        )

        val mockUri = mockk<android.net.Uri>()
        coEvery { csvImporter.parseFromUri(mockUri) } returns mockResult

        viewModel.importFromCsv(mockUri)
        testScheduler.advanceUntilIdle()

        // Act
        viewModel.confirmImport()
        testScheduler.advanceUntilIdle()

        // Assert
        // Verify only 1 new category is added ("Entertainment")
        coVerify(exactly = 1) { categoryRepository.addCategory(match { it.name == "Entertainment" }) }
        // Verify Uncategorized is NOT created
        coVerify(exactly = 0) { categoryRepository.addCategory(match { it.name.equals("Uncategorized", ignoreCase = true) }) }

        // Verify expenses are inserted
        val capturedExpenses = mutableListOf<Expense>()
        coVerify(exactly = 4) { expenseRepository.addExpense(capture(capturedExpenses)) }

        assertEquals(4, capturedExpenses.size)
        // First expense maps to Food (id = 1)
        assertEquals(1L, capturedExpenses[0].category?.id)
        // Second and third map to Entertainment (id = 10)
        assertEquals(10L, capturedExpenses[1].category?.id)
        assertEquals(10L, capturedExpenses[2].category?.id)
        // Fourth is Uncategorized (null category)
        assertEquals(null, capturedExpenses[3].category)

        // Verify state/message
        val expectedMsg = "Imported 4 expenses, created 1 categories"
        val importStatus = viewModel.uiState.value.importStatus
        assert(importStatus is OperationStatus.Success)
        assertEquals(expectedMsg, (importStatus as OperationStatus.Success).message)
    }
}
