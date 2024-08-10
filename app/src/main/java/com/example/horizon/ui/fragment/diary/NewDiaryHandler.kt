package com.example.horizon.ui.fragment.diary

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.horizon.Interface.FrameLayoutChanger
import com.example.horizon.MainActivity
import com.example.horizon.databinding.FragmentCreateDiaryBinding
import com.example.horizon.model.entities.diary
import com.example.horizon.ui.fragment.dashboard.Dashboard
import com.example.horizon.ui.fragment.diary.adapter.diaryAdapter
import com.example.horizon.utils.Helper
import com.example.horizon.viewmodel.DiaryViewModel
import java.util.Date
import java.util.Locale

class NewDiaryHandler : Fragment(){
    private lateinit var createDiaryBinding: FragmentCreateDiaryBinding
    private var frameLayoutChanger: FrameLayoutChanger? = null
    private lateinit var diaryViewModel: DiaryViewModel
    val helper = Helper()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        createDiaryBinding = FragmentCreateDiaryBinding.inflate(layoutInflater,container,false)
        diaryViewModel = ViewModelProvider(this).get(DiaryViewModel::class.java)

        val diaryId = arguments?.getInt("DIARY_ID")
        Log.d("NewDiaryHandler", "Received Diary ID: $diaryId")
        if (diaryId != null) {
            diaryViewModel.getDiaryById(diaryId).observe(viewLifecycleOwner, Observer { diary ->
                diary?.let {
                    createDiaryBinding.inputdiaryTitle.setText(diary.title)
                    createDiaryBinding.inputdiarySubtitle.setText(diary.subtitle)
                    createDiaryBinding.inputNote.setText(diary.noteText)
                    createDiaryBinding.textDateTime.setText(diary.dateTime)
                }
            })
        }

        createDiaryBinding.imageback.setOnClickListener {
            navigateToDashboard()
        }
        createDiaryBinding.imagesave.setOnClickListener {
            setDiary()
            helper?.replacetoDashboardFragment(diaryHandler(),requireFragmentManager());
        }
        val formatter = createSimpleDateFormatter("yyyy-MM-dd HH:mm:ss")
        val currentDate = Date()
        createDiaryBinding.textDateTime.setText(
            formatter.format(currentDate)
        )
        createDiaryBinding.inputdiaryTitle.setOnClickListener {  }
        return createDiaryBinding.root
    }
    fun createSimpleDateFormatter(pattern: String, locale: Locale = Locale.getDefault()): SimpleDateFormat {
        return SimpleDateFormat(pattern, locale)
    }

    fun setDiary(){
      val title =  createDiaryBinding.inputdiaryTitle.text.toString()
      val subtitle=   createDiaryBinding.inputdiarySubtitle.text.toString()
      val noteText=  createDiaryBinding.inputNote.text.toString()
       val dateTime = createDiaryBinding.textDateTime.text.toString()

        val diary = diary(
            id = 0,
            title = title,
            subtitle = subtitle,
            noteText = noteText,
            dateTime = dateTime,
            imagePath = "",
            color = "",
            webLink = ""
        )
        diaryViewModel.insert(diary)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(activity is FrameLayoutChanger){
            frameLayoutChanger = activity as FrameLayoutChanger
        }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToDashboard()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this,callback)
    }
    private fun navigateToDashboard() {
        frameLayoutChanger?.replaceFrameLayout()
        helper?.replacetoDashboardFragment(NewDiaryHandler(),requireFragmentManager())
        (requireActivity() as MainActivity).showDashboardContainer()
    }
}