package com.example.pos.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductInsert(

    val name: String,

    val price: Double,

    val stock: Double,

    @SerialName("is_active")
    val isActive: Boolean
)