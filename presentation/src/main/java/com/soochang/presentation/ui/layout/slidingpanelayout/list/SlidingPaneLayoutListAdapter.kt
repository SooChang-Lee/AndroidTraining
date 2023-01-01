package com.soochang.presentation.ui.layout.slidingpanelayout.list

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.soochang.domain.model.book.BookItem
import com.soochang.domain.model.book.BookItems
import com.soochang.presentation.databinding.FragmentSlidingPaneLayoutListCellBinding
import com.soochang.presentation.databinding.FragmentSlidingPaneLayoutListCellHeaderBinding
import com.soochang.presentation.databinding.FragmentSlidingPaneLayoutListCellProgressBinding
import com.soochang.presentation.ui.layout.slidingpanelayout.list.model.UIModel

class SlidingPaneLayoutListAdapter(
    private val bookItemListener: BookItemListener
) : ListAdapter<UIModel, RecyclerView.ViewHolder>(DiffUtilCallback()) {

    interface BookItemListener {
        fun onBookItemClick(bookItem: BookItem)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(viewGroup.context)

        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = FragmentSlidingPaneLayoutListCellHeaderBinding.inflate(layoutInflater, viewGroup, false)
                HeaderViewHolder(binding)
            }
            VIEW_TYPE_PROGRESS -> {
                val binding = FragmentSlidingPaneLayoutListCellProgressBinding.inflate(layoutInflater, viewGroup, false)
                ProgressViewHolder(binding)
            }
            else -> {
                val binding = FragmentSlidingPaneLayoutListCellBinding.inflate(layoutInflater, viewGroup, false)
                binding.txtListPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG

                DataViewHolder(binding, bookItemListener)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is HeaderViewHolder -> {
                val UIModel = currentList[position] as UIModel.Header
                holder.bind(UIModel.totalCount)
            }
            is ProgressViewHolder -> {

            }
            is DataViewHolder -> {
                val UIModel = currentList[position] as UIModel.Data
                holder.bind(UIModel)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(currentList[position]){
            is UIModel.Header -> VIEW_TYPE_HEADER
            is UIModel.Data -> VIEW_TYPE_DATA
            is UIModel.Progress -> VIEW_TYPE_PROGRESS
        }
    }

    override fun getItemCount(): Int{
        return currentList.size
    }

    override fun getItemId(position: Int): Long {
        return currentList[position].itemId
    }

    fun setData(listBookItem: List<UIModel>, commitCallback: (() -> Unit)? = null){
        //리스트 데이터 추가
        submitList(listBookItem, commitCallback)
    }



    class HeaderViewHolder(private val binding: FragmentSlidingPaneLayoutListCellHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(searchResultCount: Int) {
            binding.txtSearchResultCount.text = String.format("검색결과: %d건", searchResultCount)
        }
    }

    inner class DataViewHolder(
        private val binding: FragmentSlidingPaneLayoutListCellBinding,
        val bookItemListener: BookItemListener
    ) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        init {
            binding.root.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val dataItem = currentList[bindingAdapterPosition]
            if( dataItem is UIModel.Data ){
                bookItemListener.onBookItemClick(dataItem.bookItem)
            }
        }

        fun bind(UIModel: UIModel.Data) {
            binding.bookItem = UIModel.bookItem
            binding.bookItemListener = bookItemListener
        }
    }

    class ProgressViewHolder(private val binding: FragmentSlidingPaneLayoutListCellProgressBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    class DiffUtilCallback : DiffUtil.ItemCallback<UIModel>() {
        override fun areItemsTheSame(oldItem: UIModel, newItem: UIModel) =
            oldItem.itemId == newItem.itemId

        override fun areContentsTheSame(oldItem: UIModel, newItem: UIModel) =
            oldItem == newItem
    }

    companion object {
        const val VIEW_TYPE_HEADER = 1001
        const val VIEW_TYPE_DATA = 1002
        const val VIEW_TYPE_PROGRESS = 1003
    }
}