package com.soochang.data.openapi.kakao.response

import com.google.gson.annotations.SerializedName
import com.soochang.domain.model.place.PlaceCategory
import com.soochang.domain.model.place.PlaceItem
import com.soochang.domain.model.place.PlaceItems

data class KakaoPlaceResponse(
    @SerializedName("meta") val meta: Meta,
    @SerializedName("documents") val documents:List<Document>,
){
    fun toPlaceItemsModel(currentPage: Int, countPerPage: Int) = PlaceItems(
        meta = meta.toPlaceItemMeta(currentPage, countPerPage),
//        listPlace = kakaoPlaceDocuments.map { it.toPlaceItemModel() }
        listPlace = documents.mapIndexed{ index, it ->
            val newItemId = (((currentPage - 1) * countPerPage) + (index + 1)).toLong()

            it.toPlaceItemModel(newItemId)
        }
    )

    data class Meta(
        @SerializedName("total_count") val totalCount:Int,
        @SerializedName("pageable_count") val pageableCount:Int,
        @SerializedName("is_end") val isEnd:Boolean,
        @SerializedName("same_name") val sameName: RegionInfo,
    ){
        fun toPlaceItemMeta(currentPage: Int, countPerPage: Int): PlaceItems.Meta {
            return PlaceItems.Meta(
                currentPage = currentPage,
                totalCount = pageableCount,
                countPerPage = countPerPage
            )
        }

        data class RegionInfo(
            @SerializedName("region") val region: List<String>,
            @SerializedName("keyword") val keyword:String,
            @SerializedName("selected_region") val selectedRegion:String
        )
    }

    data class Document(
        @SerializedName("id") val id: String,
        @SerializedName("place_name") val placeName:String,
        @SerializedName("category_name") val categoryName:String,
        @SerializedName("category_group_code") val categoryGroupCode:String,
        @SerializedName("category_group_name") val categoryGroupName:String,
        @SerializedName("phone") val phone:String,
        @SerializedName("address_name") val addressName:String,
        @SerializedName("road_address_name") val roadAddressName:String,
        @SerializedName("x") val x:String,
        @SerializedName("y") val y:String,
        @SerializedName("place_url") val placeUrl:String,
        @SerializedName("distance") val distance:String
    ){
        fun toPlaceItemModel(itemId: Long) = PlaceItem(
            id = id,
            itemId = itemId,
            placeName = placeName,
            placeCategory = when(categoryGroupCode){
                "HP8" -> {
                    PlaceCategory.HOSPITAL
                }
                "PM9" -> {
                    PlaceCategory.PHARMACY
                }
                "OL7" -> {
                    PlaceCategory.GAS_STATION
                }
                else -> {
                    PlaceCategory.UNKNOWN
                }
            },
            phone = phone,
            address = addressName,
            roadAddress = roadAddressName,
            longitude = x,
            latitude = y,
            placeUrl = placeUrl
        )
    }
}