package com.soochang.domain.model.place

import kotlin.math.ceil

data class PlaceItems (
    val meta: Meta = Meta(),
    val listPlace:List<PlaceItem> = emptyList()
){
    data class Meta(
        val currentPage: Int = 1,

        val totalCount: Int = 0,
        val countPerPage: Int = 30
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