package com.boost.marketplace.adapter

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.boost.marketplace.R
import com.boost.marketplace.interfaces.HomeListener
import com.boost.marketplace.ui.details.call_track.CallTrackingActivity
import java.util.*

class MatchNumberListAdapter(
    val activity: CallTrackingActivity,
    cryptoCurrencies: ArrayList<String>?,
    searchValue: Char?,
    val listener: HomeListener
) : RecyclerView.Adapter<MatchNumberListAdapter.upgradeViewHolder>() {

    private var upgradeList = ArrayList<String>()
    private lateinit var context: Context
    private var searchItem: Char? = null

    init {
        this.upgradeList = cryptoCurrencies as ArrayList<String>
        this.searchItem = searchValue
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): upgradeViewHolder {
        val itemView = LayoutInflater.from(parent?.context).inflate(
            R.layout.layout_number_list, parent, false
        )
        context = itemView.context
        return upgradeViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return upgradeList.size
    }

    override fun onBindViewHolder(holder: upgradeViewHolder, position: Int) {
        if (searchItem != null) {

            val startPos = upgradeList[position]
                .indexOf(searchItem!!)


            if (startPos != -1) {
                val spannable = SpannableString(upgradeList[position])
                spannable.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(context, R.color.black)),
                    startPos,
                    startPos,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    startPos,
                    startPos,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                holder.title.text = spannable
            }
        } else {
            holder.title.text = upgradeList[position]

        }
    }
    fun addupdates(upgradeModel: ArrayList<String>) {
        val initPosition = upgradeList.size
        upgradeList.clear()
        upgradeList.addAll(upgradeModel)
        notifyItemRangeInserted(initPosition, upgradeList.size)
    }

    class upgradeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title = itemView.findViewById<TextView>(R.id.tv_title)

    }


}