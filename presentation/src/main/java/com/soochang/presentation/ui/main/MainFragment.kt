package com.soochang.presentation.ui.main

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.soochang.presentation.ui.base.BaseFragment
import com.soochang.presentation.databinding.FragmentMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.soochang.domain.model.Result
import com.soochang.presentation.R

@AndroidEntryPoint
class MainFragment : BaseFragment<FragmentMainBinding>(FragmentMainBinding::inflate) {
    private val viewModel: MainViewModel by viewModels()

    private val adapter: MainMenuAdapter by lazy { MainMenuAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()

        setupObserver()
    }

    private fun setupUI(){
        //Navigation component 툴바 연동
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        //RecyclerView 세팅
        //LayoutManager
        val linearLayoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = linearLayoutManager

        //Divider 설정
        val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        divider.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.recyclerview_divider_horizontal)!!)
        binding.recyclerView.addItemDecoration(divider)

        //Adapter 설정
        binding.recyclerView.adapter = adapter
//        binding.recyclerView.setHasFixedSize(true)
    }

    private fun setupObserver(){
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mainMenu.collectLatest { result ->
                    when (result) {
                        is Result.Success -> {
                            Log.d(TAG, "setupObserver: ${result}")

                            adapter.asyncListDiffer.submitList(result.data.mainMenuList)
                        }
                        is Result.Error -> {
                            
                        }
                        null -> {
                        
                        }
                    }
                }
            }
        }
    }
}