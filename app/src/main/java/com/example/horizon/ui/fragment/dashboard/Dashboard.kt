package com.example.horizon.ui.fragment.dashboard

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
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
import com.example.horizon.repository.UserDetails
import com.example.horizon.ui.activity.HybridVideoPlayer
import com.example.horizon.ui.fragment.dashboard.adapter.bannerAdapter
import com.example.horizon.ui.fragment.dashboard.adapter.blogAdapter
import com.example.horizon.ui.fragment.diary.NewDiaryHandler
import com.example.horizon.ui.fragment.diary.adapter.diaryAdapter
import com.example.horizon.ui.fragment.diary.diaryHandler
import com.example.horizon.ui.fragment.login.login
import com.example.horizon.ui.fragment.peerconnect.Connect
import com.example.horizon.utils.Helper
import com.example.horizon.utils.MySharedPrefrence
import com.example.horizon.utils.WebSocketManager
import com.example.horizon.viewmodel.DiaryViewModel
import com.google.gson.Gson
import org.json.JSONObject

class Dashboard: Fragment() ,diaryAdapter.OnItemClickListener{
    private var mainFrameChange: mainFrameChange? = null
    private lateinit var  binding : FragmentDashboardBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var bannerAdapter: bannerAdapter
    private lateinit var blogAdapter: blogAdapter
    private lateinit var diaryAdapter: diaryAdapter
    private lateinit var diaryViewModel: DiaryViewModel

    val helper = Helper()
    val gson = Gson()
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
        binding.meditationrcy.playtime.setOnClickListener {
            val iframe = """<iframe 
                src="https://www.youtube.com/embed/tgbNymZ7vqY" 
                frameborder="0" 
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" 
                allowfullscreen>
            </iframe>""".trimIndent()
            val intent = Intent(requireActivity(), HybridVideoPlayer::class.java)
            intent.putExtra("iframe_url", iframe)
            startActivity(intent)
        }
        binding.peerMeetingrcy.videobtn.setOnClickListener {
            helper.replacetoDashboardFragment(Connect(),requireFragmentManager())
            val preference = MySharedPrefrence()

            val userJsonString = preference.getUserDetail(requireContext())

            val user: UserDetails? = try {
                if (!userJsonString.isNullOrEmpty()) {
                    gson.fromJson(userJsonString, UserDetails::class.java)
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

            if (user != null) {
                println("User name: ${user.username}")
            } else {
                println("No user data found")
            }

            Log.e("user", user.toString())
            WebSocketManager.connect(
                username = user?.username ?: "unknown",
                onConnected = {
                    Log.d("MainActivity", "WebSocket connected successfully")
                },
                onError = { errorMessage ->
                    Log.e("MainActivity", "WebSocket connection failed: $errorMessage")
                }
            )
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
            bannerModel(5, "Focus", R.drawable.focus_logo, "#013220"),
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
            blogModel(1, "Journal", R.drawable.journal_icon),
            blogModel(2, "Library", R.drawable.library_icon),
        )
        blogAdapter.submitList(items)
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