package com.amazing.stamp.models

data class KoreaTrip100(
    val location1: String,
    val location2: String,
    val fullLocation: String,
    val name: String,
    val url: String,
    val tag: String
) {
    constructor() : this("", "", "", "", "", "")
}