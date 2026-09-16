package com.example.financialliteracyapp.ui.screens.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.financialliteracyapp.data.GameRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GoalViewModel(private val repo: GameRepository) : ViewModel() {
    val goals = repo.observeGoals().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val wallet = repo.observeWallet().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun addToGoal(goalId: Int, amount: Int) {
        viewModelScope.launch {
            repo.addToGoal(goalId, amount)
        }
    }

    fun withdrawFromGoal(goalId: Int, amount: Int) {
        viewModelScope.launch {
            repo.withdrawFromGoal(goalId, amount)
        }
    }

    companion object {
        fun factory(repo: GameRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    GoalViewModel(repo) as T
            }
    }
}