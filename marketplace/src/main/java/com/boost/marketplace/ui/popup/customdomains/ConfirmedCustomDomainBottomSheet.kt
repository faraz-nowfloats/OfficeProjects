package com.boost.marketplace.ui.popup.customdomains

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.boost.cart.CartActivity
import com.boost.dbcenterapi.data.api_model.CustomDomain.Domain
import com.boost.dbcenterapi.data.api_model.GetAllFeatures.response.Bundles
import com.boost.dbcenterapi.upgradeDB.model.FeaturesModel
import com.boost.dbcenterapi.utils.SharedPrefs
import com.boost.dbcenterapi.utils.WebEngageController
import com.boost.marketplace.R
import com.boost.marketplace.databinding.PopupConfirmedCustomDomainBinding
import com.boost.marketplace.interfaces.DetailsFragmentListener
import com.boost.marketplace.ui.details.domain.CustomDomainActivity
import com.boost.marketplace.ui.details.domain.CustomDomainViewModel
import com.boost.marketplace.ui.popup.removeItems.RemovePackageBottomSheet
import com.framework.analytics.SentryController
import com.framework.base.BaseBottomSheetDialog
import com.framework.pref.UserSessionManager
import com.framework.pref.getAccessTokenAuth
import com.framework.webengageconstant.ADDONS_MARKETPLACE
import com.framework.webengageconstant.ADDONS_MARKETPLACE_FEATURE_ADDED_TO_CART
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ConfirmedCustomDomainBottomSheet(val activity: CustomDomainActivity) :
    BaseBottomSheetDialog<PopupConfirmedCustomDomainBinding, CustomDomainViewModel>(),
    DetailsFragmentListener {

    var blockedItem: String?=null
    var clientid: String = "2FA76D4AFCD84494BD609FDB4B3D76782F56AE790A3744198E6F517708CAAA21"
    var experienceCode: String? = null
    var fpid: String? = null
    var domainPricing:String?=null
    var email: String? = null
    var mobileNo: String? = null
    var profileUrl: String? = null
    var accountType: String? = null
    var isDeepLink: Boolean = false
    var doDomainBooking: Boolean = false
    var domainSelectionForCart: Boolean = false
    var isOpenCardFragment: Boolean = false
    var deepLinkViewType: String = ""
    var deepLinkDay: Int = 7
    var userPurchsedWidgets = java.util.ArrayList<String>()
    var allDomainsList: List<Domain>? = null
    var  result:Boolean? = null
    var itemInCartStatus = false
    lateinit var singleAddon: FeaturesModel
    lateinit var progressDialog: ProgressDialog
    lateinit var prefs: SharedPrefs
    lateinit var removePackageBottomSheet: RemovePackageBottomSheet

    override fun getLayout(): Int {
        return R.layout.popup_confirmed_custom_domain
    }

    override fun getViewModelClass(): Class<CustomDomainViewModel> {
        return CustomDomainViewModel::class.java
    }

    override fun onCreateView() {
        blockedItem = requireArguments().getString("blockedItem")
        domainPricing= requireArguments().getString("price")
        itemInCartStatus = requireArguments().getBoolean("itemInCartStatus",false)
        experienceCode = requireArguments().getString("expCode")
        fpid = requireArguments().getString("fpid")
        doDomainBooking = requireArguments().getBoolean("doDomainBooking", false)
        domainSelectionForCart = requireArguments().getBoolean("domainSelectionForCart", false)
        isDeepLink = requireArguments().getBoolean("isDeepLink", false)
        deepLinkViewType = requireArguments().getString("deepLinkViewType") ?: ""
        deepLinkDay =requireArguments().getString("deepLinkDay")?.toIntOrNull() ?: 7
        email = requireArguments().getString("email")
        mobileNo = requireArguments().getString("mobileNo")
        profileUrl = requireArguments().getString("profileUrl")
        accountType = requireArguments().getString("accountType")
        isOpenCardFragment =requireArguments().getBoolean("isOpenCardFragment", false)
        userPurchsedWidgets = requireArguments().getStringArrayList("userPurchsedWidgets") ?: java.util.ArrayList()
        val jsonString = requireArguments().getString("bundleData")
        singleAddon = Gson().fromJson<FeaturesModel>(jsonString, object : TypeToken<FeaturesModel>() {}.type)
        viewModel?.setApplicationLifecycle(Application(), this)
        progressDialog = ProgressDialog(context)
        prefs = SharedPrefs(baseActivity)
        removePackageBottomSheet = RemovePackageBottomSheet(this)


        binding?.tvTitle?.text=blockedItem
        if(domainSelectionForCart){
            binding?.tvCart?.text = "Select above domain"
        }else {
            if (doDomainBooking) {
                binding?.tvCart?.text = "Book Domain"
            } else {
                binding?.tvCart?.text = "Add to cart at $domainPricing"
            }
        }

        //loadData()
        initMVVM()

        binding?.backBtn?.setOnClickListener {
            dismiss()
        }

        binding?.tvCart?.setOnClickListener {
            if(domainSelectionForCart){
                prefs.storeSelectedDomainName(blockedItem!!)
                activity.finish()
                dismiss()
                return@setOnClickListener
            }
            if (doDomainBooking) {
                viewModel?.bookDomainActivation(blockedItem!!, requireActivity().application, requireActivity())
            } else {

                if(itemInCartStatus == true){
                    val args = Bundle()
                    args.putString("addonName", blockedItem!!)
                    removePackageBottomSheet.arguments = args
                    fragmentManager?.let { it1 ->
                        removePackageBottomSheet.show(
                            it1,
                            RemovePackageBottomSheet::class.java.name
                        )
                    }
                }else {
                    if(itemInCartStatus == false) {
                        prefs.storeCartOrderInfo(null)
                        prefs.storeSelectedDomainName(blockedItem!!)
                        blockedItem?.let { it1 ->
                            viewModel?.addItemToCart1(
                                singleAddon,
                                it1
                            )
                        }
                        val event_attributes: HashMap<String, Any> = HashMap()
                        singleAddon!!.name?.let { it1 ->
                            event_attributes.put(
                                "Addon Name",
                                it1
                            )
                        }
                        event_attributes.put("Addon Price", singleAddon!!.price)
                        event_attributes.put(
                            "Addon Discounted Price",
                            getDiscountedPrice(
                                singleAddon!!.price,
                                singleAddon!!.discount_percent
                            )
                        )
                        event_attributes.put(
                            "Addon Discount %",
                            singleAddon!!.discount_percent
                        )
                        event_attributes.put("Addon Validity", 1)
                        event_attributes.put(
                            "Addon Feature Key",
                            singleAddon!!.boost_widget_key
                        )
                        singleAddon!!.target_business_usecase?.let { it1 ->
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
                        viewModel?.getCartItems()
                    }
                    dismiss()

                    val pref = context?.getSharedPreferences("nowfloatsPrefs", Context.MODE_PRIVATE)
                    val fpTag = pref?.getString("GET_FP_DETAILS_TAG", null)
                    val intent = Intent(context, CartActivity::class.java)
                    intent.putExtra("fpid", fpid)
                    intent.putExtra("fpTag", fpTag)
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
                }



// old implementation of add to cart without replacing packs in cart
//                  if (blockedItem != null && result == false) {
//                if (!itemInCartStatus) {
//                    if (singleAddon != null) {
//                        prefs.storeCartOrderInfo(null)
//                        prefs.storeSelectedDomainName(blockedItem)
//                        viewModel!!.addItemToCart1(singleAddon, blockedItem ?: "")
//                        val event_attributes: HashMap<String, Any> = HashMap()
//                        singleAddon.name?.let { it1 -> event_attributes.put("Addon Name", it1) }
//                        event_attributes.put("Addon Price", singleAddon.price)
//                        event_attributes.put(
//                            "Addon Discounted Price",
//                            getDiscountedPrice(singleAddon.price, singleAddon.discount_percent)
//                        )
//                        event_attributes.put("Addon Discount %", singleAddon.discount_percent)
//                        event_attributes.put("Addon Validity", 1)
//                        event_attributes.put("Addon Feature Key", singleAddon.boost_widget_key)
//                        singleAddon.target_business_usecase?.let { it1 ->
//                            event_attributes.put(
//                                "Addon Tag",
//                                it1
//                            )
//                        }
//                        WebEngageController.trackEvent(
//                            ADDONS_MARKETPLACE_FEATURE_ADDED_TO_CART,
//                            ADDONS_MARKETPLACE,
//                            event_attributes
//                        )
//                        itemInCartStatus = true
//                    }
//                }
//                val intent = Intent(context, CartActivity::class.java)
//                intent.putExtra("fpid", fpid)
//                intent.putExtra("expCode", experienceCode)
//                intent.putExtra("isDeepLink", isDeepLink)
//                intent.putExtra("deepLinkViewType", deepLinkViewType)
//                intent.putExtra("deepLinkDay", deepLinkDay)
//                intent.putExtra("isOpenCardFragment", isOpenCardFragment)
//                intent.putExtra("accountType", accountType)
//                intent.putStringArrayListExtra("userPurchsedWidgets", userPurchsedWidgets)
//                if (email != null) {
//                    intent.putExtra("email", email)
//                } else {
//                    intent.putExtra("email", "ria@nowfloats.com")
//                }
//                if (mobileNo != null) {
//                    intent.putExtra("mobileNo", mobileNo)
//                } else {
//                    intent.putExtra("mobileNo", "9160004303")
//                }
//                intent.putExtra("profileUrl", profileUrl)
//                startActivity(intent)
//                dismiss()
                //   }
//            else if (blockedItem!=null && result ==true){
//                Toasty.error(requireContext(), "Domain unavailable select other", Toast.LENGTH_SHORT).show()
//            }
            }
        }
    }

//    private fun loadData() {
//        blockedItem?.let {
//            fpid?.let { it1 ->
//                viewModel!!.domainStatus(
//                    (this).getAccessToken() ?: "", it1,
//                    "2FA76D4AFCD84494BD609FDB4B3D76782F56AE790A3744198E6F517708CAAA21", it
//                )
//            }
//        }
//    }

    private fun initMVVM() {
//        viewModel?.updateStatus()?.observe(this, androidx.lifecycle.Observer{
//            result=it.Result
//        })

        viewModel?.updatesLoader()?.observe(this, Observer {
            if (it.length>0) {
                showProgress(it)
            } else {
                hideProgress()
            }
        })

        viewModel?.domainBookingStatus()?.observe(this, Observer {
            requireActivity().finish()
        })
    }

    private fun showProgress(message: String = "Please wait...") {
        try {
            if (!progressDialog.isShowing) {
                progressDialog.setMessage(message)
                progressDialog.setCancelable(false)
                progressDialog.show()
            }
        } catch (e: Exception) {
            SentryController.captureException(e)
            e.printStackTrace()
        }
    }

    private fun hideProgress() {
        try {
            if (progressDialog.isShowing) progressDialog.cancel()
        } catch (e: Exception) {
            SentryController.captureException(e)
            e.printStackTrace()
        }
    }

    fun getAccessToken(): String {
        return UserSessionManager(requireContext()).getAccessTokenAuth()?.barrierToken() ?: ""
    }

    private fun getDiscountedPrice(price: Double, discountPercent: Double): Double {
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