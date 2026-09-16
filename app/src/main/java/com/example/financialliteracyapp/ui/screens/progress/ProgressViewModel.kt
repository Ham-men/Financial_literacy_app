package com.example.financialliteracyapp.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.financialliteracyapp.data.GameRepository
import com.example.financialliteracyapp.data.prefs.UserPrefs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class ProgressViewModel(
    private val repo: GameRepository,
    private val prefs: UserPrefs
) : ViewModel() {
    val quests = repo.observeQuests().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val goals = repo.observeGoals().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val wallet = repo.observeWallet().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val transactions = repo.observeTransactions().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val day = prefs.currentDay.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 1)

    companion object {
        fun factory(repo: GameRepository, prefs: UserPrefs): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ProgressViewModel(repo, prefs) as T
            }
    }
}