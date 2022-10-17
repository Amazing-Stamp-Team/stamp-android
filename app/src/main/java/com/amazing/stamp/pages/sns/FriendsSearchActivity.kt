package com.amazing.stamp.pages.sns

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import com.amazing.stamp.adapter.FriendAddAdapter
import com.amazing.stamp.models.FriendAddModel
import com.amazing.stamp.models.UserModel
import com.example.stamp.R
import com.example.stamp.databinding.ActivityFriendsSearchBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class FriendsSearchActivity : AppCompatActivity() {
    private val binding by lazy { ActivityFriendsSearchBinding.inflate(layoutInflater) }
    private val friendsList = ArrayList<UserModel>()
    private var fireStore: FirebaseFirestore? = null
    private var storage: FirebaseStorage? = null
    private lateinit var friendAdapter: FriendAddAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        fireStore = Firebase.firestore
        storage = Firebase.storage

        setUpFriendRecyclerView()
        setUpSearchOption()
    }


    private fun setUpFriendRecyclerView() {
        friendAdapter = FriendAddAdapter(applicationContext, storage, fireStore, friendsList)
        binding.rvFriends.adapter = friendAdapter
        friendAdapter.notifyDataSetChanged()
    }

    private fun setUpSearchOption() {
        binding.etFriendsSearch.addTextChangedListener {
            friendAdapter.fireStoreSearch(it.toString())
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}