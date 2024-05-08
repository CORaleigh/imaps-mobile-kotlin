package com.raleighnc.imapsmobile

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arcgismaps.mapping.Viewpoint
import com.arcgismaps.toolkit.geoviewcompose.MapView
import com.raleighnc.imapsmobile.HideableBottomSheetValue.Hidden
import com.raleighnc.imapsmobile.basemaps.BaseMapsViewModel
import com.raleighnc.imapsmobile.search.SearchViewModel
import kotlinx.coroutines.launch

@Composable
fun MainScreen(mainActivity: MainActivity) {

    val bottomSheetState = rememberHideableBottomSheetState(initialValue = Hidden)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val application = LocalContext.current.applicationContext as Application
    val sharedPreferences = application.getSharedPreferences("imaps_prefs", Context.MODE_PRIVATE)
    val mapViewModel = remember { MapViewModel(application, coroutineScope, sharedPreferences) }
    val baseMapsViewModel = remember { BaseMapsViewModel(application) }

    val selectedProperty = mapViewModel.selectedProperty.collectAsState()
    val searchViewModel = remember { SearchViewModel() }
    val selectedPanel = remember { mutableStateOf(Panels.NONE) }
    val locationEnabled = mapViewModel.locationEnabled.collectAsState()

    val popupViews by mapViewModel.popupViews.collectAsState()
    val isLoaded by mapViewModel.isLoaded.collectAsState()


    if (selectedProperty.value != null) {
        Log.i("map press", selectedProperty.value?.attributes.toString())
    }
    LaunchedEffect(Unit) {
        mapViewModel.selectedProperty.collect {
            if (mapViewModel.selectedProperty.value != null) {
                selectedPanel.value = Panels.SEARCH
                bottomSheetState.show()

            }

        }
    }

    LaunchedEffect(popupViews) {
        if (popupViews.isNotEmpty()) {
            selectedPanel.value = Panels.POPUP
            bottomSheetState.expand()
        }

    }

    LaunchedEffect(locationEnabled.value) {
        Log.i("test", locationEnabled.value.toString())
        if (locationEnabled.value) {
            mapViewModel.displayLocation(context, mainActivity)
        } else {
            mapViewModel.stopDisplayingLocation()
        }

    }

    HideableBottomSheetScaffold(
        bottomSheetState = bottomSheetState,
        bottomSheetContent = {
            BottomSheet(
                bottomSheetState,
                mapViewModel,
                baseMapsViewModel,
                selectedPanel.value,
                searchViewModel
            )
        },
        modifier = Modifier.fillMaxSize()
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()

        ) {
            MapView(
                modifier = Modifier.fillMaxSize(),
                arcGISMap = mapViewModel.map,
                mapViewProxy = mapViewModel.mapViewProxy,
                onViewpointChangedForCenterAndScale = { viewpoint: Viewpoint ->
                    val editor = sharedPreferences.edit()
                    editor.putString("viewpoint", viewpoint.toJson())
                    editor.apply()
                    coroutineScope.launch {
                        baseMapsViewModel.mapExtentUpdated(viewpoint)
                    }
                },
                onLongPress = { longPressConfirmedEvent ->
                    coroutineScope.launch {
                        mapViewModel.onMapLongPress(longPressConfirmedEvent.mapPoint)
                    }
                },
                onSingleTapConfirmed = { singleTapConfirmedEvent ->
                    coroutineScope.launch {
                        mapViewModel.onSingleTap(singleTapConfirmedEvent.screenCoordinate)
                    }
                },
                locationDisplay = mapViewModel.locationDisplay
            )

            //Spacer(modifier = Modifier.height(32.dp))
            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp),
                containerColor = MaterialTheme.colorScheme.background,
                onClick = {

                    coroutineScope.launch {
                        if (isLoaded) {
                            mapViewModel.locationButtonClicked()
                        }
                    }

                }) {
                Icon(
                    Icons.Filled.MyLocation,
                    contentDescription = "display location",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
                containerColor = MaterialTheme.colorScheme.background,
                onClick = {
                    coroutineScope.launch {
                        if (isLoaded) {
                            selectedPanel.value = Panels.SEARCH
                            bottomSheetState.expand()
                        }
                    }

                }) {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = stringResource(id = R.string.search_content_desc),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .absoluteOffset(y = 65.dp),
                containerColor = MaterialTheme.colorScheme.background,
                onClick = {
                    coroutineScope.launch {
                        if (isLoaded) {
                            selectedPanel.value = Panels.LAYERS
                            bottomSheetState.expand()
                        }
                    }

                }) {
                Icon(
                    Icons.Filled.Layers,
                    contentDescription = stringResource(id = R.string.layers_content_desc),
                    tint = MaterialTheme.colorScheme.onBackground
                )

            }
            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .absoluteOffset(y = 130.dp),
                containerColor = MaterialTheme.colorScheme.background,
                onClick = {
                    coroutineScope.launch {
                        if (isLoaded) {
                            selectedPanel.value = Panels.BASEMAP
                            bottomSheetState.expand()
                        }

                    }

                }) {
                Icon(
                    Icons.Filled.Map,
                    contentDescription = stringResource(id = R.string.layers_content_desc),
                    tint = MaterialTheme.colorScheme.onBackground
                )

            }
        }
    }
}

