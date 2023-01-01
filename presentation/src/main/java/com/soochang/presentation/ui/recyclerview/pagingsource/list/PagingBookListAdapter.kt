package com.soochang.presentation.ui.recyclerview.pagingsource.list

import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.soochang.domain.model.book.BookItem
import com.soochang.presentation.databinding.*
import com.soochang.presentation.ui.recyclerview.listadapter.list.ListAdapterBookListAdapter

class PagingBookListAdapter(
    private val bookItemListener: BookItemListener
) : PagingDataAdapter<BookItem, PagingBookListAdapter.DataViewHolder>(DiffUtilCallback()) {

    interface BookItemListener {
        fun onBookItemClick(bookItem: BookItem)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): DataViewHolder {
        val layoutInflater = LayoutInflater.from(viewGroup.context)

        val binding = FragmentPagingBookListCellBinding.inflate(layoutInflater, viewGroup, false)
        binding.txtListPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG

        return DataViewHolder(binding, bookItemListener)
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        val bookItem = getItem(position)
        holder.bind(bookItem)
    }

//    override fun getItemId(position: Int): Long {
//        return getItem(position)?.itemId ?: 0
//    }

    inner class DataViewHolder(
        private val binding: FragmentPagingBookListCellBinding,
        private val bookItemListener: BookItemListener
    ) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        init {
            binding.root.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val bookItem = snapshot()[bindingAdapterPosition]
            if( bookItem != null ) {
                bookItemListener.onBookItemClick(bookItem)
            }
        }

        fun bind(bookItem: BookItem?) {
            binding.bookItem = bookItem

            if( bookItem == null )
                return
        }
    }

    class DiffUtilCallback : DiffUtil.ItemCallback<BookItem>() {
        override fun areItemsTheSame(oldItem: BookItem, newItem: BookItem) =
            oldItem.itemId == newItem.itemId

        override fun areContentsTheSame(oldItem: BookItem, newItem: BookItem) =
            oldItem == newItem
    }
}