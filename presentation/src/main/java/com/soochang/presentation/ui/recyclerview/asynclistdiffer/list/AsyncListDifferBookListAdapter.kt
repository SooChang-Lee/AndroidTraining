package com.soochang.presentation.ui.recyclerview.asynclistdiffer.list

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.soochang.domain.model.book.BookItem
import com.soochang.presentation.databinding.FragmentAsynclistdifferBookListCellBinding
import com.soochang.presentation.databinding.FragmentAsynclistdifferBookListCellHeaderBinding
import com.soochang.presentation.databinding.FragmentAsynclistdifferBookListCellProgressBinding
import com.soochang.presentation.ui.recyclerview.asynclistdiffer.list.model.UIModel

class AsyncListDifferBookListAdapter(
    private val bookItemListener: BookItemListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val asyncListDiffer: AsyncListDiffer<UIModel> = AsyncListDiffer(this, DiffUtilCallback())

    interface BookItemListener {
        fun onBookItemClick(bookItem: BookItem)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(viewGroup.context)

        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = FragmentAsynclistdifferBookListCellHeaderBinding.inflate(layoutInflater, viewGroup, false)
                HeaderViewHolder(binding)
            }
            VIEW_TYPE_PROGRESS -> {
                val binding = FragmentAsynclistdifferBookListCellProgressBinding.inflate(layoutInflater, viewGroup, false)
                ProgressViewHolder(binding)
            }
            else -> {
                val binding = FragmentAsynclistdifferBookListCellBinding.inflate(layoutInflater, viewGroup, false)
                binding.txtListPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG

                DataViewHolder(binding, bookItemListener)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is HeaderViewHolder -> {
                val dataItem = asyncListDiffer.currentList[position] as UIModel.Header
                holder.bind(dataItem.totalCount)
            }
            is ProgressViewHolder -> {

            }
            is DataViewHolder -> {
                val dataItem = asyncListDiffer.currentList[position] as UIModel.Data
                holder.bind(dataItem)
            }
        }
    }

    class HeaderViewHolder(private val binding: FragmentAsynclistdifferBookListCellHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(searchResultCount: Int) {
            binding.txtSearchResultCount.text = String.format("검색결과: %d건", searchResultCount)
        }
    }

    inner class DataViewHolder(
        private val binding: FragmentAsynclistdifferBookListCellBinding,
        private val bookItemListener: BookItemListener
    ) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        init {
            binding.root.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val dataItem = asyncListDiffer.currentList[bindingAdapterPosition]
            if( dataItem is UIModel.Data ){
                bookItemListener.onBookItemClick(dataItem.bookItem)
            }
        }

        fun bind(UIModel: UIModel.Data) {
            binding.bookItem = UIModel.bookItem
        }
    }

    class ProgressViewHolder(private val binding: FragmentAsynclistdifferBookListCellProgressBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun getItemViewType(position: Int): Int {
        return when(asyncListDiffer.currentList[position]){
            is UIModel.Header -> VIEW_TYPE_HEADER
            is UIModel.Data -> VIEW_TYPE_DATA
            is UIModel.Progress -> VIEW_TYPE_PROGRESS
        }
    }

    override fun getItemCount(): Int{
        return asyncListDiffer.currentList.size
    }

    override fun getItemId(position: Int): Long {
        return asyncListDiffer.currentList[position].itemId
    }

    class DiffUtilCallback : DiffUtil.ItemCallback<UIModel>() {
        override fun areItemsTheSame(oldItem: UIModel, newItem: UIModel) =
            oldItem.itemId == newItem.itemId

        override fun areContentsTheSame(oldItem: UIModel, newItem: UIModel) =
            oldItem == newItem
    }

    companion object{
        private const val VIEW_TYPE_HEADER = 1001
        private const val VIEW_TYPE_DATA = 1002
        private const val VIEW_TYPE_PROGRESS = 1003
    }
}