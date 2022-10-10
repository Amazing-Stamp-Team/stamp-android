package com.amazing.stamp.pages

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amazing.stamp.adapter.MyPageTripAdapter
import com.amazing.stamp.adapter.decoration.VerticalGapDecoration
import com.amazing.stamp.models.MyPageTripModel
import com.example.stamp.databinding.FragmentMyPageBinding
import com.google.firebase.auth.FirebaseAuth


class MyPageFragment : Fragment() {
    val TAG = "MyPageFragment"
    private val binding by lazy { FragmentMyPageBinding.inflate(layoutInflater) }
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mAuth = FirebaseAuth.getInstance()

        binding.tvProfile.text = mAuth!!.currentUser!!.displayName

        setUpTripSampleRecyclerView()

        return binding.root
    }

    private fun setUpTripSampleRecyclerView() {
        val myPageTripModels = ArrayList<MyPageTripModel>()
        myPageTripModels.add(MyPageTripModel("", "서울, 남산타워", "2022년 01월 01일"))
        myPageTripModels.add(MyPageTripModel("", "부산, 마린시티", "2022년 10월 01일"))
        myPageTripModels.add(MyPageTripModel("", "부산, 마린시티", "2022년 10월 01일"))
        myPageTripModels.add(MyPageTripModel("", "부산, 마린시티", "2022년 10월 01일"))

        val tripSampleAdapter = MyPageTripAdapter(requireContext(), myPageTripModels)
        binding.rvMyPageTrip.addItemDecoration(VerticalGapDecoration(30))
        binding.rvMyPageTrip.adapter = tripSampleAdapter
        tripSampleAdapter.notifyDataSetChanged()
    }
}