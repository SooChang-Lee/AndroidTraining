package com.soochang.presentation.ui.recyclerview.general.list

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.soochang.domain.model.book.BookItem
import com.soochang.presentation.R
import com.soochang.presentation.databinding.FragmentGeneralBookListCellBinding
import com.soochang.presentation.databinding.FragmentGeneralBookListCellHeaderBinding
import com.soochang.presentation.databinding.FragmentGeneralBookListCellProgressBinding
import com.soochang.presentation.ui.recyclerview.general.list.model.UIModel

class GeneralBookListAdapter(
    private val bookItemListener: BookItemListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var listBookItem: ArrayList<UIModel> = ArrayList()

    interface BookItemListener {
        fun onBookItemClick(bookItem: BookItem)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(viewGroup.context)

        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = FragmentGeneralBookListCellHeaderBinding.inflate(layoutInflater, viewGroup, false)
                HeaderViewHolder(binding)
            }
            VIEW_TYPE_PROGRESS -> {
                val binding = FragmentGeneralBookListCellProgressBinding.inflate(layoutInflater, viewGroup, false)
                ProgressViewHolder(binding)
            }
            else -> {
                val binding = FragmentGeneralBookListCellBinding.inflate(layoutInflater, viewGroup, false)
                binding.txtListPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG

                DataViewHolder(binding, bookItemListener)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is HeaderViewHolder -> {
                val dataItem = listBookItem[position] as UIModel.Header
                holder.bind(dataItem.totalCount)
            }
            is ProgressViewHolder -> {

            }
            is DataViewHolder -> {
                val dataItem = listBookItem[position] as UIModel.Data
                holder.bind(dataItem)
            }
        }
    }

    class HeaderViewHolder(private val binding: FragmentGeneralBookListCellHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(searchResultCount: Int) {
            binding.txtSearchResultCount.text = String.format("검색결과: %d건", searchResultCount)
        }
    }

    inner class DataViewHolder(
        private val binding: FragmentGeneralBookListCellBinding,
        private val bookItemListener: BookItemListener
    ) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        init {
            binding.root.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val dataItem = listBookItem[bindingAdapterPosition]
            if( dataItem is UIModel.Data ){
                bookItemListener.onBookItemClick(dataItem.bookItem)
            }
        }

        fun bind(UIModel: UIModel.Data) {
            binding.apply {
                val bookItem = UIModel.bookItem

                val ctx = binding.root.context

                Glide
                    .with(ctx)
                    .load(bookItem.imageLinks?.thumbnail)
                    .placeholder(R.color.lightGray)
//                .error(R.drawable.ic_launcher)
//                .fallback(R.color.lightGray)
                    .into(imgThumbnail)

                txtTitle.text = bookItem.title

                txtAuthor.text = bookItem.authors?.joinToString(", ")

                txtPublishedData.text = bookItem.publishedDate

                txtRetailPrice.text = ctx.getString(R.string.thousand_comma_price, bookItem.retailPrice)

                txtListPrice.text = "(${ctx.getString(R.string.thousand_comma_price, bookItem.listPrice)})"

                //할인율 표시(할인중: 할인율 표시, 정가판매: 할인율, 정가 숨기기)
                if((bookItem.retailPrice ?: 0) < (bookItem.listPrice ?: 0)){
                    //할인판매
                    val discountRatio = 100 - (bookItem.retailPrice!! / bookItem.listPrice!!.toDouble() * 100).toInt()
                    txtDiscountRatio.text = "$discountRatio%"

                    txtListPrice.visibility = View.VISIBLE
                    txtDiscountRatio.visibility = View.VISIBLE
                }else{
                    //정가판매
                    txtListPrice.visibility = View.GONE
                    txtDiscountRatio.visibility = View.GONE
                }

                //품절상태 표시(판매중: 가격정보 표시, 품절: 품절상태 표시)
                if( bookItem.saleStatus == "NOT_FOR_SALE" ){
                    priceContainer.visibility = View.GONE
                    txtNotForSale.visibility = View.VISIBLE
                }else{
                    priceContainer.visibility = View.VISIBLE
                    txtNotForSale.visibility = View.GONE
                }
            }
        }
    }

    class ProgressViewHolder(private val binding: FragmentGeneralBookListCellProgressBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun getItemViewType(position: Int): Int {
        return when(listBookItem[position]){
            is UIModel.Header -> VIEW_TYPE_HEADER
            is UIModel.Data -> VIEW_TYPE_DATA
            is UIModel.Progress -> VIEW_TYPE_PROGRESS
        }
    }

    override fun getItemCount(): Int{
        return listBookItem.size
    }

    fun setData(listDataItem: List<UIModel>){
        //리스트 데이터 추가
        val initialSize = this.listBookItem.size

        this.listBookItem.clear()
        this.listBookItem.addAll(listDataItem)

        val updatedSize = this.listBookItem.size

        if( initialSize < updatedSize ){
            //헤더뷰 데이터 갱신
            notifyItemChanged(0)

            //추가된 셀만 데이터 갱신
            notifyItemRangeInserted(initialSize, updatedSize)
        }else{
            //초기로드
            notifyDataSetChanged()
        }
    }

    companion object {
        const val VIEW_TYPE_HEADER = 1001
        const val VIEW_TYPE_DATA = 1002
        const val VIEW_TYPE_PROGRESS = 1003
    }
}