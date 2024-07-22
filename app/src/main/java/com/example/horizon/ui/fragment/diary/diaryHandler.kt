package com.example.horizon.ui.fragment.diary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.horizon.databinding.FragmentDiaryBinding

class diaryHandler :Fragment() {

    private lateinit var fragmentDiaryBinding: FragmentDiaryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       fragmentDiaryBinding = FragmentDiaryBinding.inflate(layoutInflater,container,false)
        return fragmentDiaryBinding.root
    }

}