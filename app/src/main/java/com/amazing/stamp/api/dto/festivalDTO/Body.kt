package com.amazing.stamp.api.dto.festivalDTO

data class Body(
    val items: Items,
    val numOfRows: Int,
    val pageNo: Int,
    val totalCount: Int
)