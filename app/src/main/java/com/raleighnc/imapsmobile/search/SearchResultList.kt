package com.raleighnc.imapsmobile.search

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.raleighnc.imapsmobile.MapViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchResultList(
    results: List<SearchItem>,
    mapViewModel: MapViewModel,
    searchViewModel: SearchViewModel
) {
    val grouped = results.groupBy { it.title }
    val itemClicked = remember { mutableStateOf<SearchItem?>(null) }
    val application = LocalContext.current.applicationContext as Application
    val sharedPreferences = application.getSharedPreferences("imaps_prefs", Context.MODE_PRIVATE)
    val isFocused by searchViewModel.isFocused.collectAsState()

    LaunchedEffect(itemClicked.value) {
        if (itemClicked.value != null) {
            val field = itemClicked.value?.field.orEmpty()
            val value = itemClicked.value?.value.orEmpty()
            val editor = sharedPreferences.edit()

            GlobalScope.launch(Dispatchers.IO) {

                if (sharedPreferences.getString("searchHistory", "") != "") {
                    val historyString =
                        sharedPreferences.getString("searchHistory", "").toString()
                    val historyItems: MutableList<HistoryItem> =
                        Json.decodeFromString(historyString)
                    if (historyItems.count() == 10) {
                        historyItems.removeFirst()
                    }
                    historyItems.removeIf { it.field == field && it.value == value }
                    historyItems.add(HistoryItem(field, value))
                    editor.putString(
                        "searchHistory",
                        Json.encodeToString(historyItems)
                    )
                } else {
                    editor.putString(
                        "searchHistory",
                        Json.encodeToString(listOf(HistoryItem(field, value)))
                    )
                }
                editor.apply()
            }
            mapViewModel.getCondo(
                field,
                value
            )
        }
    }
    var heightFactor = if (isFocused) { 0.5f } else {1f}
    Log.i("test", isFocused.toString())
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(heightFactor)
            .padding(16.dp)
    ) {
        grouped.forEach { (title, values) ->
            if (values.isNotEmpty()) {
                stickyHeader {
                    Surface(modifier = Modifier.fillParentMaxWidth(), color =MaterialTheme.colorScheme.secondaryContainer) {

                        Text(
                            title,
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.fillMaxWidth().padding(10.dp)
                        )
                    }
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
}

@Serializable
data class HistoryItem(
    val field: String,
    var value: String
)
