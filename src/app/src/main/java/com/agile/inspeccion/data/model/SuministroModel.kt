package com.agile.inspeccion.data.model

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agile.inspeccion.data.database.DatabaseHelper
import com.agile.inspeccion.data.database.Foto
import com.agile.inspeccion.data.service.Detalle
import com.agile.inspeccion.data.service.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetalleImagen(
    val foto: Bitmap,
    val tipo: Int
)

class SuministroModel (private val databaseHelper: DatabaseHelper, private val id: Int) : ViewModel() {
//class SuministroModel : ViewModel {
//    private val databaseHelper: DatabaseHelper
//    public var id: Int = 0
//
//    // Constructor primario privado
//    private constructor(databaseHelper: DatabaseHelper, id: Int) : super() {
//        this.databaseHelper = databaseHelper
//        this.id = id
//    }

    private val _detalle = MutableStateFlow<Detalle?>(null)
    val detalle = _detalle.asStateFlow()

    init {
        GetDetalleById(id)
    }

    private val _fotoTipo = MutableStateFlow<Int>(0)
    val fotoTipo = _fotoTipo.asStateFlow()
    fun setFotoTipo(fotoTipo: Int) {
        _fotoTipo.value = fotoTipo
    }



    private val _lectura = MutableStateFlow<String>("")
    val lectura = _lectura.asStateFlow()
    fun setLectura(lectura: String) {
        _lectura.value = lectura
    }



    private val _observacion = MutableStateFlow<Int>(0)
    val observacion = _observacion.asStateFlow()
    fun setObservacion(observacion: Int) {
        _observacion.value = observacion
    }


    private val _contrato = MutableStateFlow<String>("")
    val contrato = _contrato.asStateFlow()

    fun GetDetalleById(inspeccionId: Int) {
        var error: String
        viewModelScope.launch {
            //_isLoading.value = true
            try {
                _detalle.value = databaseHelper.getDetalleById(inspeccionId)
            } catch (e: Exception) {
                error = "Error: ${e.message}"
            } finally {
                //_isLoading.value = false
            }
        }
    }

    private val _imagenesCapturadas = mutableStateOf<List<DetalleImagen>>(emptyList())
    val imagenesCapturadas: List<DetalleImagen>
        get() = _imagenesCapturadas.value

    fun agregarImagen(bitmap: DetalleImagen) {
        //_imagenesCapturadas.value.add()
        _imagenesCapturadas.value = _imagenesCapturadas.value + bitmap
    }

    fun eliminarImagen(bitmap: DetalleImagen) {
        _imagenesCapturadas.value = _imagenesCapturadas.value - bitmap
    }

    fun SaveDetalle(detalle: Detalle) {
        viewModelScope.launch {
            //_isLoading.value = true
            try {
                RetrofitClient.grabarGrabarApi.grabar("login", detalle.uniqueId, detalle.lectura, detalle.observacion.toString(), detalle.fechaSave, detalle.latitudSave.toString(), detalle.longitudSave.toString())
            } catch (e: Exception) {
                var error = "Error: ${e.message}"
            } finally {
                //_isLoading.value = false
            }
        }
    }

    fun DetalleEnviado( UniqueId: String) {
        viewModelScope.launch {
            //_isLoading.value = true
            try {
                databaseHelper.updateDetalleByUniqueId(UniqueId)

            } catch (e: Exception) {
                //_error.value = "Error: ${e.message}"
            } finally {
                //_isLoading.value = false
            }
        }
    }

    fun GetDetalleNoEnviado() :List<Detalle> {
        //viewModelScope.launch {
        //_isLoading.value = true
        try {
            return databaseHelper.getDetalleNoEnviado()
        } catch (e: Exception) {
            //_error.value = "Error: ${e.message}"
        } finally {
            //_isLoading.value = false
        }
        //}Result
        return emptyList()
    }

    fun GetFotoNoEnviado() :List<Foto> {
        //viewModelScope.launch {
        //_isLoading.value = true
        try {
            return databaseHelper.getFotoNoEnviado()
        } catch (e: Exception) {
            //_error.value = "Error: ${e.message}"
        } finally {
            //_isLoading.value = false
        }
        //}Result
        return emptyList()
    }


    private val _imagenAmpliada = mutableStateOf<DetalleImagen?>(null)
    val imagenAmpliada: DetalleImagen?
        get() = _imagenAmpliada.value

    fun seleccionarImagenParaAmpliar(bitmap: DetalleImagen) {
        _imagenAmpliada.value = bitmap
    }

    fun cerrarImagenAmpliada() {
        _imagenAmpliada.value = null
    }

    fun updateDetalle(id: Int, lectura: String, observacion: String, latitudSave: Double, longitudSave: Double, fechaSave: String) {
        databaseHelper.updateDetalle(id, lectura, observacion, latitudSave, longitudSave, fechaSave)
    }

    fun addImage(foto: Bitmap, detalleid: Int, tipo: Int){
        databaseHelper.addImage( foto, detalleid, tipo   )
    }

    fun siguiente(id: Int): Detalle? {
        return databaseHelper.getSiguiente( id )
    }
}