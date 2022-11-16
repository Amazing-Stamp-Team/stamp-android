package com.amazing.stamp.pages.sns

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.amazing.stamp.models.PostModel
import com.amazing.stamp.utils.Constants
import com.amazing.stamp.utils.FirebaseConstants
import com.amazing.stamp.utils.Utils
import com.example.stamp.R
import com.example.stamp.databinding.ActivityPostAddBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PostEditActivity : PostAddActivity() {
    private lateinit var postId: String
    private lateinit var postModel: PostModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        postId = intent.getStringExtra(Constants.INTENT_EXTRA_POST_ID)!!

        binding.toolbarPostAdd.title = "게시물 수정"

        onDefaultSetting()

        CoroutineScope(Dispatchers.Main).launch {
            getPostData()
            setPostData()
        }
    }

    private suspend fun getPostData() {
        // 게시물 데이터 가져오기
        postModel = fireStore.collection(FirebaseConstants.COLLECTION_POSTS)
            .document(postId).get().await().toObject(PostModel::class.java)!!

    }

    private fun setPostData() {
        // 게시물 데이터 세팅
        binding.etPostWritePost.setText(postModel.content)
        binding.tvPostLocation.text = postModel.location
        binding.tvPostLocation.visibility = View.VISIBLE

        startDate.timeInMillis = if(postModel.startDate == null) - 1 else postModel.startDate!!.seconds * 1000
        endDate.timeInMillis = if(postModel.endDate == null) - 1 else postModel.endDate!!.seconds * 1000

        binding.etPostDurationStart.setText(if(startDate.timeInMillis == -1L) "" else Utils.sliderDateFormat.format(startDate.time))
        binding.etPostDurationEnd.setText(if(endDate.timeInMillis == -1L) "" else Utils.sliderDateFormat.format(endDate.time))

    }
}