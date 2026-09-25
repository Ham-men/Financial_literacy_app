package com.example.financialliteracyapp.ui.screens.cheats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.financialliteracyapp.data.GameRepository
import com.example.financialliteracyapp.data.clock.GameClock
import com.example.financialliteracyapp.data.prefs.UserPrefs
import com.example.financialliteracyapp.domain.economy.GameRules
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Читы для теста функционала: добавление денег в банки и установка даты/времени. */
class CheatsViewModel(
    private val repo: GameRepository,
    private val prefs: UserPrefs
) : ViewModel() {
    val wallet = repo.observeWallet()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val day = prefs.currentDay.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 1)
    val gameMinute = GameClock.minute
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GameRules.WORK_DAY_START_MINUTE)

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _day = MutableStateFlow("")
    val dayInput = _day.asStateFlow()
    private val _minute = MutableStateFlow("")
    val minuteInput = _minute.asStateFlow()

    fun onDayInput(v: String) { _day.value = v.filter { it.isDigit() } }
    fun onMinuteInput(v: String) { _minute.value = v.filter { it.isDigit() } }

    fun cheatAddMoney(bank: String, amountText: String) {
        val amount = amountText.toIntOrNull() ?: return
        if (amount <= 0) {
            _message.value = "Введи число больше 0"
            return
        }
        viewModelScope.launch {
            repo.cheatAddToBank(bank, amount)
            val name = when (bank) {
                "NEED" -> "карман"
                "WANT" -> "желаемое"
                else -> "копилка"
            }
            _message.value = "Добавлено $amount ₡ в «$name»"
        }
    }

    fun cheatSetTime(dayText: String, minuteText: String) {
        val newDay = dayText.toIntOrNull()
        val newMinute = minuteText.toIntOrNull()
        viewModelScope.launch {
            if (newDay != null && newDay >= 1) {
                prefs.setCurrentDay(newDay)
            }
            if (newMinute != null) {
                GameClock.setMinute(newMinute)
            }
            _message.value = if (newDay != null && newDay >= 1 || newMinute != null) {
                "Дата/время установлены"
            } else {
                "Введи число для дня и времени"
            }
        }
    }

    fun clearMessage() { _message.value = null }

    companion object {
        fun factory(repo: GameRepository, prefs: UserPrefs): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    CheatsViewModel(repo, prefs) as T
            }
    }
}