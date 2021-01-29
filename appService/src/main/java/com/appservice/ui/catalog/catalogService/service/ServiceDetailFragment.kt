package com.appservice.ui.catalog.catalogService.service

import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.appservice.R
import com.appservice.base.AppBaseFragment
import com.appservice.constant.FragmentType
import com.appservice.constant.IntentConstant
import com.appservice.databinding.FragmentServiceDetailBinding
import com.appservice.extension.afterTextChanged
import com.appservice.model.FileModel
import com.appservice.model.accountDetails.AccountDetailsResponse
import com.appservice.model.accountDetails.BankAccountDetails
import com.appservice.model.auth_3
import com.appservice.model.serviceProduct.BuyOnlineLink
import com.appservice.model.serviceProduct.Product
import com.appservice.model.serviceProduct.addProductImage.response.DataImage
import com.appservice.model.serviceProduct.addProductImage.response.ProductImageResponse
import com.appservice.model.serviceProduct.delete.DeleteProductRequest
import com.appservice.model.serviceProduct.gstProduct.response.DataG
import com.appservice.model.serviceProduct.gstProduct.response.ProductGstResponse
import com.appservice.model.servicev1.*
import com.appservice.rest.TaskCode
import com.appservice.ui.bankaccount.startFragmentAccountActivity
import com.appservice.ui.catalog.startFragmentActivity
import com.appservice.ui.catalog.widgets.*
import com.appservice.utils.WebEngageController
import com.appservice.utils.getBitmap
import com.appservice.viewmodel.ServiceViewModelV1
import com.framework.base.BaseResponse
import com.framework.exceptions.NoNetworkException
import com.framework.extensions.gone
import com.framework.extensions.observeOnce
import com.framework.extensions.visible
import com.framework.glide.util.glideLoad
import com.framework.imagepicker.ImagePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

class ServiceDetailFragment : AppBaseFragment<FragmentServiceDetailBinding, ServiceViewModelV1>() {

    private var menuDelete: MenuItem? = null
    private var serviceImage: File? = null
    private var product: ServiceModelV1? = null
    private var isNonPhysicalExperience: Boolean? = null
    private var currencyType: String? = null
    private var fpId: String? = null
    private var fpTag: String? = null
    private var clientId: String? = null
    private var externalSourceId: String? = null
    private var applicationId: String? = null
    private var userProfileId: String? = null

    private var isEdit: Boolean? = null
    private var bankAccountDetail: BankAccountDetails? = null

    private var secondaryImage: ArrayList<FileModel> = ArrayList()

    private var productIdAdd: String? = null
    private var errorType: String? = null

    companion object {
        fun newInstance(): ServiceDetailFragment {
            return ServiceDetailFragment()
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_service_detail
    }

    override fun getViewModelClass(): Class<ServiceViewModelV1> {
        return ServiceViewModelV1::class.java
    }

    override fun onCreateView() {
        super.onCreateView()
        WebEngageController.trackEvent("Service product catalogue load", "SERVICE CATALOGUE ADD/UPDATE", "")
        getBundleData()
        binding?.vwPaymentConfig?.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        setOnClickListener(binding?.selectDeliveryConfig, binding?.vwPaymentConfig,
                binding?.vwSavePublish, binding?.imageAddBtn, binding?.clearImage, binding?.btnOtherInfo, binding?.bankAccountView)
//        binding?.toggleService?.isOn = product?.isPriceToggleOn()!!
        binding?.payServiceView?.visibility = View.GONE
        binding?.toggleService?.setOnToggledListener { _, _ ->
            initServiceToggle()
        }
        initServiceToggle()
        listenerEditText()
    }

    private fun initServiceToggle() {
        binding?.payServiceView?.visibility = if (binding?.toggleService?.isOn!!) View.VISIBLE else View.GONE
        binding?.freeServiceView?.visibility = if (binding?.toggleService?.isOn!!) View.GONE else View.VISIBLE
    }

    private fun listenerEditText() {
        binding?.amountEdt?.afterTextChanged { calculate(binding?.amountEdt?.text.toString(), binding?.discountEdt?.text.toString()) }
        binding?.discountEdt?.afterTextChanged { calculate(binding?.amountEdt?.text.toString(), binding?.discountEdt?.text.toString()) }
    }

    private fun calculate(amount: String, dist: String) {
        val amountD = amount.toFloatOrNull() ?: 0F
        val distD = dist.toFloatOrNull() ?: 0F
        if (distD > amountD) {
            showLongToast(resources.getString(R.string.discount_amount_cant_be_grater))
            binding?.discountEdt?.setText("")
            return
        }
//    val finalAmount = String.format("%.1f", (amountD - ((amountD * distD) / 100))).toFloatOrNull() ?: 0F
        val finalAmount = String.format("%.1f", (amountD - distD)).toFloatOrNull() ?: 0F
        binding?.finalPriceTxt?.setText("$currencyType $finalAmount")
    }

    private fun getPaymentGatewayKyc() {
        showProgress()
        viewModel?.userAccountDetails(fpId, clientId)?.observeOnce(viewLifecycleOwner, Observer {
            if ((it.error is NoNetworkException).not()) {
                val response = it as? AccountDetailsResponse
                if ((it.status == 200 || it.status == 201 || it.status == 202) && response?.result?.bankAccountDetails != null) {
                    bankAccountDetail = response.result?.bankAccountDetails
                }
            }
            if (isEdit == true) updateUiPreviousDat()
            else {
                setBankAccountData()
                hideProgress()
            }
        })
    }

    private fun setBankAccountData() {
        if (bankAccountDetail != null) {
            product?.paymentType = Product.PaymentType.ASSURED_PURCHASE.value
            binding?.txtPaymentType?.text = resources.getString(R.string.boost_payment_gateway)
            binding?.bankAccountView?.visible()
            binding?.externalUrlView?.gone()
            binding?.txtPaymentType?.text = resources.getString(R.string.boost_payment_gateway)
            binding?.bankAccountName?.visible()
            binding?.bankAccountName?.text = "${bankAccountDetail?.accountName} - ${bankAccountDetail?.accountNumber}"
            binding?.titleBankAdded?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_ok_green, 0, 0, 0)
            binding?.titleBankAdded?.text = "${resources.getString(R.string.bank_account_added)} (${bankAccountDetail?.getVerifyText()})"
        }
    }

    private fun updateUiPreviousDat() {
        binding?.tvServiceName?.setText(product?.Name)
        binding?.tvDesc?.setText(product?.Description)
        binding?.edtServiceCategory?.setText(product?.category)
        binding?.edtServiceTime?.setText(product?.ShipmentDuration)
        if (product?.paymentType == Product.PaymentType.ASSURED_PURCHASE.value && bankAccountDetail != null || !bankAccountDetail?.iFSC.isNullOrEmpty()
                || !bankAccountDetail?.accountNumber.isNullOrEmpty()) {
            binding?.txtPaymentType?.text = resources.getString(R.string.boost_payment_gateway)
            binding?.bankAccountName?.visible()
            binding?.bankAccountName?.text = "${bankAccountDetail?.accountName} - ${bankAccountDetail?.accountNumber}"
            binding?.titleBankAdded?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_ok_green, 0, 0, 0)
            binding?.titleBankAdded?.text = "${resources.getString(R.string.bank_account_added)} (${bankAccountDetail?.getVerifyText()})"
        }
        if (product?.paymentType == Product.PaymentType.UNIQUE_PAYMENT_URL.value) {
            binding?.txtPaymentType?.text = resources.getString(R.string.external_url)
            binding?.edtUrl?.setText(product?.BuyOnlineLink?.url ?: "")
            binding?.edtNameDesc?.setText(product?.BuyOnlineLink?.description ?: "")
            binding?.bankAccountView?.gone()
            binding?.externalUrlView?.visible()
        } else {
            binding?.txtPaymentType?.text = resources.getString(R.string.boost_payment_gateway)
            binding?.bankAccountName?.gone()
            binding?.titleBankAdded?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_info_circular_orange, 0, 0, 0)
            binding?.titleBankAdded?.text = resources.getString(R.string.bank_account_not_added)
        }
        if (product?.Price ?: 0.0 <= 0.0) {
            binding?.toggleService?.isOn = false
            binding?.payServiceView?.gone()
            binding?.freeServiceView?.visible()
        }
        binding?.amountEdt?.setText("${product?.Price ?: 0}")
        binding?.discountEdt?.setText("${product?.DiscountAmount ?: 0.0}")
        if (product?.image?.ImageId.isNullOrEmpty().not()) {
            binding?.imageAddBtn?.gone()
            binding?.clearImage?.visible()
            binding?.serviceImageView?.visible()
            binding?.serviceImageView?.let { activity?.glideLoad(it, product?.image?.ActualImage, R.drawable.placeholder_image) }
        }
    }

    private fun showError(string: String) {
        hideProgress()
        showLongToast(string)
    }

    private fun getBundleData() {
        // TODO need to change the typecasting to serviceModelV1
        initProductFromBundle(arguments);
        isNonPhysicalExperience = arguments?.getBoolean(IntentConstant.NON_PHYSICAL_EXP_CODE.name)
        currencyType = arguments?.getString(IntentConstant.CURRENCY_TYPE.name)
        fpId = arguments?.getString(IntentConstant.FP_ID.name)
        fpTag = arguments?.getString(IntentConstant.FP_TAG.name)
        clientId = arguments?.getString(IntentConstant.CLIENT_ID.name)
        externalSourceId = arguments?.getString(IntentConstant.EXTERNAL_SOURCE_ID.name)
        applicationId = arguments?.getString(IntentConstant.APPLICATION_ID.name)
        userProfileId = arguments?.getString(IntentConstant.USER_PROFILE_ID.name)
        if (isEdit == true) menuDelete?.isVisible = true
    }

    private fun initProductFromBundle(data: Bundle?) {
        val p = data?.getSerializable(IntentConstant.PRODUCT_DATA.name) as? ServiceModelV1
        if(p != null && p?.productId.isNullOrEmpty().not()){
            isEdit = true;
            getServiceDetailObject(p?.productId);
        }else{
            isEdit = false;
            this.product = ServiceModelV1();
        }
    }

    private fun getServiceDetailObject(serviceId: String?) {
        hitApi(viewModel?.getServiceDetails(serviceId), R.string.error_getting_service_details);
    }

    override fun onClick(v: View) {
        super.onClick(v)
        when (v) {
            binding?.bankAccountView -> {
                if (binding?.bankAccountView?.visibility == View.VISIBLE) goAddBankView()
            }
            binding?.imageAddBtn -> openImagePicker()
            binding?.clearImage -> clearImage()
            binding?.selectDeliveryConfig -> showServiceDeliveryConfigBottomSheet()
//      binding?.vwChangeDeliverLocation -> showServiceDeliveryLocationBottomSheet()
            binding?.vwPaymentConfig -> showPaymentConfigBottomSheet()
            binding?.btnOtherInfo -> {
                WebEngageController.trackEvent("Service click other information", "SERVICE CATALOGUE ADD/UPDATE", "")
                val bundle = Bundle()
                bundle.putSerializable(IntentConstant.PRODUCT_DATA.name, product)
                bundle.putSerializable(IntentConstant.NEW_FILE_PRODUCT_IMAGE.name, secondaryImage)
                startFragmentActivity(FragmentType.SERVICE_INFORMATION, bundle, isResult = true)
            }
            binding?.vwSavePublish -> if (isValid()) createUpdateApi()
        }
    }

    private fun createServiceApi() {
        WebEngageController.trackEvent("Add service product catalogue", "SERVICE CATALOGUE ADD/UPDATE", "")
        hitApi(viewModel?.createService(product), R.string.service_adding_error);
    }


    override fun onSuccess(it: BaseResponse) {
        when (it.taskcode) {
            TaskCode.POST_CREATE_SERVICE.ordinal -> onServiceCreated(it)
            TaskCode.POST_UPDATE_SERVICE.ordinal -> onServiceUpdated(it)
            TaskCode.ADD_SERVICE_PRIMARY_IMAGE_V1.ordinal -> onPrimaryImageUploaded(it)
            TaskCode.GET_SERVICE_DETAILS.ordinal -> onServiceDetailResponseReceived(it)
            TaskCode.DELETE_SERVICE.ordinal -> onServiceDelete(it)
        }
    }

    fun onServiceDelete(it: BaseResponse){
        showLongToast(getString(R.string.service_removed_successfully))
        goBack();
    }

    fun onServiceDetailResponseReceived(it: BaseResponse) {
        if (it != null) {
            this.product = (it as? ServiceDetailResponse)?.Result;
            updateUiPreviousDat()
        }
    }

    fun onPrimaryImageUploaded(it: BaseResponse) {
        uploadSecondaryImages()
    }

    // function will be called once service is created
    fun onServiceCreated(it: BaseResponse) {
        val res = it as? ServiceV1BaseResponse;
        val productId = res?.Result
        if (productId.isNullOrEmpty().not()) {
            product?.productId = res?.Result;
            productIdAdd = productId
            uploadPrimaryImage();
        } else {
            showError(resources.getString(R.string.service_adding_error))
        }
    }

    fun onServiceUpdated(it: BaseResponse) {
        updateGstService(product?.productId)
    }

    private fun createUpdateApi() {
        showProgress()
        if (product?.productId == null) {
            createServiceApi()
        } else {
            // update service
            WebEngageController.trackEvent("Update service catalogue", "SERVICE CATALOGUE ADD/UPDATE", "")
            // hit api to update the service
            hitApi(viewModel?.updateService(product), R.string.service_updating_error);
        }
    }

    private fun updateGstService(productId: String?) {
        hideProgress()
        uploadPrimaryImage()
    }

    private fun addGstService() {
        hideProgress()
        uploadPrimaryImage()
    }

    private fun uploadPrimaryImage() {
        if (serviceImage != null) {
            val request = UploadImageRequest.getInstance(0, product?.productId!!, serviceImage!!)
            hitApi(viewModel?.addPrimaryImage(request), R.string.error_service_image);
        } else {
            uploadSecondaryImages();
        }
    }

    private fun uploadSecondaryImages() {
        val images = secondaryImage.filter { it.path.isNullOrEmpty().not() }
        if (images.isNullOrEmpty().not()) {
            var checkPosition = 0
            val secondaryImageList = ArrayList<String>()
            images.forEach { fileData ->
                val request = UploadImageRequest.getInstance(1, product?.productId!!, fileData.getFile()!!)
                viewModel?.addSecondaryImage(request)?.observeOnce(viewLifecycleOwner, Observer {
                    checkPosition += 1
                    if ((it.error is NoNetworkException).not()) {
                        if (it.status == 200 || it.status == 201 || it.status == 202) {
                            // TODO check for image uploaded or not
                        } else showError(resources.getString(R.string.secondary_service_image_upload_error))
                    } else showError(resources.getString(R.string.internet_connection_not_available))
                    if (checkPosition == images.size) {
                        onSecondaryImagesUploaded()
                    }
                })
            }
        } else onSecondaryImagesUploaded()
    }

    private fun onSecondaryImagesUploaded() {
        showLongToast(if (isEdit == true) resources.getString(R.string.services_updated_success) else resources.getString(R.string.services_saved))
        goBack()
    }


    private fun goBack() {
        hideProgress()
        val data = Intent()
        data.putExtra("LOAD", true)
        appBaseActivity?.setResult(Activity.RESULT_OK, data)
        appBaseActivity?.finish()
    }


    private fun isValid(): Boolean {
        val serviceName = binding?.tvServiceName?.text.toString()
        val shipmentDuration = binding?.edtServiceTime?.text
        val serviceCategory = binding?.edtServiceCategory?.text.toString()
        val serviceDesc = binding?.tvDesc?.text.toString()
        val amount = binding?.amountEdt?.text.toString().toDoubleOrNull() ?: 0.0
        val discount = binding?.discountEdt?.text.toString().toDoubleOrNull() ?: 0.0
        val toggle = binding?.toggleService?.isOn ?: false
        val externalUrlName = binding?.edtNameDesc?.text?.toString() ?: ""
        val externalUrl = binding?.edtUrl?.text?.toString() ?: ""

        if (serviceImage == null && product?.image?.ImageId.isNullOrEmpty()) {
            showLongToast(resources.getString(R.string.add_service_image))
            return false
        }

        if (shipmentDuration.isNullOrEmpty()) {
            showLongToast(resources.getString(R.string.enter_service_duration))
            return false
        } else if (serviceName.isEmpty()) {
            showLongToast(resources.getString(R.string.enter_service_name))
            return false
        } else if (serviceCategory.isBlank()) {
            showLongToast(resources.getString(R.string.enter_service_category))
            return false
        } else if (serviceDesc.isEmpty()) {
            showLongToast(resources.getString(R.string.enter_service_desc))
            return false
        } else if (toggle && amount <= 0.0) {
            showLongToast(resources.getString(R.string.enter_valid_price))
            return false
        } else if (toggle && (discount > amount)) {
            showLongToast(resources.getString(R.string.discount_amount_not_greater_than_price))
            return false
        } else if (toggle && (product?.paymentType.isNullOrEmpty() || (product?.paymentType == Product.PaymentType.ASSURED_PURCHASE.value && bankAccountDetail == null))) {
            showLongToast(resources.getString(R.string.please_add_bank_detail))
            return false
        } else if (toggle && (product?.paymentType == Product.PaymentType.UNIQUE_PAYMENT_URL.value && (externalUrlName.isNullOrEmpty() || externalUrl.isNullOrEmpty()))) {
            showLongToast(resources.getString(R.string.please_enter_valid_url_name))
            return false
        }
        product?.ClientId = clientId
        product?.FPTag = fpTag
        product?.CurrencyCode = currencyType
        product?.Name = serviceName
        product?.Duration = shipmentDuration.toString().toIntOrNull()
        product?.category = serviceCategory
        product?.Description = serviceDesc
        product?.Price = if (toggle) amount else 0.0
        product?.DiscountAmount = if (toggle) discount else 0.0
        if (toggle && (product?.paymentType == Product.PaymentType.UNIQUE_PAYMENT_URL.value)) {
            product?.BuyOnlineLink = BuyOnlineLink(externalUrl, externalUrlName)
        } else product?.BuyOnlineLink = BuyOnlineLink()

        if (isEdit == false) {
            product?.category = product?.category ?: ""
            product?.brandName = product?.brandName ?: ""
            product?.tags = product?.tags ?: ArrayList()
            product?.otherSpecification = product?.otherSpecification ?: ArrayList()
            product?.IsAvailable = true
            product?.prepaidOnlineAvailable = true
            product?.variants = false
            product?.isProductSelected = false
            product?.codAvailable = true
        }
        return true
    }

    private fun clearImage() {
        binding?.imageAddBtn?.visible()
        binding?.clearImage?.gone()
        binding?.serviceImageView?.gone()
        product?.image = null
        serviceImage = null
    }

    private fun openImagePicker() {
        val filterSheet = ImagePickerBottomSheet()
        filterSheet.isHidePdf(true)
        filterSheet.onClicked = { openImagePicker(it) }
        filterSheet.show(this@ServiceDetailFragment.parentFragmentManager, ImagePickerBottomSheet::class.java.name)
    }


    private fun openImagePicker(it: ClickType) {
        val type = if (it == ClickType.CAMERA) ImagePicker.Mode.CAMERA else ImagePicker.Mode.GALLERY
        ImagePicker.Builder(baseActivity)
                .mode(type)
                .compressLevel(ImagePicker.ComperesLevel.SOFT).directory(ImagePicker.Directory.DEFAULT)
                .extension(ImagePicker.Extension.PNG).allowMultipleImages(false)
                .scale(800, 800)
                .enableDebuggingMode(true).build()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ImagePicker.IMAGE_PICKER_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            val mPaths = data?.getSerializableExtra(ImagePicker.EXTRA_IMAGE_PATH) as List<String>
            if (mPaths.isNotEmpty()) {
                serviceImage = File(mPaths[0])
                binding?.imageAddBtn?.gone()
                binding?.clearImage?.visible()
                binding?.serviceImageView?.visible()
                serviceImage?.getBitmap()?.let { binding?.serviceImageView?.setImageBitmap(it) }
            }
        } else if (resultCode == AppCompatActivity.RESULT_OK && requestCode == 101) {
            product = getServiceModelObjectFromBundle(data?.extras);
            secondaryImage = (data?.getSerializableExtra(IntentConstant.NEW_FILE_PRODUCT_IMAGE.name) as? ArrayList<FileModel>)
                    ?: ArrayList()
        } else if (resultCode == AppCompatActivity.RESULT_OK && requestCode == 202) {
            bankAccountDetail = data?.getSerializableExtra(IntentConstant.USER_BANK_DETAIL.name) as? BankAccountDetails
            if (bankAccountDetail != null) {
                product?.paymentType = Product.PaymentType.ASSURED_PURCHASE.value
                binding?.bankAccountView?.visible()
                binding?.externalUrlView?.gone()
                binding?.bankAccountName?.visible()
                binding?.bankAccountName?.text = "${bankAccountDetail?.accountName} - ${bankAccountDetail?.accountNumber}"
                binding?.titleBankAdded?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_ok_green, 0, 0, 0)
                binding?.titleBankAdded?.text = "${resources.getString(R.string.bank_account_added)} (${bankAccountDetail?.getVerifyText()})"
            }
        }
    }

    fun getServiceModelObjectFromBundle(data: Bundle?): ServiceModelV1? {
        // TODO need to change the typecasting to serviceModelV1
        val p = data?.getSerializable(IntentConstant.PRODUCT_DATA.name) as? ServiceModelV1
        return p;
    }

    private fun showServiceDeliveryConfigBottomSheet() {
        val dialog = ServiceDeliveryConfigBottomSheet()
        dialog.onClicked = { product?.prepaidOnlineAvailable = true }
        if (product?.prepaidOnlineAvailable != null) dialog.isUpdate(product?.prepaidOnlineAvailable
                ?: true)
        dialog.show(parentFragmentManager, ServiceDeliveryConfigBottomSheet::class.java.name)
    }

    private fun showServiceDeliveryLocationBottomSheet() {
//        val dialog = ServiceDeliveryBottomSheet()
//        dialog.setList(pickUpDataAddress, product?.pickupAddressReferenceId)
//        dialog.onClicked = {
//            if (it != null && it.id.isNullOrEmpty().not()) {
//                product?.pickupAddressReferenceId = it.id
//            }
//        }
//        dialog.show(parentFragmentManager, ServiceDeliveryBottomSheet::class.java.name)
    }

    private fun showPaymentConfigBottomSheet() {
        val dialog = PaymentConfigBottomSheet()
        dialog.onClicked = {
            product?.paymentType = it
            binding?.bankAccountView?.visible()
            binding?.externalUrlView?.gone()
            when (it) {
                Product.PaymentType.ASSURED_PURCHASE.value -> {
                    binding?.txtPaymentType?.text = resources.getString(R.string.boost_payment_gateway)
                    binding?.bankAccountName?.visible()
                    binding?.bankAccountName?.text = "${bankAccountDetail?.accountName} - ${bankAccountDetail?.accountNumber}"
                    when {
                        bankAccountDetail?.accountNumber.isNullOrBlank() || bankAccountDetail?.accountName.isNullOrBlank() -> {
                            binding?.titleBankAdded?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_info_circular_orange, 0, 0, 0)
                            binding?.titleBankAdded?.text = resources.getString(R.string.bank_account_not_added)
                        }
                        else -> {
                            binding?.titleBankAdded?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_ok_green, 0, 0, 0)
                            binding?.titleBankAdded?.text = "${resources.getString(R.string.bank_account_added)} (${bankAccountDetail?.getVerifyText()})"
                        }
                    }


                }
                Product.PaymentType.UNIQUE_PAYMENT_URL.value -> {
                    binding?.txtPaymentType?.text = resources.getString(R.string.external_url)
                    binding?.bankAccountView?.gone()
                    binding?.externalUrlView?.visible()
                }
                else -> {
                    binding?.txtPaymentType?.text = resources.getString(R.string.boost_payment_gateway)
                    binding?.bankAccountName?.gone()
                    binding?.titleBankAdded?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_info_circular_orange, 0, 0, 0)
                    binding?.titleBankAdded?.text = resources.getString(R.string.bank_account_not_added)
                }
            }
        }
        dialog.onListenerChange = { goAddBankView() }
        if (((product?.paymentType == Product.PaymentType.ASSURED_PURCHASE.value && bankAccountDetail != null) ||
                        (product?.paymentType == Product.PaymentType.UNIQUE_PAYMENT_URL.value)).not()) {
            product?.paymentType = ""
        }
        dialog.setDataPaymentGateway(bankAccountDetail, product?.paymentType)
        dialog.show(parentFragmentManager, PaymentConfigBottomSheet::class.java.name)
    }

    private fun goAddBankView() {
        WebEngageController.trackEvent("Add/Update bank account", "SERVICE CATALOGUE ADD/UPDATE", "")
        val bundle = Bundle()
        bundle.putString(IntentConstant.CLIENT_ID.name, clientId)
        bundle.putString(IntentConstant.USER_PROFILE_ID.name, userProfileId)
        bundle.putString(IntentConstant.FP_ID.name, fpId)
        bundle.putBoolean(IntentConstant.IS_SERVICE_CREATION.name, true)
        val fragment = when {
            bankAccountDetail != null && bankAccountDetail?.accountNumber.isNullOrEmpty().not() && bankAccountDetail?.iFSC.isNullOrEmpty().not() -> FragmentType.BANK_ACCOUNT_DETAILS
            else -> FragmentType.ADD_BANK_ACCOUNT_START
        }
        startFragmentAccountActivity(fragment, bundle, isResult = true, requestCode = 202)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
//    inflater.inflate(R.menu.menu_help, menu)
        inflater.inflate(R.menu.ic_menu_delete_new, menu)
        menuDelete = menu.findItem(R.id.id_delete)
        menuDelete?.isVisible = isEdit ?: false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.id_delete -> {
                MaterialAlertDialogBuilder(baseActivity, R.style.MaterialAlertDialogTheme).setTitle(resources.getString(R.string.are_you_sure))
                        .setMessage(resources.getString(R.string.delete_record_not_undone))
                        .setNegativeButton(resources.getString(R.string.cancel)) { d, _ -> d.dismiss() }.setPositiveButton(resources.getString(R.string.delete)) { d, _ ->
                            d.dismiss()
                            showProgress()
                            WebEngageController.trackEvent("Delete Service product catalogue", "SERVICE CATALOGUE ADD/UPDATE", "")
                            val req = DeleteServiceRequest(this.fpTag, product?.productId)
                            hitApi(viewModel?.deleteService(req), R.string.removing_service_failed);
                        }.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun onNavPressed() {
        dialogLogout()
    }

    private fun dialogLogout() {
        MaterialAlertDialogBuilder(baseActivity, R.style.MaterialAlertDialogTheme)
                .setTitle("Information not saved!").setMessage("You have unsaved information. Do you still want to close?")
                .setNegativeButton("No") { d, _ -> d.dismiss() }.setPositiveButton("Yes") { d, _ ->
                    baseActivity.finish()
                    d.dismiss()
                }.show()
    }
}