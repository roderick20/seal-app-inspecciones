package com.agile.inspeccion.data.model


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agile.inspeccion.data.database.DatabaseHelper
import com.agile.inspeccion.data.database.Foto
import com.agile.inspeccion.data.service.Detalle
import com.agile.inspeccion.data.service.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.agile.inspeccion.data.database.Result


class GruposViewModel(private val databaseHelper: DatabaseHelper, tipo: String) : ViewModel() {
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

    private val _error = MutableStateFlow<String>("")
    val error = _error.asStateFlow()

    init {
        GetAllGrupo(tipo)
    }

    fun cargarDatos(login: String, tipo: String) {
        viewModelScope.launch {
            isLoading = true
            downloadProgress = 0f
            downloadStatus = "Iniciando descarga..."
            showDownloadDialog = true
            try {
                downloadStatus = "Descargando libros..."
                val grupos = RetrofitClient.grupoApi.getGrupos(login, tipo)
                downloadProgress = 0.2f

                downloadStatus = "Guardando libros..."
                grupos.forEach { databaseHelper.insertGrupo(it) }
                downloadProgress = 0.4f

                val totalBibliotecas = grupos.size
                grupos.forEachIndexed { index, grupo ->
                    downloadStatus = "Descargando suministro de ${grupo.inspeccion}..."
                    val detalle = RetrofitClient.detalleApi.getDetalles(login, grupo.inspeccion, tipo)

                    downloadStatus = "Guardando suministro de ${grupo.inspeccion}..."
                    detalle.forEach { databaseHelper.insertDetalle(it) }

                    // Actualizar progreso
                    downloadProgress = 0.4f + (0.6f * (index + 1) / totalBibliotecas)
                }
                downloadStatus = "Descarga completada"
                downloadProgress = 1f
                GetAllGrupo(tipo)
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


    fun cargarObservacion(login: String, tipo: String) {
        viewModelScope.launch {
            isLoading = true
            downloadProgress = 0f
            downloadStatus = "Iniciando descarga..."
            showDownloadDialog = true
            try {
                downloadStatus = "Descargando observaciones..."
                val observations = RetrofitClient.observationApi.getObservation(login)
                downloadProgress = 0.2f

                observations.forEachIndexed { index, observation ->
                    databaseHelper.updateObservation(observation.contrato)
                }


                downloadStatus = "Descarga completada"
                downloadProgress = 1f
                GetAllGrupo(tipo)
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


    fun GetAllGrupo(tipo: String) {
        viewModelScope.launch {
            //_isLoading.value = true
            try {
                _grupos.value = databaseHelper.getGrupo(tipo)
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

    fun GetMain(tipo: String) :List<Result> {
        //viewModelScope.launch {
        //_isLoading.value = true
        try {
            return databaseHelper.getGrupo(tipo)
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
                RetrofitClient.grabarGrabarApi.grabar("login", detalle.uniqueId, detalle.lectura,
                    detalle.observacion.toString(), detalle.fechaSave, detalle.latitudSave.toString(),
                    detalle.longitudSave.toString(),
                    detalle.reset,
                    detalle.mdhfpa,
                    detalle.eatp,
                    detalle.eahpp,
                    detalle.mdhpp,
                    detalle.mdhpa,
                    detalle.eahfpp,
                    detalle.mdhfpp,
                    detalle.erp,
                    detalle.eatc,
                    detalle.eahpc,
                    detalle.mdhpc,
                    detalle.eahfpc,
                    detalle.mdhfpc,
                    detalle.erc,
                    detalle.tipolec,
                    detalle.tipolecman, detalle.ubicacion, detalle.perfilCarga
                )
            } catch (e: Exception) {
                var error = "Error: ${e.message}"
            } finally {
                //_isLoading.value = false
            }
        }
    }

    /*suspend fun SaveDetalle(detalle: Detalle): SaveResult {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.grabarGrabarApi.grabar(
                    "login",
                    detalle.uniqueId,
                    detalle.lectura,
                    detalle.observacion.toString(),
                    detalle.fechaSave,
                    detalle.latitudSave.toString(),
                    detalle.longitudSave.toString()
                )

                // Asumiendo que la API devuelve un código de éxito
                if (response.isSuccessful) {
                    SaveResult.Success("Detalle guardado exitosamente")
                } else {
                    SaveResult.Error("Error al guardar: ${response.message()}")
                }
            } catch (e: HttpException) {
                SaveResult.Error("Error de red: ${e.message()}")
            } catch (e: Exception) {
                SaveResult.Error("Error inesperado: ${e.message}")
            }
        }
    }*/

    fun SaveFoto(foto: Foto) {
        _error.value = ""
        viewModelScope.launch {
            //var file = Base64.encodeToString(foto.foto, Base64.NO_WRAP);
            try {
                RetrofitClient.uploadFile(foto )

            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                //_isLoading.value = false
            }

        }

    }

//    suspend fun SaveFoto(foto: Foto): Boolean {
//        return withContext(Dispatchers.IO) {
//            try {
//                RetrofitClient.uploadFile(foto)
//                true
//            } catch (e: Exception) {
//                val error = "Error: ${e.message}"
//                // Log the error or update some state
//                false
//            } finally {
//                //_isLoading.value = false
//            }
//        }
//    }

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