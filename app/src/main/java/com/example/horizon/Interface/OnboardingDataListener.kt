package com.example.horizon.Interface

interface OnboardingDataListener {
     val imageResourceMap:  Map<String, Int>

    fun onBoardingDataReceived(jsonString: String)
    fun onFetchFailed()

}