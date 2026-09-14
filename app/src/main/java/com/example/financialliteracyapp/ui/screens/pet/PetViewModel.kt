package com.example.financialliteracyapp.ui.screens.pet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.financialliteracyapp.data.GameRepository
import com.example.financialliteracyapp.data.local.entity.PetEntity
import com.example.financialliteracyapp.data.local.entity.WalletEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** День 3-4: состояние питомца из Room, действия через репозиторий. */
class PetViewModel(private val repo: GameRepository) : ViewModel() {

    val pet = repo.observePet()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val wallet = repo.observeWallet()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun feed() = viewModelScope.launch { repo.feedPet() }
    fun play() = viewModelScope.launch { repo.playWithPet() }
    fun rest() = viewModelScope.launch { repo.restPet() }

    companion object {
        fun factory(repo: GameRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PetViewModel(repo) as T
                }
            }
    }
}

// Дефолты для первого запуска (пока Room грузится)
val DefaultPet = PetEntity()
val DefaultWallet = WalletEntity()
