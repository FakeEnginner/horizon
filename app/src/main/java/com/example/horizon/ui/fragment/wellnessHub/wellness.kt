package com.example.horizon.ui.fragment.wellnessHub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.horizon.databinding.CounsellingSessionBinding
import com.example.horizon.databinding.LayoutWellnessHubBinding

class wellness : Fragment() {
    private var binding: LayoutWellnessHubBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LayoutWellnessHubBinding.inflate(layoutInflater,container,false)
        return binding!!.root
    }
}