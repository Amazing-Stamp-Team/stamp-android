package com.amazing.stamp.api

import com.amazing.stamp.api.dto.festivalDTO.FestivalDTO
import com.amazing.stamp.api.dto.korTripDTO.KorTripDTO
import com.amazing.stamp.utils.Utils
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.SimpleDateFormat

interface KorTripInfoAPI {
    companion object {
        val tripDateFormat = SimpleDateFormat("yyyyMMdd")
        private const val BASE_URL = "https://apis.data.go.kr/B551011/KorService/"
        val gson = GsonBuilder().setLenient().create()

        fun create(): KorTripInfoAPI {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(Utils.getUnsafeOkHttpClient().build())
                .build()
                .create(KorTripInfoAPI::class.java)
        }
    }


    @GET("locationBasedList")
    fun getLocationBasedTripInfoCall(
        @Query("serviceKey", encoded = true) serviceKey: String,
        @Query("mapX") mapX: Double,
        @Query("mapY") mapY: Double,
        @Query("radius") radius: Int,
        @Query("_type") _type: String,
        @Query("MobileOS") MobileOS: String,
        @Query("MobileApp") MobileApp: String,
        @Query("arrange") arrange: String,
        @Query("numOfRows") numOfRows: Int
    ): Call<KorTripDTO>


    @GET("searchFestival")
    fun getFestivalInfoCall(
        @Query("serviceKey", encoded = true) serviceKey: String,
        @Query("eventStartDate") eventStartDate: Int,
        @Query("_type") _type: String,
        @Query("MobileOS") MobileOS: String,
        @Query("MobileApp") MobileApp: String,
        @Query("arrange") arrange: String,
        @Query("numOfRows") numOfRows: Int
    ): Call<FestivalDTO>


//    @GET("locationBasedList")
//    fun getLocationBasedTripInfoCall(
//        @Query("serviceKey") serviceKey: String = SecretConstants.KOR_TRIP_INFO_SERVICE_KEY,
//        @Query("mapX") mapX: Double,
//        @Query("mapY") mapY: Double,
//        @Query("radius") radius: Int,
//        @Query("_type") _type: String = "json",
//        @Query("MobileOS") MobileOS: String = "AND",
//        @Query("MobileApp") MobileApp: String = "STAMF",
//        @Query("arrange") arrange: String = "O",
//        @Query("numOfRows") numOfRows: Int = 100,
//    ):Call<KorTripDTO>

    // Arrange A - 제목순 B - 수정일순, D - 생성일순
    // 대표이미지가 반드시 있는 정렬, O - 제목순, Q - 수정일순, R - 생성일순


}