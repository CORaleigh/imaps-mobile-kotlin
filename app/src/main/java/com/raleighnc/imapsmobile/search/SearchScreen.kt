package com.raleighnc.imapsmobile.search

import PdfViewer
import android.app.Application
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.raleighnc.imapsmobile.HideableBottomSheetState
import com.raleighnc.imapsmobile.MapViewModel
import com.raleighnc.imapsmobile.ServicesView
import com.raleighnc.imapsmobile.ServicesViewModel
import com.raleighnc.imapsmobile.TopBar
import com.raleighnc.imapsmobile.property.PropertyInfo
import com.raleighnc.imapsmobile.property.PropertyList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    searchViewModel: SearchViewModel,
    mapViewModel: MapViewModel,
    bottomSheetState: HideableBottomSheetState
) {
    val coroutineScope = rememberCoroutineScope()
    val searchText by searchViewModel.searchText.collectAsState()
    val isSearching by searchViewModel.isSearching.collectAsState()
    val results by searchViewModel.results.collectAsState()
    val selectedProperty by mapViewModel.selectedProperty.collectAsState()
    val selectedProperties by mapViewModel.selectedProperties.collectAsState()
    val servicesViewModel = ServicesViewModel(mapViewModel)
    val navController = rememberNavController()
    var webUrl = ""
    val keyboardController = LocalSoftwareKeyboardController.current

    var isKeyboardShown by remember { mutableStateOf(false) }

    //var isFocused by remember { mutableStateOf(false) }

    val isFocused by searchViewModel.isFocused.collectAsState()

    LaunchedEffect (bottomSheetState.isExpanded) {
        delay(250)
        if (!bottomSheetState.isExpanded) {
            keyboardController?.hide()
            searchViewModel.toggleSearching()
        }
    }

    if (selectedProperty != null) {
        LaunchedEffect(selectedProperty) {
            coroutineScope.launch {
                navController.navigate(Screens.PROPERTYINFO.name)
            }
        }
    }

    if (selectedProperties.isNotEmpty()) {
        LaunchedEffect(selectedProperties) {
            coroutineScope.launch {
                //delay(100)
                navController.navigate(Screens.PROPERTYLIST.name)
            }
        }
    }



    NavHost(
        navController = navController,
        startDestination = Screens.SEARCH.name
    ) {
        composable(Screens.SEARCH.name) {
            var searchJob: Job? = null
            LaunchedEffect(searchText) {
                searchJob?.cancel()
                searchJob = coroutineScope.launch {
                    searchText.let {
                        delay(250)
                        if (it.isNotEmpty()) {
                            searchViewModel.getData(1, "OWNER", searchText.uppercase(Locale.ROOT))
                            searchViewModel.getData(
                                4,
                                "ADDRESS",
                                searchText.uppercase(Locale.ROOT)
                            )
                            searchViewModel.getData(1, "PIN_NUM", searchText.uppercase(Locale.ROOT))
                            searchViewModel.getData(1, "REID", searchText.uppercase(Locale.ROOT))
                            searchViewModel.getData(
                                1,
                                "FULL_STREET_NAME",
                                searchText.uppercase(Locale.ROOT)
                            )

                        }
                    }
                }
            }

            Scaffold(
                topBar = {
                    TopBar(
                        title = "Search",
                        bottomSheetState = bottomSheetState,
                        coroutineScope = coroutineScope,
                        navController = navController
                    )
                }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column {
                        SearchBar(
                            query = searchText,
                            onQueryChange = searchViewModel::onSearchTextChange,
                            onSearch = searchViewModel::onSearchTextChange,
                            active = isSearching,
                            onActiveChange = {
                                if (!isSearching) {
                                    searchViewModel.toggleSearching()
                                    coroutineScope.launch {
                                        bottomSheetState.expand()
                                    }
                                }

                            },
                            placeholder = { Text("Search by address, owner, PIN  or REID") },
                            leadingIcon = { Icon(Icons.Filled.Search, "search") },
                            trailingIcon = {
                                Icon(
                                    Icons.Filled.Clear,
                                    "clear search",
                                    modifier = Modifier.clickable {
                                        //searchViewModel.onToggleSearch()
                                        searchViewModel.clearSearch()
                                    })
                            },

                            modifier = Modifier
                                .padding(it)
                                .onFocusChanged {
                                    coroutineScope.launch {
                                        searchViewModel.toggleFocus(it.isFocused)
                                    }

                                },


                            colors = SearchBarDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            )
                        ) {
                            if (results.isNotEmpty()) {
                                SearchResultList(results = results, mapViewModel = mapViewModel, searchViewModel = searchViewModel)
                            }
                        }
                        if (results.isEmpty() || !isSearching) {
                            SearchHistory(mapViewModel = mapViewModel)

                        }
                    }
                }

            }
        }

        composable(Screens.PROPERTYINFO.name) {
            selectedProperty?.let {
                PropertyInfo(
                    selectedProperty = it,
                    mapViewModel = mapViewModel,
                    bottomSheetState = bottomSheetState,
                    navController = navController,
                    showServices = {
                        navController.navigate(Screens.SERVICES.name)
                    },
                    showDeed = {url ->
                        webUrl = url
                        navController.navigate(Screens.DEED.name)
                    }
                )
            }
        }
        composable(Screens.PROPERTYLIST.name) {
            selectedProperties.let {
                PropertyList(
                    mapViewModel = mapViewModel,
                    bottomSheetState = bottomSheetState,
                    navController = navController
                )
            }
        }
        composable(Screens.SERVICES.name) {
            ServicesView(mapViewModel, servicesViewModel, bottomSheetState, navController)
        }
        composable(Screens.DEED.name) {
            PdfViewer(url = webUrl, bottomSheetState, navController)
        }
    }
}

enum class Screens {
    SEARCH,
    PROPERTYLIST,
    PROPERTYINFO,
    SERVICES,
    DEED
}

@Composable
fun SearchHistory(mapViewModel: MapViewModel) {
    val application = LocalContext.current.applicationContext as Application
    val sharedPreferences = application.getSharedPreferences("imaps_prefs", Context.MODE_PRIVATE)
    var historyItems = remember{ mutableStateListOf<HistoryItem>()}
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect (Unit) {
        if (sharedPreferences.getString("searchHistory", "") != "") {
            val historyString =
                sharedPreferences.getString("searchHistory", "").toString()
            val items: MutableList<HistoryItem> =
                Json.decodeFromString(historyString)
            historyItems.addAll(items)
        }
    }
    Column {
        if (historyItems.isNotEmpty()) {
            Text(
                "Recent Searches",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            )
        }
        LazyColumn {

            items(historyItems.reversed()) { item ->
                ClickableText(
                    text = AnnotatedString(item.value),
                    onClick = {
                        coroutineScope.launch {
                            mapViewModel.getCondo(
                                item.field,
                                item.value
                            )
                        }
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