package com.chenliee.library.http

/**
 *@Author：chenliee
 *@Date：2023/10/31 10:05
 *Describe:
 */
class ErrorException(errorStatus: Boolean, message: String?) :
    RuntimeException(message) {
    private var errorStatus = false

    init {
        this.errorStatus = errorStatus
    }

    fun getErrorMessage(): String {
        return message ?: "Unknown error"
    }
}
