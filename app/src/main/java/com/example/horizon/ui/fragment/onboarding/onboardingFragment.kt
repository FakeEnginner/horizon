package com.example.horizon.ui.fragment.onboarding

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.horizon.Interface.OnboardingDataListener
import com.example.horizon.MainActivity
import com.example.horizon.MyApplication
import com.example.horizon.R
import com.example.horizon.databinding.FragmentOnboardingBinding
import com.example.horizon.factory.OnBoardingCheckViewModelFactory
import com.example.horizon.ui.fragment.login.login
import com.example.horizon.ui.fragment.onboarding.adapter.OnboardingAdapter
import com.example.horizon.ui.fragment.onboarding.models.OnboardingPage
import com.example.horizon.utils.Helper
import com.example.horizon.utils.firebaseConfig
import com.example.horizon.viewmodel.OnBoardingCheckViewModel // Fixed: correct package name
import org.json.JSONException
import org.json.JSONObject

class onboardingFragment() : Fragment(), OnboardingDataListener {
    override val imageResourceMap: Map<String, Int> = mapOf(
        "image1" to R.drawable.anxiety,
        "image2" to R.drawable.yoga
    )
    private lateinit var binding: FragmentOnboardingBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var onboardingAdapter: OnboardingAdapter
    private val helper = Helper()
    private lateinit var remoteConfigManager: firebaseConfig

    // Fixed: Proper ViewModel initialization
    private lateinit var onBoardingCheckViewModel: OnBoardingCheckViewModel


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
        onBoardingCheckViewModel = ViewModelProvider(
            this,
            OnBoardingCheckViewModelFactory((requireActivity().application as MyApplication).database)
        )[OnBoardingCheckViewModel::class.java]

        viewPager = binding.viewPager
        val dotsIndicator = binding.dotsIndicator
        remoteConfigManager = firebaseConfig()
        remoteConfigManager.setOnboardingDataListener(this)
        remoteConfigManager.firebaseConfig(requireContext())

        // Sample JSON data for onboarding screens
//        val defaultConfig = """
//            {
//                "onboarding": [
//                    { "image": "image1", "description": "Welcome to the first page of onboarding." },
//                    { "image": "image2", "description": "Learn more about how to use our app." }
//                ]
//            }
//        """.trimIndent()
//        onBoardingDataReceived(defaultConfig)

        // Next button functionality
        binding.btnNext.setOnClickListener {
            val nextItem = viewPager.currentItem + 1
            if (nextItem < onboardingAdapter.itemCount) {
                viewPager.setCurrentItem(nextItem, true)
            }
        }

        binding.skipedbtn.setOnClickListener {
            completeOnboarding()
        }
    }
    private fun completeOnboarding() {
        val sharedPrefs = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean("onboarding_completed", true).apply()
        (requireActivity() as MainActivity).markOnboardingCompleted()
        helper.replaceFragment(login(), requireFragmentManager())
    }

    override fun onBoardingDataReceived(jsonString: String) {
        if (!isAdded) {
            Log.w(TAG, "onBoardingDataReceived called but fragment not attached or binding null.")
            return
        }
        if (jsonString.isEmpty()) {
            handleDataFetchFailure()
            return
        }
        val onboardingPages = mutableListOf<OnboardingPage>()
        try {
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
        } catch (e: JSONException) {
            handleDataFetchFailure()
            return
        }

        if (onboardingPages.isEmpty()) {
            handleDataFetchFailure()
            return
        }
        onboardingAdapter = OnboardingAdapter(onboardingPages)
        viewPager.adapter = onboardingAdapter
        binding.dotsIndicator.setViewPager2(viewPager)
    }

    override fun onFetchFailed() {
        if (!isAdded) {
            return
        }
        handleDataFetchFailure()
    }

    private fun handleDataFetchFailure() {
        val fallbackPages = listOf(
            OnboardingPage(imageResourceMap["image1"] ?: R.drawable.anxiety, "Welcome! (Fallback)"),
            OnboardingPage(imageResourceMap["image2"] ?: R.drawable.yoga, "Discover more. (Fallback)")
        )
        activity?.runOnUiThread {
            if (isAdded) {
                onboardingAdapter = OnboardingAdapter(fallbackPages)
                viewPager.adapter = onboardingAdapter
                binding.dotsIndicator.setViewPager2(viewPager)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        remoteConfigManager.clearListener()
        viewPager.adapter = null
    }
}