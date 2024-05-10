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
import com.arcgismaps.data.OrderBy
import com.arcgismaps.data.QueryFeatureFields
import com.arcgismaps.data.QueryParameters
import com.arcgismaps.data.ServiceFeatureTable
import com.arcgismaps.geometry.Geometry
import com.arcgismaps.geometry.Point
import com.arcgismaps.geometry.SpatialReference
import com.arcgismaps.location.LocationDisplayAutoPanMode
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.BasemapStyle
import com.arcgismaps.mapping.PortalItem
import com.arcgismaps.mapping.Viewpoint
import com.arcgismaps.mapping.layers.FeatureLayer
import com.arcgismaps.mapping.layers.GroupLayer
import com.arcgismaps.mapping.layers.Layer
import com.arcgismaps.mapping.popup.PopupElement
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
    val isLoaded = _isLoaded.asStateFlow()

    private val _selectedProperty: MutableStateFlow<ArcGISFeature?> = MutableStateFlow(null)
    val selectedProperty = _selectedProperty.asStateFlow()

    private val _selectedProperties: MutableStateFlow<List<Feature>> = MutableStateFlow(
        emptyList()
    )
    val selectedProperties = _selectedProperties.asStateFlow()

    private val _locationEnabled: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val locationEnabled = _locationEnabled.asStateFlow()

    val locationDisplay: LocationDisplay = LocationDisplay()


    private var _popupViews = MutableStateFlow<List<PopupView>>(emptyList())
    val popupViews = _popupViews.asStateFlow()

    private val _selectedGeometry = MutableStateFlow<Geometry?>(null)
    val selectedGeometry = _selectedGeometry.asStateFlow()

    init {
        val portalItem = PortalItem(
            Portal("https://www.arcgis.com"), "46c1f4b90df34dff8f01a12abede4ea9"//"95092428774c4b1fb6a3b6f5fed9fbc4"
        )
        map = ArcGISMap(portalItem)

        sampleCoroutineScope.launch {
            map.load().onSuccess {
                if (sharedPreferences.getString("viewpoint", "DEFAULT") != null) {
                    val viewpointJson =
                        sharedPreferences.getString("viewpoint", "DEFAULT").toString()
                    var viewpoint: Viewpoint? = Viewpoint.fromJsonOrNull(viewpointJson)
                    if (viewpoint != null) {
                        mapViewProxy.setViewpoint(viewpoint)
                    } else {
                        viewpoint = Viewpoint(center = Point(-78.7,35.8, spatialReference = SpatialReference(4326)), scale = 500000.0)
                        mapViewProxy.setViewpoint(viewpoint)
                    }
                }
                _isLoaded.value = true
                setLayerVisibility(map)

            }
        }

    }

    suspend fun getCondo(field: String, value: Any?) {
        sampleCoroutineScope.launch {
            val table = map.tables.find { it.displayName == "Condos" }
            val params = QueryParameters()
            params.whereClause = "$field = '$value'"

            if (field == "ADDRESS") {
                val addressTable = map.tables.find { it.displayName == "Addresses" }

                val addressParams = QueryParameters()
                addressParams.whereClause = "ADDRESS = '$value'"

                val addressResult = (addressTable as ServiceFeatureTable).queryFeatures(addressParams, QueryFeatureFields.LoadAll).getOrElse { Log.e("test", it.message.toString()) } as FeatureQueryResult
                val reids = addressResult.map { it.attributes["REA_REID"] } as List<String>
                params.whereClause = reids.joinToString(separator = ", ", prefix = "REID in (", postfix = ")") { "\'$it\'" }

            }
            if (table != null) {
                when (field) {
                    "PIN_NUM" -> {
                        params.orderByFields.add(OrderBy("PIN_NUM"))
                        params.orderByFields.add(OrderBy("PIN_EXT"))
                    }
                    "FULL_STREET_NAME", "ADDRESS" -> {
                        params.orderByFields.add(OrderBy("SITE_ADDRESS"))
                    }
                    else -> {
                        params.orderByFields.add(OrderBy(field))
                    }
                }
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
            params.orderByFields.add(OrderBy("PIN_NUM"))
            params.orderByFields.add(OrderBy("PIN_EXT"))
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
                } else if (featureResultList.count() > 1) {
                    selectProperties(featureResultList)

                }

            }
        }
    }

    private suspend fun getPropertyByPin(pin: String) {

        val group = map.operationalLayers.find { it.name == "Property" }
        if (group != null) {
            val layer = (group as GroupLayer).layers.first { it.name == "Property" }
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
                    Log.e("data error", it.cause.toString())
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
                _selectedGeometry.value = feature.geometry
                mapViewProxy.setViewpointGeometry(feature.geometry?.extent as Geometry, 100.0)
            }
        }
    }

    private suspend fun getPropertyByGeometry(mapPoint: Point?) {
        val group = map.operationalLayers.find { it.name == "Property" }

        if (group != null) {
            val layer = (group as GroupLayer).layers.first { it.name == "Property" }
            val params = QueryParameters()
            params.geometry = mapPoint
            params.whereClause = "1=1"
            if (layer is FeatureLayer) {
                getProperty(layer, params)
            }
        }
    }

    suspend fun onMapLongPress(mapPoint: Point?) {
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

    fun setLayerVisibility(map: ArcGISMap) {
        val visibleLayers: List<String> =
            sharedPreferences.getString("visibleLayers", "")?.split(",") ?: emptyList()

        fun setVisibility(layer: Layer) {
            if (layer is GroupLayer) {
                layer.isVisible = true
                layer.layers.forEach { subLayer ->
                    setVisibility(subLayer)
                }
            } else {
                if (layer.name != "Property") {
                    layer.isVisible = visibleLayers.any { it == layer.name }
                } else {
                    layer.isVisible = true
                }
            }
        }

        map.operationalLayers.forEach(::setVisibility)
    }

    suspend fun onSingleTap(screenCoordinate: ScreenCoordinate) {
        _popupViews.value = emptyList()
        mapViewProxy.identifyLayers(
            screenCoordinate = screenCoordinate,
            tolerance = 5.dp,
            returnPopupsOnly = false,
            maximumResults = null
        ).onSuccess { results ->
            results.forEach { result ->
                if (result.layerContent.name != "Property1") {
                    result.popups.forEach { popup ->
                        try {
                            popup.evaluateExpressions().onSuccess {
                                _popupViews.value += PopupView(
                                    popup.title,
                                    popup.evaluatedElements,
                                    result.layerContent as Layer
                                )

                                //_popupElements.value = popup.evaluatedElements
                            }.onFailure { exception ->
                                Log.e("popup error", exception.message.toString())

                            }
                        } catch (e: Exception) {
                            Log.e("popup error", e.message.toString())
                        }
                    }
                }
//                else  {
//                result.popups.forEach { popup ->
//                    popup.popupDefinition.expressions.forEach { expression ->
//                        val arcadeExpression = ArcadeExpression(expression.expression)
//                        val arcadeEvaluator =
//                            ArcadeEvaluator(arcadeExpression, ArcadeProfile.PopupElement)
//                        val profileVariables = mapOf<String, Any>("\$feature" to popup.geoElement)
//                        val evaluationResult =
//                            arcadeEvaluator.evaluate(profileVariables).onSuccess {
//                                Log.i("test", it.result as String)
//                            }
//
//                    }
//                }
//                }

            }
        }
    }

    suspend fun displayLocation(context: Context, mainActivity: MainActivity) {
        val permissionsCheckFineLocation = ContextCompat.checkSelfPermission(
            context,
            ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val permissionsCheckCourseLocation = ContextCompat.checkSelfPermission(
            context,
            ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!(permissionsCheckFineLocation && permissionsCheckCourseLocation)) {
            ActivityCompat.requestPermissions(
                mainActivity,
                arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION),
                2
            )
        } else {
            locationDisplay.setAutoPanMode(LocationDisplayAutoPanMode.Recenter)
            locationDisplay.dataSource.start()

        }
    }

    suspend fun stopDisplayingLocation() {
        locationDisplay.dataSource.stop()
    }

    fun locationButtonClicked() {
        if (isLoaded.value) {
            _locationEnabled.value = !_locationEnabled.value
        }
    }
}

data class PopupView(
    val title: String,
    val popupElements: List<PopupElement>,
    val layer: Layer
)
