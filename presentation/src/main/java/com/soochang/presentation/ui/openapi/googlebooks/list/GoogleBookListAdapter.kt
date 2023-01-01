package com.soochang.presentation.ui.openapi.googlebooks.list

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.soochang.domain.model.book.BookItem
import com.soochang.domain.model.book.BookItems
import com.soochang.presentation.databinding.*
import com.soochang.presentation.ui.openapi.googlebooks.list.model.UIModel

class GoogleBookListAdapter(
    private val bookItemListener: BookItemListener
) : ListAdapter<UIModel, RecyclerView.ViewHolder>(DiffUtilCallback()) {

    interface BookItemListener {
        fun onBookItemClick(bookItem: BookItem)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(viewGroup.context)

        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = FragmentGoogleBookListCellHeaderBinding.inflate(layoutInflater, viewGroup, false)
                HeaderViewHolder(binding)
            }
            VIEW_TYPE_PROGRESS -> {
                val binding = FragmentGoogleBookListCellProgressBinding.inflate(layoutInflater, viewGroup, false)
                ProgressViewHolder(binding)
            }
            else -> {
                val binding = FragmentGoogleBookListCellBinding.inflate(layoutInflater, viewGroup, false)
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

    class HeaderViewHolder(private val binding: FragmentGoogleBookListCellHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(searchResultCount: Int) {
            binding.txtSearchResultCount.text = String.format("검색결과: %d건", searchResultCount)
        }
    }

    class DataViewHolder(
        private val binding: FragmentGoogleBookListCellBinding,
        private val bookItemListener: BookItemListener
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(UIModel: UIModel.Data) {
            binding.bookItem = UIModel.bookItem
            binding.bookItemListener = bookItemListener
        }
    }

    class ProgressViewHolder(private val binding: FragmentGoogleBookListCellProgressBinding) : RecyclerView.ViewHolder(binding.root) {

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