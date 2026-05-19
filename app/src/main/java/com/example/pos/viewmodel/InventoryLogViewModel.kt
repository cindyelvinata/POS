package com.example.pos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pos.repository.InventoryLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InventoryLogViewModel : ViewModel() {

    private val repository =
        InventoryLogRepository()

    private val _inventoryLogUiState =
        MutableStateFlow<InventoryLogUiState>(
            InventoryLogUiState.Loading
        )

    val inventoryLogUiState:
            StateFlow<InventoryLogUiState> =
        _inventoryLogUiState

    fun loadLogs() {

        viewModelScope.launch {

            try {

                _inventoryLogUiState.value =
                    InventoryLogUiState.Loading

                val logs =
                    repository.getLogs()

                _inventoryLogUiState.value =
                    InventoryLogUiState.Success(logs)

            } catch (e: Exception) {

                _inventoryLogUiState.value =
                    InventoryLogUiState.Error(
                        e.message
                            ?: "Gagal mengambil inventory log"
                    )
            }
        }
    }
}