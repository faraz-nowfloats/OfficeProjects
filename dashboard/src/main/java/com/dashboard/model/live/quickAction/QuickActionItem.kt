package com.dashboard.model.live.quickAction

import androidx.annotation.RawRes
import com.dashboard.R
import com.dashboard.constant.RecyclerViewItemType
import com.dashboard.recyclerView.AppBaseRecyclerViewItem
import com.framework.base.BaseResponse
import com.framework.models.USER_MERCHANT_DETAIL
import com.framework.models.UserProfileDataResult
import com.framework.utils.*
import java.io.Serializable

const val LIST_OF_CLICKED_ITEMS = "LIST_OF_CLICKED_ITEMS"

class QuickActionItem(
  var title: String? = null,
  var quickActionType: String = "",
  var isNew: Boolean = false,
  var isLotty: Boolean = false,
) : BaseResponse(), Serializable, AppBaseRecyclerViewItem {

  override fun getViewType(): Int {
    return RecyclerViewItemType.QUICK_ACTION_ITEM_VIEW.getLayout()
  }

  enum class QuickActionType(var icon: Int, @RawRes var lotty: Int? = null) {
    POST_STATUS_STORY(R.drawable.ic_post_new_story_d),
    CREATE_NEW_UPDATE(R.drawable.ic_createnew_update_d, R.raw.post_update_anim),
    READY_MADE_UPDATES(R.drawable.ic_readymade_update_d, R.raw.post_update_anim),
    //POST_NEW_UPDATE(R.drawable.ic_post_edit_d, R.raw.post_update_anim),
    ADD_PHOTO_GALLERY(R.drawable.ic_add_photo_d), PLACE_CONSULT(R.drawable.ic_video_consulation_d),
    PLACE_APPOINTMENT(R.drawable.ic_book_appointment_d), PLACE_ORDER_BOOKING(R.drawable.ic_new_apt_d),
    ADD_TESTIMONIAL(R.drawable.ic_testimonials_qck),
    LIST_SERVICES(R.drawable.ic_add_service_d), ADD_CUSTOM_PAGE(R.drawable.ic_custom_page_d),
    ADD_STAFF_MEMBER(R.drawable.ic_add_doctor_staff_member_d),
    ADD_TABLE_BOOKING(R.drawable.ic_new_table_booking),
    ADD_MENU(R.drawable.ic_add_menu_item_d), ADD_PROJECT(R.drawable.ic_add_project_d),
    UPLOAD_BROCHURE(R.drawable.ic_upload_brochure_d), ADD_TEAM_MEMBER(R.drawable.ic_add_doctor_staff_member_d),
    MAKE_ANNOUNCEMENT(R.drawable.ic_add_announcement_d), LIST_TOPPER(R.drawable.ic_add_topper_testimonial_d),
    ADD_COURSE(R.drawable.ic_add_new_course_d),
    ADD_UPCOMING_BATCH(R.drawable.ic_new_upcmng_btchs),
    ADD_FACULTY_MEMBER(R.drawable.ic_add_doctor_staff_member_d), ADD_SLIDER_BANNER(R.drawable.ic_slider_banner_d),
    POST_SEASONAL_OFFER(R.drawable.ic_new_post_an_offer),
    ADD_ROOM_TYPE(R.drawable.ic_add_room_type_d),
    SERVICES_OFFERS(R.drawable.ic_new_post_an_offer),
    ADD_NEARBY_ATTRACTION(R.drawable.ic_add_nearby_attraction_d),
    ADD_PRODUCT(R.drawable.ic_add_product_d), ADD_SERVICE(R.drawable.ic_add_service_d), LIST_DRUG_MEDICINE(R.drawable.ic_add_product_d),
    LIST_PRODUCT(R.drawable.ic_add_product_d), ADD_STAFF_PROFILE(R.drawable.ic_add_doctor_staff_member_d);

    companion object {
      fun from(name: String): QuickActionType? = values().firstOrNull { it.name == name }
    }
  }

  companion object {
    fun saveListOfClickedItems(itemsClicked: List<String> = listOf()) {
      val distinct = itemsClicked.toList().toSet().toList()
      PreferencesUtils.instance.saveData(LIST_OF_CLICKED_ITEMS, convertListObjToString(distinct) ?: "")
    }

    fun getListOfClickedItems(): List<String>? {
      return convertStringToList(PreferencesUtils.instance.getData(LIST_OF_CLICKED_ITEMS, "") ?: "")
    }
  }
}