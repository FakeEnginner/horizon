package com.example.horizon.ui.fragment.forgetpassword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.horizon.R
import com.example.horizon.databinding.FragmentForgetBinding
import com.example.horizon.ui.fragment.login.login
import com.example.horizon.utils.Helper

class ForgotPassword : Fragment() {
    lateinit var binding: FragmentForgetBinding
    val helper = Helper()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                helper.replaceFragment(login(),requireFragmentManager())
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this,callback)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentForgetBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
}