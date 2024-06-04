package com.android.storyapp.core.utils

import androidx.recyclerview.widget.DiffUtil
import com.android.storyapp.data.models.StoryEntity

class StoryDiffUtil(
    private val oldList: List<StoryEntity>,
    private val newList: List<StoryEntity>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return when {
            oldList[oldItemPosition].id != newList[newItemPosition].id -> false
            oldList[oldItemPosition].photoUrl != newList[newItemPosition].photoUrl -> false
            oldList[oldItemPosition].name != newList[newItemPosition].name -> false
            oldList[oldItemPosition].createdAt != newList[newItemPosition].createdAt -> false
            oldList[oldItemPosition].description != newList[newItemPosition].description -> false
            oldList[oldItemPosition].lat != newList[newItemPosition].lat -> false
            oldList[oldItemPosition].lon != newList[newItemPosition].lon -> false
            else -> true
        }
    }
}