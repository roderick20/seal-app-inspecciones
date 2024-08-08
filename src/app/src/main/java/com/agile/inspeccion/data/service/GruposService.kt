package com.agile.inspeccion.data.service

import retrofit2.http.GET
import retrofit2.http.Query


data class GruposResponse(
    val inspeccion: Int,
    val tecnicoId: Int,
    val cantidad: Int
)

data class DetalleResponse(
    val ID: Int,
    val Contrato: Int,
    val Medidor: Int,
    val Nombres: String,
    val Direccion: String,
    val InspeccionId: Int,
    val latitud: Double,
    val longitud: Double,
    val TecnicoAsignado: Int
)


interface GruposService {
    @GET("api/InspeccionesGrupo/Index")
    suspend fun grupos(
        @Query("login") login: String,
    ):  List<GruposResponse>

    @GET("api/InspeccionesDetalle/Index")
    suspend fun detalle(
        @Query("login") login: String,
        @Query("inspeccionid") inspeccionid: Int,
    ):  List<DetalleResponse>
}