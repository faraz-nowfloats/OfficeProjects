package com.boost.cart.ui.popup

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.boost.cart.CartActivity
import com.boost.cart.ui.home.CartViewModel
import com.boost.payment.R
import com.boost.payment.PaymentActivity
import com.boost.payment.adapter.StateListAdapter
import com.boost.dbcenterapi.data.api_model.customerId.get.Result
import com.boost.dbcenterapi.data.api_model.stateCode.Data
import com.boost.payment.interfaces.StateListener
import com.boost.payment.ui.payment.PaymentViewModel
import kotlinx.android.synthetic.main.statelist_fragment.*
import kotlin.collections.ArrayList


class StateListPopFragment : DialogFragment(), com.boost.cart.interfaces.StateListener {

  var createCustomerInfoRequest: Result? = null

  var customerInfoState = false

  lateinit var stateListAdapter: com.boost.cart.adapter.StateListAdapter
  private var getState: String? = null
  private var getStateTin: String? = null


  companion object {
    fun newInstance() = StateListPopFragment()
  }

  private lateinit var viewModel: CartViewModel

  override fun onStart() {
    super.onStart()
    val width = ViewGroup.LayoutParams.MATCH_PARENT
    val height = ViewGroup.LayoutParams.MATCH_PARENT
    dialog!!.window!!.setLayout(width, height)
    dialog!!.window!!.setBackgroundDrawableResource(R.color.fullscreen_color)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.statelist_fragment, container, false)
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    viewModel = ViewModelProviders.of(requireActivity()).get(CartViewModel::class.java)
    stateListAdapter = com.boost.cart.adapter.StateListAdapter((activity as CartActivity), ArrayList(),this)
    getState = requireArguments().getString("state")
    getStateTin = requireArguments().getString("stateTin")
    showProgressBar()
    initializeFreeAddonsRecyclerView()
    initMvvm()


    close.setOnClickListener {

      dismiss()
    }

    dialog!!.setOnKeyListener { dialog, keyCode, event ->
      if (keyCode == KeyEvent.KEYCODE_BACK || event.action == KeyEvent.ACTION_UP) {
//                Toasty.info(requireContext(), "Accept Any One Condition...", Toast.LENGTH_LONG).show()
        return@setOnKeyListener true
      }
      false
    }
  }


  @SuppressLint("FragmentLiveDataObserve")
  private fun initMvvm() {
    loadStatesList()
    viewModel.getStatesResult().observe(this,{
      if(it != null){
        var stateData = arrayListOf<Data>()
        it.result!!.data!!.forEach {
            stateData.add(Data(it.id,it.state,it.stateCode,it.stateTin))
        }
        stateListAdapter.addupdates(stateData,getState,getStateTin)
        stateListAdapter.notifyDataSetChanged()
    }
      hideProgressBar()
    })

  }
  private fun loadStatesList(){
    viewModel.getStatesWithCodes(
      (activity as? CartActivity)?.getAccessToken() ?: "",
      (activity as CartActivity).clientid,
      states_progress_bar
    )
  }
  private fun showProgressBar(){
    states_progress_bar.visibility = View.VISIBLE
  }
  private fun hideProgressBar(){
    states_progress_bar.visibility = View.GONE
  }
  override fun onDestroy() {
    super.onDestroy()
//        requireActivity().viewModelStore.clear()
  }

  fun initializeFreeAddonsRecyclerView() {
    val gridLayoutManager = GridLayoutManager(requireContext(), 1)
    gridLayoutManager.orientation = LinearLayoutManager.VERTICAL
    recycler_state.apply {
      layoutManager = gridLayoutManager

    }
    recycler_state.adapter = stateListAdapter
  }


  override fun stateSelected(data: Data) {
    getState = data.state
    getStateTin = data.stateTin
    viewModel.selectedStateResult(data.state.toString())
    viewModel.selectedStateTinResult(data.stateTin.toString())
    Handler().postDelayed({
      dismiss()
    }, 300)

  }
}
