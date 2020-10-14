package com.boost.upgrades.ui.confirmation

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.biz2.nowfloats.boost.updates.base_class.BaseFragment

import com.boost.upgrades.R
import com.boost.upgrades.UpgradeActivity
import com.boost.upgrades.utils.SharedPrefs
import com.boost.upgrades.utils.WebEngageController
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.order_confirmation_fragment.*
import java.text.SimpleDateFormat
import java.util.*

class AutoRenewOrderConfirmationFragment : BaseFragment() {

    lateinit var prefs: SharedPrefs

    companion object {
        fun newInstance() = AutoRenewOrderConfirmationFragment()
    }

    private lateinit var viewModel: OrderConfirmationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        prefs = SharedPrefs(activity as UpgradeActivity)
        return inflater.inflate(R.layout.auto_renew_order_confirmation_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(OrderConfirmationViewModel::class.java)
        viewModel.emptyCurrentCart((activity as UpgradeActivity).application);

        //clear CartRelatedInfo
        prefs.storeOrderSuccessFlag(true)
        prefs.storeCartOrderInfo(null)
        prefs.storeApplyedCouponDetails(null)

        try {
            if(arguments!=null) {
                if (arguments!!.containsKey("payment_type") && arguments!!.getString("payment_type").equals("External_Link")) {
                    external_link_payment_status.visibility = View.VISIBLE
                } else {
                    external_link_payment_status.visibility = View.GONE
                }
            }
            order_details_feature_count.setText("Your have ordered " + prefs.getFeaturesCountInLastOrder() + " features for ₹" + prefs.getLatestPurchaseOrderTotalPrice() + "/month.")
            paymentBanner.setText("Order #"+prefs.getLatestPurchaseOrderId())
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat("EEE, MMM d, yyyy 'at' hh:mm aaa")
            order_id_details.setText("Order placed on " + formatter.format(date) +
                    "\nOrder ID #" + prefs.getLatestPurchaseOrderId() +
                    "\nTransaction ID #" + prefs.getTransactionIdFromCart())
        } catch (e: Exception){
            Log.e("Error", e.message)
        }

        back_button.setOnClickListener {
            (activity as UpgradeActivity).goToHomeFragment()
        }

        check_activation_status.setOnClickListener {
            WebEngageController.trackEvent("ADDONS_MARKETPLACE Check_Activation_Status Clicked", "Check_Activation_Status", "")
            (activity as UpgradeActivity).goBackToMyAddonsScreen()
        }

        order_needs_help.setOnClickListener {
            Toasty.info(requireContext(),"In case of any concerns, you can write to ria@nowfloats.com. Boost Care Team is available during business hours.").show()
        }

        WebEngageController.trackEvent("ADDONS_MARKETPLACE Order_Confirmation Loaded", "Auto_Renew_Order_Confirmation", "")

    }

}