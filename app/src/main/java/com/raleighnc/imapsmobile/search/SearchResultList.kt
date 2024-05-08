package com.raleighnc.imapsmobile.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.raleighnc.imapsmobile.MapViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchResultList(results: List<SearchItem>, mapViewModel: MapViewModel) {
    val grouped = results.groupBy { it.title }
    val itemClicked = remember { mutableStateOf<SearchItem?>(null) }
    LaunchedEffect(itemClicked.value) {
        if (itemClicked.value != null) {
            mapViewModel.getCondo(
                itemClicked.value?.field.orEmpty(),
                itemClicked.value?.value.orEmpty()
            )
        }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        grouped.forEach { (title, values) ->
            stickyHeader {
                Text(
                    title,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleLarge

                )
            }
            items(values) { item ->
                ClickableText(
                    text = AnnotatedString(item.value),
                    onClick = {
                        itemClicked.value = item
                    },
                    style = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                )
            }
        }
    }
}