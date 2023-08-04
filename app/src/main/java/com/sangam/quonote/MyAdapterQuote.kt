package com.sangam.quonote

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter


class MyAdapterQuote(val context: Context, val list: List<UserQuoteDataClass>) :
    Adapter<MyAdapterQuote.MyViewHolder>() {
    lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(quote: String? = null)
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        mListener = listener
    }

    class MyViewHolder(itemView: View, listener: onItemClickListener) :
        RecyclerView.ViewHolder(itemView) {
        val textView = itemView.findViewById<TextView>(R.id.txtQuote)

        init {
            itemView.setOnClickListener {
                val name = textView.text.toString()
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(name)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_view_2, parent, false)
        return MyViewHolder(view, mListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.textView.text = list[position].quote
    }

    override fun getItemCount(): Int {
        return list.size
    }
}