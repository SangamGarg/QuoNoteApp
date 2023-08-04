package com.sangam.quonote.explore

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.sangam.quonote.R
import com.squareup.picasso.Picasso

class MyAdapterImage(var list: List<PexelsPhoto> ) :
    Adapter<MyAdapterImage.MyViewHolder>() {
    private lateinit var mlistener: OnItemClickListener
    interface OnItemClickListener {
        fun onItemClick(photoUrl: String)
    }
    fun setOnItemClickListener(listener: OnItemClickListener){
        mlistener=listener
    }

    inner class MyViewHolder(itemView: View ,listener: OnItemClickListener) : ViewHolder(itemView) {
        val imageView = itemView.findViewById<ImageView>(R.id.imgFromApi)
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val photoUrl = list[position].src.original
                    listener.onItemClick(photoUrl)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_view, parent, false)
        return MyViewHolder(view,mlistener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val photo = list[position].src.tiny
        Picasso.get().load(photo).into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return list.size
    }
}