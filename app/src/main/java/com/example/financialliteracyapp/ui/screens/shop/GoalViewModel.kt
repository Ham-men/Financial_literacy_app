package com.example.financialliteracyapp.ui.screens.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.financialliteracyapp.data.GameRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GoalViewModel(private val repo: GameRepository) : ViewModel() {
    val goals = repo.observeGoals().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val periods = repo.observePeriods().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Средняя скорость накопления (₡/день) по закрытым дням; минимум 1. */
    val avgDailySave = periods.map { list ->
        val withSave = list.filter { it.savePlan > 0 }
        if (withSave.isEmpty()) 50 else withSave.map { it.savePlan }.average().toInt().coerceAtLeast(1)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 50)

    fun setActive(goalId: Int) {
        viewModelScope.launch { repo.setActiveGoal(goalId) }
    }

    fun addToGoal(goalId: Int, amount: Int) {
        viewModelScope.launch { repo.addToGoal(goalId, amount) }
    }

    fun withdrawFromGoal(goalId: Int, amount: Int) {
        viewModelScope.launch { repo.withdrawFromGoal(goalId, amount) }
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