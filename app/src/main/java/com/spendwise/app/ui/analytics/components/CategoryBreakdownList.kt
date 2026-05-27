package com.spendwise.app.ui.analytics.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spendwise.app.domain.model.CategorySpend
import com.spendwise.app.ui.analytics.TagSpend
import com.spendwise.app.ui.theme.CategoryColors
import java.text.NumberFormat
import java.util.Locale

/**
 * Premium expandable category breakdown list for the Analytics screen.
 *
 * Each category row shows the category name, total amount, and percentage.
 * Tapping a row expands it to reveal a tag-wise breakdown with animated
 * horizontal progress bars, search buttons (navigate to History), and
 * rename/edit buttons.
 *
 * @param breakdown List of [CategorySpend] items for the current period.
 * @param tagBreakdowns Map of category ID → list of [TagSpend] for that category.
 * @param expandedCategoryId The currently expanded category ID, or null if none.
 * @param onCategoryClick Called when a category is tapped (toggle expand/collapse).
 * @param onTagSearch Called when the search icon next to a tag is tapped.
 * @param onTagRename Called when the edit icon next to a tag is tapped.
 * @param modifier Optional [Modifier].
 */
@Composable
fun CategoryBreakdownList(
    breakdown: List<CategorySpend>,
    tagBreakdowns: Map<Long, List<TagSpend>>,
    expandedCategoryId: Long?,
    onCategoryClick: (Long) -> Unit,
    onTagSearch: (String) -> Unit,
    onTagRename: (categoryId: Long, oldTag: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }

    Column(
        modifier = modifier.animateContentSize(animationSpec = tween(300)),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        breakdown.forEachIndexed { index, spend ->
            val isExpanded = spend.category.id == expandedCategoryId
            val categoryColor = try {
                Color(android.graphics.Color.parseColor(spend.category.colorHex))
            } catch (_: Exception) {
                CategoryColors[index % CategoryColors.size]
            }

            CategoryRow(
                spend = spend,
                categoryColor = categoryColor,
                isExpanded = isExpanded,
                currencyFormat = currencyFormat,
                onClick = { onCategoryClick(spend.category.id) }
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(250))
            ) {
                val tags = tagBreakdowns[spend.category.id] ?: emptyList()
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 8.dp),
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
                        val hasMultiTag = tags.sumOf { it.amount } > spend.amount * 1.01
                        if (hasMultiTag) {
                            Surface(
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "ⓘ Some expenses have multiple tags, so tag totals may exceed the category total.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        tags.forEach { tagSpend ->
                            TagBreakdownRow(
                                tagSpend = tagSpend,
                                categoryColor = categoryColor,
                                currencyFormat = currencyFormat,
                                onSearch = { onTagSearch(tagSpend.tag) },
                                onRename = {
                                    if (tagSpend.tag != "Untagged") {
                                        onTagRename(spend.category.id, tagSpend.tag)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            if (index < breakdown.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
            }
        }
    }
}

/**
 * A single category row with name, amount, percentage, and animated chevron.
 */
@Composable
private fun CategoryRow(
    spend: CategorySpend,
    categoryColor: Color,
    isExpanded: Boolean,
    currencyFormat: NumberFormat,
    onClick: () -> Unit
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(300),
        label = "chevron"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Color indicator dot
        Canvas(Modifier.size(10.dp)) { drawCircle(categoryColor) }

        // Category name
        Text(
            spend.category.name,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.weight(1f)
        )

        // Amount
        Text(
            currencyFormat.format(spend.amount),
            style = MaterialTheme.typography.bodyMedium
        )

        // Percentage badge
        Surface(
            color = categoryColor.copy(alpha = 0.12f),
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                "${spend.percentage.toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = categoryColor,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        // Animated chevron
        Icon(
            Icons.Filled.KeyboardArrowDown,
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            modifier = Modifier
                .size(20.dp)
                .rotate(chevronRotation),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * A tag row within the expanded category breakdown.
 *
 * Shows a horizontal progress bar proportional to the tag's percentage,
 * the tag name, amount, and action icons for search and rename.
 */
@Composable
private fun TagBreakdownRow(
    tagSpend: TagSpend,
    categoryColor: Color,
    currencyFormat: NumberFormat,
    onSearch: () -> Unit,
    onRename: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Tag name
            Text(
                tagSpend.tag,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )

            // Amount
            Text(
                currencyFormat.format(tagSpend.amount),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Percentage
            Text(
                "${tagSpend.percentage.toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Search button
            IconButton(onClick = onSearch, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = "View transactions for ${tagSpend.tag}",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Rename button (hidden for "Untagged")
            if (tagSpend.tag != "Untagged") {
                IconButton(onClick = onRename, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Rename ${tagSpend.tag}",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Spacer(Modifier.size(28.dp))
            }
        }

        // Progress bar
        LinearProgressIndicator(
            progress = { (tagSpend.percentage / 100f).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = categoryColor.copy(alpha = 0.7f),
            trackColor = categoryColor.copy(alpha = 0.12f),
            strokeCap = StrokeCap.Round
        )
    }
}

/**
 * Dialog for renaming or merging a tag.
 *
 * @param currentTag The tag being renamed.
 * @param onDismiss Called when the dialog is dismissed.
 * @param onConfirm Called with the new tag name when the user confirms.
 */
@Composable
fun RenameTagDialog(
    currentTag: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newTagName by remember { mutableStateOf(currentTag) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Filled.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        title = { Text("Rename Tag") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Rename \"$currentTag\" across all expenses in this category. " +
                    "If the new name matches an existing tag, they will be merged.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = newTagName,
                    onValueChange = { newTagName = it },
                    label = { Text("New tag name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = { onConfirm(newTagName.trim()) },
                enabled = newTagName.trim().isNotBlank() && newTagName.trim() != currentTag
            ) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
