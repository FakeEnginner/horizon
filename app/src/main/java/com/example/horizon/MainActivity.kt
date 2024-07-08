package com.example.horizon

import android.content.res.Resources.Theme
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.ViewModel
import com.example.horizon.Interface.FrameLayoutChanger
import com.example.horizon.databinding.ActivityMainBinding
import com.example.horizon.factory.OnBoardingCheckViewModelFactory
import com.example.horizon.model.onBoardingCheck
import com.example.horizon.privacy.DeveloperOption
import com.example.horizon.privacy.Rooted
import com.example.horizon.ui.fragment.onboarding.onboardingFragment
import com.example.horizon.utils.Helper
import com.example.horizon.utils.Internet_connectivity
import com.example.horizon.viewModel.OnBoardingCheckViewModel
import timber.log.Timber
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen


class MainActivity : AppCompatActivity() , FrameLayoutChanger{
    lateinit var binding: ActivityMainBinding
    private lateinit var frameLayout: FrameLayout
    private lateinit var dashboardContainer : FrameLayout
    var rooted_ = Rooted()
    var developerOption: DeveloperOption = DeveloperOption()
    var helper = Helper()

    private val onBoardingCheckViewModel: OnBoardingCheckViewModel by viewModels  {
        OnBoardingCheckViewModelFactory((application as MyApplication).database)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.sleep(3000)
        installSplashScreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        /*
        * for handle visibilty of of dashboard and framelayout
        * */
        handleVisibilty()
        val developerOptionsEnabled =developerOption.isDeveloperOptionsEnabled(applicationContext)

       val onBoardingCheck = onBoardingCheck(onBoardingCheck = true)
        onBoardingCheckViewModel.insertOnBoardingCheck(onBoardingCheck)
        onBoardingCheckViewModel.isOnBoardingChecked(0) { isChecked ->
            if(isChecked){

            }
            else {

            }
        }
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

    /*
    * for replacing fragment (from layout visibility gone)
    * function override bcz of interface is implemented
    */
    override fun replaceFrameLayout() {
        frameChange()
    }
    fun showDashboardContainer() {
        frameChange()
    }

   fun  frameChange(){
       binding.frameLayout.visibility = View.GONE
       binding.dashcnt.visibility = View.VISIBLE
       binding.DashboardContainer.visibility = View.VISIBLE
    }
}