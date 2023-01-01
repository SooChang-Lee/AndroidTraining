package com.soochang.presentation.ui.heroanimation.list

import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.soochang.domain.model.book.BookItem
import com.soochang.domain.model.book.BookItems
import com.soochang.presentation.R
import com.soochang.presentation.databinding.*
import com.soochang.presentation.ui.recyclerview.listadapter.list.ListAdapterBookListAdapter

class SharedElementTransitionListAdapter(
    private val bookItemListener:BookItemListener,

    // 데이터 로드가 완료되는 시점에 대기중인 re-enter transition을 시작시킬 수 있도록
    // startPostponedEnterTransition을 수행하는 함수를 매개변수로 받음
    private val onReadyToTransition: () -> Unit
) : ListAdapter<SharedElementTransitionListAdapter.DataItem, RecyclerView.ViewHolder>(DiffUtilCallback()) {

    //re-enter transition을 시작하기전에 클릭한 id에 해당하는 유저의 썸네일 준비여부를 확인하기위해 id보관
    private var lastSelectedId: String? = null

    // Detail Fragment에서 재진입이 예상되는 경우 true
    val expectsTransition: Boolean
        get() = lastSelectedId != null

    interface BookItemListener {
        fun onBookItemClick(bookItem: BookItem, imgThumbnail: ImageView, txtTitle: TextView)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(viewGroup.context)

        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = FragmentSharedElementTransitionListCellHeaderBinding.inflate(layoutInflater, viewGroup, false)
                HeaderViewHolder(binding)
            }
            VIEW_TYPE_PROGRESS -> {
                val binding = FragmentSharedElementTransitionListCellProgressBinding.inflate(layoutInflater, viewGroup, false)
                ProgressViewHolder(binding)
            }
            else -> {
                val binding = FragmentSharedElementTransitionListCellBinding.inflate(layoutInflater, viewGroup, false)
                binding.txtListPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG

                DataViewHolder(binding, bookItemListener)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is HeaderViewHolder -> {
                val dataItem = currentList[position] as DataItem.Header
                holder.bind(dataItem.totalCount)
            }
            is ProgressViewHolder -> {

            }
            is DataViewHolder -> {
                val dataItem = currentList[position] as DataItem.Data
                holder.bind(dataItem)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(currentList[position]){
            is DataItem.Header -> VIEW_TYPE_HEADER
            is DataItem.Data -> VIEW_TYPE_DATA
            is DataItem.Progress -> VIEW_TYPE_PROGRESS
        }
    }

    override fun getItemCount(): Int{
        return currentList.size
    }

    override fun getItemId(position: Int): Long {
        return currentList[position].itemId
    }

    fun setData(listBookItem: List<BookItem>, meta: BookItems.Meta?, commitCallback: (() -> Unit)? = null){
        val mListBookItem: ArrayList<DataItem> = ArrayList()

        //헤더 추가
        if( meta == null ){
            mListBookItem.add(DataItem.Header(0))
        }else{
            mListBookItem.add(DataItem.Header(meta.totalCount))
        }

        //데이터 추가
        mListBookItem.addAll(listBookItem.map { DataItem.Data(it) })

        //프로그레스 추가
        if( meta != null && !meta.isEndPage() ){
            mListBookItem.add(DataItem.Progress)
        }

        //리스트 데이터 추가
        submitList(mListBookItem, commitCallback)
    }

    sealed class DataItem {
        abstract val itemId: Long

        data class Header(val totalCount: Int = 0): DataItem() {
            override val itemId = Long.MIN_VALUE
        }

        data class Data(val bookItem: BookItem): DataItem() {
            override val itemId = bookItem.itemId
        }

        object Progress: DataItem() {
            override val itemId = Long.MAX_VALUE
        }
    }

    class HeaderViewHolder(private val binding: FragmentSharedElementTransitionListCellHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(searchResultCount: Int) {
            binding.txtSearchResultCount.text = String.format("검색결과: %d건", searchResultCount)
        }
    }

    inner class DataViewHolder(
        private val binding: FragmentSharedElementTransitionListCellBinding,
        private val bookItemListener: BookItemListener
    ) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        init {
            binding.root.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val dataItem = currentList[bindingAdapterPosition]
            if( dataItem is DataItem.Data ){
                //리턴 트랜지션을 위해 클릭한 도서 id 기억해두기
                lastSelectedId = dataItem.bookItem.id

                bookItemListener.onBookItemClick(dataItem.bookItem, binding.imgThumbnail, binding.txtTitle)
            }
        }

        fun bind(dataItem: DataItem.Data) {
            binding.bookItem = dataItem.bookItem

            binding.apply {
                val bookItem = dataItem.bookItem

                val ctx = binding.root.context

                Glide
                    .with(ctx)
                    .load(bookItem.imageLinks?.thumbnail)
                    .placeholder(R.color.lightGray)
//                .error(R.drawable.ic_launcher)
//                .fallback(R.color.lightGray)
                    .addListener(object: RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            //이미지 로드 완료시 대기중인 히어로애니메이션 시작
                            if (bookItem.id == lastSelectedId) {
                                onReadyToTransition()
                                lastSelectedId = null
                            }

                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            //이미지 로드 완료시 대기중인 히어로애니메이션 시작
                            Log.d(this.javaClass.simpleName, "onResourceReady: ${bookItem.id} ${lastSelectedId} ${bookItem.id == lastSelectedId}")
                            if (bookItem.id == lastSelectedId) {
                                onReadyToTransition()
                                lastSelectedId = null
                            }
                            return false
                        }

                    })
                    .into(imgThumbnail)

                //sharedElementTransition에 사용 할 트랜지션 네임 각 뷰에 할당
                ViewCompat.setTransitionName(binding.imgThumbnail, "thumbnail_transition_${bookItem.id}")
                ViewCompat.setTransitionName(binding.txtTitle, "title_transition_${bookItem.id}")
            }
        }
    }

    class ProgressViewHolder(private val binding: FragmentSharedElementTransitionListCellProgressBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    class DiffUtilCallback : DiffUtil.ItemCallback<DataItem>() {
        override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem) =
            oldItem.itemId == newItem.itemId

        override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem) =
            oldItem == newItem
    }

    companion object{
        private const val VIEW_TYPE_HEADER = 1001
        private const val VIEW_TYPE_DATA = 1002
        private const val VIEW_TYPE_PROGRESS = 1003
    }
}