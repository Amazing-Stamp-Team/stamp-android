package com.amazing.stamp.pages.sns

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.location.LocationListener
import android.location.LocationManager
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

        // 액티비티간 값 전달을 위한 Intent Extra
        const val INTENT_EXTRA_PROFILE = "INTENT_EXTRA_PROFILE"
        const val INTENT_EXTRA_UID = "INTENT_EXTRA_UID"
        const val INTENT_EXTRA_NAME = "INTENT_EXTRA_NAME"
    }

    private val binding by lazy { ActivityPostAddBinding.inflate(layoutInflater) }
    private val TAG = "PostAddActivity"

    // 이미지 관련
    private var imageUriList = ArrayList<Uri>()
    private val imageAdapter by lazy { PostImageAdapter(applicationContext, imageUriList) }
    private val MAX_IMAGE_COUNT = 10
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent> // 이미지 선택 후 돌아올때 사용

    // 날짜 관련
    private val startDate = Calendar.getInstance()
    private val endDate = Calendar.getInstance()

    // 친구 태그 관련
    private val taggedFriends = ArrayList<ProfileNicknameModel>()

    // 액티비티가 다 로딩되지 않았을 때 applicationContext 를 넘겨주려 하면 에러를 일으키기 때문에,
    // lazy (늦은 초기화, 해당 변수가 처음 언급, 실행될때 초기화됨)로 applicationContext 를 넘겨줌
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

            // 친구 태그 리사이클러뷰
            // x 버튼 클릭하여 언급된 친구 삭제
            taggedFriendAdapter.onItemRemoveClickListener =
                object : ProfileNicknameAdapter.OnItemRemoveClickListener {
                    override fun onItemRemoved(model: ProfileNicknameModel, position: Int) {
                        taggedFriends.removeIf { it.uid == model.uid }
                        taggedFriendAdapter.notifyDataSetChanged()
                    }
                }

            // 이미지 첨부 리사이클러뷰
            // x 버튼 클릭하여 첨부된 사진 삭제
            imageAdapter.onImageRemoveClickListener =
                object : PostImageAdapter.OnImageRemoveClickListener {
                    override fun onRemove(position: Int) {
                        imageUriList.removeAt(position)
                        refreshImage()
                    }
                }

            refreshImage()

            // 이미지 첨부하기 버튼
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

            // 친구 언급 버튼
            btnPostAddFriends.setOnClickListener {
                val intent = Intent(applicationContext, FriendsTagActivity::class.java)
                startActivityForResult(intent, FRIEND_SEARCH_REQUEST_CODE)
            }


            // 등록하기 버튼
            btnPostAddFinish.setOnClickListener {
                val startDate_post = etPostDurationStart
                val endDate_post = etPostDurationEnd
                val written_post = etPostWritePost.toString()
                finish()
            }


            tvPostLocation.setOnClickListener { currentLocationSet() }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            // 친구 태그 RequestCode 일때
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

    private fun currentLocationSet() {

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)



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