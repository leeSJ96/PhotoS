package com.sjkorea.photos.recyclerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sjkorea.photos.App
import com.sjkorea.photos.R
import com.sjkorea.photos.model.Photo
import kotlinx.android.synthetic.main.layout_photo_item.view.*

class PhotoGridRecyeclerViewAdapter : RecyclerView.Adapter<PhotoGridRecyeclerViewAdapter.PhotoItemViewHolder>() {

    private var photoList = ArrayList<Photo>()


    inner class PhotoItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        // 뷰들을 가져온다.
        private val photoImageView = itemView.photo_image
        private val photoCreateAtText = itemView.created_at_text
        private val photoLikesCountText = itemView.likes_count_text

        fun bindWithView(photoItem: Photo){
            photoCreateAtText.text = photoItem.createdAt
            photoLikesCountText.text = photoItem.likesCount.toString()

            Glide.with(App.instance)
                .load(photoItem.thumbnail)
                .placeholder(R.drawable.ic_baseline_insert_photo_24)
                .into(photoImageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoItemViewHolder {
        val photoItemViewHolder = PhotoItemViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.layout_photo_item, parent, false))

        return photoItemViewHolder

    }

    override fun onBindViewHolder(holder: PhotoItemViewHolder, position: Int) {
        holder.bindWithView(this.photoList[position])


    }

    override fun getItemCount(): Int {
        return this.photoList.size
    }

    fun submitList(photoList: ArrayList<Photo>){
        this.photoList = photoList
    }
}