package com.amazing.stamp.pages

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amazing.stamp.adapter.MyPageTripAdapter
import com.amazing.stamp.adapter.decoration.VerticalGapDecoration
import com.amazing.stamp.models.MyPageTripModel
import com.amazing.stamp.models.UserModel
import com.amazing.stamp.utils.FirebaseConstants
import com.amazing.stamp.utils.SecretConstants
import com.example.stamp.databinding.FragmentMyPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.installations.Utils
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class MyPageFragment : Fragment() {
    val TAG = "MyPageFragment"
    private val binding by lazy { FragmentMyPageBinding.inflate(layoutInflater) }
    private var auth: FirebaseAuth? = null
    private var database: FirebaseDatabase? = null
    private var storage:FirebaseStorage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        auth = FirebaseAuth.getInstance()
        database = Firebase.database(SecretConstants.FIREBASE_REALTIME_DATABASE_URL)
        storage = FirebaseStorage.getInstance()

        binding.tvProfileNickname.text = auth!!.currentUser!!.displayName

        setUpTripSampleRecyclerView()

        CoroutineScope(Dispatchers.IO).launch {
            getUserModel()
        }

        return binding.root
    }

    private suspend fun getUserModel() {
        // 한 번만 필요할 경우 get() 으로 호출
//        database!!.getReference(FirebaseConstants.DB_REF_USERS).get().addOnCompleteListener {
//
//        }



        database!!.getReference(FirebaseConstants.DB_REF_USERS)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val dto = snapshot.getValue<UserModel>()
                    Log.d(TAG, "onDataChange: ${dto!!.nickname}")
                    binding.tvProfileNickname.text = dto?.nickname

//                    val photoFileRef = storage!!.reference.child("profile").child("IMG_PROFILE_F3cjkXMD1TcUDclqxKB4wVS9ne43_1665589384351.png\n")
//                    photoFileRef.downloadUrl.addOnCompleteListener {
//                        binding.ivProfile.setImageURI(it.result)
//                    }


                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
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