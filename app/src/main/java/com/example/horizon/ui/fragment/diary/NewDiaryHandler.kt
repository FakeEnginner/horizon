package com.example.horizon.ui.fragment.diary

import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
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
import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.text.Spannable
import android.text.style.ImageSpan
import android.util.Patterns
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.drawToBitmap
import java.net.URI

class NewDiaryHandler : Fragment() {
    private lateinit var createDiaryBinding: FragmentCreateDiaryBinding
    private var frameLayoutChanger: FrameLayoutChanger? = null
    private lateinit var diaryViewModel: DiaryViewModel
    private val helper = Helper()
    private var selectedColor: String = "#D3D3D3"
    private lateinit var viewSubtitleIndicator: View
    private lateinit var diaryAdapter: diaryAdapter
    private lateinit var imageViews: List<ImageView>
    private val REQUEST_CODE_STORAGE_PERMISSION = 1
    private val REQUEST_CODE_SELECT_IMAGE = 2
    private lateinit var dialogAddUrl: AlertDialog
    private lateinit var dialogDeleteNote: AlertDialog

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
        createDiaryBinding.inputdiaryTitle.requestFocus()
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
            color = selectedColor,
            webLink = ""
        )
        if(createDiaryBinding.layoutWebUrl.visibility === View.VISIBLE){
            diary.webLink = createDiaryBinding.textWebURL.text.toString()
        }
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

        createDiaryBinding.layoutMiscellenous.layoutaddImage.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
            } else {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

            if (permissions.any { ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED }) {
                requestPermissions(permissions, REQUEST_CODE_STORAGE_PERMISSION)
            } else {
                // Permission already granted, proceed with accessing storage
                openImagePicker()
            }
        }

        createDiaryBinding.layoutMiscellenous.layoutaddUrl.setOnClickListener{
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            showAddUrlDialog()
        }
        createDiaryBinding.layoutMiscellenous.layoutDeleteNoteContainer.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            val diaryId = arguments?.getInt("DIARY_ID")
            diaryId?.let {
                diaryViewModel.getDiaryById(it).observe(viewLifecycleOwner) { diary ->
                    diary?.let {
                        showDeleteDialog(diary)
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // All required permissions are granted
                openImagePicker()
            } else {
                // Permission denied
                Toast.makeText(requireContext(), "Storage permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if(intent.resolveActivity(requireActivity().packageManager) != null){
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val selectedImageURI: Uri = uri
                Log.d("Selected URI", selectedImageURI.toString())
                if (selectedImageURI != null) {
                    try {
                        // Open InputStream from URI
                        val inputStream = requireActivity().contentResolver.openInputStream(selectedImageURI)
                        if (inputStream != null) {
                            // Decode the bitmap from the input stream
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            // Ensure the bitmap is not null
                            if (bitmap != null) {
                                // Scale the bitmap to fit within the EditText
                                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 500, 500, false)
                                // Create an ImageSpan from the bitmap
                                val imageSpan = ImageSpan(requireContext(), scaledBitmap)
                                // Get the current text in the EditText
                                val spannableText = createDiaryBinding.inputNote.text
                                // Check if there is any text in the EditText
                                if (spannableText.isNotEmpty()) {
                                    // If text exists, insert the image span at the current cursor position
                                    val cursorPosition = createDiaryBinding.inputNote.selectionStart
                                    // Insert the image span at the cursor position
                                    spannableText.insert(cursorPosition, " ") // First, insert a space so the span is valid
                                    spannableText.setSpan(imageSpan, cursorPosition, cursorPosition + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    // Move cursor to the end of the inserted image span
                                    createDiaryBinding.inputNote.setSelection(cursorPosition + 1)
                                } else {
                                    // If text is empty, append a space and insert the image span
                                    spannableText.append(" ") // Adding a space to the empty text
                                    val cursorPosition = spannableText.length - 1
                                    spannableText.setSpan(imageSpan, cursorPosition, cursorPosition + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    createDiaryBinding.inputNote.setSelection(cursorPosition + 1)
                                }
                            } else {
                                Log.e("Bitmap Error", "Failed to decode the image into a bitmap.")
                            }
                        } else {
                            Log.e("InputStream Error", "Input stream is null.")
                        }
                    } catch (e: Exception) {
                        Log.e("Error", "Error while processing image: ${e.message}")
                        Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
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

    private fun showAddUrlDialog() {
        if (::dialogAddUrl.isInitialized.not()) {
            val builder = AlertDialog.Builder(requireContext())
            val view = layoutInflater.inflate(R.layout.layout_add_url, createDiaryBinding.root, false)

            builder.setView(view)
            dialogAddUrl = builder.create().apply {
                setCancelable(true)
                setCanceledOnTouchOutside(true)
                window?.setBackgroundDrawableResource(android.R.color.transparent)
            }

            val inputUrl = view.findViewById<EditText>(R.id.inputUrl)
            inputUrl.requestFocus()

            view.findViewById<TextView>(R.id.textAdd).setOnClickListener {
                val url = inputUrl.text.toString().trim()
                when {
                    url.isEmpty() -> {
                        Toast.makeText(requireContext(), "Enter URL", Toast.LENGTH_SHORT).show()
                    }
                    !Patterns.WEB_URL.matcher(url).matches() -> {
                        Toast.makeText(requireContext(), "Enter Valid URL", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        createDiaryBinding.textWebURL.text = url
                        createDiaryBinding.layoutWebUrl.visibility = View.VISIBLE
                        dialogAddUrl.dismiss()
                    }
                }
            }

            view.findViewById<TextView>(R.id.textCancel).setOnClickListener {
                dialogAddUrl.dismiss()
            }
        }

        dialogAddUrl.show()
    }


    private fun showDeleteDialog(diary: diary) {
        if (::dialogDeleteNote.isInitialized.not()) {
            val builder = AlertDialog.Builder(requireContext())
            val view = layoutInflater.inflate(R.layout.layout_delete_note, createDiaryBinding.root, false)
            builder.setView(view)
            dialogDeleteNote = builder.create().apply {
                setCancelable(true)
                setCanceledOnTouchOutside(true)
                window?.setBackgroundDrawableResource(android.R.color.transparent)
            }

            view.findViewById<TextView>(R.id.textDelete).setOnClickListener {
                diaryViewModel.delete(diary)
                dialogDeleteNote.dismiss()
                Toast.makeText(requireContext(), "Diary entry deleted", Toast.LENGTH_SHORT).show()
                helper.replacetoDashboardFragment(diaryHandler(),requireFragmentManager())
            }

            view.findViewById<TextView>(R.id.textDeleteCancel).setOnClickListener {
                dialogDeleteNote.dismiss()
            }
        }
        dialogDeleteNote.show()
    }
}