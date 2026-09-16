package com.example.financialliteracyapp.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.financialliteracyapp.data.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MapViewModel(private val repo: GameRepository) : ViewModel() {
    val wallet = repo.observeWallet().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    private val _selectedDistrict = MutableStateFlow<String?>("Рынок")
    val selectedDistrict = _selectedDistrict.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "Рынок")

    fun onDistrictSelected(district: String) {
        viewModelScope.launch {
            _selectedDistrict.value = district
        }
    }

    companion object {
        fun factory(repo: GameRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    MapViewModel(repo) as T
            }
    }
}