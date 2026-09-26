package com.example.financialliteracyapp.ui.screens.adult

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.financialliteracyapp.data.GameRepository
import com.example.financialliteracyapp.data.clock.GameClock
import com.example.financialliteracyapp.data.local.entity.PetEntity
import com.example.financialliteracyapp.data.prefs.UserPrefs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdultViewModel(
    private val repo: GameRepository,
    private val prefs: UserPrefs
) : ViewModel() {
    val pet = repo.observePet().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PetEntity())
    val wallet = repo.observeWallet().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val goals = repo.observeGoals().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val quests = repo.observeQuests().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val periods = repo.observePeriods().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val day = prefs.currentDay.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 1)
    val demoMode = prefs.demoMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun setDemo(on: Boolean) {
        viewModelScope.launch {
            prefs.setDemoMode(on)
            GameClock.setDemoMode(on)
        }
    }

    /** Полный сброс профиля: Room + DataStore. */
    fun resetProfile() {
        viewModelScope.launch {
            repo.resetProfile()
            prefs.clear()
            GameClock.setDemoMode(false)
        }
    }

    companion object {
        fun factory(repo: GameRepository, prefs: UserPrefs): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AdultViewModel(repo, prefs) as T
            }
    }
}