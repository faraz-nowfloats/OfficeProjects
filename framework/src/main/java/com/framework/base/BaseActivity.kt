package com.framework.base

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.framework.R
import com.framework.helper.Navigator
import com.framework.models.BaseViewModel
import com.framework.utils.ConversionUtils
import com.framework.utils.hideKeyBoard
import com.framework.views.customViews.CustomToolbar
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.lang.IllegalStateException

abstract class BaseActivity<Binding : ViewDataBinding, ViewModel : BaseViewModel> : AppCompatActivity(), View.OnClickListener {

  protected var TAG = this.javaClass.simpleName
  protected var navigator: Navigator? = null
  protected var binding: Binding? = null
  protected lateinit var viewModel: ViewModel
  protected var compositeDisposable = CompositeDisposable()

  protected abstract fun getLayout(): Int
  protected abstract fun getViewModelClass(): Class<ViewModel>
  protected abstract fun onCreateView()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setTheme()
    binding = DataBindingUtil.setContentView(this, getLayout())
    binding?.lifecycleOwner = this
    viewModel = ViewModelProviders.of(this).get(getViewModelClass())
    navigator = Navigator(this)
    onCreateView()
    setToolbar()
    val observables = getObservables()
    for (observable in observables) {
      observable?.let { compositeDisposable.add(it) }
    }
  }

  protected open fun getObservables(): List<Disposable?> {
    return listOf()
  }

  protected open fun setTheme() {

  }

  override fun onDestroy() {
    super.onDestroy()
    compositeDisposable.clear()
  }

  open fun getToolbar(): CustomToolbar? {
    return null
  }

  open fun getToolbarTitle(): String? {
    return ""
  }

  open fun getToolbarSubTitle(): String? {
    return null
  }

  open fun getToolbarTitleTypeface(): Typeface? {
    return null
  }

  open fun getToolbarTitleSize(): Float? {
    return null
  }

  open fun getToolbarSubTitleSize(): Float? {
    return resources.getDimension(R.dimen.toolbar_sub_title)
  }

  open fun getNavIconScale(): Float {
    return 1f
  }

  open fun getToolbarTitleGravity(): Int {
    return Gravity.START
  }

  /**
   * The new text color in 0xAARRGGBB format
   */
  open fun getToolbarTitleColor(): Int? {
    return null
  }

  /**
   * The new text color in 0xAARRGGBB format
   */
  open fun getToolbarBackgroundColor(): Int? {
    return null
  }

  open fun getNavigationIcon(): Drawable? {
    return null
  }

  open fun isVisibleBackButton(): Boolean {
    return true
  }

  open fun isHideToolbar(): Boolean {
    return false
  }

  fun adjustToolbarTitleMarginEnd(menu: Menu) {
    if (menu.size() > 0 && this.getToolbarTitleGravity() == Gravity.CENTER_HORIZONTAL) {
      this.getToolbar()?.titleMarginEnd = ConversionUtils.dp2px(16f)
    } else {
      this.getToolbar()?.titleMarginEnd = ConversionUtils.dp2px(72f)
    }
  }

  private fun setToolbar() {
    val toolbar = getToolbar() ?: return
    if (!isHideToolbar()) {
      toolbar.setNavigationOnClickListener {
        onBackPressed()
      }
      setToolbarTitle(getToolbarTitle())
      getToolbarSubTitle()?.let { setToolbarSubTitle(it) }
      toolbar.getNavImageButton()?.let {
        it.scaleX = getNavIconScale()
        it.scaleY = getNavIconScale()
      }
      getNavigationIcon()?.let { toolbar.navigationIcon = it }
      getToolbarBackgroundColor()?.let { toolbar.setBackgroundColor(it) }
      setSupportActionBar(toolbar)
      val actionBar: ActionBar? = supportActionBar
      actionBar?.setDisplayHomeAsUpEnabled(isVisibleBackButton())
    } else toolbar.visibility = View.GONE
  }

  fun setToolbarTitle(title: String?) {
    val toolbar = getToolbar() ?: return
    toolbar.title = title
    getToolbarTitleColor()?.let { toolbar.setTitleTextColor(it) }
    toolbar.getTitleTextView()?.let { titleView->
      titleView.setToolbarTitleGravity()
      getToolbarTitleTypeface()?.let { titleView.typeface = it }
      getToolbarTitleSize()?.let { titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, it) }
    }
  }

  fun setToolbarSubTitle(subTitle: String) {
    val toolbar = getToolbar() ?: return
    toolbar.subtitle = subTitle
    getToolbarTitleColor()?.let { toolbar.setSubtitleTextColor(it) }
    toolbar.getSubTitleTextView()?.let { subTitleView ->
      subTitleView.setToolbarTitleGravity()
      getToolbarSubTitleSize()?.let { subTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, it) }
      subTitleView.alpha = 0.7F
    }
  }

  private fun TextView.setToolbarTitleGravity() {
    textAlignment = TextView.TEXT_ALIGNMENT_GRAVITY
    gravity = getToolbarTitleGravity()
    (layoutParams as? Toolbar.LayoutParams)?.width = Toolbar.LayoutParams.MATCH_PARENT
  }


  override fun onClick(v: View?) {

  }


  open fun onNavPressed() {
    this.hideKeyBoard()
    onBackPressed()
  }

  override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
    return if (keyCode == KeyEvent.KEYCODE_MENU) {
      true
    } else super.onKeyDown(keyCode, event)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      android.R.id.home -> {
        onNavPressed()
        return true
      }
    }
    return false
  }


  override fun onPrepareOptionsMenu(menu: Menu): Boolean {
    val result = super.onPrepareOptionsMenu(menu)
    this.adjustToolbarTitleMarginEnd(menu)
    return result
  }

  open fun setOnClickListener(vararg views: View?) {
    for (view in views) view?.setOnClickListener(this)
  }

  open fun removeOnClickListener(vararg views: View) {
    for (view in views) view.setOnClickListener(null)
  }

  // Fragment
  open fun addFragmentReplace(containerId: Int?, fragment: Fragment?, addToBackStack: Boolean) {
    if (supportFragmentManager.isDestroyed) return
    if (containerId == null || fragment == null) return

    val fragmentTransaction = supportFragmentManager.beginTransaction()
    if (addToBackStack) {
      fragmentTransaction.addToBackStack(fragment.javaClass.name)
    }
    try {
      fragmentTransaction.replace(containerId, fragment).commit()
    } catch (e: IllegalStateException) {
      e.printStackTrace()
    }
  }

  open fun getTopFragment(): Fragment? {
    supportFragmentManager.run {
      return when (backStackEntryCount) {
        0 -> null
        else -> findFragmentByTag(getBackStackEntryAt(backStackEntryCount - 1).name)
      }
    }
  }

  fun showLongToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
  }

  fun showShortToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
  }

}