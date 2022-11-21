package com.amazing.stamp.pages.chat

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.amazing.stamp.adapter.ChatRoomAdapter
import com.amazing.stamp.models.ChatRoomModel
import com.amazing.stamp.utils.Constants
import com.amazing.stamp.utils.FirebaseConstants
import com.example.stamp.R
import com.example.stamp.databinding.ActivityChatHomeBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatHomeActivity : AppCompatActivity() {
    private val binding by lazy { ActivityChatHomeBinding.inflate(layoutInflater) }
    private val fireStore by lazy { Firebase.firestore }
    private val auth by lazy { Firebase.auth }
    private val chatRoomModels = ArrayList<ChatRoomModel>()
    private val chatRoomIds = ArrayList<String>()
    private val chatRoomAdapter by lazy { ChatRoomAdapter(applicationContext, chatRoomModels) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarRegister)
        supportActionBar?.run {
            // 앱 바 뒤로가기 버튼 설정
            setDisplayHomeAsUpEnabled(true)
        }

        binding.run {
            btnChatAdd.setOnClickListener {
                val newChatDialog = Dialog(this@ChatHomeActivity)
                newChatDialog.setContentView(R.layout.dialog_new_chat)
                newChatDialog.show()

                newChatDialog.findViewById<TextView>(R.id.tv_item_new_chat).setOnClickListener {
                    newChatDialog.dismiss()
                    val intent = Intent(this@ChatHomeActivity, ChatAddActivity::class.java)
                    startActivity(intent)
                }
                newChatDialog.findViewById<TextView>(R.id.tv_item_search_chat).setOnClickListener {
                    newChatDialog.dismiss()
                    val intent = Intent(this@ChatHomeActivity, ChatSearchActivity::class.java)
                    startActivity(intent)
                }
            }

            CoroutineScope(Dispatchers.Main).launch {
                getChatRoomList()
            }
        }
    }

    private suspend fun getChatRoomList() {
        val result = fireStore.collection(FirebaseConstants.COLLECTION_CHAT)
            .whereArrayContains(FirebaseConstants.CHAT_FIELD_USERS, auth.currentUser!!.uid)
            .get().await()

        result.forEach {
            chatRoomIds.add(it.id)
            chatRoomModels.add(it.toObject())
        }
        val dividerItemDecoration = DividerItemDecoration(applicationContext, LinearLayoutManager(applicationContext).orientation)
        binding.rvChatRooms.addItemDecoration(dividerItemDecoration)
        binding.rvChatRooms.adapter = chatRoomAdapter
        chatRoomAdapter.notifyDataSetChanged()

        chatRoomAdapter.onChatClickListener = object : ChatRoomAdapter.OnChatClickListener {
            override fun onChatClick(position: Int) {
                val intent = Intent(this@ChatHomeActivity, ChatActivity::class.java)
                intent.putExtra(Constants.INTENT_EXTRA_CHAT_ID, chatRoomIds[position])
                startActivity(intent)
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