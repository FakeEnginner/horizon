package com.example.horizon.ui.fragment.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.horizon.Interface.mainFrameChange
import com.example.horizon.R
import com.example.horizon.databinding.FragmentDashboardBinding
import com.example.horizon.databinding.FragmentForgetBinding
import com.example.horizon.model.bannerModel
import com.example.horizon.ui.fragment.dashboard.adapter.bannerAdapter
import com.example.horizon.ui.fragment.login.login
import com.example.horizon.utils.Helper

class Dashboard: Fragment() {
    private var mainFrameChange: mainFrameChange? = null
    private lateinit var  binding : FragmentDashboardBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: bannerAdapter
    val helper = Helper()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashboardBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (activity is mainFrameChange) {
            mainFrameChange = activity as mainFrameChange
        }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
              mainFrameChange()
              helper.replaceFragment(login(),requireFragmentManager())
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }
    private fun setupRecyclerView() {
        recyclerView = binding.banner
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        adapter = bannerAdapter()
        recyclerView.adapter = adapter
        val items = listOf(
            bannerModel(1, "Item 1", "https://example.com/image1.jpg"),
            bannerModel(2, "Item 2", "https://example.com/image2.jpg"),
            bannerModel(3, "Item 3", "https://example.com/image3.jpg")
        )
        adapter.submitList(items)
    }

    fun mainFrameChange() {
        mainFrameChange?.mainFrameChange()
    }


}