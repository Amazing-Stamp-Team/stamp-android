package com.amazing.stamp.pages

import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import com.amazing.stamp.utils.ParentActivity
import com.example.stamp.R
import com.example.stamp.databinding.ActivityLocalFestivalsBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class HomeFestivalsActivity : ParentActivity() {
    private val binding by lazy { ActivityLocalFestivalsBinding.inflate(layoutInflater) }

    private val storage by lazy { Firebase.storage }
    private val fireStore by lazy { Firebase.firestore }
    private val auth by lazy { Firebase.auth }
    private val seasonCheck by lazy { Array(12) { false } }
    private var regionBtn = arrayOf<Button?>()
    private val region = arrayOf(
        "전국",
        "서울",
        "경기",
        "강원",
        "충북",
        "충남",
        "전북",
        "전남",
        "경북",
        "경남",
        "부산",
        "제주"
    )
    private val regionCheck by lazy { Array(region.size) { false } }

    private val TAG = "TAG_HOMEFESTIVALS"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarLocalAttractions)
        supportActionBar?.run {
            // 앱 바 뒤로가기 버튼 설정
            setDisplayHomeAsUpEnabled(true)
        }


        // 지역 버튼 클릭 이벤트
        regionBtn = Array<Button?>(region.size) { null }
        for(i in region.indices) {
            regionBtn[i] = findViewById(resources.getIdentifier("btn_attraction_region_$i", "id", packageName))
            regionBtn[i]?.setOnClickListener {
                if(i == 0) {
                    regionCheck[0] = !regionCheck[0]
                    for(j in 1 until region.size) {
                        regionCheck[j] = regionCheck[0]
                    }
                } else {
                    regionCheck[i] = !regionCheck[i]
                    var checkCnt = 0
                    for (j in 1 until region.size) {
                        if (regionCheck[j]) {
                            checkCnt++
                        }
                    }
                    regionCheck[0] = checkCnt == region.size - 1
                }
                btnRefresh()
            }
        }


        // 시즌 버튼 클릭 리스너 설정
        val seasonBtn = Array<Button?>(12) { null }

        for (i in 0..11) {
            seasonBtn[i] = findViewById(
                resources.getIdentifier(
                    "btn_attraction_season_${i + 1}",
                    "id",
                    packageName
                )
            )

            seasonBtn[i]?.setOnClickListener {
                if (seasonCheck[i]) {
                    seasonCheck[i] = false
                    seasonBtn[i]?.setBackgroundResource(R.drawable.btn_dark_gray_10)
                } else {
                    seasonCheck[i] = true
                    seasonBtn[i]?.setBackgroundResource(R.drawable.btn_main_10)
                }
            }
        }
    }

    private fun btnRefresh() {
        for(i in region.indices) {
            if (regionCheck[i]) {
                regionBtn[i]?.setBackgroundResource(R.drawable.btn_main_10)
            } else {
                regionBtn[i]?.setBackgroundResource(R.drawable.btn_dark_gray_10)
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