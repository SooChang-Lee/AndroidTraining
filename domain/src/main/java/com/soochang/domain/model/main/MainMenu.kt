package com.soochang.domain.model.main

data class MainMenu(
    val mainMenuList: List<MenuItem>
){
    data class MenuItem(
        val type: String,
        val title: String,
        val subTitle: String,
        val id: String
    )
}