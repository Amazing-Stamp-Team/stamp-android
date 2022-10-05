package com.amazing.stamp.pages.session

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import com.example.stamp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    private var auth: FirebaseAuth? = null
    private val binding by lazy { ActivityRegisterBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = Firebase.auth

        setSupportActionBar(binding.toolbarRegister)
        supportActionBar?.run {
            // 앱 바 뒤로가기 버튼 설정
            setDisplayHomeAsUpEnabled(true)
        }

        binding.run {
            btnRegisterFinish.setOnClickListener { onRegister() }
        }
    }

    private fun onRegister() {
        binding.run {
            val email = etEmail.text.toString()
            val password = etPw.text.toString()
            val passwordCheck = etPwCheck.text.toString()
            val nickname = etNickname.text.toString()

            if (email.isEmpty()) {
                Toast.makeText(applicationContext, "이름을 입력해주세요", Toast.LENGTH_SHORT).show()
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

            auth?.createUserWithEmailAndPassword(email, password)
                ?.addOnCompleteListener(this@RegisterActivity) { task ->
                    if(task.isSuccessful) {
                        Toast.makeText(applicationContext, "계정이 생성되었습니다", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(applicationContext, "계정 생성에 실패하였습니다",Toast.LENGTH_SHORT).show()
                    }
                }

        }

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