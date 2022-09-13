package com.example.stamp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.stamp.databinding.ActivityMainBinding
import com.richpath.RichPath


class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        binding.run {



            val cheonan = ivKorea.findRichPathByName("충남 천안")!!
            cheonan.fillColor = getColor(R.color.teal_700)

        }
    }
}