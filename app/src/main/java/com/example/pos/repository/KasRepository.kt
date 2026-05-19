package com.example.pos.data.repository

import com.example.pos.data.SupabaseClientProvider
import com.example.pos.data.model.CashAccount
import com.example.pos.data.model.CashLog
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.datetime.Clock
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class KasRepository {

    private val client = SupabaseClientProvider.client

    /* =============================================
     * CASH ACCOUNTS
     * ============================================= */

    /**
     * Ambil semua kas (aktif maupun non-aktif),
     * diurutkan dari yang terbaru dibuat.
     */
    suspend fun getAllCashAccounts(): List<CashAccount> {
        return client.postgrest["cash_accounts"]
            .select {
                order("created_at", Order.DESCENDING)
            }
            .decodeList<CashAccount>()
    }

    /**
     * Ambil hanya kas yang aktif saja.
     * Dipakai saat memilih kas untuk transaksi.
     */
    suspend fun getActiveCashAccounts(): List<CashAccount> {
        return client.postgrest["cash_accounts"]
            .select {
                filter {
                    eq("is_active", true)
                }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<CashAccount>()
    }

    /**
     * Buat kas baru.
     * Saldo awal akan langsung menjadi current_balance.
     */
    suspend fun createCashAccount(name: String, initialBalance: Double) {
        client.postgrest["cash_accounts"].insert(
            buildJsonObject {
                put("name", name)
                put("current_balance", initialBalance)
                put("is_active", true)
            }
        )
    }

    /**
     * Update nama kas.
     */
    suspend fun updateCashAccountName(id: String, newName: String) {
        client.postgrest["cash_accounts"].update(
            {
                set("name", newName)
                set("updated_at", Clock.System.now().toString())
            }
        ) {
            filter {
                eq("id", id)
            }
        }
    }

    /**
     * Toggle status aktif/non-aktif kas.
     */
    suspend fun toggleCashAccountStatus(id: String, isActive: Boolean) {
        client.postgrest["cash_accounts"].update(
            {
                set("is_active", isActive)
                set("updated_at", Clock.System.now().toString())
            }
        ) {
            filter {
                eq("id", id)
            }
        }
    }

    /* =============================================
     * TRANSAKSI MANUAL (Tambah / Kurang Saldo)
     * ============================================= */

    /**
     * Tambah saldo kas secara manual.
     * 1. Update current_balance di cash_accounts
     * 2. Catat log dengan type = "in"
     */
    suspend fun addBalance(
        cashAccountId: String,
        currentBalance: Double,
        amount: Double,
        notes: String
    ) {
        val newBalance = currentBalance + amount
        val now = Clock.System.now().toString()

        // 1. Update saldo
        client.postgrest["cash_accounts"].update(
            {
                set("current_balance", newBalance)
                set("updated_at", now)
            }
        ) {
            filter {
                eq("id", cashAccountId)
            }
        }

        // 2. Catat log
        client.postgrest["cash_logs"].insert(
            buildJsonObject {
                put("cash_accounts_id", cashAccountId)
                put("amount", amount)
                put("running_balance", newBalance)
                put("type", "in")
                put("source", "transaksi manual")
                put("notes", notes)
                put("created_at", now)
            }
        )
    }

    /**
     * Kurangi saldo kas secara manual.
     * 1. Validasi saldo tidak minus
     * 2. Update current_balance di cash_accounts
     * 3. Catat log dengan type = "out"
     *
     * @throws IllegalArgumentException jika saldo tidak cukup
     */
    suspend fun subtractBalance(
        cashAccountId: String,
        currentBalance: Double,
        amount: Double,
        notes: String
    ) {
        if (amount > currentBalance) {
            throw IllegalArgumentException("Saldo tidak cukup. Saldo saat ini: Rp ${currentBalance.toLong()}")
        }

        val newBalance = currentBalance - amount
        val now = Clock.System.now().toString()

        // 1. Update saldo
        client.postgrest["cash_accounts"].update(
            {
                set("current_balance", newBalance)
                set("updated_at", now)
            }
        ) {
            filter {
                eq("id", cashAccountId)
            }
        }

        // 2. Catat log
        client.postgrest["cash_logs"].insert(
            buildJsonObject {
                put("cash_accounts_id", cashAccountId)
                put("amount", amount)
                put("running_balance", newBalance)
                put("type", "out")
                put("source", "transaksi manual")
                put("notes", notes)
                put("created_at", now)
            }
        )
    }

    /* =============================================
     * CASH LOGS
     * ============================================= */

    /**
     * Ambil log kas berdasarkan ID kas tertentu.
     * Diurutkan dari yang terbaru.
     */
    suspend fun getCashLogsByAccountId(cashAccountId: String): List<CashLog> {
        return client.postgrest["cash_logs"]
            .select {
                filter {
                    eq("cash_accounts_id", cashAccountId)
                }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<CashLog>()
    }

    /**
     * Ambil log kas terbaru dari semua kas,
     * dibatasi 10 data untuk ditampilkan di Dashboard.
     */
    suspend fun getRecentCashLogs(limit: Int = 10): List<CashLog> {
        return client.postgrest["cash_logs"]
            .select {
                order("created_at", Order.DESCENDING)
                limit(limit.toLong())
            }
            .decodeList<CashLog>()
    }

    /* =============================================
     * DASHBOARD
     * ============================================= */

    /**
     * Hitung total saldo dari semua kas yang aktif.
     */
    suspend fun getTotalActiveBalance(): Double {
        val accounts = getActiveCashAccounts()
        return accounts.sumOf { it.currentBalance }
    }

    /**
     * Ambil total penjualan hari ini dari tabel sales.
     * Filter berdasarkan transaction_time hari ini dan status "Lunas".
     */
    suspend fun getTodaySalesTotal(): Double {
        val today = Clock.System.now().toString().substring(0, 10) // "YYYY-MM-DD"

        return try {
            val result = client.postgrest["sales"]
                .select {
                    filter {
                        gte("transaction_time", "${today}T00:00:00Z")
                        lte("transaction_time", "${today}T23:59:59Z")
                        eq("status", "Lunas")
                    }
                }
                .decodeList<kotlinx.serialization.json.JsonObject>()

            result.sumOf { obj ->
                obj["total"]?.toString()?.toDoubleOrNull() ?: 0.0
            }
        } catch (e: Exception) {
            0.0
        }
    }
}