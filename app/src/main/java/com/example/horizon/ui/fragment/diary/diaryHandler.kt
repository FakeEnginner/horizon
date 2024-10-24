package com.example.horizon.ui.fragment.diary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.horizon.Interface.mainFrameChange
import com.example.horizon.databinding.FragmentDiaryBinding
import com.example.horizon.model.entities.diary
import com.example.horizon.ui.fragment.dashboard.Dashboard
import com.example.horizon.ui.fragment.diary.adapter.diaryAdapter
import com.example.horizon.utils.Helper
import com.example.horizon.viewmodel.DiaryViewModel


class diaryHandler :Fragment(), diaryAdapter.OnItemClickListener {

    private lateinit var fragmentDiaryBinding: FragmentDiaryBinding
    private var mainFrameChange: mainFrameChange? = null
    private lateinit var diaryViewModel: DiaryViewModel
    private lateinit var diaryAdapter: diaryAdapter

    val helper = Helper()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
       fragmentDiaryBinding = FragmentDiaryBinding.inflate(layoutInflater,container,false)

        val recyclerView = fragmentDiaryBinding.diaryRecyclerView
        diaryAdapter = diaryAdapter(this)
        recyclerView.adapter = diaryAdapter
//        recyclerView.layoutManager = LinearLayoutManager(context)
        val staggeredGridLayoutManager =
            StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        recyclerView.layoutManager = staggeredGridLayoutManager
        diaryViewModel = ViewModelProvider(this).get(DiaryViewModel::class.java)

        // get all diary from database
        diaryViewModel.allDiaries.observe(viewLifecycleOwner, Observer { diaries ->
            diaries?.let {
                diaryAdapter.setDiaries(it)
            }
        })
        //search functionality
        fragmentDiaryBinding.editTextText.addTextChangedListener { text ->
            val query = text.toString()
            diaryViewModel.searchDiaries(query).observe(viewLifecycleOwner, Observer { diaries ->
                diaries?.let {
                    diaryAdapter.setDiaries(it)
                }
            })
        }

        //add new diary
        fragmentDiaryBinding.fab.setOnClickListener {
            mainFrameChange()
            helper.replacetoDashboardFragment(NewDiaryHandler(),requireFragmentManager())
        }
        return fragmentDiaryBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                helper.replacetoDashboardFragment(Dashboard(),requireFragmentManager())
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this,callback)
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
        helper.replacetoDashboardFragment(fragment, requireFragmentManager())
    }

}