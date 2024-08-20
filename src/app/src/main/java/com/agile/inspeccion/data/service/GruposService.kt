package com.agile.inspeccion.data.service

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query


data class Grupo(
    val inspeccion: Int,
    val tecnicoId: Int,
    val cantidad: Int
)

data class Detalle(
    val id: Int,
    val uniqueId: String,
    val contrato: Int,
    val medidor: String,
    val nombres: String,
    val ruta: String,
    val direccion: String,
    val nim: String,
    val inspeccionId: Int,
    val latitud: Double,
    val longitud: Double,
    val tecnicoAsignado: Int,


    val lectura: String,
    val observacion: Int,
    val latitudSave: Double,
    val longitudSave: Double,
    val fechaSave: String,

    val actualizado: Int,
    val enviado: Int,
)


interface GruposApi {
    @GET("api/InspeccionesGrupo/Index")
    suspend fun getGrupos(
        @Query("login") login: String,
    ):  List<Grupo>
}

interface GrabarGrabarApi {
    @GET("api/InspeccionesDetalleGrabar/Index")
    suspend fun grabar(
        @Query("login") login: String,
        @Query("UniqueId") UniqueId: String,
        @Query("AppLectura") AppLectura: String,
        @Query("AppObservacionCodigo") AppObservacionCodigo: String,
        @Query("AppFechaRegistro") AppFechaRegistro: String,
        @Query("AppLat") AppLat: String,
        @Query("AppLon") AppLon: String,

    ):  String

}

interface GrabarFoto2Api {
    @Multipart
    @POST("api/InspeccionesDetalleGrabarFoto/Index")
    suspend fun grabar(
        @Part("detalleid") detalleid: RequestBody,
        @Part("tipo") tipo: RequestBody,
        @Part file: MultipartBody.Part

        ):  String

}

interface GrabarFotoApi {

    @POST("api/InspeccionesDetalleGrabarFoto/Index")
    suspend fun grabar(
        @Query("detalleId") detalleId: Int,
        @Query("tipo") tipo: Int,
        @Query("file") file: String,

        ):  String

}

interface DetalleApi {
    @GET("api/InspeccionesDetalle/Index")
    suspend fun getDetalles(
        @Query("login") login: String,
        @Query("inspeccionid") inspeccionid: Int,
    ):  List<Detalle>
}