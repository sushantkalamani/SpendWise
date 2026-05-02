package com.spendwise.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.spendwise.app.domain.repository.UserPreferencesRepository
import com.spendwise.app.ui.addexpense.AddExpenseSheet
import com.spendwise.app.ui.addexpense.AddExpenseDetailScreen
import com.spendwise.app.ui.addexpense.AddExpenseViewModel
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
        bottomBar = { BottomNavBar(navController) },
        floatingActionButton = {
            LargeFloatingActionButton(onClick = { showAddExpenseSheet = true }) {
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
                val viewModel: HomeViewModel = koinViewModel()
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = { navController.navigate(SettingsRoute) }
                )
            }
            composable<AnalyticsRoute> {
                val viewModel: AnalyticsViewModel = koinViewModel()
                AnalyticsScreen(viewModel = viewModel)
            }
            composable<HistoryRoute> {
                val viewModel: HistoryViewModel = koinViewModel()
                HistoryScreen(viewModel = viewModel)
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
