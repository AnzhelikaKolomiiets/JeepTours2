package com.example.jeeptours2.domain.model

sealed class AppError : Throwable() {
    object NetworkError : AppError()
    object ServerError : AppError()
    object TimeoutError : AppError()
    object ConfigError : AppError()
    object InvalidInputError : AppError()
    object Unknown : AppError()
}