package com.soochang.presentation.ui.compose.list

import androidx.paging.CombinedLoadStates
import androidx.paging.PagingData
import com.soochang.presentation.ui.base.UiEffect
import com.soochang.presentation.ui.base.UiEvent
import com.soochang.presentation.ui.base.UiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class ComposeBookListContract {
    data class State(
        val showInitialPage: Boolean,
        val showNoDataPage: Boolean,
        val showProgress: Boolean,

        val bookListLoadState: LoadState,

        val query: String
    ): UiState{
        enum class LoadState {
            Loading, Idle, Error
        }
    }

    sealed class Event: UiEvent {
        class OnSearchAction(val query: String): Event()
        class OnBookClicked(val id: String): Event()

        object OnBackPressed : Event()

        class OnLoadStateChanged(val loadStates: CombinedLoadStates, val itemCount: Int): Event()
    }

    sealed class Effect: UiEffect {
        class NavigateBookDetailScreen(val id: String) : Effect()
        object ScrollToTop : Effect()

        class ShowErrorSnackbar(val messageId: Int): Effect()

        object PopBackStack : Effect()
    }
}