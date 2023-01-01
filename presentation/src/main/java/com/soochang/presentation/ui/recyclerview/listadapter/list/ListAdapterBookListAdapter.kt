package com.soochang.presentation.ui.recyclerview.listadapter.list

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.soochang.domain.model.book.BookItem
import com.soochang.presentation.databinding.FragmentListadapterBookListCellBinding
import com.soochang.presentation.databinding.FragmentListadapterBookListCellHeaderBinding
import com.soochang.presentation.databinding.FragmentListadapterBookListCellProgressBinding
import com.soochang.presentation.ui.recyclerview.listadapter.list.model.UIModel

class ListAdapterBookListAdapter(
    private val bookItemListener: BookItemListener
) : ListAdapter<UIModel, RecyclerView.ViewHolder>(DiffUtilCallback()) {

    interface BookItemListener {
        fun onBookItemClick(bookItem: BookItem)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(viewGroup.context)

        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = FragmentListadapterBookListCellHeaderBinding.inflate(layoutInflater, viewGroup, false)
                HeaderViewHolder(binding)
            }
            VIEW_TYPE_PROGRESS -> {
                val binding = FragmentListadapterBookListCellProgressBinding.inflate(layoutInflater, viewGroup, false)
                ProgressViewHolder(binding)
            }
            else -> {
                val binding = FragmentListadapterBookListCellBinding.inflate(layoutInflater, viewGroup, false)
                binding.txtListPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG

                DataViewHolder(binding, bookItemListener)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is HeaderViewHolder -> {
                val dataItem = currentList[position] as UIModel.Header
                holder.bind(dataItem.totalCount)
            }
            is ProgressViewHolder -> {

            }
            is DataViewHolder -> {
                val dataItem = currentList[position] as UIModel.Data
                holder.bind(dataItem)
            }
        }
    }

    class HeaderViewHolder(private val binding: FragmentListadapterBookListCellHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(searchResultCount: Int) {
            binding.txtSearchResultCount.text = String.format("검색결과: %d건", searchResultCount)
        }
    }

    inner class DataViewHolder(
        private val binding: FragmentListadapterBookListCellBinding,
        private val bookItemListener: BookItemListener
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
        }
    }

    class ProgressViewHolder(private val binding: FragmentListadapterBookListCellProgressBinding) : RecyclerView.ViewHolder(binding.root) {

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
        return position.toLong()
    }

    class DiffUtilCallback : DiffUtil.ItemCallback<UIModel>() {
        override fun areItemsTheSame(oldItem: UIModel, newItem: UIModel) =
            oldItem.toString() == newItem.toString()

        override fun areContentsTheSame(oldItem: UIModel, newItem: UIModel) =
            oldItem == newItem
    }

    companion object{
        private const val VIEW_TYPE_HEADER = 1001
        private const val VIEW_TYPE_DATA = 1002
        private const val VIEW_TYPE_PROGRESS = 1003
    }
}