package com.amazing.stamp.api

import com.amazing.stamp.api.dto.Trip100Models
import com.squareup.okhttp.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface Trip100API {
    companion object {
        private const val BASE_URL = "https://api.odcloud.kr/api/"

        fun create(): Trip100API {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(Trip100API::class.java)
        }
    }

    // 지도 검색 API
    @GET("15003416/v1/uddi:a635e6c7-82cf-4714-b002-c7cf4cb20121_201609071527")
    fun getData(
        @Query(value = "serviceKey", encoded = true) serviceKey: String,
        @Query(value = "perPage") perPage: Int,
    ): Call<Trip100Models>
}