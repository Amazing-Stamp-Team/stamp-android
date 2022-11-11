package com.amazing.stamp.models

import android.net.Uri
import com.google.firebase.Timestamp

data class MyPageTripModel(
    val writer: String,
    val imageUris: ArrayList<Uri>?,
    val startDate: Timestamp?,
    val createdAt: Timestamp?,
    val location: String,
    val imageNames:ArrayList<String>?
) {
    constructor() : this(
        "",
        null,
        null,
        null,
        "",
        null
    )
}