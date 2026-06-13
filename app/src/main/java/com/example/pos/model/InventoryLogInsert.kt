package com.example.pos.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InventoryLogInsert(

    @SerialName("products_id")
    val productsId: String,

    val quantity: Double,

    @SerialName("stock_before")
    val stockBefore: Double,

    @SerialName("stock_after")
    val stockAfter: Double,

    val type: String,

    val description: String
)