package com.amazing.stamp.pages.sns

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stamp.R
import com.amazing.stamp.adapter.PostImageAdapter
import com.amazing.stamp.adapter.ProfileNicknameAdapter
import com.amazing.stamp.models.ProfileNicknameModel
import com.amazing.stamp.pages.session.RegisterActivity
import com.amazing.stamp.utils.ParentActivity
import com.example.stamp.databinding.ActivityPostAddBinding
import com.amazing.stamp.utils.Utils
import java.util.*
import kotlin.collections.ArrayList

class PostAddActivity : ParentActivity() {
    companion object {
        const val FRIEND_SEARCH_REQUEST_CODE = 1001
        const val INTENT_EXTRA_PROFILE = "INTENT_EXTRA_PROFILE"
        const val INTENT_EXTRA_UID = "INTENT_EXTRA_UID"
        const val INTENT_EXTRA_NAME = "INTENT_EXTRA_NAME"
    }

    private val TAG = "PostAddActivity"
    private val binding by lazy { ActivityPostAddBinding.inflate(layoutInflater) }
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var imageUriList = ArrayList<Uri>()
    private val imageAdapter by lazy { PostImageAdapter(applicationContext, imageUriList) }
    private val MAX_IMAGE_COUNT = 10
    private val startDate = Calendar.getInstance()
    private val endDate = Calendar.getInstance()
    private val taggedFriends = ArrayList<ProfileNicknameModel>()
    private val taggedFriendAdapter by lazy {
        ProfileNicknameAdapter(
            applicationContext,
            taggedFriends
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarPostAdd)
        supportActionBar?.run {
            // 앱 바 뒤로가기 버튼 설정
            setDisplayHomeAsUpEnabled(true)
        }

        refreshImage()
        initGetImage()
        initDurationPicker()

        binding.run {
            rvPostPhoto.adapter = imageAdapter
            rvTaggedFriends.adapter = taggedFriendAdapter

            taggedFriendAdapter.onItemRemoveClickListener =
                object : ProfileNicknameAdapter.OnItemRemoveClickListener {
                    override fun onItemRemoved(model: ProfileNicknameModel, position: Int) {
                        taggedFriends.removeIf { it.uid == model.uid }
                        taggedFriendAdapter.notifyDataSetChanged()
                    }
                }

            imageAdapter.onImageRemoveClickListener =
                object : PostImageAdapter.OnImageRemoveClickListener {
                    override fun onRemove(position: Int) {
                        imageUriList.removeAt(position)
                        refreshImage()
                    }
                }

            refreshImage()

            btnPostPhotoAdd.setOnClickListener {
                // startActivityForResult 가 메모리 관련 문제로 Deprecated (사용 가능하긴 하지만 비권장) 됨
                // 개선판인 activityResultLauncher 와 registerForActivityResult 를 사용하는 것이 권장

                if (imageUriList.size >= MAX_IMAGE_COUNT) {
                    showShortToast(R.string.msg_max_image_count_exceed)
                    return@setOnClickListener
                }

                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                activityResultLauncher.launch(intent)
            }

            btnPostAddFriends.setOnClickListener {
                val intent = Intent(applicationContext, FriendsTagActivity::class.java)
                startActivityForResult(intent, FRIEND_SEARCH_REQUEST_CODE)
            }

            btnPostAddFinish.setOnClickListener {
                val startDate_post = etPostDurationStart
                val endDate_post = etPostDurationEnd
                val written_post = etPostWritePost.toString()
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            FRIEND_SEARCH_REQUEST_CODE -> {
                val uid = data?.getStringExtra(INTENT_EXTRA_UID)
                val nickname = data?.getStringExtra(INTENT_EXTRA_NAME)
                val profile = data?.getByteArrayExtra(INTENT_EXTRA_PROFILE)
                if (uid != null && nickname != null) {
                    tagFriend(profile, uid, nickname)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun tagFriend(profile: ByteArray?, uid: String, nickname: String) {
        // 존재하지 않을 경우 (idx == -1) 추가
        if (taggedFriends.indexOfFirst { it.uid == uid } == -1) {
            taggedFriends.add(ProfileNicknameModel(profile, uid, nickname))
            taggedFriendAdapter.notifyDataSetChanged()
        } else {
            showShortToast("이미 태그된 사용자입니다")
        }
    }

    private fun initDurationPicker() {
        binding.run {
            etPostDurationStart.setOnClickListener {
                val datePickerDialog = DatePickerDialog(
                    this@PostAddActivity,
                    DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
                        startDate[Calendar.YEAR] = year
                        startDate[Calendar.MONTH] = month - 1
                        startDate[Calendar.DAY_OF_MONTH] = day
                        etPostDurationStart.setText(Utils.sliderDateFormat.format(startDate.timeInMillis))
                    },
                    startDate[Calendar.YEAR],
                    startDate[Calendar.MONTH] + 1,
                    startDate[Calendar.DAY_OF_MONTH]
                )

                datePickerDialog.show()
            }

            etPostDurationEnd.setOnClickListener {
                val datePickerDialog = DatePickerDialog(
                    this@PostAddActivity,
                    DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
                        endDate[Calendar.YEAR] = year
                        endDate[Calendar.MONTH] = month - 1
                        endDate[Calendar.DAY_OF_MONTH] = day
                        etPostDurationEnd.setText(Utils.sliderDateFormat.format(endDate.timeInMillis))
                    },
                    endDate[Calendar.YEAR],
                    endDate[Calendar.MONTH] + 1,
                    endDate[Calendar.DAY_OF_MONTH]
                )
                datePickerDialog.show()
            }
        }
    }

    private fun refreshImage() {
        imageAdapter.notifyDataSetChanged()

        binding.rvPostPhoto.visibility = if (imageUriList.size == 0) View.GONE else View.VISIBLE

        binding.run {
            btnPostPhotoAdd.text =
                "${getString(R.string.obj_post_add_photo)} (${imageUriList.size}/${MAX_IMAGE_COUNT})"
        }
    }

    private fun initGetImage() {
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK && it.data != null) {
                    val currentImageUri = it.data?.data
                    try {
                        imageUriList.add(currentImageUri!!)
                        refreshImage()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else if (it.resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "사진 선택 취소", Toast.LENGTH_LONG).show();
                } else {
                    Log.d("ActivityResult", "something wrong")
                }
            }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 앱 바 클릭 이벤트
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}