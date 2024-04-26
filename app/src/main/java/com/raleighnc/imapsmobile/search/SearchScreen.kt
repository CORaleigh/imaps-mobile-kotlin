package com.raleighnc.imapsmobile.search

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.raleighnc.imapsmobile.HideableBottomSheetState
import com.raleighnc.imapsmobile.MapViewModel
import com.raleighnc.imapsmobile.TopBar
import com.raleighnc.imapsmobile.property.PropertyInfo
import com.raleighnc.imapsmobile.property.PropertyList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(searchViewModel: SearchViewModel, mapViewModel: MapViewModel, bottomSheetState: HideableBottomSheetState) {
    val coroutineScope = rememberCoroutineScope()
    val searchText by searchViewModel.searchText.collectAsState()
    val isSearching by searchViewModel.isSearching.collectAsState()
    val results by searchViewModel.results.collectAsState()
    val selectedProperty by mapViewModel.selectedProperty.collectAsState()
    val selectedProperties by mapViewModel.selectedProperties.collectAsState()

    val navController = rememberNavController()

    if (selectedProperty != null) {
        LaunchedEffect (selectedProperty) {
            coroutineScope.launch {
                //navController.navigate(Screens.SEARCH.name)
                //delay(100)
                navController.navigate(Screens.PROPERTYINFO.name)
            }
        }

    }

    if (selectedProperties.isNotEmpty()) {
        LaunchedEffect (selectedProperties) {
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
                            searchViewModel.getData(1, "SITE_ADDRESS", searchText.uppercase(Locale.ROOT))
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

                SearchBar(
                    query = searchText,
                    onQueryChange = searchViewModel::onSearchTextChange,
                    onSearch = searchViewModel::onSearchTextChange,
                    active = isSearching,
                    onActiveChange = {
                        searchViewModel.onToggleSearch()
                    },
                    placeholder = { Text("Search by address, owner, PIN  or REID") },
                    leadingIcon = { Icon(Icons.Filled.Search, "search") },
                    modifier = Modifier.padding(it)
                ) {
                    SearchResultList(results = results, mapViewModel = mapViewModel)
                }
            }
        }

        composable(Screens.PROPERTYINFO.name) {
            selectedProperty?.let {
                PropertyInfo(
                    selectedProperty = it,
                    mapViewModel = mapViewModel,
                    bottomSheetState = bottomSheetState,
                    navController = navController
                )
            }
        }
        composable(Screens.PROPERTYLIST.name) {
            selectedProperties?.let {
                PropertyList(
                    mapViewModel = mapViewModel,
                    bottomSheetState = bottomSheetState,
                    navController = navController
                )
            }
        }
    }




}

enum class Screens {
    SEARCH,
    PROPERTYLIST,
    PROPERTYINFO
}

