package com.amazing.stamp.pages.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.amazing.stamp.adapter.FestivalPreviewAdapter
import com.example.stamp.R
import com.amazing.stamp.adapter.ImageSliderAdapter
import com.amazing.stamp.adapter.Trip100Adapter
import com.amazing.stamp.api.KorTripInfoAPI
import com.amazing.stamp.api.dto.festivalDTO.FestivalDTO
import com.amazing.stamp.api.dto.festivalDTO.Item
import com.amazing.stamp.models.FestivalModel
import com.amazing.stamp.models.KoreaTrip100
import com.amazing.stamp.models.StamfPickModel
import com.amazing.stamp.pages.map.LocationBasedViewActivity
import com.amazing.stamp.utils.FirebaseConstants
import com.amazing.stamp.utils.SecretConstants
import com.example.stamp.databinding.FragmentHomeBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.util.*
import kotlin.collections.ArrayList


class HomeFragment : Fragment() {
    private val binding by lazy { FragmentHomeBinding.inflate(layoutInflater) }
    private val fireStore by lazy { Firebase.firestore }
    private val storage by lazy { Firebase.storage }
    private val korTripInfoAPI by lazy { KorTripInfoAPI.create() }
    private val stamfPickModels by lazy { ArrayList<StamfPickModel>() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val images = Array(5) { requireActivity().getDrawable(R.drawable.img_sample_3)!! }
        images[1] = requireActivity().getDrawable(R.drawable.img_sample_2)!!
        val descriptions = Array(5) { " " }
        descriptions[0] = "전주, 한옥마을"
        descriptions[1] = "Page2"

        binding.run {
            tvHomePopularAttractions.setOnClickListener {
                val intent = Intent(requireActivity(), HomeFestivalsActivity::class.java)
                startActivity(intent)
            }

            btnKoreaTrip100AllView.setOnClickListener {
                startActivity(
                    Intent(
                        requireContext(),
                        KoreaTrip100Activity::class.java
                    )
                )
            }

            btnLocationBased.setOnClickListener {
                startActivity(
                    Intent(
                        requireContext(),
                        LocationBasedViewActivity::class.java
                    )
                )
            }
        }

        setUpStamfPick()
        setUpTrip100RecyclerView()
        setUpFestivalRecyclerView()

        return binding.root
    }

    private fun setUpStamfPick() = with(binding) {
        vpHome.offscreenPageLimit = 1

        fireStore.collection(FirebaseConstants.COLLECTION_STAMF_PICK)
            .get().addOnSuccessListener { querySnapshot ->
                querySnapshot.documents.forEach {
                    stamfPickModels.add(it.toObject<StamfPickModel>()!!)
                }

                vpHome.adapter = ImageSliderAdapter(requireContext(), stamfPickModels)
                vpHome.adapter!!.notifyDataSetChanged()
                setupIndicators(stamfPickModels.size)
            }

        vpHome.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
            }
        })
    }


    private fun setUpFestivalRecyclerView() {
        //     @Query("serviceKey", encoded = true) serviceKey: String,
        //        @Query("eventStartDate") eventStartDate: String,
        //        @Query("_type") _type: String,
        //        @Query("MobileOS") MobileOS: String,
        //        @Query("MobileApp") MobileApp: String,
        //        @Query("arrange") arrange: String,
        //        @Query("numOfRows") numOfRows: Int

        val getFestivalCall = korTripInfoAPI.getFestivalInfoCall(
            SecretConstants.KOR_TRIP_INFO_SERVICE_KEY,
            KorTripInfoAPI.tripDateFormat.format(System.currentTimeMillis()),
            "json",
            "AND",
            "STAMF",
            "B",
            6
        )

        getFestivalCall.enqueue(object : Callback<FestivalDTO> {
            override fun onResponse(call: Call<FestivalDTO>, response: Response<FestivalDTO>) {
                val festivalModel = ArrayList<Item>()

                response.body()?.response?.body?.items?.item?.forEach {
                    festivalModel.add(it)
                }

                val festivalPreviewAdapter = FestivalPreviewAdapter(
                    requireContext(),
                    festivalModel
                )
                binding.rvFestivalPreview.adapter = festivalPreviewAdapter
                festivalPreviewAdapter.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<FestivalDTO>, t: Throwable) {

            }
        })
    }

    private fun setUpTrip100RecyclerView() = with(binding) {
        var trip100List = ArrayList<KoreaTrip100>()

        val trip100Adapter = Trip100Adapter(requireContext(), trip100List)

        rvTrip100.adapter = trip100Adapter

        var idx = 0

        fireStore.collection(FirebaseConstants.COLLECTION_KOREA_TRIP_100)
            .get().addOnSuccessListener {
                for (document in it) {
                    idx++
                    val model = document.toObject(KoreaTrip100::class.java)
                    trip100List.add(model)
                    if (idx > 2) break
                }
                trip100Adapter.notifyDataSetChanged()
            }
    }


    private fun setupIndicators(count: Int) {
        val indicators: Array<ImageView?> = arrayOfNulls<ImageView>(count)
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(16, 8, 16, 8)
        for (i in indicators.indices) {
            indicators[i] = ImageView(requireContext())
            indicators[i]!!.setImageDrawable(
                ContextCompat.getDrawable(
                    requireActivity(),
                    R.drawable.bg_indicator_inactive
                )
            )
            indicators[i]!!.setLayoutParams(params)
            binding.layoutIndicators.addView(indicators[i])
        }
        setCurrentIndicator(0)
    }

    private fun setCurrentIndicator(position: Int) {
        val childCount: Int = binding.layoutIndicators.getChildCount()
        for (i in 0 until childCount) {
            val imageView: ImageView = binding.layoutIndicators.getChildAt(i) as ImageView
            if (i == position) {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.bg_indicator_active
                    )
                )
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.bg_indicator_inactive
                    )
                )
            }
        }
    }
}