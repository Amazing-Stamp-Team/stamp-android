package com.amazing.stamp.pages.map

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.amazing.stamp.adapter.MapSearchAdapter
import com.amazing.stamp.api.NaverAPI
import com.amazing.stamp.api.dto.Item
import com.amazing.stamp.api.dto.NaverMapSearchResponseDTO
import com.amazing.stamp.utils.Constants
import com.amazing.stamp.utils.Constants.EXTRA_MAP_SEARCH_REQUEST_CODE
import com.example.stamp.databinding.ActivityMapSearchBinding

class MapSearchActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMapSearchBinding.inflate(layoutInflater) }
    private val naverAPI by lazy { NaverAPI.create() }
    private val mapSearchModels = ArrayList<Item>()
    private val mapSearchAdapter by lazy { MapSearchAdapter(applicationContext, mapSearchModels) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.run {
            rvMapSearch.adapter = mapSearchAdapter

            etSearch.addTextChangedListener {
                if (it.toString().isNotEmpty()) {
                    val searchCall = naverAPI.getMapSearch(it.toString(), 5)
                    searchCall.enqueue(object : retrofit2.Callback<NaverMapSearchResponseDTO> {
                        override fun onResponse(
                            call: retrofit2.Call<NaverMapSearchResponseDTO>,
                            response: retrofit2.Response<NaverMapSearchResponseDTO>
                        ) {
                            if (response.isSuccessful) {
                                val body = response.body()!!

                                for(i in 0 until body.items.size) {
                                    body.items[i].title = body.items[i].title.replace("<b>", "").replace("</b>", "")
                                }

                                mapSearchModels.clear()
                                mapSearchModels.addAll(body.items)
                                mapSearchAdapter.notifyDataSetChanged()
                            }
                        }

                        override fun onFailure(
                            call: retrofit2.Call<NaverMapSearchResponseDTO>,
                            t: Throwable
                        ) {
                            t.printStackTrace()
                        }
                    })
                }
            }

            mapSearchAdapter.onItemClickListener = object : MapSearchAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    val intent = Intent().apply {
                        putExtra(Constants.INTENT_EXTRA_MAP_TITLE, mapSearchModels[position].title)
                        putExtra(Constants.INTENT_EXTRA_ADDRESS, mapSearchModels[position].address)
                        putExtra(
                            Constants.INTENT_EXTRA_MAP_CATEGORY,
                            mapSearchModels[position].category
                        )
                    }
                    setResult(EXTRA_MAP_SEARCH_REQUEST_CODE, intent)
                    finish()
                }
            }
        }
    }
}