package com.boost.upgrades.ui.popup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import com.boost.upgrades.R
import com.boost.upgrades.UpgradeActivity
import com.boost.upgrades.ui.payment.PaymentViewModel
import com.razorpay.Razorpay
import com.razorpay.ValidateVpaCallback
import kotlinx.android.synthetic.main.add_upi_popup.*
import org.json.JSONObject

class UPIPopUpFragement : DialogFragment() {

    lateinit var root: View
    private lateinit var viewModel: PaymentViewModel

    lateinit var razorpay: Razorpay

    var validatingStatus = false

    override fun onStart() {
        super.onStart()
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.MATCH_PARENT
        dialog!!.window!!.setLayout(width, height)
        dialog!!.window!!.setBackgroundDrawableResource(R.color.fullscreen_color)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.add_upi_popup, container, false)

        razorpay = (activity as UpgradeActivity).getRazorpayObject()

        return root

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(requireActivity()).get(PaymentViewModel::class.java)

        upi_popup_outer_layout.setOnClickListener {
            dialog!!.dismiss()
        }
        upi_popup_container_layout.setOnClickListener {}

        upi_popup_submit.setOnClickListener {
            if(!validatingStatus) {
                validatingStatus = true
                upi_popup_submit.setText("Validating...")
                validateVPA()
            }
        }
    }

    fun validateVPA(){
        razorpay.isValidVpa(upi_popup_value.text.toString(),object: ValidateVpaCallback{
            override fun onFailure() {
                validatingStatus = false
                upi_popup_submit.setText("VERIFY AND PAY")
                invalid_UPI.visibility = View.GONE
                Toast.makeText(requireContext(),"Cannot Validate!",Toast.LENGTH_LONG).show()
            }

            override fun onResponse(p0: Boolean) {
                validatingStatus = false
                upi_popup_submit.setText("VERIFY AND PAY")
                if(p0){
                    upiPaymentRazorpay()
                    invalid_UPI.visibility = View.GONE
                }else{
                    Toast.makeText(requireContext(),"Invalid UPI!",Toast.LENGTH_LONG).show()
                    invalid_UPI.visibility = View.VISIBLE
                }
            }
        })
    }

    fun upiPaymentRazorpay(){
        val data = JSONObject()
        data.put("method", "upi")
        data.put("vpa", upi_popup_value.text.toString())
        viewModel.UpdateUPIPaymentData(data)
        dialog!!.dismiss()
        clearData()
    }

    fun clearData(){
        upi_popup_value.text.clear()
    }
}