package com.spendwise.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.spendwise.app.data.local.dao.BudgetDao
import com.spendwise.app.data.local.dao.CategoryDao
import com.spendwise.app.data.local.dao.ExpenseDao
import com.spendwise.app.data.local.entity.BudgetEntity
import com.spendwise.app.data.local.entity.CategoryEntity
import com.spendwise.app.data.local.entity.ExpenseEntity

@Database(
    entities = [ExpenseEntity::class, CategoryEntity::class, BudgetEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "spendwise.db"
            )
                .addCallback(DefaultCategoryCallback())
                .build()
        }
    }

    private class DefaultCategoryCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val defaults = listOf(
                "('Food & Dining', 'Restaurant', '#4CAF50', 0, 1)",
                "('Transport', 'DirectionsCar', '#2196F3', 1, 1)",
                "('Bills & Utilities', 'Receipt', '#FF9800', 2, 1)",
                "('Shopping', 'ShoppingBag', '#E91E63', 3, 1)",
                "('Health', 'LocalHospital', '#F44336', 4, 1)",
                "('Entertainment', 'Movie', '#9C27B0', 5, 1)",
                "('Groceries', 'ShoppingCart', '#8BC34A', 6, 1)",
                "('Other', 'MoreHoriz', '#607D8B', 7, 1)"
            )
            defaults.forEach { values ->
                db.execSQL("INSERT INTO categories (name, icon, colorHex, sortOrder, isDefault) VALUES $values")
            }
        }
    }
}
