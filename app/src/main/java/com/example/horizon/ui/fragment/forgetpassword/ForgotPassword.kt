package com.example.horizon.ui.fragment.forgetpassword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.horizon.R
import com.example.horizon.databinding.FragmentForgetBinding

class ForgotPassword : Fragment() {
    lateinit var binding: FragmentForgetBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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