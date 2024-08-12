package com.agile.inspeccion.data.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agile.inspeccion.data.service.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginViewModel : ViewModel() {

    private val _loginResult = MutableStateFlow<Boolean>(false)
    val loginResult = _loginResult.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _login = MutableStateFlow<String>("")
    val login = _login.asStateFlow()

    private val _nombre = MutableStateFlow<String>("")
    val nombre = _nombre.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()



    fun login(usuario: String, password: String, deviceId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.loginApi.login(usuario, password, deviceId)
                _loginResult.value = response.estado
                 if (response.estado) {
                     _login.value = response.login
                     _nombre.value = response.nombre
                }
                else{
                     _error.value = "Usuario o contraseña invalido"
                 }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}