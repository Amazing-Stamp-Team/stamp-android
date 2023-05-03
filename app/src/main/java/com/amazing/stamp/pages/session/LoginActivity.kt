package com.amazing.stamp.pages.session

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.amazing.stamp.pages.MainActivity
import com.amazing.stamp.pages.RequireAuthorityActivity
import com.amazing.stamp.pages.map.LocationBasedViewActivity
import com.amazing.stamp.utils.FirebaseConstants
import com.amazing.stamp.utils.ParentActivity
import com.amazing.stamp.utils.Utils
import com.example.stamp.R
import com.example.stamp.databinding.ActivityLoginBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class LoginActivity : ParentActivity() {
    private lateinit var auth: FirebaseAuth
    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkPermission() //앱 구동시, 권한 체크

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
                onLogin("aaa@aaa.aaa", "aaaaaa")
            }
            btnTest.setOnClickListener {
                startActivity(Intent(applicationContext, LocationBasedViewActivity::class.java))
            }
        }
        dev()
    }

    private fun dev() {

        val fireStore = Firebase.firestore



    }


    private fun checkPermission() {
        val permissionCheckStorage =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) // 외부 저장소 권한 확인
        val permissionCheckLocation =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) // GPS 권한 확인

        if (permissionCheckStorage == PackageManager.PERMISSION_DENIED || permissionCheckLocation == PackageManager.PERMISSION_DENIED) {
            val intent = Intent(applicationContext, RequireAuthorityActivity::class.java)
            startActivity(intent)
            // 권한 없을 때 권한 획득 페이지 이동
        }
    }

    private val permissionListener = object : PermissionListener {
        override fun onPermissionGranted() {
            Utils.showShortToast(applicationContext, "권한이 승인되었습니다")
        }

        override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
            Utils.showShortToast(applicationContext, "권한이 없으면 기능을 사용할 수 없습니다.")

        }
    }

    private fun permissionCheck() {
        TedPermission.create()
            .setPermissionListener(permissionListener)
            .setDeniedMessage("[설정] > [권한] 에서 권한 허용을 할 수 있습니다")
            .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
            .check()
    }


    private fun onLogin(id: String, password: String) {
        FirebaseApp.initializeApp(applicationContext)

        showProgress(this, "잠시만 기다려주세요")

        auth.signInWithEmailAndPassword(id, password).addOnCompleteListener { task ->
            hideProgress()
            if (task.isSuccessful) {
                Toast.makeText(applicationContext, "로그인 성공", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(applicationContext, "로그인 실패", Toast.LENGTH_SHORT).show()
            }
        }


    }
}