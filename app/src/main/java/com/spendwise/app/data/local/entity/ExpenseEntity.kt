package com.spendwise.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId"), Index("date"), Index("upiRefId")]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val categoryId: Long?,
    val description: String = "",
    val date: Long,
    val paymentMethod: String = "UPI",
    val tags: String = "",
    val upiRefId: String? = null,
    val merchantVpa: String? = null,
    val source: String = "MANUAL",
    val isRecurring: Boolean = false,
    val recurringInterval: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
