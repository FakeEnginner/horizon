package com.example.horizon.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import android.content.ContentValues.TAG
import com.example.horizon.Interface.OnboardingDataListener
import com.example.horizon.R


class firebaseConfig {

    private lateinit var listener: OnboardingDataListener

    fun setOnboardingDataListener(listener: OnboardingDataListener) {
        this.listener = listener
    }

    /*
   * firebase remote config implementation
   * */
    fun firebaseConfig(context: Context){
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 36
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        setupDefaultConfig(remoteConfig,context)
    }
    private fun setupDefaultConfig(remoteConfig: FirebaseRemoteConfig, context: Context) {
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        fetchConfig(remoteConfig,context)
    }
    private fun fetchConfig(remoteConfig: FirebaseRemoteConfig, context: Context) {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if(task.isSuccessful)
                {
                    val updated = task.result
                    Log.d(TAG, "Config params updated: $updated")
                    Toast.makeText(
                        context,
                        "Fetch and activate succeeded",
                        Toast.LENGTH_SHORT,
                    ).show()

                    val myValue = getValueByKey(remoteConfig, "onboarding")
                    listener.onBoardingDataReceived(myValue)
                    Log.d(TAG, "Fetched value for 'onboarding': $myValue")
                } else {
                    Toast.makeText(
                        context,
                        "Fetch failed",
                        Toast.LENGTH_SHORT,
                    ).show()
                    listener.onFetchFailed()
                }
            }
    }
    private fun getValueByKey(remoteConfig: FirebaseRemoteConfig, key: String): String {
        return remoteConfig.getString(key)
    }
}