package com.example.financialliteracyapp.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.financialliteracyapp.data.GameRepository
import com.example.financialliteracyapp.data.local.entity.PetEntity
import com.example.financialliteracyapp.data.local.entity.QuestEntity
import com.example.financialliteracyapp.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine

class MainViewModel(private val repo: GameRepository) : ViewModel() {
    val pet = repo.observePet().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PetEntity())
    val wallet = repo.observeWallet().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val goals = repo.observeGoals().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val quests = repo.observeQuests().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    
    val currentGoal = goals.map { goalsList ->
        goalsList.find { !it.completed && it.currentAmount < it.targetAmount } ?: goalsList.firstOrNull()
    }
    val activeQuest = quests.map { questsList ->
        questsList.find { !it.completed } ?: questsList.firstOrNull()
    }

    companion object {
        fun factory(repo: GameRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    MainViewModel(repo) as T
            }
    }
}