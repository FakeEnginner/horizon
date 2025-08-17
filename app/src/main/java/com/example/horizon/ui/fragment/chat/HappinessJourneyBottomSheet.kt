package com.example.horizon.ui.fragment.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.horizon.databinding.BottomSheetHappinessJourneyBinding

class HappinessJourneyBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetHappinessJourneyBinding
    private var onSelectionListener: OnSelectionListener? = null

    interface OnSelectionListener {
        fun onExpertSelected()
        fun onFriendSelected()
    }

    fun setOnSelectionListener(listener: OnSelectionListener) {
        onSelectionListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetHappinessJourneyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Expert card click
        binding.expertsCard.setOnClickListener {
            onSelectionListener?.onExpertSelected()
            dismiss()
        }

        // Expert button click
        binding.findExpertButton.setOnClickListener {
            onSelectionListener?.onExpertSelected()
            dismiss()
        }

        // Friends card click
        binding.friendsCard.setOnClickListener {
            onSelectionListener?.onFriendSelected()
            dismiss()
        }

        // Friends button click
        binding.findFriendButton.setOnClickListener {
            onSelectionListener?.onFriendSelected()
            dismiss()
        }
    }

    companion object {
        const val TAG = "HappinessJourneyBottomSheet"

        fun newInstance(): HappinessJourneyBottomSheet {
            return HappinessJourneyBottomSheet()
        }
    }
}