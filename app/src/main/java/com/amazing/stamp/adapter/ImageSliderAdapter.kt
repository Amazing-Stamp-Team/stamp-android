package com.amazing.stamp.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amazing.stamp.models.StamfPickModel
import com.amazing.stamp.utils.FirebaseConstants
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.example.stamp.R
import com.example.stamp.databinding.ItemImgSliderBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class ImageSliderAdapter(
    val context: Context,
    val models: ArrayList<StamfPickModel>
) :
    RecyclerView.Adapter<ImageSliderAdapter.MyViewHolder>() {

    private val storage by lazy { Firebase.storage }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_img_slider, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = models[position]

        holder.binding.run {
            tvItemArea.text = model.area
            tvItemName.text = model.name

            try {
                storage.getReference(FirebaseConstants.STORAGE_STAMF_PICK)
                    .child("${model.name}.JPG").downloadUrl.addOnSuccessListener {
                        Glide.with(context).load(it).into(ivItemSlider)
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun getItemCount(): Int {
        return models.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemImgSliderBinding.bind(itemView)
    }
}