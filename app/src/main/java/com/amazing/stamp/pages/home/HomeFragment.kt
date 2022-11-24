package com.amazing.stamp.pages.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.stamp.R
import com.amazing.stamp.adapter.ImageSliderAdapter
import com.amazing.stamp.adapter.Trip100Adapter
import com.amazing.stamp.models.KoreaTrip100
import com.amazing.stamp.pages.map.LocationBasedViewActivity
import com.amazing.stamp.utils.FirebaseConstants
import com.example.stamp.databinding.FragmentHomeBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class HomeFragment : Fragment() {
    private val binding by lazy { FragmentHomeBinding.inflate(layoutInflater) }
    private val fireStore by lazy { Firebase.firestore }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val images = Array(5) { requireActivity().getDrawable(R.drawable.img_sample_3)!! }
        val descriptions = Array(5) { " " }
        descriptions[0] = "전주, 한옥마을"
        descriptions[1] = "Page2"

        binding.run {
            tvHomePopularAttractions.setOnClickListener {
                val intent = Intent(requireActivity(), HomeFestivalsActivity::class.java)
                startActivity(intent)
            }

            vpHome.offscreenPageLimit = 1
            vpHome.adapter = ImageSliderAdapter(requireContext(), images, descriptions)

            vpHome.registerOnPageChangeCallback(object : OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    setCurrentIndicator(position)
                }
            })

            setupIndicators(images.size)


            btnKoreaTrip100AllView.setOnClickListener { startActivity(Intent(requireContext(), KoreaTrip100Activity::class.java)) }

            btnLocationBased.setOnClickListener { startActivity(Intent(requireContext(), LocationBasedViewActivity::class.java)) }
        }

        setUpTrip100RecyclerView()


        return binding.root
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
                    if(idx > 2) break
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