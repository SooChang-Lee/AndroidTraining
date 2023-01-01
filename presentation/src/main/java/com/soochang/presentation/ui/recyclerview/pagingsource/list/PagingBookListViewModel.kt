package com.soochang.presentation.ui.recyclerview.pagingsource.list

import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.soochang.domain.model.book.BookItem
import com.soochang.domain.model.error.ErrorEntity
import com.soochang.domain.repository.openapi.OpenApiRepository
import com.soochang.presentation.R
import com.soochang.presentation.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PagingBookListViewModel @Inject constructor(
    private val openApiRepository: OpenApiRepository

): BaseViewModel() {
    data class UiState(
        val showInitialPage: Boolean = true,
        val showNoDataPage: Boolean = false,
        val showProgress: Boolean = false,

        val query: String = ""
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val _pagingData = MutableStateFlow<PagingData<BookItem>>(PagingData.empty())
    val pagingData = _pagingData.asStateFlow()

    sealed class UiEvent {
        data class ShowErrorSnackbar(val messageId: Int): UiEvent()
        object ScrollToTop: UiEvent()
    }

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    //키보드 액션버튼 이벤트
    fun onActionSearch(query: String){
        //초기화면, 데이터없음 페이지 숨기기
        _uiState.update { currentUiState ->
            currentUiState.copy(
                query = query,
                showInitialPage = false
            )
        }

        searchBookList(query)
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
            findBooksByTitlePagingSource(query, COUNT_PER_PAGE)
                .cachedIn(viewModelScope)
                .collectLatest { pagingData ->
                    _pagingData.value = pagingData
                }
        }
    }

    //adapter 로딩상태에 맞는 UI상태 및 에러처리
    suspend fun onLoadStateChanged(loadStates: CombinedLoadStates, itemCount: Int){
        Log.d(TAG, "${loadStates.refresh.javaClass.simpleName} ${loadStates.append.javaClass.simpleName} ${loadStates.prepend.javaClass.simpleName}")

        _uiState.update { currentUiState ->
            currentUiState.copy(
                showNoDataPage = loadStates.refresh is LoadState.NotLoading && itemCount == 0,//검색결과 0건일때 검색어가 없어요 화면 표시
                showProgress = loadStates.refresh is LoadState.Loading//키워드 검색시 프로그레스바 포시
            )
        }

        //키워드 검색시 RecyclerView 스크롤 상단으로 이동
        if( loadStates.refresh is LoadState.Loading ){
            _eventFlow.emit(UiEvent.ScrollToTop)
        }

        //에러 발생시 스낵바 처리
        val errorState = if( loadStates.refresh is LoadState.Error ){
            loadStates.refresh as LoadState.Error
        }else if( loadStates.append is LoadState.Error ){
            loadStates.append as LoadState.Error
        }else{
            null
        }

        if( errorState != null ) {
            //정의된 에러상태에 맞는 메세지 가져오기
            _eventFlow.emit(
                when(errorState.error){
                    is ErrorEntity.Api.Network -> {
                        UiEvent.ShowErrorSnackbar(messageId = R.string.error_msg_network_unavailable)
                    }

                    is ErrorEntity.Api.ServiceUnvailable -> {
                        UiEvent.ShowErrorSnackbar(messageId = R.string.error_msg_service_unavailable)
                    }
                    is ErrorEntity.Api.UnknownError -> {
                        UiEvent.ShowErrorSnackbar(messageId = R.string.error_msg_unknown)
                    }
                    else -> UiEvent.ShowErrorSnackbar(messageId = R.string.error_msg_unknown)
                }
            )
        }
    }

    companion object{
        const val COUNT_PER_PAGE: Int = 30
    }
}