package com.example.financialliteracyapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Питомец Финни — кастомизация: тело × аксессуар × фон, 3 шкалы, 3 стадии роста. Позиция на карте (x,y). */
@Entity(tableName = "pet")
data class PetEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Финни",
    val bodyType: Int = 0,       // 0=рыжий, 1=серый, 2=пятнистый
    val accessory: Int = 0,      // 0=без, 1=шарф, 2=кепка
    val background: Int = 0,     // 0=рынок, 1=парк, 2=речка
    val hunger: Int = 70,
    val mood: Int = 80,
    val energy: Int = 90,
    val level: Int = 1,          // 1=Малыш, 2=Подросток, 3=Хозяин ларька
    val x: Float = 0f,           // позиция на карте (пиксели)
    val y: Float = 0f
)
