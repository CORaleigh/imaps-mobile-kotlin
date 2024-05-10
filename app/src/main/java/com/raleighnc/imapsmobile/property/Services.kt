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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.arcgismaps.mapping.popup.FieldsPopupElement
import com.arcgismaps.mapping.popup.MediaPopupElement
import com.arcgismaps.mapping.popup.PopupMediaType
import com.arcgismaps.mapping.popup.TextPopupElement
import com.ireward.htmlcompose.HtmlText

@Composable
fun ServicesView(
    mapViewModel: MapViewModel,
    servicesViewModel: ServicesViewModel,
    bottomSheetState: HideableBottomSheetState,
    navController: NavController
) {

    val selectedGeometry by mapViewModel.selectedGeometry.collectAsState()
    val popupViews by servicesViewModel.popupViews.collectAsState()
    val uriHandler = LocalUriHandler.current
    val coroutineScope = rememberCoroutineScope()
    val categories by servicesViewModel.categories.collectAsState()
    val selectedCategory by servicesViewModel.selectedCategory.collectAsState()

    LaunchedEffect(key1 = selectedGeometry, key2 = selectedCategory) {
        servicesViewModel.clearPopups()
        selectedGeometry?.let {
            selectedCategory.layers.forEach { layer ->
                if (layer != null) {
                    servicesViewModel.getPopups(layer, it)
                }
            }

        }
    }


    Scaffold(
        topBar = {
            TopBar(
                title = "Services",
                bottomSheetState = bottomSheetState,
                coroutineScope = coroutineScope,
                navController = navController
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(start = 30.dp, end = 30.dp, bottom = 30.dp)
        ) {
            ServiceCategorySelector(items = categories, categorySelected = {
                servicesViewModel.selectCategory(it)
            })
            Spacer(modifier = Modifier.height(30.dp))
            if (popupViews.isEmpty()) {
                Text(text = "No information available in this area")
            }
            LazyColumn {
                items(popupViews) { popupView ->
                    Text(
                        popupView.title,
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Normal,
                            fontSize = 22.sp,
                            lineHeight = 28.sp,
                            letterSpacing = 0.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    popupView.popupElements.forEach { element ->
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
                                                style = TextStyle(MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)
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
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                        }
                        if (element is TextPopupElement) {
                            Spacer(modifier = Modifier.height(6.dp))
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
                    Spacer(modifier = Modifier.height(30.dp))

                }
            }
        }

    }


}

