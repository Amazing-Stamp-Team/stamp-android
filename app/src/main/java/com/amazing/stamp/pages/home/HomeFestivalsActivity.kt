package com.amazing.stamp.pages.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import com.amazing.stamp.adapter.FestivalAdapter
import com.amazing.stamp.api.KorTripInfoAPI
import com.amazing.stamp.api.dto.festivalDTO.FestivalDTO
import com.amazing.stamp.api.dto.festivalDTO.Item
import com.amazing.stamp.models.FestivalModel
import com.amazing.stamp.utils.ParentActivity
import com.amazing.stamp.utils.SecretConstants
import com.example.stamp.R
import com.example.stamp.databinding.ActivityLocalFestivalsBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Query
import java.util.*
import kotlin.collections.ArrayList


class HomeFestivalsActivity : ParentActivity() {
    private val binding by lazy { ActivityLocalFestivalsBinding.inflate(layoutInflater) }

    private val korTripInfoAPI by lazy { KorTripInfoAPI.create() }
    private val seasonCheck by lazy { Array(12) { true } }
    private var regionBtn = arrayOf<Button?>()
    private var seasonBtn = arrayOf<Button?>()
    private val region =
        arrayOf(
            "전국",
            "서울특별시",
            "경기도",
            "강원도",
            "충청북도",
            "충청남도",
            "전라북도",
            "전라남도",
            "경상북도",
            "경상남도",
            "부산광역시",
            "제주특별자치도"
        )
    private val regionCheck by lazy { Array(region.size) { true } }
    private val TAG = "TAG_HOMEFESTIVALS"
    private val festivalOriginItems = ArrayList<Item>()
    private val festivalItems = ArrayList<Item>()
    private val festivalAdapter by lazy {
        FestivalAdapter(
            applicationContext,
            festivalItems
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarLocalAttractions)
        supportActionBar?.run {
            // 앱 바 뒤로가기 버튼 설정
            setDisplayHomeAsUpEnabled(true)
        }

        showProgress(this@HomeFestivalsActivity, "축제 정보를 가져오는 중")
        setUpBtnEvent()
        btnRefresh()
        getFestival()

        festivalAdapter.itemClickListener = object : FestivalAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val intent = Intent(Intent.ACTION_VIEW)
                val uri: Uri = Uri.parse("http://www.naver.com")
                intent.data = uri
                startActivity(intent)
            }
        }

        binding.rvLocalAttractions.adapter = festivalAdapter
    }

    private fun getFestival() {
        Log.d(TAG, "getFestival: ${KorTripInfoAPI.tripDateFormat.format(System.currentTimeMillis())}")
        val festivalCall = korTripInfoAPI.getFestivalInfoCall(
            SecretConstants.KOR_TRIP_INFO_SERVICE_KEY,
            KorTripInfoAPI.tripDateFormat.format(System.currentTimeMillis()).toInt(),
            "json",
            "AND",
            "STAMF",
            "B",
            1000
        )

        festivalCall.enqueue(object:Callback<FestivalDTO>{
            override fun onResponse(call: Call<FestivalDTO>, response: Response<FestivalDTO>) {
                val festivalDTO = response.body()
                festivalDTO?.response?.body?.items?.item?.forEach {
                    festivalOriginItems.add(it)
                    festivalItems.add(it)
                }
                festivalAdapter.notifyDataSetChanged()
                hideProgress()
            }

            override fun onFailure(call: Call<FestivalDTO>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
                hideProgress()
            }
        })
    }

    private fun filterFestival() {
        festivalItems.clear()

        val addressFilter = ArrayList<Item>()

        for (i in regionCheck.indices) {
            if (regionCheck[i]) {
                addressFilter.addAll(festivalOriginItems.filter { it.addr1.startsWith(region[i]) })
            }
        }

        val seasonFilter = ArrayList<Item>()

        for (i in seasonCheck.indices) {
            if (seasonCheck[i]) {
                seasonFilter.addAll(addressFilter.filter { it.eventenddate.substring(4, 6).toInt() >= i + 1 && it.eventenddate.substring(4, 6).toInt() <= i + 3 })
            }
        }

        // 시즌별 필터링 과정에서 중복데이터 발생
        // distinctBy를 통해 제목 기준으로 중복데이터 제거
        festivalItems.addAll(seasonFilter.distinctBy { it.title })
        festivalAdapter.notifyDataSetChanged()
    }


    private fun setUpBtnEvent() {
        // 지역 버튼 클릭 이벤트
        regionBtn = Array<Button?>(region.size) { null }
        for (i in region.indices) {
            regionBtn[i] =
                findViewById(resources.getIdentifier("btn_attraction_region_$i", "id", packageName))
            regionBtn[i]?.setOnClickListener {
                if (i == 0) {
                    regionCheck[0] = !regionCheck[0]
                    for (j in 1 until region.size) {
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
                filterFestival()
            }
        }


        // 시즌 버튼 클릭 리스너 설정
        seasonBtn = Array<Button?>(12) { null }

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
                btnRefresh()
                filterFestival()
            }
        }
    }

    private fun btnRefresh() {
        for (i in region.indices) {
            if (regionCheck[i]) {
                regionBtn[i]?.setBackgroundResource(R.drawable.btn_main_10)
            } else {
                regionBtn[i]?.setBackgroundResource(R.drawable.btn_dark_gray_10)
            }
        }

        for (i in seasonCheck.indices) {
            if (seasonCheck[i]) {
                seasonBtn[i]?.setBackgroundResource(R.drawable.btn_main_10)
            } else {
                seasonBtn[i]?.setBackgroundResource(R.drawable.btn_dark_gray_10)
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