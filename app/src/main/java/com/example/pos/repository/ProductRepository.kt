package com.example.pos.repository

import com.example.pos.data.SupabaseClientProvider
import com.example.pos.model.Product
import com.example.pos.model.ProductInsert
import com.example.pos.model.ProductUpdate
import io.github.jan.supabase.postgrest.from

class ProductRepository {

    private val supabase = SupabaseClientProvider.client

    /*
     * Ambil semua products
     */
    suspend fun getProducts(): List<Product> {

        return supabase
            .from("products")
            .select()
            .decodeList<Product>()
    }

    /*
     * Tambah product
     */
    suspend fun addProduct(
        name: String,
        price: Double,
        stock: Double
    ) {

        val product = ProductInsert(
            name = name,
            price = price,
            stock = stock,
            isActive = true
        )

        supabase
            .from("products")
            .insert(product)
    }

    /*
     * Update product
     */
    suspend fun updateProduct(
        id: String,
        name: String,
        price: Double,
        stock: Double,
        isActive: Boolean
    ) {

        val product = ProductUpdate(
            name = name,
            price = price,
            stock = stock,
            isActive = isActive
        )

        supabase
            .from("products")
            .update(product) {

                filter {
                    eq("id", id)
                }
            }
    }
}