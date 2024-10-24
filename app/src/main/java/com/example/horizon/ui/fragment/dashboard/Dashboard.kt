package com.example.horizon.ui.fragment.dashboard

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.horizon.Interface.mainFrameChange
import androidx.lifecycle.Observer
import com.example.horizon.R
import com.example.horizon.databinding.FragmentDashboardBinding
import com.example.horizon.model.bannerModel
import com.example.horizon.model.blogModel
import com.example.horizon.model.entities.diary
import com.example.horizon.model.trendeningModel
import com.example.horizon.ui.fragment.dashboard.adapter.bannerAdapter
import com.example.horizon.ui.fragment.dashboard.adapter.blogAdapter
import com.example.horizon.ui.fragment.dashboard.adapter.trendeningAdapter
import com.example.horizon.ui.fragment.diary.NewDiaryHandler
import com.example.horizon.ui.fragment.diary.adapter.diaryAdapter
import com.example.horizon.ui.fragment.diary.diaryHandler
import com.example.horizon.ui.fragment.login.login
import com.example.horizon.utils.Helper
import com.example.horizon.viewmodel.DiaryViewModel

class Dashboard: Fragment() ,diaryAdapter.OnItemClickListener{
    private var mainFrameChange: mainFrameChange? = null
    private lateinit var  binding : FragmentDashboardBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var bannerAdapter: bannerAdapter
    private lateinit var blogAdapter: blogAdapter
    private lateinit var trendingAdapter: trendeningAdapter
    private lateinit var diaryAdapter: diaryAdapter
    private lateinit var diaryViewModel: DiaryViewModel


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
        binding.viewAllDiary.setOnClickListener {
            helper.replacetoDashboardFragment(diaryHandler(),requireFragmentManager())
        }
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
        initRecyclerViewHandler()
    }

    fun initRecyclerViewHandler(){
        setupBannerRecyclerView()
        setupBlogsRecyclerView()
        setupTrendingRecyclerView()
        setupDiaryRecylerView()
    }

    private fun setupBannerRecyclerView() {
        recyclerView = binding.banner
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        bannerAdapter = bannerAdapter()
        recyclerView.adapter = bannerAdapter
        val items = listOf(
            bannerModel(1, "Happy", R.drawable.smile, "#EF5DA8"),
            bannerModel(2, "Calm", R.drawable.calm, "#AEAFF7"),
            bannerModel(3, "Manic", R.drawable.relax, "#A0E3E2"),
            bannerModel(4, "Angry", R.drawable.angry, "#F09E54"),
            bannerModel(5, "Angry", R.drawable.relax, "#C3F2A6"),
            bannerModel(6, "Item 3", R.drawable.relax, "#A0E3E2"),
        )
        bannerAdapter.submitList(items)
        handler.postDelayed(scrollRunnable, 3000)
    }

    private fun setupDiaryRecylerView(){
        recyclerView = binding.diaryRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        diaryAdapter = diaryAdapter(this)
        recyclerView.adapter = diaryAdapter
        diaryViewModel = ViewModelProvider(this).get(DiaryViewModel::class.java)
        diaryViewModel.allDiaries.observe(viewLifecycleOwner, Observer { diaries ->
            diaries?.let {
                diaryAdapter.setDiaries(it)
            }
        })
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

    override fun onItemClick(diary: diary) {
        val fragment = NewDiaryHandler().apply {
            arguments = Bundle().apply {
                putInt("DIARY_ID", diary.id)
            }
        }
        helper.replacetoDashboardFragment(fragment, requireActivity().supportFragmentManager)
    }
}