package com.raleighnc.imapsmobile.property

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.arcgismaps.data.ArcGISFeature
import com.raleighnc.imapsmobile.HideableBottomSheetState
import com.raleighnc.imapsmobile.MapViewModel
import com.raleighnc.imapsmobile.TopBar

@Composable
fun PropertyInfo(
    selectedProperty: ArcGISFeature,
    mapViewModel: MapViewModel,
    bottomSheetState: HideableBottomSheetState,
    navController: NavController,
    showServices: () -> Unit,
    showDeed: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var address = selectedProperty.attributes["SITE_ADDRESS"]?.toString().orEmpty()
    var misc = selectedProperty.attributes["STMISC"]?.toString().orEmpty()
    Log.i("test", misc)
    if (misc == "1/2") {
        address = address.replaceFirst(" ", " 1/2 ")
    } else if (misc != "") {
        address += " $misc"
    }

    // val webViewUrl: MutableState<String> = remember { mutableStateOf("") }
    Scaffold(
        topBar = {
            TopBar(
                title = "Property",
                bottomSheetState = bottomSheetState,
                coroutineScope = coroutineScope,
                navController = navController
            )
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(it)
        )
        {
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp)) {

                Text(
                    text = address,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()

                )
                GeneralInfo(selectedProperty = selectedProperty)
                OwnerInfo(selectedProperty = selectedProperty)
                ValuationInfo(selectedProperty = selectedProperty)
                SaleInfo(selectedProperty = selectedProperty)
                DeedInfo(selectedProperty = selectedProperty, mapViewModel = mapViewModel, showDeed = showDeed)
                BuildingInfo(selectedProperty = selectedProperty)
                Button(
                    onClick = {
                        showServices()
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp)
                ) {
                    Text("Services")
                }
                PropertyPhotos(selectedProperty = selectedProperty, mapViewModel = mapViewModel)

            }

        }
    }
}

