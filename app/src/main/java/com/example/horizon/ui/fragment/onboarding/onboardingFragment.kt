package com.example.horizon.ui.fragment.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.horizon.Interface.onBoardingDao
import com.example.horizon.MyApplication
import com.example.horizon.R
import com.example.horizon.databinding.FragmentOnboardingBinding
import com.example.horizon.factory.OnBoardingCheckViewModelFactory
import com.example.horizon.model.Database.AppDatabase
import com.example.horizon.model.onBoardingCheck
import com.example.horizon.ui.fragment.login.login
import com.example.horizon.ui.fragment.onboarding.adapter.OnboardingAdapter
import com.example.horizon.ui.fragment.onboarding.models.OnboardingPage
import com.example.horizon.utils.Helper
import com.example.horizon.viewModel.OnBoardingCheckViewModel
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator

class onboardingFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var onboardingAdapter: OnboardingAdapter
    lateinit var onboardingBinding: FragmentOnboardingBinding
    private lateinit var onBoardingCheckViewModel: OnBoardingCheckViewModel
    private lateinit var onBoardingDao: onBoardingDao
    var helper = Helper()




    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_onboarding, container, false)
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = view.findViewById(R.id.viewPager)
        onboardingAdapter = OnboardingAdapter(createOnboardingPages())
        viewPager.adapter = onboardingAdapter

        val dotsIndicator = view.findViewById<DotsIndicator>(R.id.dots_indicator)
        dotsIndicator.setViewPager2(viewPager)

        val application = requireActivity().application as MyApplication
        val viewModelFactory = OnBoardingCheckViewModelFactory(application.database)

        onBoardingCheckViewModel = ViewModelProvider(this, viewModelFactory)
            .get(OnBoardingCheckViewModel::class.java)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {

            }
        })

        view.findViewById<ImageView>(R.id.btnNext).setOnClickListener {
            val nextItem = viewPager.currentItem + 1
            if (nextItem < onboardingAdapter.itemCount) {
                viewPager.setCurrentItem(nextItem, true)
            } else {
            }
        }
        view.findViewById<ImageView>(R.id.skipedbtn).setOnClickListener{
            val onBoardingCheck = onBoardingCheck(onBoardingCheck = true)
            onBoardingCheckViewModel.insertOnBoardingCheck(onBoardingCheck)
            helper.replaceFragment(login(),requireFragmentManager())
        }
    }

    private fun createOnboardingPages(): List<OnboardingPage> {
        return listOf(
            OnboardingPage(R.drawable.yoga, getString(R.string.lorem_ipsum)),
            OnboardingPage(R.drawable.yoga, getString(R.string.lorem_ipsum)),
            OnboardingPage(R.drawable.yoga, getString(R.string.lorem_ipsum))
        )
    }
}
