package com.example.pos.repository

import com.example.pos.data.SupabaseClientProvider
import com.example.pos.model.Product
import com.example.pos.model.ProductInsert
import com.example.pos.model.ProductUpdate
import io.github.jan.supabase.postgrest.from

class ProductRepository {

    private val supabase =
        SupabaseClientProvider.client

    private val inventoryLogRepository =
        InventoryLogRepository()

    suspend fun getProducts(): List<Product> {

        return supabase
            .from("products")
            .select {

                filter {
                    eq("is_active", true)
                }
            }
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

    suspend fun updateProduct(
        id: String,
        name: String,
        price: Double,
        stock: Double,
        isActive: Boolean
    ) {

        println("========== UPDATE PRODUCT ==========")

        val currentProduct = supabase
            .from("products")
            .select {
                filter {
                    eq("id", id)
                }
            }
            .decodeSingle<Product>()

        println("ID : $id")
        println("Nama Lama : ${currentProduct.name}")
        println("Stock Lama : ${currentProduct.stock}")
        println("Stock Baru : $stock")

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

        println("UPDATE PRODUK BERHASIL")

        if (currentProduct.stock != stock) {

            println("STOCK BERUBAH")
            println("MEMBUAT INVENTORY LOG")

            inventoryLogRepository.createUpdateStockLog(
                productId = id,
                stockBefore = currentProduct.stock,
                stockAfter = stock
            )

            println("INVENTORY LOG BERHASIL")
        } else {

            println("STOCK TIDAK BERUBAH")
        }
    }

    suspend fun deleteProduct(
        id: String
    ) {

        supabase
            .from("products")
            .update(
                mapOf(
                    "is_active" to false
                )
            ) {

                filter {
                    eq("id", id)
                }
            }
    }
}