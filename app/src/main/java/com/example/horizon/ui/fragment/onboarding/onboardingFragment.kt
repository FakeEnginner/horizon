package com.example.horizon.ui.fragment.onboarding

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.horizon.Interface.OnboardingDataListener
import com.example.horizon.MyApplication
import com.example.horizon.R
import com.example.horizon.databinding.FragmentOnboardingBinding
import com.example.horizon.factory.OnBoardingCheckViewModelFactory
import com.example.horizon.model.onBoardingCheck
import com.example.horizon.ui.fragment.login.login
import com.example.horizon.ui.fragment.onboarding.adapter.OnboardingAdapter
import com.example.horizon.ui.fragment.onboarding.models.OnboardingPage
import com.example.horizon.utils.Helper
import com.example.horizon.viewModel.OnBoardingCheckViewModel
import org.json.JSONObject

class onboardingFragment() : Fragment(), OnboardingDataListener {

    override val imageResourceMap: Map<String, Int> = mapOf(
        "image1" to R.drawable.anxiety, // replace with actual drawable resource IDs
        "image2" to R.drawable.yoga
    )

    private lateinit var binding: FragmentOnboardingBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var onboardingAdapter: OnboardingAdapter
    private val helper = Helper()
    private val onBoardingCheckViewModel: OnBoardingCheckViewModel by viewModels {
        OnBoardingCheckViewModelFactory((requireActivity().application as MyApplication).database)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = binding.viewPager
        val dotsIndicator = binding.dotsIndicator

        // Sample JSON data for onboarding screens
        val defaultConfig = """
            {
                "onboarding": [
                    { "image": "image1", "description": "Welcome to the first page of onboarding." },
                    { "image": "image2", "description": "Learn more about how to use our app." }
                ]
            }
        """.trimIndent()

        onBoardingDataReceived(defaultConfig)

        // Next button functionality
        binding.btnNext.setOnClickListener {
            val nextItem = viewPager.currentItem + 1
            if (nextItem < onboardingAdapter.itemCount) {
                viewPager.setCurrentItem(nextItem, true)
            }
        }

        // Skip button functionality
        binding.skipedbtn.setOnClickListener {
            val onBoardingCheck = onBoardingCheck(onBoardingCheck = true)
            onBoardingCheckViewModel.insertOnBoardingCheck(onBoardingCheck)
            helper.replaceFragment(login(), requireFragmentManager())
        }
    }

    override fun onBoardingDataReceived(jsonString: String) {
        val onboardingPages = mutableListOf<OnboardingPage>()
        val jsonObject = JSONObject(jsonString)
        val jsonArray = jsonObject.getJSONArray("onboarding")

        for (i in 0 until jsonArray.length()) {
            val pageObject = jsonArray.getJSONObject(i)
            val imageId = pageObject.getString("image")
            val description = pageObject.getString("description")
            val imageResId = imageResourceMap[imageId] ?: R.drawable.yoga

            val onboardingPage = OnboardingPage(imageResId, description)
            onboardingPages.add(onboardingPage)
        }

        onboardingAdapter = OnboardingAdapter(onboardingPages)
        viewPager.adapter = onboardingAdapter
        binding.dotsIndicator.setViewPager2(viewPager)
    }

    override fun onFetchFailed() {
        Log.e(TAG, "Fetching onboarding data failed")
    }
}
