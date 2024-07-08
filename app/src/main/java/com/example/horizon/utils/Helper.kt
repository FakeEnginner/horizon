package com.example.horizon.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.horizon.R
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings


class Helper {
    private lateinit var frameLayout: FrameLayout
    fun replaceFragment(fragment: Fragment, fragmentManager: FragmentManager) {
        fragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .addToBackStack(null)
            .commit()
    }


    @SuppressLint("SuspiciousIndentation")
    fun replacetoDashboardFragment(fragment: Fragment, fragmentManager: FragmentManager){
        fragmentManager.beginTransaction()
            .replace(R.id.DashboardContainer,fragment)
            .addToBackStack("null")
            .commit()
    }
    fun showFrameLayout() {
        frameLayout.visibility = FrameLayout.VISIBLE
    }
    fun hideFrameLayout() {
        frameLayout.visibility = FrameLayout.INVISIBLE
    }

    /*
    * firebase remote config implementation
    * */
    fun firebaseConfig(context: Context){
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        setupDefaultConfig(remoteConfig,context)
    }
    private fun setupDefaultConfig(remoteConfig: FirebaseRemoteConfig, context: Context ) {
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        fetchConfig(remoteConfig,context)
    }
    private fun fetchConfig(remoteConfig: FirebaseRemoteConfig, context: Context) {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(
                context as Activity
            ) { task ->
            if(task.isSuccessful)
            {
                val updated = task.result
                Log.d(TAG, "Config params updated: $updated")
                Toast.makeText(
                    context,
                    "Fetch and activate succeeded",
                    Toast.LENGTH_SHORT,
                ).show()
            } else {
                Toast.makeText(
                    context,
                    "Fetch failed",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }
     fun addFrameLayout(container: FrameLayout) {
        container.addView(frameLayout)
    }

     fun removeFrameLayout(container: FrameLayout) {
        container.removeView(frameLayout)
    }
}


