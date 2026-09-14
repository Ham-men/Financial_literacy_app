package com.example.financialliteracyapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Три банки + наличные. План День 2 / День 5. */
@Entity(tableName = "wallet")
data class WalletEntity(
    @PrimaryKey val id: Int = 1,
    val cash: Int = 500,
    val spend: Int = 0,
    val save: Int = 0,
    val invest: Int = 0
)
