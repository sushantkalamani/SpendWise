package com.spendwise.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.spendwise.app.domain.repository.UserPreferencesRepository
import com.spendwise.app.ui.addexpense.AddExpenseSheet
import com.spendwise.app.ui.addexpense.AddExpenseDetailScreen
import com.spendwise.app.ui.addexpense.AddExpenseViewModel
import com.spendwise.app.ui.addexpense.EditExpenseScreen
import com.spendwise.app.ui.analytics.AnalyticsScreen
import com.spendwise.app.ui.analytics.AnalyticsViewModel
import com.spendwise.app.ui.categories.CategoriesScreen
import com.spendwise.app.ui.categories.CategoriesViewModel
import com.spendwise.app.ui.history.HistoryScreen
import com.spendwise.app.ui.history.HistoryViewModel
import com.spendwise.app.ui.home.HomeScreen
import com.spendwise.app.ui.home.HomeViewModel
import com.spendwise.app.ui.onboarding.OnboardingScreen
import com.spendwise.app.ui.onboarding.OnboardingViewModel
import com.spendwise.app.ui.settings.SettingsScreen
import com.spendwise.app.ui.settings.SettingsViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

/**
 * Root composable that gates on onboarding status and hosts the main navigation.
 *
 * After onboarding, [MainAppContent] provides the scaffold with bottom nav,
 * FAB, and a [NavHost] for all screens including the new Edit Expense route.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph() {
    val prefsRepository: UserPreferencesRepository = koinInject()
    val isOnboardingComplete by prefsRepository.isOnboardingComplete.collectAsState(initial = null)

    when (isOnboardingComplete) {
        null -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        false -> {
            val viewModel: OnboardingViewModel = koinViewModel()
            OnboardingScreen(viewModel = viewModel, onComplete = { })
        }
        true -> {
            MainAppContent()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainAppContent() {
    val navController = rememberNavController()
    var showAddExpenseSheet by remember { mutableStateOf(false) }
    var showDetailedAdd by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddExpenseSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.AddCard, contentDescription = "Add expense")
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<HomeRoute> {
                val viewModel: HomeViewModel = koinViewModel()
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = { navController.navigate(SettingsRoute) },
                    onEditExpense = { expenseId ->
                        navController.navigate(EditExpenseRoute(expenseId))
                    },
                    onDuplicateExpense = { expenseId ->
                        // Open add screen pre-filled with duplicate data
                        showDetailedAdd = true
                        // The AddExpenseViewModel for the detail screen will handle this
                    }
                )
            }
            composable<AnalyticsRoute> {
                val viewModel: AnalyticsViewModel = koinViewModel()
                AnalyticsScreen(
                    viewModel = viewModel,
                    onNavigateToHistory = { searchQuery ->
                        navController.navigate(HistoryRoute(searchQuery = searchQuery))
                    }
                )
            }
            composable<HistoryRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<HistoryRoute>()
                val viewModel: HistoryViewModel = koinViewModel()
                HistoryScreen(
                    viewModel = viewModel,
                    initialSearchQuery = route.searchQuery,
                    onEditExpense = { expenseId ->
                        navController.navigate(EditExpenseRoute(expenseId))
                    },
                    onDuplicateExpense = { expenseId ->
                        showDetailedAdd = true
                    }
                )
            }
            composable<CategoriesRoute> {
                val viewModel: CategoriesViewModel = koinViewModel()
                CategoriesScreen(viewModel = viewModel)
            }
            composable<SettingsRoute> {
                val viewModel: SettingsViewModel = koinViewModel()
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Edit Expense screen — navigated to from detail sheet
            composable<EditExpenseRoute> { backStackEntry ->
                val route = backStackEntry.arguments?.getLong("expenseId") ?: return@composable
                val addViewModel: AddExpenseViewModel = koinViewModel()
                LaunchedEffect(route) {
                    addViewModel.loadExpenseForEdit(route)
                }
                EditExpenseScreen(
                    viewModel = addViewModel,
                    onDismiss = { navController.popBackStack() }
                )
            }
        }
    }

    if (showAddExpenseSheet && !showDetailedAdd) {
        val addViewModel: AddExpenseViewModel = koinViewModel()
        AddExpenseSheet(
            viewModel = addViewModel,
            onDismiss = { showAddExpenseSheet = false },
            onExpandToDetail = {
                showAddExpenseSheet = false
                showDetailedAdd = true
            }
        )
    }

    if (showDetailedAdd) {
        val addViewModel: AddExpenseViewModel = koinViewModel()
        AddExpenseDetailScreen(
            viewModel = addViewModel,
            onDismiss = { showDetailedAdd = false }
        )
    }
}
