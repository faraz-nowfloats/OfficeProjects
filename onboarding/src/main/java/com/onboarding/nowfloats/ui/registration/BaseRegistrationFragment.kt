package com.onboarding.nowfloats.ui.registration

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import androidx.databinding.ViewDataBinding
import com.framework.exceptions.IllegalFragmentTypeException
import com.framework.views.DotProgressBar
import com.onboarding.nowfloats.R
import com.onboarding.nowfloats.base.AppBaseFragment
import com.onboarding.nowfloats.constant.FragmentType
import com.onboarding.nowfloats.constant.IntentConstant
import com.onboarding.nowfloats.extensions.addInt
import com.onboarding.nowfloats.extensions.addParcelable
import com.onboarding.nowfloats.extensions.getInt
import com.onboarding.nowfloats.extensions.getParcelable
import com.onboarding.nowfloats.managers.NavigatorManager
import com.onboarding.nowfloats.model.RequestFloatsModel
import com.onboarding.nowfloats.model.category.CategoryDataModel
import com.onboarding.nowfloats.model.channel.ChannelModel
import com.onboarding.nowfloats.model.navigator.ScreenModel
import com.onboarding.nowfloats.model.navigator.ScreenModel.*
import com.onboarding.nowfloats.model.navigator.ScreenModel.Screen.*
import com.onboarding.nowfloats.ui.startFragmentActivity
import com.onboarding.nowfloats.viewmodel.business.BusinessCreateViewModel

open class BaseRegistrationFragment<binding : ViewDataBinding> : AppBaseFragment<binding, BusinessCreateViewModel>() {

    protected val channels: ArrayList<ChannelModel>
        get() {
            return requestFloatsModel?.channels ?: ArrayList()
        }

    protected val categoryDataModel: CategoryDataModel?
        get() {
            return requestFloatsModel?.categoryDataModel
        }

    protected var requestFloatsModel: RequestFloatsModel? = null

    protected var totalPages = 1
    protected var currentPage = 1

    override fun getLayout(): Int {
        return when (this) {
            is RegistrationBusinessContactInfoFragment -> R.layout.fragment_registration_business_contact_info
            is RegistrationBusinessWebsiteFragment -> R.layout.fragment_registration_business_website
            is RegistrationBusinessFacebookPageFragment -> R.layout.fragment_registration_business_facebook_page
            is RegistrationBusinessFacebookShopFragment -> R.layout.fragment_registration_business_facebook_shop
            is RegistrationBusinessTwitterDetailsFragment -> R.layout.fragment_registration_business_twitter_details
            is RegistrationBusinessWhatsAppFragment -> R.layout.fragment_registration_business_whatsapp
            is RegistrationBusinessApiFragment -> R.layout.fragment_registration_business_api
            is RegistrationCompleteFragment -> R.layout.fragment_registration_complete
            else -> throw IllegalFragmentTypeException()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        when (this) {
            is RegistrationCompleteFragment -> inflater.inflate(R.menu.menu_facebook_profile, menu)
        }
    }

//    override fun getViewModelClass(): Class<BaseViewModel> {
//        return BaseViewModel::class.java
//    }

    override fun onCreateView() {
        requestFloatsModel = arguments?.getParcelable(IntentConstant.REQUEST_FLOATS_INTENT)
        currentPage = arguments?.getInt(IntentConstant.CURRENT_PAGES) ?: currentPage
        totalPages = arguments?.getInt(IntentConstant.TOTAL_PAGES) ?: totalPages
        if (this !is RegistrationCompleteFragment) {
            if (this !is RegistrationBusinessApiFragment) setToolbarTitle(resources.getString(R.string.step) + " $currentPage/$totalPages")
        }
    }

    fun getDotProgress(): DotProgressBar? {
        return DotProgressBar.Builder().setMargin(0).setAnimationDuration(800)
                .setDotBackground(R.drawable.ic_dot).setMaxScale(.7f).setMinScale(0.3f)
                .setNumberOfDots(3).setdotRadius(8).build(baseActivity)
    }

    protected fun gotoBusinessWebsite() {
        startFragmentActivity(FragmentType.REGISTRATION_BUSINESS_WEBSITE, getBundle())
        NavigatorManager.pushToStackAndSaveRequest(ScreenModel(BUSINESS_INFO, getToolbarTitle()), requestFloatsModel)
    }

    protected fun gotoFacebookShop() {
        startFragmentActivity(FragmentType.REGISTRATION_BUSINESS_FACEBOOK_SHOP, getBundle())
        NavigatorManager.pushToStackAndSaveRequest(ScreenModel(BUSINESS_FACEBOOK_SHOP, getToolbarTitle()), requestFloatsModel)
    }

    protected fun gotoFacebookPage() {
        startFragmentActivity(FragmentType.REGISTRATION_BUSINESS_FACEBOOK_PAGE, getBundle())
        NavigatorManager.pushToStackAndSaveRequest(ScreenModel(BUSINESS_FACEBOOK_PAGE, getToolbarTitle()), requestFloatsModel)
    }

    protected fun gotoTwitterDetails() {
        startFragmentActivity(FragmentType.REGISTRATION_BUSINESS_TWITTER_DETAILS, getBundle())
        NavigatorManager.pushToStackAndSaveRequest(ScreenModel(BUSINESS_TWITTER, getToolbarTitle()), requestFloatsModel)
    }

    protected fun gotoWhatsAppCallDetails() {
        startFragmentActivity(FragmentType.REGISTRATION_BUSINESS_WHATSAPP, getBundle())
        NavigatorManager.pushToStackAndSaveRequest(ScreenModel(BUSINESS_WHATSAPP, getToolbarTitle()), requestFloatsModel)
    }

    protected open fun gotoBusinessApiCallDetails() {
        startFragmentActivity(FragmentType.REGISTRATION_BUSINESS_API_CALL, getBundle())
    }

    protected fun gotoRegistrationComplete() {
        startFragmentActivity(FragmentType.REGISTRATION_COMPLETE, getBundle())
    }

    private fun getBundle(): Bundle {
        val bundle = Bundle()
        bundle.addParcelable(IntentConstant.REQUEST_FLOATS_INTENT, requestFloatsModel)
                .addInt(IntentConstant.TOTAL_PAGES, totalPages)
                .addInt(IntentConstant.CURRENT_PAGES, currentPage + 1)
        return bundle
    }

    override fun getViewModelClass(): Class<BusinessCreateViewModel> {
        return BusinessCreateViewModel::class.java
    }
}