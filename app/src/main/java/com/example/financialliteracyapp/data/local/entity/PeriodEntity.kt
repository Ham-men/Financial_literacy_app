package com.example.financialliteracyapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Итог игрового дня: план (сколько выделено в банки) vs факт (сколько потрачено/отложено). */
@Entity(tableName = "periods")
data class PeriodEntity(
    @PrimaryKey val day: Int,
    val needPlan: Int,
    val wantPlan: Int,
    val savePlan: Int,
    val needFact: Int,
    val wantFact: Int,
    val saveFact: Int,
    val income: Int,
    val expense: Int,
    val success: Boolean
) {
    val balance: Int get() = income - expense
}