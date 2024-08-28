package com.agile.inspeccion.data.service

import retrofit2.http.GET
import retrofit2.http.Query

data class Observation(
    val contrato: Int
)


interface ObservationApi {
    @GET("api/InspeccionesGrupo/Observation")
    suspend fun getObservation(
        @Query("login") login: String,
    ):  List<Observation>
}