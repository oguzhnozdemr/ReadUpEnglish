package com.oguzhanozdemir.readupenglish.service

import com.google.gson.annotations.SerializedName
import com.google.gson.internal.GsonBuildConfig
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TranslationApiService {
    @GET("language/translate/v2")
    suspend fun translate(
        @Query("q") text: String,
        @Query("target") target: String,
        @Query("key") apiKey: String = "AIzaSyA0L9gQxJL4WZJeW6YK3rxYO4BExng6xN4"
    ): TranslateResponse
}

data class TranslateResponse(
    @SerializedName("data") val data: TranslationData
)

data class TranslationData(
    @SerializedName("translations") val translations: List<Translation>
)

data class Translation(
    @SerializedName("translatedText") val translatedText: String
)