package com.agile.inspeccion.data.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agile.inspeccion.data.database.DatabaseHelper
import com.agile.inspeccion.data.service.Detalle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ListModel(private val databaseHelper: DatabaseHelper) : ViewModel() {

    private val _detalles = MutableStateFlow<List<Detalle>>(emptyList())
    val detalles = _detalles.asStateFlow()

    fun GetDetalle(inspeccionId: Int) {
        viewModelScope.launch {
            //_isLoading.value = true
            try {
                _detalles.value = databaseHelper.getDetalleByInspeccionId(inspeccionId)
            } catch (e: Exception) {
                //_error.value = "Error: ${e.message}"
            } finally {
                //_isLoading.value = false
            }
        }
    }

    fun SearchDetalle(column: String, search: String) {
        viewModelScope.launch {
            //_isLoading.value = true
            try {
                _detalles.value = databaseHelper.search(column, search)
            } catch (e: Exception) {
                //_error.value = "Error: ${e.message}"
            } finally {
                //_isLoading.value = false
            }
        }
    }
}