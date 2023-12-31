package com.appservice.ui.updatesBusiness

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.lifecycleScope
import com.appservice.R
import com.appservice.base.AppBaseActivity
import com.appservice.databinding.UpdateCropImageActivityBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.framework.constants.UPDATE_PIC_FILE_NAME
import com.framework.imagepicker.BoostImageUtils.isImageGalleryValid
import com.framework.models.BaseViewModel
import com.framework.utils.saveAsImageToAppFolder
import com.framework.utils.setStatusBarColor
import com.framework.utils.showSnackBarNegative
import com.onboarding.nowfloats.bottomsheet.util.runOnUi
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import kotlin.math.absoluteValue

class UpdateCropImageActivity:AppBaseActivity<UpdateCropImageActivityBinding,BaseViewModel>() {

    private var currentRotation =0.0F
    private var path: String?=null

    companion object{
        val IK_IMAGE_PATH="IK_IMAGE_PATH"
        fun launchActivity(path:String,activity:Activity,launcher: ActivityResultLauncher<Intent>?){
            val intent = Intent(activity,UpdateCropImageActivity::class.java)
            intent.putExtra(IK_IMAGE_PATH,path)
            launcher?.launch(intent)
        }
    }
    override fun getLayout(): Int {
        return R.layout.update_crop_image_activity
    }

    override fun getViewModelClass(): Class<BaseViewModel> {
        return BaseViewModel::class.java
    }

    override fun onCreateView() {
        setOnClickListener(binding!!.layoutChangeImage,binding!!.layoutTick,binding!!.layoutRotate,binding!!.ivDelete,binding?.ivCropUpdateClose)

        path = intent.getStringExtra(IK_IMAGE_PATH)

        showImageInUi()

    }

    override fun onResume() {
        super.onResume()
        setStatusBarColor(R.color.black_3333)
    }
    fun showImageInUi(){
        lifecycleScope.launch {
            withContext(Dispatchers.Default){
                val bitmap = Glide.with(this@UpdateCropImageActivity).asBitmap().load(
                    path
                ).apply(RequestOptions.skipMemoryCacheOf(true))
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE)).submit().get()
                runOnUi {
                    if(bitmap.width <= 200 || bitmap.height <= 200 ){
                        binding!!.ivCrop.setImageBitmap(bitmap)
                        binding!!.ivCrop.setAspectRatio(bitmap.width+50,bitmap.height+50)
                        binding!!.ivCrop.setFixedAspectRatio(false)
                    }else {
                        binding!!.ivCrop.setImageBitmap(bitmap)
                        binding!!.ivCrop.setMinCropResultSize(200, 200)
                        binding!!.ivCrop.setCropRect(Rect(400, 400, 600, 600))
//                    binding!!.ivCrop.setAspectRatio(600,600)
//                    binding!!.ivCrop.setFixedAspectRatio(false)
                    }
                }
            }
        }
    }

    override fun onClick(v: View?) {
        super.onClick(v)
        when(v){
            binding!!.layoutChangeImage->{
                UpdateImagePickerBSheet.newInstance(object :UpdateImagePickerBSheet.Callbacks{
                    override fun onImagePicked(path: String) {
                        val isBusinessValidMessage = isImageGalleryValid(this@UpdateCropImageActivity, File(path))
                        if (path.isNullOrEmpty().not() && isBusinessValidMessage == "") {
                            this@UpdateCropImageActivity.path = path
                            showImageInUi()
                        }else{
                            showSnackBarNegative(
                                this@UpdateCropImageActivity,
                                if (path == null || path.isEmpty()) resources.getString(R.string.select_image_upload) else isBusinessValidMessage
                            )
                        }
                    }
                }).show(supportFragmentManager,UpdateImagePickerBSheet::class.java.name)
            }
            binding!!.layoutRotate->{
                val fromRotation = if (currentRotation.absoluteValue == 360f) 0f else currentRotation
                val rotateDegrees = 90f
                val toRotation = (fromRotation + rotateDegrees) % 450f
                binding!!.ivCrop.rotateImage(toRotation.toInt())
            }

            binding!!.layoutTick->{
                if(binding!!.ivCrop.croppedImage.width < 1 && binding!!.ivCrop.croppedImage.height < 1){
                    Toasty.error(this@UpdateCropImageActivity,"Invalid Image Cropping", Toasty.LENGTH_LONG).show()
                }else {
                    binding!!.ivCrop.croppedImage.saveAsImageToAppFolder(
                        getExternalFilesDir(null)?.path + File.separator
                                + UPDATE_PIC_FILE_NAME
                    )
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
            binding!!.ivDelete->{
                File(getExternalFilesDir(null)?.path+File.separator
                        + UPDATE_PIC_FILE_NAME).delete()
                setResult(Activity.RESULT_OK)
                finish()
            }

            binding?.ivCropUpdateClose ->{
                finish()
            }

        }
    }
}