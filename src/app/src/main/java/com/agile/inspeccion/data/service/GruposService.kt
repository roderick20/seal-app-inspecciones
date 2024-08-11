package com.agile.inspeccion.data.service

import retrofit2.http.GET
import retrofit2.http.Query


data class Grupo(
    val inspeccion: Int,
    val tecnicoId: Int,
    val cantidad: Int
)

data class Detalle(
    val id: Int,
    val contrato: Int,
    val medidor: Int,
    val nombres: String,
    val direccion: String,
    val inspeccionId: Int,
    val latitud: Double,
    val longitud: Double,
    val tecnicoAsignado: Int
)


interface GruposApi {
    @GET("api/InspeccionesGrupo/Index")
    suspend fun getGrupos(
        @Query("login") login: String,
    ):  List<Grupo>
}

interface DetalleApi {
    @GET("api/InspeccionesDetalle/Index")
    suspend fun getDetalles(
        @Query("login") login: String,
        @Query("inspeccionid") inspeccionid: Int,
    ):  List<Detalle>
}