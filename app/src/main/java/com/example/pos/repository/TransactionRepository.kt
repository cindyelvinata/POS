package com.example.pos.repository

import com.example.pos.data.SupabaseClientProvider
import com.example.pos.model.CartItem
import com.example.pos.model.CashAccount
import com.example.pos.model.Customer
import com.example.pos.model.Product
import com.example.pos.model.Sale
import com.example.pos.model.SaleItem
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.datetime.Clock
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class TransactionRepository {

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

                order(
                    "name",
                    Order.ASCENDING
                )
            }
            .decodeList<Product>()
    }

    suspend fun getCustomers(): List<Customer> {

        return supabase
            .from("customers")
            .select {

                filter {
                    eq("is_active", true)
                }

                order(
                    "name",
                    Order.ASCENDING
                )
            }
            .decodeList<Customer>()
    }

    suspend fun getCashAccounts(): List<CashAccount> {

        return supabase
            .from("cash_accounts")
            .select {

                filter {
                    eq("is_active", true)
                }

                order(
                    "name",
                    Order.ASCENDING
                )
            }
            .decodeList<CashAccount>()
    }

    suspend fun getSalesHistory(): List<Sale> {

        return supabase
            .from("sales")
            .select(
                columns = Columns.raw(
                    "*, customers(name), cash_accounts(name)"
                )
            ) {

                order(
                    "transaction_time",
                    Order.DESCENDING
                )
            }
            .decodeList<Sale>()
    }

    suspend fun getSaleItems(
        saleId: String
    ): List<SaleItem> {

        return supabase
            .from("sale_items")
            .select {

                filter {
                    eq(
                        "sales_id",
                        saleId
                    )
                }
            }
            .decodeList<SaleItem>()
    }

    suspend fun checkout(
        customer: Customer,
        cashAccount: CashAccount,
        cartItems: List<CartItem>,
        paidAmount: Double
    ) {

        if (cartItems.isEmpty()) {
            throw IllegalArgumentException(
                "Keranjang masih kosong"
            )
        }

        val total =
            cartItems.sumOf { it.subtotal }

        if (paidAmount < total) {
            throw IllegalArgumentException(
                "Jumlah bayar kurang dari total transaksi"
            )
        }

        cartItems.forEach { item ->

            if (item.quantity <= 0.0) {
                throw IllegalArgumentException(
                    "Jumlah produk tidak valid"
                )
            }

            if (item.quantity > item.product.stock) {
                throw IllegalArgumentException(
                    "Stok ${item.product.name} tidak mencukupi"
                )
            }
        }

        val now =
            Clock.System.now().toString()

        val changeAmount =
            paidAmount - total

        val newCashBalance =
            cashAccount.currentBalance + total

        val sale =
            createSale(
                customer = customer,
                cashAccount = cashAccount,
                total = total,
                paidAmount = paidAmount,
                changeAmount = changeAmount,
                transactionTime = now
            )

        cartItems.forEach { item ->

            createSaleItem(
                saleId = sale.id,
                item = item
            )

            val stockBefore =
                item.product.stock

            val stockAfter =
                stockBefore - item.quantity

            supabase
                .from("products")
                .update(
                    {
                        set(
                            "stock",
                            stockAfter
                        )

                        set(
                            "updated_at",
                            now
                        )
                    }
                ) {

                    filter {
                        eq(
                            "id",
                            item.product.id
                        )
                    }
                }

            inventoryLogRepository
                .createUpdateStockLog(
                    productId = item.product.id,
                    stockBefore = stockBefore,
                    stockAfter = stockAfter
                )
        }

        supabase
            .from("cash_accounts")
            .update(
                {
                    set(
                        "current_balance",
                        newCashBalance
                    )

                    set(
                        "updated_at",
                        now
                    )
                }
            ) {

                filter {
                    eq(
                        "id",
                        cashAccount.id
                    )
                }
            }

        supabase
            .from("cash_logs")
            .insert(
                buildJsonObject {

                    put(
                        "cash_accounts_id",
                        cashAccount.id
                    )

                    put(
                        "amount",
                        total
                    )

                    put(
                        "running_balance",
                        newCashBalance
                    )

                    put(
                        "type",
                        "in"
                    )

                    put(
                        "source",
                        "transaksi manual"
                    )

                    put(
                        "notes",
                        "Penjualan ${sale.id}"
                    )

                    put(
                        "created_at",
                        now
                    )
                }
            )
    }

    private suspend fun createSale(
        customer: Customer,
        cashAccount: CashAccount,
        total: Double,
        paidAmount: Double,
        changeAmount: Double,
        transactionTime: String
    ): Sale {

        return runCatching {

            supabase
                .from("sales")
                .insert(
                    buildJsonObject {

                        put(
                            "customers_id",
                            customer.id
                        )

                        put(
                            "cash_accounts_id",
                            cashAccount.id
                        )

                        put(
                            "total",
                            total
                        )

                        put(
                            "amount_paid",
                            paidAmount
                        )

                        put(
                            "change",
                            changeAmount
                        )

                        put(
                            "status",
                            "Lunas"
                        )

                        put(
                            "transaction_time",
                            transactionTime
                        )
                    }
                ) {
                    select()
                }
                .decodeSingle<Sale>()

        }.recoverCatching {

            supabase
                .from("sales")
                .insert(
                    buildJsonObject {

                        put(
                            "customers_id",
                            customer.id
                        )

                        put(
                            "cash_accounts_id",
                            cashAccount.id
                        )

                        put(
                            "total",
                            total
                        )

                        put(
                            "amount_paid",
                            paidAmount
                        )

                        put(
                            "status",
                            "Lunas"
                        )

                        put(
                            "transaction_time",
                            transactionTime
                        )
                    }
                ) {
                    select()
                }
                .decodeSingle<Sale>()

        }.getOrThrow()
    }

    private suspend fun createSaleItem(
        saleId: String,
        item: CartItem
    ) {

        runCatching {

            supabase
                .from("sale_items")
                .insert(
                    buildJsonObject {

                        put(
                            "sales_id",
                            saleId
                        )

                        put(
                            "products_id",
                            item.product.id
                        )

                        put(
                            "product_name",
                            item.product.name
                        )

                        put(
                            "unit_price",
                            item.product.price
                        )

                        put(
                            "quantity",
                            item.quantity.toInt()
                        )

                        put(
                            "subtotal",
                            item.subtotal
                        )
                    }
                )
        }
    }
}