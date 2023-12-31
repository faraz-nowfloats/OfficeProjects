package com.boost.marketplace.ui.popup.call_track

import android.app.Application
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.boost.cart.CartActivity
import com.boost.dbcenterapi.data.api_model.GetAllFeatures.response.Bundles
import com.boost.dbcenterapi.upgradeDB.model.FeaturesModel
import com.boost.dbcenterapi.utils.SharedPrefs
import com.boost.dbcenterapi.utils.WebEngageController
import com.boost.marketplace.R
import com.boost.marketplace.databinding.CallTrackingAddToCartPopupBinding
import com.boost.marketplace.interfaces.DetailsFragmentListener
import com.boost.marketplace.ui.details.FeatureDetailsViewModel
import com.boost.marketplace.ui.details.call_track.CallTrackingActivity
import com.boost.marketplace.ui.details.domain.CustomDomainActivity
import com.boost.marketplace.ui.popup.removeItems.RemovePackageBottomSheet
import com.framework.base.BaseBottomSheetDialog
import com.framework.pref.UserSessionManager
import com.framework.pref.getAccessTokenAuth
import com.framework.webengageconstant.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import es.dmoral.toasty.Toasty

class CallTrackAddToCartBottomSheet :
    BaseBottomSheetDialog<CallTrackingAddToCartPopupBinding, FeatureDetailsViewModel>(),DetailsFragmentListener {

    var blockedItem: String? = null
    var clientid: String = "2FA76D4AFCD84494BD609FDB4B3D76782F56AE790A3744198E6F517708CAAA21"
    var experienceCode: String? = null
    var fpid: String? = null
    var email: String? = null
    var mobileNo: String? = null
    var profileUrl: String? = null
    var accountType: String? = null
    var isDeepLink: Boolean = false
    var isOpenCardFragment: Boolean = false
    var deepLinkViewType: String = ""
    var deepLinkDay: Int = 7

    //    var result: Boolean? = null
    var userPurchsedWidgets = java.util.ArrayList<String>()
    var itemInCartStatus = false
    lateinit var singleAddon: FeaturesModel
    lateinit var progressDialog: ProgressDialog
    lateinit var prefs: SharedPrefs
    var numberPrice: String? = null
    var vmnSelectionForCart: Boolean = false
    var doVMNBooking: Boolean = false
    var result: Long? = null
    lateinit var removePackageBottomSheet: RemovePackageBottomSheet

    override fun getLayout(): Int {
        return R.layout.call_tracking_add_to_cart_popup
    }

    companion object {
        fun newInstance() = CallTrackAddToCartBottomSheet()
    }

    override fun getViewModelClass(): Class<FeatureDetailsViewModel> {
        return FeatureDetailsViewModel::class.java
    }

    override fun onCreateView() {
        experienceCode = requireArguments().getString("expCode")
        fpid = requireArguments().getString("fpid")
        itemInCartStatus = requireArguments().getBoolean("itemInCartStatus", false)
        vmnSelectionForCart = requireArguments().getBoolean("vmnSelectionForCart", false)
        doVMNBooking = requireArguments().getBoolean("doVMNBooking", false)
        isDeepLink = requireArguments().getBoolean("isDeepLink", false)
        deepLinkViewType = requireArguments().getString("deepLinkViewType") ?: ""
        deepLinkDay = requireArguments().getString("deepLinkDay")?.toIntOrNull() ?: 7
        email = requireArguments().getString("email")
        mobileNo = requireArguments().getString("mobileNo")
        profileUrl = requireArguments().getString("profileUrl")
        accountType = requireArguments().getString("accountType")
        isOpenCardFragment = requireArguments().getBoolean("isOpenCardFragment", false)
        userPurchsedWidgets =
            requireArguments().getStringArrayList("userPurchsedWidgets") ?: java.util.ArrayList()
        val jsonString = requireArguments().getString("bundleData")
        singleAddon =
            Gson().fromJson<FeaturesModel>(jsonString, object : TypeToken<FeaturesModel>() {}.type)
        viewModel?.setApplicationLifecycle(Application(), this)
        viewModel = ViewModelProviders.of(this).get(FeatureDetailsViewModel::class.java)
        progressDialog = ProgressDialog(context)
        prefs = SharedPrefs(baseActivity)
        blockedItem = requireArguments().getString("number")
        binding?.tvTitle?.text = requireArguments().getString("number")
        numberPrice = requireArguments().getString("price")
        binding?.tvCart?.text = "Add to cart at $numberPrice"
        removePackageBottomSheet = RemovePackageBottomSheet(this)


//        loadData()
        initMVVM()
        binding?.backBtn?.setOnClickListener {
            dismiss()
        }

        binding?.tvTitle?.text = blockedItem
        if (vmnSelectionForCart) {
            binding?.tvCart?.text = "Select above VMN"
        } else {
            if (doVMNBooking) {
                binding?.tvCart?.text = "Book VMN"
            } else {
                binding?.tvCart?.text = "Add to cart at $numberPrice"
            }
        }

        binding?.tvCart?.setOnClickListener {
            if (vmnSelectionForCart) {
                prefs.storeSelectedVMNName(blockedItem!!)
                activity?.finish()
                dismiss()
                return@setOnClickListener
            }
            if (doVMNBooking) {
                val num =  blockedItem!!.replace("+91 ","0").replace("-","")
                viewModel!!.bookVMNPostPurchase(getAccessToken(), clientid,fpid.toString(), num,requireActivity())
            } else {
                if (itemInCartStatus == true) {
                    val args = Bundle()
                    args.putString("addonName", blockedItem!!)
                    removePackageBottomSheet.arguments = args
                    fragmentManager?.let { it1 ->
                        removePackageBottomSheet.show(
                            it1,
                            RemovePackageBottomSheet::class.java.name
                        )
                    }
                } else {
                    if (itemInCartStatus == false) {
                        val event_attributes: java.util.HashMap<String, Any> = java.util.HashMap()
                        event_attributes.put("Addon Discounted Price", singleAddon.price)
                        event_attributes.put("Addon Tag", singleAddon.name.toString())
                        event_attributes.put("Addon Discount %", singleAddon.discount_percent)
                        event_attributes.put("Addon Name", singleAddon.name.toString())
                        event_attributes.put("Addon Price", singleAddon.price)
                        event_attributes.put(
                            "Addon activatedDate",
                            singleAddon.activatedDate.toString()
                        )
                        event_attributes.put("Addon expiryDate", singleAddon.expiryDate.toString())


                        WebEngageController.trackEvent(
                            ADD_ON_MARKET_PLACE_ADDED_TO_CART,
                            ADDONS_MARKETPLACE,
                            event_attributes
                        )
                        if (blockedItem != null) {
                            if (singleAddon != null) {
                                prefs.storeCartOrderInfo(null)
                                prefs.storeSelectedVMNName(blockedItem!!)
                                viewModel!!.addItemToCart1(singleAddon, baseActivity, blockedItem!!)
                                val event_attributes: HashMap<String, Any> = HashMap()
                                singleAddon.name?.let { it1 ->
                                    event_attributes.put(
                                        "Addon Name",
                                        it1
                                    )
                                }
                                event_attributes.put("Addon Price", singleAddon.price)
                                event_attributes.put(
                                    "Addon Discounted Price",
                                    getDiscountedPrice(
                                        singleAddon.price,
                                        singleAddon.discount_percent
                                    )
                                )
                                event_attributes.put(
                                    "Addon Discount %",
                                    singleAddon.discount_percent
                                )
                                event_attributes.put("Addon Validity", 1)
                                event_attributes.put(
                                    "Addon Feature Key",
                                    singleAddon.boost_widget_key
                                )
                                singleAddon.target_business_usecase?.let { it1 ->
                                    event_attributes.put(
                                        "Addon Tag",
                                        it1
                                    )
                                }
                                WebEngageController.trackEvent(
                                    ADDONS_MARKETPLACE_FEATURE_ADDED_TO_CART,
                                    ADDONS_MARKETPLACE,
                                    event_attributes
                                )
                                itemInCartStatus = true
                            }


                        }
                    } else {
                        Toasty.error(
                            requireContext(),
                            "Number not available, please select other",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    val intent = Intent(context, CartActivity::class.java)
                    intent.putExtra("fpid", fpid)
                    intent.putExtra("expCode", experienceCode)
                    intent.putExtra("isDeepLink", isDeepLink)
                    intent.putExtra("deepLinkViewType", deepLinkViewType)
                    intent.putExtra("deepLinkDay", deepLinkDay)
                    intent.putExtra("isOpenCardFragment", isOpenCardFragment)
                    intent.putExtra("accountType", accountType)
                    intent.putStringArrayListExtra("userPurchsedWidgets", userPurchsedWidgets)
                    if (email != null) {
                        intent.putExtra("email", email)
                    } else {
                        intent.putExtra("email", "ria@nowfloats.com")
                    }
                    if (mobileNo != null) {
                        intent.putExtra("mobileNo", mobileNo)
                    } else {
                        intent.putExtra("mobileNo", "9160004303")
                    }
                    intent.putExtra("profileUrl", profileUrl)
                    startActivity(intent)
                    dismiss()

                }
            }
        }


    }

        private fun initMVVM() {
            viewModel?.vmnBooking()?.observe(this, Observer {
                requireActivity().finish()
            })
    }

    fun getAccessToken(): String {
        return UserSessionManager(requireContext()).getAccessTokenAuth()?.barrierToken() ?: ""
    }

    fun getDiscountedPrice(price: Double, discountPercent: Double): Double {
        return price - ((discountPercent / 100) * price)
    }

    override fun imagePreviewPosition(list: ArrayList<String>, pos: Int) {

    }

    override fun onPackageClicked(item: Bundles?) {

    }

    override fun itemAddedToCart(status: Boolean) {
        viewModel?.getCartItems()
    }

    override fun goToCart() {
        navigateToCart()
    }

    fun navigateToCart() {
        val intent = Intent(
            context,
            CartActivity::class.java
        )
        intent.putExtra("fpid", fpid)
        intent.putExtra("expCode", experienceCode)
        intent.putExtra("isDeepLink", isDeepLink)
        intent.putExtra("deepLinkViewType", deepLinkViewType)
        intent.putExtra("deepLinkDay", deepLinkDay)
        intent.putExtra("isOpenCardFragment", isOpenCardFragment)
        intent.putExtra(
            "accountType",
            accountType
        )
        intent.putStringArrayListExtra(
            "userPurchsedWidgets",
            userPurchsedWidgets
        )
        if (email != null) {
            intent.putExtra("email", email)
        } else {
            intent.putExtra("email", "ria@nowfloats.com")
        }
        if (mobileNo != null) {
            intent.putExtra("mobileNo", mobileNo)
        } else {
            intent.putExtra("mobileNo", "9160004303")
        }
        intent.putExtra("profileUrl", profileUrl)
        startActivity(intent)
    }
}