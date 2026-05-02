package com.spendwise.app.data.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.spendwise.app.domain.model.Expense
import java.io.File
import java.text.NumberFormat
import java.util.Locale

class CsvExporter(private val context: Context) {

    fun exportExpenses(expenses: List<Expense>): File {
        val file = File(context.cacheDir, "spendwise_export.csv")
        file.bufferedWriter().use { writer ->
            writer.write("Date,Category,Description,Amount,Payment Method,Tags")
            writer.newLine()
            expenses.forEach { expense ->
                val date = "${expense.date.dayOfMonth}/${expense.date.monthNumber}/${expense.date.year}"
                val category = expense.category?.name?.replace(",", " ") ?: "Uncategorized"
                val description = expense.description.replace(",", " ").replace("\"", "'")
                val amount = String.format("%.2f", expense.amount)
                val method = expense.paymentMethod.name
                val tags = expense.tags.joinToString(";")
                writer.write("$date,$category,$description,$amount,$method,$tags")
                writer.newLine()
            }
        }
        return file
    }

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
}
