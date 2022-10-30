package com.amazing.stamp.models

// 프로필, 닉네임, 유저 uid 모델
data class ProfileNicknameModel(
    var image: ByteArray?,
    var uid: String,
    var nickname: String
)