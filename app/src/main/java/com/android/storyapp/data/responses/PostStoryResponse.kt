package com.android.storyapp.data.responses

import com.google.gson.annotations.SerializedName

data class PostStoryResponse(

    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String
)