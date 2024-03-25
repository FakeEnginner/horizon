package com.example.horizon

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.horizon.privacy.DeveloperOption
import com.example.horizon.privacy.Rooted
import com.example.horizon.utils.Helper
import com.example.horizon.utils.Internet_connectivity
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    var rooted_ = Rooted()
    var developerOption: DeveloperOption = DeveloperOption()
    var helper = Helper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val developerOptionsEnabled =developerOption.isDeveloperOptionsEnabled(applicationContext)
        //root check condition
        if(!rooted_.isRootedDevice()){
            //Developer option check
            Timber.tag("developer_option").e("${developerOptionsEnabled}")
            if(developerOptionsEnabled) {
                Timber.tag("Developeroption").e("Developer option is On")
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
}