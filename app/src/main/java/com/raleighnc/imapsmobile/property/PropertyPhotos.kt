package com.raleighnc.imapsmobile.property

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.arcgismaps.data.ArcGISFeature
import com.arcgismaps.data.Feature
import com.arcgismaps.data.FeatureQueryResult
import com.arcgismaps.data.QueryFeatureFields
import com.arcgismaps.data.QueryParameters
import com.arcgismaps.data.ServiceFeatureTable
import com.raleighnc.imapsmobile.MapViewModel
import kotlinx.coroutines.launch

@Composable
fun PropertyPhotos(selectedProperty: ArcGISFeature, mapViewModel: MapViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val photos: MutableState<List<Feature>> = remember { mutableStateOf(emptyList()) }
    LaunchedEffect(selectedProperty) {
        coroutineScope.launch {
            photos.value = getPhotos(selectedProperty, mapViewModel)
        }
    }
    Column(modifier = Modifier.fillMaxSize()) {
        photos.value.reversed().forEach {
            val dir = it.attributes["IMAGEDIR"]
            val name = it.attributes["IMAGENAME"]
            Log.i("photo", it.attributes.toString())
            val url = "https://services.wake.gov/realestate/photos/mvideo/${dir}/${name}"
            AsyncImage(
                model = url,
                contentDescription = "property image",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )
            Spacer(modifier = Modifier.height(20.dp))

        }

    }
}

private suspend fun getPhotos(condoFeature: Feature, mapViewModel: MapViewModel): List<Feature> {
    val table = mapViewModel.map.tables.find { it.displayName == "Photos" }
    if (table != null) {
        val queryParameters = QueryParameters().apply {
            // make search case insensitive
            whereClause = "PARCEL = '" + condoFeature.attributes["REID"].toString() + "'"
            returnGeometry = false
        }
        Log.i("feature table", queryParameters.whereClause)
        val featureQueryResult =
            (table as ServiceFeatureTable).queryFeatures(
                queryParameters,
                QueryFeatureFields.LoadAll
            )
                .getOrElse {
                    Log.e("data error", it.cause.toString())
                }
        Log.i("feature table", featureQueryResult.toString())
        if (featureQueryResult is FeatureQueryResult) {
            return featureQueryResult.toList()

        }
    }
    return emptyList()
}
