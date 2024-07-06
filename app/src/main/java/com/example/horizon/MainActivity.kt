package com.example.horizon

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.horizon.Interface.FrameLayoutChanger
import com.example.horizon.databinding.ActivityMainBinding
import com.example.horizon.privacy.DeveloperOption
import com.example.horizon.privacy.Rooted
import com.example.horizon.ui.fragment.onboarding.onboardingFragment
import com.example.horizon.utils.Helper
import com.example.horizon.utils.Internet_connectivity
import timber.log.Timber

class MainActivity : AppCompatActivity() , FrameLayoutChanger{
    lateinit var binding: ActivityMainBinding
    private lateinit var frameLayout: FrameLayout
    private lateinit var dashboardContainer : FrameLayout
    var rooted_ = Rooted()
    var developerOption: DeveloperOption = DeveloperOption()
    var helper = Helper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handleVisibilty()
        val developerOptionsEnabled =developerOption.isDeveloperOptionsEnabled(applicationContext)
        //root check condition
        if(!rooted_.isRootedDevice()){
            //Developer option check
            Timber.tag("developer_option").e("${developerOptionsEnabled}")
            if(developerOptionsEnabled) {
                Timber.tag("Developeroption").e("Developer option is On")
                helper.replaceFragment(onboardingFragment(),supportFragmentManager)
            }
            else{
                Timber.tag("Developeroption").e("Developer option is Off")
                Timber.tag("PhoneStatus").e("Phone is not Rooted")
            }
        }else{
            Timber.tag("PhoneStatus").e("Phone is Rooted")
        }
        val internetConnectivity = Internet_connectivity()
        val context = applicationContext
        if (internetConnectivity.isNetworkAvailable(context)) {
            // Internet is available
            // Perform network-related tasks here
        } else {
            // No internet connection
            // Handle the case where there is no internet connectivity
        }
    }

    fun handleVisibilty(){
        binding.dashcnt.visibility = View.GONE
        binding.frameLayout.visibility = View.VISIBLE
    }

    override fun replaceFrameLayout() {
        binding.frameLayout.visibility = View.GONE
        binding.dashcnt.visibility = View.VISIBLE
        binding.DashboardContainer.visibility = View.VISIBLE
    }
    fun showDashboardContainer() {
        // Show DashboardContainer and hide frameLayout
        binding.frameLayout.visibility = View.GONE
        binding.dashcnt.visibility = View.VISIBLE
        binding.DashboardContainer.visibility = View.VISIBLE
    }
}