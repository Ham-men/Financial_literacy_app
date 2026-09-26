package com.example.financialliteracyapp.ui.tutorial

/** Регион экрана, на который указывает стрелка обучения. */
enum class Spot {
    SIDEBAR, HUD,
    TOP_LEFT, TOP_CENTER, TOP_RIGHT,
    CENTER, CENTER_LEFT, CENTER_RIGHT,
    BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
}

/** Один шаг обучения: сцена + куда указать + что написать. */
data class TutorialStep(
    val route: String,
    val spot: Spot,
    val title: String,
    val text: String
)