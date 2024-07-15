package com.example.horizon.utils

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.horizon.R



class Helper {

    fun replaceFragment(fragment: Fragment, fragmentManager: FragmentManager) {
        fragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .addToBackStack("null")
            .commit()
    }

    @SuppressLint("SuspiciousIndentation")
    fun replacetoDashboardFragment(fragment: Fragment, fragmentManager: FragmentManager){
        fragmentManager.beginTransaction()
            .replace(R.id.DashboardContainer,fragment)
            .addToBackStack("null")
            .commit()
    }
}


