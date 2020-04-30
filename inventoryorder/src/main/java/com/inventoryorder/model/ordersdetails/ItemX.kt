package com.inventoryorder.model.ordersdetails

import com.inventoryorder.constant.RecyclerViewItemType
import com.inventoryorder.recyclerView.AppBaseRecyclerViewItem
import java.io.Serializable

data class ItemX(
    val ActualPrice: Double? = null,
    val Product: Product? = null,
    val Quantity: Int? = null,
    val SalePrice: Double? = null,
    val ShippingCost: Double? = null,
    val Type: String? = null
) : AppBaseRecyclerViewItem, Serializable {

  override fun getViewType(): Int {
    return RecyclerViewItemType.ITEM_ORDER_DETAILS.getLayout()
  }
}