package com.example.horizon.utils

import android.annotation.SuppressLint
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.horizon.R

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
            .replace(R.id.frameLayout,fragment)
            .addToBackStack("null")
            .commit()
    }
    fun showFrameLayout() {
        frameLayout.visibility = FrameLayout.VISIBLE
    }
    fun hideFrameLayout() {
        frameLayout.visibility = FrameLayout.INVISIBLE
    }
}