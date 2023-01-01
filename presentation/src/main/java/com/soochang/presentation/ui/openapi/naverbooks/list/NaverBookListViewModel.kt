package com.soochang.presentation.ui.openapi.naverbooks.list

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.soochang.domain.model.Result
import com.soochang.domain.model.book.BookItem
import com.soochang.domain.model.book.BookItems
import com.soochang.domain.model.error.ErrorEntity
import com.soochang.domain.repository.openapi.OpenApiRepository
import com.soochang.domain.usecase.book.GetBookListByTitleUseCase
import com.soochang.presentation.R
import com.soochang.presentation.ui.base.BaseViewModel
import com.soochang.presentation.ui.openapi.naverbooks.list.model.DataItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NaverBookListViewModel @Inject constructor(
    private val getBookListByTitleUseCase: GetBookListByTitleUseCase
): BaseViewModel() {
    data class UiState(
        val showInitialPage: Boolean = true,
        val showNoDataPage: Boolean = false,
        val showProgress: Boolean = false,

        val query: String = ""
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    data class BookDataResponse(
        val currentPageMeta: BookItems.Meta? = null,
        val listDataItem: List<DataItem> = emptyList()
    )

    private val _bookDataResponse = MutableStateFlow(BookDataResponse())
    val bookDataResponse = _bookDataResponse.asStateFlow()

    sealed class UiEvent {
        data class ShowErrorSnackbar(val messageId: Int): UiEvent()
    }

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    //키보드 액션버튼 이벤트
    fun onActionSearch(query: String){
        //초기화면, 데이터없음 페이지 숨기기
        _uiState.update { currentUiState ->
            currentUiState.copy(
                showInitialPage = false,
                showNoDataPage = false,
                showProgress = true
            )
        }

        searchBookList(query)
    }

    //다음페이지 요청 이벤트
    fun onRequestNextPage(){
        bookDataResponse.value.currentPageMeta ?: return


        //초기화면, 데이터없음 페이지 숨기기
        _uiState.update { currentUiState ->
            currentUiState.copy(
                showInitialPage = false,
                showNoDataPage = false
            )
        }

        searchBookList(uiState.value.query, bookDataResponse.value.currentPageMeta!!.currentPage + 1)
    }

    //도서정보 리퀘스트
    private fun searchBookList(query: String, reqPageNo: Int = 1){
        viewModelScope.launch {
            //프로그레스바 표시
            _uiState.update { currentUiState ->
                currentUiState.copy(
                    query = query
                )
            }

            val result: Result<BookItems> = getBookListByTitleUseCase(OpenApiRepository.BookDataSource.NaverBooks, query, reqPageNo, COUNT_PER_PAGE)

            when(result){
                is Result.Success -> {
                    Log.d(TAG, "searchBookList: ${result}")
                    _uiState.update { currentUiState ->
                        currentUiState.copy(
                            showNoDataPage = result.data.items.isEmpty(),
                            showProgress = false,
                        )
                    }

                    //리스트 아이템 구성
                    val listDataItem: ArrayList<DataItem> = ArrayList()
                    listDataItem.add(DataItem.Header(result.data.meta.totalCount ?: 0))

                    listDataItem.addAll(result.data.items.map { DataItem.Data(it) })

                    if( !result.data.meta.isEndPage() ){
                        listDataItem.add(DataItem.Progress)
                    }

                    _bookDataResponse.update { bookDataResponse ->
                        bookDataResponse.copy(
                            currentPageMeta = result.data.meta,
                            listDataItem = if(reqPageNo == 1){
                                listDataItem
                            }else{
                                bookDataResponse.listDataItem + listDataItem
                            }
                        )
                    }
                }
                is Result.Error -> {
                    _eventFlow.emit(
                        when(result.throwable){
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
        }
    }

    companion object{
        const val COUNT_PER_PAGE: Int = 30
    }
}