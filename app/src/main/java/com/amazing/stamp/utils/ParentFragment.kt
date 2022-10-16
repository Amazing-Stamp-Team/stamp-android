package com.amazing.stamp.utils

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.stamp.R

open class ParentFragment : Fragment() {
    var progressDialog: Dialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_parent, container, false)
    }

    fun showProgress(activity: Activity, text: String) {
        if (progressDialog != null) progressDialog = null

        progressDialog = Dialog(activity)
        progressDialog?.setContentView(R.layout.dialog_progress)
        progressDialog?.findViewById<TextView>(R.id.tv_dialog_progress)?.text = text
        progressDialog?.setCancelable(false) // 다이얼로그 외 터치 시 취소 막음
        progressDialog?.show()
    }

    fun hideProgress() {
        if (progressDialog != null) {
            progressDialog?.dismiss()
        }
        progressDialog = null
    }
}