package com.amazing.stamp.models

data class FeedModel(
    val imageName : ArrayList<String>,
    val startTime : String,
    val endTime : String,
    val friends : ArrayList<String>,
    val writtenFeed : String
    // val place : Int, GPS 기능 구현후에

)