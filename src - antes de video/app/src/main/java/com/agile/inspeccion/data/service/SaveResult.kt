package com.agile.inspeccion.data.service

sealed class SaveResult {
    data class Success(val message: String) : SaveResult()
    data class Error(val message: String) : SaveResult()
}