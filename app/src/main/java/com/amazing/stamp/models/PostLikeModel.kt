package com.amazing.stamp.models

data class PostLikeModel(val users: ArrayList<String>?) {
    constructor() : this(
        null
    )
}
