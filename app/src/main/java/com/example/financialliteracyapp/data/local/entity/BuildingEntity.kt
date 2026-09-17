package com.example.financialliteracyapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** v5.0: здание = рабочее место + точка продажи. 5 типов + жилой дом. */
@Entity(tableName = "buildings")
data class BuildingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String = "PRODUCTS",           // PRODUCTS, AUTO_SERVICE, CONSTRUCTION, HEALTH, ART, RESIDENTIAL
    val district: String = "Рынок",
    val x: Int = 0,                          // тайловая позиция на карте
    val y: Int = 0,
    val level: Int = 1,
    val stock: Int = 0,                      // товар на складе/полках
    val price: Int = 0,                      // цена продажи
    val costPrice: Int = 0,                  // себестоимость
    val cash: Int = 0,                       // касса здания
    val soldToday: Int = 0,
    val revenueTotal: Int = 0,
    val dirtLevel: Int = 0,                  // 0..100 грязь
    val upgradesBitmask: Int = 0,            // биты улучшений
    val employeesJson: String = "[]",        // JSON массив EmployeeRole
    val isOpen: Boolean = true
) {
    /** Выручка за сегодня (= накопленная, пока не сброшена) — совместимость с отчётом ларька. */
    val revenueToday: Int get() = revenueTotal
}