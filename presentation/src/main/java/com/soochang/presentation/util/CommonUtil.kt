package com.soochang.presentation.util

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.window.layout.WindowMetricsCalculator
import com.google.android.material.snackbar.Snackbar
import com.soochang.presentation.BuildConfig

object CommonUtil{
    /**
     * 공통 다이얼로그
     */
    fun showDialog(
        ctx: Context,
        title:String? = null,
        message:String? = null,
        positiveButtonName:String? = null, positiveButtonListener: DialogInterface.OnClickListener? = null,
        negativeButtonName:String? = null, negativeButtonListener: DialogInterface.OnClickListener? = null,
        neutralButtonName:String? = null, neutralButtonListener: DialogInterface.OnClickListener? = null,
        cancelable:Boolean = true,
        onCancelListener: DialogInterface.OnCancelListener? = null
    ) {
        val builder = AlertDialog.Builder(ctx)

        if( title != null ){
            builder.setTitle(title)
        }

        if( message != null ){
            builder.setMessage(message)
        }

        if( positiveButtonName == null && neutralButtonName == null && negativeButtonName == null ){
            builder.setPositiveButton("확인", null)
        }else{
            if( positiveButtonName != null ){
                builder.setPositiveButton(positiveButtonName, positiveButtonListener)
            }
            if( neutralButtonName != null ){
                builder.setNeutralButton(neutralButtonName, neutralButtonListener)
            }
            if( negativeButtonName != null ){
                builder.setNegativeButton(negativeButtonName, negativeButtonListener)
            }
        }

        builder.setCancelable(cancelable)
        builder.setOnCancelListener(onCancelListener)

        val dialog = builder.create()
        dialog.show()
    }

    /**
     * 스낵바
     */
    fun showSnackbar(view: View, msg: String?, snackbarLength: Int, actionText: String? = null, listener: View.OnClickListener? = null){
        Snackbar.make(view, msg!!, snackbarLength).run {
            if( actionText != null ){
                setAction(actionText, listener)
            }

            show()
        }
    }

    /**
     * 토스트
     */
    fun showToast(ctx: Context, msg: String?, toastLength: Int){
        Toast.makeText(ctx, msg, toastLength).show()
    }

    /**
     * 토스트(디버그용)
     */
    fun showDebugToast(ctx: Context, msg: String?){
        if( !BuildConfig.DEBUG )
            return

        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
    }

    /**
     * 사이즈 클래스 - 일반폰, 폴더블, 태블릿 판단용
     */
    //Compact: 일반 휴대폰, Medium: 폴더블, Expanded: 태블릿
    enum class WindowSizeClass { COMPACT, MEDIUM, EXPANDED }

    fun getWidthSizeClasses(activity: Activity): WindowSizeClass {
        val metrics = WindowMetricsCalculator.getOrCreate()
            .computeCurrentWindowMetrics(activity)

        val widthDp = metrics.bounds.width() /
                activity.resources.displayMetrics.density
        return when {
            widthDp < 600f -> WindowSizeClass.COMPACT
            widthDp < 840f -> WindowSizeClass.MEDIUM
            else -> WindowSizeClass.EXPANDED
        }
    }

    fun getHeightSizeClasses(activity: Activity): WindowSizeClass {
        val metrics = WindowMetricsCalculator.getOrCreate()
            .computeCurrentWindowMetrics(activity)

        val heightDp = metrics.bounds.height() /
                activity.resources.displayMetrics.density
        return when {
            heightDp < 480f -> WindowSizeClass.COMPACT
            heightDp < 900f -> WindowSizeClass.MEDIUM
            else -> WindowSizeClass.EXPANDED
        }
    }

    /**
     * DP <-> PX <-> SP 변환
     */
    fun pxToDp(context: Context, px: Float): Float {
        return px / context.resources.displayMetrics.density
    }

    fun dpToPx(context: Context, dp: Float): Float {
//        return dp / context.resources.displayMetrics.density
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
    }

    fun pxToSp(context: Context, px: Float): Float{
        return px / context.resources.displayMetrics.scaledDensity
    }

    fun spToPx(context: Context, sp: Float): Float{
//            return sp * context.resources.displayMetrics.scaledDensity + 0.5f
//            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics)
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics )
    }
}