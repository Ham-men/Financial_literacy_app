package com.example.financialliteracyapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** v5.0: боты с типами, машинами, работой, домом, состоянием и целями движения. */
@Entity(tableName = "bots")
data class BotEntity(
    @PrimaryKey val id: Long = 0,
    val type: String = "WORKER",       // WORKER, ENGINEER, MANAGER, RETIREE, STUDENT, ELITE
    val district: String = "Рынок",
    val salary: Int = 800,
    val wallet: Int = 200,
    val loyalty: Float = 0f,
    val hasCar: Boolean = false,
    val homeBuildingId: Long = -1,
    val workBuildingId: Long = -1,
    val state: String = "HOME",        // HOME, MOVING_TO_WORK, WORKING, MOVING_TO_SHOP, SHOPPING, MOVING_HOME
    val targetX: Float = 0f,
    val targetY: Float = 0f
)