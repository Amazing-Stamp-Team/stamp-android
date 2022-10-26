package com.amazing.stamp.utils

object FirebaseConstants {

    // Firestore - 데이터베이스

    /*
        USER
     */
    const val COLLECTION_USERS = "users"
    const val USER_FIELD_NICKNAME = "nickname"
    const val USER_FIELD_IMAGE_NAME = "imageName"


    /*
        FRIENDS
     */
    const val COLLECTION_FRIENDS = "friends"
    const val FRIENDS_FIELD_FOLLOWERS = "followers"
    const val FRIENDS_FIELD_FOLLOWINGS = "followings"


    // Firebase storage - 이미지, 파일 등 구글드라이브 같은 저장소
    const val STORAGE_PROFILE = "profile"
    const val TEN_MEGABYTE: Long = 1024 * 1024 * 10

}