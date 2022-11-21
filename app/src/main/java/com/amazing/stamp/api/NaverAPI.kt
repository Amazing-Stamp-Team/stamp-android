package com.amazing.stamp.api

import com.amazing.stamp.api.dto.NaverMapSearchResponseDTO
import com.amazing.stamp.utils.SecretConstants
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface NaverAPI {
    companion object {
        private const val BASE_URL = "https://openapi.naver.com"

        fun create(): NaverAPI {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(NaverAPI::class.java)
        }
    }
    // 지도 검색 API
    @GET("/v1/search/local.json")
    @Headers("X-Naver-Client-Id: ${SecretConstants.NAVER_CLIENT_ID}", "X-Naver-Client-Secret: ${SecretConstants.NAVER_CLIENT_SECRET}")
    fun getMapSearch(
        @Query("query") query: String,
        @Query("display") display: Int,
        @Query("sort") sort: String = "random" // random - 정확도순 내림차순 정렬, comment - 리뷰순 내림차순 정렬
    ): Call<NaverMapSearchResponseDTO>
}