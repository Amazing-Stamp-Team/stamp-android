package com.amazing.stamp.pages.session

import android.content.Intent
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
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

    var nicknameDuplicateCheck = false // 닉네임 중복 체크 변수

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
            btnNicknameDupl.setOnClickListener { checkDuplicatedNickname() }

            etNickname.addTextChangedListener {
                nicknameDuplicateCheck = false
                tvNicknameDuplCheck.visibility = View.GONE
            }
        }
    }

    private fun checkDuplicatedNickname() {
        val nickname = binding.etNickname.text.toString()

        if(nickname.isEmpty()) {
            showShortToast(applicationContext, "닉네임을 입력해주세요")
            return
        }

        database!!.getReference(FirebaseConstants.DB_REF_USERS).orderByChild("nickname").equalTo(nickname).get()
            .addOnCompleteListener{

                if(it.result.childrenCount == 0L) {
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

            var uid: String? = null

            // 콜백 중첩 현상을 방지하기 위해 Coroutine - await 사용
            CoroutineScope(Dispatchers.IO).launch {

                // Step 1. Email, Password 로 계정 생성
                auth!!.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this@RegisterActivity) { task ->
                        if (task.isSuccessful) {
                            uid = task.result.user!!.uid
                        } else {
                            showShortToast(applicationContext, task.exception.toString())
                            hideProgress()
                        }
                    }.await()


                // Step 2. 프로필 사진 업로드
                var profilePhotoFileName: String? = null

                if (pathUri != null) {
                    profilePhotoFileName = "IMG_PROFILE_${uid}_${System.currentTimeMillis()}.png"

                    val photoFileRef = storage!!.reference.child("profile").child(profilePhotoFileName)
                    //val uploadTask = photoFileRef.putFile(Uri.fromFile(file))
                    val uploadTask = photoFileRef.putStream(FileInputStream(File(pathUri)))

                    uploadTask.addOnCompleteListener {
                        if (it.isSuccessful) {
                            showShortToast(applicationContext, "프로필 사진 업로드 성공")
                        } else {
                            hideProgress()
                            showShortToast(applicationContext, "프로필 사진 업로드 실패")
                        }
                    }.await()
                }

                // Step 3. UserModel 객체 업로드
                val userModel = UserModel(uid!!, email, nickname, profilePhotoFileName)

                database!!.getReference(FirebaseConstants.DB_REF_USERS).child(uid!!).setValue(userModel)
                    .addOnCompleteListener {
                        hideProgress()
                        if (it.isSuccessful) {
                            showShortToast(applicationContext, "계정 생성에 성공하였습니다")
                            finish()
                        } else showShortToast(applicationContext, "닉네임 실패")
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