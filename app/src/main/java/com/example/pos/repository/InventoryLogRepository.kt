package com.example.pos.repository

import com.example.pos.data.SupabaseClientProvider
import com.example.pos.model.InventoryLog
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
}