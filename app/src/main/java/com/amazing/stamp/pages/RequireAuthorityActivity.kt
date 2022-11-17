package com.amazing.stamp.pages

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.amazing.stamp.utils.ParentActivity
import com.amazing.stamp.utils.Utils
import com.example.stamp.databinding.ActivityRequireAuthorityBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission

class RequireAuthorityActivity : ParentActivity() {
    private val binding by lazy { ActivityRequireAuthorityBinding.inflate(layoutInflater) }

    private val storage by lazy { Firebase.storage }
    private val fireStore by lazy { Firebase.firestore }
    private val auth by lazy { Firebase.auth }

    private val TAG = "TAG_REQUIREAUTHORITY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        binding.run {
            btnRequireAuthority.setOnClickListener {
                permissionCheck()
            }
        }
    }


    private val permissionListener = object : PermissionListener {
        override fun onPermissionGranted() { //권한이 승인 되었을때
            Utils.showShortToast(applicationContext, "권한이 승인되었습니다")
            checkPermissionAndQuit()
        }

        override fun onPermissionDenied(deniedPermissions: MutableList<String>?) { //권한 설정을 거부하였을 때
            Utils.showShortToast(applicationContext, "권한이 없으면 해당 기능을 사용할 수 없습니다.")
        }
    }

    private fun checkPermissionAndQuit(){ //권한을 체크하고 모두 승인되었을 떄, 해당 액티비티를 나간다.
        val permissionCheckStorage =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) // 외부 저장소 권한 확인
        val permissionCheckLocation =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) // GPS 권한 확인

        if (permissionCheckStorage == PackageManager.PERMISSION_GRANTED && permissionCheckLocation == PackageManager.PERMISSION_GRANTED) {
            finish()
            // 권한 없을 때 권한 획득 페이지 이동
        }
    }

    private fun permissionCheck() {
        TedPermission.create()
            .setPermissionListener(permissionListener)
            .setDeniedMessage("[설정] > [권한] 에서 권한 허용을 할 수 있습니다")
            .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE) //설정할 권한 개수대로(GPS, 외부 저장소)
            .check()

    }
}