package com.spendwise.app.data.backup

import android.content.Context
import android.net.Uri
import com.spendwise.app.data.local.AppDatabase
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Handles full-database backup/restore and data clearing.
 *
 * Backup and restore operate on the raw SQLite `.db` file via SAF URIs,
 * giving users control over where backups are stored.
 */
class DatabaseBackupManager(
    private val context: Context,
    private val database: AppDatabase
) {

    private val dbFile: File get() = context.getDatabasePath("spendwise.db")

    /**
     * Copies the database file to the given SAF [uri].
     *
     * @return `true` on success, `false` on I/O failure.
     */
    fun backupTo(uri: Uri): Boolean {
        return try {
            // Checkpoint WAL to ensure all data is in the main db file
            database.openHelper.writableDatabase.execSQL("PRAGMA wal_checkpoint(FULL)")
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

    /**
     * Restores the database from the given SAF [uri].
     *
     * **Warning**: This replaces the entire database. The caller should
     * close and re-create the database instance after a successful restore.
     *
     * @return `true` on success, `false` on I/O failure.
     */
    fun restoreFrom(uri: Uri): Boolean {
        return try {
            // Close the database before overwriting
            database.close()
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

    /**
     * Deletes all expenses, budgets, and resets categories to defaults.
     *
     * Unlike [restoreFrom], this preserves the database structure and
     * user preferences (which live in DataStore, not Room).
     *
     * @return `true` on success, `false` on failure.
     */
    fun clearAllData(): Boolean {
        return try {
            database.openHelper.writableDatabase.apply {
                execSQL("DELETE FROM expenses")
                execSQL("DELETE FROM budgets")
                // Keep categories — users may have custom ones
            }
            true
        } catch (_: Exception) {
            false
        }
    }
}
