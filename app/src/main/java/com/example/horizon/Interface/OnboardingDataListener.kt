package com.example.horizon.Interface

interface OnboardingDataListener {
    fun onBoardingDataReceived(jsonString: String)
    fun onFetchFailed()

}