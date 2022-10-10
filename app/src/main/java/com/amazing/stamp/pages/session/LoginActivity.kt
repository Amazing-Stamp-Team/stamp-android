package com.amazing.stamp.pages.session

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.stamp.R
import com.example.stamp.databinding.ActivityLoginBinding
import com.amazing.stamp.pages.MainActivity
import com.amazing.stamp.utils.ParentActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : ParentActivity() {
    private lateinit var auth: FirebaseAuth
    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(binding.root)


        val loginSheet = layoutInflater.inflate(R.layout.bottom_login_popup, null)
        val loginSheetDialog = BottomSheetDialog(this)
        loginSheetDialog.setContentView(loginSheet)

        binding.run {
            btnLogin.setOnClickListener {
                loginSheetDialog.show()

                val et_id = loginSheet.findViewById<EditText>(R.id.et_login_id)
                val et_pw = loginSheet.findViewById<EditText>(R.id.et_login_pw)
                val login = loginSheet.findViewById<Button>(R.id.btn_login)

                login.setOnClickListener {
                    val id = et_id.text.toString()
                    val pw = et_pw.text.toString()
                    loginSheetDialog.dismiss()
                    onLogin(id, pw)
                }
            }

            btnRegister.setOnClickListener {
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intent)
            }

            // dev
            btnDev.setOnClickListener {
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun onLogin(id: String, password: String) {
        FirebaseApp.initializeApp(applicationContext)

        showProgress(this, "잠시만 기다려주세요")

        auth.signInWithEmailAndPassword(id, password).addOnCompleteListener { task ->
            hideProgress()
            if(task.isSuccessful) {
                Toast.makeText(applicationContext, "로그인 성공",Toast.LENGTH_SHORT).show()
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(applicationContext, "로그인 실패",Toast.LENGTH_SHORT).show()
            }
        }


    }
}