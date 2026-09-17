package com.example.financialliteracyapp.ui.screens.kiosk

/** Покупатель, ждущий обслуживания на кассе. units — сколько товара он взял на полках. */
data class KioskCustomer(val id: Int, val units: Int)

/** Бот, идущий по сцене ларька: дверь → полка → касса. */
data class StageBot(val id: Int, val units: Int)