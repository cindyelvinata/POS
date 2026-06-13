package com.example.pos.repository

import android.util.Log
import com.example.pos.data.SupabaseClientProvider
import com.example.pos.model.InventoryLog
import com.example.pos.model.InventoryLogInsert
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest

class InventoryLogRepository {

    private val supabase =
        SupabaseClientProvider.client

    suspend fun getLogs(): List<InventoryLog> {

        return supabase
            .postgrest
            .rpc("get_inventory_logs")
            .decodeList<InventoryLog>()
    }

    suspend fun createUpdateStockLog(
        productId: String,
        stockBefore: Double,
        stockAfter: Double
    ) {

        val quantity =
            stockAfter - stockBefore

        Log.d(
            "INVENTORY_LOG",
            """
            productId = $productId
            stockBefore = $stockBefore
            stockAfter = $stockAfter
            quantity = $quantity
            """.trimIndent()
        )

        val log = InventoryLogInsert(
            productsId = productId,
            quantity = quantity,
            stockBefore = stockBefore,
            stockAfter = stockAfter,
            type = "edit",
            description = "Update stock produk"
        )

        supabase
            .from("inventory_logs")
            .insert(log)
    }
}