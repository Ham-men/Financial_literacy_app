package com.example.financialliteracyapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** MVP: 20 ботов, один тип (Рабочий). План День 2 / День 11-12. */
@Entity(tableName = "bots")
data class BotEntity(
    @PrimaryKey val id: Long = 0,
    val type: String = "WORKER",
    val district: String = "Рынок",
    val salary: Int = 800,
    val wallet: Int = 200,
    val loyalty: Float = 0f
)
