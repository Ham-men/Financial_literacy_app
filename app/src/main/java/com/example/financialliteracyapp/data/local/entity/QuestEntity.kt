package com.example.financialliteracyapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Задание по теме: PLANNING / SAVING / SPENDING. */
@Entity(tableName = "quests")
data class QuestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val topic: String = "PLANNING", // PLANNING, SAVING, SPENDING
    val title: String = "",
    val description: String = "",
    val reward: Int = 0,
    val completed: Boolean = false,
    val day: Int = 1,
    val order: Int = 0,
    val progress: Int = 0,
    val target: Int = 1
)