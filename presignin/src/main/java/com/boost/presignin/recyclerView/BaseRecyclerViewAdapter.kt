package com.boost.presignin.recyclerView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.boost.presignin.constant.RecyclerViewItemType
import com.boost.presignin.recyclerView.BaseRecyclerViewHolder
import com.framework.base.BaseActivity

abstract class BaseRecyclerViewAdapter<T : BaseRecyclerViewItem>(
  var activity: BaseActivity<*, *>,
  var list: ArrayList<T>,
  private var itemClickListener: RecyclerItemClickListener?,
) : RecyclerView.Adapter<BaseRecyclerViewHolder<*>>() {

  protected var isLoaderVisible = false

  override fun onBindViewHolder(viewHolder: BaseRecyclerViewHolder<*>, position: Int) {
    viewHolder.activity = activity
    viewHolder.listener = itemClickListener
    viewHolder.itemCount = itemCount
    viewHolder.bind(position, list[position])
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseRecyclerViewHolder<*> {
    return getViewHolder(parent, viewType)
  }

  abstract fun getViewHolder(parent: ViewGroup, viewType: Int): BaseRecyclerViewHolder<*>

  override fun getItemViewType(position: Int): Int {
    return list[position].getViewType()
  }

  override fun getItemCount(): Int = list.size

  fun updateList(newList: ArrayList<T>) {
    this.list.clear()
    this.list.addAll(newList)
    notifyDataSetChanged()
  }

  fun insertItem(index: Int, item: T) {
    this.list.add(index, item)
  }

  fun removeAt(index: Int) {
    this.list.removeAt(index)
  }


  protected fun getViewDataBinding(
    inflater: LayoutInflater,
    recyclerViewItemType: RecyclerViewItemType,
    parent: ViewGroup
  ): ViewDataBinding {
    return DataBindingUtil.inflate(inflater, recyclerViewItemType.getLayout(), parent, false)
  }
}