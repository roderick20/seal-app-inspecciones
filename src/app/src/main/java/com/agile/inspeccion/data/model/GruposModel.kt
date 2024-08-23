package com.agile.inspeccion.data.model


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agile.inspeccion.data.database.DatabaseHelper
import com.agile.inspeccion.data.database.Foto
import com.agile.inspeccion.data.service.Detalle
import com.agile.inspeccion.data.service.Grupo
import com.agile.inspeccion.data.service.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.agile.inspeccion.data.database.Result
//import kotlin.io.encoding.Base64
import android.util.Base64
import java.io.File

class GruposViewModel(private val databaseHelper: DatabaseHelper) : ViewModel() {
    var isLoading by mutableStateOf(false)
        private set
    var downloadProgress by mutableStateOf(0f)
        private set
    var downloadStatus by mutableStateOf("")
        private set
    var showDownloadDialog by mutableStateOf(false)
        private set

    private val _grupos = MutableStateFlow<List<Result>>(emptyList())
    val grupos = _grupos.asStateFlow()

    init {
        GetAllGrupo()
    }

    fun cargarDatos() {
        viewModelScope.launch {
            isLoading = true
            downloadProgress = 0f
            downloadStatus = "Iniciando descarga..."
            showDownloadDialog = true
            try {
                downloadStatus = "Descargando bibliotecas..."
                val grupos = RetrofitClient.grupoApi.getGrupos("rquicaña")
                downloadProgress = 0.2f

                downloadStatus = "Guardando bibliotecas..."
                grupos.forEach { databaseHelper.insertGrupo(it) }
                downloadProgress = 0.4f

                val totalBibliotecas = grupos.size
                grupos.forEachIndexed { index, grupo ->
                    downloadStatus = "Descargando libros de ${grupo.inspeccion}..."
                    val detalle = RetrofitClient.detalleApi.getDetalles("rquicaña", grupo.inspeccion)

                    downloadStatus = "Guardando libros de ${grupo.inspeccion}..."
                    detalle.forEach { databaseHelper.insertDetalle(it) }

                    // Actualizar progreso
                    downloadProgress = 0.4f + (0.6f * (index + 1) / totalBibliotecas)
                }
                downloadStatus = "Descarga completada"
                downloadProgress = 1f
                GetAllGrupo()
                delay(500)
            } catch (e: Exception) {
                downloadStatus = "Error: ${e.localizedMessage}"
            } finally {
                isLoading = false
                showDownloadDialog = false
                // Reiniciar estados
                downloadProgress = 0f
                downloadStatus = ""
            }
        }
    }


    fun GetAllGrupo() {
        viewModelScope.launch {
            //_isLoading.value = true
            try {
                _grupos.value = databaseHelper.getGrupo()
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
            var _error = "Error: ${e.message}"
        } finally {
            //_isLoading.value = false
        }
        //}Result
        return emptyList()
    }

    fun GetMain() :List<Result> {
        //viewModelScope.launch {
        //_isLoading.value = true
        try {
            return databaseHelper.getGrupo()
        } catch (e: Exception) {
            //_error.value = "Error: ${e.message}"
        } finally {
            //_isLoading.value = false
        }
        //}
        return emptyList()
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

    fun SaveFoto(foto: Foto) {
        viewModelScope.launch {
            //var file = Base64.encodeToString(foto.foto, Base64.NO_WRAP);
            try {

                RetrofitClient.uploadFile(foto )
            } catch (e: Exception) {
                var error = "Error: ${e.message}"
            } finally {
                //_isLoading.value = false
            }
        }
    }

    fun DeleteAll() {
        viewModelScope.launch {
            //_isLoading.value = true
            try {
                databaseHelper.deleteAllGrupo()
                databaseHelper.deleteAllDetalle()
                _grupos.value = emptyList()
            } catch (e: Exception) {
                //_error.value = "Error: ${e.message}"
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

    fun DetalleFotoEnviado( Id: Int) {
        viewModelScope.launch {
            //_isLoading.value = true
            try {
                databaseHelper.updateDetalleFotoByUniqueId(Id)

            } catch (e: Exception) {
                //_error.value = "Error: ${e.message}"
            } finally {
                //_isLoading.value = false
            }
        }
    }
}