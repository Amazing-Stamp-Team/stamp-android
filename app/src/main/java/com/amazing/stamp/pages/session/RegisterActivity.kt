package com.amazing.stamp.pages.session

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.amazing.stamp.models.FriendModel
import com.amazing.stamp.models.UserModel
import com.amazing.stamp.utils.FirebaseConstants
import com.amazing.stamp.utils.ParentActivity
import com.amazing.stamp.utils.Utils
import com.example.stamp.databinding.ActivityRegisterBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.amazing.stamp.utils.Utils.showShortToast
import com.example.stamp.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileInputStream



class RegisterActivity : ParentActivity() {
    private val TAG = "RegisterActivity"
    private var auth: FirebaseAuth? = null
    private var storage: FirebaseStorage? = null
    private val binding by lazy { ActivityRegisterBinding.inflate(layoutInflater) }
    private var imageUri: Uri? = null
    private var pathUri: String? = null
    private var fireStore :FirebaseFirestore? = null

    private var nicknameDuplicateCheck = false // 닉네임 중복 체크 변수

    private val permissionListener = object :PermissionListener{
        override fun onPermissionGranted() {
            showShortToast(applicationContext, "권한이 승인되었습니다")
            selectProfile()
        }

        override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
            showShortToast(applicationContext, "권한이 없으면 사진 기능을 사용할 수 없습니다.")
        }
    }

    companion object {
        const val PICK_FROM_ALBUM = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        fireStore = Firebase.firestore

        setSupportActionBar(binding.toolbarRegister)
        supportActionBar?.run {
            // 앱 바 뒤로가기 버튼 설정
            setDisplayHomeAsUpEnabled(true)
        }

        binding.run {
            btnRegisterFinish.setOnClickListener { onRegister() }
            ivProfileAdd.setOnClickListener {
                permissionCheck()
            }
            btnNicknameDupl.setOnClickListener { checkDuplicatedNickname() }

            etNickname.addTextChangedListener {
                nicknameDuplicateCheck = false
                tvNicknameDuplCheck.visibility = View.GONE
            }
        }
    }

    private fun permissionCheck() {
        TedPermission.create()
            .setPermissionListener(permissionListener)
            .setDeniedMessage("[설정] > [권한] 에서 권한 허용을 할 수 있습니다")
            .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
            .check()
    }

    private fun checkDuplicatedNickname() {
        val nickname = binding.etNickname.text.toString()

        if(nickname.isEmpty()) {
            showShortToast(applicationContext, "닉네임을 입력해주세요")
            return
        }

        fireStore!!.collection(FirebaseConstants.COLLECTION_USERS).whereEqualTo(FirebaseConstants.USER_FIELD_NICKNAME, nickname)
            .get()
            .addOnCompleteListener {
                if(it.result.isEmpty) {
                    nicknameDuplicateCheck = true
                    binding.tvNicknameDuplCheck.visibility = View.VISIBLE
                } else {
                    showShortToast(applicationContext,"이미 존재하는 닉네임입니다")
                }
            }
    }

    private fun selectProfile() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        startActivityForResult(intent, PICK_FROM_ALBUM)
    }

    private fun onRegister() {

        binding.run {
            val email = etEmail.text.toString()
            val password = etPw.text.toString()
            val passwordCheck = etPwCheck.text.toString()
            val nickname = etNickname.text.toString()

            if (email.isEmpty()) {
                Toast.makeText(applicationContext, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
                return
            }

            if (password.isEmpty()) {
                Toast.makeText(applicationContext, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                return
            }

            if (password != passwordCheck) {
                Toast.makeText(applicationContext, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
                return
            }

            if (nickname.isEmpty()) {
                Toast.makeText(applicationContext, "닉네임을 입력해주세요", Toast.LENGTH_SHORT).show()
                return
            }

            if(!nicknameDuplicateCheck) {
                showShortToast(applicationContext, "닉네임 중복체크를 해주세요")
                return
            }

            showProgress(this@RegisterActivity, "잠시만 기다려주세요")



            /*
                회원가입 시작부
             */


            // 콜백 중첩 현상을 방지하기 위해 Coroutine - await 사용
            CoroutineScope(Dispatchers.IO).launch {
                var uid: String? = null

                // Step 1. Email, Password 로 계정 생성
                val authResult = auth!!.createUserWithEmailAndPassword(email, password).await()
                uid = authResult.user!!.uid


                // Step 2. 프로필 사진 업로드
                var profilePhotoFileName: String? = null

                if (pathUri != null) {
                    //profilePhotoFileName = "IMG_PROFILE_${uid}_${System.currentTimeMillis()}.png"

                    val photoFileRef = storage!!.reference.child(FirebaseConstants.STORAGE_PROFILE).child(uid)
                    val uploadTask = photoFileRef.putStream(FileInputStream(File(pathUri)))
                    val uploadResult = uploadTask.await()
                }


                // Step 3. UserModel 객체 업로드
                val userModel = UserModel(uid, email, nickname, profilePhotoFileName)
                fireStore?.collection(FirebaseConstants.COLLECTION_USERS)?.document(uid)?.set(userModel)?.await()

//                fireStore?.collection(FirebaseConstants.COLLECTION_USERS)?.document(uid!!)?.set(userModel)
//                    ?.addOnCompleteListener {
//                        hideProgress()
//                        if(it.isSuccessful) showShortToast(applicationContext, "계정 생성에 성공하였습니다")
//                        else showShortToast(applicationContext, "닉네임 실패")
//                        finish()
//                    }

                // Step 4. Friend Collection 생성
                val friendModel = FriendModel(ArrayList(), ArrayList())
                fireStore?.collection(FirebaseConstants.COLLECTION_FRIENDS)?.document(uid)?.set(friendModel)?.await()
                hideProgress()
                finish()

            }
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != RESULT_OK) return

        when (requestCode) {
            PICK_FROM_ALBUM -> {
                imageUri = data?.data
                pathUri = Utils.getPath(applicationContext, data!!.data!!)
                binding.ivProfile.setImageURI(imageUri)
                binding.ivProfile.background = getDrawable(R.drawable.bg_for_rounding_10)
                binding.ivProfile.clipToOutline = true
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 앱 바 클릭 이벤트
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}