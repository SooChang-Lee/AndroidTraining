package com.soochang.presentation.util.custom

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import com.google.android.material.textfield.TextInputEditText

class CustomTextInputEditText: TextInputEditText {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    //키보드 떠있는 상태에서 백버튼 터치시 키보드만 사라지고, 포커스 유지되는 현상 처리
    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            clearFocus()
        }

        return super.onKeyPreIme(keyCode, event)
    }
}