package com.dashboard.recyclerView

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.dashboard.R
import com.dashboard.constant.RecyclerViewItemType.*
import com.dashboard.controller.ui.customisationnav.holder.WebsiteNavHolder
import com.dashboard.controller.ui.more.holder.AboutViewHolder
import com.dashboard.controller.ui.more.holder.UsefulLinksHolder
import com.dashboard.databinding.*
import com.dashboard.holder.*
import com.dashboard.holder.v2.*
import com.framework.base.BaseActivity
import java.util.*

open class AppBaseRecyclerViewAdapter<T : AppBaseRecyclerViewItem>(
  activity: BaseActivity<*, *>, list: ArrayList<T>, itemClickListener: RecyclerItemClickListener? = null
) : BaseRecyclerViewAdapter<T>(activity, list, itemClickListener) {

  override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseRecyclerViewHolder<*> {
    val inflater = LayoutInflater.from(parent.context)
    val recyclerViewItemType = values().first { it.getLayout() == viewType }
    val binding = getViewDataBinding(inflater, recyclerViewItemType, parent)
    return when (recyclerViewItemType) {
      PAGINATION_LOADER -> PagingViewHolder(binding as PaginationLoaderBinding)
      CHANNEL_ITEM_VIEW -> ChannelViewHolder(binding as ItemChannelDBinding)
      CHANNEL_STATUS_ITEM_VIEW -> ChannelSocialMediaViewHolder(binding as ItemSocialMediaBinding)
      BUSINESS_SETUP_ITEM_VIEW -> BusinessSetupViewHolder(binding as ItemBusinessManagementBinding)
      BUSINESS_SETUP_HIGH_ITEM_VIEW -> BusinessSetupHighViewHolder(binding as ItemBusinessSetupHighBinding)
      QUICK_ACTION_ITEM_VIEW -> QuickActionViewHolder(binding as ItemQuickActionBinding)
      RIA_ACADEMY_ITEM_VIEW -> RiaAcademyViewHolder(binding as ItemRiaAcademyBinding)
      BOOST_PREMIUM_ITEM_VIEW -> BoostPremiumViewHolder(binding as ItemBoostPremiumBinding)
      ROI_SUMMARY_ITEM_VIEW -> RoiSummaryViewHolder(binding as ItemRoiSummaryBinding)
      MANAGE_BUSINESS_ITEM_VIEW -> ManageBusinessViewHolder(binding as ItemManageBusinessDBinding)
      GROWTH_STATE_ITEM_VIEW -> GrowthStateViewHolder(binding as ItemGrowthStateBinding)
      BUSINESS_CONTENT_SETUP_ITEM_VIEW -> BusinessContentSetupViewHolder(binding as ItemBusinessContentSetupBinding)
      ITEMS_CONTENT_SETUP_ITEM_VIEW -> ItemContentSetupHolder(binding as ItemContentSetupManageBinding)
      ALL_BOOST_ADD_ONS_VIEW -> BoostAddOnsViewHolder(binding as ItemBoostAddOnsBinding)
      BOOST_ENQUIRIES_ITEM_VIEW -> EnquiriesItemViewHolder(binding as ItemCustomerPatientItemBinding)
      BOOST_WEBSITE_ITEM_VIEW -> WebsiteItemViewHolder(binding as ItemWebsiteItemV2Binding)
      FILTER_DATE_VIEW -> DateFilterViewHolder(binding as ItemFilterDateBinding)
      WEBSITE_COLOR_VIEW -> WebsiteColorViewHolder(binding as RecyclerItemColorsBinding)
      WEBSITE_FONT_VIEW -> WebSiteFontViewHolder(binding as RecyclerItemSelectFontBinding)
      RECYCLER_USEFUL_LINKS -> UsefulLinksHolder(binding as RecyclerItemUsefulLinksBinding)
      RECYCLER_ABOUT_APP -> AboutViewHolder(binding as RecyclerItemAboutAppBinding)
      BOOST_WEBSITE_ITEM_FEATURE_VIEW -> WebsiteItemFeatureViewHolder(binding  as WebsiteItemFeatureV2Binding)
//      BOOST_WEBSITE_ITEM_FEATURE_VIEW -> WebsiteItemFeatureViewHolder(binding as WebsiteItemFeatureBinding)
      RECYCLER_WEBSITE_NAV -> WebsiteNavHolder(binding = binding as RecyclerItemWebsiteNavBinding)
      CONSULTATION_VIEW -> ConsultationViewHolder(binding as RecyclerItemConsultationBinding)

      //My To Do List Cards View Holders
      OPTIONAL_TASKS_VIEW -> OptionalTasksViewHolder(binding as ItemOptionalTasksBinding)
      POST_PURCHASE_1_VIEW -> PostPurchase1ViewHolder(binding as ItemPostPurchase1Binding)
      POST_PURCHASE_2_VIEW -> PostPurchase2ViewHolder(binding as ItemPostPurchase2Binding)
      MY_TODO_LIST_ACTION -> MyToDoActionItemViewHolder(binding as ItemTodoListActionBinding)
      READINESS_SCORE_2_VIEW -> ReadinessScore2ViewHolder(binding as ItemReadinessScore2Binding)
      RENEWAL_1_VIEW -> Renewal1ViewHolder(binding as ItemRenewal1Binding)
      RENEWAL_2_VIEW -> Renewal2ViewHolder(binding as ItemRenewal2Binding)
      RENEWAL_3_VIEW -> Renewal3ViewHolder(binding as ItemRenewal3Binding)
      NEW_SINGLE_FEATURE_VIEW -> NewSingleFeatureViewHolder(binding as ItemNewSingleFeatureBinding)
      NEW_MULTIPLE_FEATURE_VIEW -> NewMultipleFeatureViewHolder(binding as ItemNewMultipleFeatureBinding)
      CONTINUE_WHERE_LEFT_VIEW -> ContinueWhereLeftViewHolder(binding as ItemContinueWhereLeftBinding)
      MISSED_CALL_1_VIEW -> MissedCall1ViewHolder(binding as ItemMissedCall1Binding)
      MISSED_CALL_2_VIEW -> MissedCall2ViewHolder(binding as ItemMissedCall2Binding)
      MISSED_CALL_3_VIEW -> MissedCall3ViewHolder(binding as ItemMissedCall3Binding)
      MISSED_CALL_4_VIEW -> MissedCall4ViewHolder(binding as ItemMissedCall4Binding)
      ORDER_DETAILS_VIEW -> OrderDetailsViewHolder(binding as ItemOrderDetailsCardBinding)
      ATTENTION_ORDER_ALERT_VIEW -> AttentionOrderViewHolder(binding as ItemAttentionOrderAlertBinding)
      SMS_EMAIL_ENQUIRY_VIEW -> SMSEmailEnquiryViewHolder(binding as ItemSmsEmailEnquiryBinding)
    }
  }

  fun runLayoutAnimation(
    recyclerView: RecyclerView?,
    anim: Int = R.anim.layout_animation_fall_down
  ) = recyclerView?.apply {
    layoutAnimation = AnimationUtils.loadLayoutAnimation(context, anim)
    notifyDataSetChanged()
    scheduleLayoutAnimation()
  }

  override fun getItemViewType(position: Int): Int {
    return if (isLoaderVisible) {
      return if (position == list.size - 1) PAGINATION_LOADER.getLayout() else super.getItemViewType(
        position
      )
    } else super.getItemViewType(position)
  }

  fun notify(list: ArrayList<T>?) {
    list?.let { updateList(it) }
  }

  open fun addItems(addList: ArrayList<T>?) {
    addList?.let { list.addAll(it) }
    notifyDataSetChanged()
  }

  override fun getItemCount(): Int {
    return if (list.isNotEmpty()) list.size else 0
  }


  open fun remove(item: T) {
    val position = list.indexOf(item)
    if (position > -1) {
      list.removeAt(position)
      notifyItemRemoved(position)
    }
  }

  open fun clear() {
    isLoaderVisible = false
    while (itemCount > 0) {
      getItem(0)?.let { remove(it) }
    }
  }

  open fun isEmpty(): Boolean {
    return itemCount == 0
  }

  open fun addLoadingFooter(t: T) {
    isLoaderVisible = true
    list.add(t)
    notifyItemInserted(list.size - 1)
  }

  open fun removeLoadingFooter() {
    isLoaderVisible = false
    val position = list.size - 1
    if (position > -1) {
      val item: T? = getItem(position)
      if (item != null) {
        list.removeAt(position)
        notifyItemRemoved(position)
      }
    }
  }

  open fun getItem(position: Int): T? {
    return list[position]
  }

  open fun list(): ArrayList<T> {
    return list
  }

  // New Function
  open fun clearAllItem() {
    val size = itemCount
    list.clear()
    notifyItemRangeRemoved(0, size)
  }

  open fun insertItem(`object`: T, index: Int) {
    list.add(index, `object`)
    notifyItemInserted(index)
  }

  open fun positionItem(item: T): Int {
    return list.indexOf(item)
  }

  open fun addItem(`object`: T) {
    list.add(`object`)
    notifyItemInserted(itemCount - 1)
  }

  open fun removeItem(`object`: T) {
    val position: Int = positionItem(`object`)
    list.remove(`object`)
    notifyItemRemoved(position)
  }

  open fun sortItem(comparator: Comparator<in T?>?) {
    Collections.sort(list, comparator)
    notifyItemRangeChanged(0, itemCount)
  }
  // New Function
}