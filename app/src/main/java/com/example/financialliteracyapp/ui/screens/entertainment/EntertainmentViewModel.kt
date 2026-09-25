package com.example.financialliteracyapp.ui.screens.entertainment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.financialliteracyapp.data.GameRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Комната развлечений: игрушки → опыт → уровень Финни → ставка копилки. */
class EntertainmentViewModel(private val repo: GameRepository) : ViewModel() {
    val catalog = repo.observeCatalog().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val wallet = repo.observeWallet().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val pet = repo.observePet().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val quests = repo.observeQuests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun buy(itemId: Int) {
        viewModelScope.launch { repo.buyCatalogItem(itemId) }
    }

    companion object {
        fun factory(repo: GameRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    EntertainmentViewModel(repo) as T
            }
    }
}