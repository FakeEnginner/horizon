package com.example.horizon.ui.fragment.dashboard

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.example.horizon.model.blogModel
import com.example.horizon.model.trendeningModel
import com.example.horizon.ui.fragment.dashboard.adapter.bannerAdapter
import com.example.horizon.ui.fragment.dashboard.adapter.blogAdapter
import com.example.horizon.ui.fragment.dashboard.adapter.trendeningAdapter
import com.example.horizon.ui.fragment.login.login
import com.example.horizon.utils.Helper

class Dashboard: Fragment() {
    private var mainFrameChange: mainFrameChange? = null
    private lateinit var  binding : FragmentDashboardBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var bannerAdapter: bannerAdapter
    private lateinit var blogAdapter: blogAdapter
    private lateinit var trendingAdapter: trendeningAdapter

    val helper = Helper()
    private val handler = Handler(Looper.getMainLooper())
    private val scrollRunnable = object : Runnable {
        override fun run() {
            val currentPosition = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            val bannerPosition = if (currentPosition == bannerAdapter.itemCount - 1) 0 else currentPosition + 1
            recyclerView.smoothScrollToPosition(bannerPosition)
            handler.postDelayed(this, 3000)
        }
    }

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
        setupBannerRecyclerView()
        setupBlogsRecyclerView()
        setupTrendingRecyclerView()
    }
    private fun setupBannerRecyclerView() {
        recyclerView = binding.banner
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        bannerAdapter = bannerAdapter()
        recyclerView.adapter = bannerAdapter
        val items = listOf(
            bannerModel(1, "Item 1", "https://example.com/image1.jpg"),
            bannerModel(2, "Item 2", "https://example.com/image2.jpg"),
            bannerModel(3, "Item 3", "https://example.com/image3.jpg")
        )
        bannerAdapter.submitList(items)
        handler.postDelayed(scrollRunnable, 3000)
    }

    private fun setupBlogsRecyclerView() {
        val blogsRecyclerView = binding.ViewOngoing
        blogsRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        blogAdapter = blogAdapter()
        blogsRecyclerView.adapter = blogAdapter
        val items = listOf(
            blogModel(1, "Item 1", "https://example.com/image1.jpg"),
            blogModel(2, "Item 2", "https://example.com/image2.jpg"),
            blogModel(3, "Item 3", "https://example.com/image3.jpg"),
            blogModel(4, "Item 3", "https://example.com/image3.jpg"),
            blogModel(1, "Item 1", "https://example.com/image1.jpg"),
            blogModel(2, "Item 2", "https://example.com/image2.jpg"),
            blogModel(3, "Item 3", "https://example.com/image3.jpg"),
            blogModel(4, "Item 3", "https://example.com/image3.jpg")

        )
        blogAdapter.submitList(items)
    }

    private fun setupTrendingRecyclerView(){
        val trendeningRecyclerView = binding.trendingrcy
        trendeningRecyclerView.layoutManager =
            LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        trendingAdapter = trendeningAdapter()
        trendeningRecyclerView.adapter = trendingAdapter
        val items = listOf(
            trendeningModel(1, "Item 1", "https://example.com/image1.jpg"),
            trendeningModel(2, "Item 2", "https://example.com/image2.jpg"),
            trendeningModel(3, "Item 3", "https://example.com/image3.jpg"),
            trendeningModel(4, "Item 3", "https://example.com/image3.jpg"),
            trendeningModel(1, "Item 1", "https://example.com/image1.jpg"),
            trendeningModel(2, "Item 2", "https://example.com/image2.jpg"),
            trendeningModel(3, "Item 3", "https://example.com/image3.jpg"),
            trendeningModel(4, "Item 3", "https://example.com/image3.jpg")

        )
        trendingAdapter.submitList(items)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(scrollRunnable)
    }

    fun mainFrameChange() {
        mainFrameChange?.mainFrameChange()
    }


}