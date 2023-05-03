package com.amazing.stamp.models

import com.google.firebase.Timestamp

data class ChatModel(val user: String, val content: String, val timestamp: Timestamp) {
    constructor() : this(
        "", "", Timestamp.now()
    )
}
