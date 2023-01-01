package com.soochang.data.main

import android.util.Log
import com.soochang.data.error.ApiErrorHandler
import com.soochang.data.main.local.MainDataSource
import com.soochang.domain.model.Result
import com.soochang.domain.model.main.MainMenu
import com.soochang.domain.repository.main.MainRepository
import javax.inject.Inject

class MainRepositoryImpl @Inject constructor(
    private val mainDataSource: MainDataSource,
    private val apiErrorHandler: ApiErrorHandler) : MainRepository {
    override suspend fun getMainMenu(): Result<MainMenu> {
        return try {
            val mainMenu = mainDataSource.getMainMenu()

            Result.Success(
                mainMenu
            )
        } catch (e: Exception) {
            Log.d(this.javaClass.simpleName, "getMainMenu: Exception $e")

            Result.Error(apiErrorHandler.getErrorEntity(e))
        }
    }
}