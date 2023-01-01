package com.soochang.presentation.ui.base

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.soochang.presentation.R

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

abstract class BaseFragment<V: ViewDataBinding>(
    private val inflate: Inflate<V>
) : Fragment() {
    protected val TAG = this.javaClass.simpleName

//    lateinit var _binding: V

    private var _binding: V? = null
    val binding get() = _binding ?: error("View를 참조하기위한 제너릭클래스가 전달되지 않았습니다.")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        Log.d(TAG, "onCreateView: _binding.isInitialized ${::_binding.isInitialized}")
//        if( !::_binding.isInitialized ){
//            _binding = inflate.invoke(inflater, container, false)
//            binding.lifecycleOwner = viewLifecycleOwner
//        }

        if( _binding == null ){
            _binding = inflate.invoke(inflater, container, false)
            binding.lifecycleOwner = viewLifecycleOwner
        }


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}