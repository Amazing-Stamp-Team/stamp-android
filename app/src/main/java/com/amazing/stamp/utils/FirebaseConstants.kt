package com.amazing.stamp.utils

object FirebaseConstants {

    // Firestore - 데이터베이스

    /*
        User
     */
    const val COLLECTION_USERS = "users"
    const val USER_FIELD_NICKNAME = "nickname"
    const val USER_FIELD_IMAGE_NAME = "imageName"


    /*
        Friend
     */
    const val COLLECTION_FRIENDS = "friends"
    const val FRIENDS_FIELD_FOLLOWERS = "followers"
    const val FRIENDS_FIELD_FOLLOWINGS = "followings"


    /*
        Post
     */
    const val COLLECTION_POSTS = "posts"
    const val POSTS_FIELD_IMAGE_NAME = "imageNames"
    const val POSTS_FIELD_WRITER = "writer"
    const val POSTS_FIELD_CREATED_AT = "createdAt"
    const val POSTS_FIELD_CONTENT = "content"
    const val POSTS_FIELD_LOCATION = "location"
    const val POSTS_FIELD_START_DATE = "startDate"
    const val POSTS_FIELD_END_DATE = "endDate"
    const val POSTS_FIELD_FRIENDS = "friends"

    /*
        PostLikes
     */
    const val COLLECTION_POST_LIKES = "postLikes"
    const val POST_LIKES_FIELD_USER_ID = "users"


    /*
        Chat
     */
    const val COLLECTION_CHAT = "chat"
    const val CHAT_FIELD_TITLE = "title"
    const val CHAT_FIELD_HEAD = "head"
    const val CHAT_FIELD_INTRODUCE = "introduce"
    const val CHAT_FIELD_ADDRESS = "address"
    const val CHAT_FIELD_ADDRESS_TITLE = "addressTitle"
    const val CHAT_FIELD_USERS = "users"
    const val CHAT_FIELD_PROVINCE = "province"
    const val CHAT_FIELD_CITY = "city"

    const val COLLECTION_MESSAGE_LOG = "messageLog"


    /*
        TripLocation
     */
    const val COLLECTION_TRIP_LOCATION = "tripLocation"
    const val TRIP_LOCATION_FIELD_VISITED = "visited"

    /*
        Festival
     */
    const val COLLECTION_FESTIVAL = "festival"


    /*
        KoreaTrip100
     */
    const val COLLECTION_KOREA_TRIP_100 = "koreaTrip100"



    // Firebase storage - 이미지, 파일 등 구글드라이브 같은 저장소
    const val STORAGE_PROFILE = "profile"
    const val STORAGE_POST = "posts"
    const val STORAGE_FESTIVAL = "festival"
    const val STORAGE_KOREA_TRIP_100 = "koreaTrip100"
    const val TEN_MEGABYTE: Long = 1024 * 1024 * 10

}