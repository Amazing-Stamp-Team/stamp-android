package com.amazing.stamp.pages

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.amazing.stamp.pages.home.HomeFragment
import com.amazing.stamp.pages.map.MapFragment
import com.example.stamp.R
import com.example.stamp.databinding.ActivityMainBinding
import com.amazing.stamp.pages.sns.FeedFragment


class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        binding.run {
            //val cheonan = ivKorea.findRichPathByName("충남 천안")!!
            //cheonan.fillColor = getColor(R.color.teal_700)
            supportFragmentManager.beginTransaction().add(R.id.container_main, HomeFragment()).commit()

            navMain.setOnItemSelectedListener {
                when(it.itemId) {
                    R.id.nav_home -> {
                        supportFragmentManager.beginTransaction().replace(R.id.container_main, HomeFragment()).commit()
                        return@setOnItemSelectedListener true
                    }

                    R.id.nav_map -> {
                        supportFragmentManager.beginTransaction().replace(R.id.container_main, MapFragment()).commit()
                        return@setOnItemSelectedListener true
                    }

                    R.id.nav_feed -> {
                        supportFragmentManager.beginTransaction().replace(R.id.container_main, FeedFragment()).commit()
                        return@setOnItemSelectedListener true
                    }

                    R.id.nav_my_page -> {
                        supportFragmentManager.beginTransaction().replace(R.id.container_main, MyPageFragment()).commit()
                        return@setOnItemSelectedListener true
                    }
                }
                return@setOnItemSelectedListener false
            }

        }
    }
}