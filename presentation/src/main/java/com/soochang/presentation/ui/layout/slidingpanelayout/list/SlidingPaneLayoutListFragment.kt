package com.soochang.presentation.ui.layout.slidingpanelayout.list

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.AbstractListDetailFragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import com.google.android.material.snackbar.Snackbar
import com.soochang.domain.model.book.BookItem
import com.soochang.presentation.R
import com.soochang.presentation.databinding.FragmentSlidingPaneLayoutListBinding
import com.soochang.presentation.ui.base.BaseActivity
import com.soochang.presentation.ui.layout.slidingpanelayout.list.model.UIModel
import com.soochang.presentation.util.CommonUtil
import com.soochang.presentation.util.pagination.PaginationScrollListener
import com.soochang.presentation.util.pagination.OnPagingEventListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SlidingPaneLayoutListFragment : AbstractListDetailFragment(), OnPagingEventListener, SlidingPaneLayoutListAdapter.BookItemListener {

    private val viewModel: SlidingPaneLayoutListViewModel by viewModels()

    lateinit var adapter: SlidingPaneLayoutListAdapter

    lateinit var paginationScrollListener: PaginationScrollListener

    private var _binding: FragmentSlidingPaneLayoutListBinding? = null
    val binding get() = _binding!!

    override fun onCreateListPaneView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSlidingPaneLayoutListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onCreateDetailPaneNavHostFragment(): NavHostFragment {
        return NavHostFragment.create(R.navigation.nav_graph_sliding_pane_detail_layout)
    }

    override fun onListPaneViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onListPaneViewCreated(view, savedInstanceState)

        setupUI()

        setupObserver()
    }

    private fun openDetails(id: String) {
        Log.d(this.javaClass.simpleName, "openDetails: $id")

        val bundle = Bundle()
        bundle.putString("id", id)

        val detailNavController = detailPaneNavHostFragment.navController
        detailNavController.navigate(
            R.id.slidingPaneLayoutDetailFragment,
            bundle,
            NavOptions.Builder()
                .setPopUpTo(detailNavController.graph.startDestinationId, true)
                .apply {
                    if (slidingPaneLayout.isOpen) {
                        setEnterAnim(androidx.navigation.ui.R.anim.nav_default_enter_anim)
                        setExitAnim(androidx.navigation.ui.R.anim.nav_default_exit_anim)
                    }
                }
                .build()
        )
        slidingPaneLayout.open()
    }

    private fun setupUI(){
        //Navigation component 툴바 연동
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        //스와이프로 디테일화면 열고/닫기 여부
        slidingPaneLayout.lockMode = SlidingPaneLayout.LOCK_MODE_LOCKED_CLOSED

        if( slidingPaneLayout.isOpen ){
            Log.d(this.javaClass.simpleName, "setupUI: slidingPaneLayout.close()")
            slidingPaneLayout.close()
        }

        // Connect the SlidingPaneLayout to the system back button.
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            TwoPaneOnBackPressedCallback(slidingPaneLayout)
        )

        //RecyclerView 세팅
        //LayoutManager
        val linearLayoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = linearLayoutManager

        //Divider 설정
        val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        divider.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.recyclerview_divider_horizontal)!!)
        binding.recyclerView.addItemDecoration(divider)

        //Adapter 설정
        if( !::adapter.isInitialized ){
            adapter = SlidingPaneLayoutListAdapter(this)
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
                        Log.d(this.javaClass.simpleName, "observeViewmodel: uiState ${uiState}")

                        //페이지상태 제어
                        binding.llInitialPage.isVisible = uiState.showInitialPage
                        binding.llNoDataPage.isVisible = uiState.showNoDataPage
                        binding.llProgressPage.isVisible = uiState.showProgress
                    }
                }

                //데이터 받기
                launch {
                    viewModel.bookDataResponse.collectLatest { bookDataResponse ->
                        Log.d(this.javaClass.simpleName, "observeViewmodel: listBookItem.size = ${bookDataResponse.listBookItem.size} bookDataResponse currentPageMeta = ${bookDataResponse.currentPageMeta} getTotalPage() = ${bookDataResponse.currentPageMeta?.getTotalPage()} isEndPage() = ${bookDataResponse.currentPageMeta?.isEndPage()}")

                        val listDataItem: ArrayList<UIModel> = ArrayList()
                        listDataItem.add(UIModel.Header(bookDataResponse.currentPageMeta?.totalCount ?: 0))

                        listDataItem.addAll(bookDataResponse.listBookItem.map { UIModel.Data(it) })

                        if(bookDataResponse.currentPageMeta?.isEndPage() != true){
                            listDataItem.add(UIModel.Progress)
                        }

                        val recyclerViewState = binding.recyclerView.layoutManager?.onSaveInstanceState()

                        adapter.setData(
                            listBookItem = listDataItem,
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

                        paginationScrollListener.setLoadFinished(bookDataResponse.currentPageMeta?.isEndPage() ?: false)
                    }
                }

                //에러 발생시 스낵바 표시(뒤로가기 액션 포함)
                launch {
                    viewModel.eventFlow.collectLatest { event ->
                        when(event) {
                            is SlidingPaneLayoutListViewModel.UiEvent.ShowErrorSnackbar -> {
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
        Log.d(this.javaClass.simpleName, "onNeedNextPage: ")

        viewModel.onRequestNextPage()
    }

    inner class TwoPaneOnBackPressedCallback(
        private val slidingPaneLayout: SlidingPaneLayout
    ) : OnBackPressedCallback(
        // Set the default 'enabled' state to true only if it is slidable (i.e., the panes
        // are overlapping) and open (i.e., the detail pane is visible).
        slidingPaneLayout.isSlideable && slidingPaneLayout.isOpen
    ), SlidingPaneLayout.PanelSlideListener {

        init {
            slidingPaneLayout.addPanelSlideListener(this)
        }

        override fun handleOnBackPressed() {
            // Return to the list pane when the system back button is pressed.
            slidingPaneLayout.closePane()
        }

        override fun onPanelSlide(panel: View, slideOffset: Float) {

        }

        override fun onPanelOpened(panel: View) {
            // Intercept the system back button when the detail pane becomes visible.
            isEnabled = true
        }

        override fun onPanelClosed(panel: View) {
            // Disable intercepting the system back button when the user returns to the
            // list pane.
            isEnabled = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }

    override fun onBookItemClick(bookItem: BookItem) {
        openDetails(bookItem.id)
    }
}