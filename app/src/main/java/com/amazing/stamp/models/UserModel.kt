package com.amazing.stamp.models

data class UserModel(
    val uid: String,
    val email: String,
    val nickname: String,
    val imageName: String?,
    val followers: ArrayList<String>?,
    val followings: ArrayList<String>?
    
) {
    constructor() : this(
        "",
        "",
        "",
        "",
        null,
        null
    )
}
