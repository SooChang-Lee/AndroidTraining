package com.soochang.presentation.ui.compose.list

sealed class ListItemViewType<out R> {
    data class Data<out T>(val data: T?) : ListItemViewType<T>()

    data class Separator(val tag: String) : ListItemViewType<Nothing>()
}

//sealed class Result<out R> {
//    //    object Loading : Result<Nothing>()
//    data class Success<out T>(val data: T) : Result<T>()
//    data class Error(val throwable: ErrorEntity) : Result<Nothing>()
//
//    override fun toString(): String {
//        return when (this) {
////            is Loading -> "Loading"
//            is Success<*> -> "Success[data=$data]"
//            is Error -> "Error[exception=$throwable]"
//        }
//    }
//}