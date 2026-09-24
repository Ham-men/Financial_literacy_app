package com.example.financialliteracyapp.ui.screens.banks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.financialliteracyapp.data.GameRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BankViewModel(private val repo: GameRepository) : ViewModel() {
    val wallet = repo.observeWallet()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Перенос монеты/суммы из мешка в банку. */
    fun moveToBank(bank: String, amount: Int) {
        viewModelScope.launch { repo.moveBagToBank(bank, amount) }
    }

    /** «Вывести всё в мешок» — вернуть все банки обратно. */
    fun withdrawAllToBag() {
        viewModelScope.launch { repo.withdrawAllToBag() }
    }

    companion object {
        fun factory(repo: GameRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    BankViewModel(repo) as T
            }
    }
}
