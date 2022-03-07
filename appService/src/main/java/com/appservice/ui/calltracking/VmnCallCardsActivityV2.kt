package com.appservice.ui.calltracking

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appservice.R
import com.appservice.base.AppBaseActivity
import com.appservice.databinding.ActivityVmnCallCardsV2Binding
import com.appservice.model.VmnCallModel
import com.appservice.recyclerView.AppBaseRecyclerViewAdapter
import com.appservice.recyclerView.BaseRecyclerViewItem
import com.appservice.recyclerView.RecyclerItemClickListener
import com.appservice.utils.WebEngageController
import com.appservice.viewmodel.VmnCallsViewModel
import com.framework.constants.PremiumCode
import com.framework.constants.SupportVideoType
import com.framework.extensions.gone
import com.framework.extensions.visible
import com.framework.pref.Key_Preferences
import com.framework.pref.Key_Preferences.GET_FP_DETAILS_CATEGORY
import com.framework.pref.clientId
import com.framework.utils.*
import com.framework.utils.ExoPlayerUtils.play
import com.framework.views.zero.old.AppFragmentZeroCase
import com.framework.views.zero.old.AppOnZeroCaseClicked
import com.framework.views.zero.old.AppRequestZeroCaseBuilder
import com.framework.views.zero.old.AppZeroCases
import com.framework.webengageconstant.BUSINESS_CALLS
import com.framework.webengageconstant.EVENT_LABEL_BUSINESS_CALLS
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.material.tabs.TabLayout
import com.google.gson.JsonObject
import com.onboarding.nowfloats.constant.IntentConstant
import java.util.ArrayList
import java.util.HashMap

/**
 * Created by Admin on 27-04-2017.
 */
class VmnCallCardsActivityV2 : AppBaseActivity<ActivityVmnCallCardsV2Binding, VmnCallsViewModel>(),
    RecyclerItemClickListener,
    AppOnZeroCaseClicked {
    var seeMoreLessStatus = false
    var totalPotentialCallCount = 0
    var stopApiCall = false
    var headerList: ArrayList<VmnCallModel> = ArrayList<VmnCallModel>()
    var vmnCallAdapter: AppBaseRecyclerViewAdapter<VmnCallModel>? = null
    var selectedViewType = "ALL"
    private var offset = 0
    private var appFragmentZeroCase: AppFragmentZeroCase? = null
    enum class CallType{
        CONNECTED,
        MISSED
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ExoPlayerUtils.newInstance() //required for call playback on list
        mangePlayerOnList()
        appFragmentZeroCase = AppRequestZeroCaseBuilder(
            AppZeroCases.BUSINESS_CALLS,
            this,
            this,
            isPremium
        ).getRequest().build()
        addFragment(binding?.childContainer?.getId(), appFragmentZeroCase, false)
        // MixPanelController.track(MixPanelController.VMN_CALL_TRACKER, null)
        setSupportActionBar(binding?.toolbar)
        if (supportActionBar != null) {
            title = getString(R.string.business_calls)
            supportActionBar!!.setDisplayShowHomeEnabled(false)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        WebEngageController.trackEvent(BUSINESS_CALLS, EVENT_LABEL_BUSINESS_CALLS, null)


        val phone = session.getFPDetails(Key_Preferences.GET_FP_DETAILS_PRIMARY_NUMBER)
        binding?.tvTrackedCall?.text = spanColor(getString(R.string.tracked_calls) + " " +phone,R.color.colorPrimary,phone?:"")


        //tracking calls
        showTrackedCalls()
        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding?.callRecycler?.layoutManager = linearLayoutManager
        binding?.callRecycler?.setHasFixedSize(true)
        vmnCallAdapter = AppBaseRecyclerViewAdapter(this, headerList)
        binding?.callRecycler?.adapter = vmnCallAdapter
        binding?.callRecycler?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val totalItemCount = linearLayoutManager.itemCount
                val lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()
                if (lastVisibleItem >= totalItemCount - 2 && !stopApiCall) {
                    calls()
                }
            }
        })
        setOnClickListener(
            binding?.seeMoreLess, binding?.websiteHelper,
            binding?.phoneHelper, binding?.parentLayout
        )

        websiteCallCount()
        if (isPremium) {
            nonEmptyView()
            binding?.callRecycler?.visible()
            binding?.secondLayout?.visible()
            calls()
        } else {
            binding?.callRecycler?.gone()
            binding?.secondLayout?.gone()

            emptyView()
        }

    }

    private fun mangePlayerOnList() {

        ExoPlayerUtils.onProgressChanged { progress ->
            val model = getCurrentPlayerModel()
            model?.audioPosition = ExoPlayerUtils.player.currentPosition
            model?.audioLength = ExoPlayerUtils.player.duration
            vmnCallAdapter?.notifyItemChanged(currentPlayerIndex(), Unit)
        }


        ExoPlayerUtils.isPlayingChanged = { isPlaying ->
            val model = getCurrentPlayerModel()
            model?.isAudioPlayState = isPlaying
            val playbackState = ExoPlayerUtils.player.playbackState
            when {
                playbackState == Player.STATE_ENDED -> {
                    model?.audioPosition = 0L
                }
            }
            vmnCallAdapter?.notifyItemChanged(currentPlayerIndex(), Unit)
        }

        ExoPlayerUtils.playBackStateChanged={state->
            if (state == ExoPlayer.STATE_BUFFERING){
                showProgress()
            } else {
                hideProgress()
            }
        }

    }

    fun currentPlayerIndex(): Int {
        return ExoPlayerUtils.player.currentMediaItem?.mediaId?.toInt() ?: 0
    }

    fun getCurrentPlayerModel(): VmnCallModel? {
        val index = ExoPlayerUtils.player.currentMediaItem?.mediaId?.toInt()
        if (index != null && vmnCallAdapter?.list?.size ?: 0 > index) {
            return (vmnCallAdapter?.list?.get(index) as VmnCallModel)
        } else
            return null
    }

    private val isPremium: Boolean
        get() {
            val keys = session?.getStoreWidgets()
            return keys != null && keys.contains(PremiumCode.CALLTRACKER.value).not()
        }

    private fun showTrackedCalls() {
        binding?.tableLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == 0) {
                    if (selectedViewType != "ALL") {
                        selectedViewType = "ALL"
                        updateRecyclerData(null)
                    }
                } else if (tab.position == 1) {
                    if (selectedViewType != "MISSED") {
                        selectedViewType = "MISSED"
                        updateRecyclerData(null)
                    }
                } else if (tab.position == 2) {
                    if (selectedViewType != "CONNECTED") {
                        selectedViewType = "CONNECTED"
                        updateRecyclerData(null)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    fun calls() {
        Log.i(TAG, "getCalls: function called")
        stopApiCall = true
        showProgress()
        val startOffset = offset.toString()
        val hashMap = HashMap<String, String?>()
        hashMap["clientId"] = clientId
        hashMap["fpid"] =
            if (session?.iSEnterprise.equals("true")) session?.fPParentId else session?.fPID

        hashMap["offset"] = startOffset
        hashMap["limit"] = 100.toString()
        hashMap["identifierType"] =
            if (session?.iSEnterprise?.equals("true") == true) "MULTI" else "SINGLE"
        viewModel.trackerCalls(hashMap).observe(this) {
            Log.i(TAG, "getCalls success: ")
            val vmnCallModels = (it.anyResponse as? ArrayList<VmnCallModel>)
            setTabCount(vmnCallModels)
            hideProgress()
            if (vmnCallModels == null || it.isSuccess().not()) {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.something_went_wrong),
                    Toast.LENGTH_SHORT
                ).show()
                return@observe
            } else {
                stopApiCall = false
                val size = vmnCallModels.size
                Log.v("getCalls", " $size")
                stopApiCall = size < 100
                if (size<=0&&offset==0) {
                    emptyView()
                    binding?.firstLayout?.gone()
                    binding?.view2?.gone()
                    binding?.secondLayout?.gone()
                    binding?.callRecycler?.gone()
                    appFragmentZeroCase?.setRootBG(R.color.grey_f9f9f9)
                } else {
                    binding?.firstLayout?.visible()
                    binding?.view2?.visible()
                    binding?.secondLayout?.visible()
                    binding?.callRecycler?.visible()

                    nonEmptyView()
                }
                updateRecyclerData(vmnCallModels)

                if (size != 0) {
                    offset += 100
                }

            }
        }
    }

    private fun setTabCount(vmnCallModels: ArrayList<VmnCallModel>?) {
        binding?.tableLayout?.getTabAt(0)?.text =
            getString(R.string.all)+" ("+vmnCallModels?.size+")"
        binding?.tableLayout?.getTabAt(1)?.text =
            getString(R.string.missed)+" ("+vmnCallModels?.filter{
                it.callStatus==CallType.MISSED.name }?.size+")"
        binding?.tableLayout?.getTabAt(2)?.text =
            getString(R.string.received)+" ("+vmnCallModels?.filter{
                it.callStatus==CallType.CONNECTED.name }?.size+")"

    }

    private fun updateRecyclerData(newItems: ArrayList<VmnCallModel>?) {
        ExoPlayerUtils.player.stop()
        if (newItems != null) {
            val sizeOfList = headerList.size
            val listSize = newItems.size
            for (i in 0 until listSize) {
                val model: VmnCallModel = newItems[i]
                headerList.add(model)
                vmnCallAdapter?.notifyItemInserted(sizeOfList + i);
            }
        }
        Log.i(TAG, "updateRecyclerData: header list size " + getSelectedTypeList(headerList).size)
        vmnCallAdapter = AppBaseRecyclerViewAdapter(this, getSelectedTypeList(headerList), this)
        binding?.callRecycler?.adapter = vmnCallAdapter
    }

    private fun getSelectedTypeList(list: ArrayList<VmnCallModel>): ArrayList<VmnCallModel> {
        var selectedItems: ArrayList<VmnCallModel> = ArrayList<VmnCallModel>()
        when (selectedViewType) {
            "ALL" -> {
                selectedItems = list
            }
            CallType.MISSED.name -> {
                var i = 0
                while (i < list.size) {
                    if (list[i].callStatus.equals("MISSED")) {
                        selectedItems.add(list[i])
                    }
                    i++
                }
            }
            CallType.CONNECTED.name -> {
                var i = 0
                while (i < list.size) {
                    if (list[i].callStatus.equals("CONNECTED")) {
                        selectedItems.add(list[i])
                    }
                    i++
                }
            }
        }
        return selectedItems
    }

    private fun showEmptyScreen() {

    }


    fun websiteCallCount() {
        showProgress()

        viewModel.getCallCountByType(
            session.fpTag,
            "POTENTIAL_CALLS",
            "WEB",
        ).observe(this) {

            hideProgress()
            val jsonObject = it.anyResponse as JsonObject
            if (it.isSuccess().not()) {
                showEmptyScreen()
                showSnackBarNegative(
                    this,
                    getString(R.string.something_went_wrong)
                )
            } else {
                val callCount: String = jsonObject.get("POTENTIAL_CALLS").getAsString()
                binding?.webCallCount!!.text = callCount
                totalPotentialCallCount += callCount.toInt()
                binding?.totalNumberOfCalls?.text =
                    "View potential calls ($totalPotentialCallCount)"
                getPhoneCallCount()
            }
        }

    }

    private fun getPhoneCallCount() {
        showProgress()


        viewModel.getCallCountByType(
            session.fpTag,
            "POTENTIAL_CALLS",
            "MOBILE"
        ).observe(this) {
            hideProgress()
            if (it.isSuccess().not()) {
                showEmptyScreen()
                showSnackBarNegative(
                    this,
                    getString(R.string.something_went_wrong)
                )
            } else {
                val jsonObject = it.anyResponse as JsonObject

                val callCount: String = jsonObject.get("POTENTIAL_CALLS").getAsString()
                binding?.phoneCallCount?.text = callCount
                totalPotentialCallCount += callCount.toInt()
                binding?.totalNumberOfCalls!!.text =
                    "View potential calls ($totalPotentialCallCount)"
            }

        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onClick(v: View?) {
        super.onClick(v)
        when (v) {

            binding?.seeMoreLess -> {
                if (!seeMoreLessStatus) {
                    seeMoreLessStatus = true
                    binding?.seeMoreLessImage?.rotation=90F
                    binding?.helpWebPhoneLayout?.setVisibility(View.VISIBLE)
                } else {
                    seeMoreLessStatus = false
                    binding?.seeMoreLessImage?.rotation=270F
                    binding?.helpWebPhoneLayout?.setVisibility(View.GONE)

                    //hide info
                    binding?.helpWebsiteInfo?.setVisibility(View.GONE)
                    binding?.helpPhoneInfo?.setVisibility(View.GONE)
                }
            }
            binding?.websiteHelper -> {
                binding?.helpPhoneInfo?.setVisibility(View.GONE)
                if (binding?.helpWebsiteInfo?.getVisibility() == View.VISIBLE) {
                    binding?.helpWebsiteInfo?.setVisibility(View.GONE)
                } else {
                    binding?.helpWebsiteInfo?.setVisibility(View.VISIBLE)
                }
            }
            binding?.phoneHelper -> {
                binding?.helpWebsiteInfo?.setVisibility(View.GONE)
                if (binding?.helpPhoneInfo?.getVisibility() == View.VISIBLE) {
                    binding?.helpPhoneInfo?.setVisibility(View.GONE)
                } else {
                    binding?.helpPhoneInfo?.setVisibility(View.VISIBLE)
                }
            }
            binding?.parentLayout -> {
                if (seeMoreLessStatus) {
                    seeMoreLessStatus = false
                    binding?.seeMoreLessImage?.setImageResource(R.drawable.vmn_down_arrow)
                    binding?.helpWebPhoneLayout?.setVisibility(View.GONE)

                    //hide info
                    binding?.helpWebsiteInfo?.setVisibility(View.GONE)
                    binding?.helpPhoneInfo?.setVisibility(View.GONE)
                }
            }
        }
    }


    private fun initiateBuyFromMarketplace() {
        showProgress()
        val intent = Intent(this, Class.forName("com.boost.upgrades.UpgradeActivity"))
        intent.putExtra("expCode", session?.fP_AppExperienceCode)
        intent.putExtra("fpName", session?.fPName)
        intent.putExtra("fpid", session?.fPID)
        intent.putExtra("fpTag", session?.fpTag)
        intent.putExtra("accountType", session?.getFPDetails(GET_FP_DETAILS_CATEGORY))
        intent.putStringArrayListExtra(
            "userPurchsedWidgets",
            session.getStoreWidgets()?.toArrayList()
        )
        if (session.userPrimaryMobile != null) {
            intent.putExtra("email", session.userPrimaryMobile)
        } else {
            intent.putExtra("email", getString(R.string.ria_customer_mail))
        }
        if (session.userPrimaryMobile != null) {
            intent.putExtra("mobileNo", session?.userPrimaryMobile)
        } else {
            intent.putExtra("mobileNo", getString(R.string.ria_customer_number))
        }
        intent.putExtra("profileUrl", session?.fPLogo)
        intent.putExtra("buyItemKey", "CALLTRACKER")
        startActivity(intent)
        Handler().postDelayed({ hideProgress() }, 1000)
    }

    private fun nonEmptyView() {
        binding?.childContainer?.setVisibility(View.GONE)
    }

    private fun emptyView() {
        binding?.childContainer?.setVisibility(View.VISIBLE)
    }

    override fun primaryButtonClicked() {
        initiateBuyFromMarketplace()
    }

    override fun secondaryButtonClicked() {
        try {
            startActivity(
                Intent(
                    this,
                    Class.forName("com.onboarding.nowfloats.ui.supportVideo.SupportVideoPlayerActivity")
                )
                    .putExtra(IntentConstant.SUPPORT_VIDEO_TYPE.name, SupportVideoType.TOB.value)
            )
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
    }

    override fun ternaryButtonClicked() {}
    override fun appOnBackPressed() {}
    override fun onStop() {
        super.onStop()
        if (vmnCallAdapter?.getItemCount() ?: 0 > 1) {
            InAppReviewUtils.showInAppReview(
                this,
                InAppReviewUtils.Events.in_app_review_out_of_customer_calls
            )
        }
    }

    companion object {
        private const val TAG = "VmnCallCardsActivity"
    }

    override fun getLayout(): Int {
        return R.layout.activity_vmn_call_cards_v2
    }

    override fun getViewModelClass(): Class<VmnCallsViewModel> {
        return VmnCallsViewModel::class.java
    }


    override fun onItemClick(position: Int, item: BaseRecyclerViewItem?, actionType: Int) {
        when (actionType) {
            com.appservice.constant.RecyclerViewActionType.VMN_PLAY_CLICKED.ordinal -> {
                if ((ExoPlayerUtils.player.isLoading || ExoPlayerUtils.player.isPlaying) && position != ExoPlayerUtils.player.currentMediaItem?.mediaId?.toInt()) {
                   // showLongToast(getString(R.string.you_can_only_play_one_audio_clip))
                    ExoPlayerUtils.player.pause()
                }
                    if (ExoPlayerUtils.player.isPlaying) {
                        ExoPlayerUtils.player.pause()
                    } else {
                        val listItem = item as VmnCallModel

                        if (position != ExoPlayerUtils.player.currentMediaItem?.mediaId?.toInt() || getCurrentPlayerModel()?.audioPosition == 0L) {
                            listItem.callRecordingUri?.let { it1 -> play(it1, position,listItem.audioPosition) }
                        } else {
                            ExoPlayerUtils.player.play()
                        }

                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ExoPlayerUtils.release()
    }
}