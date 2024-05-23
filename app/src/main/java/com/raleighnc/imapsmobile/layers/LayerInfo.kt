package com.raleighnc.imapsmobile.layers

import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.arcgismaps.mapping.layers.ArcGISMapImageLayer
import com.arcgismaps.mapping.layers.Layer
import kotlinx.coroutines.launch

@Composable
fun LayerInfo(layer: Layer) {
    val coroutineScope = rememberCoroutineScope()
    val swatches = remember { mutableListOf<LegendSwatch>() }
    val legendItems = remember { mutableListOf<LegendItem>() }
    var sliderPosition by remember { mutableFloatStateOf(layer.opacity) }
    layer.opacity = sliderPosition
    LaunchedEffect(layer) {
        coroutineScope.launch {
            legendItems.removeAll(legendItems)
            if (layer is ArcGISMapImageLayer) {
                for (sublayer in layer.mapImageSublayers) {
                    val legendItem = LegendItem(sublayer.name, emptyList())
                    sublayer.fetchLegendInfos().onSuccess { infos ->
                        infos.forEach { info ->
                            info.symbol?.createSwatch(screenScale = 4.0F)?.onSuccess { swatch ->
                                val label = info.name
                                legendItem.swatches += LegendSwatch(label = label, swatch = swatch)
                            }
                        }

                    }
                    legendItems += legendItem

                }

            } else {
                val legendItem = LegendItem(layer.name, emptyList())
                layer.fetchLegendInfos().onSuccess { infos ->
                    infos.forEach { info ->
                        info.symbol?.createSwatch(screenScale = 4.0F)?.onSuccess { swatch ->
                            var label = info.name
                            if (label == "") {
                                label = layer.name
                            }
                            legendItem.swatches += LegendSwatch(label = label, swatch = swatch)
                        }
                    }
                    legendItems += legendItem
                }
            }

        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 30.dp, end = 30.dp)
    ) {
        Slider(
            value = sliderPosition,
            onValueChange = { sliderPosition = it },
            steps = 10,
            valueRange = 0f..1f
        )
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(legendItems) { item ->
                Text(text = item.label)
                for (swatch in item.swatches) {
                    Row {
                        Image(
                            bitmap = swatch.swatch.bitmap.asImageBitmap(),
                            contentDescription = swatch.label,
                            modifier = Modifier.alpha(sliderPosition)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = swatch.label)
                    }
                }
                Spacer(modifier = Modifier.padding(10.dp))


            }
        }
    }
}

data class LegendItem(
    var label: String,
    var swatches: List<LegendSwatch>
)
data class LegendSwatch(
    val label: String,
    val swatch: BitmapDrawable
)
