package com.spendwise.app.ui.analytics.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spendwise.app.domain.model.CategorySpend
import com.spendwise.app.ui.analytics.TagSpend
import com.spendwise.app.ui.theme.CategoryColors
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Interactive donut chart with clickable arcs and legend rows.
 *
 * Tapping a donut segment or its legend row expands an inline tag-wise
 * breakdown below that legend entry. Tag rows include search (navigate
 * to History) and rename action buttons.
 *
 * @param breakdown List of [CategorySpend] for the current period.
 * @param totalAmount Total spend for the center label.
 * @param tagBreakdowns Map of category ID → list of [TagSpend].
 * @param expandedCategoryId Currently expanded category ID, or null.
 * @param onCategoryClick Called when a category is tapped (arc or legend).
 * @param onTagSearch Called when the search icon next to a tag is tapped.
 * @param onTagRename Called when the edit icon next to a tag is tapped.
 * @param modifier Optional [Modifier].
 */
@Composable
fun CategoryDonutChart(
    breakdown: List<CategorySpend>,
    totalAmount: Double,
    tagBreakdowns: Map<Long, List<TagSpend>> = emptyMap(),
    expandedCategoryId: Long? = null,
    onCategoryClick: (Long) -> Unit = {},
    onTagSearch: (String) -> Unit = {},
    onTagRename: (categoryId: Long, oldTag: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply { maximumFractionDigits = 0 }

    // Pre-compute arc angle ranges for tap detection
    val arcRanges = remember(breakdown) {
        val ranges = mutableListOf<Triple<Long, Float, Float>>() // (categoryId, startAngle, endAngle)
        var startAngle = -90f
        breakdown.forEach { spend ->
            val sweep = (spend.percentage / 100 * 360).toFloat()
            ranges.add(Triple(spend.category.id, startAngle, startAngle + sweep))
            startAngle += sweep
        }
        ranges
    }

    Column(modifier = modifier.animateContentSize(animationSpec = tween(300)), horizontalAlignment = Alignment.CenterHorizontally) {
        // ---- Donut ----
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
            Canvas(
                modifier = Modifier
                    .size(200.dp)
                    .pointerInput(arcRanges) {
                        detectTapGestures { offset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val dx = offset.x - center.x
                            val dy = offset.y - center.y
                            val distance = sqrt(dx * dx + dy * dy)
                            val strokeWidth = 32.dp.toPx()
                            val maxStroke = strokeWidth + 6.dp.toPx()
                            val radius = size.width / 2f
                            val strokeCenter = radius - maxStroke / 2f
                            val innerLimit = strokeCenter - strokeWidth / 2f
                            val outerLimit = strokeCenter + maxStroke / 2f

                            // Only register taps on the donut ring
                            if (distance in innerLimit..outerLimit) {
                                // atan2 gives angle in radians; convert to degrees matching Canvas convention
                                var angleDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                // Normalize to match our -90° start
                                // angleDeg is 0° at 3 o'clock, we need to find the matching arc
                                for ((catId, start, end) in arcRanges) {
                                    val normStart = ((start % 360) + 360) % 360
                                    val normEnd = ((end % 360) + 360) % 360
                                    val normAngle = ((angleDeg % 360) + 360) % 360
                                    val inArc = if (normStart <= normEnd) {
                                        normAngle in normStart..normEnd
                                    } else {
                                        normAngle >= normStart || normAngle <= normEnd
                                    }
                                    if (inArc) {
                                        onCategoryClick(catId)
                                        break
                                    }
                                }
                            }
                        }
                    }
            ) {
                val strokeWidth = 32.dp.toPx()
                val maxStroke = strokeWidth + 6.dp.toPx()
                val inset = maxStroke / 2f
                val arcSize = Size(size.width - maxStroke, size.height - maxStroke)
                val arcTopLeft = Offset(inset, inset)
                var startAngle = -90f
                breakdown.forEachIndexed { index, spend ->
                    val sweep = (spend.percentage / 100 * 360).toFloat()
                    val isSelected = spend.category.id == expandedCategoryId
                    val color = try {
                        Color(android.graphics.Color.parseColor(spend.category.colorHex))
                    } catch (_: Exception) {
                        CategoryColors[index % CategoryColors.size]
                    }
                    drawArc(
                        color = if (isSelected) color else color.copy(alpha = if (expandedCategoryId != null) 0.4f else 1f),
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(
                            width = if (isSelected) strokeWidth + 6.dp.toPx() else strokeWidth,
                            cap = StrokeCap.Butt
                        )
                    )
                    startAngle += sweep
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(currencyFormat.format(totalAmount), style = MaterialTheme.typography.titleMedium)
                Text("total", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(16.dp))

        // ---- Legend with expandable tag breakdown ----
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            breakdown.forEachIndexed { index, spend ->
                val color = try {
                    Color(android.graphics.Color.parseColor(spend.category.colorHex))
                } catch (_: Exception) {
                    CategoryColors[index % CategoryColors.size]
                }
                val isExpanded = spend.category.id == expandedCategoryId
                val chevronRotation by animateFloatAsState(
                    targetValue = if (isExpanded) 180f else 0f,
                    animationSpec = tween(300),
                    label = "chevron_${spend.category.id}"
                )

                // Legend row — clickable
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCategoryClick(spend.category.id) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Canvas(Modifier.size(12.dp)) { drawCircle(color) }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        spend.category.name,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = if (isExpanded) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Text("${spend.percentage.toInt()}%", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.width(8.dp))
                    Text(currencyFormat.format(spend.amount), style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        modifier = Modifier.size(16.dp).rotate(chevronRotation),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Expanded tag breakdown
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(animationSpec = tween(300)),
                    exit = shrinkVertically(animationSpec = tween(250))
                ) {
                    val tags = tagBreakdowns[spend.category.id] ?: emptyList()
                    Column(
                        modifier = Modifier.padding(start = 36.dp, end = 16.dp, bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (tags.isEmpty()) {
                            Text(
                                "No tag data available",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            // Multi-tag warning
                            if (tags.sumOf { it.amount } > spend.amount * 1.01) {
                                Surface(
                                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                                    shape = MaterialTheme.shapes.small,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        "ⓘ Multi-tag expenses may cause tag totals to exceed category total.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            tags.forEach { tagSpend ->
                                // Tag row
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            tagSpend.tag,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            currencyFormat.format(tagSpend.amount),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                        Text(
                                            "${tagSpend.percentage.toInt()}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        IconButton(onClick = { onTagSearch(tagSpend.tag) }, modifier = Modifier.size(24.dp)) {
                                            Icon(
                                                Icons.Filled.Search,
                                                contentDescription = "View ${tagSpend.tag} transactions",
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        if (tagSpend.tag != "Untagged") {
                                            IconButton(onClick = { onTagRename(spend.category.id, tagSpend.tag) }, modifier = Modifier.size(24.dp)) {
                                                Icon(
                                                    Icons.Filled.Edit,
                                                    contentDescription = "Rename ${tagSpend.tag}",
                                                    modifier = Modifier.size(14.dp),
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        } else {
                                            Spacer(Modifier.size(24.dp))
                                        }
                                    }
                                    // Progress bar
                                    LinearProgressIndicator(
                                        progress = { (tagSpend.percentage / 100f).toFloat().coerceIn(0f, 1f) },
                                        modifier = Modifier.fillMaxWidth().height(5.dp),
                                        color = color.copy(alpha = 0.7f),
                                        trackColor = color.copy(alpha = 0.12f),
                                        strokeCap = StrokeCap.Round
                                    )
                                }
                            }
                        }
                    }
                }

                if (index < breakdown.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}
