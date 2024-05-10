package com.raleighnc.imapsmobile.layers

import android.app.Application
import android.content.Context
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.raleighnc.imapsmobile.LayerListTopBar
import com.raleighnc.imapsmobile.MapViewModel
import com.raleighnc.imapsmobile.TopBar
import com.raleighnc.imapsmobile.expandAllLayers

@Composable
fun LayerList(
    mapViewModel: MapViewModel, bottomSheetState: HideableBottomSheetState
) {
    val coroutineScope = rememberCoroutineScope()
    val navController = rememberNavController()
    val layerClicked = remember { mutableStateOf<Layer?>(null) }
    val expandedLayers = remember { mutableStateListOf<Layer>() }
    var layerSearchText by remember { mutableStateOf("") }

    LaunchedEffect(layerClicked.value) {
        if (layerClicked.value != null) {
            navController.navigate(LayerScreens.LAYERINFO.name)
        }
    }
    NavHost(
        navController = navController, startDestination = LayerScreens.LAYERLIST.name
    ) {
        composable(LayerScreens.LAYERLIST.name) {
            Scaffold(topBar = {
                LayerListTopBar(title = "Layers",
                    bottomSheetState = bottomSheetState,
                    coroutineScope = coroutineScope,
                    navController = navController,
                    mapViewModel = mapViewModel,
                    expandedLayers = expandedLayers,
                    addExpandedLayer = { layer ->
                        expandedLayers.add(layer)
                    },
                    resetLayerList = {
                        expandedLayers.removeAll(expandedLayers)
                    })
            }) {
                Box(
                    modifier = Modifier.padding(
                        top = it.calculateTopPadding(), bottom = it.calculateBottomPadding() + 20.dp
                    )
                ) {
                    Column {
                        TextField(value = layerSearchText,
                            onValueChange = {
                                layerSearchText = it
                                if (it.isEmpty()) {
                                    expandedLayers.removeAll(expandedLayers)
                                } else {
                                    expandAllLayers(mapViewModel.map) {
                                        expandedLayers.add(it)
                                    }
                                }
                            },
                            trailingIcon = {
                                if (layerSearchText.isNotEmpty()) {
                                    Icon(Icons.Default.Clear,
                                        contentDescription = "clear text",
                                        modifier = Modifier.clickable {
                                                layerSearchText = ""
                                                expandedLayers.removeAll(expandedLayers)
                                            })
                                }
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "search")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.tertiary,
                                unfocusedContainerColor = MaterialTheme.colorScheme.tertiary,

                                )
                        )
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 30.dp, end = 30.dp)
                        ) {
                            item {
                                for (layer in mapViewModel.map.operationalLayers.reversed()) {
                                    layer.isVisible = true
                                    SubLayer(
                                        layer = layer,
                                        layerClicked = layerClicked,
                                        expandedLayers = expandedLayers,
                                        layerSearchText = layerSearchText
                                    )
                                }
                            }
                        }
                    }

                }
            }
        }
        composable(LayerScreens.LAYERINFO.name) {
            Scaffold(topBar = {
                TopBar(
                    title = "Layer Info",
                    bottomSheetState = bottomSheetState,
                    coroutineScope = coroutineScope,
                    navController = navController
                )
            }) {
                Box(modifier = Modifier.padding(it)) {
                    layerClicked.value?.let { layer -> LayerInfo(layer = layer) }
                }
            }
        }
    }

}

@Composable
fun SubLayer(
    layer: Layer,
    layerClicked: MutableState<Layer?>,
    expandedLayers: MutableList<Layer>,
    layerSearchText: String
) {
    var isExpanded by remember { mutableStateOf(false) }
    isExpanded = expandedLayers.contains(layer)
    var isVisible by remember { mutableStateOf(layer.isVisible) }
    val application = LocalContext.current.applicationContext as Application
    val sharedPreferences = application.getSharedPreferences("imaps_prefs", Context.MODE_PRIVATE)

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = {
                    if (layer is GroupLayer) {
                        if (expandedLayers.contains(layer)) {
                            expandedLayers.remove(layer)
                        } else {
                            expandedLayers.add(layer)
                        }
                        isExpanded = !isExpanded
                    } else {
                        isVisible = !isVisible
                        layer.isVisible = isVisible
                    }
                })
        ) {
            if (layer is GroupLayer) {
                if (layer.layers.filter {
                        it.name.lowercase().contains(layerSearchText.lowercase())
                    }.isNotEmpty()) {
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
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Expanded",
                                tint = MaterialTheme.colorScheme.onSurface
                            )

                        } else {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Collapsed",
                                tint = MaterialTheme.colorScheme.onSurface
                            )

                        }

                    }
                }
            } else {
                if (layerSearchText.isEmpty() || layer.name.lowercase()
                        .contains(layerSearchText.lowercase())
                ) {
                    Text(
                        text = layer.name,
                        modifier = Modifier
                            .wrapContentWidth(align = Alignment.Start)
                            .weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp)) // Optional: Add spacing between text components
                    Switch(
                        checked = isVisible, onCheckedChange = {
                            isVisible = it
                            layer.isVisible = isVisible
                            val editor = sharedPreferences.edit()
                            if (sharedPreferences.getString("visibleLayers", "") != null) {
                                val layersString =
                                    sharedPreferences.getString("visibleLayers", "").toString()

                                val layers = layersString.split(",").toMutableList()
                                if (!isVisible) {
                                    layers.remove(layer.name)
                                } else {
                                    layers.add(layer.name)
                                }
                                editor.putString(
                                    "visibleLayers",
                                    layers.toString().replace("[", "").replace("]", "")
                                        .replace(", ", ",")
                                )
                            } else {
                                editor.putString("visibleLayers", layer.name)
                            }
                            editor.apply()

                        }, colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.surface,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                            uncheckedTrackColor = MaterialTheme.colorScheme.background,
                        )
                    )
                    IconButton(onClick = { layerClicked.value = layer }) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Collapsed",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

            }


        }
        AnimatedVisibility(
            visible = isExpanded, modifier = Modifier
                .animateContentSize()
                .padding(start = 10.dp)
        ) {

            Column {
                if (layer is GroupLayer) {
                    for (subLayer in layer.layers.reversed()) {
                        SubLayer(
                            layer = subLayer,
                            layerClicked = layerClicked,
                            expandedLayers = expandedLayers,
                            layerSearchText = layerSearchText
                        )
                    }
                }
            }
        }
    }
}

enum class LayerScreens {
    LAYERLIST, LAYERINFO
}