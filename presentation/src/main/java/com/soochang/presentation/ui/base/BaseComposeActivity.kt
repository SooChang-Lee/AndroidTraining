package com.soochang.presentation.ui.base

import androidx.activity.ComponentActivity

abstract class BaseComposeActivity(): ComponentActivity() {
    protected val TAG = this.javaClass.simpleName

}