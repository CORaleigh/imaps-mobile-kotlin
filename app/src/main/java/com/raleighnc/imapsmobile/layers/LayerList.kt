package com.raleighnc.imapsmobile.layers

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.arcgismaps.mapping.layers.GroupLayer
import com.arcgismaps.mapping.layers.Layer
import com.raleighnc.imapsmobile.HideableBottomSheetState
import com.raleighnc.imapsmobile.MapViewModel
import com.raleighnc.imapsmobile.TopBar

@Composable
fun LayerList(mapViewModel: MapViewModel,
              bottomSheetState: HideableBottomSheetState
) {
    val application = LocalContext.current.applicationContext as Application
    val sharedPreferences = application.getSharedPreferences("imaps_prefs", Context.MODE_PRIVATE)
    val coroutineScope = rememberCoroutineScope()
    val navController = rememberNavController()
    val layerClicked = remember { mutableStateOf<Layer?>(null) }
    val expandedLayers = remember { mutableListOf<Layer>() }
    LaunchedEffect(layerClicked.value) {
        if (layerClicked.value != null) {
            navController.navigate(LayerScreens.LAYERINFO.name)
        }
    }
    NavHost(
        navController = navController,
        startDestination = LayerScreens.LAYERLIST.name
    ) {
        composable(LayerScreens.LAYERLIST.name) {
            Scaffold(
                topBar = {
                    TopBar(
                        title = "Layers",
                        bottomSheetState = bottomSheetState,
                        coroutineScope = coroutineScope,
                        navController = navController
                    )
                }
            ) {
                Box(modifier = Modifier.padding( top = it.calculateTopPadding(), bottom = it.calculateBottomPadding() + 20.dp)) {
                    LazyColumn (modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 30.dp, end = 30.dp)) {
                        item {
                            for (layer in mapViewModel.map.operationalLayers.reversed()) {
                                layer.isVisible = true
                                SubLayer(layer = layer, layerClicked = layerClicked, expandedLayers = expandedLayers)
                            }
                        }
                    }
                }
            }
        }
        composable(LayerScreens.LAYERINFO.name) {
            Scaffold(
                topBar = {
                    TopBar(
                        title = "Layer Info",
                        bottomSheetState = bottomSheetState,
                        coroutineScope = coroutineScope,
                        navController = navController
                    )
                }
            ) {
                Box(modifier = Modifier.padding(it)) {
                    layerClicked.value?.let { layer -> LayerInfo(layer = layer) }
                }
            }
        }
    }

}

@Composable
fun SubLayer(layer: Layer, layerClicked: MutableState<Layer?>, expandedLayers: MutableList<Layer>) {
    var isExpanded by remember { mutableStateOf(expandedLayers.contains(layer)) }
    var isVisible by remember { mutableStateOf(layer.isVisible) }

    val subLayerContent = layer.subLayerContents.collectAsState()

    Column  {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = {
                    if (expandedLayers.contains(layer)) {
                        expandedLayers.remove(layer)
                    } else {
                        expandedLayers.add(layer)
                    }
                    isExpanded = !isExpanded
                })
        ) {
            if (layer is GroupLayer) {
                Text(
                    text = AnnotatedString(layer.name),
                    style = MaterialTheme.typography.bodyMedium
                )

                IconButton(onClick = {
                    if (expandedLayers.contains(layer)) {
                        expandedLayers.remove(layer)
                    } else {
                        expandedLayers.add(layer)
                    }
                    isExpanded = !isExpanded
                }) {
                    if (expandedLayers.contains(layer)) {
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Expanded",
                            tint = MaterialTheme.colorScheme.onSurface
                        )

                    } else {
                        Icon(
                            Icons.Filled.KeyboardArrowRight,
                            contentDescription = "Collapsed",
                            tint = MaterialTheme.colorScheme.onSurface
                        )

                    }

                }
            } else {
                Text(text = layer.name, modifier = Modifier
                    .wrapContentWidth(align = Alignment.Start)
                    .weight(1f))
                Spacer(modifier = Modifier.width(16.dp)) // Optional: Add spacing between text components
                Switch(checked = isVisible, onCheckedChange = {
                    isVisible = it
                    layer.isVisible = isVisible
                    Log.i("LayerVisibility", "Layer ${layer.name} visibility set to $it")

                })
                IconButton(onClick = { layerClicked.value = layer }) {
                    Icon(
                        Icons.Filled.KeyboardArrowRight,
                        contentDescription = "Collapsed",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }


        }
        AnimatedVisibility(
            visible = isExpanded, modifier = Modifier
                .animateContentSize()
                .padding(start = 10.dp)
        ) {

            Column {
                if (subLayerContent != null) {
                    for (subLayer in subLayerContent.value.reversed()) {
                        SubLayer(layer = subLayer as Layer, layerClicked = layerClicked, expandedLayers = expandedLayers)
                    }
                }
            }
        }
    }
}

enum class LayerScreens {
    LAYERLIST,
    LAYERINFO
}