package com.soochang.presentation.ui.recyclerview.general.list.model

import com.soochang.domain.model.book.BookItem

sealed class UIModel {
    abstract val itemId: Long

    data class Header(val totalCount: Int = 0): UIModel() {
        override val itemId = Long.MIN_VALUE
    }

    data class Data(val bookItem: BookItem): UIModel() {
        override val itemId = bookItem.itemId
    }

    object Progress: UIModel() {
        override val itemId = Long.MAX_VALUE
    }
}