package com.raleighnc.imapsmobile.basemaps

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.raleighnc.imapsmobile.HideableBottomSheetState
import com.raleighnc.imapsmobile.MapViewModel
import com.raleighnc.imapsmobile.TopBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BaseMaps(
    mapViewModel: MapViewModel, baseMapsViewModel: BaseMapsViewModel,
    bottomSheetState: HideableBottomSheetState
) {
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            baseMapsViewModel.getPortalGroup("f6329364e80c438a958ce74aadc3a89f")
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = "Basemaps",
                bottomSheetState = bottomSheetState,
                coroutineScope = coroutineScope,
                navController = null
            )
        },
        bottomBar = {
            BasemapButtonBar(baseMapsViewModel, coroutineScope)
        }
    ) {
        Box(
            modifier = Modifier.padding(
                bottom = it.calculateBottomPadding() + 20.dp,
                top = it.calculateTopPadding()
            )
        ) {
            BasemapList(
                baseMapsViewModel = baseMapsViewModel,
                mapViewModel = mapViewModel,
                coroutineScope = coroutineScope
            )
        }
    }
}

@Composable
fun BasemapButtonBar(baseMapsViewModel: BaseMapsViewModel, coroutineScope: CoroutineScope) {
    val selected = baseMapsViewModel.selected.collectAsState()

    BottomAppBar(
        modifier = Modifier.padding(10.dp), containerColor = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                modifier = Modifier,
                onClick = {
                    coroutineScope.launch {
                        baseMapsViewModel.basemapGroupChanged(BasemapGroup.MAPS)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    contentColor = if (selected.value == BasemapGroup.MAPS) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface,
                    containerColor = if (selected.value == BasemapGroup.MAPS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                ),

                ) {
                Text(text = "Maps")
            }
            Button(
                onClick = {
                    coroutineScope.launch {
                        baseMapsViewModel.basemapGroupChanged(BasemapGroup.IMAGES)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    contentColor = if (selected.value == BasemapGroup.IMAGES) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface,
                    containerColor = if (selected.value == BasemapGroup.IMAGES) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,

                    )

            ) {
                Text(text = "Images")
            }
            Button(
                onClick = {
                    coroutineScope.launch {
                        baseMapsViewModel.basemapGroupChanged(BasemapGroup.ESRI)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    contentColor = if (selected.value == BasemapGroup.ESRI) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface,
                    containerColor = if (selected.value == BasemapGroup.ESRI) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,

                    ),
            ) {
                Text(text = "Esri")
            }

        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasemapList(
    baseMapsViewModel: BaseMapsViewModel,
    mapViewModel: MapViewModel,
    coroutineScope: CoroutineScope
) {
    val maps = baseMapsViewModel.maps.collectAsState()
    val selectedBasemap = baseMapsViewModel.selectedBasemap.collectAsState()
    val inRaleigh = baseMapsViewModel.inRaleigh.collectAsState()
    val selectedGroup = baseMapsViewModel.selected.collectAsState()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(maps.value.filter {
            selectedGroup.value != BasemapGroup.IMAGES || (inRaleigh.value || it.tags.contains(
                "countywide"
            ))
        }) { item ->
            if (item.thumbnail != null) {
                OutlinedCard(
                    modifier = Modifier
                        .height(170.dp)
                        .padding(6.dp),
                    onClick = {
                        Log.i("test", item.title)
                        coroutineScope.launch {
                            baseMapsViewModel.changeBasemap(item, mapViewModel.map)
                        }
                    },
                    border = if (selectedBasemap.value == item.itemId) BorderStroke(
                        2.dp,
                        MaterialTheme.colorScheme.primary
                    ) else BorderStroke(10.dp, Color.Transparent),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )

                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        AsyncImage(
                            model = item.thumbnail?.uri,
                            contentDescription = item.title,
                            modifier = Modifier.fillMaxWidth()
                        )
                        //Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = item.title,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier
                                .wrapContentSize()
                                .padding(10.dp)
                        )

                    }
                }
            }
        }
    }
}
