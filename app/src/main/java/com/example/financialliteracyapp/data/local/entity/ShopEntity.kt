package com.example.financialliteracyapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Один магазин — Ларёк с лимонадом. План День 2. */
@Entity(tableName = "shop")
data class ShopEntity(
    @PrimaryKey val id: Long = 1,
    val name: String = "Ларёк с лимонадом",
    val district: String = "Рынок",
    val stock: Int = 100,
    val price: Int = 8,
    val costPrice: Int = 3,
    val cash: Int = 200,
    val soldToday: Int = 0,
    val revenueToday: Int = 0
)
