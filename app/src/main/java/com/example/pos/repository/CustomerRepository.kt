package com.example.pos.repository

import com.example.pos.data.SupabaseClientProvider
import com.example.pos.model.Customer
import com.example.pos.model.CustomerLog
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class CustomerRepository {

    private val supabase = SupabaseClientProvider.client

    suspend fun getCustomers(): List<Customer> {
        return supabase
            .from("customers")
            .select {
                order("name", Order.ASCENDING)
            }
            .decodeList<Customer>()
    }

    suspend fun createCustomer(name: String, phone: String, isActive: Boolean) {
        val newCustomer = buildJsonObject {
            put("name", name)
            put("phone", phone)
            put("is_active", isActive)
        }

        val created = supabase.from("customers").insert(newCustomer) {
            select()
        }.decodeSingle<Customer>()

        // Simpan log registrasi
        val logDesc = "Registrasi pelanggan baru: ${created.name} (${created.phone})"
        supabase.from("customer_logs").insert(
            buildJsonObject {
                put("customers_id", created.id)
                put("description", logDesc)
                put("before_updated", "-")
            }
        )
    }

    suspend fun updateCustomer(
        id: String,
        name: String,
        phone: String,
        isActive: Boolean,
        oldName: String,
        oldPhone: String,
        oldIsActive: Boolean
    ) {
        val updatedFields = buildJsonObject {
            put("name", name)
            put("phone", phone)
            put("is_active", isActive)
        }

        supabase.from("customers").update(updatedFields) {
            filter {
                eq("id", id)
            }
        }

        // Catat log aktivitas jika ada perubahan data
        val changes = mutableListOf<String>()
        if (name != oldName) changes.add("Nama diubah dari '$oldName' menjadi '$name'")
        if (phone != oldPhone) changes.add("No. Telp diubah dari '$oldPhone' menjadi '$phone'")
        if (isActive != oldIsActive) {
            val oldStatus = if (oldIsActive) "Aktif" else "Nonaktif"
            val newStatus = if (isActive) "Aktif" else "Nonaktif"
            changes.add("Status diubah dari '$oldStatus' menjadi '$newStatus'")
        }

        if (changes.isNotEmpty()) {
            val logDesc = "Perubahan profil pelanggan: " + changes.joinToString(", ")
            val beforeDesc = "Nama: '$oldName', No. Telp: '$oldPhone', Status: '${if (oldIsActive) "Aktif" else "Nonaktif"}'"
            supabase.from("customer_logs").insert(
                buildJsonObject {
                    put("customers_id", id)
                    put("description", logDesc)
                    put("before_updated", beforeDesc)
                }
            )
        }
    }

    suspend fun getCustomerLogs(): List<CustomerLog> {
        return supabase
            .from("customer_logs")
            .select {
                order("created_at", Order.DESCENDING)
            }
            .decodeList<CustomerLog>()
    }
}
