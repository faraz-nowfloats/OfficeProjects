package com.appservice.staffs.ui.details

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.util.Base64
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.appservice.R
import com.appservice.base.AppBaseFragment
import com.appservice.constant.FragmentType
import com.appservice.constant.IntentConstant
import com.appservice.databinding.FragmentStaffDetailsBinding
import com.appservice.staffs.model.*
import com.appservice.staffs.ui.Constants
import com.appservice.staffs.ui.UserSession
import com.appservice.staffs.ui.startStaffFragmentActivity
import com.appservice.staffs.ui.viewmodel.StaffViewModel
import com.appservice.staffs.widgets.ExperienceBottomSheet
import com.appservice.ui.catalog.widgets.ClickType
import com.appservice.ui.catalog.widgets.ImagePickerBottomSheet
import com.framework.glide.util.glideLoad
import com.framework.imagepicker.ImagePicker
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_staff_details.*
import kotlinx.android.synthetic.main.item_preview_image.*
import kotlinx.android.synthetic.main.item_preview_image.view.*
import java.io.ByteArrayOutputStream

class StaffDetailsFragment : AppBaseFragment<FragmentStaffDetailsBinding, StaffViewModel>() {
    private var resultCode: Int? = 0
    private var isAvailable: Boolean? = false
    private lateinit var yearOfExperience: String
    private lateinit var staffDescription: String
    private var staffAge: Int = 0
    private lateinit var staffName: String
    private lateinit var specializationList: ArrayList<SpecialisationsItem>
    private lateinit var serviceListId: java.util.ArrayList<String>
    private lateinit var specialization: String
    private lateinit var staffImage: StaffImage
    private lateinit var genderArray: Array<String>
    private var imageUri: Uri? = null
    private var isEdit: Boolean? = null
    private var staffProfile: StaffCreateProfileRequest? = StaffCreateProfileRequest()
    private var servicesList: ArrayList<DataItemService>? = null
    private var staffDetails: StaffDetailsResult? = null

    companion object {
        fun newInstance(): StaffDetailsFragment {
            return StaffDetailsFragment()
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_staff_details
    }

    override fun getViewModelClass(): Class<StaffViewModel> {
        return StaffViewModel::class.java
    }

    override fun onCreateView() {
        setOnClickListener(binding?.flAddStaffImg, binding?.rlStaffTiming, binding?.rlServiceProvided,
                binding?.rlScheduledBreaks, binding!!.btnSave, binding?.edtExperience)
        initViews()
        getBundleData()

    }

    private fun getBundleData() {
        staffDetails = arguments?.getSerializable(IntentConstant.STAFF_DATA.name) as? StaffDetailsResult
        isEdit = (staffDetails != null && staffDetails?.id.isNullOrEmpty().not())
        when {
            isEdit!! -> updatePreviousData()
        }
    }

    private fun updatePreviousData() {
        val specialisations = staffDetails?.specialisations
        setImage(listOf(staffDetails?.tileImageUrl.toString()))
        binding?.etvName?.setText(staffDetails?.name.toString())
        binding?.etvStaffDescription?.setText(staffDetails?.description.toString())
        binding?.spinnerGender?.setSelection(genderArray.toList().indexOf(staffDetails?.gender))
        binding?.cetAge?.setText(staffDetails?.age.toString())
        if (specialisations?.isNullOrEmpty() == false)
            binding?.etvSpecialization?.setText(specialisations[0]?.value)
        binding?.edtExperience?.setText(staffDetails?.experience.toString())
        binding?.btnSave?.text = "UPDATE"
        binding?.toggleIsAvailable?.isOn = staffDetails?.isAvailable!!
        setServicesList()


    }

    private fun openExperienceDetail() {
        val experienceSheet = ExperienceBottomSheet()
        experienceSheet.onClicked = { binding?.edtExperience?.setText("$it") }
        experienceSheet.show(this@StaffDetailsFragment.parentFragmentManager, ExperienceBottomSheet::class.java.name)
    }

    private fun initViews() {
        this.genderArray = arrayOf("Male", "Female", "Please select")
        binding!!.spinnerGender.setHintAdapter(requireContext(), list = genderArray)
    }

    override fun onClick(v: View) {
        super.onClick(v)
        when (v) {
            binding?.flAddStaffImg -> {
                openImagePicker()
            }
            binding?.rlStaffTiming -> {
                startStaffFragmentActivity(requireActivity(), FragmentType.STAFF_TIMING_FRAGMENT, clearTop = false, isResult = true, requestCode = Constants.REQUEST_CODE_STAFF_TIMING)
            }
            binding?.rlServiceProvided -> {

                startStaffFragmentActivity(requireActivity(), FragmentType.STAFF_SELECT_SERVICES_FRAGMENT, clearTop = false, isResult = true, requestCode = Constants.REQUEST_CODE_SERVICES_PROVIDED)
            }
            binding?.rlScheduledBreaks -> {
                startStaffFragmentActivity(requireActivity(), FragmentType.STAFF_SCHEDULED_BREAK_FRAGMENT, clearTop = false, isResult = true, requestCode = Constants.REQUEST_CODE_SCHEDULED_BREAK)
            }
            binding?.edtExperience -> {
                openExperienceDetail()
            }
            binding?.btnSave -> {
                when {
                    (isEdit == false || isEdit == null) && isValid() -> {
                        createStaffProfile()
                    }
                }
                when {
                    isEdit == true && isValid() -> {
                        updateStaffProfile()
                    }
                }
            }
        }
    }

    private fun updateStaffImage() {
        viewModel?.updateStaffImage(StaffUpdateImageRequest(staffDetails?.id, staffImage))?.observe(viewLifecycleOwner, {
            when (it.status) {
                200 -> {

                }
                else -> {
                }
            }

        })
    }

    private fun updateStaffProfile() {
        val staffGender = binding?.spinnerGender?.selectedItem.toString()
        viewModel?.updateStaffProfile(
                StaffProfileUpdateRequest(isAvailable, getServiceIds(), staffGender, UserSession.fpId, name = staffName, staffDescription,
                        experience = yearOfExperience.toInt(), staffDetails?.id, staffAge, specializationList
                ))?.observe(viewLifecycleOwner, { t ->
            when (t.status) {
                200 -> {
                    showShortToast("profile updated")
                    baseActivity.setResult(AppCompatActivity.RESULT_OK)
                    baseActivity.finish()

                }
                else -> {
                    showShortToast("something went wrong")
                }
            }


        })

    }

    private fun getServiceIds(): ArrayList<String> {
        val serviceIds = ArrayList<String>()
        servicesList?.filter { serviceIds.add(it.id!!) }
        return serviceIds
    }


    private fun isValid(): Boolean {
        this.specialization = binding?.etvSpecialization?.text.toString()
        this.serviceListId = ArrayList()
        this.specializationList = ArrayList()
        this.staffName = binding?.etvName?.text.toString()
        this.staffAge = binding?.cetAge?.text.toString().toIntOrNull() ?: 0
        this.staffDescription = binding?.etvStaffDescription?.text.toString()
        val staffGender = binding?.spinnerGender?.isHintSelected()
        this.yearOfExperience = binding?.edtExperience?.text.toString()
        this.isAvailable = binding?.toggleIsAvailable?.isOn

        if (staffName.isBlank()) {
            showLongToast("Enter Staff Name")
            return false
        } else if (staffGender == null || staffGender) {
            showLongToast("Select Staff Gender")
            return false
        } else if (specialization.isEmpty()) {
            showLongToast("Please add specialization")
            return false
        } else if (yearOfExperience.isBlank()) {
            showLongToast("select year of experience")
            return false
        } else if (staffAge == 0) {
            showLongToast("please enter your age")
            return false
        } else if (imageUri == null) {
            showLongToast("please choose image")
            return false
        }
        specializationList.add(SpecialisationsItem(specialization, "key"))
        val imageExtension: String = imageUri!!.toString().substring(imageUri.toString().lastIndexOf("."))
        if (isEdit == null || isEdit == false) {
            val imageToByteArray: ByteArray = imageToByteArray()
            servicesList?.forEach { serviceListId.add(it.id!!) }
            staffProfile?.age = staffAge
            staffProfile?.isAvailable = isAvailable
            staffProfile?.description = staffDescription
            staffProfile?.gender = binding?.spinnerGender?.selectedItem.toString()
            staffProfile?.experience = yearOfExperience.toIntOrNull() ?: 0
            staffProfile?.floatingPointTag = UserSession.fpId
            staffProfile?.name = staffName
            staffProfile?.serviceIds = serviceListId
            this.staffImage = StaffImage(image = "data:image/png;base64,${Base64.encodeToString(imageToByteArray, Base64.DEFAULT)}",
                    fileName = "$staffName$imageExtension", imageFileType = imageExtension.removePrefix("."))
            staffProfile?.image = staffImage
            staffProfile?.specialisations = specializationList
        }
        return true
    }
    private fun createStaffProfile() {
        viewModel?.createStaffProfile(staffProfile)?.observe(viewLifecycleOwner, { t ->
            when (t.status) {
                200 -> {
                    showShortToast("profile created")
                    baseActivity.setResult(AppCompatActivity.RESULT_OK)
                    baseActivity.finish()
                }
                else -> {
                    showShortToast("something went wrong")
                }
            }
        })
    }
    private fun imageToByteArray(): ByteArray {
        val bm: Bitmap = BitmapFactory.decodeFile(imageUri!!.toString())
        val byteArrayOutStream = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutStream) // bm is the bitmap object
        return byteArrayOutStream.toByteArray()
    }
    private fun openImagePicker() {
        val filterSheet = ImagePickerBottomSheet()
        filterSheet.isHidePdf(true)
        filterSheet.onClicked = { openImagePicker(it) }
        filterSheet.show(this@StaffDetailsFragment.parentFragmentManager, ImagePickerBottomSheet::class.java.name)
    }
    private fun openImagePicker(it: ClickType) {
        val type = if (it == ClickType.CAMERA) ImagePicker.Mode.CAMERA else ImagePicker.Mode.GALLERY
        ImagePicker.Builder(baseActivity)
                .mode(type)
                .compressLevel(ImagePicker.ComperesLevel.SOFT).directory(ImagePicker.Directory.DEFAULT)
                .extension(ImagePicker.Extension.PNG).allowMultipleImages(false)
                .enableDebuggingMode(true).build()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            requestCode == ImagePicker.IMAGE_PICKER_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK -> {
                val mPaths = data?.getSerializableExtra(ImagePicker.EXTRA_IMAGE_PATH) as List<String>
                setImage(mPaths)
            }
            requestCode == Constants.REQUEST_CODE_SERVICES_PROVIDED && resultCode == AppCompatActivity.RESULT_OK -> {
                this.servicesList?.clear()
                this.resultCode = resultCode
                this.servicesList = data!!.extras!![Constants.SERVICES_LIST] as ArrayList<DataItemService>
                val serviceName = ArrayList<String>()
                servicesList?.forEach { dataItem -> serviceName.add(dataItem.name!!) }
                binding!!.ctvServices.text = serviceName.joinToString(" ,", limit = 5, truncated = "5 more")
            }
            requestCode == Constants.REQUEST_CODE_STAFF_TIMING && resultCode == AppCompatActivity.RESULT_OK -> {
            }
        }

    }
    private fun setServicesList() {
        val serviceName = ArrayList<String>()
        if (isEdit == true && 0== resultCode) {
            viewModel!!.getServiceListing(ServiceListRequest(floatingPointTag = UserSession.fpId)
            ).observe(viewLifecycleOwner, {
                when (it.status) {
                    200 -> {
                        val data = (it as ServiceListResponse).result!!.data!!
                        val servicesProvided = data.filter { item -> staffDetails?.serviceIds!!.contains(item?.id) } as ArrayList<DataItemService>
                        servicesProvided.forEach { itemService -> serviceName.add(itemService.name!!) }
                        binding!!.ctvServices.text = serviceName.joinToString(" ,", limit = 5, truncated = "5 more")
                    }
                    else -> {
                    }
                }
            })
        }
    }
    private fun setImage(mPaths: List<String>) {
        this.imageUri = Uri.parse(mPaths[0])
        let { activity?.glideLoad(binding?.civStaffImg!!, imageUri.toString(), R.drawable.ic_staff_img_blue) }
        binding?.ctvImgChange?.text = getString(R.string.change_picture)
        binding?.ctvImgChange?.setTextColor(getColor(R.color.black_4a4a4a))
        binding?.ctvImgChange?.setBackgroundColor(Color.WHITE)
        binding?.flAddStaffImg?.setPadding(2, 2, 2, 2)
        binding?.flAddStaffImg?.backgroundTintList = ColorStateList.valueOf(getColor(R.color.gray_light_4))

    }

}

class HintAdapter<T>(context: Context, resource: Int, objects: Array<T>) :
        ArrayAdapter<T>(context, resource, objects) {
    override fun getCount(): Int {
        val count = super.getCount()
        // The last item will be the hint.
        return if (count > 0) count - 1 else count
    }
    override fun isEnabled(position: Int): Boolean {
        return when (position) {
            count -> false
            else -> true
        }
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        val textView = view as TextView
        if (position == count)
            textView.setTextColor(Color.GRAY)
        else textView.setTextColor(Color.BLACK)
        return view
    }
}

fun Spinner.setHintAdapter(context: Context, list: Array<String>) {
    val hintAdapter =
            HintAdapter(
                    context,
                    android.R.layout.simple_spinner_dropdown_item,
                    list
            )
    adapter = hintAdapter
    setSelection(count)


}

fun Spinner.isHintSelected(): Boolean {
    return selectedItemPosition == count
}