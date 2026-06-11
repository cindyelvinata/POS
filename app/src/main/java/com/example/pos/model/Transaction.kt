package com.example.pos.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Sale(

    val id: String = "",

    val total: Double = 0.0,

    @SerialName("amount_paid")
    val paidAmount: Double = 0.0,

    @SerialName("change")
    val changeAmount: Double? = null,

    val status: String = "",

    @SerialName("transaction_time")
    val transactionTime: String = "",

    @SerialName("customers_id")
    val customersId: String? = null,

    @SerialName("cash_accounts_id")
    val cashAccountsId: String = "",

    @SerialName("customers")
    val customer: SaleCustomerInfo? = null,

    @SerialName("cash_accounts")
    val cashAccount: SaleCashAccountInfo? = null
)

@Serializable
data class SaleCustomerInfo(

    val name: String = ""
)

@Serializable
data class SaleCashAccountInfo(

    val name: String = ""
)

@Serializable
data class SaleItem(

    val id: String = "",

    @SerialName("sales_id")
    val salesId: String = "",

    @SerialName("products_id")
    val productsId: String = "",

    @SerialName("product_name")
    val productName: String = "",

    @SerialName("unit_price")
    val price: Double = 0.0,

    val quantity: Double = 0.0,

    @SerialName("sub_total")
    val subtotal: Double = 0.0
)

data class CartItem(

    val product: Product,

    val quantity: Double
) {

    val subtotal: Double
        get() = product.price * quantity
}