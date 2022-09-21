package com.example.stamp.adapter

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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.example.stamp.R


class ImageSliderAdapter(
    context: Context,
    sliderImage: Array<Drawable>,
    sliderDescription: Array<String>
) :
    RecyclerView.Adapter<ImageSliderAdapter.MyViewHolder>() {
    private val context: Context
    private val sliderImage: Array<Drawable>
    private val sliderDescription: Array<String>
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_img_slider, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindSliderImage(sliderImage[position])
        holder.bindText(sliderDescription[position])
    }

    override fun getItemCount(): Int {
        return sliderImage.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mImageView: ImageView
        private val mTextView: TextView

        fun bindText(text: String) {
            mTextView.text = text
        }

        fun bindSliderImage(drawable: Drawable?) {
            // Glide.with(context).load(imageURL).into(mImageView)

            // 검은색 박스 씌우기
            mImageView.setColorFilter(Color.parseColor("#A5A5A5"), PorterDuff.Mode.MULTIPLY)

            mImageView.setImageDrawable(drawable)
        }

        init {
            mImageView = itemView.findViewById(R.id.iv_item_slider)
            mTextView = itemView.findViewById(R.id.tv_item_slider)
        }
    }

    init {
        this.context = context
        this.sliderDescription = sliderDescription
        this.sliderImage = sliderImage
    }
}