package com.amazing.stamp.models

data class UserModel(
    val uid: String,
    val email: String,
    val nickname: String,
    val imageName: String?,
    
) {
    constructor() : this(
        "",
        "",
        "",
        "",
    )
}
