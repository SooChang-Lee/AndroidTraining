package com.soochang.presentation.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.soochang.domain.model.main.MainMenu
import com.soochang.presentation.databinding.FragmentMainGroupBinding
import com.soochang.presentation.databinding.FragmentMainHeaderBinding
import com.soochang.presentation.databinding.FragmentMainMenuBinding

class MainMenuAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val asyncListDiffer = AsyncListDiffer(this, DiffUtilCallback())

    class HeaderViewHolder(private val binding: FragmentMainHeaderBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    class GroupViewHolder(private val binding: FragmentMainGroupBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(menuItem: MainMenu.MenuItem) {
            binding.txtTitle.text = menuItem.title
        }
    }

    class MenuViewHolder(private val binding: FragmentMainMenuBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(menuItem: MainMenu.MenuItem) {
            binding.txtTitle.text = menuItem.title
            binding.txtSubtitle.text = "${menuItem.subTitle} (${menuItem.id})"

            binding.root.setOnClickListener {
                //화면이동
                when(menuItem.id){
                     "ui_1" -> {
                        val action = MainFragmentDirections.actionMainFragmentToSharedElementTransitionListFragment()
                        binding.root.findNavController().navigate(action)
                    }
                    "ui_2" -> {
                        val action = MainFragmentDirections.actionMainFragmentToSlidingPaneLayoutListFragment()
                        binding.root.findNavController().navigate(action)
                    }

                    "list_1" -> {
                        val action = MainFragmentDirections.actionMainFragmentToGeneralBookListFragment()
                        binding.root.findNavController().navigate(action)
                    }
                    "list_2" -> {
                        val action = MainFragmentDirections.actionMainFragmentToAsyncListDifferBookListFragment()
                        binding.root.findNavController().navigate(action)
                    }
                    "list_3" -> {
                        val action = MainFragmentDirections.actionMainFragmentToListAdapterBookListFragment()
                        binding.root.findNavController().navigate(action)
                    }
                    "list_4" -> {
                        val action = MainFragmentDirections.actionMainFragmentToPagingBookListFragment()
                        binding.root.findNavController().navigate(action)
                    }
                    "list_5" -> {
                        val action = MainFragmentDirections.actionMainFragmentToComposeBookListFragment()
                        binding.root.findNavController().navigate(action)
                    }

                    "openapi_1" -> {
                        val action = MainFragmentDirections.actionMainFragmentToKakaoBookListFragment()
                        binding.root.findNavController().navigate(action)
                    }
                    "openapi_2" -> {
                        val action = MainFragmentDirections.actionMainFragmentToNaverBookListFragment()
                        binding.root.findNavController().navigate(action)
                    }
                    "openapi_3" -> {
                        val action = MainFragmentDirections.actionMainFragmentToGoogleBookListFragment()
                        binding.root.findNavController().navigate(action)
                    }
                    "map_2" -> {
                        val action = MainFragmentDirections.actionMainFragmentToKakaoMapFragment()
                        binding.root.findNavController().navigate(action)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(viewGroup.context)

        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = FragmentMainHeaderBinding.inflate(layoutInflater, viewGroup, false)
                HeaderViewHolder(binding)
            }
            VIEW_TYPE_GROUP -> {
                val binding = FragmentMainGroupBinding.inflate(layoutInflater, viewGroup, false)
                GroupViewHolder(binding)
            }
            else -> {
                val binding = FragmentMainMenuBinding.inflate(layoutInflater, viewGroup, false)
                MenuViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is HeaderViewHolder -> {

            }
            is GroupViewHolder -> {
                holder.bind(
                    getItem(position)
                )
            }
            is MenuViewHolder -> {
                holder.bind(
                    getItem(position)
                )
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when{
            position == 0 -> VIEW_TYPE_HEADER
            getItem(position).type == "group" -> VIEW_TYPE_GROUP
            else -> VIEW_TYPE_MENU
        }
    }

    override fun getItemCount(): Int{
        return asyncListDiffer.currentList.size + 1
    }

    fun getItem(position: Int): MainMenu.MenuItem = asyncListDiffer.currentList[position-1]

    class DiffUtilCallback : DiffUtil.ItemCallback<MainMenu.MenuItem>() {
        override fun areItemsTheSame(oldItem: MainMenu.MenuItem, newItem: MainMenu.MenuItem) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: MainMenu.MenuItem, newItem: MainMenu.MenuItem) =
            oldItem == newItem
    }

    companion object{
        val VIEW_TYPE_HEADER = 1001
        val VIEW_TYPE_GROUP = 1002
        val VIEW_TYPE_MENU = 1003
    }
}