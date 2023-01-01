package com.soochang.presentation.util.pagination

import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PaginationScrollListener(
    private val onPagingEventListener: OnPagingEventListener,
    private val layoutManager: LinearLayoutManager
) : RecyclerView.OnScrollListener() {

    private var nextPageRequested = false
    private var isEndPage = false

    private val visibleThreshold = 10
    private var previousTotal = 0
    private var firstVisibleItem = 0
    private var visibleItemCount = 0
    private var totalItemCount = 0

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

//        Log.d(this.javaClass.simpleName, "onScrolled: isEndPage=$isEndPage previousTotal=$previousTotal")

        if(isEndPage )
            return

        visibleItemCount = recyclerView.childCount
        totalItemCount = layoutManager.itemCount
        firstVisibleItem = layoutManager.findFirstVisibleItemPosition()

        //첫페이지 이후 다음페이지 로드 필요한 상황만 체크
        if(previousTotal == 0 )
            return

        Log.d(this.javaClass.simpleName, "onScrolled: totalItemCount - visibleItemCount = ${totalItemCount - visibleItemCount}, firstVisibleItem + visibleThreshold = ${(firstVisibleItem + visibleThreshold)}")


        if (!nextPageRequested && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
            Log.d(this.javaClass.simpleName, "onScrolled: onNeedNextPage")
            Log.d(this.javaClass.simpleName, "onScrolled: nextPageRequested=$nextPageRequested totalItemCount=$totalItemCount visibleItemCount=$visibleItemCount firstVisibleItem=$firstVisibleItem visibleThreshold=$visibleThreshold")
            Log.d(this.javaClass.simpleName, "onScrolled: totalItemCount - visibleItemCount = ${totalItemCount - visibleItemCount}, firstVisibleItem + visibleThreshold = ${(firstVisibleItem + visibleThreshold)}")

            onPagingEventListener.onNeedNextPage()

            nextPageRequested = true
        }
    }

    fun setLoadFinished(isEndPage: Boolean = false){
        Log.d(this.javaClass.simpleName, "setLoadFinished: isEndPage ${isEndPage} totalItemCount=$totalItemCount previousTotal=$previousTotal")

        this.isEndPage = isEndPage

        nextPageRequested = false
        previousTotal = layoutManager.itemCount
    }
}

interface OnPagingEventListener{
    fun onNeedNextPage()
}