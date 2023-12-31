package com.appservice.ui.catalog.catalogProduct.listing

import android.os.Bundle
import com.appservice.R
import com.appservice.base.AppBaseFragment
import com.appservice.constant.IntentConstant
import com.appservice.constant.RecyclerViewItemType
import com.appservice.databinding.FragmentProductCategoryListingBinding
import com.appservice.model.serviceProduct.CatalogProduct
import com.appservice.recyclerView.AppBaseRecyclerViewAdapter
import com.appservice.recyclerView.AppBaseRecyclerViewItem
import com.appservice.recyclerView.BaseRecyclerViewItem
import com.appservice.recyclerView.RecyclerItemClickListener
import com.appservice.rest.TaskCode
import com.appservice.viewmodel.ProductViewModel
import com.framework.base.BaseResponse
import com.framework.pref.UserSessionManager
import com.framework.pref.clientId
import kotlinx.android.synthetic.main.fragment_product_details.*
import java.io.Serializable
import java.util.*
import kotlin.collections.HashSet

class FragmentProductCategory : AppBaseFragment<FragmentProductCategoryListingBinding, ProductViewModel>(), RecyclerItemClickListener {
    override fun getLayout(): Int {
        return R.layout.fragment_product_category_listing
    }

    private var categoryList: HashSet<CategoryProduct> = hashSetOf()
    private var adapterN: AppBaseRecyclerViewAdapter<CategoryProduct>? = null
    override fun getViewModelClass(): Class<ProductViewModel> {
        return ProductViewModel::class.java
    }

    companion object {
        fun newInstance(fpTag: String?): FragmentProductCategory {
            val bundle = Bundle()
            bundle.putString(IntentConstant.FP_TAG.name, fpTag)
            val fragment = FragmentProductCategory()
            fragment.arguments = bundle
            return fragment
        }

        fun newInstance(): FragmentProductCategory {
            return FragmentProductCategory()
        }
    }



    override fun onCreateView() {
        sessionLocal = UserSessionManager(requireActivity())
        getProductListing()
    }

    private fun getProductListing() {
        hitApi(viewModel?.getProductListing(sessionLocal.fpTag, clientId = clientId, 0), R.string.error_getting_a_product_list)
    }

    override fun onSuccess(it: BaseResponse) {
        when (it.taskcode) {
            TaskCode.GET_PRODUCT_LISTING.ordinal -> onProductListingReceived(it)
        }
    }

    private fun onProductListingReceived(it: BaseResponse) {
        if (it.isSuccess()) {
            val productListing = it.anyResponse as? ArrayList<CatalogProduct>
            if (productListing.isNullOrEmpty().not())
            filterCategories(productListing)
        }

    }

    private fun filterCategories(productListing: ArrayList<CatalogProduct>?) {
        val categories: ArrayList<String> = arrayListOf()
        productListing?.forEach { catalogProduct -> if (catalogProduct.category != null) categories.add(catalogProduct.category!!) }
        categories.forEach { cat -> categoryList.add(CategoryProduct(cat, Collections.frequency(categories, cat))) }
        binding?.rvProductCategory?.apply {
            adapterN = AppBaseRecyclerViewAdapter(baseActivity, categoryList.toList() as ArrayList<CategoryProduct>, this@FragmentProductCategory)
            adapter = adapterN
        }
        (parentFragment as FragmentProductHome).setTabTitle("Categories (${categoryList.size})", 1)
    }


    override fun onItemClick(position: Int, item: BaseRecyclerViewItem?, actionType: Int) {

    }
}

data class CategoryProduct(var name: String, var countItems: Int? = 0, var isSelected: Boolean? = false, var recyclerViewItem: Int = RecyclerViewItemType.PRODUCT_CATEGORY_ITEM_VIEW.getLayout()) : AppBaseRecyclerViewItem, Serializable {
    override fun getViewType(): Int {
        return recyclerViewItem
    }
}