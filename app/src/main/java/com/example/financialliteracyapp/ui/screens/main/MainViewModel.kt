package com.example.financialliteracyapp.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.financialliteracyapp.data.GameRepository
import com.example.financialliteracyapp.data.clock.GameClock
import com.example.financialliteracyapp.data.local.entity.PetEntity
import com.example.financialliteracyapp.data.local.entity.QuestEntity
import com.example.financialliteracyapp.data.local.entity.GoalEntity
import com.example.financialliteracyapp.data.prefs.UserPrefs
import com.example.financialliteracyapp.domain.economy.GameRules
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val repo: GameRepository,
    private val prefs: UserPrefs
) : ViewModel() {
    val pet = repo.observePet().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PetEntity())
    val wallet = repo.observeWallet().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val goals = repo.observeGoals().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val quests = repo.observeQuests().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val day = prefs.currentDay.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 1)
    val cashierHired = prefs.cashierHired.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
    val gameMinute = GameClock.minute.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GameRules.WORK_DAY_START_MINUTE)

    private val _sleepMessage = MutableStateFlow<String?>(null)
    val sleepMessage: StateFlow<String?> = _sleepMessage.asStateFlow()

    val currentGoal = goals.map { goalsList ->
        goalsList.find { !it.completed && it.currentAmount < it.targetAmount } ?: goalsList.firstOrNull()
    }
    val activeQuest = quests.map { questsList ->
        questsList.find { !it.completed } ?: questsList.firstOrNull()
    }

    fun feed() = viewModelScope.launch { repo.feedPet() }
    fun heal() = viewModelScope.launch { repo.healPet() }

    /** Лечь спать: новый день в 10:00. Разрешено с 18:00 до 23:00. */
    fun sleep(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            val minute = GameClock.minute.value
            if (!GameRules.canSleep(minute)) {
                _sleepMessage.value = "Магазины ещё работают. Спать можно с 18:00 до 23:00."
                return@launch
            }
            val cur = day.value
            prefs.setCurrentDay(cur + 1)
            GameClock.newDay()
            repo.payCashierSalary(cashierHired.value)
            repo.resetDay()
            _sleepMessage.value = "Сладких снов! День ${cur + 1} начался в 10:00."
            onDone()
        }
    }

    companion object {
        fun factory(repo: GameRepository, prefs: UserPrefs): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    MainViewModel(repo, prefs) as T
            }
    }
}