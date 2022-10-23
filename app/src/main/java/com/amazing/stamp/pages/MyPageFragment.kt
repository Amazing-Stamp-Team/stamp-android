package com.amazing.stamp.pages

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.amazing.stamp.adapter.MyPageTripAdapter
import com.amazing.stamp.adapter.decoration.VerticalGapDecoration
import com.amazing.stamp.models.MyPageTripModel
import com.amazing.stamp.models.UserModel
import com.amazing.stamp.pages.session.LoginActivity
import com.amazing.stamp.utils.FirebaseConstants
import com.amazing.stamp.utils.ParentFragment
import com.amazing.stamp.utils.SecretConstants
import com.amazing.stamp.utils.Utils.showShortToast
import com.example.stamp.R
import com.example.stamp.databinding.FragmentMyPageBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
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

        binding.run {
            btnWithdrawal.setOnClickListener { withdrawal() }
            btnChangePw.setOnClickListener { changepw() }
            btnChangeNickname.setOnClickListener { changenickname() }
        }

        setUpTripSampleRecyclerView()
        showProgress(requireActivity(), "잠시만 기다려주세요")

        CoroutineScope(Dispatchers.IO).launch {

            // UserModel 의 imageName 값을 이용해 이미지를 가져오므로 순차적으로 실행되야함
            getUserModel() // UserModel 가져오기
            getUserProfilePhoto() // UserProfileImage 가져오기

            hideProgress()
        }

        return binding.root
    }

    private fun changenickname() {
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_changenickname, null)
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        var nicknameDuplicateCheck = false

        bottomSheetDialog.setContentView(bottomSheetView)

        bottomSheetDialog.show()

        bottomSheetDialog.findViewById<Button>(R.id.btn_change_nickname_finish)
            ?.setOnClickListener {
                val newNickname =
                    bottomSheetDialog.findViewById<EditText>(R.id.et_change_nickname!!)?.text.toString()

                //닉네임 중복체크
                fireStore!!.collection(FirebaseConstants.COLLECTION_USERS)
                    .whereEqualTo(FirebaseConstants.USER_FIELD_NICKNAME, newNickname)
                    .get()
                    .addOnCompleteListener {
                        if (it.result.isEmpty) {
                            nicknameDuplicateCheck = true
                        } else {
                            showShortToast(requireContext(), "이미 존재하는 닉네임입니다")
                        }

                        if (!newNickname.isEmpty() && nicknameDuplicateCheck) {
                            val uid = auth!!.currentUser!!.uid
                            val ref = fireStore?.collection(FirebaseConstants.COLLECTION_USERS)?.document(uid)

                            //ex)db.collection에서 db는 firestore로 지정해줘야함
                            ref?.update(FirebaseConstants.USER_FIELD_NICKNAME, newNickname //해당 컬렉션, 필드의 값을 update(변경) 한다
                            )?.addOnSuccessListener {
                                    Toast.makeText(requireContext(), "닉네임이 변경되었습니다.", Toast.LENGTH_SHORT).show()
                                    bottomSheetDialog.dismiss()
                                    Log.d(TAG, "DocumentSnapshot successfully updated!")
                                }
                                ?.addOnFailureListener { e -> Log.w(TAG, "Error updating document", e)
                                }
                        }
                        if (newNickname.isEmpty()) {
                            Toast.makeText(requireContext(), "변경하실 닉네임을 입력해주세요", Toast.LENGTH_SHORT)
                                .show()
                        }

                    }
            }
    }
    

    private fun changepw(){
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_changepassword, null)
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(bottomSheetView)

        bottomSheetDialog.show()

        bottomSheetDialog.findViewById<Button>(R.id.btn_change_pw_finish)?.setOnClickListener {
                val user = Firebase.auth.currentUser
                val newPassword : String
                val password = bottomSheetDialog.findViewById<EditText>(R.id.et_change_pw!!)?.text.toString()
                val passwordCheck = bottomSheetDialog.findViewById<EditText>(R.id.et_change_pw_check!!)?.text.toString()

                if (password == passwordCheck){
                    if (!passwordCheck.isEmpty()){
                        showProgress(requireActivity(), "잠시만 기다려주세요")
                        newPassword = passwordCheck
                        //Log.d(TAG, "${newPassword}+${user}")
                        hideProgress()
                        user!!.updatePassword(newPassword).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(TAG, "User password updated.")
                                Toast.makeText(requireContext(), "비밀번호가 변경되었습니다", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(requireActivity(), LoginActivity::class.java))
                                requireActivity().finish()
                            }
                            else{
                                Log.d(TAG, "Can't updated User password.")
                                Toast.makeText(requireContext(), "비밀번호 변경 실패", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                if(password != passwordCheck){
                    Toast.makeText(requireContext(), "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
                }
                if(password.isEmpty() || passwordCheck.isEmpty()){
                    Toast.makeText(requireContext(), "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                }

            }

    }

    private fun withdrawal() {
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_withdrawal, null)
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(bottomSheetView)

        bottomSheetDialog.show()


        bottomSheetDialog.findViewById<Button>(R.id.btn_withdrawal_yes)?.setOnClickListener {
            showProgress(requireActivity(), "잠시만 기다려주세요")
            bottomSheetDialog.dismiss()

            val uid = auth!!.currentUser!!.uid

            auth!!.currentUser?.delete()?.addOnCompleteListener {
                hideProgress()
                if (it.isSuccessful) {

                    fireStore!!.collection(FirebaseConstants.COLLECTION_USERS)
                        .document(uid).delete().addOnCompleteListener {
                            showShortToast(requireContext(), "계정이 삭제되었습니다")
                            startActivity(Intent(requireActivity(), LoginActivity::class.java))
                            requireActivity().finish()
                        }
                } else {
                    showShortToast(requireContext(), "계정 삭제에 실패했습니다")
                }
            }
        }

        bottomSheetDialog.findViewById<Button>(R.id.btn_withdrawal_no)?.setOnClickListener {
            showShortToast(requireContext(), "아니오 클릭했음")
            bottomSheetDialog.dismiss()
        }
    }

    private suspend fun getUserModel() {
        // 한 번만 필요할 경우 get() 으로 호출
        val userModelResult =
            fireStore!!.collection(FirebaseConstants.COLLECTION_USERS).document(auth!!.uid!!).get()
                .await()
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
            val gsReference =
                storage!!.getReference("${FirebaseConstants.STORAGE_PROFILE}/${userModel!!.imageName!!}")

            gsReference.getBytes(FirebaseConstants.TEN_MEGABYTE).addOnCompleteListener {
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