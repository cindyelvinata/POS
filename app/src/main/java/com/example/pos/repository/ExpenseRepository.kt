package com.example.pos.repository

import com.example.pos.data.SupabaseClientProvider
import com.example.pos.model.Expense
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.datetime.Clock
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ExpenseRepository {

    private val supabase = SupabaseClientProvider.client

    suspend fun getExpenses(): List<Expense> {
        return supabase
            .from("expenses")
            .select(columns = Columns.raw("*, cash_accounts(name)")) {
                order("created_at", Order.DESCENDING)
            }
            .decodeList<Expense>()
    }

    suspend fun addExpense(
        description: String,
        amount: Double,
        cashAccountId: String,
        currentBalance: Double,
        dateString: String // Format ISO-8601 e.g. YYYY-MM-DDT00:00:00Z
    ) {
        val newBalance = currentBalance - amount
        if (newBalance < 0) {
            throw IllegalArgumentException("Saldo kas tidak mencukupi untuk pengeluaran ini")
        }

        val now = Clock.System.now().toString()

        val dateVal = dateString.substring(0, 10)

        // 1. Catat pengeluaran ke tabel expenses
        supabase.from("expenses").insert(
            buildJsonObject {
                put("date", dateVal)
                put("description", description)
                put("amount", amount)
                put("cash_accounts_id", cashAccountId)
                put("is_cancelled", false)
                put("created_at", dateString)
            }
        )

        // 2. Update saldo kas di tabel cash_accounts
        supabase.from("cash_accounts").update(
            {
                set("current_balance", newBalance)
                set("updated_at", now)
            }
        ) {
            filter {
                eq("id", cashAccountId)
            }
        }

        // 3. Catat transaksi out di cash_logs (source dibatasi oleh DB constraint: "transaksi manual")
        supabase.from("cash_logs").insert(
            buildJsonObject {
                put("cash_accounts_id", cashAccountId)
                put("amount", amount)
                put("running_balance", newBalance)
                put("type", "out")
                put("source", "transaksi manual")
                put("notes", "Pengeluaran: $description")
                put("created_at", dateString)
            }
        )
    }

    suspend fun updateExpenseDescription(expenseId: String, newDescription: String) {
        supabase.from("expenses").update(
            {
                set("description", newDescription)
            }
        ) {
            filter {
                eq("id", expenseId)
            }
        }
    }

    suspend fun cancelExpense(
        expenseId: String,
        amount: Double,
        cashAccountId: String,
        currentBalance: Double,
        expenseDescription: String
    ) {
        val now = Clock.System.now().toString()
        val newBalance = currentBalance + amount

        // 1. Set status is_cancelled = true di tabel expenses
        supabase.from("expenses").update(
            {
                set("is_cancelled", true)
            }
        ) {
            filter {
                eq("id", expenseId)
            }
        }

        // 2. Kembalikan saldo kas di cash_accounts
        supabase.from("cash_accounts").update(
            {
                set("current_balance", newBalance)
                set("updated_at", now)
            }
        ) {
            filter {
                eq("id", cashAccountId)
            }
        }

        // 3. Catat transaksi in di cash_logs (source dibatasi oleh DB constraint: "transaksi manual")
        supabase.from("cash_logs").insert(
            buildJsonObject {
                put("cash_accounts_id", cashAccountId)
                put("amount", amount)
                put("running_balance", newBalance)
                put("type", "in")
                put("source", "transaksi manual")
                put("notes", "Batal Pengeluaran: $expenseDescription")
                put("created_at", now)
            }
        )
    }
}
