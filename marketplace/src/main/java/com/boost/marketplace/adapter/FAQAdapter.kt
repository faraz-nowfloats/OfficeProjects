package com.boost.marketplace.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.boost.dbcenterapi.upgradeDB.local.AppDatabase
import com.boost.marketplace.R
import com.boost.marketplace.interfaces.HomeListener
import com.boost.marketplace.ui.home.MarketPlaceActivity
import com.framework.analytics.SentryController
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class FAQAdapter(
  val activity: Activity,
  cryptoCurrencies: List<String>?
) : RecyclerView.Adapter<FAQAdapter.upgradeViewHolder>() {

  private var upgradeList = ArrayList<String>()
  private lateinit var context: Context

  init {
    this.upgradeList = cryptoCurrencies as ArrayList<String>
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): upgradeViewHolder {
    val itemView = LayoutInflater.from(parent?.context).inflate(
      R.layout.faq_item, parent, false
    )
    context = itemView.context

    return upgradeViewHolder(itemView)
  }

  override fun getItemCount(): Int {
    return 5//upgradeList.size
  }

  override fun onBindViewHolder(holder: upgradeViewHolder, position: Int) {
    holder.title.setOnClickListener {
      if(holder.desc.isVisible) {
        holder.desc.visibility = View.GONE
      }else{
        holder.desc.visibility = View.VISIBLE
      }
    }
    holder.downArrow.setOnClickListener {
      if(holder.desc.isVisible) {
        holder.desc.visibility = View.GONE
      }else{
        holder.desc.visibility = View.VISIBLE
      }
    }
  }

  fun addupdates(upgradeModel: List<String>) {
    val initPosition = upgradeList.size
    upgradeList.clear()
    upgradeList.addAll(upgradeModel)
    notifyItemRangeInserted(initPosition, upgradeList.size)
  }

  class upgradeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var position = itemView.findViewById<TextView>(R.id.position)
    var title = itemView.findViewById<TextView>(R.id.title)
    var downArrow = itemView.findViewById<ImageView>(R.id.arrow_btn)
    var desc = itemView.findViewById<TextView>(R.id.desc)
  }
}