package com.amazing.stamp.pages.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.amazing.stamp.adapter.ChatAdapter
import com.amazing.stamp.models.ChatModel
import com.amazing.stamp.models.ChatRoomModel
import com.amazing.stamp.models.UserModel
import com.amazing.stamp.utils.Constants
import com.amazing.stamp.utils.FirebaseConstants
import com.example.stamp.databinding.ActivityChatBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatActivity : AppCompatActivity() {
    private val binding by lazy { ActivityChatBinding.inflate(layoutInflater) }
    private val fireStore by lazy { Firebase.firestore }
    private val auth by lazy { Firebase.auth }
    private lateinit var chatRoomModel: ChatRoomModel
    private val chatModels = ArrayList<ChatModel>()
    private val userModels = ArrayList<UserModel>()
    private val chatAdapter by lazy { ChatAdapter(applicationContext, chatModels, userModels) }
    private var chatRoomID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarChat)
        supportActionBar?.run {
            // 앱 바 뒤로가기 버튼 설정
            setDisplayHomeAsUpEnabled(true)
        }

        chatRoomID = intent.getStringExtra(Constants.INTENT_EXTRA_CHAT_ID)

        binding.run {
            CoroutineScope(Dispatchers.Main).launch {
                setUpRecyclerView()
                setUpSend()
                getUserNicknames()
            }
        }
    }

    private fun getUserNicknames() {
        chatRoomModel.users.forEach { userUID ->
            fireStore.collection(FirebaseConstants.COLLECTION_USERS).document(userUID).get()
                .addOnSuccessListener { documentSnapshot ->
                    val userModel = documentSnapshot.toObject<UserModel>()
                    if (userModel != null) {
                        userModels.add(userModel)
                        chatAdapter.notifyDataSetChanged()
                    }
                }
        }
    }

    private fun setUpSend() {
        binding.btnSendMessage.setOnClickListener {
            val content = binding.etMessage.text.toString()
            if (content.isNotEmpty()) {
                // val user: String, val content: String, val timestamp: Timestamp
                val chatModel = ChatModel(auth.currentUser!!.uid, content, Timestamp.now())

                fireStore.collection(FirebaseConstants.COLLECTION_CHAT)
                    .document(chatRoomID.toString())
                    .collection(FirebaseConstants.COLLECTION_MESSAGE_LOG).add(chatModel)
                binding.etMessage.setText("")
            }
        }
    }

    private suspend fun setUpRecyclerView() {
        binding.run {
            chatRoomModel = fireStore.collection(FirebaseConstants.COLLECTION_CHAT)
                .document(chatRoomID!!).get().await().toObject<ChatRoomModel>()!!

            toolbarChat.title = chatRoomModel.title

            fireStore.collection(FirebaseConstants.COLLECTION_CHAT)
                .document(chatRoomID!!).collection(FirebaseConstants.COLLECTION_MESSAGE_LOG)
                .orderBy(FirebaseConstants.MESSAGE_LOG_FIELD_TIMESTAMP, Query.Direction.ASCENDING)
                .addSnapshotListener { value, error ->
                    value?.documentChanges?.forEach {
                        if (it.type == DocumentChange.Type.ADDED) {
                            val chatModel = it.document.toObject<ChatModel>()
                            chatModels.add(chatModel)
                            rvChat.smoothScrollToPosition(chatModels.size - 1)
                        }
                    }
                    chatAdapter.notifyDataSetChanged()
                }

            rvChat.adapter = chatAdapter
            chatAdapter.notifyDataSetChanged()
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