package com.raleighnc.imapsmobile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.arcgismaps.mapping.popup.FieldsPopupElement
import com.arcgismaps.mapping.popup.MediaPopupElement
import com.arcgismaps.mapping.popup.PopupMediaType
import com.arcgismaps.mapping.popup.TextPopupElement
import com.ireward.htmlcompose.HtmlText

@Composable
fun PopupScreen(mapViewModel: MapViewModel, bottomSheetState: HideableBottomSheetState) {
    val popupViews by mapViewModel.popupViews.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val selectedIndex = remember {
        mutableIntStateOf(0)
    }

    Scaffold(
        topBar = {
            TopBar(
                title = "",
                bottomSheetState = bottomSheetState,
                coroutineScope = coroutineScope,
                navController = null
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(start = 30.dp, end = 30.dp, bottom = 30.dp)
        ) {
            if (popupViews.count() > 1) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.ArrowBackIos, "Previous", modifier = Modifier
                        .clickable {
                            if (selectedIndex.value == 0) {
                                selectedIndex.value = popupViews.count() - 1
                            } else {
                                selectedIndex.value -= 1
                            }
                        }
                        .weight(1f))
                    Text(
                        text = "${(selectedIndex.value + 1).toString()} of ${
                            popupViews.count().toString()
                        }",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )
                    Icon(Icons.Filled.ArrowForwardIos, "Next", modifier = Modifier
                        .clickable {
                            if (selectedIndex.value == popupViews.count() - 1) {
                                selectedIndex.value = 0
                            } else {
                                selectedIndex.value += 1
                            }
                        }
                        .weight(1f))
                }
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    popupViews[selectedIndex.value].title,
                    style = MaterialTheme.typography.titleLarge
                )
                LazyColumn {
                    items(popupViews[selectedIndex.value].popupElements) { element ->
                        if (element is FieldsPopupElement) {
                            element.labels.forEachIndexed { index, label ->
                                Row(
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = label, modifier = Modifier.weight(1f))
                                    Text(
                                        text = element.formattedValues.get(index),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                        if (element is MediaPopupElement) {
                            element.media.forEach { media ->
                                if (media.type == PopupMediaType.Image) {
                                    AsyncImage(
                                        model = media.value?.sourceUrl,
                                        contentDescription = media.title
                                    )
                                }
                            }
                        }
                        if (element is TextPopupElement) {
                            HtmlText(text = element.text,
                                style = MaterialTheme.typography.bodyLarge,
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