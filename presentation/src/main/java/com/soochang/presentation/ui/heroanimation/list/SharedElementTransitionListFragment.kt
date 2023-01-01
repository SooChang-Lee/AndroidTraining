package com.soochang.presentation.ui.heroanimation.list

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.soochang.domain.model.book.BookItem
import com.soochang.presentation.R
import com.soochang.presentation.databinding.FragmentSharedElementTransitionListBinding
import com.soochang.presentation.ui.base.BaseActivity
import com.soochang.presentation.ui.base.BaseFragment
import com.soochang.presentation.util.CommonUtil
import com.soochang.presentation.util.pagination.PaginationScrollListener
import com.soochang.presentation.util.pagination.OnPagingEventListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SharedElementTransitionListFragment : BaseFragment<FragmentSharedElementTransitionListBinding>(
    FragmentSharedElementTransitionListBinding::inflate),
    OnPagingEventListener, SharedElementTransitionListAdapter.BookItemListener {
    private val viewModel: SharedElementTransitionListViewModel by viewModels()

    lateinit var adapter: SharedElementTransitionListAdapter

    lateinit var paginationScrollListener: PaginationScrollListener

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
        Log.d(TAG, "setupUI: ::adapter.isInitialized ${::adapter.isInitialized}")
        if( !::adapter.isInitialized ){
            adapter = SharedElementTransitionListAdapter(
                bookItemListener = this,
                onReadyToTransition = {
                    //디테일 화면에서 리스트화면으로 돌아왔을떄 이미지로딩이 완료되면 정지된 애니메이션 시작시켜 주기
                    startPostponedEnterTransition()
                }
            )
        }

        //Detail Fragment에서 돌아온 경우, 클릭한 도서 이미지 로딩이 완료될때까지 트랜지션 대기
        if (adapter.expectsTransition) {
            postponeEnterTransition()
        }

        //DiffUtil Callback 사용시 setHasStableIds적용하면 데이터 로드시마다 깜빡이는 문제 해소됨
        if (!adapter.hasObservers()) {
            adapter.setHasStableIds(true)
        }

        binding.recyclerView.adapter = adapter
        binding.recyclerView.setHasFixedSize(true)

        //커스텀 ScrollListener추가(다음페이지 로드시점에 이벤트 발생해주기)
        paginationScrollListener = PaginationScrollListener(this, linearLayoutManager)
        binding.recyclerView.addOnScrollListener(paginationScrollListener)

        //키보드 검색버튼 리스너
        binding.editSearchText.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.onActionSearch(v.text.toString())

                //키보드 숨기고 포커스 뺏기
                (requireActivity() as BaseActivity<*>).hideKeyboard()
                v.clearFocus()

                return@OnEditorActionListener true
            }
            false
        })
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
                    viewModel.bookDataResponse.collectLatest { bookDataResponse ->
                        Log.d(TAG, "observeViewmodel: listBookItem.size = ${bookDataResponse.listBookItem.size} bookDataResponse currentPageMeta = ${bookDataResponse.currentPageMeta} getTotalPage() = ${bookDataResponse.currentPageMeta?.getTotalPage()} isEndPage() = ${bookDataResponse.currentPageMeta?.isEndPage()}")

                        val recyclerViewState = binding.recyclerView.layoutManager?.onSaveInstanceState()

                        adapter.setData(
                            listBookItem = bookDataResponse.listBookItem,
                            meta = bookDataResponse.currentPageMeta,
                            commitCallback = {
                                if((bookDataResponse.currentPageMeta?.currentPage ?: 1) == 1){
                                    binding.recyclerView.scrollToPosition(0)
                                    paginationScrollListener.setLoadFinished(bookDataResponse.currentPageMeta?.isEndPage() ?: false)
                                }else{
                                    //DiffUtil Callback 적용시 데이터 로드될때마다 스크롤 포지션이 바뀌는 문제가 있어, 데이터 로드 후 스크롤 포지션 복원
                                    binding.recyclerView.layoutManager?.onRestoreInstanceState(recyclerViewState)
                                    paginationScrollListener.setLoadFinished(bookDataResponse.currentPageMeta?.isEndPage() ?: false)
                                }
                            }
                        )
                    }
                }

                //에러 발생시 스낵바 표시(뒤로가기 액션 포함)
                launch {
                    viewModel.eventFlow.collectLatest { event ->
                        when(event) {
                            is SharedElementTransitionListViewModel.UiEvent.ShowErrorSnackbar -> {
                                CommonUtil.showSnackbar(
                                    requireView(),
                                    getString(event.messageId),
                                    Snackbar.LENGTH_INDEFINITE,
                                    getString(R.string.error_snackbar_retry_button_title)
                                ) {
                                    if( viewModel.bookDataResponse.value.currentPageMeta?.currentPage == null ){
                                        viewModel.onActionSearch(binding.editSearchText.text.toString())
                                    }else{
                                        onNeedNextPage()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNeedNextPage() {
        Log.d(TAG, "onNeedNextPage: ")

        viewModel.onRequestNextPage()
    }

    override fun onBookItemClick(bookItem: BookItem, imgThumbnail: ImageView, txtTitle: TextView) {
        //히어로 애니메이션 적용 할 뷰 설정
        val extras = FragmentNavigatorExtras(
            imgThumbnail to "thumbnail_transition",
            txtTitle to "title_transition",
        )

        //화면이동
        val action = SharedElementTransitionListFragmentDirections.actionSharedElementTransitionListFragmentToSharedElementTransitionDetailFragment(
            bookItem.id
        )
        binding.root.findNavController().navigate(action, extras)    }
}