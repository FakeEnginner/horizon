package com.example.horizon.ui.fragment.onboarding

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.horizon.Interface.OnboardingDataListener
import com.example.horizon.Interface.dao.onBoardingDao
import com.example.horizon.MyApplication
import com.example.horizon.R
import com.example.horizon.databinding.FragmentOnboardingBinding
import com.example.horizon.factory.OnBoardingCheckViewModelFactory
import com.example.horizon.model.onBoardingCheck
import com.example.horizon.ui.fragment.login.login
import com.example.horizon.ui.fragment.onboarding.adapter.OnboardingAdapter
import com.example.horizon.ui.fragment.onboarding.models.OnboardingPage
import com.example.horizon.utils.Helper
import com.example.horizon.utils.firebaseConfig
import com.example.horizon.viewModel.OnBoardingCheckViewModel
import com.google.gson.Gson
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import org.json.JSONObject

class onboardingFragment : Fragment(), OnboardingDataListener {

    private lateinit var viewPager: ViewPager2
    private lateinit var onboardingAdapter: OnboardingAdapter
    lateinit var onboardingBinding: FragmentOnboardingBinding
    private lateinit var onBoardingCheckViewModel: OnBoardingCheckViewModel
    private lateinit var onBoardingDao: onBoardingDao
    var helper = Helper()
    var firebaseConfig = firebaseConfig()
     lateinit var dotsIndicator : DotsIndicator



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
        firebaseConfig.setOnboardingDataListener(this)
        firebaseConfig.firebaseConfig(requireContext())
        viewPager = view.findViewById(R.id.viewPager)
        dotsIndicator = view.findViewById<DotsIndicator>(R.id.dots_indicator)

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


    override fun onBoardingDataReceived(jsonString: String) {

        if (!jsonString.isNullOrBlank()) {
            val gson = Gson()
            val jsonObject = JSONObject(jsonString)
            val jsonArray = jsonObject.getJSONArray("onboarding")
            val onboardingPages = mutableListOf<OnboardingPage>()

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val image = jsonObject.getString("image")
                val description = jsonObject.getString("description")

                // Create OnboardingPage object and add to list
                val onboardingPage = OnboardingPage(image, description)
                onboardingPages.add(onboardingPage)
            }

            onboardingAdapter = OnboardingAdapter(onboardingPages)
            viewPager.adapter = onboardingAdapter
            dotsIndicator.setViewPager2(viewPager)
        }

    }

    override fun onFetchFailed() {
        Log.e(TAG, "Fetching onboarding data failed")
    }
}
