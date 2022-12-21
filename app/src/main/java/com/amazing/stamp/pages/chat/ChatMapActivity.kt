package com.amazing.stamp.pages.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.stamp.databinding.ActivityChatMapBinding

class ChatMapActivity : AppCompatActivity() {
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1007
    }

    private val binding by lazy { ActivityChatMapBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)




    }
}