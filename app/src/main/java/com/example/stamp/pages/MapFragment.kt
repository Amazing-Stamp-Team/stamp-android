package com.example.stamp.pages

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.stamp.R
import com.example.stamp.databinding.FragmentMapBinding


class MapFragment : Fragment() {

    private val binding by lazy { FragmentMapBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding.run {
            val cheonan = ivKorea.findRichPathByName("충남 천안")!!
            cheonan.fillColor = requireActivity().getColor(R.color.teal_700)
        }


        return binding.root
    }

}