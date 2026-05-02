package com.spendwise.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

@Serializable object OnboardingRoute
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
