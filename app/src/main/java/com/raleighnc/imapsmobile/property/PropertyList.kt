package com.raleighnc.imapsmobile.property

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.arcgismaps.data.ArcGISFeature
import com.raleighnc.imapsmobile.HideableBottomSheetState
import com.raleighnc.imapsmobile.MapViewModel
import com.raleighnc.imapsmobile.TopBar

@Composable
fun PropertyList (
    mapViewModel: MapViewModel,
    bottomSheetState: HideableBottomSheetState,
    navController: NavController
) {
    val selectedProperty by mapViewModel.selectedProperty.collectAsState()
    val selectedProperties by mapViewModel.selectedProperties.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TopBar(
                title = "Property List",
                bottomSheetState = bottomSheetState,
                coroutineScope = coroutineScope,
                navController = navController
            )
        }
    ) {
        LazyColumn (modifier = Modifier.padding(it).fillMaxSize()) {
            items(selectedProperties) { feature ->
                Column (modifier = Modifier.padding(30.dp).clickable(onClick = {
                    mapViewModel.selectProperty(feature as ArcGISFeature)
                }), horizontalAlignment = Alignment.Start) {
                    Text(
                        text = feature.attributes["OWNER"]?.toString().orEmpty(),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    Text(
                        text = feature.attributes["SITE_ADDRESS"]?.toString().orEmpty(),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                    )

                }
            }
        }
    }
}