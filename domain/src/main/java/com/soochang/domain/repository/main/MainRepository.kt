package com.soochang.domain.repository.main

import com.soochang.domain.model.Result
import com.soochang.domain.model.main.MainMenu

interface MainRepository {
    suspend fun getMainMenu(): Result<MainMenu>
}