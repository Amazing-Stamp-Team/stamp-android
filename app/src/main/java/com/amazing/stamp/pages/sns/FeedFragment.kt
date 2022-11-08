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
    private val feedAdapter by lazy { FeedAdapter(requireActivity(), postModels) }
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
                getFeeds()
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
            .orderBy(FirebaseConstants.POSTS_FIELD_CREATED_AT, Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                value?.documentChanges?.forEach { dc ->
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val postModel = dc.document.toObject<PostAddModel>()
                        postModels.add(postModel)
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

    private suspend fun getFeeds() {
//        val uid = ArrayList<String>()
//
//        friendModel!!.followers?.forEach { uid.add(it) }
//        friendModel!!.followings?.forEach { uid.add(it) }
//
//        uid.forEach {
//            val postResult = fireStore.collection(FirebaseConstants.COLLECTION_POSTS)
//                .whereEqualTo(FirebaseConstants.POSTS_FIELD_WRITER, it)
//                .get().await()
//
//            postResult.forEach {
//                val postAddModel = it.toObject<PostAddModel>()
//
//                postModels.add(
//                    FeedModel(
//                        getUserNickname(postAddModel.writer),
//                        ArrayList(),
//                        postAddModel.content.toString(),
//                        postAddModel.startDate.toString(),
//                        postAddModel.endDate.toString(),
//                        ArrayList(),
//                        "",
//                        postAddModel.location.toString()
//                    )
//                )
//            }
//
//            feedAdapter.notifyDataSetChanged()
//
////            val gsReference =
////                storage!!.getReference("${FirebaseConstants.STORAGE_PROFILE}/IMG_PROFILE_1kaofPc9DReZJJ1TqqR0wJr2IMg2_1667401347534.png")
////                    .downloadUrl.addOnSuccessListener {
////                        val uris = ArrayList<Uri>()
////                        uris.add(it)
////                        uris.add(it)
////                        uris.add(it)
////                        feedModes.add(FeedModel("너굴맨", uris, "광안리 여행을 다녀왔습니다", "", "", ArrayList(), "", "부산 광안리"))
////                        feedModes.add(FeedModel("너굴맨", uris, "광안리 여행을 다녀왔습니다", "", "", ArrayList(), "", "부산 광안리"))
////                        feedModes.add(FeedModel("너굴맨", uris, "광안리 여행을 다녀왔습니다", "", "", ArrayList(), "", "부산 광안리"))
////
////                        feedAdapter.notifyDataSetChanged()
////                    }
//        }
    }

    private suspend fun getUserNickname(userUid: String): String {
        val result = fireStore.collection(FirebaseConstants.COLLECTION_USERS)
            .document(userUid)
            .get().await()

        return result.get(FirebaseConstants.USER_FIELD_NICKNAME).toString()
    }
}