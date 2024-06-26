package com.example.horizon.ui.fragment.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.horizon.R
import com.example.horizon.ui.fragment.onboarding.adapter.OnboardingAdapter
import com.example.horizon.ui.fragment.onboarding.models.OnboardingPage
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator

class onboardingFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var onboardingAdapter: OnboardingAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_onboarding, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = view.findViewById(R.id.viewPager)
        onboardingAdapter = OnboardingAdapter(createOnboardingPages())
        viewPager.adapter = onboardingAdapter

        val dotsIndicator = view.findViewById<DotsIndicator>(R.id.dots_indicator)
        dotsIndicator.setViewPager2(viewPager)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {

            }
        })

        view.findViewById<Button>(R.id.btnNext).setOnClickListener {
            val nextItem = viewPager.currentItem + 1
            if (nextItem < onboardingAdapter.itemCount) {
                viewPager.setCurrentItem(nextItem, true)
            } else {
                view.findViewById<Button>(R.id.btnNext).setText("Finish")
            }
        }
    }

    private fun createOnboardingPages(): List<OnboardingPage> {
        return listOf(
            OnboardingPage(R.drawable.ic_launcher_background, getString(R.string.app_name)),
            OnboardingPage(R.drawable.ic_launcher_foreground, getString(R.string.app_name)),
            OnboardingPage(R.drawable.ic_launcher_foreground, getString(R.string.app_name))
        )
    }
}
