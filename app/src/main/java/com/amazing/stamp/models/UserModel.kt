package com.amazing.stamp.models

data class UserModel(
    val userName: String,
    val profileImageUrl: String,
    val uid: String?,
    val pushToken: String?
)
