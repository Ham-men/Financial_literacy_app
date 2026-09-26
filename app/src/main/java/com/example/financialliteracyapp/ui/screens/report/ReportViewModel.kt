package com.example.financialliteracyapp.ui.screens.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.financialliteracyapp.data.GameRepository
import com.example.financialliteracyapp.data.prefs.UserPrefs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ReportViewModel(
    private val repo: GameRepository,
    private val prefs: UserPrefs
) : ViewModel() {
    val shop = repo.observeShop().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val wallet = repo.observeWallet().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val day = prefs.currentDay.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 1)
    val periods = repo.observePeriods().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val hiredCount = prefs.hiredBuildings
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    companion object {
        fun factory(repo: GameRepository, prefs: UserPrefs): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ReportViewModel(repo, prefs) as T
            }
    }
}
