package com.spendwise.app.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendwise.app.ui.onboarding.components.*

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) onComplete()
    }

    val darkBg = Color(0xFF0D1117)
    val neonGreen = Color(0xFF00E676)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            AnimatedProgressBar(
                currentPage = uiState.currentPage,
                totalPages = 7
            )

            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = uiState.currentPage,
                    transitionSpec = {
                        slideInHorizontally { if (targetState > initialState) it else -it } + fadeIn() togetherWith
                            slideOutHorizontally { if (targetState > initialState) -it else it } + fadeOut()
                    },
                    label = "pageTransition"
                ) { page ->
                    when (page) {
                        0 -> WelcomePage(onGetStarted = { viewModel.nextPage() })
                        1 -> NamePage(
                            name = uiState.userName,
                            onNameChange = viewModel::updateName,
                            isError = uiState.nameError,
                            onNext = { viewModel.nextPage() }
                        )
                        2 -> IncomePage(
                            income = uiState.monthlyIncome,
                            onIncomeChange = viewModel::updateIncome,
                            onNext = { viewModel.nextPage() },
                            onSkip = { viewModel.nextPage() }
                        )
                        3 -> SalaryDayPage(
                            salaryDay = uiState.salaryDay,
                            isCalendarMode = uiState.isCalendarMode,
                            onSalaryDayChange = viewModel::updateSalaryDay,
                            onCalendarModeChange = viewModel::toggleCalendarMode,
                            onNext = { viewModel.nextPage() }
                        )
                        4 -> CategoryPage(
                            categories = uiState.categories,
                            selectedIds = uiState.selectedCategoryIds,
                            onToggle = viewModel::toggleCategory,
                            onNext = { viewModel.nextPage() }
                        )
                        5 -> BudgetPage(
                            budget = uiState.overallBudget,
                            onBudgetChange = viewModel::updateBudget,
                            onNext = { viewModel.nextPage() },
                            onSkip = { viewModel.nextPage() }
                        )
                        6 -> AllSetPage(
                            userName = uiState.userName,
                            onLetsGo = { viewModel.completeOnboarding() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomePage(onGetStarted: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PulsingOrb()
        Spacer(Modifier.height(32.dp))
        GlowingText("SPENDWISE")
        Spacer(Modifier.height(16.dp))
        Text(
            "Your money. Your rules.",
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(48.dp))
        NeonButton("GET STARTED", onClick = onGetStarted)
    }
}

@Composable
private fun NamePage(name: String, onNameChange: (String) -> Unit, isError: Boolean, onNext: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "What should we\ncall you?",
            color = Color.White,
            style = MaterialTheme.typography.displayMedium.copy(fontSize = 28.sp),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(40.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = { Text("Your name...", color = Color.White.copy(alpha = 0.3f)) },
            isError = isError,
            supportingText = if (isError) {{ Text("Please enter your name", color = MaterialTheme.colorScheme.error) }} else null,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF00E676),
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                cursorColor = Color(0xFF00E676)
            ),
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        Spacer(Modifier.height(40.dp))
        NeonButton("NEXT", onClick = onNext)
    }
}

@Composable
private fun IncomePage(income: String, onIncomeChange: (String) -> Unit, onNext: () -> Unit, onSkip: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "How much do you\nearn each month?",
            color = Color.White,
            style = MaterialTheme.typography.displayMedium.copy(fontSize = 28.sp),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text("\u20B9", color = Color(0xFF00E676), style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = income,
            onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) onIncomeChange(it) },
            placeholder = { Text("1,20,000", color = Color.White.copy(alpha = 0.3f)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF00E676),
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                cursorColor = Color(0xFF00E676)
            ),
            modifier = Modifier.fillMaxWidth(0.6f)
        )
        Spacer(Modifier.height(12.dp))
        Text("This helps us show your savings", color = Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(40.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TextButton(onClick = onSkip) { Text("Skip", color = Color.White.copy(alpha = 0.5f)) }
            NeonButton("NEXT", onClick = onNext)
        }
    }
}

@Composable
private fun SalaryDayPage(
    salaryDay: Int, isCalendarMode: Boolean,
    onSalaryDayChange: (Int) -> Unit, onCalendarModeChange: (Boolean) -> Unit, onNext: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "When does your\nsalary hit?",
            color = Color.White,
            style = MaterialTheme.typography.displayMedium.copy(fontSize = 28.sp),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))

        if (!isCalendarMode) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                FilledTonalButton(onClick = { onSalaryDayChange((salaryDay - 1).coerceAtLeast(1)) }) { Text("\u25C4") }
                Text(
                    "$salaryDay",
                    color = Color(0xFF00E676),
                    style = MaterialTheme.typography.displayMedium.copy(fontSize = 48.sp)
                )
                FilledTonalButton(onClick = { onSalaryDayChange((salaryDay + 1).coerceAtMost(28)) }) { Text("\u25BA") }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "Your month runs ${salaryDay}th \u2192 ${salaryDay - 1}th",
                color = Color.White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isCalendarMode,
                onCheckedChange = { onCalendarModeChange(it) },
                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00E676), checkmarkColor = Color.Black)
            )
            Text("I use calendar months (1st \u2192 31st)", color = Color.White, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(40.dp))
        NeonButton("NEXT", onClick = onNext)
    }
}

@Composable
private fun CategoryPage(
    categories: List<com.spendwise.app.domain.model.Category>,
    selectedIds: Set<Long>,
    onToggle: (Long) -> Unit,
    onNext: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        Text(
            "What do you\nspend on?",
            color = Color.White,
            style = MaterialTheme.typography.displayMedium.copy(fontSize = 28.sp),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text("Select at least 3", color = Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(24.dp))

        val neonGreen = Color(0xFF00E676)
        categories.chunked(3).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                row.forEach { cat ->
                    val isSelected = cat.id in selectedIds
                    OutlinedCard(
                        onClick = { onToggle(cat.id) },
                        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(neonGreen)
                        ) else CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color.White.copy(alpha = 0.2f))
                        ),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isSelected) neonGreen.copy(alpha = 0.1f) else Color.Transparent
                        ),
                        modifier = Modifier.width(100.dp).height(80.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(cat.name, color = if (isSelected) neonGreen else Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                            if (isSelected) Text("\u2726", color = neonGreen, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        NeonButton("NEXT", onClick = onNext, modifier = Modifier)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun BudgetPage(budget: String, onBudgetChange: (String) -> Unit, onNext: () -> Unit, onSkip: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Set a monthly\nspending limit",
            color = Color.White,
            style = MaterialTheme.typography.displayMedium.copy(fontSize = 28.sp),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))

        // Animated ring preview
        val budgetVal = budget.toDoubleOrNull() ?: 0.0
        val progress = if (budgetVal > 0) 0.6f else 0f

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
            androidx.compose.foundation.Canvas(modifier = Modifier.size(160.dp)) {
                drawArc(Color.White.copy(alpha = 0.1f), 0f, 360f, false, style = androidx.compose.ui.graphics.drawscope.Stroke(12.dp.toPx()))
                if (progress > 0) {
                    drawArc(Color(0xFF00E676), -90f, 360f * progress, false, style = androidx.compose.ui.graphics.drawscope.Stroke(12.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
                }
            }
            Text(
                if (budgetVal > 0) "\u20B9${budget}" else "\u20B90",
                color = Color(0xFF00E676),
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = budget,
            onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*$"))) onBudgetChange(it) },
            placeholder = { Text("80,000", color = Color.White.copy(alpha = 0.3f)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            prefix = { Text("\u20B9 ", color = Color(0xFF00E676)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF00E676),
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                cursorColor = Color(0xFF00E676)
            ),
            modifier = Modifier.fillMaxWidth(0.6f)
        )
        Spacer(Modifier.height(12.dp))
        Text("We'll alert you at 80%", color = Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(40.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TextButton(onClick = onSkip) { Text("Skip", color = Color.White.copy(alpha = 0.5f)) }
            NeonButton("NEXT", onClick = onNext)
        }
    }
}

@Composable
private fun AllSetPage(userName: String, onLetsGo: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("\u2726  \u2726  \u2726  \u2726", color = Color(0xFF00E676), fontSize = 24.sp)
        Spacer(Modifier.height(8.dp))
        Text("\u2726   \u2726  \u2726   \u2726  \u2726", color = Color(0xFF00E676).copy(alpha = 0.6f), fontSize = 16.sp)
        Spacer(Modifier.height(32.dp))
        GlowingText(
            text = "You're all set,",
            style = MaterialTheme.typography.displayMedium.copy(fontSize = 28.sp)
        )
        GlowingText(
            text = "$userName!",
            style = MaterialTheme.typography.displayMedium.copy(fontSize = 32.sp)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "Let's take control of\nyour finances.",
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(48.dp))
        NeonButton("LET'S GO!", onClick = onLetsGo)
    }
}
