package com.agile.inspeccion.data.model

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agile.inspeccion.data.database.DatabaseHelper
import com.agile.inspeccion.data.database.Grupo
import com.agile.inspeccion.data.service.GruposResponse
import com.agile.inspeccion.data.service.GruposService
import com.agile.inspeccion.data.service.LoginService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory



class GruposViewModel (private val dbHelper: DatabaseHelper): ViewModel() {

    private val _gruposResult = MutableStateFlow<List<Grupo>>(emptyList())
    val gruposResult = _gruposResult.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    /*private val _login = MutableStateFlow<String>("")
    val login = _login.asStateFlow()

    private val _nombre = MutableStateFlow<String>("")
    val nombre = _nombre.asStateFlow()*/

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val apiService: GruposService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://operacionessealapi.seal.com.pe/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(GruposService::class.java)

        GetGruposDB()
    }

    /*fun GetGrupos(usuario: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val grupos = apiService.grupos(usuario)
                grupos.forEach { grupo ->
                    dbHelper.insertGrupo(grupo.inspeccion, grupo.tecnicoId, grupo.cantidad)
                }
                var gruposdb = dbHelper.getAllGrupos()
                gruposdb.forEach { grupo ->
                    val operaciones = apiService.detalle(usuario, grupo.inspeccion)
                    operaciones.forEach { operacion ->
                        dbHelper.insertOperacion(operacion.Medidor,operacion.TecnicoAsignado,operacion.Nombres,operacion.Direccion,operacion.InspeccionId,grupo.id,operacion.latitud,operacion.longitud,)
                    }
                }
                GetGruposDB()
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }*/

    fun GetGrupos(usuario: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Obtener grupos de la API
                val grupos = withContext(Dispatchers.IO) {
                    apiService.grupos(usuario)
                }

                // Insertar grupos en la base de datos
                withContext(Dispatchers.IO) {
                    grupos.forEach { grupo ->
                        dbHelper.insertGrupo(grupo.inspeccion, grupo.tecnicoId, grupo.cantidad)
                    }
                }

                // Obtener grupos de la base de datos
                val gruposDb = withContext(Dispatchers.IO) {
                    dbHelper.getAllGrupos()
                }

                // Procesar operaciones para cada grupo
                gruposDb.forEach { grupo ->
                    // Obtener detalles de la API
                    val operaciones = withContext(Dispatchers.IO) {
                        apiService.detalle(usuario, grupo.inspeccion)
                    }

                    // Insertar operaciones en la base de datos
                    withContext(Dispatchers.IO) {
                        operaciones.forEach { operacion ->
                            dbHelper.insertOperacion(
                                operacion.Medidor,
                                operacion.TecnicoAsignado,
                                operacion.Nombres,
                                operacion.Direccion,
                                operacion.InspeccionId,
                                grupo.id,
                                operacion.latitud,
                                operacion.longitud
                            )
                        }
                    }
                }

                // Actualizar la UI con los datos de la base de datos
                GetGruposDB()
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun GetGruposDB() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _gruposResult.value = dbHelper.getAllGrupos()
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun DeleteGruposDB() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                dbHelper.deleteAllGrupos()
                dbHelper.deleteAllOperaciones()
                _gruposResult.value = emptyList()
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}