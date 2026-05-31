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
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val preferencesRepository: UserPreferencesRepository = koinInject()
            val themeMode by preferencesRepository.themeMode.collectAsState(initial = "system")
            val dynamicColor by preferencesRepository.isDynamicColor.collectAsState(initial = true)

            SpendWiseTheme(themeMode = themeMode, dynamicColor = dynamicColor) {
                AppNavGraph()
            }
        }
    }
}
