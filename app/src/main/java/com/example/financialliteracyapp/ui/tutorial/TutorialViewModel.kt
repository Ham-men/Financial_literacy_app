package com.example.financialliteracyapp.ui.tutorial

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Обучение функционалу при первом запуске: перебор шагов по сценам.
 * Навигация между сценами выполняется в NavGraph по [currentStep].
 */
class TutorialViewModel : ViewModel() {

    private val _steps = MutableStateFlow(TutorialScenarios.steps)
    val steps: StateFlow<List<TutorialStep>> = _steps.asStateFlow()

    private val _index = MutableStateFlow(0)
    val index: StateFlow<Int> = _index.asStateFlow()

    private val _active = MutableStateFlow(false)
    val active: StateFlow<Boolean> = _active.asStateFlow()

    private val _currentStep = MutableStateFlow<TutorialStep?>(null)
    /** Текущий шаг (null, если обучение неактивно). */
    val currentStep: StateFlow<TutorialStep?> = _currentStep.asStateFlow()

    fun start() {
        _index.value = 0
        _currentStep.value = _steps.value.first()
        _active.value = true
    }

    fun next() {
        if (_index.value >= _steps.value.lastIndex) {
            finish()
        } else {
            val newIndex = _index.value + 1
            _index.value = newIndex
            _currentStep.value = _steps.value[newIndex]
        }
    }

    fun prev() {
        if (_index.value > 0) {
            val newIndex = _index.value - 1
            _index.value = newIndex
            _currentStep.value = _steps.value[newIndex]
        }
    }

    fun finish() {
        _active.value = false
    }
}