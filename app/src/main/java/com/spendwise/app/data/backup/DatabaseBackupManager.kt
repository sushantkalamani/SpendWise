package com.spendwise.app.data.backup

import android.content.Context
import android.net.Uri
import com.spendwise.app.data.local.AppDatabase
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class DatabaseBackupManager(private val context: Context) {

    private val dbFile: File get() = context.getDatabasePath("spendwise.db")

    fun backupTo(uri: Uri): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                FileInputStream(dbFile).use { input ->
                    input.copyTo(output)
                }
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun restoreFrom(uri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (_: Exception) {
            false
        }
    }
}
