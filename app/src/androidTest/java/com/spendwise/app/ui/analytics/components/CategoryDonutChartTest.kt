package com.spendwise.app.ui.analytics.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.spendwise.app.domain.model.Category
import com.spendwise.app.domain.model.CategorySpend
import org.junit.Rule
import org.junit.Test

class CategoryDonutChartTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testDonutChartLegendShowsCategoriesAndPercentages() {
        val breakdown = listOf(
            CategorySpend(
                category = Category(id = 1, name = "Food", icon = "🍔", colorHex = "#FF5722"),
                amount = 100.0,
                percentage = 50.0,
                budgetLimit = null
            ),
            CategorySpend(
                category = Category(id = -1, name = "Uncategorized", icon = "❓", colorHex = "#9E9E9E"),
                amount = 100.0,
                percentage = 50.0,
                budgetLimit = null
            )
        )

        composeTestRule.setContent {
            CategoryDonutChart(
                breakdown = breakdown,
                totalAmount = 200.0,
                tagBreakdowns = emptyMap()
            )
        }

        // Check if Food and Uncategorized legends are displayed
        composeTestRule.onNodeWithText("Food").assertIsDisplayed()
        composeTestRule.onNodeWithText("Uncategorized").assertIsDisplayed()

        // Check if percentages are displayed
        composeTestRule.onNodeWithText("50%").assertIsDisplayed()
    }
}
