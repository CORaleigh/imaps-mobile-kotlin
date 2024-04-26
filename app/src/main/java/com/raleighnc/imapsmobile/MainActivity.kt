package com.raleighnc.imapsmobile

//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import com.raleighnc.imapsmobile.ui.theme.IMAPSMobileTheme


import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.arcgismaps.ApiKey
import com.arcgismaps.ArcGISEnvironment
import com.raleighnc.imapsmobile.ui.theme.IMAPSMobileTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ArcGISEnvironment.apiKey =
            ApiKey.create("AAPK5a3922f5cd3d458a9fc5281430dc0c21qf2ndlLZpV5klkmHjZXDoFsziUl3txws6ahr2JHVHzHUpSdO2uBEgzzava-mGOiM")
        ArcGISEnvironment.applicationContext = applicationContext

        setContent {
            IMAPSMobileTheme {
                MainScreen(this@MainActivity)

            }

        }
    }

}


