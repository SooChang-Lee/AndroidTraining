package com.soochang.data.openapi.google.response

import com.soochang.domain.model.book.BookItem
import com.soochang.domain.model.book.BookItems
import com.soochang.domain.repository.openapi.OpenApiRepository

data class VolumeListResponse(
    val kind: String,
    val items: List<VolumeResponse>?,
    val totalItems: Int
) {
    fun toBookItemsModel(currentPage: Int, countPerPage: Int) = BookItems(
        meta = BookItems.Meta(currentPage, totalItems, countPerPage),

        //데이터셋이 Long형의 고유한 id값이 없어 인덱스를 기반으로 itemId 부여(RecyclerView Adapter)
        //items = items?.map { it.toBookItemModel() } ?: emptyList(),
        items = items?.mapIndexed{index, it ->
            val bookItem = it.toBookItemModel()
            bookItem.itemId = (((currentPage - 1) * countPerPage) + (index + 1)).toLong()
            bookItem
        } ?: emptyList()
    )

    data class VolumeResponse(
        val kind: String,
        val id: String,
        val etag: String,
        val selfLink: String,
        val volumeInfo: VolumeInfoResponse?,
        val saleInfo: SaleInfoResponse?,
        val accessInfo: AccessInfoResponse?,
        val searchInfo: SearchInfoResponse?
    ) {
        data class VolumeInfoResponse(
            val title: String,
            val subtitle: String,
            val authors: List<String>,
            val publisher: String,
            val publishedDate: String,
            val description: String,
            val industryIdentifiers: List<IndustryIdentifierResponse>,
            val readingModes: ReadingModesResponse,
            val pageCount: Int,
            val printType: String,
            val categories: List<String>,
            val mainCategory: String,
            val maturityRating: String,
            val allowAnonLogging: Boolean,
            val contentVersion: String,
            val panelizationSummary: PanelizationSummaryResponse,
            val imageLinks: ImageLinksResponse?,
            val language: String,
            val previewLink: String,
            val infoLink: String,
            val canonicalVolumeLink: String
        ) {
            data class IndustryIdentifierResponse(
                val type: String,
                val identifier: String
            )

            data class ReadingModesResponse(
                val text: Boolean,
                val image: Boolean
            )

            data class PanelizationSummaryResponse(
                val containsEpubBubbles: Boolean,
                val containsImageBubbles: Boolean
            )

            data class ImageLinksResponse(
                val thumbnail: String,
                val small: String,
                val medium: String,
                val large: String,
                val smallThumbnail: String,
                val extraLarge: String,
            )
        }

        data class SaleInfoResponse(
            val country: String,
            val saleability: String,
            val isEbook: Boolean,
            val listPrice: ListPriceResponse,
            val retailPrice: RetailPriceResponse,
            val buyLink: String,
            val offers: List<OfferResponse>
        ) {
            data class ListPriceResponse(
                val amount: Int,
                val currencyCode: String
            )

            data class RetailPriceResponse(
                val amount: Int,
                val currencyCode: String
            )

            data class OfferResponse(
                val finskyOfferType: Int,
                val listPrice: ListPriceResponse,
                val retailPrice: RetailPriceResponse
            ) {
                data class ListPriceResponse(
                    val amountInMicros: Long,
                    val currencyCode: String
                )

                data class RetailPriceResponse(
                    val amountInMicros: Long,
                    val currencyCode: String
                )
            }
        }

        data class AccessInfoResponse(
            val country: String,
            val viewability: String,
            val embeddable: Boolean,
            val publicDomain: Boolean,
            val textToSpeechPermission: String,
            val epub: EpubResponse,
            val pdf: PdfResponse,
            val webReaderLink: String,
            val accessViewStatus: String,
            val quoteSharingAllowed: Boolean
        ) {
            data class EpubResponse(
                val isAvailable: Boolean
            )

            data class PdfResponse(
                val isAvailable: Boolean,
                val acsTokenLink: String
            )
        }

        data class SearchInfoResponse(
            val textSnippet: String
        )

        fun toBookItemModel() = BookItem(
            id = id,
            title = volumeInfo?.title,
            subtitle = volumeInfo?.subtitle,
            description = volumeInfo?.description,
            authors = volumeInfo?.authors,
            publisher = volumeInfo?.publisher,
            publishedDate = volumeInfo?.publishedDate,
            saleStatus = saleInfo?.saleability,
            listPrice = saleInfo?.listPrice?.amount,
            retailPrice = saleInfo?.retailPrice?.amount,
            categories = volumeInfo?.categories,
            mainCategory = volumeInfo?.mainCategory,
            isbn10 = volumeInfo?.industryIdentifiers?.filter { it.type == "ISBN_13" }?.firstOrNull()?.identifier,
            isbn13 = volumeInfo?.industryIdentifiers?.filter { it.type == "ISBN_10" }?.firstOrNull()?.identifier,
            imageLinks = BookItem.ImageLinks(
                thumbnail = volumeInfo?.imageLinks?.thumbnail,
                cover = volumeInfo?.imageLinks?.small,
            ),
            bookDataSource = OpenApiRepository.BookDataSource.GoogleBooks
        )
    }
}

