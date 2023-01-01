package com.soochang.domain.model.book

import com.soochang.domain.repository.openapi.OpenApiRepository

data class BookItem(
    var itemId: Long = -1L,

    val id: String,
    val title: String?,
    val subtitle: String?,
    val description: String?,
    val authors: List<String>?,
    val publisher: String?,
    val publishedDate: String?,
    val saleStatus: String?,
    val listPrice: Int?,
    val retailPrice: Int?,
    val categories: List<String>?,
    val mainCategory: String?,
    val isbn10: String?,
    val isbn13: String?,
    val imageLinks: ImageLinks?,

    var farvorite: Boolean = false,//RemoteMediator버전에서 사용

    val bookDataSource: OpenApiRepository.BookDataSource,

    var query: String = ""
){
    data class ImageLinks(
        val thumbnail: String?,
        val cover: String?
    )

    val saleability
        get() = when(bookDataSource){
            OpenApiRepository.BookDataSource.GoogleBooks -> {
                saleStatus == "FOR_SALE"
            }
            OpenApiRepository.BookDataSource.KakaoBooks,
            OpenApiRepository.BookDataSource.NaverBooks -> {
                saleStatus == "정상판매"
            }
        }

    val strAuthors
        get() = authors?.joinToString(", ") ?: ""

    val discounted: Boolean
        get() = (this.listPrice ?: 0) > (this.retailPrice ?: 0)

    val discountRatio: Int
        get() = 100 - ((this.retailPrice ?: 0) / (this.listPrice ?: 0).toDouble() * 100).toInt()
}