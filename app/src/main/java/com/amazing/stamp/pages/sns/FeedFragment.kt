package com.amazing.stamp.pages.sns

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amazing.stamp.adapter.FeedAdapter
import com.amazing.stamp.models.FriendModel
import com.amazing.stamp.models.PostModel
import com.amazing.stamp.utils.FirebaseConstants
import com.amazing.stamp.utils.Utils.showShortToast
import com.example.stamp.R
import com.example.stamp.databinding.FragmentFeedBinding
import com.example.stamp.databinding.ItemFeedBinding
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.TaskState
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class FeedFragment : Fragment() {
    private val binding by lazy { FragmentFeedBinding.inflate(layoutInflater) }
    private val postModels = ArrayList<PostModel>()
    private val postIds = ArrayList<String>()
    private val isLikeClickeds = ArrayList<Boolean>()
    private val feedAdapter by lazy {
        FeedAdapter(
            requireActivity(),
            postIds,
            postModels,
            isLikeClickeds
        )
    }
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

            setUpFeedLikeClick()

            CoroutineScope(Dispatchers.Main).launch {
                getUserFriends()
                setUpFeedRecyclerView()
            }
        }

        return binding.root
    }


    private fun setUpFeedRecyclerView() {
        binding.recyclerFeed.adapter = feedAdapter


        val myFriends = ArrayList<String>()
        myFriends.add(auth.currentUser!!.uid) // 내 게시글도 보이기

        // 내가 팔로잉 하는 사람들 게시글만
//        friendModel?.followers?.forEach {
//            myFriends.add(it)
//        }
        friendModel?.followings?.forEach {
            myFriends.add(it)
        }

        myFriends.forEach {
            Log.d(TAG, "setUpFeedRecyclerView: $it")
        }

        fireStore.collection(FirebaseConstants.COLLECTION_POSTS)
            .orderBy(FirebaseConstants.POSTS_FIELD_CREATED_AT, Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                value?.documentChanges?.forEach { dc ->
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val postModel = dc.document.toObject<PostModel>()

                        if (postModel.writer in myFriends) {
                            isLikeClickeds.add(false)
                            postModels.add(postModel)
                            postIds.add(dc.document.id)
                        }
                    }
                    feedAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun setUpFeedLikeClick() {
        feedAdapter.onLikeClickListener = object : FeedAdapter.OnLikeClickListener {
            override fun onLikeClick(feedBinding: ItemFeedBinding, postId: String, position: Int) {
                if (isLikeClickeds[position]) {
                    feedBinding.ivItemFeedFoot.imageTintList = ColorStateList.valueOf(Color.BLACK)

                    fireStore.collection(FirebaseConstants.COLLECTION_POST_LIKES)
                        .document(postId).update(
                            FirebaseConstants.POST_LIKES_FIELD_USER_ID,
                            FieldValue.arrayUnion(auth.currentUser!!.uid)
                        )

                    feedBinding.tvItemFeedFootCount.text =
                        (feedBinding.tvItemFeedFootCount.text.toString().toInt() - 1).toString()
                } else {
                    feedBinding.ivItemFeedFoot.imageTintList = ColorStateList.valueOf(Color.RED)

                    fireStore.collection(FirebaseConstants.COLLECTION_POST_LIKES)
                        .document(postId).update(
                            FirebaseConstants.POST_LIKES_FIELD_USER_ID,
                            FieldValue.arrayRemove(auth.currentUser!!.uid)
                        )

                    feedBinding.tvItemFeedFootCount.text =
                        (feedBinding.tvItemFeedFootCount.text.toString().toInt() + 1).toString()
                }

                isLikeClickeds[position] = !isLikeClickeds[position]
            }
        }
    }

    private suspend fun getUserFriends() {
        val getFriendResult = fireStore.collection(FirebaseConstants.COLLECTION_FRIENDS)
            .document(auth.uid!!).get().await()

        friendModel = getFriendResult.toObject<FriendModel>()
    }

}