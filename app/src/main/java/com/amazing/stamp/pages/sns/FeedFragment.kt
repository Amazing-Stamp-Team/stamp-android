package com.amazing.stamp.pages.sns

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.amazing.stamp.utils.Utils.showShortToast
import com.example.stamp.R
import com.example.stamp.databinding.FragmentFeedBinding


class FeedFragment : Fragment() {
    private val binding by lazy { FragmentFeedBinding.inflate(layoutInflater) }
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
                        showShortToast(requireActivity(), "Friends Add Click")
                        return@setOnMenuItemClickListener true
                    }
                    R.id.toolbar_action_dm -> {
                        showShortToast(requireActivity(), "DM Click")
                        return@setOnMenuItemClickListener true
                    }
                }
                return@setOnMenuItemClickListener false
            }
        }


        return binding.root
    }
}