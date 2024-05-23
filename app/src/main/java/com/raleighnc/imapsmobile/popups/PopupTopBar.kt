package com.raleighnc.imapsmobile.popups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.raleighnc.imapsmobile.HideableBottomSheetState
import com.raleighnc.imapsmobile.PopupView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopupTopBar(
    title: String,
    selectedPopupView: PopupView?,
    popupViewCount: Int,
    bottomSheetState: HideableBottomSheetState,
    coroutineScope: CoroutineScope,
    returnToList: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            Spacer(
                modifier = Modifier
                    .height(5.dp)
            )
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        }
    },
        navigationIcon = {
            if (selectedPopupView != null && popupViewCount > 1) {
                TextButton(
                    onClick = { returnToList() },
                    modifier = Modifier.padding(top = 14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "zoom to",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSize))
                    Text(
                        "Results",
                        style = TextStyle(color = MaterialTheme.colorScheme.onBackground)
                    )
                }

            }

        },
        actions = {

            IconButton(
                modifier = Modifier.padding(top=10.dp),
                onClick = {
                coroutineScope.launch {
                    bottomSheetState.hide()
                    returnToList()
                }
            }) {
                Icon(Icons.Filled.Close, contentDescription = "Close")
            }
        },
        colors =  TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )

}


