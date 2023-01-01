package com.soochang.data.openapi.kakao.response

import com.google.gson.annotations.SerializedName
import com.soochang.domain.model.book.BookItem
import com.soochang.domain.model.book.BookItems
import com.soochang.domain.repository.openapi.OpenApiRepository

data class KakaoBooksResponse(
    @SerializedName("meta") val meta: Meta,
    @SerializedName("documents") val documents: List<Document>,
){
    fun toBookItemsModel(currentPage: Int, countPerPage: Int) = BookItems(
        meta = BookItems.Meta(currentPage, meta.totalCount, countPerPage),
//        items = documents.map { it.toBookItemModel() }

        //숫자형의 고유한 Id가 없어 페이지넘버 기반으로 Id부여
        items = documents.mapIndexed{index, it ->
            val bookItem = it.toBookItemModel()
            bookItem.itemId = (((currentPage - 1) * countPerPage) + (index + 1)).toLong()
            bookItem
        }
    )

    data class Meta(
        @SerializedName("total_count") val totalCount: Int,
        @SerializedName("pageable_count") val pageableCount: Int,
        @SerializedName("is_end") val isEnd: Boolean,
    )

    data class Document(
        @SerializedName("title") val title: String,
        @SerializedName("contents") val contents: String,
        @SerializedName("url") val url: String,
        @SerializedName("isbn") val isbn: String,
        @SerializedName("datetime") val datetime: String,
        @SerializedName("authors") val authors: List<String>,
        @SerializedName("publisher") val publisher: String,
        @SerializedName("translators") val translators: List<String>,
        @SerializedName("price") val price: Int,
        @SerializedName("sale_price") val salePrice: Int,
        @SerializedName("thumbnail") val thumbnail: String,
        @SerializedName("status") val status: String
    ){
        fun toBookItemModel() = BookItem(
            id = isbn.split(" ").maxBy { it.length },
            title = title,
            subtitle = null,
            description = contents,
            authors = authors,
            publisher = publisher,
            publishedDate = datetime,
            saleStatus = status,
            listPrice = price,
            retailPrice = salePrice,
            categories = null,
            mainCategory = null,
            isbn10 = isbn.split(" ").find { it.length == 10 },
            isbn13 = isbn.split(" ").find { it.length == 13 },
            imageLinks = BookItem.ImageLinks(
                thumbnail = thumbnail,
                cover = thumbnail
            ),
            bookDataSource = OpenApiRepository.BookDataSource.KakaoBooks
        )
    }
}