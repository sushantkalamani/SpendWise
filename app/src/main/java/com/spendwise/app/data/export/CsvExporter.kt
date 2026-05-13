package com.spendwise.app.data.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.spendwise.app.domain.model.Expense
import java.io.File
import java.io.OutputStream

/**
 * Exports expenses to CSV in RFC 4180 format.
 *
 * Includes all [Expense] fields so the CSV can be re-imported losslessly.
 * Two export paths are supported:
 * - [exportToUri] writes to a user-chosen SAF destination.
 * - [exportToCache] + [shareFile] writes to the app cache and opens a share sheet.
 */
class CsvExporter(private val context: Context) {

    companion object {
        /** CSV column header row — order must match [expenseToRow]. */
        const val HEADER = "Date,Amount,Category,PaymentMethod,Description,Tags,Source,IsRecurring,RecurringInterval,UpiRefId,MerchantVpa"
    }

    // ---- Public API ----

    /**
     * Writes [expenses] as CSV to the given SAF [uri].
     *
     * @return `true` on success, `false` on I/O failure.
     */
    fun exportToUri(expenses: List<Expense>, uri: Uri): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                writeExpenses(stream, expenses)
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Writes [expenses] to a temporary cache file and returns it.
     * Useful for the share-intent flow where we need a [File] reference.
     */
    fun exportToCache(expenses: List<Expense>): File {
        val file = File(context.cacheDir, "spendwise_export.csv")
        file.outputStream().use { stream -> writeExpenses(stream, expenses) }
        return file
    }

    /**
     * Opens the system share sheet for the given CSV [file].
     */
    fun shareFile(file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Export Expenses").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    // ---- Internals ----

    private fun writeExpenses(stream: OutputStream, expenses: List<Expense>) {
        stream.bufferedWriter().use { writer ->
            writer.write(HEADER)
            writer.newLine()
            expenses.forEach { expense ->
                writer.write(expenseToRow(expense))
                writer.newLine()
            }
        }
    }

    /**
     * Converts a single [Expense] to an RFC 4180 CSV row.
     *
     * Fields containing commas, quotes or newlines are wrapped in double-quotes
     * with internal quotes escaped by doubling (`""`).
     */
    private fun expenseToRow(expense: Expense): String {
        val date = expense.date.let {
            "${it.year}-${it.monthNumber.pad()}-${it.dayOfMonth.pad()}T${it.hour.pad()}:${it.minute.pad()}:${it.second.pad()}"
        }
        val parts = listOf(
            date,
            String.format("%.2f", expense.amount),
            expense.category?.name ?: "Uncategorized",
            expense.paymentMethod.name,
            expense.description,
            expense.tags.joinToString(";"),
            expense.source.name,
            expense.isRecurring.toString(),
            expense.recurringInterval?.name ?: "",
            expense.upiRefId ?: "",
            expense.merchantVpa ?: ""
        )
        return parts.joinToString(",") { csvEscape(it) }
    }

    /** Wraps a field in double-quotes if it contains special characters. */
    private fun csvEscape(field: String): String {
        return if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            "\"${field.replace("\"", "\"\"")}\""
        } else {
            field
        }
    }

    private fun Int.pad(): String = this.toString().padStart(2, '0')
}
