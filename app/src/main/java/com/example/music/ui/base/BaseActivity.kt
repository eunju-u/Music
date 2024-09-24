package com.example.music.ui.base

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.databinding.library.baseAdapters.BR

abstract class BaseActivity<B : ViewDataBinding, VM : ViewModel> : AppCompatActivity() {
    val TAG = "music play ${this.javaClass.simpleName}"

    protected abstract fun getLayoutRes(): Int
    protected abstract fun getVmClass(): Class<VM>

    protected lateinit var binding: B
    protected lateinit var vm: VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        binding = DataBindingUtil.setContentView(this, getLayoutRes())
        vm = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(application)
        )[getVmClass()]
        binding.setVariable(BR.vm, vm)

    }
}