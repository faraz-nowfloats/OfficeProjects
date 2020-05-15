package com.framework.base

import android.app.Dialog
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProviders
import com.framework.helper.Navigator
import com.framework.models.BaseViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheetDialog<Binding : ViewDataBinding, ViewModel : BaseViewModel?> : BottomSheetDialogFragment(), View.OnClickListener {

  companion object {
    val RESULT_OK = -1
    val RESULT_CANCELED = 0
  }

  interface BottomSheetDialogResult {
    fun onBottomSheetDismiss(result: Int, data: Any?)
  }

  var callback: BottomSheetDialogResult? = null

  protected lateinit var baseActivity: BaseActivity<*, *>
  protected lateinit var root: View
  protected var viewModel: ViewModel? = null
  protected var binding: Binding? = null
  protected var navigator: Navigator? = null

  protected abstract fun getLayout(): Int
  protected abstract fun getViewModelClass(): Class<ViewModel>
  protected abstract fun onCreateView()

  private var resultCancelled = true
  protected lateinit var dialog: BottomSheetDialog

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    baseActivity = activity as BaseActivity<*, *>
    binding = DataBindingUtil.inflate(inflater, getLayout(), container, false)
    binding?.lifecycleOwner = this
    navigator = Navigator(baseActivity)
    viewModel = ViewModelProviders.of(this).get(getViewModelClass())
    return binding?.root
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
    dialog.setOnShowListener {
      val dialog = it as? BottomSheetDialog ?: return@setOnShowListener
      val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) ?: return@setOnShowListener
      BottomSheetBehavior.from(bottomSheet).apply { state = getBottomSheetInitialState() }
    }
    return dialog
  }

  fun dismiss(result: Int, data: Any?) {
    resultCancelled = result == RESULT_CANCELED
    if (!resultCancelled) callback?.onBottomSheetDismiss(result, data)
    dismiss()
  }

  override fun dismiss() {
    if (resultCancelled) callback?.onBottomSheetDismiss(RESULT_CANCELED, null)
    callback = null
    super.dismiss()
  }

  override fun onResume() {
    super.onResume()
    val window = dialog.window ?: return
    val params = window.attributes
    params.width = getWidth() ?: params.width
    params.height = getHeight() ?: params.height
    window.attributes = params
  }

  protected open fun getWidth(): Int? {
    return null
  }

  protected open fun getHeight(): Int? {
    return null
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    onCreateView()

//    getBehaviour().addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
//      override fun onSlide(bottomSheet: View, slideOffset: Float) {
//
//      }
//
//      override fun onStateChanged(bottomSheet: View, newState: Int) {
//        if (newState == BottomSheetBehavior.STATE_HALF_EXPANDED ||
//            newState == BottomSheetBehavior.STATE_COLLAPSED) {
//          getBehaviour().state = BottomSheetBehavior.STATE_EXPANDED
//        } else {
//          println()
//        }
//      }
//    })
    val parent = view.parent as? View
    val layoutParams = parent?.layoutParams as? CoordinatorLayout.LayoutParams
    layoutParams?.setMargins(getMarginStart(), getMarginTop(), getMarginEnd(), getMarginBottom())
    parent?.layoutParams = layoutParams
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    val resources = resources
    if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
      val parent = view?.parent as? View
      val layoutParams = parent?.layoutParams as? CoordinatorLayout.LayoutParams
      layoutParams?.setMargins(getMarginStart(), getMarginTop(), getMarginEnd(), getMarginBottom())
      parent?.layoutParams = layoutParams
    }
  }

  override fun onClick(v: View) {}


  fun setOnClickListener(vararg views: View?) {
    for (view in views) view?.setOnClickListener(this)
  }

  fun removeOnClickListener(vararg views: View) {
    for (view in views) view.setOnClickListener(null)
  }

  fun showLongToast(string: String) {
    Toast.makeText(activity, string, Toast.LENGTH_LONG).show()
  }

  fun showShortToast(string: String) {
    Toast.makeText(activity, string, Toast.LENGTH_SHORT).show()
  }

  fun getBehaviour(): BottomSheetBehavior<FrameLayout> {
    return dialog.behavior
  }

  open fun getBottomSheetInitialState(): Int {
    return BottomSheetBehavior.STATE_EXPANDED
  }

  open fun getMarginTop(): Int {
    return 0
  }

  open fun getMarginStart(): Int {
    return 0
  }

  open fun getMarginEnd(): Int {
    return 0
  }

  open fun getMarginBottom(): Int {
    return 0
  }
}