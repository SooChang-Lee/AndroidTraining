package com.soochang.presentation.ui.openapi.naverbooks.list.model

import com.soochang.domain.model.book.BookItem

sealed class DataItem {
    abstract val itemId: Long

    data class Header(val totalCount: Int = 0): DataItem() {
        override val itemId = Long.MIN_VALUE
    }

    data class Data(val bookItem: BookItem): DataItem() {
        override val itemId = bookItem.itemId
    }

    object Progress: DataItem() {
        override val itemId = Long.MAX_VALUE
    }
}