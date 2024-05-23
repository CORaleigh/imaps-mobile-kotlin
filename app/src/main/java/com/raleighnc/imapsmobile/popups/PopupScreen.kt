package com.raleighnc.imapsmobile.popups

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.arcgismaps.data.Feature
import com.arcgismaps.mapping.layers.FeatureLayer
import com.arcgismaps.mapping.popup.FieldsPopupElement
import com.arcgismaps.mapping.popup.MediaPopupElement
import com.arcgismaps.mapping.popup.PopupMediaType
import com.arcgismaps.mapping.popup.TextPopupElement
import com.ireward.htmlcompose.HtmlText
import com.raleighnc.imapsmobile.HideableBottomSheetState
import com.raleighnc.imapsmobile.MapViewModel
import com.raleighnc.imapsmobile.PopupView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PopupScreen(
    mapViewModel: MapViewModel,
    bottomSheetState: HideableBottomSheetState
) {
    val popupViews by mapViewModel.popupViews.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val selectedIndex = remember {
        mutableIntStateOf(0)
    }



    var lastLayer by remember { mutableStateOf<FeatureLayer?>(null) }
    //var selectedFeature by remember { mutableStateOf<Feature?>(null) }


    var selectedPopupView by remember { mutableStateOf<PopupView?>(null) }


    LaunchedEffect (popupViews) {
        selectedPopupView = null
        if (popupViews.isEmpty()) {
            bottomSheetState.hide()
        }
    }

//    LaunchedEffect(bottomSheetState.isHidden ) {
//        delay(100)
//        //Log.i("test", lastLayer?.name.toString())
//        lastLayer?.clearSelection()
//
//    }
    LaunchedEffect(bottomSheetState.isHidden ) {
        delay(250)
        if (bottomSheetState.isHidden) {
            lastLayer?.clearSelection()
        }
    }

    DisposableEffect (Unit) {
        onDispose {
            lastLayer?.clearSelection()
        }
    }

    Scaffold(
        topBar = {
            val title = if (selectedPopupView != null) {
                selectedPopupView?.layer?.name ?: ""
            } else {
                "${popupViews.count() ?: 0} Features"
            }
            PopupTopBar(
                title = title,
                selectedPopupView = selectedPopupView,
                popupViewCount = popupViews.count(),
                bottomSheetState = bottomSheetState,
                coroutineScope = coroutineScope,
                returnToList = {
                    selectedPopupView = null
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {

            if (popupViews.isNotEmpty()) {
                if (popupViews.count() == 1) {
                    selectedPopupView = popupViews.first()
                }

                if (popupViews.count() > 1 && selectedPopupView == null) {

                    PopupList(mapViewModel = mapViewModel, bottomSheetState = bottomSheetState, popupSelected = { popupView ->
                        selectedPopupView = popupView
                    })

//                    Row(modifier = Modifier.fillMaxWidth()) {
//                        Icon(
//                            Icons.Filled.ArrowBackIos, "Previous", modifier = Modifier
//                                .clickable {
//                                    if (selectedIndex.intValue == 0) {
//                                        selectedIndex.intValue = popupViews.count() - 1
//                                    } else {
//                                        selectedIndex.intValue -= 1
//                                    }
//                                }
//                                .weight(1f))
//                        Text(
//                            text = "${(selectedIndex.intValue + 1).toString()} of ${
//                                popupViews.count().toString()
//                            }",
//                            textAlign = TextAlign.Center,
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .weight(1f),
//                        )
//                        Icon(
//                            Icons.Filled.ArrowForwardIos, "Next", modifier = Modifier
//                                .clickable {
//                                    if (selectedIndex.intValue == popupViews.count() - 1) {
//                                        selectedIndex.intValue = 0
//                                    } else {
//                                        selectedIndex.intValue += 1
//                                    }
//                                }
//                                .weight(1f))
//                    }
//                    Spacer(modifier = Modifier.height(10.dp))

                }


                //popupViews.getOrNull(selectedIndex.intValue)?.let { popupView ->
                else if (selectedPopupView != null) {
                    lastLayer?.clearSelection()
                    var popupView = selectedPopupView
                    if (popupView?.geoElement is Feature) {
                        val feature = popupView?.geoElement as Feature
                        TextButton (
                            onClick = { zoomTo(coroutineScope, feature, mapViewModel) }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ZoomIn,
                                contentDescription = "zoom to",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.size(ButtonDefaults.IconSize))
                            Text("Zoom To", style = TextStyle(color=MaterialTheme.colorScheme.onBackground))
                        }
                    }
                    if (popupView?.geoElement is Feature && popupView?.layer is FeatureLayer) {

                        // Safely cast geoElement and layer
                        val geoElement = popupView?.geoElement as Feature
                        val featureLayer = popupView?.layer as FeatureLayer

                        // Clear selection
                        featureLayer.clearSelection()
                        lastLayer = featureLayer
                        // Select the feature
                        featureLayer.selectFeature(geoElement)
                    }

                    popupView?.title?.let { it1 ->
                        Text(
                            it1,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                //}
                LazyColumn(modifier=Modifier.padding(16.dp)) {
                    popupViews.getOrNull(selectedIndex.intValue)?.let { popupView ->
                        items(popupView.popupElements) { element ->
                            if (element is FieldsPopupElement) {
                                element.labels.forEachIndexed { index, label ->
                                    if (element.fields[index].isVisible) {

                                        Row(
                                            modifier = Modifier
                                                .padding(10.dp)
                                                .fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = label, modifier = Modifier.weight(1f))
                                            if (element.formattedValues[index].startsWith("https://")) {
                                                Text(
                                                    text = "View",
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable {
                                                            uriHandler.openUri(element.formattedValues[index])
                                                        },
                                                    style = TextStyle(
                                                        MaterialTheme.colorScheme.primary,
                                                        textDecoration = TextDecoration.Underline
                                                    )
                                                )
                                            } else {
                                                Text(
                                                    text = element.formattedValues[index],
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }

                                        }
                                    }
                                }
                            }
                            if (element is MediaPopupElement) {
                                element.media.forEach { media ->
                                    if (media.type == PopupMediaType.Image) {
                                        AsyncImage(
                                            model = media.value?.sourceUrl,
                                            contentDescription = media.title,
                                            contentScale = ContentScale.FillWidth,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }
                                }
                            }
                            if (element is TextPopupElement) {
                                    HtmlText(text = element.text,
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .fillMaxWidth(),
                                        style = TextStyle(
                                            fontFamily = FontFamily.Default,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 16.sp,
                                            lineHeight = 24.sp,
                                            letterSpacing = 0.5.sp,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onBackground),
                                        linkClicked = { url ->
                                            uriHandler.openUri(url)

                                        })


                            }
                        }
                    }
                }
            }
        }
    }
}

fun zoomTo(coroutineScope: CoroutineScope, feature: Feature, mapViewModel: MapViewModel) {
    coroutineScope.launch {
        val geometry = feature?.geometry
        if (geometry != null) {
            mapViewModel.mapViewProxy.setViewpointGeometry(geometry, 100.0)
        }
    }
}

