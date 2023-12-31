package com.boost.cart.adapter

import android.app.Activity
import android.content.Context
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.boost.cart.R
import com.boost.cart.interfaces.CartFragmentListener
import com.boost.cart.utils.SharedPrefs
import com.boost.cart.utils.Utils.priceCalculatorForYear
import com.boost.cart.utils.Utils.yearlyOrMonthlyOrEmptyValidity
import com.boost.dbcenterapi.upgradeDB.model.CartModel
import com.boost.dbcenterapi.upgradeDB.model.WidgetModel
import com.boost.dbcenterapi.utils.WebEngageController
import com.bumptech.glide.Glide
import com.framework.webengageconstant.ADDONS_MARKETPLACE
import com.framework.webengageconstant.ADDONS_MARKETPLACE_ADD_ON_CROSSED_DELETED_FROM_CART
import java.text.NumberFormat
import java.util.*


class CartAddonsAdaptor(cardItems: List<CartModel>?, val listener: CartFragmentListener, val activity: Activity) :
  RecyclerView.Adapter<CartAddonsAdaptor.upgradeViewHolder>() {

  private var list = ArrayList<CartModel>()
  private lateinit var context: Context
  val prefs = SharedPrefs(activity)

  init {
    this.list = cardItems as ArrayList<CartModel>
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): upgradeViewHolder {
    val itemView = LayoutInflater.from(parent.context).inflate(
      R.layout.item_feature_one, parent, false
    )
    context = itemView.context

    return upgradeViewHolder(itemView)
  }

  override fun getItemCount(): Int {
    return list.size
  }

  override fun onBindViewHolder(holder: upgradeViewHolder, position: Int) {
    Glide.with(context).load(list.get(position).link).placeholder(R.drawable.boost_360_insignia)
      .into(holder.image)
    val price = priceCalculatorForYear(list.get(position).price * list.get(position).min_purchase_months, list.get(position).widget_type, activity)
    val MRPPrice = priceCalculatorForYear(list.get(position).MRPPrice * list.get(position).min_purchase_months, list.get(position).widget_type, activity)
    holder.price.text = "₹" + NumberFormat.getNumberInstance(Locale.ENGLISH).format(price) + yearlyOrMonthlyOrEmptyValidity(list.get(position).widget_type, activity)
    if (price != MRPPrice) {
      spannableString(holder, MRPPrice, list.get(position))
      holder.MRPPrice.visibility = View.VISIBLE
    } else {
      holder.MRPPrice.visibility = View.GONE
    }
    if(list.get(position).boost_widget_key!!.contains("DOMAINPURCHASE")
//      || list.get(position).boost_widget_key!!.contains("EMAILACCOUNTS")
//      || list.get(position).boost_widget_key!!.contains("CALLTRACKER")
//      || list.get(position).boost_widget_key!!.contains("IVR")
    ){
      if(!prefs.getSelectedDomainName().isNullOrEmpty()) {
        holder.title.text = prefs.getSelectedDomainName()
        holder.desc.text = list.get(position).item_name
      }else{
        holder.desc.text = list.get(position).description_title
        holder.title.text = list.get(position).item_name
      }
    } else if(list.get(position).boost_widget_key!!.contains("CALLTRACKER")
    //     || list.get(position).boost_widget_key!!.contains("IVR")
    ) {
      if(!prefs.getSelectedVMNName().isNullOrEmpty()) {
        holder.title.text = prefs.getSelectedVMNName()
        holder.desc.text = list.get(position).item_name
      }else{
        holder.desc.text = list.get(position).description_title
        holder.title.text = list.get(position).item_name
      }
    }else {
      holder.desc.text = list.get(position).description_title
      holder.title.text = list.get(position).item_name
    }

//    if (list.get(position).discount > 0) {
//      holder.discount.text = list.get(position).discount.toString() + "%"
//    } else {
//      holder.discount.visibility = View.GONE
//    }
    holder.remove_addons.setOnClickListener {
      list.get(position).item_name?.let { it1 ->
        WebEngageController.trackEvent(
          ADDONS_MARKETPLACE_ADD_ON_CROSSED_DELETED_FROM_CART,
          ADDONS_MARKETPLACE,
          it1
        )
      }
      listener.deleteCartAddonsItem(list.get(position))
      //remove selected domain
      if(list.get(position).boost_widget_key!!.contains("DOMAINPURCHASE")) {
          prefs.storeSelectedDomainName(null)
        }
      if(list.get(position).boost_widget_key!!.contains("CALLTRACKER")
      //  || list.get(position).boost_widget_key!!.contains("IVR")
      ) {
        prefs.storeSelectedVMNName(null)
      }
    }
//    holder.view.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
//    holder.view.visibility = if (list.size - 1 == position) View.GONE else View.VISIBLE
  }

  fun addupdates(cardItems: List<CartModel>) {
    val initPosition = list.size
    list.clear()
    list.addAll(cardItems)
    notifyItemRangeInserted(initPosition, list.size)
  }

  class upgradeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var image = itemView.findViewById<ImageView>(R.id.addons_profile_image)!!
    var remove_addons = itemView.findViewById<ImageView>(R.id.addons_remove)!!
    var title = itemView.findViewById<TextView>(R.id.addons_title)!!
    var price = itemView.findViewById<TextView>(R.id.cart_item_price)!!
    var MRPPrice = itemView.findViewById<TextView>(R.id.cart_item_orig_cost)!!
    var desc=itemView.findViewById<TextView>(R.id.desc)
 //   var discount = itemView.findViewById<TextView>(R.id.cart_item_discount)!!
//    var view = itemView.findViewById<View>(R.id.cart_single_addons_bottom_view)!!


    fun upgradeListItem(updateModel: WidgetModel) {

    }
  }

  fun spannableString(holder: upgradeViewHolder, value: Double, singleItem: CartModel) {
    val origCost = SpannableString(
        "₹" + NumberFormat.getNumberInstance(Locale.ENGLISH)
          .format(value) + yearlyOrMonthlyOrEmptyValidity(singleItem.widget_type, activity))

    origCost.setSpan(
      StrikethroughSpan(),
      0,
      origCost.length,
      0
    )
    holder.MRPPrice.text = origCost
  }
}