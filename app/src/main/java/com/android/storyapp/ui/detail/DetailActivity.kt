package com.android.storyapp.ui.detail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.storyapp.data.models.StoryEntity
import com.android.storyapp.databinding.ActivityDetailBinding
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior

class DetailActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityDetailBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val entity = intent.getParcelableExtra<StoryEntity>(EXTRA_STORY)
        setData(entity)

        BottomSheetBehavior.from(binding.bottomSheet).apply {
            peekHeight = 100
            this.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        binding.apply {
            btnBack.setOnClickListener { onBackPressed() }
        }
    }

    private fun setData(entity: StoryEntity?) {
        if (entity != null){
            binding.apply {
                tvUserName.text = entity.name
                tvStoryDesc.text = entity.description

                Glide.with(this@DetailActivity)
                    .load(entity.photoUrl)
                    .into(ivStoryPhoto)
            }
        }
    }

    companion object {
        const val EXTRA_STORY = "extra_story"
    }
}