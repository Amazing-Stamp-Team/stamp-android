package com.amazing.stamp.models

import com.google.firebase.Timestamp

data class FestivalModel(
    val call: String?,
    val durationStart: Timestamp?,
    val durationEnd: Timestamp?,
    val location: String?,
    val title: String?,
    val url: String?,
) {
    constructor() : this(null, null, null, null, null, null)
}