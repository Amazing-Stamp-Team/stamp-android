package com.amazing.stamp.pages.sns

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amazing.stamp.adapter.FeedAdapter
import com.amazing.stamp.models.FeedModel
import com.amazing.stamp.models.FriendModel
import com.amazing.stamp.models.PostAddModel
import com.amazing.stamp.utils.FirebaseConstants
import com.amazing.stamp.utils.Utils.showShortToast
import com.example.stamp.R
import com.example.stamp.databinding.FragmentFeedBinding
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
import kotlinx.coroutines.tasks.await


class FeedFragment : Fragment() {
    private val binding by lazy { FragmentFeedBinding.inflate(layoutInflater) }
    private val postModels = ArrayList<PostAddModel>()
    private val postIds = ArrayList<String>()
    private val feedAdapter by lazy { FeedAdapter(requireActivity(), postIds, postModels) }
    private val storage by lazy { Firebase.storage }
    private val fireStore by lazy { Firebase.firestore }
    private val auth by lazy { Firebase.auth }
    private var friendModel: FriendModel? = null

    private val TAG = "TAG_FEED"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding.run {
            // 글쓰기 버튼 세팅
            btnPostAdd.setOnClickListener {
                val intent = Intent(requireActivity(), PostAddActivity::class.java)
                startActivity(intent)
            }
            // 툴바 세팅
            toolbarFeed.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.toolbar_action_friends -> {
                        val intent = Intent(requireActivity(), FriendsSearchActivity::class.java)
                        startActivity(intent)
                        requireActivity().overridePendingTransition(
                            R.anim.horizon_enter,
                            R.anim.none
                        ) // 옆에서 들어오는 애니메이션

                        return@setOnMenuItemClickListener true
                    }
                    R.id.toolbar_action_dm -> {
                        showShortToast(requireActivity(), "DM Click")
                        return@setOnMenuItemClickListener true
                    }
                }
                return@setOnMenuItemClickListener false
            }
            setUpFeedRecyclerView()

            CoroutineScope(Dispatchers.Main).launch {
                getUserFriends()
            }


        }


        return binding.root
    }


    private fun setUpFeedRecyclerView() {
        binding.recyclerFeed.adapter = feedAdapter

        //    val writer: String,
        //    val imageName: ArrayList<String>,
        //    val content: String,
        //    val startTime: String,
        //    val endTime: String,
        //    val friends: ArrayList<String>,
        //    val createdAt: String,
        //    val place: String // GPS 기능 구현후에


        fireStore.collection(FirebaseConstants.COLLECTION_POSTS)
            .orderBy(FirebaseConstants.POSTS_FIELD_CREATED_AT, Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                value?.documentChanges?.forEach { dc ->
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val postModel = dc.document.toObject<PostAddModel>()
                        postModels.add(postModel)
                        postIds.add(dc.document.id)
                    }
                    feedAdapter.notifyDataSetChanged()
                }
            }

    }

    private suspend fun getUserFriends() {
        val getFriendResult = fireStore.collection(FirebaseConstants.COLLECTION_FRIENDS)
            .document(auth.uid!!).get().await()

        friendModel = getFriendResult.toObject<FriendModel>()
    }

    private suspend fun getUserNickname(userUid: String): String {
        val result = fireStore.collection(FirebaseConstants.COLLECTION_USERS)
            .document(userUid)
            .get().await()

        return result.get(FirebaseConstants.USER_FIELD_NICKNAME).toString()
    }
}