package com.raleighnc.imapsmobile

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.arcgismaps.Color
import com.arcgismaps.mapping.Viewpoint
import com.arcgismaps.mapping.view.BackgroundGrid
import com.arcgismaps.toolkit.geoviewcompose.MapView
import com.raleighnc.imapsmobile.HideableBottomSheetValue.Hidden
import com.raleighnc.imapsmobile.basemaps.BaseMapsViewModel
import com.raleighnc.imapsmobile.search.SearchViewModel
import kotlinx.coroutines.CoroutineScope
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


    val searchViewModel = remember { SearchViewModel() }
    val selectedPanel = remember { mutableStateOf(Panels.NONE) }
    val locationEnabled by mapViewModel.locationEnabled.collectAsState()

    val popupViews by mapViewModel.popupViews.collectAsState()
    val isLoaded by mapViewModel.isLoaded.collectAsState()
    val graphics by mapViewModel.graphics.collectAsState()
    val darkMode by rememberSaveable { mutableStateOf(false) }
    var isLargerThanPhone = checkIsLargerThanPhone()
    LaunchedEffect (darkMode) {
        if (bottomSheetState.isVisible) {
            bottomSheetState.hide()

        }

    }


    LaunchedEffect(Unit) {
        mapViewModel.selectedProperty.collect {
            if (mapViewModel.selectedProperty.value != null) {
                selectedPanel.value = Panels.SEARCH
                if (bottomSheetState.isHalfExpanded) {
                    bottomSheetState.halfExpand()
                }
                if (bottomSheetState.isExpanded) {
                    bottomSheetState.expand()
                }
            }
        }
    }

    LaunchedEffect(popupViews) {
        if (popupViews.isNotEmpty()) {
            Log.i("test", popupViews.count().toString())
            selectedPanel.value = Panels.POPUP
            if (bottomSheetState.isHidden) {
                bottomSheetState.show()
                if (isLargerThanPhone) {
                    bottomSheetState.expand()
                }
            }
            if (bottomSheetState.isHalfExpanded) {
                bottomSheetState.halfExpand()
            }
            if (bottomSheetState.isExpanded) {
                bottomSheetState.expand()
            }


        }

    }

    LaunchedEffect(locationEnabled) {
        if (locationEnabled) {
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
                .background(MaterialTheme.colorScheme.background)

        ) {
            MapView(
                modifier = Modifier.fillMaxSize(),
                backgroundGrid = BackgroundGrid(Color.transparent),
                arcGISMap = mapViewModel.map,
                mapViewProxy = mapViewModel.mapViewProxy,
                graphicsOverlays = graphics,
                onViewpointChangedForCenterAndScale = { viewpoint: Viewpoint ->
                    coroutineScope.launch {
                        val editor = sharedPreferences.edit()
                        editor.putString("viewpoint", viewpoint.toJson())
                        editor.apply()
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
                        tint = if (locationEnabled) { MaterialTheme.colorScheme.primary } else {MaterialTheme.colorScheme.onBackground }
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
                                showBottomsheet(bottomSheetState, coroutineScope)

                            }
                        }

                    }) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "Search",
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
                                showBottomsheet(bottomSheetState, coroutineScope)
                            }
                        }

                    }) {
                    Icon(
                        Icons.Filled.Layers,
                        contentDescription = "Layers",
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
                                showBottomsheet(bottomSheetState, coroutineScope)
                            }

                        }

                    }) {
                    Icon(
                        Icons.Filled.Map,
                        contentDescription = "Basemaps",
                        tint = MaterialTheme.colorScheme.onBackground
                    )

                }
        }
    }
}

fun showBottomsheet(bottomSheetState: HideableBottomSheetState, coroutineScope: CoroutineScope) {
    coroutineScope.launch {
        if (bottomSheetState.isHalfExpanded) {
            bottomSheetState.halfExpand()
        }
        if (bottomSheetState.isExpanded) {
            bottomSheetState.expand()
        }
        if (bottomSheetState.isHidden) {
            bottomSheetState.expand()
        }
    }

}

@Composable
fun checkIsLargerThanPhone(): Boolean {
    val configuration = LocalConfiguration.current
    val smallestWidthDp = configuration.smallestScreenWidthDp
    return smallestWidthDp >= 600 // Adjust the threshold as needed
}