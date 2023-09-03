package com.bankmandiri.mobileapps.model

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String,
        @Query("page") p: Int,
        @Query("apiKey") apiKey: String
    ): Response<ResponseApi>

    @GET("everything")
    suspend fun getEverything(
        @Query("q") q: String,
        @Query("page") p: Int,
        @Query("apiKey") apiKey: String,
        @Query("sortBy") sort: String
    ): Response<ResponseApi>
}

