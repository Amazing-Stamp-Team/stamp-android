package com.amazing.stamp.api.dto

data class Trip100Models(
    val currentCount: Int,
    val `data`: List<Data>,
    val matchCount: Int,
    val page: Int,
    val perPage: Int,
    val totalCount: Int
)

data class Data(
    val 대분류: String,
    val 문의및안내: String,
    val 소분류: String,
    val 시군: String,
    val 읍면동: String,
    val 정보명: String,
    val 주소: String,
    val 중분류: String,
    val 지역: String
)