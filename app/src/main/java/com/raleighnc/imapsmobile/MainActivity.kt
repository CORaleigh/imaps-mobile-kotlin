package com.raleighnc.imapsmobile

//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import com.raleighnc.imapsmobile.ui.theme.IMAPSMobileTheme


import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.arcgismaps.ArcGISEnvironment
import com.arcgismaps.LicenseKey
import com.raleighnc.imapsmobile.ui.theme.IMAPSMobileTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val licenseKey =
            LicenseKey.create("runtimelite,1000,rud1508631011,none,XXMFA0PL4S0BNERL1153")
                ?: return showError("Null license key. ")
        val licenseResult = ArcGISEnvironment.setLicense(licenseKey)
        ArcGISEnvironment.applicationContext = applicationContext

        setContent {
            IMAPSMobileTheme {
                MainScreen(this@MainActivity)

            }

        }
    }

    private fun showError(msg: String) {
        Log.e("License error", msg)
    }

}


