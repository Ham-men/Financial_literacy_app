package com.example.financialliteracyapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Цель накопления: 300 / 800 / 1500 ₡. */
@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String = "",
    val targetAmount: Int = 0,
    val currentAmount: Int = 0,
    val completed: Boolean = false,
    val order: Int = 0
)