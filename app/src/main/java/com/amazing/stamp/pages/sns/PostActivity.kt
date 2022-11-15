package com.amazing.stamp.pages.sns

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.amazing.stamp.utils.Constants
import com.example.stamp.databinding.ActivityPostBinding

class PostActivity : AppCompatActivity() {
    private val binding by lazy { ActivityPostBinding.inflate(layoutInflater) }
    private var postId:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        postId = intent.getStringExtra(Constants.INTENT_EXTRA_POST_ID)

        binding.includedFeed.tvItemFeedNickname.text = "testasdafsdsd"
    }
}