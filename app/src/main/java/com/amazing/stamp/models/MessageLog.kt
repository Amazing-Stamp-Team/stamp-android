package com.amazing.stamp.models

import com.google.firebase.Timestamp

data class MessageLog(val content: String, val user: String, val time: Timestamp) {
    constructor() : this("", "", Timestamp.now())
}