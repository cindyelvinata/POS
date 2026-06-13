package com.example.pos.viewmodel

import com.example.pos.model.Product

sealed class ProductUiState {

    object Loading : ProductUiState()

    data class Success(
        val products: List<Product>
    ) : ProductUiState()

    data class Error(
        val message: String
    ) : ProductUiState()
}