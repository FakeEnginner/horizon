package com.example.horizon.ui.fragment.login

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.horizon.R
import com.example.horizon.ui.fragment.forgetpassword.ForgotPassword
import com.example.horizon.ui.fragment.signup.signup
import com.example.horizon.utils.Helper

class login : Fragment(){
    val helper = Helper()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        val loginbtn = view.findViewById<ImageView>(R.id.Login)
        val signup = view.findViewById<TextView>(R.id.sign_uplogin)
        val forgotpassword = view.findViewById<TextView>(R.id.forgot_pass)

        signup.setOnClickListener {
            helper?.replaceFragment(signup(),requireFragmentManager())
        }
        forgotpassword.setOnClickListener {
            helper?.replaceFragment(ForgotPassword(),requireFragmentManager())
        }
        return view
    }
}