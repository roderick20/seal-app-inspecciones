package com.agile.inspeccion.data.service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://operacionessealapi.seal.com.pe/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val loginApi: LoginApi = retrofit.create(LoginApi::class.java)

    val grupoApi: GruposApi = retrofit.create(GruposApi::class.java)
    val detalleApi: DetalleApi = retrofit.create(DetalleApi::class.java)

}