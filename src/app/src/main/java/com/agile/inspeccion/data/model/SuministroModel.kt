package com.agile.inspeccion.data.model

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agile.inspeccion.data.database.DatabaseHelper
import com.agile.inspeccion.data.service.Detalle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SuministroModel (private val databaseHelper: DatabaseHelper) : ViewModel() {

    private val _detalle = MutableStateFlow<Detalle?>(null)
    val detalle = _detalle.asStateFlow()

    fun GetDetalleById(inspeccionId: Int) {
        viewModelScope.launch {
            //_isLoading.value = true
            try {
                _detalle.value = databaseHelper.getDetalleById(inspeccionId)
            } catch (e: Exception) {
                //_error.value = "Error: ${e.message}"
            } finally {
                //_isLoading.value = false
            }
        }
    }

    private val _imagenesCapturadas = mutableStateOf<List<Bitmap>>(emptyList())
    val imagenesCapturadas: List<Bitmap>
        get() = _imagenesCapturadas.value

    fun agregarImagen(bitmap: Bitmap) {
        _imagenesCapturadas.value = _imagenesCapturadas.value + bitmap
    }

    fun eliminarImagen(bitmap: Bitmap) {
        _imagenesCapturadas.value = _imagenesCapturadas.value - bitmap
    }

    private val _imagenAmpliada = mutableStateOf<Bitmap?>(null)
    val imagenAmpliada: Bitmap?
        get() = _imagenAmpliada.value

    fun seleccionarImagenParaAmpliar(bitmap: Bitmap) {
        _imagenAmpliada.value = bitmap
    }

    fun cerrarImagenAmpliada() {
        _imagenAmpliada.value = null
    }
}