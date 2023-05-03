package com.amazing.stamp.utils

object Constants {

    // 기타 Request Code
    const val FRIEND_SEARCH_REQUEST_CODE = 1001

    // 기타 Intent Extra
    const val INTENT_EXTRA_POST_ID = "post_id"
    const val INTENT_EXTRA_CHAT_ID = "chat_id"

    // 지도 검색 관련
    const val EXTRA_MAP_SEARCH_REQUEST_CODE = 1005
    const val INTENT_EXTRA_ADDRESS = "address"
    const val INTENT_EXTRA_MAP_TITLE = "map_title"
    const val INTENT_EXTRA_MAP_CATEGORY = "map_category"

    // 사진 첨부 관련
    const val PHOTO_ADD_REQUEST_CODE = 1002
    const val INTENT_EXTRA_PROFILE = "INTENT_EXTRA_PROFILE"
    const val INTENT_EXTRA_UID = "INTENT_EXTRA_UID"
    const val INTENT_EXTRA_NAME = "INTENT_EXTRA_NAME"


    val METROPLITAN_CITY = arrayOf(
        "서울특별시",
        "부산광역시",
        "대구광역시",
        "인천광역시",
        "광주광역시",
        "대전광역시",
        "울산광역시"
    )
}