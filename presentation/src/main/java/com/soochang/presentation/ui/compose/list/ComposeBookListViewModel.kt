package com.soochang.presentation.ui.compose.list

import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.soochang.domain.model.book.BookItem
import com.soochang.domain.model.error.ErrorEntity
import com.soochang.domain.repository.openapi.OpenApiRepository
import com.soochang.presentation.R
import com.soochang.presentation.ui.base.BaseMVIViewModel
import com.soochang.presentation.ui.recyclerview.pagingsource.list.GoogleBooksPagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ComposeBookListViewModel @Inject constructor(
    private val openApiRepository: OpenApiRepository
): BaseMVIViewModel<ComposeBookListContract.Event, ComposeBookListContract.State, ComposeBookListContract.Effect>() {

    //초기 State설정
    override fun createInitialState(): ComposeBookListContract.State {
        return ComposeBookListContract.State(
            showInitialPage = true,
            showNoDataPage = false,
            showProgress = false,

            bookListLoadState = ComposeBookListContract.State.LoadState.Idle,

            query = ""
        )
    }

    private val _pagingData = MutableStateFlow<PagingData<ListItemViewType<BookItem>>>(PagingData.empty())
    val pagingData = _pagingData.asStateFlow()

    //Event발생(SharedFlow)
    override fun handleEvent(event: ComposeBookListContract.Event) {
        when (event) {
            ComposeBookListContract.Event.OnBackPressed -> {
                setEffect { ComposeBookListContract.Effect.PopBackStack }
            }
            is ComposeBookListContract.Event.OnBookClicked -> {
                setEffect { ComposeBookListContract.Effect.NavigateBookDetailScreen(event.id) }
            }
            is ComposeBookListContract.Event.OnSearchAction -> {
                //초기화면, 데이터없음 페이지 숨기기
                setState {
                    copy(
                        showInitialPage = false,
                        bookListLoadState = ComposeBookListContract.State.LoadState.Loading,
                        query = event.query
                    )
                }

                // 검색
                searchBookList(event.query)
            }
            is ComposeBookListContract.Event.OnLoadStateChanged -> {
                onLoadStateChanged(event.loadStates, event.itemCount)
            }
        }
    }

    fun findBooksByTitlePagingSource(
        query: String,
        countPerPage: Int
    ): Flow<PagingData<BookItem>> {
        return Pager(
            PagingConfig(pageSize = countPerPage)
        ) {
            GoogleBooksPagingSource(openApiRepository, query, countPerPage)
        }.flow
//            .cachedIn(viewModelScope)
    }

    //도서정보 리퀘스트
    private fun searchBookList(query: String){
        viewModelScope.launch {
            findBooksByTitlePagingSource(query, COUNT_PER_PAGE).map { pagingData ->
                pagingData.map { bookItem ->
                    ListItemViewType.Data(bookItem)
                }.insertSeparators { before, after ->
                    when {
                        before == null -> null
                        after == null -> null
                        else -> ListItemViewType.Separator("Separator: $before-$after")
                    }
                }
            }.cachedIn(viewModelScope)
                .collectLatest {pagingData ->
                    setState {
                        copy(
                            bookListLoadState = ComposeBookListContract.State.LoadState.Idle
                        )
                    }

                    _pagingData.value = pagingData
                }
        }
    }

    //로딩상태에 맞는 UI상태정의 및 에러처리(Paging 3 LoadState)
    private fun onLoadStateChanged(loadStates: CombinedLoadStates, itemCount: Int){
        Log.d(TAG, "${loadStates.refresh.javaClass.simpleName} ${loadStates.append.javaClass.simpleName} ${loadStates.prepend.javaClass.simpleName}")

        setState {
            copy(
                showNoDataPage = loadStates.refresh is LoadState.NotLoading && itemCount == 0,//검색결과 0건일때 검색어가 없어요 화면 표시
                showProgress = loadStates.refresh is LoadState.Loading//최초 키워드 검색시 프로그레스바 포시
            )
        }

        //키워드 검색시 RecyclerView 스크롤 상단으로 이동
        if( loadStates.refresh is LoadState.Loading ){
            setEffect { ComposeBookListContract.Effect.ScrollToTop }
        }

        //에러 발생시 스낵바 처리
        val loadStateError = if( loadStates.refresh is LoadState.Error ){
            loadStates.refresh as LoadState.Error
        }else if( loadStates.append is LoadState.Error ){
            loadStates.append as LoadState.Error
        }else{
            null
        }

        if( loadStateError != null ) {
            setState {
                copy(
                    bookListLoadState = ComposeBookListContract.State.LoadState.Error,
                )
            }

            //정의된 에러상태에 맞는 스낵바 이펙트 발생시키기
            setEffect {
                when(loadStateError.error){
                    is ErrorEntity.Api.Network -> {
                        Log.e(TAG, "onLoadStateChanged: ErrorEntity.Api.Network", )

                        ComposeBookListContract.Effect.ShowErrorSnackbar(messageId = R.string.error_msg_network_unavailable)
                    }

                    is ErrorEntity.Api.ServiceUnvailable -> {
                        Log.e(TAG, "onLoadStateChanged: ErrorEntity.Api.ServiceUnvailable", )

                        ComposeBookListContract.Effect.ShowErrorSnackbar(messageId = R.string.error_msg_service_unavailable)
                    }
                    is ErrorEntity.Api.UnknownError -> {
                        Log.e(TAG, "onLoadStateChanged: ErrorEntity.Api.UnknownError", )

                        ComposeBookListContract.Effect.ShowErrorSnackbar(messageId = R.string.error_msg_unknown)
                    }
                    else -> {
                        Log.e(TAG, "onLoadStateChanged: ErrorEntity.Api.UnknownError", )

                        ComposeBookListContract.Effect.ShowErrorSnackbar(messageId = R.string.error_msg_unknown)
                    }
                }

            }
        }
    }

    companion object{
        const val COUNT_PER_PAGE: Int = 30
    }
}