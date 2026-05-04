package com.example.kalah

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView

class AvatarAdapter(
    private val context: Context,
    private val avatars: List<Int>,
    private val selectedAvatar: Int
) : BaseAdapter() {

    override fun getCount(): Int = avatars.size

    override fun getItem(position: Int): Any = avatars[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_avatar, parent, false)

        val imageView = view.findViewById<ImageView>(R.id.iv_avatar)
        imageView.setImageResource(avatars[position])

        // Подсветка выбранного аватара
        if (avatars[position] == selectedAvatar) {
            imageView.setBackgroundResource(R.drawable.avatar_selected_border)
        } else {
            imageView.background = null
        }

        return view
    }
}