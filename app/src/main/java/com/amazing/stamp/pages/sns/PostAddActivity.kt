package com.amazing.stamp.pages.sns

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.amazing.stamp.adapter.PostImageAdapter
import com.amazing.stamp.adapter.ProfileNicknameAdapter
import com.amazing.stamp.models.PostLikeModel
import com.amazing.stamp.models.PostModel
import com.amazing.stamp.models.ProfileNicknameModel
import com.amazing.stamp.utils.FirebaseConstants
import com.amazing.stamp.utils.ParentActivity
import com.amazing.stamp.utils.Utils
import com.example.stamp.R
import com.example.stamp.databinding.ActivityPostAddBinding
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileInputStream
import java.util.*
import kotlin.collections.ArrayList

open class PostAddActivity : ParentActivity() {

    companion object {
        const val FRIEND_SEARCH_REQUEST_CODE = 1001
        const val PHOTO_ADD_REQUEST_CODE = 1002

        // 액티비티간 값 전달을 위한 Intent Extra
        const val INTENT_EXTRA_PROFILE = "INTENT_EXTRA_PROFILE"
        const val INTENT_EXTRA_UID = "INTENT_EXTRA_UID"
        const val INTENT_EXTRA_NAME = "INTENT_EXTRA_NAME"
    }

    protected val binding by lazy { ActivityPostAddBinding.inflate(layoutInflater) }
    protected val TAG = "PostAddActivity"

    // 이미지 관련
    protected var imageUriList = ArrayList<Uri>()
    protected val pathUri = ArrayList<String?>()

    protected val imageAdapter by lazy { PostImageAdapter(applicationContext, imageUriList) }
    private val MAX_IMAGE_COUNT = 10
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent> // 이미지 선택 후 돌아올때 사용

    // 날짜 관련
    protected val startDate = Calendar.getInstance()
    protected val endDate = Calendar.getInstance()

    // 친구 태그 관련
    protected val taggedFriends = ArrayList<ProfileNicknameModel>()

    // 액티비티가 다 로딩되지 않았을 때 applicationContext 를 넘겨주려 하면 에러를 일으키기 때문에,
    // lazy (늦은 초기화, 해당 변수가 처음 언급, 실행될때 초기화됨)로 applicationContext 를 넘겨줌
    protected val taggedFriendAdapter by lazy {
        ProfileNicknameAdapter(
            applicationContext,
            taggedFriends
        )
    }


    // 위치 관련
    protected var location: String? = null


    //파이어베이스 관련
    protected val auth by lazy { Firebase.auth }
    protected val storage by lazy { Firebase.storage }
    protected val fireStore by lazy { Firebase.firestore }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        // 등록하기 버튼
        binding.btnPostAddFinish.setOnClickListener { onPostAdd() }

        // 디폴트 세팅
        onDefaultSetting()
    }

    // PostEditActivity 에서 onCreate() 를 오버라이딩 하기 위함
    protected fun onDefaultSetting() {
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
                        pathUri.removeAt(position)
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

                val intent = Intent(Intent.ACTION_PICK)
                intent.type = MediaStore.Images.Media.CONTENT_TYPE
                startActivityForResult(intent, PHOTO_ADD_REQUEST_CODE)
            }

            // 친구 언급 버튼
            btnPostAddFriends.setOnClickListener {
                val intent = Intent(applicationContext, FriendsTagActivity::class.java)
                startActivityForResult(intent, FRIEND_SEARCH_REQUEST_CODE)
            }



            tvPostLocationSet.setOnClickListener { currentLocationSet() }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_CANCELED) return

        when (requestCode) {
            // 친구 태그 RequestCode 일때
            FRIEND_SEARCH_REQUEST_CODE -> {
                val uid = data?.getStringExtra(INTENT_EXTRA_UID)
                val nickname = data?.getStringExtra(INTENT_EXTRA_NAME)
                if (uid != null && nickname != null) {
                    tagFriend(uid, nickname)
                }
            }

            PHOTO_ADD_REQUEST_CODE -> {
                val currentImageUri = data?.data!!
                imageUriList.add(currentImageUri)
                val currentPathUri = Utils.getPath(applicationContext, currentImageUri)
                pathUri.add(currentPathUri)
                refreshImage()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationAvailability(p0: LocationAvailability) {
            super.onLocationAvailability(p0)
        }

        override fun onLocationResult(p0: LocationResult) {
            val lastLocation = p0.lastLocation
            val lat = lastLocation!!.latitude
            val long = lastLocation.longitude

            // 위도와 경도를 이용하여 주소로 변환

            val geocoder = Geocoder(this@PostAddActivity)
            val address = geocoder.getFromLocation(lat, long, 10)

            if (address.size == 0) showShortToast("주소 찾기 오류")
            else {
                // 반환 예시) 대한민국 충청남도 천안시 서북구 두정역동0길 00
                location = address[0].getAddressLine(0).replaceFirst("대한민국 ", "")
                binding.tvPostLocation.text = location
                binding.tvPostLocation.visibility = View.VISIBLE
            }
        }
    }

    private fun currentLocationSet() {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
            priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun tagFriend(uid: String, nickname: String) {
        // 태그된 사용자 리스트에 존재하지 않을 경우 (idx == -1) 사용자를 추가
        // 태그된 사용자 리스트에 있으면 이미 태그된 사용자입니다 메시지 출력
        if (taggedFriends.indexOfFirst { it.uid == uid } == -1) {
            taggedFriends.add(ProfileNicknameModel(uid, nickname))
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
                        startDate[Calendar.MONTH] = month
                        startDate[Calendar.DAY_OF_MONTH] = day
                        etPostDurationStart.setText(Utils.sliderDateFormat.format(startDate.timeInMillis))
                    },
                    startDate[Calendar.YEAR],
                    startDate[Calendar.MONTH],
                    startDate[Calendar.DAY_OF_MONTH]
                )

                datePickerDialog.show()
            }

            etPostDurationEnd.setOnClickListener {
                val datePickerDialog = DatePickerDialog(
                    this@PostAddActivity,
                    DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
                        endDate[Calendar.YEAR] = year
                        endDate[Calendar.MONTH] = month
                        endDate[Calendar.DAY_OF_MONTH] = day
                        etPostDurationEnd.setText(Utils.sliderDateFormat.format(endDate.timeInMillis))
                    },
                    endDate[Calendar.YEAR],
                    endDate[Calendar.MONTH],
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
                        val currentPathUri = getPath(currentImageUri)
                        Log.d(TAG, "initGetImage: $currentImageUri")
                        Log.d(TAG, "initGetImage: $currentPathUri")
                        pathUri.add(currentPathUri)
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

    private fun onPostAdd() {
        //    val writer: String,
        //    val friends: ArrayList<String>,
        //    val content: String?,
        //    val startDate: LocalDate?,
        //    val endDate: LocalDate?,
        //    val createdAt: LocalDateTime

        val friendsUID = ArrayList<String>()
        taggedFriends.forEach { friendsUID.add(it.uid) }

        // 구글 파이어베이스의 Timestamp 타입 사용
        val createdAt = Timestamp.now()
        val startTimeStamp = if (binding.etPostDurationStart.text.isEmpty()) null else Timestamp(Date(startDate.timeInMillis))
        val endTimeStamp = if (binding.etPostDurationEnd.text.isEmpty()) null else Timestamp(Date(endDate.timeInMillis))



        showProgress(this, "게시글 등록 중...")

        // 코틀린 코루틴의 Dispatchers 에는 여러 종류가 있음
        // IO - 네트워크 작업 최적화
        // Main - UI와 상호작용
        // Default - CPU를 많이 사용하는 작업
        CoroutineScope(Dispatchers.Main).launch {
            val postModel = PostModel(
                auth.uid!!,
                friendsUID,
                binding.etPostWritePost.text.toString(),
                location,
                startTimeStamp,
                endTimeStamp,
                createdAt,
                null
            )

            hideProgress()
            showProgress(this@PostAddActivity, "사진 업로드 중...")

            val postModelUploadResult =
                fireStore.collection(FirebaseConstants.COLLECTION_POSTS).add(postModel).await()

            // PostLike 문서 만듬
            fireStore.collection(FirebaseConstants.COLLECTION_POST_LIKES)
                .document(postModelUploadResult.id)
                .set(PostLikeModel(ArrayList()))


            imageUpload(postModelUploadResult.id)
        }
    }

    private suspend fun imageUpload(id: String) {
        val imageName = ArrayList<String>()

        for (i in 0 until imageUriList.size) {
            imageName.add("IMG_POST_${id}_${i}")

            Log.d(TAG, "imageUpload: $i   ${imageUriList[i]}  ${pathUri[i]}")

            if (pathUri[i] == null) continue
            val photoFileRef = storage.reference.child(FirebaseConstants.STORAGE_POST).child(id)
                .child(imageName[i])
            val uploadTask = photoFileRef.putStream(FileInputStream(File(pathUri[i])))
            val uploadResult = uploadTask.await()
        }

        fireStore.collection(FirebaseConstants.COLLECTION_POSTS)
            .document(id)
            .update(FirebaseConstants.POSTS_FIELD_IMAGE_NAME, imageName)

        hideProgress()
        finish()
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


    fun getPath(uri: Uri?): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor = managedQuery(uri, projection, null, null, null)
        startManagingCursor(cursor)
        val columnIndex: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(columnIndex)
    }
}