package com.amazing.stamp.pages

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.amazing.stamp.adapter.MyPageTripAdapter
import com.amazing.stamp.models.FriendModel
import com.amazing.stamp.models.PostModel
import com.amazing.stamp.models.UserModel
import com.amazing.stamp.pages.session.LoginActivity
import com.amazing.stamp.utils.FirebaseConstants
import com.amazing.stamp.utils.ParentFragment
import com.amazing.stamp.utils.SecretConstants
import com.amazing.stamp.utils.Utils
import com.amazing.stamp.utils.Utils.showShortToast
import com.bumptech.glide.Glide
import com.example.stamp.R
import com.example.stamp.databinding.FragmentMyPageBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileInputStream


class MyPageFragment : ParentFragment() {
    val TAG = "MyPageFragment"
    private val binding by lazy { FragmentMyPageBinding.inflate(layoutInflater) }
    private val myPageTripModel = ArrayList<PostModel>()
    private val postIDs = ArrayList<String>()
    private val myPageTripAdapter by lazy {
        MyPageTripAdapter(
            requireActivity(),
            postIDs,
            myPageTripModel
        )
    }
    private var auth: FirebaseAuth? = null
    private var storage: FirebaseStorage? = null
    private val fireStore by lazy { Firebase.firestore }
    private var userModel: UserModel? = null
    private var imageUri: Uri? = null
    private var pathUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    companion object {
        const val PICK_FROM_ALBUM = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        auth = FirebaseAuth.getInstance()
        storage = Firebase.storage(SecretConstants.FIREBASE_STORAGE_URL)

        countFollow()
        setUpFeedRecyclerView()

        binding.tvProfileNickname.text = auth!!.currentUser!!.displayName

        binding.run {
            btnMyPageAllAttractions.setOnClickListener {
                val intent = Intent(requireActivity(), MyPageAttractionsActivity::class.java)
                startActivity(intent)
            }
            btnWithdrawal.setOnClickListener { withdrawal() }
            btnChangePw.setOnClickListener { changepw() }
            btnChangeNickname.setOnClickListener { changeNickname() }
            btnChangeProfilepicture.setOnClickListener { changephoto() }
        }

        CoroutineScope(Dispatchers.Main).launch {

            // UserModel 의 imageName 값을 이용해 이미지를 가져오므로 순차적으로 실행되야함
            getUserModel() // UserModel 가져오기
            getUserProfilePhoto() // UserProfileImage 가져오기

            //hideProgress()
        }

        return binding.root
    }

    private fun setUpFeedRecyclerView() {
        binding.rvMyPageTrip.adapter = myPageTripAdapter

        fireStore.collection(FirebaseConstants.COLLECTION_POSTS)
            .whereEqualTo(FirebaseConstants.POSTS_FIELD_WRITER, auth!!.currentUser?.uid)
            .orderBy(FirebaseConstants.POSTS_FIELD_CREATED_AT, Query.Direction.DESCENDING)
            .limit(3)
            .get().addOnSuccessListener { value ->
                value.documents.forEach { dc ->
                    val postModel = dc.toObject<PostModel>()
                    postIDs.add(dc.id)
                    myPageTripModel.add(postModel!!)
                }

                myPageTripAdapter.notifyDataSetChanged()
            }
    }


    private fun countFollow() {
        val uid = auth!!.currentUser!!.uid
        val followRef = fireStore?.collection("friends")?.document(uid)


        followRef?.get()
            ?.addOnSuccessListener { document ->
                if (document != null) {
                    val model =
                        document.toObject<FriendModel>() // 다큐먼트.toObject<모델>()을 변수로 받아와 접근 가능하게 한다
//                    Log.d(TAG,model!!.followers!!.size.toString()) 해당 유저 모델의 followers 변수의 길이를 받는다
//                    Log.d(TAG, model!!.followings!!.size.toString())

                    binding.tvProfileFollowerCount.text = model!!.followers!!.size.toString()
                    binding.tvProfileFollowingCount.text = model!!.followings!!.size.toString()

                } else {
                    Log.d(TAG, "No such document")
                }
            }
            ?.addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    private fun changeNickname() {
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
                fireStore.collection(FirebaseConstants.COLLECTION_USERS)
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
                            val ref = fireStore.collection(FirebaseConstants.COLLECTION_USERS)
                                ?.document(uid)

                            //ex)db.collection에서 db는 firestore로 지정해줘야함
                            ref?.update(
                                FirebaseConstants.USER_FIELD_NICKNAME,
                                newNickname //해당 컬렉션, 필드의 값을 update(변경) 한다
                            )?.addOnSuccessListener {
                                Toast.makeText(
                                    requireContext(),
                                    "닉네임이 변경되었습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                binding.tvProfileNickname.text =
                                    (newNickname) //마이페이지에 변경된 닉네임을 즉시 반영하여 출력해준다.
                                bottomSheetDialog.dismiss()

                                val profileUpdates =
                                    userProfileChangeRequest { //firestore의 displayNickname 도 동시 업데이트한다.
                                        displayName = newNickname
                                    }
                                val user = Firebase.auth.currentUser
                                user!!.updateProfile(profileUpdates)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Log.d(TAG, "User profile updated.")
                                        }
                                    }
                                Log.d(TAG, "DocumentSnapshot successfully updated!")
                            }
                                ?.addOnFailureListener { e ->
                                    Log.w(TAG, "Error updating document", e)
                                }
                        }
                        if (newNickname.isEmpty()) {
                            Toast.makeText(requireContext(), "변경하실 닉네임을 입력해주세요", Toast.LENGTH_SHORT)
                                .show()
                        }

                    }
            }
    }

    private fun changephoto() { //프로필 사진 변경하기
        val uid = auth!!.currentUser!!.uid
        val user = Firebase.auth.currentUser
        val ref = fireStore?.collection(FirebaseConstants.COLLECTION_USERS)?.document(uid)
        //0. 기존에 사용하던 프로필의 imageName을 받아 변수에 저장한다.


        //1. 갤러리를 실행하고 프로필 업로드할 사진을 고른 후, 파이어베이스 storage에 업로드하고 프로필의 이미지 주소를 update.
        selectProfile()

        // 2.새로운 프로필 사진이 업로드되면, 기존 프로필 사진은 삭제한다.


    }

    private fun selectProfile() { //갤러리 오픈 후, 인텐트로 선택한 이미지를 넘긴다
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        startActivityForResult(intent, PICK_FROM_ALBUM)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // 갤러리에서 선택한 이미지의 경로를 추출, 마이페이지의 프로필 사진을 변경하고 파이어베이스 storage에 업로드한다.
        val uid = auth!!.currentUser!!.uid
        val ref = fireStore?.collection(FirebaseConstants.COLLECTION_USERS)?.document(uid)

        if (resultCode != AppCompatActivity.RESULT_OK) return

        when (requestCode) {
            PICK_FROM_ALBUM -> {
                imageUri = data?.data
                pathUri = Utils.getPath(requireContext(), data!!.data!!)
                Log.d(TAG, "${pathUri} 사진 경로")
                binding.ivProfile.setImageURI(imageUri)
                binding.ivProfile.background =
                    ContextCompat.getDrawable(requireActivity(), R.drawable.bg_for_rounding_10)
                binding.ivProfile.clipToOutline = true

                var profilePhotoFileName: String? = null

                //Log.d(TAG,"${pathUri} 사진 업로드 과정")
                if (pathUri != null) {
                    profilePhotoFileName = "IMG_PROFILE_${uid}_${System.currentTimeMillis()}.png"
                    Log.d(TAG, "${pathUri} 파이어베이스에 들어갈 파일 경로")
                    val photoFileRef = storage!!.reference.child(FirebaseConstants.STORAGE_PROFILE)
                        .child(profilePhotoFileName)
                    val uploadTask = photoFileRef.putStream(FileInputStream(File(pathUri)))
                    val uploadResult = uploadTask
                }

                //프로필의 이미지 경로를 바뀐 프로필 이미지의 경로로 수정(update)한다
                ref?.update(
                    FirebaseConstants.USER_FIELD_IMAGE_NAME,
                    profilePhotoFileName //해당 컬렉션, 필드의 값을 update(변경) 한다
                )?.addOnSuccessListener {
                    Toast.makeText(requireContext(), "프로필 사진이 변경되었습니다.", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "DocumentSnapshot successfully updated!")
                }
                    ?.addOnFailureListener { e ->
                        Log.w(TAG, "Error updating document", e)
                    }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun changepw() {
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_changepassword, null)
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(bottomSheetView)

        bottomSheetDialog.show()

        bottomSheetDialog.findViewById<Button>(R.id.btn_change_pw_finish)?.setOnClickListener {
            val user = Firebase.auth.currentUser
            val newPassword: String
            val password =
                bottomSheetDialog.findViewById<EditText>(R.id.et_change_pw!!)?.text.toString()
            val passwordCheck =
                bottomSheetDialog.findViewById<EditText>(R.id.et_change_pw_check!!)?.text.toString()

            if (password == passwordCheck) {
                if (!passwordCheck.isEmpty()) {
                    showProgress(requireActivity(), "잠시만 기다려주세요")
                    newPassword = passwordCheck
                    //Log.d(TAG, "${newPassword}+${user}")
                    hideProgress()
                    user!!.updatePassword(newPassword).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "User password updated.")
                            Toast.makeText(requireContext(), "비밀번호가 변경되었습니다", Toast.LENGTH_SHORT)
                                .show()
                            startActivity(Intent(requireActivity(), LoginActivity::class.java))
                            requireActivity().finish()
                        } else {
                            Log.d(TAG, "Can't updated User password.")
                            Toast.makeText(requireContext(), "비밀번호 변경 실패", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
            if (password != passwordCheck) {
                Toast.makeText(requireContext(), "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
            }
            if (password.isEmpty() || passwordCheck.isEmpty()) {
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
    }

    private fun getUserProfilePhoto() {

        if (userModel!!.imageName != null && userModel!!.imageName != "") {
            val gsReference =
                storage!!.getReference("${FirebaseConstants.STORAGE_PROFILE}/${userModel!!.imageName!!}")

            gsReference.downloadUrl.addOnSuccessListener {
                Glide.with(requireContext()).load(it).centerCrop().into(binding.ivProfile)
            }
        }
    }
}