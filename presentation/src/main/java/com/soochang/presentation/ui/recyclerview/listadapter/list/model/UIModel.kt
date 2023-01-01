package com.soochang.presentation.ui.recyclerview.listadapter.list.model

import com.soochang.domain.model.book.BookItem

sealed class UIModel {
    data class Header(val totalCount: Int = 0): UIModel()

    data class Data(val bookItem: BookItem): UIModel()

    object Progress: UIModel()
}