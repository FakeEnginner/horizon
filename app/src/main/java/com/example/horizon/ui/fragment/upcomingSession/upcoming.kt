package com.example.horizon.ui.fragment.upcomingSession

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.horizon.databinding.CounsellingSessionBinding

class upcoming : Fragment() {

    private var binding: CounsellingSessionBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = CounsellingSessionBinding.inflate(layoutInflater,container,false)
        return binding!!.root
    }

}