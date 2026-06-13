package com.example.pos.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Model untuk tabel cash_accounts di Supabase.
 */
@Serializable
data class CashAccount(
    val id: String = "",
    val name: String = "",

    @SerialName("current_balance")
    val currentBalance: Double = 0.0,

    @SerialName("is_active")
    val isActive: Boolean = true,

    @SerialName("created_at")
    val createdAt: String = "",

    @SerialName("updated_at")
    val updatedAt: String = ""
)

/**
 * Model untuk tabel cash_logs di Supabase.
 * type: "in" atau "out"
 * source: "transaksi manual" atau "penjualan"
 */
@Serializable
data class CashLog(
    val id: String = "",
    val amount: Double = 0.0,

    @SerialName("running_balance")
    val runningBalance: Double = 0.0,

    val type: String = "",       // "in" | "out"
    val source: String = "",     // "transaksi manual" | "penjualan"
    val notes: String? = null,

    @SerialName("created_at")
    val createdAt: String = "",

    @SerialName("cash_accounts_id")
    val cashAccountsId: String = ""
)
