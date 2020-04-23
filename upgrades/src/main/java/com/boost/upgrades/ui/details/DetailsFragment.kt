package com.boost.upgrades.ui.details

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.biz2.nowfloats.boost.updates.base_class.BaseFragment
import com.biz2.nowfloats.boost.updates.data.remote.ApiInterface
import com.boost.upgrades.R
import com.boost.upgrades.UpgradeActivity
import com.boost.upgrades.adapter.ReviewViewPagerAdapter
import com.boost.upgrades.adapter.SecondaryImagesAdapter
import com.boost.upgrades.adapter.ZoomOutPageTransformer
import com.boost.upgrades.data.api_model.GetAllFeatures.response.ExtendedProperty
import com.boost.upgrades.data.api_model.GetAllFeatures.response.LearnMoreLink
import com.boost.upgrades.data.api_model.GetAllWidgets.FeatureDetails
import com.boost.upgrades.data.api_model.GetAllWidgets.Review
import com.boost.upgrades.data.api_model.customerId.create.CreateCustomerIDResponse
import com.boost.upgrades.data.model.CartModel
import com.boost.upgrades.data.model.FeaturesModel
import com.boost.upgrades.data.model.WidgetModel
import com.boost.upgrades.database.LocalStorage
import com.boost.upgrades.interfaces.DetailsFragmentListener
import com.boost.upgrades.ui.cart.CartFragment
import com.boost.upgrades.ui.popup.ImagePreviewPopUpFragement
import com.boost.upgrades.ui.webview.WebViewFragment
import com.boost.upgrades.utils.Constants
import com.boost.upgrades.utils.Constants.Companion.CART_FRAGMENT
import com.boost.upgrades.utils.Constants.Companion.IMAGE_PREVIEW_POPUP_FRAGMENT
import com.boost.upgrades.utils.Constants.Companion.WEB_VIEW_FRAGMENT
import com.boost.upgrades.utils.HorizontalMarginItemDecoration
import com.boost.upgrades.utils.Utils.longToast
import com.boost.upgrades.utils.WebEngageController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestListener
//import com.devs.readmoreoption.ReadMoreOption
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.details_fragment.*
import retrofit2.Retrofit
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade


class DetailsFragment : BaseFragment(), DetailsFragmentListener {

    lateinit var root: View
    lateinit var viewModel: DetailsViewModel
    lateinit var detailsViewModelFactory: DetailsViewModelFactory

    lateinit var retrofit: Retrofit
    lateinit var ApiService: ApiInterface
    lateinit var localStorage: LocalStorage
    var singleItemId: String? = null
    var badgeNumber = 0
    var addonDetails: FeaturesModel? = null
    var cart_list: List<CartModel>? = null
    var itemInCartStatus = false
    var widgetLearnMoreLink: String? = null

    lateinit var progressDialog: ProgressDialog

    lateinit var reviewAdaptor: ReviewViewPagerAdapter
    lateinit var secondaryImagesAdapter: SecondaryImagesAdapter

    val imagePreviewPopUpFragement = ImagePreviewPopUpFragement()

    companion object {
        fun newInstance() = DetailsFragment()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.details_fragment, container, false)


        detailsViewModelFactory =
                DetailsViewModelFactory(requireNotNull(requireActivity().application))

        viewModel = ViewModelProviders.of(requireActivity(), detailsViewModelFactory)
                .get(DetailsViewModel::class.java)

        progressDialog = ProgressDialog(requireContext())
        secondaryImagesAdapter = SecondaryImagesAdapter(ArrayList(), this)
        reviewAdaptor = ReviewViewPagerAdapter(ArrayList())
        localStorage = LocalStorage.getInstance(context!!)!!
        singleItemId = arguments!!.getString("itemId")

//        addons_list = localStorage.getInitialLoad()

//        cart_list = localStorage.getCartItems()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spannableString()
        loadData()
        initializeSecondaryImage()
        initializeViewPager()
        initMvvM()


        app_bar_layout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (Math.abs(verticalOffset) - appBarLayout.totalScrollRange == 0) {
                title_appbar.visibility = View.VISIBLE
                title_image.visibility = View.VISIBLE
                // Collapsed
            } else if (verticalOffset == 0) {
                title_appbar.visibility = View.INVISIBLE
                title_image.visibility = View.INVISIBLE

                // Expanded
            } else {
                title_appbar.visibility = View.INVISIBLE
                title_image.visibility = View.INVISIBLE
            }
        }
        )

        abcText.setText(getString(R.string.addons_description))
        description_gradient1.visibility = View.VISIBLE
        var readmoreState = false
        readmore.setOnClickListener {
            if (readmoreState) {
                abcText.maxLines = 10
                readmoreState = false
                readmore.setText(getString(R.string.read_more))
                description_gradient1.visibility = View.VISIBLE
            } else {
                abcText.maxLines = 50
                readmoreState = true
                readmore.setText(getString(R.string.read_less))
                description_gradient1.visibility = View.GONE
            }
        }


        add_item_to_cart.setOnClickListener {
            if (!itemInCartStatus) {
                if (addonDetails != null) {
                    viewModel.addItemToCart(addonDetails!!)
                    badgeNumber = badgeNumber + 1
                    badge121.setText(badgeNumber.toString())
                    badge121.visibility = View.VISIBLE
                    Constants.CART_VALUE = badgeNumber

//                    localStorage.addCartItem(addons_list!!.get(itemId))

                    add_item_to_cart.background = ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.grey_button_click_effect
                    )
                    add_item_to_cart.setTextColor(Color.parseColor("#bbbbbb"))
                    add_item_to_cart.setText(getString(R.string.added_to_cart))
                    havent_bought_the_feature.visibility = View.INVISIBLE
                    itemInCartStatus = true
                }
            }
        }

        imageView121.setOnClickListener {
            (activity as UpgradeActivity).popFragmentFromBackStack()
        }

        imageViewCart121.setOnClickListener {
            (activity as UpgradeActivity).addFragment(
                    CartFragment.newInstance(),
                    CART_FRAGMENT
            )
        }

        fb_layout.setOnClickListener {
            if (widgetLearnMoreLink != null && widgetLearnMoreLink!!.length > 0) {
                val webViewFragment: WebViewFragment = WebViewFragment.newInstance()
                val args = Bundle()
                args.putString("link", widgetLearnMoreLink)
                webViewFragment.arguments = args
                (activity as UpgradeActivity).addFragment(
                        webViewFragment,
                        WEB_VIEW_FRAGMENT
                )
            } else {
                Toasty.warning(requireContext(), "Failed to load the article.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val pos = 2
        reviewViewpager.postDelayed(Runnable { reviewViewpager.setCurrentItem(pos) }, 100)
        getLineCount()
    }

    fun getLineCount(){

        abcText.postDelayed({
            val count = abcText.lineCount
            if(count < 11){
                readmore.visibility = View.INVISIBLE
                description_gradient1.visibility = View.GONE
            }
        }, 300)
    }

    fun loadCostToButtons() {
        if (addonDetails!!.is_premium) {
            add_item_to_cart.visibility = View.VISIBLE
            add_item_to_cart.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.orange_button_click_effect
            )
            add_item_to_cart.setTextColor(Color.WHITE)
            val discount = 100 - addonDetails!!.discount_percent
            val paymentPrice = (discount * addonDetails!!.price) / 100
            money.text = "₹" + paymentPrice + "/month"
            if (discount > 0) {
                orig_cost.visibility = View.VISIBLE
                orig_cost.text = "Original cost ₹" + addonDetails!!.price + "/month"
            } else {
                orig_cost.visibility = View.INVISIBLE
            }
            add_item_to_cart.text = "Add for ₹" + paymentPrice + "/Month"
            havent_bought_the_feature.visibility = View.VISIBLE
        } else {
            add_item_to_cart.visibility = View.GONE
            orig_cost.visibility = View.GONE
            money.text = "Free Forever"
        }
    }

    fun spannableString() {
        val origCost = SpannableString("Original cost ₹799/month")

        origCost.setSpan(
                StrikethroughSpan(),
                15,
                origCost.length,
                0
        )
        orig_cost.setText(origCost)
    }

    fun loadData() {
        viewModel.loadAddonsFromDB(singleItemId!!)
    }

    @SuppressLint("FragmentLiveDataObserve")
    fun initMvvM() {
        viewModel.addonsResult().observe(this, Observer {
            viewModel.getCartItems()
            addonDetails = it
            if (addonDetails != null) {
                val learnMoreLinkType = object : TypeToken<LearnMoreLink>() {}.type
                val learnMoreLink: LearnMoreLink? = if (addonDetails!!.learn_more_link == null) null else Gson().fromJson(addonDetails!!.learn_more_link, learnMoreLinkType)
                Glide.with(this).load(addonDetails!!.primary_image)
                        .into(image1222)

                Glide.with(this).load(addonDetails!!.primary_image)
                        .fitCenter()
                        .into(title_image)


                Glide.with(this).load(addonDetails!!.feature_banner)
                        .transition(withCrossFade())
                        .fitCenter()
                        .into(details_image_bg)

                if (addonDetails!!.secondary_images.isNullOrEmpty())
                    secondary_images_panel.visibility = View.GONE
                else {
                    val objectType = object : TypeToken<ArrayList<String>>() {}.type
                    var secondaryImages = Gson().fromJson<ArrayList<String>>(addonDetails!!.secondary_images, objectType)
                    if (secondaryImages != null && secondaryImages.count() > 0) {
                        addUpdateSecondaryImage(secondaryImages)
//                        val imgSize: Int = 70 * requireContext().getResources().getDisplayMetrics().density.toInt()
//                        val imgPadding: Int = 5 * requireContext().getResources().getDisplayMetrics().density.toInt()
//                        for(img in secondaryImages){
//                            val imageView = ImageView(requireContext())
//                            imageView.layoutParams = LinearLayout.LayoutParams(imgSize, imgSize, 1f)
//                            imageView.setPadding(imgPadding, imgPadding, imgPadding, imgPadding)
//                            imageView.setBackgroundResource(R.drawable.background_image_fade)
//
//                            secondary_images_panel?.addView(imageView)
//                            Glide.with(this).load(img)
//                                    .fitCenter()
//                                    .into(imageView)
//                        }
                    }
                }

                if (addonDetails!!.is_premium)
                    havent_bought_the_feature.visibility = View.VISIBLE
                else
                    havent_bought_the_feature.visibility = View.GONE

                if (addonDetails!!.target_business_usecase != null) {
                    title_top_1.visibility = View.VISIBLE
                    title_top_1.text = addonDetails!!.target_business_usecase
                } else {
                    title_top_1.visibility = View.INVISIBLE
                }
                title_top.text = addonDetails!!.name
                title_appbar.text = addonDetails!!.name
                if (addonDetails!!.discount_percent > 0) {
                    details_discount.visibility = View.VISIBLE
                    details_discount.text = addonDetails!!.discount_percent.toString() + "% OFF"
                } else {
                    details_discount.visibility = View.GONE
                }
                if (addonDetails!!.total_installs.isNullOrEmpty() || addonDetails!!.total_installs.equals("--")) {
                    title_bottom2.text = "Less than 100 businesses have added this"
                }else {
                    val totalInstall = addonDetails!!.total_installs + " businesses have added this"
                    val businessUses = SpannableString(totalInstall)
                    businessUses.setSpan(
                            ForegroundColorSpan(ContextCompat.getColor(context!!, R.color.light_blue)),
                            0,
                            addonDetails!!.total_installs!!.length,
                            0
                    )
                    title_bottom2.text = businessUses
                }

                loadCostToButtons()

                if (learnMoreLink != null) {
                    widgetLearnMore.text = learnMoreLink.link_description
                    widgetLearnMoreLink = learnMoreLink.link
                    fb_layout.visibility = View.VISIBLE
                } else {
                    fb_layout.visibility = View.GONE
                }
                xheader.text = addonDetails!!.description_title
                abcText.text = addonDetails!!.description
                review_layout.visibility = View.GONE

                WebEngageController.trackEvent("ADDONS_MARKETPLACE Feature_Details Loaded", "Feature_Details " + addonDetails!!.boost_widget_key, "")
                WebEngageController.trackEvent("ADDONS_MARKETPLACE Feature_Details - " + addonDetails!!.boost_widget_key + " Loaded", "Feature_Details" + addonDetails!!.boost_widget_key, "")

            }
        })

        viewModel.cartResult().observe(this, Observer {
            cart_list = it
            itemInCartStatus = false
            if (cart_list != null && cart_list!!.size > 0) {
                badge121.visibility = View.VISIBLE
                for (item in cart_list!!) {
                    if (item.boost_widget_key == singleItemId) {
                        add_item_to_cart.background = ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.added_to_cart_grey
                        )
                        add_item_to_cart.setTextColor(Color.parseColor("#bbbbbb"))
                        add_item_to_cart.setText(getString(R.string.added_to_cart))
                        havent_bought_the_feature.visibility = View.INVISIBLE
                        itemInCartStatus = true
                        break
                    }
                }
                badgeNumber = cart_list!!.size
                badge121.setText(badgeNumber.toString())
                Constants.CART_VALUE = badgeNumber
                if (!itemInCartStatus) {
                    loadCostToButtons()
                }
            } else {
                badgeNumber = 0
                badge121.visibility = View.GONE
                itemInCartStatus = false
                loadCostToButtons()
            }
        })

        viewModel.addonsError().observe(this, Observer {
            longToast(requireContext(), "onFailure: " + it)
        })

        viewModel.addonsLoader().observe(this, Observer {
            if (it) {
                val status = "Loading. Please wait..."
                progressDialog.setMessage(status)
                progressDialog.setCancelable(false) // disable dismiss by tapping outside of the dialog
                progressDialog.show()
            } else {
                progressDialog.dismiss()
            }
        })
    }

    fun updateReview(list: List<Review>) {
        reviewAdaptor.addupdates(list)
    }

    fun addUpdateSecondaryImage(list: ArrayList<String>) {
        secondaryImagesAdapter.addUpdates(list)
        secondary_image_recycler.adapter = secondaryImagesAdapter
        secondaryImagesAdapter.notifyDataSetChanged()
    }

    fun initializeSecondaryImage() {
        val gridLayoutManager = GridLayoutManager(requireContext(), 1)
        gridLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        secondary_image_recycler.apply {
            layoutManager = gridLayoutManager
        }
    }

    private fun initializeViewPager() {
        reviewViewpager.adapter = reviewAdaptor
        dots_indicator.setViewPager2(reviewViewpager)
        reviewViewpager.offscreenPageLimit = 1

        reviewViewpager.setPageTransformer(ZoomOutPageTransformer())

        val itemDecoration = HorizontalMarginItemDecoration(
                requireContext(),
                R.dimen.viewpager_current_item_horizontal_margin
        )
        reviewViewpager.addItemDecoration(itemDecoration)
//        viewpager.setPageTransformer(ZoomOutPageTransformer())
    }

    override fun onBackPressed() {
        if (::viewModel.isInitialized) {
            viewModel.getCartItems()
        }
    }

    override fun imagePreviewPosition(list: ArrayList<String>, pos: Int) {
        val args = Bundle()
        args.putInt("position", pos)
        args.putStringArrayList("list", list)
        imagePreviewPopUpFragement.arguments = args
        imagePreviewPopUpFragement.show((activity as UpgradeActivity).supportFragmentManager, IMAGE_PREVIEW_POPUP_FRAGMENT)
    }

}
