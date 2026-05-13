package com.spendwise.app.data.export

import android.content.Context
import android.net.Uri
import kotlinx.datetime.LocalDateTime

/**
 * Parses a CSV file (from a SAF [Uri]) into structured [ImportRow] objects.
 *
 * Supports two CSV flavours:
 * - **SpendWise format** (11 columns matching [CsvExporter.HEADER])
 * - **Minimal format** (at least Date, Amount, Category columns)
 *
 * Each row is individually validated; invalid rows carry an [ImportRow.error]
 * message so the UI can show them in a preview before the user confirms.
 */
class CsvImporter(private val context: Context) {

    // ---- Public data classes ----

    /**
     * A single parsed row. If [error] is non-null the row failed validation
     * and should not be imported.
     */
    data class ImportRow(
        val date: LocalDateTime? = null,
        val amount: Double? = null,
        val categoryName: String? = null,
        val paymentMethod: String? = null,
        val description: String? = null,
        val tags: List<String> = emptyList(),
        val source: String? = null,
        val isRecurring: Boolean = false,
        val recurringInterval: String? = null,
        val upiRefId: String? = null,
        val merchantVpa: String? = null,
        val lineNumber: Int = 0,
        val error: String? = null
    )

    /** Aggregate result of parsing an entire CSV file. */
    data class ImportResult(
        val validRows: List<ImportRow>,
        val invalidRows: List<ImportRow>,
        val totalRows: Int
    )

    // ---- Public API ----

    /**
     * Reads and parses the CSV at [uri].
     *
     * @return an [ImportResult] with categorised valid/invalid rows.
     */
    fun parseFromUri(uri: Uri): ImportResult {
        val lines = context.contentResolver.openInputStream(uri)
            ?.bufferedReader()
            ?.readLines()
            ?: return ImportResult(emptyList(), emptyList(), 0)

        if (lines.isEmpty()) return ImportResult(emptyList(), emptyList(), 0)

        // Detect header (first line may or may not be a header)
        val firstLine = lines.first().lowercase()
        val hasHeader = firstLine.contains("date") && firstLine.contains("amount")
        val dataLines = if (hasHeader) lines.drop(1) else lines
        val headerLine = if (hasHeader) lines.first() else null
        val columnMap = headerLine?.let { parseHeader(it) }

        val validRows = mutableListOf<ImportRow>()
        val invalidRows = mutableListOf<ImportRow>()

        dataLines.forEachIndexed { index, line ->
            val lineNumber = if (hasHeader) index + 2 else index + 1 // 1-indexed, skip header
            if (line.isBlank()) return@forEachIndexed

            val fields = parseCsvLine(line)
            val row = parseRow(fields, columnMap, lineNumber)
            if (row.error != null) {
                invalidRows.add(row)
            } else {
                validRows.add(row)
            }
        }

        return ImportResult(
            validRows = validRows,
            invalidRows = invalidRows,
            totalRows = dataLines.count { it.isNotBlank() }
        )
    }

    // ---- Internals ----

    /**
     * Maps header column names (lowercased) to their zero-based index.
     * Handles minor variations like "payment method" vs "paymentmethod".
     */
    private fun parseHeader(header: String): Map<String, Int> {
        return parseCsvLine(header).mapIndexed { index, col ->
            col.lowercase().replace(" ", "").replace("_", "") to index
        }.toMap()
    }

    /**
     * Parses a single row using the column map (or positional fallback for SpendWise format).
     */
    private fun parseRow(fields: List<String>, columnMap: Map<String, Int>?, lineNumber: Int): ImportRow {
        try {
            // Get field by column name (preferred) or positional index (fallback)
            fun field(vararg names: String, index: Int): String? {
                if (columnMap != null) {
                    for (name in names) {
                        val idx = columnMap[name.lowercase().replace(" ", "").replace("_", "")]
                        if (idx != null && idx < fields.size) return fields[idx].trim()
                    }
                }
                return if (index < fields.size) fields[index].trim() else null
            }

            val dateStr = field("date", index = 0)
            val amountStr = field("amount", index = 1)
            val category = field("category", index = 2)
            val paymentMethod = field("paymentmethod", "payment method", "payment_method", index = 3)
            val description = field("description", index = 4)
            val tagsStr = field("tags", index = 5)
            val source = field("source", index = 6)
            val isRecurringStr = field("isrecurring", "is_recurring", index = 7)
            val recurringInterval = field("recurringinterval", "recurring_interval", index = 8)
            val upiRefId = field("upirefid", "upi_ref_id", index = 9)
            val merchantVpa = field("merchantvpa", "merchant_vpa", index = 10)

            // Validate required fields
            if (dateStr.isNullOrBlank()) {
                return ImportRow(lineNumber = lineNumber, error = "Missing date")
            }
            if (amountStr.isNullOrBlank()) {
                return ImportRow(lineNumber = lineNumber, error = "Missing amount")
            }

            val date = parseDate(dateStr)
                ?: return ImportRow(lineNumber = lineNumber, error = "Invalid date format: $dateStr")

            val amount = amountStr.replace(",", "").toDoubleOrNull()
                ?: return ImportRow(lineNumber = lineNumber, error = "Invalid amount: $amountStr")

            if (amount <= 0) {
                return ImportRow(lineNumber = lineNumber, error = "Amount must be positive: $amountStr")
            }

            val tags = tagsStr
                ?.split(";")
                ?.map { it.trim() }
                ?.filter { it.isNotBlank() }
                ?: emptyList()

            return ImportRow(
                date = date,
                amount = amount,
                categoryName = category?.ifBlank { null },
                paymentMethod = paymentMethod?.ifBlank { null },
                description = description ?: "",
                tags = tags,
                source = source?.ifBlank { null },
                isRecurring = isRecurringStr?.lowercase() == "true",
                recurringInterval = recurringInterval?.ifBlank { null },
                upiRefId = upiRefId?.ifBlank { null },
                merchantVpa = merchantVpa?.ifBlank { null },
                lineNumber = lineNumber
            )
        } catch (e: Exception) {
            return ImportRow(lineNumber = lineNumber, error = "Parse error: ${e.message}")
        }
    }

    /**
     * Parses date strings in multiple formats:
     * - ISO: `YYYY-MM-DDTHH:MM:SS`
     * - SpendWise display: `DD/MM/YYYY`
     * - ISO date only: `YYYY-MM-DD`
     */
    private fun parseDate(dateStr: String): LocalDateTime? {
        // ISO datetime: 2025-05-13T14:30:00
        try {
            return LocalDateTime.parse(dateStr)
        } catch (_: Exception) { }

        // DD/MM/YYYY (legacy SpendWise export format)
        try {
            val parts = dateStr.split("/")
            if (parts.size == 3) {
                val day = parts[0].trim().toInt()
                val month = parts[1].trim().toInt()
                val year = parts[2].trim().toInt()
                return LocalDateTime(year, month, day, 0, 0, 0)
            }
        } catch (_: Exception) { }

        // ISO date only: YYYY-MM-DD
        try {
            val parts = dateStr.split("-")
            if (parts.size == 3) {
                val year = parts[0].trim().toInt()
                val month = parts[1].trim().toInt()
                val day = parts[2].trim().toInt()
                return LocalDateTime(year, month, day, 0, 0, 0)
            }
        } catch (_: Exception) { }

        return null
    }

    /**
     * Parses a single CSV line respecting RFC 4180 quoting rules.
     *
     * Handles quoted fields that contain commas, newlines, and escaped
     * double-quotes (`""`).
     */
    private fun parseCsvLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val ch = line[i]
            when {
                ch == '"' && !inQuotes -> inQuotes = true
                ch == '"' && inQuotes -> {
                    // Peek next char: doubled quote = escaped literal quote
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++ // skip the second quote
                    } else {
                        inQuotes = false
                    }
                }
                ch == ',' && !inQuotes -> {
                    fields.add(current.toString())
                    current.clear()
                }
                else -> current.append(ch)
            }
            i++
        }
        fields.add(current.toString())
        return fields
    }
}
