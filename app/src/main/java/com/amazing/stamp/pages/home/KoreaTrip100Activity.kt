package com.amazing.stamp.pages.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.amazing.stamp.adapter.Trip100Adapter
import com.amazing.stamp.models.KoreaTrip100
import com.amazing.stamp.utils.FirebaseConstants
import com.example.stamp.R
import com.example.stamp.databinding.ActivityKoreaTrip100Binding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class KoreaTrip100Activity : AppCompatActivity() {
    private val fireStore by lazy { Firebase.firestore }
    private val binding by lazy { ActivityKoreaTrip100Binding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarKorea100)
        supportActionBar?.run {
            // 앱 바 뒤로가기 버튼 설정
            setDisplayHomeAsUpEnabled(true)
        }

        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        val trip100List = ArrayList<KoreaTrip100>()
        val trip100Adapter = Trip100Adapter(applicationContext, trip100List)

        binding.rvKorea100.adapter = trip100Adapter

        fireStore.collection(FirebaseConstants.COLLECTION_KOREA_TRIP_100)
            .get().addOnSuccessListener {
                for (document in it) {
                    val model = document.toObject(KoreaTrip100::class.java)
                    trip100List.add(model)
                }
                trip100List.random()
                trip100Adapter.notifyDataSetChanged()
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