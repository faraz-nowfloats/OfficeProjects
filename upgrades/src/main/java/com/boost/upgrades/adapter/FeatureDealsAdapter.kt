package com.boost.upgrades.adapter

import android.graphics.Color
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.biz2.nowfloats.boost.updates.persistance.local.AppDatabase
import com.boost.upgrades.R
import com.boost.upgrades.UpgradeActivity
import com.boost.upgrades.data.api_model.GetAllFeatures.response.Bundles
import com.boost.upgrades.data.api_model.GetAllFeatures.response.FeatureDeals
import com.boost.upgrades.data.model.CartModel
import com.boost.upgrades.data.model.FeaturesModel
import com.boost.upgrades.data.model.WidgetModel
import com.boost.upgrades.interfaces.HomeListener
import com.bumptech.glide.Glide
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.package_item.view.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class FeatureDealsAdapter(
        val list: ArrayList<FeatureDeals>, val cartList: ArrayList<CartModel>, val activity: UpgradeActivity, val homeListener: HomeListener)
    : RecyclerView.Adapter<FeatureDealsAdapter.PagerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder {
        val item = View.inflate(parent.getContext(), R.layout.feature_deals_item, null)
        val lp = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        )
        item.layoutParams = lp
        return PagerViewHolder(item)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {

        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val date = inputFormat.parse(list.get(position).deal_end_date);
        val currentDate = Calendar.getInstance()
        val diffInMilliconds = date.time - currentDate.timeInMillis
        val diffInSeconds = TimeUnit.SECONDS.convert(diffInMilliconds, TimeUnit.MILLISECONDS)
        val diffInMinutes = TimeUnit.MINUTES.convert(diffInMilliconds, TimeUnit.MILLISECONDS)
        val diffInHours = TimeUnit.HOURS.convert(diffInMilliconds, TimeUnit.MILLISECONDS)
        val diffInDays = TimeUnit.DAYS.convert(diffInMilliconds, TimeUnit.MILLISECONDS)

        getFeatureInfoFromDB(holder, list.get(position).feature_code, diffInMilliconds)

        if (diffInMilliconds > 0) {
            object : CountDownTimer(diffInMilliconds, 1000) {
                override fun onFinish() {
                    holder.submit.background = ContextCompat.getDrawable(
                            activity.applicationContext,
                            R.drawable.added_to_cart_grey
                    )
                    holder.submit.setTextColor(Color.parseColor("#bbbbbb"))
                    holder.submit.setText("DEAL HAS EXPIRED!!")
                    holder.submit.isClickable = false
                }

                override fun onTick(millisUntilFinished: Long) {
                    val timerFormat = SimpleDateFormat()
                    if (diffInDays > 0) {
                        timerFormat.applyPattern("dd'd':HH'h':mm'm':ss's'")
                    } else if (diffInHours > 0) {
                        timerFormat.applyPattern("HH'h':mm'm':ss's'")
                    } else if (diffInMinutes > 0) {
                        timerFormat.applyPattern("mm'm':ss's'")
                    } else if (diffInSeconds > 0) {
                        timerFormat.applyPattern("ss's'")
                    }
                    holder.timer.setText(timerFormat.format(Date(millisUntilFinished)).toString())
                }
            }.start()
        } else {
            //deal of the day expired
            holder.submit.background = ContextCompat.getDrawable(
                    activity.applicationContext,
                    R.drawable.added_to_cart_grey
            )
            holder.submit.setTextColor(Color.parseColor("#bbbbbb"))
            holder.submit.setText("DEAL HAS EXPIRED!!")
            holder.submit.isClickable = false
        }
    }


    fun addupdates(item: List<FeatureDeals>, cartItems: List<CartModel>) {
        val initPosition = list.size
        list.clear()
        list.addAll(item)
        notifyItemRangeInserted(initPosition, list.size)

        //update cartList
        val initCartPosition = cartList.size
        cartList.clear()
        cartList.addAll(cartItems)
        notifyItemRangeInserted(initCartPosition, cartList.size)
    }

    class PagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timer = itemView.findViewById<TextView>(R.id.deal_value)
        val title = itemView.findViewById<TextView>(R.id.title)
        val name = itemView.findViewById<TextView>(R.id.details)
        val image = itemView.findViewById<ImageView>(R.id.feature_profile)
        val offerPrice = itemView.findViewById<TextView>(R.id.offer_price)
        val origCost = itemView.findViewById<TextView>(R.id.orig_cost)
        val discount = itemView.findViewById<TextView>(R.id.discount)
        val submit = itemView.findViewById<TextView>(R.id.add_deals_to_cart)
        val imageLayout = itemView.findViewById<CardView>(R.id.imageLayout)
    }

    fun getFeatureInfoFromDB(holder: PagerViewHolder, itemId: String, remainingTime: Long) {
        var mrpPrice = 0.0
        var grandTotal = 0.0
        CompositeDisposable().add(
                AppDatabase.getInstance(activity.application)!!
                        .featuresDao()
                        .getFeaturesItemById(itemId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                {
                                    val featureModel = it
                                    holder.title.setText(it.target_business_usecase)
                                    holder.name.setText(it.name)
                                    if (it.primary_image != null) {
                                        Glide.with(holder.itemView.context).load(it.primary_image).into(holder.image)
                                        holder.imageLayout.visibility = View.VISIBLE
                                    } else {
                                        holder.imageLayout.visibility = View.INVISIBLE
                                    }
                                    if (it.discount_percent > 0) {
                                        holder.discount.setText(it.discount_percent.toString() + "%")
                                        holder.discount.visibility = View.VISIBLE
                                    } else {
                                        holder.discount.visibility = View.GONE
                                    }
                                    val total = (it.price - ((it.price * it.discount_percent) / 100.0))
                                    grandTotal += total
                                    mrpPrice += it.price

                                    holder.offerPrice.setText("₹" + grandTotal + "/month")
                                    if (grandTotal != mrpPrice) {
                                        spannableString(holder, mrpPrice)
                                        holder.origCost.visibility = View.VISIBLE
                                    } else {
                                        holder.origCost.visibility = View.GONE
                                    }
                                    if (remainingTime > 0) {
                                        holder.submit.setOnClickListener(object : View.OnClickListener {
                                            override fun onClick(v: View?) {
                                                homeListener.onAddFeatureDealItemToCart(featureModel, 1)
                                                holder.submit.background = ContextCompat.getDrawable(
                                                        activity.applicationContext,
                                                        R.drawable.added_to_cart_grey
                                                )
                                                holder.submit.setTextColor(Color.parseColor("#bbbbbb"))
                                                holder.submit.setText(activity.getString(R.string.added_to_cart))
                                                holder.submit.isClickable = false
                                            }

                                        })
                                        var itemInCart = false
                                        for (cart in cartList) {
                                            if (itemId.equals(cart.boost_widget_key)) {
                                                itemInCart = true
                                                break
                                            }
                                        }
                                        if (!itemInCart) {
                                            holder.submit.background = ContextCompat.getDrawable(
                                                    activity.applicationContext,
                                                    R.drawable.feature_deals_click_effect
                                            )
                                            holder.submit.setTextColor(activity.resources.getColor(R.color.app_text_yellow))
                                            holder.submit.setText("Add to cart")
                                        } else {
                                            holder.submit.background = ContextCompat.getDrawable(
                                                    activity.applicationContext,
                                                    R.drawable.added_to_cart_grey
                                            )
                                            holder.submit.setTextColor(Color.parseColor("#bbbbbb"))
                                            holder.submit.setText(activity.getString(R.string.added_to_cart))
                                            holder.submit.isClickable = false
                                        }
                                    }
                                },
                                {
                                    it.printStackTrace()
                                }
                        )
        )
    }

    fun spannableString(holder: PagerViewHolder, value: Double) {
        val origCost = SpannableString("₹" + value + "/month")

        origCost.setSpan(
                StrikethroughSpan(),
                0,
                origCost.length,
                0
        )
        holder.origCost.setText(origCost)
    }
}