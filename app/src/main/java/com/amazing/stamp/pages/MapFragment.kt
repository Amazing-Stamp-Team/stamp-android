package com.amazing.stamp.pages

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.devs.vectorchildfinder.VectorChildFinder
import com.example.stamp.R
import com.example.stamp.databinding.FragmentFeedBinding
import com.example.stamp.databinding.FragmentMapBinding
import com.richpath.RichPathView


class MapFragment : Fragment() {
    private lateinit var binding: FragmentMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMapBinding.inflate(inflater, container, false)


        val vector =
            VectorChildFinder(requireContext(), R.drawable.ic_img_korea_detail, binding.ivKorea)
        val seoul = vector.findPathByName("충남 천안")
        seoul.fillColor = Color.RED

        binding.ivKorea.invalidate()


//        binding.run {
//            val cheonan = ivKorea.findRichPathByName("충남 천안")!!
//            cheonan.fillColor = requireActivity().getColor(R.color.teal_700)
//
//            val yesan = ivKorea.findRichPathByName("충남_예산군")!!
//            yesan.fillColor = requireActivity().getColor(R.color.purple_700)
//        }

        return binding.root
//        return inflater.inflate(R.layout.fragment_map, container, false)
    }

}