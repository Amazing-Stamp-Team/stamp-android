package com.amazing.stamp.adapter

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.stamp.R

class PostImageAdapter(val context: Context, private val imageUris: ArrayList<Uri>) :
    RecyclerView.Adapter<PostImageAdapter.Holder>() {

    interface OnImageRemoveClickListener {
        fun onRemove(position: Int)
    }

    lateinit var onImageRemoveClickListener: OnImageRemoveClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.item_post_image, parent, false)

        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.run {
            try {
                Glide.with(context).load(imageUris[position]).into(iv_item_post_image)
                iv_item_post_image.clipToOutline = true
            } catch (e: Exception) {
                e.printStackTrace()
            }

            iv_item_post_remove.setOnClickListener {
                onImageRemoveClickListener.onRemove(position)
            }

//            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUris[position])
//            iv_item_post_image.setImageBitmap(bitmap)
//            iv_item_post_image.clipToOutline = true
//            iv_item_post_remove.setOnClickListener {
//                onImageRemoveClickListener.onRemove(position)
//            }
        }
    }

    override fun getItemCount(): Int {
        return imageUris.size
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var iv_item_post_image = itemView.findViewById<ImageView>(R.id.iv_item_post_image)!!
        var iv_item_post_remove = itemView.findViewById<ImageView>(R.id.iv_item_post_remove)
    }
}