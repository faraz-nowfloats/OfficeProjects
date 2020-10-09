package com.inventoryorder.model.orderRequest

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ItemsItem(
    @SerializedName("Type")
    val type: String = "",
    @SerializedName("ProductDetails")
    val productDetails: ProductDetails,
    @SerializedName("Quantity")
    val quantity: Int = 0,
    @SerializedName("ProductOrOfferId")
    val productOrOfferId: String = ""
):Serializable