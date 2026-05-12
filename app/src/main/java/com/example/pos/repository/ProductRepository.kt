package com.example.pos.repository

import com.example.pos.data.SupabaseClientProvider
import com.example.pos.model.Product
import com.example.pos.model.ProductInsert
import io.github.jan.supabase.postgrest.from

class ProductRepository {

    private val supabase = SupabaseClientProvider.client

    suspend fun getProducts(): List<Product> {

        return supabase
            .from("products")
            .select()
            .decodeList<Product>()
    }

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
}