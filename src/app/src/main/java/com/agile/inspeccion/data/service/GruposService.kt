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
    val cantidad: Int,
    val tipo: String
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

    val reset: String,
    val mdhfpa: String,
    val eatp: String,
    val eahpp: String,
    val mdhpp: String,
    val mdhpa: String,
    val eahfpp: String,
    val mdhfpp: String,
    val erp: String,
    val eatc: String,
    val eahpc: String,
    val mdhpc: String,
    val eahfpc: String,
    val mdhfpc: String,
    val erc: String,

    val tipolec: String,
    val tipolecman: String,

    val sed: String,

    val ubicacion: String,
    val perfilCarga: String


)


interface GruposApi {
    @GET("api/InspeccionesGrupo/Index")
    suspend fun getGrupos(
        @Query("login") login: String,
        @Query("tipo") tipo: String,
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
        @Query("reset") reset: String,
        @Query("mdhfpa") mdhfpa: String,
        @Query("eatp") eatp: String,
        @Query("eahpp") eahpp: String,
        @Query("mdhpp") mdhpp: String,
        @Query("mdhpa") mdhpa: String,
        @Query("eahfpp") eahfpp: String,
        @Query("mdhfpp") mdhfpp: String,
        @Query("erp") erp: String,
        @Query("eatc") eatc: String,
        @Query("eahpc") eahpc: String,
        @Query("mdhpc") mdhpc: String,
        @Query("eahfpc") eahfpc: String,
        @Query("mdhfpc") mdhfpc: String,
        @Query("erc") erc: String,

        @Query("tipolec") tipolec: String,
        @Query("tipolecman") tipolecman: String,

        @Query("ubicacion") ubicacion: String,
        @Query("perfilcarga") perfilcarga: String,
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

interface GrabarVideoApi {
    @Multipart
    @POST("api/InspeccionesDetalleGrabarVideo/Index")
    suspend fun grabar(
        @Part("detalleid") detalleid: RequestBody,
           @Part file: MultipartBody.Part
    ):  String

}

/*interface GrabarFotoApi {

    @POST("api/InspeccionesDetalleGrabarFoto/Index")
    suspend fun grabar(
        @Query("detalleId") detalleId: Int,
        @Query("tipo") tipo: Int,
        @Query("file") file: String,

        ):  String

}*/



interface DetalleApi {
    @GET("api/InspeccionesDetalle/Index")
    suspend fun getDetalles(
        @Query("login") login: String,
        @Query("inspeccionid") inspeccionid: Int,
        @Query("tipo") tipo: String,
    ):  List<Detalle>
}

