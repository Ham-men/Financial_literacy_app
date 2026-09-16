package com.example.financialliteracyapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Товар в каталоге: 4 нужных + 4 желаемых. */
@Entity(tableName = "catalog_items")
data class CatalogItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String = "",
    val price: Int = 0,
    val category: String = "NEED", // NEED | WANT
    val hungerEffect: Int = 0,
    val moodEffect: Int = 0,
    val energyEffect: Int = 0,
    val iconRes: String = "", // имя drawable ресурса
    val order: Int = 0
)