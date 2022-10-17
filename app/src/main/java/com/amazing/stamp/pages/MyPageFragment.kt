package com.amazing.stamp.pages

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amazing.stamp.adapter.MyPageTripAdapter
import com.amazing.stamp.adapter.decoration.VerticalGapDecoration
import com.amazing.stamp.models.MyPageTripModel
import com.amazing.stamp.models.UserModel
import com.amazing.stamp.utils.FirebaseConstants
import com.amazing.stamp.utils.ParentFragment
import com.amazing.stamp.utils.SecretConstants
import com.amazing.stamp.utils.Utils.showShortToast
import com.example.stamp.databinding.FragmentMyPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await


class MyPageFragment : ParentFragment() {
    val TAG = "MyPageFragment"
    private val binding by lazy { FragmentMyPageBinding.inflate(layoutInflater) }
    private var auth: FirebaseAuth? = null
    private var storage: FirebaseStorage? = null
    private var fireStore: FirebaseFirestore? = null
    private var userModel: UserModel? = null
    private val TEN_MEGABYTE: Long = 1024 * 1024 * 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        auth = FirebaseAuth.getInstance()
        storage = Firebase.storage(SecretConstants.FIREBASE_STORAGE_URL)
        fireStore = Firebase.firestore
        
        binding.tvProfileNickname.text = auth!!.currentUser!!.displayName

        setUpTripSampleRecyclerView()
        showProgress(requireActivity(), "잠시만 기다려주세요")

        CoroutineScope(Dispatchers.IO).launch {

            // UserModel 의 imageName 값을 이용해 이미지를 가져오므로 순차적으로 실행되야함
            runBlocking {
                getUserModel() // UserModel 가져오기
            }
            getUserProfilePhoto() // UserProfileImage 가져오기

            hideProgress()
        }

        return binding.root
    }

    private suspend fun getUserModel() {
        // 한 번만 필요할 경우 get() 으로 호출
        val userModelResult = fireStore!!.collection(FirebaseConstants.COLLECTION_USERS).document(auth!!.uid!!).get().await()
        userModel = userModelResult.toObject()
        binding.tvProfileNickname.text = userModel!!.nickname


//        fireStore!!.collection(FirebaseConstants.COLLECTION_USERS).document(auth!!.uid!!).get()
//            .addOnCompleteListener { task ->
//                if(task.isSuccessful) {
//                    userModel = task.result.toObject<UserModel>()!!
//                    binding.tvProfileNickname.text = userModel!!.nickname
//                } else {
//                    showShortToast(requireContext(), "유저 모델 가져오기 실패")
//                }
//            }.await()
    }

    private suspend fun getUserProfilePhoto() {

        if (userModel!!.imageName != null && userModel!!.imageName != "") {
            val gsReference = storage!!.getReference("${FirebaseConstants.STORAGE_PROFILE}/${userModel!!.imageName!!}")

            gsReference.getBytes(TEN_MEGABYTE).addOnCompleteListener {
                val bmp = BitmapFactory.decodeByteArray(it.result, 0, it.result.size)
                binding.ivProfile.setImageBitmap(
                    Bitmap.createScaledBitmap(
                        bmp,
                        binding.ivProfile.width,
                        binding.ivProfile.height,
                        false
                    )
                )
            }.await()
        }
    }

    private fun setUpTripSampleRecyclerView() {
        val myPageTripModels = ArrayList<MyPageTripModel>()
        myPageTripModels.add(MyPageTripModel("", "서울, 남산타워", "2022년 01월 01일"))
        myPageTripModels.add(MyPageTripModel("", "부산, 마린시티", "2022년 10월 01일"))
        myPageTripModels.add(MyPageTripModel("", "부산, 마린시티", "2022년 10월 01일"))
        myPageTripModels.add(MyPageTripModel("", "부산, 마린시티", "2022년 10월 01일"))

        val tripSampleAdapter = MyPageTripAdapter(requireContext(), myPageTripModels)
        binding.rvMyPageTrip.addItemDecoration(VerticalGapDecoration(30))
        binding.rvMyPageTrip.adapter = tripSampleAdapter
        tripSampleAdapter.notifyDataSetChanged()
    }
}