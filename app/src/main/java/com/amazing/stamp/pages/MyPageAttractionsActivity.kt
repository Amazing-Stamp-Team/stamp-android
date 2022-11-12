package com.amazing.stamp.pages

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.amazing.stamp.adapter.FeedAdapter
import com.amazing.stamp.adapter.MyPageTripAdapter
import com.amazing.stamp.models.FriendModel
import com.amazing.stamp.models.MyPageTripModel
import com.amazing.stamp.models.PostAddModel
import com.amazing.stamp.pages.sns.FriendsSearchActivity
import com.amazing.stamp.pages.sns.PostAddActivity
import com.amazing.stamp.utils.FirebaseConstants
import com.amazing.stamp.utils.ParentActivity
import com.amazing.stamp.utils.Utils
import com.example.stamp.R
import com.example.stamp.databinding.ActivityMyPageAttractionsBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyPageAttractionsActivity : ParentActivity() {
    private val binding by lazy { ActivityMyPageAttractionsBinding.inflate(layoutInflater) }
    private val myPageTripModel = ArrayList<MyPageTripModel>()
    private val postIDs = ArrayList<String>()
    private val myPageTripAdapter by lazy {
        MyPageTripAdapter(
            applicationContext,
            postIDs,
            myPageTripModel
        )
    }
    private val storage by lazy { Firebase.storage }
    private val fireStore by lazy { Firebase.firestore }
    private val auth by lazy { Firebase.auth }

    private val TAG = "TAG_MYPAGEATTRACTIONS"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.run {
            binding.ibMyPageAttractionsBack.setOnClickListener {  //뒤로가기 버튼을 눌렀을때
                finish()
            }
            setUpFeedRecyclerView()
        }
    }


    private fun setUpFeedRecyclerView() {
        binding.rvMyAttractions.adapter = myPageTripAdapter

        fireStore.collection(FirebaseConstants.COLLECTION_POSTS)
            .whereEqualTo(FirebaseConstants.POSTS_FIELD_WRITER, auth.currentUser?.uid)
            .orderBy(FirebaseConstants.POSTS_FIELD_CREATED_AT, Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                value?.documentChanges?.forEach { dc ->
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val myPageModel = dc.document.toObject<MyPageTripModel>()
                        postIDs.add(dc.document.id)
                        myPageTripModel.add(myPageModel)
                    }
                    myPageTripAdapter.notifyDataSetChanged()
                }
            }
    }
}

