package com.amazing.stamp.models

data class ChatRoomModel(
    val title: String,
    val introduce: String,
    val addressTitle: String?,
    val address: String?,
    val head: String, // 방장 UID
    val users: ArrayList<String> // 유저 UID
) {
    constructor() : this(
        "", "", null, null, "", ArrayList()
    )
}
