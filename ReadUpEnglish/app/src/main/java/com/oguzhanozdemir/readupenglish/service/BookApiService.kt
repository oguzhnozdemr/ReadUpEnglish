package com.oguzhanozdemir.readupenglish.service

import com.oguzhanozdemir.readupenglish.model.BookResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.http.Url
import com.google.gson.annotations.SerializedName
import com.google.gson.internal.GsonBuildConfig

interface BookApiService {
    @GET("books")
    suspend fun getBooks(): BookResponse

    @GET("books")
    suspend fun getHistoryBooks(
        @Query("topic") topic: String = "",
        @Query("languages") language: String = "en",
        @Query("page") page: Int = 1,
        @Query("mime_type") mimeType: String = "image/jpeg"
    ): BookResponse

    @GET
    suspend fun getBookText(
        @Url url: String,
        @Header("User-Agent") userAgent: String = "Mozilla/5.0",
        @Header("Accept") accept: String = "text/plain"
    ): Response<ResponseBody>
}

