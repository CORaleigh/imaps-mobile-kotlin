package com.raleighnc.imapsmobile

import com.arcgismaps.data.QueryFeatureFields
import com.arcgismaps.data.QueryParameters
import com.arcgismaps.data.ServiceFeatureTable
import com.arcgismaps.geometry.Geometry
import com.arcgismaps.mapping.GeoElement
import com.arcgismaps.mapping.layers.FeatureLayer
import com.arcgismaps.mapping.layers.GroupLayer
import com.arcgismaps.mapping.layers.Layer
import com.arcgismaps.mapping.popup.Popup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ServicesViewModel(mapViewModel: MapViewModel) {
    private val _popupViews = MutableStateFlow<List<PopupView>>(emptyList())
    val popupViews = _popupViews.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(
        listOf(
            Category(
                "Electoral",
                layers = listOf(
                    getLayer("Precincts", mapViewModel),
                    getLayer("US House of Representatives Districts", mapViewModel),
                    getLayer("NC Senate Districts", mapViewModel),
                    getLayer("School Board Districts", mapViewModel),
                    getLayer("Board of Commissioners Districts", mapViewModel),
                    getLayer("District Court Judicial Districts", mapViewModel),
                    getLayer("Raleigh City Council", mapViewModel),
                    getLayer("Cary Town Council", mapViewModel)
                )
            ),
            Category(
                "Planning",
                layers = listOf(
                    getLayer("Corporate Limits", mapViewModel),
                    getLayer("Planning Jurisdictions", mapViewModel),
                    getLayer("Subdivisions", mapViewModel),
                    getLayer("Raleigh Zoning", mapViewModel),
                    getLayer("Future Landuse", mapViewModel),
                    getLayer("Cary Zoning", mapViewModel),
                    getLayer("Angier Zoning", mapViewModel),
                    getLayer("Apex Zoning", mapViewModel),
                    getLayer("County Zoning", mapViewModel),
                    getLayer("Fuquay-Varina Zoning", mapViewModel),
                    getLayer("Garner Zoning", mapViewModel),
                    getLayer("Holly Springs Zoning", mapViewModel),
                    getLayer("Knightdale Zoning", mapViewModel),
                    getLayer("Morrisville Zoning", mapViewModel),
                    getLayer("Rolesville Zoning", mapViewModel),
                    getLayer("Wake Forest Zoning", mapViewModel),
                    getLayer("Wendell Zoning", mapViewModel),
                    getLayer("Zebulon Zoning", mapViewModel)
                )
            ),
            Category(
                "Solid Waste",
                layers = listOf(
                    getLayer("Raleigh Solid Waste Collection Routes", mapViewModel)

                )
            ),
            Category(
                "Environmental",
                layers = listOf(
                    getLayer("Soils", mapViewModel),
                    getLayer("Flood Hazard Areas (Floodplains)", mapViewModel)
                )
            )
        )
    )
    val categories = _categories.asStateFlow()
    private val _selectedCategory = MutableStateFlow(_categories.value.first())
    val selectedCategory = _selectedCategory.asStateFlow()

    fun clearPopups() {
        _popupViews.value = emptyList()
    }

    fun selectCategory(category: Category) {
        _selectedCategory.value = category
    }

    suspend fun getPopups(layer: Layer, selectedGeometry: Geometry) {
        if (layer != null) {
            val params = QueryParameters().apply {
                returnGeometry = true
                geometry = selectedGeometry
                whereClause = "1=1"
            }
            ((layer as FeatureLayer).featureTable as ServiceFeatureTable).queryFeatures(
                params,
                QueryFeatureFields.LoadAll
            ).onSuccess { results ->
                results.forEach { result ->
                    val popup = Popup(geoElement = result as GeoElement, layer.popupDefinition)
                    popup.evaluateExpressions().onSuccess {
                        _popupViews.value += PopupView(
                            popup.title,
                            popup.evaluatedElements,
                            layer,
                            result
                        )
                    }
                }
            }
        }
    }

    private fun getLayer(name: String, mapViewModel: MapViewModel): Layer? {
        fun findLayer(layer: Layer): Layer? {
            if (layer.name.equals(name)) {
                return layer
            } else if (layer is GroupLayer) {
                for (subLayer in layer.layers) {
                    val foundLayer = findLayer(subLayer)
                    if (foundLayer != null) {
                        return foundLayer
                    }
                }
            }
            return null
        }

        mapViewModel.map.operationalLayers.forEach { layer ->
            val foundLayer = findLayer(layer)
            if (foundLayer != null) {
                return foundLayer
            }
        }
        return null
    }
}

data class Category(val title: String, val layers: List<Layer?>)