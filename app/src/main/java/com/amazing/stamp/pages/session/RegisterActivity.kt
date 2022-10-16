package com.amazing.stamp.pages.session

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.amazing.stamp.models.UserModel
import com.amazing.stamp.utils.FirebaseConstants
import com.amazing.stamp.utils.ParentActivity
import com.amazing.stamp.utils.SecretConstants
import com.amazing.stamp.utils.Utils
import com.example.stamp.databinding.ActivityRegisterBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.amazing.stamp.utils.Utils.showShortToast
import com.example.stamp.R
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileInputStream

class RegisterActivity : ParentActivity() {
    private val TAG = "RegisterActivity"
    private var auth: FirebaseAuth? = null
    private var database: FirebaseDatabase? = null
    private var storage: FirebaseStorage? = null
    private val binding by lazy { ActivityRegisterBinding.inflate(layoutInflater) }
    private var imageUri: Uri? = null
    private var pathUri: String? = null

    companion object {
        val PICK_FROM_ALBUM = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        database = Firebase.database(SecretConstants.FIREBASE_REALTIME_DATABASE_URL)
        storage = FirebaseStorage.getInstance()

        setSupportActionBar(binding.toolbarRegister)
        supportActionBar?.run {
            // 앱 바 뒤로가기 버튼 설정
            setDisplayHomeAsUpEnabled(true)
        }

        binding.run {
            btnRegisterFinish.setOnClickListener { onRegister() }
            ivProfileAdd.setOnClickListener { selectProfile() }
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
            showProgress(this@RegisterActivity, "잠시만 기다려주세요")


            // Step 1. Email, Password 로 계정 생성
            auth?.createUserWithEmailAndPassword(email, password)
                ?.addOnCompleteListener(this@RegisterActivity) { task ->
                    hideProgress()

                    if (task.isSuccessful) {
                        val uid = task.result.user!!.uid

                        // Step 2. 프로필 사진 업로드
                        // 콜백 중첩 현상을 방지하기 위해 Coroutine - await 사용
                        CoroutineScope(Dispatchers.IO).launch {
                            var profilePhotoFileName: String? = null

                            if(pathUri != null) {
                                profilePhotoFileName = "IMG_PROFILE_${uid}_${System.currentTimeMillis()}.png"
                                
                                
                                val photoFileRef = storage!!.reference.child("profile").child(profilePhotoFileName)
                                //val uploadTask = photoFileRef.putFile(Uri.fromFile(file))
                                val uploadTask = photoFileRef.putStream(FileInputStream(File(pathUri)))
                                
                                uploadTask.addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        showShortToast(applicationContext, "프로필 사진 업로드 성공")
                                    } else {
                                            showShortToast(applicationContext, "프로필 사진 업로드 실패")
                                    }
                                }.await()
                            }

                            // Step 3. UserModel 객체 업로드
                            val userModel = UserModel(uid, email, nickname, profilePhotoFileName)

                            database!!.getReference(FirebaseConstants.DB_REF_USERS).child(uid)
                                .setValue(userModel)
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        showShortToast(applicationContext, "계정 생성에 성공하였습니다")
                                        finish()
                                    }
                                    else showShortToast(applicationContext, "닉네임 실패")
                                }
                        }
                    } else {
                        showShortToast(applicationContext, "계정 생성에 실패하였습니다")
                    }
                }
        }
    }

    private suspend fun firebasePutProfile(uid: String): String? {
        val profilePhotoFileName = "IMG_PROFILE_${uid}_${System.currentTimeMillis()}.png"
        val photoFileRef = storage!!.reference.child("profile").child(profilePhotoFileName)
        val uploadTask = photoFileRef.putFile(Uri.fromFile(File(pathUri)))
        var photoUri: Uri? = null

        uploadTask.addOnCompleteListener {
            if (it.isSuccessful) {
                photoUri = it.result.uploadSessionUri
                Log.d(TAG, "dev: point2")
                showShortToast(applicationContext, "Photo Upload Success")
            } else {
                showShortToast(applicationContext, "Photo Upload Fail")
            }
        }.await()

        return if (photoUri == null) null else photoUri.toString()
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