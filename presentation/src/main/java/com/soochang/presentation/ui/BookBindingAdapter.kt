package com.soochang.presentation.ui

import android.content.res.Resources
import android.graphics.Color
import android.graphics.Paint
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.text.toSpannable
import androidx.core.text.toSpanned
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.soochang.domain.model.book.BookItem
import com.soochang.presentation.R


object BookBindingAdapter {

    @BindingAdapter("bind_image")
    @JvmStatic
    //도서 썸네일 이미지
    fun bindImage(imageView: ImageView, url: String?){
        Glide.with(imageView.context)
            .load(url)
            .placeholder(R.color.lightGray)
//                .error(R.drawable.ic_launcher)
//                .fallback(R.color.lightGray)
            .apply(RequestOptions.centerInsideTransform())
            .into(imageView)
    }

    @BindingAdapter("bind_spannable_title")
    @JvmStatic
    //도서제목 검색어 일치 텍스트 강조(Spannable Text)
    fun bindSpanableTitle(textView: TextView, bookItem: BookItem?){
        if(bookItem?.title == null)
            return

        val queryIndex = bookItem.title!!.lowercase().indexOf(bookItem.query.lowercase())
        if( queryIndex > -1 ){
            val spannable = SpannableStringBuilder(bookItem.title)
            spannable.setSpan(
                ForegroundColorSpan(textView.context.getColor(R.color.book_title_spannable_color)),
                queryIndex, // start
                queryIndex + bookItem.query.length, // end
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )

            textView.setText(spannable, TextView.BufferType.SPANNABLE)
        }else{
            textView.text = bookItem.title
        }
    }
//
//
//    @BindingAdapter("bind_display_discount_ratio")
//    @JvmStatic
//    //할인율 계산
//    fun bindDisplayDiscountRatio(textView: TextView, bookItem: BookItem?){
//        if( bookItem != null && bookItem.salePrice != bookItem.price ){
//            textView.text = "${100 - (bookItem.salePrice!!.toDouble() / bookItem.price!! * 100).toInt()}%"
//            textView.visibility = View.VISIBLE
//        }else{
//            textView.visibility = View.GONE
//        }
//    }
//
//    @BindingAdapter("bind_display_price_container")
//    @JvmStatic
//    //정가판매인경우(할인율 0%) 정가 숨기기
//    fun bindDisplayPriceContainer(linearLayout: LinearLayout, bookItem: BookItem?){
//        if( bookItem != null && bookItem.salePrice != bookItem.price ){
//            linearLayout.visibility = View.VISIBLE
//        }else{
//            linearLayout.visibility = View.GONE
//        }
//    }
//
//    @BindingAdapter("bind_display_cancel_line")
//    @JvmStatic
//    //정가에 취소선 표시
//    fun bindDisplayCancelLine(textView: TextView, bookItem: BookItem?){
//        textView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
//    }
}