package com.soochang.presentation.ui.heroanimation.detail

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.transition.TransitionInflater
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.material.snackbar.Snackbar
import com.soochang.domain.model.book.BookItem
import com.soochang.presentation.R
import com.soochang.presentation.ui.base.BaseFragment
import com.soochang.presentation.databinding.FragmentSharedElementTransitionDetailBinding
import com.soochang.presentation.util.CommonUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SharedElementTransitionDetailFragment : BaseFragment<FragmentSharedElementTransitionDetailBinding>(FragmentSharedElementTransitionDetailBinding::inflate) {
    private val viewModel: SharedElementTransitionDetailViewModel by viewModels()

    private val args: SharedElementTransitionDetailFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate: args.volumeId ${args.id}")

        sharedElementEnterTransition = TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()

        setupObserver()

        getDetail(args.id)
    }

    //화면 세팅
    private fun setupUI() {
        //Navigation component 툴바 연동
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        //sharedElementTransition에 사용 할 트랜지션 네임 각 뷰에 할당
        ViewCompat.setTransitionName(binding.imgThumbnail, "thumbnail_transition")
        ViewCompat.setTransitionName(binding.txtTitle, "title_transition")

        //데이터 로드전까지 히어로 애니메이션 대기
        postponeEnterTransition()
    }

    //옵저버 세팅
    private fun setupObserver(){
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
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
                            is SharedElementTransitionDetailViewModel.UiEvent.ShowErrorSnackbar -> {
                                CommonUtil.showSnackbar(
                                    requireView(),
                                    getString(event.messageId),
                                    Snackbar.LENGTH_INDEFINITE,
//                            getString(R.string.error_snackbar_back_button_title)
                                    getString(R.string.error_snackbar_retry_button_title)
                                ) {
//                            findNavController().popBackStack()
                                    getDetail(args.id)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //도서 상세정보 조회
    private fun getDetail(id: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getBookDetail(id)
            }
        }
    }

    private fun displayBookItemDetail(bookItem: BookItem) {
        binding.apply {
            val ctx = root.context

            Glide
                .with(imgThumbnail)
                .load(bookItem.imageLinks?.cover)
                .addListener(object: RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        //이미지 로드 완료시 대기중인 히어로애니메이션 시작
                        startPostponedEnterTransition()
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
                        startPostponedEnterTransition()
                        return false
                    }

                })
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

            bookDetailContainer.visibility = View.VISIBLE
        }
    }
}