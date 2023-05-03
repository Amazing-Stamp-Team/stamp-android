package com.amazing.stamp.api.dto.korTripDTO

data class Body(
    val items: Items,
    val numOfRows: Int,
    val pageNo: Int,
    val totalCount: Int
)