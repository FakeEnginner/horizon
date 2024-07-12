package com.example.horizon.WebViewContainer.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WebViewModel: ViewModel() {
    private var pwaStatus = MutableLiveData<Boolean>(false)
    private var currentModule : String = ""

    fun setPwaStatus(value: Boolean) {
        pwaStatus.postValue(value)
    }

    fun getPwaStatus(): MutableLiveData<Boolean> {
        return pwaStatus
    }

    fun setCurrentModule(value: String) {
        currentModule = value
    }

    fun getCurrentModule(): String {
        return currentModule
    }
}