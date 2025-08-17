package com.example.horizon.ui.fragment.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import com.example.horizon.R
import com.example.horizon.databinding.FragmentChatBinding

class chat : Fragment() {
    private lateinit var _binding: FragmentChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatBinding.inflate(layoutInflater)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToggleButtons()
        showAsPeerContent()
    }

    private fun setupToggleButtons() {
        _binding.asPeerToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                _binding.favouritesToggle.isChecked = false
                showAsPeerContent()
                updateToggleButtonStyles(true, false)
            }
        }

        // Favourites toggle button click listener
        _binding.favouritesToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                _binding.asPeerToggle.isChecked = false
                showFavouritesContent()
                updateToggleButtonStyles(false, true)
            }
        }

        // Handle case where both toggles become unchecked
        _binding.asPeerToggle.setOnClickListener {
            if (!_binding.asPeerToggle.isChecked && !_binding.favouritesToggle.isChecked) {
                _binding.asPeerToggle.isChecked = true
            }
        }

        _binding.favouritesToggle.setOnClickListener {
            if (!_binding.asPeerToggle.isChecked && !_binding.favouritesToggle.isChecked) {
                _binding.favouritesToggle.isChecked = true
            }
        }
    }

    private fun showAsPeerContent() {
        _binding.asPeerContent.visibility = View.VISIBLE
        _binding.favouritesContent.visibility = View.GONE
    }

    private fun showFavouritesContent() {
        _binding.asPeerContent.visibility = View.GONE
        _binding.favouritesContent.visibility = View.VISIBLE
    }

    private fun updateToggleButtonStyles(asPeerActive: Boolean, favouritesActive: Boolean) {
        if (asPeerActive) {
            _binding.asPeerToggle.setTextColor(ContextCompat.getColor(requireContext(), R.color.active_text_color))
            _binding.asPeerToggle.setBackgroundResource(R.drawable.toggle_background_selected)
        } else {
            _binding.asPeerToggle.setTextColor(ContextCompat.getColor(requireContext(), R.color.inactive_text_color))
            _binding.asPeerToggle.setBackgroundResource(R.drawable.toggle_background_unselected)
        }
        if (favouritesActive) {
            _binding.favouritesToggle.setTextColor(ContextCompat.getColor(requireContext(), R.color.active_text_color))
            _binding.favouritesToggle.setBackgroundResource(R.drawable.toggle_background_selected)
        } else {
            _binding.favouritesToggle.setTextColor(ContextCompat.getColor(requireContext(), R.color.inactive_text_color))
            _binding.favouritesToggle.setBackgroundResource(R.drawable.toggle_background_unselected)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding.asPeerToggle.setOnCheckedChangeListener(null)
        _binding.favouritesToggle.setOnCheckedChangeListener(null)
        _binding.asPeerToggle.setOnClickListener(null)
        _binding.favouritesToggle.setOnClickListener(null)
    }
}