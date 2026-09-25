package com.example.financialliteracyapp.ui.screens.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.financialliteracyapp.data.GameRepository
import com.example.financialliteracyapp.data.clock.GameClock
import com.example.financialliteracyapp.data.prefs.UserPrefs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReportViewModel(
    private val repo: GameRepository,
    private val prefs: UserPrefs
) : ViewModel() {
    val shop = repo.observeShop().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val wallet = repo.observeWallet().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val day = prefs.currentDay.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 1)
    val cashierHired = prefs.cashierHired.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun nextDay(onDone: () -> Unit) {
        viewModelScope.launch {
            val cur = day.value
            prefs.setCurrentDay(cur + 1)
            GameClock.newDay()
            // Зарплата Милы — расход дня, списывается если хватает баланса
            repo.payCashierSalary(cashierHired.value)
            repo.resetDay()
            onDone()
        }
    }

    companion object {
        fun factory(repo: GameRepository, prefs: UserPrefs): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ReportViewModel(repo, prefs) as T
            }
    }
}
