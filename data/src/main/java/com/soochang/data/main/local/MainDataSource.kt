package com.soochang.data.main.local

import android.content.Context
import com.google.gson.Gson
import com.soochang.domain.model.main.MainMenu
import javax.inject.Inject


interface MainDataSource {
    suspend fun getMainMenu(): MainMenu
}

class MainDataSourceImpl @Inject constructor(
    private val appContext: Context
) : MainDataSource {
    override suspend fun getMainMenu(): MainMenu {
//        val inputStream = appContext.resources.openRawResource(R.raw.main_menu)
        val inputStream = appContext.assets.open("menu/main_menu.json")
        val jsonString: String = inputStream.bufferedReader().use { it.readText() }

        val gson = Gson()

        return gson.fromJson(jsonString, MainMenu::class.java)
    }
}