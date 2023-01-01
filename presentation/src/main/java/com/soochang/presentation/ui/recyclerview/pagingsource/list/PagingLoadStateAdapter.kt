package com.soochang.presentation.ui.recyclerview.pagingsource.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.soochang.presentation.databinding.ItemLoadStateBinding

class PagingLoadStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<PagingLoadStateAdapter.PagingLoadStateViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): PagingLoadStateViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PagingLoadStateViewHolder(ItemLoadStateBinding.inflate(layoutInflater, parent, false), retry)
    }

    override fun onBindViewHolder(holder: PagingLoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    class PagingLoadStateViewHolder(
        private val binding: ItemLoadStateBinding,
        private val retry: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(state: LoadState) {
            //에러는 화면에서 처리하고 마지막셀 로딩중만 표시)
            binding.isLoading = state is LoadState.Loading
        }
    }
}