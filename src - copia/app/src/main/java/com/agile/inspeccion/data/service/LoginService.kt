package com.agile.inspeccion.data.service

import retrofit2.http.GET
import retrofit2.http.Query


data class Login(
    val estado: Boolean,
    val login: String,
    val nombre: String
)

interface LoginApi {
    @GET("api/Login2/Login")
    suspend fun login(
        @Query("usuario") usuario: String,
        @Query("password") password: String,
        @Query("deviceid") deviceId: String
    ): Login
}