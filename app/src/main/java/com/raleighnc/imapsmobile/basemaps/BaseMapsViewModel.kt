package com.raleighnc.imapsmobile.basemaps

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.Basemap
import com.arcgismaps.mapping.PortalItem
import com.arcgismaps.mapping.layers.ImageTiledLayer
import com.arcgismaps.mapping.layers.RasterLayer
import com.arcgismaps.portal.Portal
import com.arcgismaps.portal.PortalGroup
import com.arcgismaps.portal.PortalGroupContentSearchParameters
import com.arcgismaps.portal.PortalItemType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class BaseMapsViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val _maps = MutableStateFlow<List<PortalItem>>(emptyList())
    val maps = _maps.asStateFlow()
    private val _selected = MutableStateFlow(BasemapGroup.MAPS)
    val selected = _selected.asStateFlow()
    private val _selectedBasemap = MutableStateFlow("")
    val selectedBasemap = _selectedBasemap.asStateFlow()
    suspend fun changeBasemap(item: PortalItem, map: ArcGISMap) {
        if (selected.value == BasemapGroup.IMAGES) {
            val basemapMap = ArcGISMap(item)
            basemapMap.load().getOrElse { }
            val reference = basemapMap.basemap.value?.referenceLayers?.first()
            val raster =
                basemapMap.basemap.value?.baseLayers?.filterIsInstance<RasterLayer>()?.first()
            val tiled =
                basemapMap.basemap.value?.baseLayers?.filterIsInstance<ImageTiledLayer>()?.first()
            if (tiled?.spatialReference?.wkid != map.spatialReference?.wkid) {
                if (raster?.item != null) {
                    val newRaster = RasterLayer(item = raster.item!!)
                    newRaster.minScale = null
                    newRaster.maxScale = null
                    val newBasemap = Basemap(baseLayer = newRaster.clone())
                    if (reference != null) {
                        newBasemap.referenceLayers.add(reference.clone())
                    }
                    newBasemap.name = item.title
                    map.setBasemap(newBasemap)
                }
            } else {
                map.setBasemap(Basemap(item = item))
            }
        } else {
            map.setBasemap(Basemap(item = item))
        }
        _selectedBasemap.value = item.itemId
    }

    private suspend fun getMaps(portalGroup: PortalGroup) {
        val params = PortalGroupContentSearchParameters.items(types = listOf(PortalItemType.WebMap))
        portalGroup.findItems(params).onSuccess { resultSet ->
            val basemaps = if (selected.value == BasemapGroup.IMAGES) {
                resultSet.results.sortedByDescending { it.title }
            } else {
                resultSet.results.sortedBy { it.title }
            }
            _maps.value = basemaps
        }.onFailure {
            Log.i("portal error", "Error loading portal items: ${it.message}")
        }
    }

    suspend fun getPortalGroup(id: String) {
        // set the portal
        val portal = Portal("https://www.arcgis.com")
        // create the portal item with the item ID for the Portland tree service data
        val portalGroup = PortalGroup(portal, id)
        portalGroup.load().onSuccess {
            getMaps(portalGroup)
        }.onFailure {
            Log.i("portal error", "Error loading portal group: ${it.message}")
        }
    }

    suspend fun basemapGroupChanged(group: BasemapGroup) {
        _selected.value = group
        val id = when (group) {
            BasemapGroup.MAPS -> "f6329364e80c438a958ce74aadc3a89f"
            BasemapGroup.IMAGES -> "492386759d264d49948bf7f83957ddb9"
            BasemapGroup.ESRI -> "5e4b1873eeed4e448aca4bf930df0cd0"
        }
        getPortalGroup(id)
    }

}

enum class BasemapGroup {
    MAPS, IMAGES, ESRI
}
