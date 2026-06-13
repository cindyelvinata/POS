package com.example.pos.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Product(

    val id: String,

    val name: String,

    val price: Double,

    val stock: Double,

    @SerialName("is_active")
    val isActive: Boolean,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("updated_at")
    val updatedAt: String
)