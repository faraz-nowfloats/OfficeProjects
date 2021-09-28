package com.framework.base

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.FontRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProviders
import com.framework.R
import com.framework.helper.Navigator
import com.framework.models.BaseViewModel
import com.framework.utils.hideKeyBoard
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseFragment<Binding : ViewDataBinding, ViewModel : BaseViewModel?> : Fragment(), View.OnClickListener {

  protected lateinit var baseActivity: BaseActivity<*, *>
  protected lateinit var root: View
  protected var viewModel: ViewModel? = null
  protected var binding: Binding? = null
  protected var navigator: Navigator? = null
  protected var compositeDisposable = CompositeDisposable()


  protected abstract fun getLayout(): Int
  protected abstract fun getViewModelClass(): Class<ViewModel>
  protected abstract fun onCreateView()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    baseActivity = activity as BaseActivity<*, *>
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    setHasOptionsMenu(true)
    binding = DataBindingUtil.inflate(inflater, getLayout(), container, false)
    binding?.lifecycleOwner = this
    navigator = Navigator(baseActivity)
    viewModel = ViewModelProviders.of(this).get(getViewModelClass())
    return binding?.root
  }

  override fun onPrepareOptionsMenu(menu: Menu) {
    super.onPrepareOptionsMenu(menu)
    baseActivity.adjustToolbarTitleMarginEnd(menu)
  }

  fun setToolbarTitle(title: String?) {
    baseActivity.setToolbarTitle(title)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    onCreateView()
    val observables = getObservables()
    for (observable in observables) {
      observable?.let { compositeDisposable.add(it) }
    }
  }

  fun showSnackBarNegative(context: Activity, msg: String?) {
    val snackBar =
      Snackbar.make(context.findViewById(android.R.id.content), msg!!, Snackbar.LENGTH_INDEFINITE)
    snackBar.view.setBackgroundColor(
      ContextCompat.getColor(
        context,
        R.color.snackbar_negative_color
      )
    )
    snackBar.duration = 4000
    snackBar.show()
  }


  fun showSnackBarPositive(context: Activity, msg: String?) {
    val snackBar =
      Snackbar.make(context.findViewById(android.R.id.content), msg!!, Snackbar.LENGTH_INDEFINITE)
    snackBar.view.setBackgroundColor(
      ContextCompat.getColor(
        context,
        R.color.snackbar_positive_color
      )
    )
    snackBar.duration = 4000
    snackBar.show()
  }

  override fun onDestroy() {
    super.onDestroy()
    compositeDisposable.clear()
  }

  override fun onClick(v: View) {}


  fun setOnClickListener(vararg views: View?) {
    for (view in views) view?.setOnClickListener(this)
  }

  fun isVisible(vararg views: View?) {
    for (view in views) view?.visibility = View.VISIBLE
  }

  fun isGone(vararg views: View?) {
    for (view in views) view?.visibility = View.GONE
  }

  fun removeOnClickListener(vararg views: View) {
    for (view in views) view.setOnClickListener(null)
  }

  protected open fun getObservables(): List<Disposable?> {
    return listOf()
  }

  // Transactions
  fun addFragmentReplace(containerID: Int, fragment: Fragment, addToBackStack: Boolean) {
    val fragmentTransaction = fragmentManager?.beginTransaction()
    if (addToBackStack) fragmentTransaction?.addToBackStack(null)
    fragmentTransaction?.replace(containerID, fragment)?.commit()
  }

  fun getTopFragment(): Fragment? {
    parentFragmentManager.run {
      return when (backStackEntryCount) {
        0 -> null
        else -> findFragmentByTag(getBackStackEntryAt(backStackEntryCount - 1).name)
      }
    }
  }

  fun getLifeCycleState(): Lifecycle.State {
    return viewLifecycleOwner.lifecycle.currentState
  }

  fun showLongToast(string: String?) {
    Toast.makeText(activity, string, Toast.LENGTH_LONG).show()
  }

  fun showShortToast(string: String?) {
    Toast.makeText(activity, string, Toast.LENGTH_SHORT).show()
  }

  protected fun getColor(@ColorRes color: Int): Int {
    return ResourcesCompat.getColor(resources, color, context?.theme)
  }

  protected fun getFont(@FontRes font: Int): Typeface? {
    return ResourcesCompat.getFont(baseActivity, font)
  }

  override fun onStop() {
    super.onStop()
    baseActivity.hideKeyBoard()
  }
}