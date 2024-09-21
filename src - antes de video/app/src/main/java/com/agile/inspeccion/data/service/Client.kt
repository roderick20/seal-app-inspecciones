package com.agile.inspeccion.data.service

import com.agile.inspeccion.data.database.Foto
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    val retrofit = Retrofit.Builder()
        //.connectTimeout(60, TimeUnit.SECONDS)
        //.readTimeout(60, TimeUnit.SECONDS)
        //.writeTimeout(60, TimeUnit.SECONDS)

        .baseUrl("https://operacionessealapi.seal.com.pe/")
        //.baseUrl("https://86cf-191-98-137-123.ngrok-free.app/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val loginApi: LoginApi = retrofit.create(LoginApi::class.java)

    val grupoApi: GruposApi = retrofit.create(GruposApi::class.java)
    val detalleApi: DetalleApi = retrofit.create(DetalleApi::class.java)


    val grabarGrabarApi: GrabarGrabarApi = retrofit.create(GrabarGrabarApi::class.java)

    val grabarFotoApi: GrabarFotoApi = retrofit.create(GrabarFotoApi::class.java)

    val observationApi: ObservationApi = retrofit.create(ObservationApi::class.java)

    suspend fun uploadFile(foto: Foto, fileName: String ="foto.jpeg", mimeType: String = "image/jpeg") {

        val detalleidBody = foto.detalleid.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val tipoBody = foto.tipo.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        val requestBody = foto.foto.toRequestBody(mimeType.toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("file", fileName, requestBody)

        val apiService = retrofit.create(GrabarFoto2Api::class.java)
        val call = apiService.grabar(detalleidBody,tipoBody, filePart)

        /*call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val uploadResponse = response.body()
                    println("Subida exitosa: ${String}")
                } else {
                    println("Error en la subida: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                println("Fallo en la subida: ${t.message}")
            }
        })*/
    }
}
