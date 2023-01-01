package com.soochang.domain.model.place

data class PlaceItem(
    val id: String,
    val itemId: Long,
    val placeName:String,
    val placeCategory: PlaceCategory,
    val phone:String,
    val address:String,
    val roadAddress:String,
    val longitude:String,
    val latitude:String,
    val placeUrl:String
)