package com.example.horizon.ui.fragment.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.horizon.R
import com.example.horizon.ui.fragment.login.login
import com.example.horizon.utils.Helper


class signup: Fragment() {
    val helper = Helper()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_signup, container, false)

        val creatAccount = view.findViewById<ImageView>(R.id.createaccount)
        val login = view.findViewById<TextView>(R.id.logintxt)
        login.setOnClickListener {
            helper?.replaceFragment(login(),requireFragmentManager())
        }


        return view
    }
}