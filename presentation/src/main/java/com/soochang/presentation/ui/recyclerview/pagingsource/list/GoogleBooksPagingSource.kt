package com.soochang.presentation.ui.recyclerview.pagingsource.list

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.soochang.domain.model.Result
import com.soochang.domain.model.book.BookItem
import com.soochang.domain.repository.openapi.OpenApiRepository

class GoogleBooksPagingSource constructor(
    private val openApiRepository: OpenApiRepository,
    private val query: String,
    private val pageSize: Int
) : PagingSource<Int, BookItem>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, BookItem> {
        val page = params.key ?: 1

        val result = openApiRepository.findBooksByTitle(OpenApiRepository.BookDataSource.GoogleBooks, query, page, pageSize)

        return when(result){
            is Result.Success -> {
                LoadResult.Page(
                    data = result.data.items,
                    prevKey = null, // Only paging forward.
                    nextKey = when( result.data.meta.isEndPage() || result.data.items.isEmpty()) {
                        true -> null
                        false -> page + 1
                    }
                )
            }
            is Result.Error -> {
                LoadResult.Error(
                    result.throwable
                )
            }
        }
    }

    override fun getRefreshKey(state: PagingState<Int, BookItem>): Int? {
        // Try to find the page key of the closest page to anchorPosition, from
        // either the prevKey or the nextKey, but you need to handle nullability
        // here:
        //  * prevKey == null -> anchorPage is the first page.
        //  * nextKey == null -> anchorPage is the last page.
        //  * both prevKey and nextKey null -> anchorPage is the initial page, so
        //    just return null.
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}