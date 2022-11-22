package com.amazing.stamp.pages

import android.os.Bundle
import android.view.MenuItem
import com.amazing.stamp.utils.ParentActivity
import com.example.stamp.databinding.ActivityLocalFestivalsBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class HomeFestivalsActivity : ParentActivity(){
    private val binding by lazy { ActivityLocalFestivalsBinding.inflate(layoutInflater) }

    private val storage by lazy { Firebase.storage }
    private val fireStore by lazy { Firebase.firestore }
    private val auth by lazy { Firebase.auth }


    private val TAG = "TAG_HOMEFESTIVALS"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarLocalAttractions)
        supportActionBar?.run {
            // 앱 바 뒤로가기 버튼 설정
            setDisplayHomeAsUpEnabled(true)
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