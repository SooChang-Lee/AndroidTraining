package com.soochang.presentation.ui.layout.slidingpanelayout.detail

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.soochang.domain.model.book.BookItem
import com.soochang.presentation.R
import com.soochang.presentation.databinding.FragmentSlidingPaneLayoutDetailBinding
import com.soochang.presentation.ui.base.BaseFragment
import com.soochang.presentation.util.CommonUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class SlidingPaneLayoutDetailFragment : BaseFragment<FragmentSlidingPaneLayoutDetailBinding>(
    FragmentSlidingPaneLayoutDetailBinding::inflate) {
    private val viewModel: SlidingPaneLayoutDetailViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()

        setupObserver()

        val id: String? = arguments?.getString("id")
        getDetail(id)
    }

    //화면 세팅
    private fun setupUI() {
        //일반폰 사이즈는 백버튼 강제노출
        val navController = findNavController()

        val windowSizeClass = CommonUtil.getWidthSizeClasses(requireActivity())

        Log.d(TAG, "setupUI: windowSizeClass=$windowSizeClass")

        val appBarConfiguration = when( windowSizeClass ){
            CommonUtil.WindowSizeClass.COMPACT -> {
                //툴바 백버튼 강제 노출시키기
                //아래방법으로 DetailFragment에 툴바 백버튼이 안나오는 문제를 해소 하였지만,
                //지금처럼 DetailFragment가 별도의 navigation graph에 있는경우 백스택 pop을 제대로 하는 방법을 알아봐야함
//                AppBarConfiguration
//                    .Builder()
//                    .setFallbackOnNavigateUpListener {
//                        Log.d(TAG, "setupUI: requireActivity().onBackPressed()")
//                        requireActivity().onBackPressed()
//                        true
//                    }
//                    .build()

                AppBarConfiguration(
                    topLevelDestinationIds = setOf(),
                    fallbackOnNavigateUpListener = {
                        Log.d(TAG, "setupUI: requireActivity().onBackPressed()")
                        requireActivity().onBackPressed()
                        true
                    }
                )
            }
            CommonUtil.WindowSizeClass.MEDIUM,
            CommonUtil.WindowSizeClass.EXPANDED -> {
                //Navigation component 툴바 연동
                AppBarConfiguration(navController.graph)
            }
        }

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    //옵저버 세팅
    private fun setupObserver(){
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                //UI상태 제어
                launch {
                    viewModel.uiState.collectLatest { uiState ->
                        binding.pleaseSelectContainer.isVisible = uiState.showPleaseSelectContainer
                        binding.bookDetailContainer.isVisible = uiState.showBookDetailContainer
                    }
                }

                //도서 상세정보 로드
                launch {
                    viewModel.bookDataResponse.collectLatest { bookDataResponse ->
                        bookDataResponse.bookItem?.let { displayBookItemDetail(it) }
                    }
                }

                //에러 발생시 스낵바 표시(뒤로가기 액션 포함)
                launch {
                    viewModel.eventFlow.collectLatest { event ->
                        when(event) {
                            is SlidingPaneLayoutDetailViewModel.UiEvent.ShowErrorSnackbar -> {
                                CommonUtil.showSnackbar(
                                    requireView(),
                                    getString(event.messageId),
                                    Snackbar.LENGTH_INDEFINITE,
//                            getString(R.string.error_snackbar_back_button_title)
                                    getString(R.string.error_snackbar_retry_button_title)
                                ) {
//                            findNavController().popBackStack()
                                    val id: String? = arguments?.getString("id")
                                    getDetail(id)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //도서 상세정보 조회
    private fun getDetail(id: String?) {
        viewModel.getBookDetail(id)
    }

    private fun displayBookItemDetail(bookItem: BookItem) {
        binding.apply {
            val ctx = root.context

            Glide
                .with(imgThumbnail)
                .load(bookItem.imageLinks?.cover)
                .apply(RequestOptions.centerInsideTransform())
                .into(imgThumbnail)

            txtTitle.text = bookItem.title

            txtPublisher.text = bookItem.publisher

            txtAuthors.text = bookItem.authors?.joinToString(", ")

            rlPriceContainer.isVisible = bookItem.saleStatus == "FOR_SALE"

            txtRetailPrice.text = ctx.getString(R.string.thousand_comma_price, bookItem.retailPrice)

            txtListPrice.text = "(${ctx.getString(R.string.thousand_comma_price, bookItem.listPrice)})"

            txtContents.text = HtmlCompat.fromHtml(bookItem.description ?: "", HtmlCompat.FROM_HTML_MODE_LEGACY)

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
        }
    }
}