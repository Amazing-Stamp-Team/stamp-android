package com.amazing.stamp.pages.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.stamp.databinding.ActivityChatAddBinding

class ChatAddActivity : AppCompatActivity() {
    private val binding by lazy { ActivityChatAddBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        

    }
}