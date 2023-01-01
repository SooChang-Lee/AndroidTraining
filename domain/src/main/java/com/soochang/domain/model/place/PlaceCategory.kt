package com.soochang.domain.model.place

enum class PlaceCategory(
    val id : String,
    val placeName : String
){
    MART("MT1", "대형마트"),
    CONVENIENCE_STORE("CS2", "편의점"),
    KINDERGARTEN("PS3", "어린이집, 유치원"),
    SCHOOL("SC4", "학교"),
    PRIVATE_INSTITUTE("AC5", "학원"),
    PARKING_LOT("PK6", "주차장"),
    GAS_STATION("OL7", "주유소, 충전소"),
    SUBWAY_STATION("SW8", "지하철역"),
    BANK("BK9", "은행"),
    CULTURAL_FACILITIE("CT1", "문화시설"),
    BROKERAGE_AGENCY("AG2", "중개업소"),
    GOVERNMENT_OFFICE("PO3", "공공기관"),
    TOURIST_ATTRACTIONS("AT4", "관광명소"),
    LODGMENT("AD5", "숙박"),
    RESTAURANT("FD6", "음식점"),
    CAFE("CE7", "카페"),
    HOSPITAL("HP8", "병원"),
    PHARMACY("PM9", "약국"),
    UNKNOWN("UNKNOWN", "알 수 없는 카테고리")
}