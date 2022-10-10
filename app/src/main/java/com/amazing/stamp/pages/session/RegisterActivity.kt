package com.amazing.stamp.pages.session

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.amazing.stamp.models.UserModel
import com.amazing.stamp.utils.ParentActivity
import com.amazing.stamp.utils.Utils
import com.example.stamp.databinding.ActivityRegisterBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.amazing.stamp.utils.Utils.showShortToast
import com.example.stamp.R
import java.io.File

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
        database = FirebaseDatabase.getInstance()
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

            auth?.createUserWithEmailAndPassword(email, password)
                ?.addOnCompleteListener(this@RegisterActivity) { task ->
                    hideProgress()

                    if (task.isSuccessful) {

                        val uid = task.result.user!!.uid
                        val file = if (pathUri == null) null else Uri.fromFile(File(pathUri))

                        if (pathUri == null) {
                            val userModel = UserModel(nickname, uid, null, null)
                            database!!.reference.child("users").child(uid).setValue(userModel)
                        } else {
                            val storageReference = storage!!.reference.child("usersprofileImages")
                                .child("uid/" + file?.lastPathSegment)

                            storageReference.putFile(imageUri!!).addOnCompleteListener { task2 ->

//                                val imageUrl = task2.result.storage.downloadUrl
//
//                                while (!imageUrl.isComplete) {
//                                }
//
//                                val userModel =
//                                    UserModel(nickname, uid, imageUrl.result.toString(), null)
//                                database!!.reference.child("users").child(uid).setValue(userModel)
                            }
                        }

//                    if (task.isSuccessful) {
//
//                        task.result.user!!.updateProfile(userProfileChangeRequest {
//                            displayName = nickname
//                        }).addOnCompleteListener {
//                            if (it.isSuccessful) {
//                                showShortToast(applicationContext, "계정이 생성되었습니다")
//                                finish()
//                            } else {
//                                showShortToast(applicationContext, "계정 생성에 실패하였습니다")
//                            }
//                        }
//                    } else {
//                        Log.d(TAG, "onRegister: ${task.exception.toString()}")
//                        showShortToast(applicationContext, "계정 생성에 실패하였습니다")
//                    }
                    } else {
                        showShortToast(applicationContext, "계정 생성 실패")
                    }
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