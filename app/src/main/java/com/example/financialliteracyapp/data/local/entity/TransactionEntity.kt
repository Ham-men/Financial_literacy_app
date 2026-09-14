package com.example.financialliteracyapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Кассовая книга: приход/расход. План День 2 / День 15. */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val day: Int = 1,
    val kind: String = "EXPENSE", // INCOME | EXPENSE
    val category: String = "other",
    val amount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
