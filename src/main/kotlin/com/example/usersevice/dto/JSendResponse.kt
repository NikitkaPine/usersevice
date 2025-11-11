package com.example.usersevice.dto

data class JSendResponse<T>(
    val status: String,
    val data: T? = null,
    val message: String? = null
){
    companion object{
        fun <T> success(data:T):JSendResponse<T> = JSendResponse(
            status = "success",
            data = data
        )

        fun <T> fail(data:T): JSendResponse<T> = JSendResponse(
            status = "fail",
            data = data
        )

        fun error (message: String): JSendResponse<Unit> = JSendResponse(
            status = "error",
            message = message
        )
    }
}
