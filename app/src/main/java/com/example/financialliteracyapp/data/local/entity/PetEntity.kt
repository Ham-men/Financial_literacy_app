package com.example.financialliteracyapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Питомец — 1 вид (Енот), 3 шкалы. План День 2. */
@Entity(tableName = "pet")
data class PetEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Енот Копилкин",
    val hunger: Int = 70,
    val mood: Int = 80,
    val energy: Int = 90,
    val level: Int = 1
)
