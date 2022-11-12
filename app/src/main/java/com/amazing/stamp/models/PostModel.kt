package com.amazing.stamp.models

import com.google.firebase.Timestamp
import java.time.LocalDate

data class PostModel(
    val writer: String,
    val friends: ArrayList<String>?,
    val content: String?,
    val location: String?,
    val startDate: Timestamp?,
    val endDate: Timestamp?,
    val createdAt: Timestamp?,
    val imageNames: ArrayList<String>?
){
    constructor() : this(
        "",
        null,
        null,
        null,
        null,
        null,
        null,
        null
    )
}
