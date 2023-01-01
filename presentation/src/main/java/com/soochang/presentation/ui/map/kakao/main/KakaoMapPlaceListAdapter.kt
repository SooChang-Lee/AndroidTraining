package com.soochang.presentation.ui.map.kakao.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.soochang.domain.model.place.PlaceItem
import com.soochang.domain.model.place.PlaceItems
import com.soochang.presentation.databinding.FragmentKakaoMapPlaceCellBinding

class KakaoMapPlaceListAdapter(
    private val placeItemListener: PlaceItemListener
) : ListAdapter<PlaceItem, KakaoMapPlaceListAdapter.PlaceViewHolder>(DiffUtilCallback()) {

    interface PlaceItemListener {
        fun onClick(placeItem: PlaceItem)
        fun onDetailClick(placeItem: PlaceItem)
        fun onNavigateRouteClick(placeItem: PlaceItem)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): PlaceViewHolder {
        val layoutInflater = LayoutInflater.from(viewGroup.context)

        val binding = FragmentKakaoMapPlaceCellBinding.inflate(layoutInflater, viewGroup, false)
        return PlaceViewHolder(binding, placeItemListener)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val dataItem = currentList[position]
        holder.bind(dataItem)
    }

    override fun getItemCount(): Int{
        return currentList.size
    }

//    override fun getItemId(position: Int): Long {
//        return currentList[position].itemId
//    }

    fun setData(placeItems: PlaceItems, commitCallback: (() -> Unit)? = null){
        //리스트 데이터 추가
        submitList(placeItems.listPlace, commitCallback)
    }

    class PlaceViewHolder(
        private val binding: FragmentKakaoMapPlaceCellBinding,
        private val placeItemListener: PlaceItemListener
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(placeItem: PlaceItem) {
            binding.placeItem = placeItem
            binding.placeItemListener = placeItemListener
        }
    }

    class DiffUtilCallback : DiffUtil.ItemCallback<PlaceItem>() {
        override fun areItemsTheSame(oldItem: PlaceItem, newItem: PlaceItem) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: PlaceItem, newItem: PlaceItem) =
            oldItem == newItem
    }

    companion object{

    }
}