package com.raleighnc.imapsmobile.layers

import android.app.Application
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Segment
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.layers.GroupLayer
import com.arcgismaps.mapping.layers.Layer
import com.raleighnc.imapsmobile.HideableBottomSheetState
import com.raleighnc.imapsmobile.MapViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayerListTopBar(
    title: String,
    bottomSheetState: HideableBottomSheetState,
    coroutineScope: CoroutineScope,
    navController: NavController?,
    mapViewModel: MapViewModel,
    expandedLayers: MutableList<Layer>,
    addExpandedLayer: (Layer) -> Unit,
    resetLayerList: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val application = LocalContext.current.applicationContext as Application

    val sharedPreferences = application.getSharedPreferences("imaps_prefs", Context.MODE_PRIVATE)

    CenterAlignedTopAppBar(title = {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .size(30.dp, 5.dp)
                    .background(Color.Transparent)
                    .background(Color.LightGray, shape = RoundedCornerShape(5.dp))
            )
            Spacer(
                modifier = Modifier
                    .height(5.dp)

            )

            Text(
                title,
                style = MaterialTheme.typography.titleMedium

            )
        }
    },
        navigationIcon = {
            if (navController?.previousBackStackEntry != null) {
                IconButton(onClick = {
                    navController.popBackStack()
                }) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowLeft,
                        contentDescription = "Localized description"
                    )
                }
            }

        },
        colors =  TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        actions = {
            IconButton(onClick = { menuExpanded = !menuExpanded }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "More",
                )
            }
            // 5
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                // 6
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Segment,
                                contentDescription = "Expand Layers",

                                )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Expand Layers")
                        }
                    },
                    onClick = {
                        expandAllLayers(mapViewModel.map, addExpandedLayer)
                        menuExpanded = false

                    },
                )
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.List,
                                contentDescription = "Collapse Layers",

                                )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Collapse Layers")
                        }
                    },
                    onClick = {
                        expandedLayers.removeAll(expandedLayers)
                        menuExpanded = false

                    },
                )
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Reset Layers",

                                )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Reset Layers")
                        }
                    },
                    onClick = {
                        coroutineScope.launch {
                            val editor = sharedPreferences.edit()
                            editor.remove("visibleLayers")
                            editor.apply()
                            mapViewModel.setLayerVisibility(mapViewModel.map)
                            resetLayerList()
                            menuExpanded = false
                        }
                    })

            }
            IconButton(onClick = {
                coroutineScope.launch {
                    bottomSheetState.hide()
                }
            }) {
                Icon(Icons.Filled.Close, contentDescription = "Close")
            }
        }
    )


}

fun expandAllLayers(
    map: ArcGISMap,
    addExpandedLayer: (Layer) -> Unit
) {
    fun expandLayer(layer: Layer) {
        if (layer is GroupLayer) {
            addExpandedLayer(layer)
            layer.layers.forEach { subLayer ->
                expandLayer(subLayer)
            }
        }
    }
    map.operationalLayers.forEach(::expandLayer)

}
