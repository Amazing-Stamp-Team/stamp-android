package com.amazing.stamp.pages.sns

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.amazing.stamp.adapter.FeedAdapter
import com.amazing.stamp.models.FeedModel
import com.amazing.stamp.utils.Utils.showShortToast
import com.example.stamp.R
import com.example.stamp.databinding.FragmentFeedBinding


class FeedFragment : Fragment() {
    private val binding by lazy { FragmentFeedBinding.inflate(layoutInflater) }
    private val feedModes = ArrayList<FeedModel>()
    private val feedAdapter by lazy { FeedAdapter(requireActivity(), feedModes) }

    private val TAG = "TAG_FEED"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        binding.run {
            btnPostAdd.setOnClickListener {
                val intent = Intent(requireActivity(), PostAddActivity::class.java)
                startActivity(intent)
            }

            toolbarFeed.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.toolbar_action_friends -> {
                        val intent = Intent(requireActivity(), FriendsSearchActivity::class.java)
                        startActivity(intent)
                        requireActivity().overridePendingTransition(
                            R.anim.horizon_enter,
                            R.anim.none
                        ) // 옆에서 들어오는 애니메이션

                        return@setOnMenuItemClickListener true
                    }
                    R.id.toolbar_action_dm -> {
                        showShortToast(requireActivity(), "DM Click")
                        return@setOnMenuItemClickListener true
                    }
                }
                return@setOnMenuItemClickListener false
            }

            setUpFeedRecyclerView()
        }


        return binding.root
    }


    private fun setUpFeedRecyclerView() {
        binding.recyclerFeed.adapter = feedAdapter

        //    val writer: String,
        //    val imageName: ArrayList<String>,
        //    val content: String,
        //    val startTime: String,
        //    val endTime: String,
        //    val friends: ArrayList<String>,
        //    val createdAt: String,
        //    val place: String // GPS 기능 구현후에

        feedModes.add(FeedModel("너굴맨", ArrayList(), "광안리 여행을 다녀왔습니다", "", "", ArrayList(), "", "부산 광안리"))
        feedModes.add(FeedModel("너굴맨", ArrayList(), "광안리 여행을 다녀왔습니다", "", "", ArrayList(), "", "부산 광안리"))
        feedModes.add(FeedModel("너굴맨", ArrayList(), "광안리 여행을 다녀왔습니다", "", "", ArrayList(), "", "부산 광안리"))

        feedAdapter.notifyDataSetChanged()
    }
}