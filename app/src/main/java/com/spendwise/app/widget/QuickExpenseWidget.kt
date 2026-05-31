package com.spendwise.app.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.compose.ui.graphics.Color
import androidx.glance.color.ColorProvider
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.spendwise.app.data.local.AppDatabase
import com.spendwise.app.ui.MainActivity
import kotlinx.datetime.*
import java.text.NumberFormat
import java.util.Locale
private val WidgetBackground = ColorProvider(day = Color(0xFF090A0C), night = Color(0xFF090A0C))
private val WidgetMint = ColorProvider(day = Color(0xFF7CF7C8), night = Color(0xFF7CF7C8))
private val WidgetText = ColorProvider(day = Color(0xFFF4F7F8), night = Color(0xFFF4F7F8))
private val WidgetTextMuted = ColorProvider(day = Color(0xFFA9B0BB), night = Color(0xFFA9B0BB))

/**
 * Home-screen widget showing a spending snapshot and quick-add action.
 *
 * Displays:
 * - Today's total spend
 * - This month's total spend
 * - A quick "+ Add Expense" action that opens the app
 *
 * Widget data is loaded directly from Room (without Koin) because
 * Glance widgets run outside the normal Compose lifecycle.
 */
class QuickExpenseWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Load spending data directly from the database
        val db = AppDatabase.create(context)
        val dao = db.expenseDao()
        val tz = TimeZone.currentSystemDefault()

        // Today's range
        val now = Clock.System.now().toLocalDateTime(tz)
        val todayStart = LocalDate(now.year, now.month, now.dayOfMonth)
            .atStartOfDayIn(tz).toEpochMilliseconds()
        val todayEnd = LocalDate(now.year, now.month, now.dayOfMonth)
            .plus(1, DateTimeUnit.DAY)
            .atStartOfDayIn(tz).toEpochMilliseconds() - 1

        // Month range (calendar month)
        val monthStart = LocalDate(now.year, now.month, 1)
            .atStartOfDayIn(tz).toEpochMilliseconds()
        val monthEnd = LocalDate(now.year, now.month, 1)
            .plus(1, DateTimeUnit.MONTH)
            .atStartOfDayIn(tz).toEpochMilliseconds() - 1

        val todayTotal = dao.getCountForDateRange(todayStart, todayEnd).let {
            // We need the sum, not count — use a direct query approach
            // Since we can't easily get Flow value here, compute from entities
            val entities = dao.getRecurringExpenses() // fallback: compute inline
            0.0 // Will be computed below
        }

        // Compute totals from entities (simpler than wiring flows for widget)
        val allExpenses = try {
            // Direct DB query for today and month totals
            val todayExpenses = db.openHelper.readableDatabase.let { rdb ->
                var total = 0.0
                val cursor = rdb.query("SELECT SUM(amount) FROM expenses WHERE date BETWEEN $todayStart AND $todayEnd")
                if (cursor.moveToFirst()) total = cursor.getDouble(0)
                cursor.close()
                total
            }
            val monthExpenses = db.openHelper.readableDatabase.let { rdb ->
                var total = 0.0
                val cursor = rdb.query("SELECT SUM(amount) FROM expenses WHERE date BETWEEN $monthStart AND $monthEnd")
                if (cursor.moveToFirst()) total = cursor.getDouble(0)
                cursor.close()
                total
            }
            Pair(todayExpenses, monthExpenses)
        } catch (_: Exception) {
            Pair(0.0, 0.0)
        }

        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .background(WidgetBackground)
                    .clickable(actionStartActivity<MainActivity>()),
                verticalAlignment = Alignment.Top,
                horizontalAlignment = Alignment.Start
            ) {
                // App name
                Text(
                    text = "SpendWise",
                    style = TextStyle(
                        color = WidgetMint,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = GlanceModifier.height(8.dp))

                // Today's spend
                Text(
                    text = "Today",
                    style = TextStyle(
                        color = WidgetTextMuted,
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = currencyFormat.format(allExpenses.first),
                    style = TextStyle(
                        color = WidgetText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = GlanceModifier.height(4.dp))

                // Month total
                Text(
                    text = "This month: ${currencyFormat.format(allExpenses.second)}",
                    style = TextStyle(
                        color = WidgetTextMuted,
                        fontSize = 12.sp
                    )
                )

                Spacer(modifier = GlanceModifier.height(8.dp).defaultWeight())

                // Add button
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "+ Add Expense",
                        style = TextStyle(
                            color = WidgetMint,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

class QuickExpenseWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickExpenseWidget()
}
