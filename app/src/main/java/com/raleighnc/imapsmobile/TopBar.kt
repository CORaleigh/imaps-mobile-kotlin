package com.raleighnc.imapsmobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    bottomSheetState: HideableBottomSheetState,
    coroutineScope: CoroutineScope,
    navController: NavController?
) {
    CenterAlignedTopAppBar(title = {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp, 5.dp)
                    .background(Color.Transparent)
                    .background(Color.Transparent, shape = RoundedCornerShape(5.dp))
            )
            Spacer(
                modifier = Modifier
                    .height(5.dp)
            )
            Text(
                title,
                style = MaterialTheme.typography.titleMedium
            )
        }
    },
        navigationIcon = {
            if (navController?.previousBackStackEntry != null) {
                IconButton(onClick = {
                    if (navController.previousBackStackEntry?.destination?.route == "PROPERTYINFO" && navController.currentDestination?.route != "SERVICES") {
                        navController.navigate("SEARCH")
                    } else {
                        navController.popBackStack()
                    }

                }) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowLeft,
                        contentDescription = "Localized description"
                    )
                }
            }

        },
        actions = {
            IconButton(onClick = {
                coroutineScope.launch {
                    bottomSheetState.hide()
                }
            }) {
                Icon(Icons.Filled.Close, contentDescription = "Close")
            }
        }
    )
}