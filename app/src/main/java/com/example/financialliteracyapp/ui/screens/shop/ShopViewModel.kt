package com.example.financialliteracyapp.ui.screens.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.financialliteracyapp.data.GameRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShopViewModel(private val repo: GameRepository) : ViewModel() {
    val shop = repo.observeShop()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val wallet = repo.observeWallet()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setPrice(price: Int) {
        viewModelScope.launch { repo.setShopPrice(price) }
    }

    fun purchaseStock(pricePerUnit: Double, units: Int) {
        viewModelScope.launch { repo.purchaseStock(pricePerUnit, units) }
    }

    fun simulateBots(onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            repo.simulateBotDay()
            onDone?.invoke()
        }
    }

    companion object {
        fun factory(repo: GameRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ShopViewModel(repo) as T
            }
    }
}
