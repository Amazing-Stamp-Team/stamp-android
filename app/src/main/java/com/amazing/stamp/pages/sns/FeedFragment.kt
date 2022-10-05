package com.amazing.stamp.pages.sns

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.stamp.R
import com.example.stamp.databinding.FragmentFeedBinding


class FeedFragment : Fragment() {
    private val binding by lazy { FragmentFeedBinding.inflate(layoutInflater) }

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
        }


        return binding.root
    }
}