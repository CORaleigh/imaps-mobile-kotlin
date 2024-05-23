package com.raleighnc.imapsmobile.popups

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.arcgismaps.mapping.layers.Layer
import com.raleighnc.imapsmobile.HideableBottomSheetState
import com.raleighnc.imapsmobile.MapViewModel
import com.raleighnc.imapsmobile.PopupView

@Composable
fun PopupList(
    mapViewModel: MapViewModel,
    bottomSheetState: HideableBottomSheetState,
    popupSelected: (PopupView) -> Unit
) {
    val popupViews by mapViewModel.popupViews.collectAsState()
    val expandedLayers = remember { mutableStateListOf<Layer>() }


    LaunchedEffect (popupViews) {
        if (popupViews.isEmpty()) {
            bottomSheetState.hide()
        }
        popupViews.groupBy { it.layer }.toList().forEach { (layer, groupedPopupViews) ->
            if (layer != null) {
                expandedLayers.add(layer)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        item {
            val grouped = popupViews.groupBy { it.layer }.toList()
            grouped.forEach { (layer, groupedPopupViews) ->

                if (layer != null) {
                    Column {
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
                                })
                        )  {
                            Text(
                                text = AnnotatedString("${layer.name} (${groupedPopupViews.count()})"),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                            IconButton(onClick = {
                                if (expandedLayers.contains(layer)) {
                                    expandedLayers.remove(layer)
                                } else {
                                    expandedLayers.add(layer)
                                }
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
                        }
                        AnimatedVisibility(
                            visible = expandedLayers.contains(layer), modifier = Modifier
                                .animateContentSize()
                                .padding(start = 10.dp)
                        ) {

                            Column {
                                groupedPopupViews.forEach { popupView ->
                                    Row {
                                        Row (modifier = Modifier.fillMaxWidth()
                                            .clickable {
                                                popupSelected(popupView)
                                            },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(popupView.title, modifier = Modifier

                                                .wrapContentWidth(align = Alignment.Start)
                                                .weight(1f)
                                                .padding(16.dp)
                                            )
                                                Icon(
                                                    Icons.Filled.KeyboardArrowRight,
                                                    contentDescription = "View Feature",
                                                    tint = MaterialTheme.colorScheme.onSurface
                                                )

                                        }

                                    }
                                }
                            }
                        }

                    }


                }
            }
        }
    }
}