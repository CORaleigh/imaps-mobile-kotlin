package com.raleighnc.imapsmobile.search

import androidx.lifecycle.ViewModel
import com.arcgismaps.ArcGISEnvironment
import com.arcgismaps.httpcore.Request
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class SearchViewModel : ViewModel() {
    private val _isSearching = MutableStateFlow(false)
    var isSearching = _isSearching.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _isFocused = MutableStateFlow(false)
    val isFocused = _isFocused.asStateFlow()


    private val _results = MutableStateFlow<List<SearchItem>>(emptyList())
    val results: StateFlow<List<SearchItem>> get() = _results
    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

    fun onToggleSearch() {
        _isSearching.value = !_isSearching.value
        if (!_isSearching.value) {
            onSearchTextChange("")

        }
    }

    fun toggleSearching() {
        _isSearching.value = !_isSearching.value
    }

    fun toggleFocus(focused: Boolean) {
        _isFocused.value = focused
    }

    suspend fun getData(layerId: Int, field: String, value: String) {
        if (value.count() > 3) {
            val request = Request.builder()
            request.url("https://maps.raleighnc.gov/arcgis/rest/services/Property/Property/FeatureServer/$layerId/query")
            request.addParameter("f", "json")
            request.addParameter("returnGeometry", "false")
            request.addParameter("returnDistinctValues", "true")
            request.addParameter("outFields", field)
            request.addParameter("orderByFields", "$field ASC")
            request.addParameter("where", "$field like '$value%'")
            request.addParameter("resultRecordCount", "10")
            ArcGISEnvironment.arcGISHttpClient.execute(request.build()).onSuccess {
                val str = it.body?.string().orEmpty()
                if (str != "") {
                    val obj = Json.decodeFromString<SearchResponse>(str)
                    _results.update { list -> list.filter { it.field !== field } }
                    val values = obj.features?.map { f ->
                        SearchItem(
                            field,
                            getTitle(field),
                            f.attributes[field].toString()
                        )
                    }
                    values?.forEach { value ->
                        _results.update { list -> list.plus(value) }
                    }
                }
            }
        } else {
            _results.value = emptyList()
        }

    }

    fun clearSearch() {
        _searchText.value = ""
        _results.value = emptyList()
        _isSearching.value = false
    }
}

data class SearchItem(
    val field: String,
    val title: String,
    var value: String
)

@Serializable
data class SearchResponse(
    val features: List<Feature>? = emptyList(),
    val fields: List<Field>? = emptyList(),
    val globalIdFieldName: String? = "",
    val objectIdFieldName: String? = "",
    val exceededTransferLimit: Boolean? = false
)

@Serializable
data class Feature(
    val attributes: Attributes
)

@Serializable
data class Field(
    val alias: String,
    val length: Int,
    val name: String,
    val type: String
)

@Serializable
data class Attributes(
    val OWNER: String? = null,
    val SITE_ADDRESS: String? = null,
    val ADDRESS: String? = null,
    val FULL_STREET_NAME: String? = null,
    val REID: String? = null,
    val PIN_NUM: String? = null
)

operator fun Attributes.get(field: String): Any? {
    return when (field) {
        "OWNER" -> OWNER
        "SITE_ADDRESS" -> SITE_ADDRESS
        "ADDRESS" -> ADDRESS
        "FULL_STREET_NAME" -> FULL_STREET_NAME
        "REID" -> REID
        "PIN_NUM" -> PIN_NUM
        else -> throw IndexOutOfBoundsException("Invalid Field Name")
    }
}

fun getTitle(field: String): String {
    return when (field) {
        "OWNER" -> "Owner"
        "SITE_ADDRESS" -> "Address"
        "ADDRESS" -> "Address"
        "FULL_STREET_NAME" -> "Street Name"
        "REID" -> "REID"
        "PIN_NUM" -> "PIN Number"
        else -> ""
    }
}