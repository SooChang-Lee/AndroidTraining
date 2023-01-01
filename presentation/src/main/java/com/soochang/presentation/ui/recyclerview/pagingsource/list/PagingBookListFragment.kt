package com.soochang.presentation.ui.recyclerview.pagingsource.list

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.soochang.domain.model.book.BookItem
import com.soochang.presentation.R
import com.soochang.presentation.ui.base.BaseActivity
import com.soochang.presentation.ui.base.BaseFragment
import com.soochang.presentation.databinding.FragmentPagingBookListBinding
import com.soochang.presentation.util.CommonUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PagingBookListFragment : BaseFragment<FragmentPagingBookListBinding>(FragmentPagingBookListBinding::inflate), PagingBookListAdapter.BookItemListener{
    private val viewModel: PagingBookListViewModel by viewModels()

    lateinit var adapter: PagingBookListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()

        setupObserver()

        monitorLoadState()
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
        if( !::adapter.isInitialized ){//BookItemListener
            adapter = PagingBookListAdapter(this)
        }

        //DiffUtil Callback 사용시 setHasStableIds적용하면 데이터 로드시마다 깜빡이는 문제 해소됨
        if (!adapter.hasObservers()) {
            adapter.setHasStableIds(true)
        }

        binding.recyclerView.adapter = adapter.withLoadStateFooter(
            PagingLoadStateAdapter { /*adapter.retry()*/ }
        )
//        binding.recyclerView.setHasFixedSize(true)

        //키보드 검색버튼 리스너
        binding.editSearchText.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                actionSearch(v.text.toString())

                //키보드 숨기고 포커스 뺏기
                (requireActivity() as BaseActivity<*>).hideKeyboard()
                v.clearFocus()

                return@OnEditorActionListener true
            }
            false
        })
    }

    private fun actionSearch(query: String) {
        viewLifecycleOwner.lifecycleScope.launch {

            viewModel.onActionSearch(query)
        }
    }

    private fun setupObserver(){
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                //UI상태 제어
                launch {
                    viewModel.uiState.collectLatest { uiState ->
                        Log.d(TAG, "observeViewmodel: uiState ${uiState}")

                        //페이지상태 제어
                        binding.llInitialPage.isVisible = uiState.showInitialPage
                        binding.llNoDataPage.isVisible = uiState.showNoDataPage
                        binding.llProgressPage.isVisible = uiState.showProgress
                    }
                }

                //데이터 받기
                launch {
                    lifecycleScope.launchWhenStarted {
                        viewModel.pagingData.collectLatest { pagingData ->
                            adapter.submitData(pagingData)
                        }
                    }
                }

                //이벤트 수신
                launch {
                    viewModel.eventFlow.collectLatest { event ->
                        when(event) {
                            //에러발생시 스낵바 노출(뒤로가기 액션 포함)
                            is PagingBookListViewModel.UiEvent.ShowErrorSnackbar -> {
                                CommonUtil.showSnackbar(
                                    requireView(),
                                    getString(event.messageId),
                                    Snackbar.LENGTH_INDEFINITE,
                                    getString(R.string.error_snackbar_retry_button_title)
                                ) {
                                    adapter.refresh()
                                }
                            }
                            //키워드 검색시 스크롤 상단으로 이동
                            is PagingBookListViewModel.UiEvent.ScrollToTop -> {
                                binding.recyclerView.scrollToPosition(0)
                            }
                        }
                    }
                }
            }
        }
    }

    //ViewModel에 로드상태 전달 - 로딩완료 여부, 에러발생시 표시 등
    private fun monitorLoadState(){
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { loadStates ->
                    viewModel.onLoadStateChanged(loadStates, adapter.itemCount)
                }
            }
        }
    }

    override fun onBookItemClick(bookItem: BookItem) {
        //화면이동
        val action = PagingBookListFragmentDirections.actionPagingBookListFragmentToGoogleBookDetailFragment(
            bookItem.id
        )
        binding.root.findNavController().navigate(action)
    }
}