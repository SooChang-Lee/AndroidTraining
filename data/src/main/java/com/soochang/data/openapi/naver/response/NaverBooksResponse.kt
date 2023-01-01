package com.soochang.data.openapi.naver.response

import com.google.gson.annotations.SerializedName
import com.soochang.domain.model.book.BookItem
import com.soochang.domain.model.book.BookItems
import com.soochang.domain.repository.openapi.OpenApiRepository

data class NaverBooksResponse(
    @SerializedName("lastBuildDate") val lastBuildDate: String,
    @SerializedName("total") val total: Int,
    @SerializedName("start") val start: Int,
    @SerializedName("display") val display: Int,
    @SerializedName("items") val items: List<Item>
) {
    fun toBookItemsModel(currentPage: Int, countPerPage: Int) = BookItems(
        meta = BookItems.Meta(currentPage, total, countPerPage),
//        items = documents.map { it.toBookItemModel() }

        //데이터셋이 Long형의 고유한 id값이 없어 인덱스를 기반으로 itemId 부여(RecyclerView Adapter)
        //items = items?.map { it.toBookItemModel() } ?: emptyList(),
        items = items.mapIndexed{index, it ->
            val bookItem = it.toBookItemModel()
            bookItem.itemId = (((currentPage - 1) * countPerPage) + (index + 1)).toLong()
            bookItem
        }
    )

    data class Item(
        @SerializedName("title") val title: String,
        @SerializedName("link") val link: String,
        @SerializedName("image") val image: String,
        @SerializedName("author") val author: String,
        @SerializedName("discount") val discount: Int,
        @SerializedName("publisher") val publisher: String,
        @SerializedName("pubdate") val pubdate: String,
        @SerializedName("isbn") val isbn: String,
        @SerializedName("description") val description: String
    ){
        fun toBookItemModel() = BookItem(
            id = isbn.split(" ").maxBy { it.length },
            title = title,
            subtitle = null,
            description = description,
            authors = listOf(author),
            publisher = publisher,
            publishedDate = pubdate,
            saleStatus = "정상판매",
            listPrice = discount,
            retailPrice = discount,
            categories = null,
            mainCategory = null,
            isbn10 = isbn.split(" ").find { it.length == 10 },
            isbn13 = isbn.split(" ").find { it.length == 13 },
            imageLinks = BookItem.ImageLinks(
                thumbnail = image,
                cover = image
            ),
            bookDataSource = OpenApiRepository.BookDataSource.KakaoBooks
        )
    }
}