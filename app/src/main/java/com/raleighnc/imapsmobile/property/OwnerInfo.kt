package com.raleighnc.imapsmobile.property

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arcgismaps.data.Feature

@Composable
fun OwnerInfo(selectedProperty: Feature) {
    Text(
        text = "Owner",
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    )
    Text(
        text = selectedProperty.attributes["OWNER"]?.toString().orEmpty(),
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.fillMaxWidth()
    )
    Text(
        text = selectedProperty.attributes["ADDR1"]?.toString().orEmpty(),
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.fillMaxWidth()
    )
    Text(
        text = selectedProperty.attributes["ADDR2"]?.toString().orEmpty(),
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.fillMaxWidth()
    )
    Text(
        text = selectedProperty.attributes["ADDR3"]?.toString().orEmpty(),
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.fillMaxWidth()
    )
}