package com.raleighnc.imapsmobile


import android.util.Log
import android.view.ViewTreeObserver
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.arcgismaps.data.FeatureTable
import com.raleighnc.imapsmobile.basemaps.BaseMaps
import com.raleighnc.imapsmobile.basemaps.BaseMapsViewModel
import com.raleighnc.imapsmobile.layers.LayerList
import com.raleighnc.imapsmobile.popups.PopupScreen
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
    val isSearching by searchViewModel.isSearching.collectAsState()
    val imeState = rememberImeState()


    LaunchedEffect(key1 = imeState.value) {
        if (imeState.value) {
            bottomSheetState.expand()
        }
    }
    LaunchedEffect(isSearching) {
        if (isSearching) {
            bottomSheetState.expand()
        }
    }




    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding()
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()

        ) {
            if (selectedPanel == Panels.SEARCH) {
                val table: FeatureTable? =
                    mapViewModel.map.tables.find { it.displayName.contains("Condo") }
                if (table != null) {
                    SearchScreen(
                        searchViewModel = searchViewModel,
                        mapViewModel = mapViewModel,
                        bottomSheetState = bottomSheetState
                    )
                }
            }
            if (selectedPanel == Panels.LAYERS) {
                LayerList(mapViewModel = mapViewModel, bottomSheetState = bottomSheetState)
            }
            if (selectedPanel == Panels.BASEMAP) {
                BaseMaps(
                    mapViewModel = mapViewModel,
                    baseMapsViewModel = baseMapsViewModel,
                    bottomSheetState = bottomSheetState
                )
            }
            if (selectedPanel == Panels.POPUP) {
                Log.i("test", "POPUP")
                PopupScreen(mapViewModel = mapViewModel, bottomSheetState = bottomSheetState)
            }
        }
    }

}

enum class Panels {
    SEARCH, PROPERTYINFO, LAYERS, BASEMAP, POPUP, NONE
}


@Composable
fun rememberImeState(): State<Boolean> {
    val imeState = remember {
        mutableStateOf(false)
    }

    val view = LocalView.current
    DisposableEffect(view) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val isKeyboardOpen = ViewCompat.getRootWindowInsets(view)
                ?.isVisible(WindowInsetsCompat.Type.ime()) ?: true
            imeState.value = isKeyboardOpen
        }

        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }
    return imeState
}
