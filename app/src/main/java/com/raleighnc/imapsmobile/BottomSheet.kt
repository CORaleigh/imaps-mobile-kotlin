package com.raleighnc.imapsmobile

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arcgismaps.data.FeatureTable
import com.raleighnc.imapsmobile.basemaps.BaseMaps
import com.raleighnc.imapsmobile.basemaps.BaseMapsViewModel
import com.raleighnc.imapsmobile.layers.LayerList
import com.raleighnc.imapsmobile.search.SearchScreen
import com.raleighnc.imapsmobile.search.SearchViewModel


@Composable
fun BottomSheet(
    bottomSheetState: HideableBottomSheetState,
    mapViewModel: MapViewModel,
    baseMapsViewModel: BaseMapsViewModel,
    selectedPanel: Panels,
    searchViewModel: SearchViewModel
) {
    Log.i("test", selectedPanel.name)
    val isSearching by searchViewModel.isSearching.collectAsState()
    val selectedProperty by mapViewModel.selectedProperty.collectAsState()
    LaunchedEffect(isSearching) {
        if (isSearching) {
            bottomSheetState.expand()
        }
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (selectedPanel == Panels.SEARCH) {
                val table: FeatureTable? =
                    mapViewModel.map.tables.find { it.displayName.contains("Condo") }
                if (table != null) {
                    SearchScreen(searchViewModel = searchViewModel, mapViewModel = mapViewModel, bottomSheetState = bottomSheetState)
                }
            }
//            if (selectedPanel == Panels.PROPERTYINFO && selectedProperty != null) {
//                selectedProperty?.let {
//                    PropertyInfo(
//                        selectedProperty = it,
//                        mapViewModel = mapViewModel,
//                        bottomSheetState = bottomSheetState
//                    )
//                }
//            }
            if (selectedPanel == Panels.LAYERS) {
                LayerList(mapViewModel = mapViewModel, bottomSheetState = bottomSheetState)
            }
            if (selectedPanel == Panels.BASEMAP) {
                BaseMaps(mapViewModel = mapViewModel, baseMapsViewModel = baseMapsViewModel, bottomSheetState = bottomSheetState)
            }
        }
    }

}

enum class Panels {
    SEARCH, PROPERTYINFO, LAYERS, BASEMAP, NONE
}


