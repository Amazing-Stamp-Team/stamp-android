package com.amazing.stamp.utils

import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.loader.content.CursorLoader
import com.google.firebase.Timestamp


object Utils {
    val sliderDateFormat = SimpleDateFormat("yyyy/MM/dd")

    fun showShortToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun parseTimeStampToStringDate(timestamp: Timestamp?): String {
        return if (timestamp == null) ""
        else SimpleDateFormat("yyyy/MM/dd").format(timestamp.seconds * 1000)
    }

    // uri 절대경로 가져오기
    fun getPath(context: Context, uri: Uri): String? {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursorLoader = CursorLoader(context, uri, proj, null, null, null)
        val cursor = cursorLoader.loadInBackground()!!
        val index: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(index)
    }

    fun getRealPathFromURI(activity: Activity, contentURI: Uri): String? {
        val result: String?
        val cursor: Cursor? = activity.contentResolver.query(contentURI, null, null, null, null)
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.path
        } else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            result = cursor.getString(idx)
            cursor.close()
        }
        return result
    }

}