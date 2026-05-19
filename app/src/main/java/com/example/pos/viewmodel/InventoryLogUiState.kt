package com.example.pos.viewmodel

import com.example.pos.model.InventoryLog

sealed class InventoryLogUiState {

    object Loading : InventoryLogUiState()

    data class Success(
        val logs: List<InventoryLog>
    ) : InventoryLogUiState()

    data class Error(
        val message: String
    ) : InventoryLogUiState()
}