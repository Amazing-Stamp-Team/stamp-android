package com.amazing.stamp.pages.sns

import android.content.Intent
import android.graphics.Bitmap
import com.amazing.stamp.adapter.FriendAddAdapter
import com.amazing.stamp.models.UserModel
import com.amazing.stamp.utils.FirebaseConstants
import com.amazing.stamp.utils.Utils
import com.example.stamp.R

class FriendsTagActivity : FriendsSearchActivity() {

    // 클릭했을때 이벤트만 오버라이딩
    // UID와 닉네임을 return 해줌
    override fun setUpItemClickEvent() {
        friendAdapter.itemClickListener = object : FriendAddAdapter.ItemClickListener {
            override fun onItemClick(profile: ByteArray?, followingUserModel: UserModel) {

                val returnIntent = Intent().apply {
                    putExtra(PostAddActivity.INTENT_EXTRA_NAME, followingUserModel.nickname)
                    putExtra(PostAddActivity.INTENT_EXTRA_UID, followingUserModel.uid)
                    putExtra(PostAddActivity.INTENT_EXTRA_PROFILE, profile)
                }

                setResult(PostAddActivity.FRIEND_SEARCH_REQUEST_CODE, returnIntent)
                finish()
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
        }
    }
}