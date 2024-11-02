package com.example.horizon

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.horizon.Interface.FrameLayoutChanger
import com.example.horizon.Interface.mainFrameChange
import com.example.horizon.databinding.ActivityMainBinding
import com.example.horizon.factory.OnBoardingCheckViewModelFactory
import com.example.horizon.model.onBoardingCheck
import com.example.horizon.privacy.DeveloperOption
import com.example.horizon.privacy.Rooted
import com.example.horizon.ui.fragment.login.login
import com.example.horizon.ui.fragment.onboarding.onboardingFragment
import com.example.horizon.utils.Helper
import com.example.horizon.utils.Internet_connectivity
import com.example.horizon.utils.firebaseConfig
import com.example.horizon.viewModel.OnBoardingCheckViewModel
import timber.log.Timber
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : AppCompatActivity(), FrameLayoutChanger, mainFrameChange {

    // View binding and layouts
    private lateinit var binding: ActivityMainBinding
    private lateinit var frameLayout: FrameLayout
    private lateinit var dashboardContainer: FrameLayout

    // Utility and helper instances
    private val rootedCheck = Rooted()
    private val developerOption = DeveloperOption()
    private val helper = Helper()
    private val firebaseConfig = firebaseConfig()
    private val internetConnectivity = Internet_connectivity()

    // ViewModel for OnBoarding
    private val onBoardingCheckViewModel: OnBoardingCheckViewModel by viewModels {
        OnBoardingCheckViewModelFactory((application as MyApplication).database)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup splash screen
        installSplashScreen()

        // Setup binding and layout
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup initial UI visibility
        setupInitialUIVisibility()

        // Check and handle onboarding status
        checkOnBoardingStatus()

        // Check for device security settings
        performSecurityChecks()

        // Check for internet connectivity
        checkInternetConnectivity()
    }

    /**
     * Sets initial visibility of dashboard and frame layout
     */
    private fun setupInitialUIVisibility() {
        binding.dashcnt.visibility = View.GONE
        binding.frameLayout.visibility = View.VISIBLE
    }

    /**
     * Checks the onboarding status and navigates to the appropriate fragment
     */
    private fun checkOnBoardingStatus() {
        val initialOnBoardingCheck = onBoardingCheck(onBoardingCheck = false)

        onBoardingCheckViewModel.getOnBoardingCheckById(0) { onBoardingCheck ->
            if (onBoardingCheck == null) {
                onBoardingCheckViewModel.insertOnBoardingCheck(initialOnBoardingCheck)
                helper.replaceFragment(onboardingFragment(), supportFragmentManager)
            } else {
                onBoardingCheckViewModel.isOnBoardingChecked(0) { isChecked ->
                    val fragment = if (isChecked) login() else onboardingFragment()
                    helper.replaceFragment(fragment, supportFragmentManager)
                }
            }
        }
    }

    /**
     * Performs security checks like root status and developer options
     */
    private fun performSecurityChecks() {
        if (!rootedCheck.isRootedDevice()) {
            val developerOptionsEnabled = developerOption.isDeveloperOptionsEnabled(applicationContext)

            Timber.tag("developer_option").e("$developerOptionsEnabled")
            if (developerOptionsEnabled) {
                Timber.tag("Developeroption").e("Developer option is On")
            } else {
                Timber.tag("Developeroption").e("Developer option is Off")
                Timber.tag("PhoneStatus").e("Phone is not Rooted")
            }
        } else {
            Timber.tag("PhoneStatus").e("Phone is Rooted")
        }
    }

    /**
     * Checks internet connectivity status
     */
    private fun checkInternetConnectivity() {
        if (internetConnectivity.isNetworkAvailable(applicationContext)) {
            // Handle tasks when internet is available
        } else {
            // Handle no internet connectivity
        }
    }

    /**
     * Changes visibility for frame layout and dashboard container
     */
    override fun replaceFrameLayout() {
        changeToDashboardView()
    }

    private fun changeToDashboardView() {
        binding.frameLayout.visibility = View.GONE
        binding.dashcnt.visibility = View.VISIBLE
        binding.DashboardContainer.visibility = View.VISIBLE
    }

    override fun mainFrameChange() {
        binding.frameLayout.visibility = View.VISIBLE
        binding.dashcnt.visibility = View.GONE
        binding.DashboardContainer.visibility = View.GONE
    }

    override fun onBackPressed() {
        if (shouldExitApp()) {
            finishAffinity()
        } else {
            super.onBackPressed()
        }
    }

    /**
     * Checks if the app should exit based on navigation stack
     */
    private fun shouldExitApp(): Boolean {
        return isTaskRoot && supportFragmentManager.backStackEntryCount == 0
    }

    fun showDashboardContainer() {
        changeToDashboardView()
    }
}
