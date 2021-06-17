package com.appservice.staffs.doctors

import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.appservice.R
import com.appservice.base.AppBaseFragment
import com.appservice.constant.IntentConstant
import com.appservice.databinding.FragmentAdditionalDoctorInfoBinding
import com.appservice.staffs.doctors.bottomsheet.AppointmentTypeBottomSheet
import com.appservice.staffs.model.StaffDetailsResult
import com.appservice.staffs.ui.Constants
import com.appservice.staffs.ui.viewmodel.StaffViewModel
import com.framework.utils.ValidationUtils
import kotlin.math.exp

class AdditionalDoctorsInfoFragment :
  AppBaseFragment<FragmentAdditionalDoctorInfoBinding, StaffViewModel>() {
  private var isEdit: Boolean? = false
  private var staffDetailsResult: StaffDetailsResult? = null

  override fun getLayout(): Int {
    return R.layout.fragment_additional_doctor_info
  }

  override fun getViewModelClass(): Class<StaffViewModel> {
    return StaffViewModel::class.java
  }

  companion object {
    fun newInstance(): AdditionalDoctorsInfoFragment {
      return AdditionalDoctorsInfoFragment()
    }
  }

  override fun onCreateView() {
    super.onCreateView()
    setOnClickListener(binding?.confirmBtn,binding?.ctfConsultationType)
    this.staffDetailsResult = arguments?.getSerializable(IntentConstant.STAFF_DATA.name) as? StaffDetailsResult
    isEdit = (staffDetailsResult != null)
    if (isEdit==true)
    setView(staffDetailsResult)
    else staffDetailsResult = StaffDetailsResult()
  }

  private fun setView(staffDetailsResult: StaffDetailsResult?) {
    binding?.ctfEducation?.setText(staffDetailsResult?.education?:"")
    binding?.ctfExperiencer?.setText(staffDetailsResult?.experience?:"")
    binding?.ctfMembership?.setText(staffDetailsResult?.memberships?:"")
    binding?.ctfMobileNumber?.setText(staffDetailsResult?.contactNumber?:"")
    binding?.ctfRegistration?.setText(staffDetailsResult?.registration?:"")
  }

  override fun onClick(v: View) {
    super.onClick(v)
    when(v){
      binding?.confirmBtn->{
        updateReqeust()
      }  binding?.ctfConsultationType->{
        openConsultationTypeBottomSheet()
      }
    }
  }

  private fun openConsultationTypeBottomSheet() {
    val appointmentTypeBottomSheet = AppointmentTypeBottomSheet()
    appointmentTypeBottomSheet.onClicked = {staffDetailsResult?.appointmentType =it }
    appointmentTypeBottomSheet.show(parentFragmentManager,AppointmentTypeBottomSheet::javaClass.name)
  }

  private fun updateReqeust() {
    val education = binding?.ctfEducation?.text.toString()
    val experience = binding?.ctfExperiencer?.text.toString()
    val membership = binding?.ctfMembership?.text.toString()
    val mobileNumber = binding?.ctfMobileNumber?.text.toString()
    val registration = binding?.ctfRegistration?.text.toString()
    when {mobileNumber.isNotEmpty() -> {if (ValidationUtils.isMobileNumberValid(mobileNumber)) staffDetailsResult?.contactNumber = mobileNumber else {showShortToast(getString(R.string.please_enter_valid_mobile_number)); return } } }
    staffDetailsResult?.education = education
    staffDetailsResult?.experience = experience
    staffDetailsResult?.memberships = membership
    staffDetailsResult?.registration = registration
    val intent = Intent()
    intent.putExtra(IntentConstant.STAFF_DATA.name, staffDetailsResult)
    baseActivity.setResult(AppCompatActivity.RESULT_OK, intent)
    baseActivity.finish()
  }
}