package com.raleighnc.imapsmobile

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.arcgismaps.data.ArcGISFeature
import com.arcgismaps.data.Feature
import com.arcgismaps.data.FeatureQueryResult
import com.arcgismaps.data.QueryFeatureFields
import com.arcgismaps.data.QueryParameters
import com.arcgismaps.data.ServiceFeatureTable
import com.arcgismaps.geometry.Geometry
import com.arcgismaps.location.LocationDisplayAutoPanMode
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.BasemapStyle
import com.arcgismaps.mapping.PortalItem
import com.arcgismaps.mapping.Viewpoint
import com.arcgismaps.mapping.layers.FeatureLayer
import com.arcgismaps.mapping.layers.GroupLayer
import com.arcgismaps.mapping.layers.Layer
import com.arcgismaps.mapping.view.LocationDisplay
import com.arcgismaps.mapping.view.ScreenCoordinate
import com.arcgismaps.portal.Portal
import com.arcgismaps.toolkit.geoviewcompose.MapViewProxy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapViewModel(
    application: Application,
    private val sampleCoroutineScope: CoroutineScope,
    private val sharedPreferences: SharedPreferences
) : AndroidViewModel(application) {

    val mapViewProxy = MapViewProxy()
    var map = ArcGISMap(BasemapStyle.ArcGISCommunity)
    private val _isLoaded = MutableStateFlow(false)
    private val _selectedProperty: MutableStateFlow<ArcGISFeature?> = MutableStateFlow(null)
    val selectedProperty = _selectedProperty.asStateFlow()
    private val _selectedProperties: MutableStateFlow<List<Feature>> = MutableStateFlow(
        emptyList()
    )
    val selectedProperties = _selectedProperties.asStateFlow()

    private val _locationEnabled: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val locationEnabled = _locationEnabled.asStateFlow()
    val locationDisplay: LocationDisplay = LocationDisplay()
    init {
        val portalItem = PortalItem(
            Portal("https://www.arcgis.com"), "95092428774c4b1fb6a3b6f5fed9fbc4"
        )
        map = ArcGISMap(portalItem)

        sampleCoroutineScope.launch {
            map.load().onSuccess {
                if (sharedPreferences.getString("viewpoint", "DEFAULT") != null) {
                    val viewpointJson =
                        sharedPreferences.getString("viewpoint", "DEFAULT").toString()
                    val viewpoint: Viewpoint? = Viewpoint.fromJsonOrNull(viewpointJson)
                    if (viewpoint != null) {
                        mapViewProxy.setViewpoint(viewpoint)
                    }
                }
                _isLoaded.value = true
                setLayerVisiblity(map)

            }
        }

    }

    suspend fun getCondo(field: String, value: String) {
        sampleCoroutineScope.launch {
            val table = map.tables.find { it.displayName == "Condos" }
            if (table != null) {
                val params = QueryParameters()
                params.whereClause = "$field = '$value'"
                val featureQueryResult =
                    (table as ServiceFeatureTable).queryFeatures(params, QueryFeatureFields.LoadAll)
                        .getOrElse {
                            Log.e("data error", it.cause.toString())
                        } as FeatureQueryResult
                val featureResultList = featureQueryResult.toList()
                if (featureResultList.isNotEmpty()) {
                    Log.i("test",featureResultList.count().toString())

                    if (featureResultList.count() == 1) {
                        val condoFeature = featureResultList.first()
                        selectProperty(condoFeature as ArcGISFeature)
                        val pin = condoFeature.attributes["PIN_NUM"]
                        if (pin != null) {
                            getPropertyByPin(pin.toString())
                        }
                    } else if (featureResultList.count() > 1) {
                        selectProperties(featureResultList)
                    }

                }
            }
        }
    }

    private suspend fun getCondoByProperty(propertyFeature: Feature) {
        val table = map.tables.find { it.displayName == "Condos" }
        if (table != null) {
            val params = QueryParameters()
            params.whereClause = "PIN_NUM = '${propertyFeature.attributes["PIN_NUM"].toString()}'"
            val featureQueryResult =
                (table as ServiceFeatureTable).queryFeatures(params, QueryFeatureFields.LoadAll)
                    .getOrElse {
                        Log.e("data error", it.cause.toString())
                    } as FeatureQueryResult
            val featureResultList = featureQueryResult.toList()
            if (featureResultList.isNotEmpty()) {
                if (featureResultList.count() == 1) {
                    val condoFeature = featureResultList.first()
                    selectProperty(condoFeature as ArcGISFeature)
                    Log.i("feature table", condoFeature.attributes.toString())
                } else if (featureResultList.count() > 1) {
                    selectProperties(featureResultList)

                }

            }
        }
    }

    private suspend fun getPropertyByPin(pin: String) {

        val group = map.operationalLayers.find { it.name == "Property" }
        if (group != null) {
            val layer = (group as GroupLayer).subLayerContents.value.first { it.name == "Property" }
            val params = QueryParameters()
            params.whereClause = "PIN_NUM = '$pin'"
            if (layer is FeatureLayer) {
                getProperty(layer, params)
            }

        }
    }

    private suspend fun getProperty(layer: FeatureLayer, params: QueryParameters) {
        val featureQueryResult =
            (layer.featureTable as ServiceFeatureTable).queryFeatures(
                params,
                QueryFeatureFields.LoadAll
            )
                .getOrElse {
                    Log.i("data error", it.cause.toString())
                } as FeatureQueryResult

        val featureResultList = featureQueryResult.toList()

        if (featureResultList.isNotEmpty()) {
            val feature = featureResultList.first()
            if (params.whereClause == "1=1" && params.geometry != null) {
                getCondoByProperty(feature)

            }
            layer.clearSelection()
            layer.selectFeature(feature)
            sampleCoroutineScope.launch {
                mapViewProxy.setViewpointGeometry(feature.geometry?.extent as Geometry, 100.0)
            }
        }
    }

    private suspend fun getPropertyByGeometry(mapPoint: com.arcgismaps.geometry.Point?) {
        val group = map.operationalLayers.find { it.name == "Property" }

        if (group != null) {
            val layer = (group as GroupLayer).subLayerContents.value.first { it.name == "Property" }
            val params = QueryParameters()
            params.geometry = mapPoint
            params.whereClause = "1=1"
            if (layer is FeatureLayer) {
                getProperty(layer, params)
            }
        }
    }

    suspend fun onMapLongPress(mapPoint: com.arcgismaps.geometry.Point?) {
        if (mapPoint != null) {
            getPropertyByGeometry(mapPoint)
        }
    }

    fun selectProperty(feature: ArcGISFeature?) {
        _selectedProperty.value = feature
    }
    private fun selectProperties(features: List<Feature>) {
        _selectedProperties.value = features
    }
    private fun setLayerVisiblity(map: ArcGISMap) {
        val visibleLayers: List<Layer> = emptyList()
        map.operationalLayers.forEach { layer ->
            if (layer is GroupLayer) {
                layer.isVisible = true
                layer.subLayerContents.value.forEach { sublayer ->
                    if (sublayer is GroupLayer) {
                        sublayer.isVisible = true
                        sublayer.subLayerContents.value.forEach { sublayer2 ->
                            if (sublayer2 is GroupLayer) {
                                sublayer2.isVisible = true
                                sublayer2.subLayerContents.value.forEach { sublayer3 ->
                                    if (sublayer3 is GroupLayer) {
                                        sublayer3.isVisible = true
                                    } else if (sublayer3.name != "Property") {
                                        sublayer3.isVisible =
                                            visibleLayers.any { it.name == sublayer3.name }
                                    }
                                }
                            } else if (sublayer2.name != "Property") {
                                sublayer2.isVisible =
                                    visibleLayers.any { it.name == sublayer2.name }
                            }
                        }
                    } else if (sublayer.name != "Property") {
                        sublayer.isVisible = visibleLayers.any { it.name == sublayer.name }
                    }
                }
            } else if (layer.name != "Property") {
                layer.isVisible = false
            }
        }
    }

    suspend fun onSingleTap(screenCoordinate: ScreenCoordinate) {
        Log.i("identify", "identify")

        mapViewProxy.identifyLayers(
            screenCoordinate = screenCoordinate,
            tolerance = 100.dp,
            returnPopupsOnly = false
        ).onSuccess { results ->
            results.forEach { result ->

                if (result.geoElements.isNotEmpty()) {
                    val feature = result.geoElements.first() as ArcGISFeature
                    Log.i("identify", feature.attributes.toString())
                }
            }
        }
    }

    suspend fun displayLocation(context: Context, mainActivity: MainActivity) {
        val permissionsCheckFineLocation = ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val permissionsCheckCourseLocation = ContextCompat.checkSelfPermission(context, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!(permissionsCheckFineLocation && permissionsCheckCourseLocation)) {
            ActivityCompat.requestPermissions(mainActivity, arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION), 2)
        } else {
            locationDisplay.setAutoPanMode(LocationDisplayAutoPanMode.Recenter)
            locationDisplay.dataSource.start()

        }
    }

    suspend fun stopDisplayingLocation() {
        locationDisplay.dataSource.stop()
    }

    fun locationButtonClicked() {
        _locationEnabled.value = !_locationEnabled.value
    }


}

