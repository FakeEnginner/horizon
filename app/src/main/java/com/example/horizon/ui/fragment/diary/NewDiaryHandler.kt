package com.example.horizon.ui.fragment.diary

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.horizon.Interface.FrameLayoutChanger
import com.example.horizon.MainActivity
import com.example.horizon.R
import com.example.horizon.databinding.FragmentCreateDiaryBinding
import com.example.horizon.model.entities.diary
import com.example.horizon.ui.fragment.diary.adapter.diaryAdapter
import com.example.horizon.utils.Helper
import com.example.horizon.viewmodel.DiaryViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.util.Date
import java.util.Locale

class NewDiaryHandler : Fragment() {
    private lateinit var createDiaryBinding: FragmentCreateDiaryBinding
    private var frameLayoutChanger: FrameLayoutChanger? = null
    private lateinit var diaryViewModel: DiaryViewModel
    private val helper = Helper()
    private var selectedColor: String = "#D3D3D3" // Default color
    private lateinit var viewSubtitleIndicator: View
    private lateinit var diaryAdapter: diaryAdapter
    private lateinit var imageViews: List<ImageView>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        createDiaryBinding = FragmentCreateDiaryBinding.inflate(layoutInflater, container, false)
        diaryViewModel = ViewModelProvider(this).get(DiaryViewModel::class.java)

        val diaryId = arguments?.getInt("DIARY_ID")
        Log.d("NewDiaryHandler", "Received Diary ID: $diaryId")
        diaryId?.let {
            diaryViewModel.getDiaryById(it).observe(viewLifecycleOwner, Observer { diary ->
                diary?.let {
                    createDiaryBinding.inputdiaryTitle.setText(diary.title)
                    createDiaryBinding.inputdiarySubtitle.setText(diary.subtitle)
                    createDiaryBinding.inputNote.setText(diary.noteText)
                    createDiaryBinding.textDateTime.setText(diary.dateTime)
                }
            })
        }

        createDiaryBinding.imageback.setOnClickListener {
            helper.replacetoDashboardFragment(diaryHandler(),requireFragmentManager())
        }
        createDiaryBinding.imagesave.setOnClickListener {
            saveDiary()
            helper.replacetoDashboardFragment(diaryHandler(),requireFragmentManager())
        }
        viewSubtitleIndicator = createDiaryBinding.viewSubtitleIndicator
        initMiscellenous()
        setSubTitleIndicator()

        val formatter = createSimpleDateFormatter("yyyy-MM-dd HH:mm:ss")
        createDiaryBinding.textDateTime.setText(formatter.format(Date()))

        return createDiaryBinding.root
    }

    private fun createSimpleDateFormatter(pattern: String, locale: Locale = Locale.getDefault()): SimpleDateFormat {
        return SimpleDateFormat(pattern, locale)
    }

    private fun saveDiary() {
        val diary = diary(
            id = 0,
            title = createDiaryBinding.inputdiaryTitle.text.toString(),
            subtitle = createDiaryBinding.inputdiarySubtitle.text.toString(),
            noteText = createDiaryBinding.inputNote.text.toString(),
            dateTime = createDiaryBinding.textDateTime.text.toString(),
            imagePath = "",
            color = selectedColor, // Store the selected color
            webLink = ""
        )
        diaryViewModel.insert(diary)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (activity is FrameLayoutChanger) {
            frameLayoutChanger = activity as FrameLayoutChanger
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToDashboard()
            }
        })
    }

    private fun navigateToDashboard() {
        frameLayoutChanger?.replaceFrameLayout()
        helper.replacetoDashboardFragment(NewDiaryHandler(), requireFragmentManager())
        (requireActivity() as MainActivity).showDashboardContainer()
    }

    private fun initMiscellenous() {
        val layoutMiscellenousRoot = createDiaryBinding.layoutMiscellenous.root
        val bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellenousRoot)

        createDiaryBinding.layoutMiscellenous.miscellenousTxt.setOnClickListener {
            bottomSheetBehavior.state = if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                BottomSheetBehavior.STATE_COLLAPSED
            } else {
                BottomSheetBehavior.STATE_EXPANDED
            }
        }

        imageViews = listOf(
            layoutMiscellenousRoot.findViewById(R.id.imagecolor1),
            layoutMiscellenousRoot.findViewById(R.id.imagecolor2),
            layoutMiscellenousRoot.findViewById(R.id.imagecolor3),
            layoutMiscellenousRoot.findViewById(R.id.imagecolor4),
            layoutMiscellenousRoot.findViewById(R.id.imagecolor5)
        )

        setupColorClickListeners(layoutMiscellenousRoot)
    }

    private fun setupColorClickListeners(layoutMiscellenousRoot: View) {
        val colorMap = mapOf(
            R.id.viewcolor_1 to "#D3D3D3",
            R.id.viewcolor_2 to "#ffea99",
            R.id.viewcolor_3 to "#FFA500",
            R.id.viewcolor_4 to "#000080",
            R.id.viewcolor_5 to "#ff2c2c"
        )

        colorMap.forEach { (viewId, color) ->
            layoutMiscellenousRoot.findViewById<View>(viewId).setOnClickListener {
                selectColor(color)
            }
        }
    }

    private fun selectColor(color: String) {
        selectedColor = color

        imageViews.forEachIndexed { index, imageView ->
            imageView.setImageResource(if (color == selectedColor) R.drawable.icon_done else 0)
        }
        setSubTitleIndicator()
    }

    private fun setSubTitleIndicator() {
        val gradientIndication = viewSubtitleIndicator.background as? GradientDrawable
        gradientIndication?.setColorFilter(Color.parseColor(selectedColor), PorterDuff.Mode.SRC_IN)
            ?: Log.e("Error", "Background is not a GradientDrawable")
    }
}