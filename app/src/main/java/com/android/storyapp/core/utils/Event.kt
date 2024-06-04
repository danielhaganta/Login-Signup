package com.android.storyapp.core.utils

open class Event<out T>(private val content: T) {

    private var hasBeenHandled = false

    // This function will check if the message has been shown before.
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    fun peekContent(): T = content
}