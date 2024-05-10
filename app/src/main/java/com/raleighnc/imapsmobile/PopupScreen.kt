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
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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

    LaunchedEffect (popupViews) {
        selectedIndex.intValue = 0
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

            if (popupViews.isNotEmpty()) {
                if (popupViews.count() > 1) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBackIos, "Previous", modifier = Modifier
                            .clickable {
                                if (selectedIndex.intValue == 0) {
                                    selectedIndex.intValue = popupViews.count() - 1
                                } else {
                                    selectedIndex.intValue -= 1
                                }
                            }
                            .weight(1f))
                        Text(
                            text = "${(selectedIndex.intValue + 1).toString()} of ${
                                popupViews.count().toString()
                            }",
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForwardIos, "Next", modifier = Modifier
                            .clickable {
                                if (selectedIndex.intValue == popupViews.count() - 1) {
                                    selectedIndex.intValue = 0
                                } else {
                                    selectedIndex.intValue += 1
                                }
                            }
                            .weight(1f))
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                }


                popupViews.getOrNull(selectedIndex.intValue)?.let { popupView ->
                    Text(
                        popupView.title,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
                LazyColumn {
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
                                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
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