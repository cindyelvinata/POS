package com.example.pos.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CashAccountInfo(
    val name: String = ""
)

@Serializable
data class Expense(
    val id: String = "",
    val description: String,
    val amount: Double,

    @SerialName("is_cancelled")
    val isCancelled: Boolean = false,

    @SerialName("cash_accounts_id")
    val cashAccountsId: String,

    @SerialName("created_at")
    val createdAt: String = "",

    @SerialName("cash_accounts")
    val cashAccount: CashAccountInfo? = null
)
