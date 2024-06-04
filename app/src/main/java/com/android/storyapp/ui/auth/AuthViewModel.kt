package com.android.storyapp.ui.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.storyapp.core.utils.Event
import com.android.storyapp.core.utils.Preferences
import com.android.storyapp.data.responses.LoginResponse
import com.android.storyapp.data.responses.RegisterResponse
import com.android.storyapp.data.services.ApiService
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val api: ApiService,
    private val preferences: Preferences
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> = _isError

    private val _responseMessage = MutableLiveData<Event<String>>()
    val responseMessage: LiveData<Event<String>> = _responseMessage


    fun registerNewUser(name: String, email: String, password: String) {
        _isLoading.value = true
        val client = api.registerNewUser(name, email, password)
        client.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(
                call: Call<RegisterResponse>,
                response: Response<RegisterResponse>
            ) {
                _isLoading.value = false
                _isError.value = !response.isSuccessful

                if (response.isSuccessful) {
                    _responseMessage.value = Event(response.body()?.message.toString())
                } else {
                    _responseMessage.value = Event(response.message())

                    try {
                        val responseBody = Gson().fromJson(response.errorBody()?.charStream(), RegisterResponse::class.java)

                        Log.e(TAG, "response.errorBody()::" + responseBody.error.toString())
                        Log.e(TAG, "response.errorBody()::" + responseBody.message)

                        _responseMessage.value = Event(responseBody.message)
                    } catch (e: Exception){
                        Log.e(TAG, e.message.toString())
                    }
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                Log.e("$TAG(onFail)", t.message.toString())
                _isLoading.value = false
                _isError.value = true
                _responseMessage.value = Event(t.message.toString())
            }

        })
    }

    fun loginUser(email: String, password: String) {
        _isLoading.value = true
        val client = api.loginUser(email, password)
        client.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                _isLoading.value = false
                _isError.value = !response.isSuccessful

                Log.e("$TAG(Body)", response.body().toString())
                Log.e(TAG, response.message())

                if (response.isSuccessful) {
                    val loginResult = response.body()?.loginResult
                    _responseMessage.value = Event(response.body()?.message.toString())
                    if (loginResult != null) {
                        preferences.setValues(Preferences.USER_ID, loginResult.userId)
                        preferences.setValues(Preferences.USER_NAME, loginResult.name)
                        preferences.setValues(Preferences.USER_TOKEN, loginResult.token)
                    }
                } else {
                    _responseMessage.value = Event(response.message())

                    try {
                        val responseBody = Gson().fromJson(response.errorBody()?.charStream(), LoginResponse::class.java)

                        Log.e(TAG, "response.errorBody()::" + responseBody.error.toString())
                        Log.e(TAG, "response.errorBody()::" + responseBody.message)

                        _responseMessage.value = Event(responseBody.message)
                    } catch (e: Exception){
                        Log.e(TAG, e.message.toString())
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("$TAG(onFail)", t.message.toString())
                _responseMessage.value = Event(t.message.toString())
                _isLoading.value = false
                _isError.value = true
            }
        })
    }

    companion object {
        private const val TAG = "AuthViewModel"
    }
}