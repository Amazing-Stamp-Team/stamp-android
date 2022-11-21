package com.amazing.stamp.models

data class ChatRoomModel(
    val title: String,
    val introduce: String,
    val addressTitle: String?,
    val address: String?, // 전체 주소
    val province: String?, // 도
    val city: String?, // 시
    val head: String, // 방장 UID
    val users: ArrayList<String> // 유저 UID
) {
    constructor() : this(
        "", "", null, null, null, null, "", ArrayList()
    )
}
