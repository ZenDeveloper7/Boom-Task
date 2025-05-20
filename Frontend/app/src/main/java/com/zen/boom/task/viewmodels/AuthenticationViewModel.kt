package com.zen.boom.task.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.zen.boom.task.network.Resource
import com.zen.boom.task.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthenticationViewModel : ViewModel() {

    private val _loginMutableStateFlow = MutableStateFlow<Resource<JsonObject>>(Resource.Idle())
    val loginMutableStateFlow = _loginMutableStateFlow

    private val _registerMutableStateFlow = MutableStateFlow<Resource<JsonObject>>(Resource.Idle())
    val registerMutableStateFlow = _registerMutableStateFlow

    fun register(email: String, password: String) {
        _registerMutableStateFlow.value = Resource.Loading()
        viewModelScope.launch {
            val body = JsonObject()
            body.addProperty("email", email)
            body.addProperty("password", password)
            RetrofitClient.apiService.register(body).enqueue(object : Callback<JsonObject> {
                override fun onResponse(
                    call: Call<JsonObject?>,
                    response: Response<JsonObject?>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        if (null != data) {
                            _registerMutableStateFlow.value = Resource.Success(data)
                        } else {
                            _registerMutableStateFlow.value = Resource.Error("Registration failed")
                        }
                    } else {
                        _registerMutableStateFlow.value = Resource.Error("Registration failed")
                    }
                }

                override fun onFailure(
                    call: Call<JsonObject?>,
                    t: Throwable
                ) {
                    _registerMutableStateFlow.value = Resource.Error("Registration failed")
                }
            })
        }
    }


    fun login(email: String, password: String) {
        _loginMutableStateFlow.value = Resource.Loading()
        viewModelScope.launch {
            val body = JsonObject()
            body.addProperty("email", email)
            body.addProperty("password", password)
            RetrofitClient.apiService.login(body).enqueue(object : Callback<JsonObject> {
                override fun onResponse(
                    call: Call<JsonObject?>,
                    response: Response<JsonObject?>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        if (data != null) {
                            _loginMutableStateFlow.value = Resource.Success(data)
                        } else {
                            _loginMutableStateFlow.value = Resource.Error("Login failed")
                        }
                    } else {
                        _loginMutableStateFlow.value = Resource.Error("Login failed")
                    }
                }

                override fun onFailure(
                    call: Call<JsonObject?>,
                    t: Throwable
                ) {
                    _loginMutableStateFlow.value = Resource.Error("Login failed")
                }
            })
        }
    }
}