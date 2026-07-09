package com.example.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface MailTmApi {
    @GET("domains")
    suspend fun getDomains(): DomainListResponse

    @POST("accounts")
    suspend fun createAccount(@Body request: AccountRequest): AccountResponse

    @POST("token")
    suspend fun getToken(@Body request: AccountRequest): TokenResponse

    @GET("messages")
    suspend fun getMessages(@Header("Authorization") token: String): MessageListResponse

    @GET("messages/{id}")
    suspend fun getMessage(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): MessageDetail

    @DELETE("messages/{id}")
    suspend fun deleteMessage(
        @Header("Authorization") token: String,
        @Path("id") id: String
    )
}
