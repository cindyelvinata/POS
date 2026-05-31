package com.example.pos.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Customer(
    val id: String = "",
    val name: String,
    val phone: String,

    @SerialName("is_active")
    val isActive: Boolean = true,

    @SerialName("created_at")
    val createdAt: String = "",

    @SerialName("updated_at")
    val updatedAt: String = ""
)

@Serializable
data class CustomerLog(
    val id: String = "",

    @SerialName("customers_id")
    val customersId: String,

    val description: String,

    @SerialName("before_updated")
    val beforeUpdated: String = "",

    @SerialName("created_at")
    val createdAt: String = "",

    @SerialName("updated_at")
    val updatedAt: String = ""
)
