package com.soochang.presentation.ui.base

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<V : ViewDataBinding>(
    val bindingFactory: (LayoutInflater) -> V
): AppCompatActivity() {
    protected val TAG = this.javaClass.simpleName

    lateinit var binding: V

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if( !::binding.isInitialized ){
            binding = bindingFactory(layoutInflater)
            binding.lifecycleOwner = this
        }

        setContentView(binding.root)
    }

    open fun hideKeyboard() {
        val inputManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    //editText 바깓영역 터치시 키보드 내리고, 포커스 뺏기
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if( currentFocus != null && currentFocus is EditText && ev != null ){
            val rect = Rect()
            currentFocus?.getGlobalVisibleRect(rect)
            val x = ev.x.toInt()
            val y = ev.y.toInt()

            if( !rect.contains(x, y) ){
                hideKeyboard()
                currentFocus?.clearFocus()
            }
        }

        return super.dispatchTouchEvent(ev)
    }
}