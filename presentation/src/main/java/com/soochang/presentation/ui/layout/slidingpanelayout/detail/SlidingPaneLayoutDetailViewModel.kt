package com.soochang.presentation.ui.layout.slidingpanelayout.detail

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.soochang.domain.model.book.BookItem
import com.soochang.domain.usecase.book.*
import com.soochang.presentation.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.soochang.domain.model.Result
import com.soochang.domain.model.error.ErrorEntity
import com.soochang.domain.repository.openapi.OpenApiRepository
import com.soochang.presentation.R
import kotlinx.coroutines.flow.*

@HiltViewModel
class SlidingPaneLayoutDetailViewModel @Inject constructor(
    private val getBookDetailUseCase: GetBookDetailUseCase
): BaseViewModel() {
    data class UiState(
        val showPleaseSelectContainer: Boolean = false,
        val showBookDetailContainer: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    data class BookDataResponse(
        val bookItem: BookItem? = null
    )

    private val _bookDataResponse = MutableStateFlow(BookDataResponse())
    val bookDataResponse = _bookDataResponse.asStateFlow()

    sealed class UiEvent {
        data class ShowErrorSnackbar(val messageId: Int): UiEvent()
    }

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun getBookDetail(id: String?) {
        if( id == null ){
            _uiState.value = UiState(
                showPleaseSelectContainer = true,
                showBookDetailContainer = false
            )
            return
        }

        viewModelScope.launch {
            val result: Result<BookItem> = getBookDetailUseCase(OpenApiRepository.BookDataSource.GoogleBooks, id)

            when(result){
                is Result.Success -> {
                    Log.d(TAG, "searchBookList: ${result}")

                    _uiState.value = UiState(
                        showPleaseSelectContainer = false,
                        showBookDetailContainer = true
                    )

                    _bookDataResponse.update { bookDataResponse ->
                        bookDataResponse.copy(
                            bookItem = result.data
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
}