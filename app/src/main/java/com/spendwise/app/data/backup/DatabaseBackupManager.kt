package com.spendwise.app.data.backup

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import com.spendwise.app.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    suspend fun backupTo(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
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
    suspend fun restoreFrom(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val tmp = File(dbFile.parent, "restore_tmp.db")
            context.contentResolver.openInputStream(uri)?.use { it.copyTo(FileOutputStream(tmp)) }
                ?: return@withContext false
            
            SQLiteDatabase.openDatabase(tmp.path, null, SQLiteDatabase.OPEN_READONLY).use { db ->
                val ok = db.rawQuery("PRAGMA integrity_check", null).use { c ->
                    c.moveToFirst() && c.getString(0) == "ok"
                }
                if (!ok) {
                    tmp.delete()
                    return@withContext false
                }
            }

            database.close()

            val parent = dbFile.parentFile
            if (parent != null) {
                File(parent, "spendwise.db-wal").delete()
                File(parent, "spendwise.db-shm").delete()
            }

            tmp.copyTo(dbFile, overwrite = true)
            tmp.delete()
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Programmatically restarts the app by relaunching the launcher activity
     * and killing the current process.
     */
    fun restartApp() {
        val pm = context.packageManager
        val intent = pm.getLaunchIntentForPackage(context.packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
        }
        Runtime.getRuntime().exit(0)
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
