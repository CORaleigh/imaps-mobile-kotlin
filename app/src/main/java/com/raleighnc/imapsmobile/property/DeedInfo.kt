package com.raleighnc.imapsmobile.property


import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arcgismaps.data.ArcGISFeature
import com.arcgismaps.data.Feature
import com.arcgismaps.data.FeatureQueryResult
import com.arcgismaps.data.QueryFeatureFields
import com.arcgismaps.data.QueryParameters
import com.arcgismaps.data.ServiceFeatureTable
import com.raleighnc.imapsmobile.MapViewModel
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DeedInfo(selectedProperty: ArcGISFeature, mapViewModel: MapViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val formatter = DecimalFormat("#,##0.00")
    val dateSource = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
    val dateInstance = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    var deedDateValue = ""
    val deedDate = selectedProperty.attributes["SALE_DATE"]?.toString().orEmpty()
    if (deedDate != "") {
        val deedDateLocal: LocalDate? = LocalDate.parse(deedDate, dateSource)
        deedDateValue = deedDateLocal?.format(dateInstance).toString()
    }
    val book: String? = selectedProperty.attributes["DEED_BOOK"]?.toString()
    val page: String? = selectedProperty.attributes["DEED_PAGE"]?.toString()
    val acres: String? =
        formatter.format(selectedProperty.attributes["DEED_ACRES"] as? Double ?: 0.0)
    val desc: String? = selectedProperty.attributes["PROPDESC"]?.toString()
    val deed: MutableState<Feature?> = remember { mutableStateOf(null) }


    LaunchedEffect(selectedProperty) {
        coroutineScope.launch {
            Log.i("feature table", "DEEDS")

            deed.value = getDeeds(condoFeature = selectedProperty, mapViewModel = mapViewModel)
            Log.i("feature table", deed.value?.attributes.toString())
        }
    }


    Text(
        text = "Deeds",
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Book",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = book.orEmpty(),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(2f)
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Page",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = page.orEmpty(),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(2f)
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Date",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = deedDateValue,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(2f)
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Acres",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = acres.orEmpty(),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(2f)
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Property Description",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = desc.orEmpty(),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(2f)
        )
    }
    Log.i("feature table", deed.value?.attributes.toString())
    if (deed.value != null) {

        val deedDoc = deed.value?.attributes?.get("DEED_DOC_NUM")?.toString().orEmpty()
        val bomDoc = deed.value?.attributes?.get("BOM_DOC_NUM")?.toString().orEmpty()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (deedDoc != "") {
                Button(
                    onClick = {
                        uriHandler.openUri("https://rodcrpi.wakegov.com/booksweb/pdfview.aspx?docid=${bomDoc}&RecordDate=")
                    }, modifier = Modifier
                        .weight(1f)
                        .padding(5.dp)
                ) {
                    Text("Deed")
                }
            }
            if (bomDoc != "") {
                Button(
                    onClick = {
                        uriHandler.openUri("https://rodcrpi.wakegov.com/booksweb/pdfview.aspx?docid=${bomDoc}&RecordDate=")
                    }, modifier = Modifier
                        .weight(1f)
                        .padding(5.dp)
                ) {
                    Text("Plat")
                }
            }
        }

    }


}

private suspend fun getDeeds(condoFeature: Feature, mapViewModel: MapViewModel): Feature? {
    val table = mapViewModel.map.tables.find { it.displayName == "Deeds" }
    if (table != null) {
        val queryParameters = QueryParameters().apply {
            // make search case insensitive
            whereClause = "REID = '" + condoFeature.attributes["REID"].toString() + "'"
            returnGeometry = false
        }
        Log.i("feature table", queryParameters.whereClause)
        val featureQueryResult = (table as ServiceFeatureTable).queryFeatures(
            queryParameters,
            QueryFeatureFields.LoadAll
        ).getOrElse {
            Log.e("data error", it.cause.toString())
        }
        Log.i("feature table", featureQueryResult.toString())
        if (featureQueryResult is FeatureQueryResult) {
            val featureResultList = featureQueryResult.toList()
            if (featureResultList.isNotEmpty()) {
                val deedFeature = featureResultList.first()
                Log.i("feature table", condoFeature.attributes.toString())
                return deedFeature
            }
        } else {
            Log.i("feature table", featureQueryResult.toString())
        }

    }
    return null
}
