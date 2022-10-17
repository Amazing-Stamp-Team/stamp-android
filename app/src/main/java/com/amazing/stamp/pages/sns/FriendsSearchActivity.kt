package com.amazing.stamp.pages.sns

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.amazing.stamp.adapter.FriendAddAdapter
import com.amazing.stamp.models.FriendAddModel
import com.example.stamp.R
import com.example.stamp.databinding.ActivityFriendsSearchBinding

class FriendsSearchActivity : AppCompatActivity() {
    private val binding by lazy { ActivityFriendsSearchBinding.inflate(layoutInflater) }
    private lateinit var friendAdapter: FriendAddAdapter
    private val friendsList = ArrayList<FriendAddModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        pushSampleData()
        setUpFriendRecyclerView()
    }

    private fun pushSampleData() {
        friendsList.add(FriendAddModel("홍길동", "asdf@asd.sd", null))
        friendsList.add(FriendAddModel("홍길동", "asdf@asd.sd", null))
        friendsList.add(FriendAddModel("홍길동", "asdf@asd.sd", null))
        friendsList.add(FriendAddModel("홍길동", "asdf@asd.sd", null))
    }

    private fun setUpFriendRecyclerView() {
        friendAdapter = FriendAddAdapter(applicationContext, friendsList)
        binding.rvFriends.adapter = friendAdapter
        friendAdapter.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}