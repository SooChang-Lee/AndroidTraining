package com.soochang.domain.model.book

import kotlin.math.ceil

data class BookItems(
    val meta: Meta,

    val items: List<BookItem>
){
    data class Meta(
        val currentPage: Int,

        val totalCount: Int,
        val countPerPage: Int
    ){
        fun getTotalPage(): Int {
            return ceil(totalCount / countPerPage.toDouble()).toInt()
        }

        fun isEndPage(): Boolean {
            return if( getTotalPage() == 0 ) {
                true
            }else{
                getTotalPage() <= currentPage
            }
        }
    }
}