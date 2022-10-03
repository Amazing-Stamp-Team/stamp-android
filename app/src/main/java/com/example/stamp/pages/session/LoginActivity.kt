package com.example.stamp.pages.session

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.stamp.R
import com.example.stamp.databinding.ActivityLoginBinding
import com.example.stamp.pages.MainActivity
import com.google.android.material.bottomsheet.BottomSheetDialog

class LoginActivity : AppCompatActivity() {
    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

                    Toast.makeText(applicationContext, "$id\n$pw", Toast.LENGTH_SHORT).show()
                    loginSheetDialog.dismiss()

                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
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
}