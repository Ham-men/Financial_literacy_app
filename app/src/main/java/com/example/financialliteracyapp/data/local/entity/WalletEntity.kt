package com.example.financialliteracyapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Кошелёк: наличные + план/факт по 3 банкам (нужное/желаемое/копилка). */
@Entity(tableName = "wallet")
data class WalletEntity(
    @PrimaryKey val id: Int = 1,
    val cash: Int = 500,
    // План
    val needPlan: Int = 0,
    val wantPlan: Int = 0,
    val savePlan: Int = 0,
    // Факт
    val needFact: Int = 0,
    val wantFact: Int = 0,
    val saveFact: Int = 0
)
